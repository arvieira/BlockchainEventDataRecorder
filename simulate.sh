#!/bin/bash



# Definindo as variáveis globais dos parâmetros da simulação
duration=0
number_of_vehicle=0
tps=0
repeat=0

# Variáveis do MOSAIC
mosaic_path="./first_stage/MOSAIC21/"
mosaic_tps_param_file="param.json"
mosaic_duration_param_file="scenarios/MosaicBroker/scenario_config.json"
mosaic_number_vehicles_directory="scenarios/MosaicBroker/mapping/"

# Variáveis da simulação
tmp_dir="tmp/"
output_dir="output/"
exp_name=""

# Variáveis do Iroha e DispatcherBroker
sleep_time=15
containers_path="./second_stage/Iroha/"
dispatcher_path="second_stage/DispatcherBroker/"

# Variáveis do Transactioncounter
transaction_counter_path="./third_stage/TransactionCounter/"



# Função para leitura dos parâmetros
handle_params () {
  # Enquanto tiver parâmetros, ele irá entrar no case e ir fazendo o parser
  while getopts d:n:t:r: flag
  do
    case "${flag}" in
      d) duration=${OPTARG};;
      n) number_of_vehicle=${OPTARG};;
      t) tps=${OPTARG};;
      r) repeat=${OPTARG};;
      # Caso seja passado um parâmetro errado ele imprime o manual
      *) echo "Usage: ./simulate.sh -d <duration> -n <number_of_vehicle - 3, 5, 7, or 9> -t <tps> -r <number_of_executions>" && exit;;
    esac
  done

  # Se todos os parâmetros não foram definidos com êxito, ele imprime o manual e sai
  if [ -z "$duration" ] || [ -z "$number_of_vehicle" ] || [ -z "$tps" ] || [ -z "$repeat" ]; then
    echo "You need to fill all parameters."
    echo "Usage: ./simulate.sh -d <duration> -n <number_of_vehicle - 3, 4, 5, 6, 7, 8 or 9> -t <tps> -r <number_of_executions>"
    exit
  elif [ "$number_of_vehicle" -ne 3 ] && [ "$number_of_vehicle" -ne 4 ] && [ "$number_of_vehicle" -ne 5 ] && [ "$number_of_vehicle" -ne 6 ] && [ "$number_of_vehicle" -ne 7 ] && [ "$number_of_vehicle" -ne 8 ] && [ "$number_of_vehicle" -ne 9 ]; then
    echo "Number of vehicles only can be 3, 4, 5, 7 or 9."
    echo "Usage: ./simulate.sh -d <duration> -n <number_of_vehicle - 3, 4, 5, 6, 7, 8 or 9> -t <tps> -r <number_of_executions>"
    exit
  elif [ "$repeat" -le 0 ]; then
    echo "Please insert a positive value for number of execution."
    echo "Usage: ./simulate.sh -d <duration> -n <number_of_vehicle - 3, 4, 5, 6, 7, 8 or 9> -t <tps> -r <number_of_executions>"
    exit
  else
    exp_name="${number_of_vehicle}veh_${duration}s_${tps}TPS_${repeat}times"
  fi

  if [ -f "${output_dir}${exp_name}.json" ]; then
    echo "The output file ${output_dir}${exp_name}.txt of this experiment already exists. Overwrite it? [y/n]"
    read -r -n1 option
    if [ "$option" != "y" ] && [ "$option" != "Y" ]; then
      exit
    fi
  fi

  echo "Starting $duration seconds simulation with $number_of_vehicle vehicles and $tps tps per vehicle."
  echo -e "Repeating $repeat times.\n\n"
}



# Função responsável por executar a simulação do MOSAIC
run_mosaic () {
  # Calculando o interval pelo número de tps selecionado
  TPS=$(echo "1000000000/$tps" | bc)

  # Escrevendo o arquivo de parâmetros da aplicação do MOSAIC
  echo "{\"natural\": false, \"unit\": 1, \"interval\": $TPS}" > "${mosaic_path}${mosaic_tps_param_file}"
  echo "{ \"simulation\": { \"id\": \"BlockchainVehicleUnit\", \"duration\": \"${duration}s\",\
   \"randomSeed\": 268965854, \"projection\": { \"centerCoordinates\": { \"latitude\": -22.98516,\
    \"longitude\": -43.20605 }, \"cartesianOffset\": { \"x\": -682964.78, \"y\": -7456682.16 }},\
     \"network\": { \"netMask\": \"255.255.0.0\", \"vehicleNet\": \"10.1.0.0\", \"rsuNet\": \"10.2.0.0\",\
      \"tlNet\": \"10.3.0.0\", \"csNet\": \"10.4.0.0\", \"serverNet\": \"10.5.0.0\", \"tmcNet\": \"10.6.0.0\" }},\
       \"federates\": { \"application\": true, \"cell\": false, \"environment\": true, \"sns\": true,\
        \"ns3\": false, \"omnetpp\": false, \"output\": true, \"sumo\": true }}" > "${mosaic_path}${mosaic_duration_param_file}"
  cp "${mosaic_path}${mosaic_number_vehicles_directory}mapping_files/mapping_config_${number_of_vehicle}.json" "${mosaic_path}${mosaic_number_vehicles_directory}/mapping_config.json"

  # Rodando a simulação no mosaic
  old_dir=$(pwd)
  cd ${mosaic_path} || (echo "Mosaic simulator not founded." && exit)
  ./mosaic.sh -s MosaicBroker
  cd "$old_dir" || exit

  # Renomeando a pasta de logs do experimento
  mv "${mosaic_path}logs/$(ls "${mosaic_path}logs" | grep log)" "${tmp_dir}${exp_name}"
}


# Função para limpar o diretório gerado e colocar no formato que o DispatcherBroker entende
rename_mosaic_output () {
  echo "Cleaning and formating output logs directory for DispatcherBroker..."

  # Salvando o diretório original para retornar depois
  old_dir=$(pwd)

  # Tratando os arquivos de log gerados para o DispatcherBroker entender
  cd "${tmp_dir}${exp_name}" || exit
  mkdir temp
  mv ./* temp 2> /dev/null
  cd temp/apps || exit

  for veh in ./*
  do
          cp "${veh}/IntelligentVehicleWitnessMerge.log" "../../${veh}.log"
  done

  # Removendo lixos e retornando ao diretório inicial
  cd ../../
  rm -r temp/
  cd "$old_dir" || exit
}


# Gerenciador do primeiro estágio
first_stage () {
  echo "##################################################################"
  echo "#                    Starting first stage                        #"
  echo "##################################################################"
  run_mosaic
  rename_mosaic_output
  echo "##################################################################"
  echo "#                    First stage completed                       #"
  echo "##################################################################"
}



# Copia os blocos genesis para os veículos
copy_files () {
  for i in $(seq 0 $((number_of_vehicle - 1)));
  do
    cp "second_stage/Iroha/conf/default_files/genesis/genesis${number_of_vehicle}veh.block" "second_stage/Iroha/conf/veh_${i}/genesis.block"
  done
}


# Inicia os containers e executa o segundo estágio da simulação
start_containers () {
  # Iniciando containers
  old_dir=$(pwd)
  cd ${containers_path} || exit
  vehicles="docker-compose up -d"
  for i in $(seq 0 $((number_of_vehicle - 1)));
  do
    vehicles="${vehicles} veh_${i}";
  done
  $vehicles &
  sleep "${sleep_time}"

  # Executando o segundo estágio da simulação
  export PYTHONPATH="${old_dir}"
  time python3 "${old_dir}/${dispatcher_path}main.py" -i "${old_dir}/${tmp_dir}${exp_name}/"
  # Essa linha roda com --20 de tempo real para tentar fazer a simulação no tempo, mas não funcionou
  # Ela está sem variáveis de ambiente pq não estou conseguindo levar para o root. Tem que adaptar para cada execução
  # sudo -E bash -c 'export PYTHONPATH="/home/andre/PycharmProjects/ShortChainTransientBlockchain/" && nice --20 time python3 "/home/andre/PycharmProjects/ShortChainTransientBlockchain/second_stage/DispatcherBroker/main.py" -i "/home/andre/PycharmProjects/ShortChainTransientBlockchain/tmp/5veh_30s_100TPS_1times/"'
  docker-compose down
  docker volume rm iroha_psql_storage
  cp -r "volumes" "${old_dir}/${tmp_dir}/${exp_name}"
  sudo rm -rf volumes/veh_*/*
  mv "Total.txt" "Queues.txt" "${old_dir}/${tmp_dir}/${exp_name}/volumes/"
  cd "$old_dir" || exit
}


# Prepara a saída do segundo estágio para o contador de transações
rename_containers_output () {
  # Salvando o diretório original e navegando para o diretório dos volumes dos containers
  old_dir=$(pwd)
  cd "${tmp_dir}/${exp_name}/volumes" || exit

  # Após entrar em cada volume dos containers, adiciona a estensão json nos blocos para facilitar a leitura posterior
  for i in $(seq 0 $((number_of_vehicle - 1)));
  do
    cd "veh_${i}" || exit
      if [[ $(ls | wc -l) -ne 0 ]]; then
              for f in *; do mv "$f" "$f.json"; done
      else
              echo "Empty: $veh"
      fi
      cd ../
  done

  # Retornando para o diretório inicial
  cd "${old_dir}" || exit
}


# Gerenciador do segundo estágio
second_stage () {
  echo "##################################################################"
  echo "#                    Starting second stage                       #"
  echo "##################################################################"
  copy_files
  start_containers
  rename_containers_output
  echo "##################################################################"
  echo "#                    Second stage completed                      #"
  echo "##################################################################"
}



# Executa o contador de transações para exibir os resultados
run_transaction_counter () {
  python3 "${transaction_counter_path}main.py" "${exp_name}" "${tmp_dir}/${exp_name}/volumes" "${output_dir}" "$1"
}


# Limpa o diretório tmp para uma nova execução
clean () {
  rm -rf tmp/*
}


# Gerenciador do terceiro estágio
third_stage () {
  echo "##################################################################"
  echo "#                    Starting third stage                        #"
  echo "##################################################################"
  run_transaction_counter "$1"
  clean
  echo "##################################################################"
  echo "#                    Third stage completed                       #"
  echo "##################################################################"
}



# Chamando a função de tratamento de parâmetros
# o $@ é o vator com todos os parâmetros passados para o script
handle_params "$@"
run=1
while [ $run -le "$repeat" ]
do
  echo "##################################################################"
  echo "#                    Execution number ${run}:                    #"
  echo "##################################################################"

  # Executando o primeiro estágio da simulação
  first_stage

  # Executando o segundo estágio da simulação
  second_stage

  # Executando o terceiro estágio da simulação para contagem dos resultados
  third_stage $run

  # Atualizando o contador
  run=$(( run + 1 ))
done

sed -i '$s/}$/}]/' "${output_dir}${exp_name}.json"