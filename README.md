# Mosaic_Broker_Iroha

# [MOSAIC](https://www.eclipse.org/mosaic/)
O MOSAIC é um framework de simuladores que se encaixaria na classificação
de simulador de VANET por possuir simulação de redes e mobilidade em um 
local só. No entanto, por simular outros fatores como bateria de veículos
elétricos, consumo, redes de celular etc, ele é descrito como um framework
de simulação.

A estrutura do simulador é composta for uma infraestrutura de execução da
simulação que provê o gerenciamento dos federados, do tempo e das interações.
Um federado é o nome dado a um simulador que se queira plugar na infraestrutura.
O gerenciamento de federados é realizado com a criação de um embaixador para
cada simulador que se vai utilizar.
Logo, um novo simulador fala com o embaixador que foi desenvolvido para ele 
e consegue utilizar a infraestrutura subjacente.

A título de exemplo se tem os seguintes embaixadores já implementados:
* Simulador de Aplicação
  * Safety App
  * Traffic App
  * Aqui está plugada a aplicação de clustering desenvolvida
* Comunicação
  * OMNeT++
  * ns-3
  * MOSAIC SNS (Utilizado para esta simulação)
  * MOSAIC Cell
* Mobilidade
  * Eclipse SUMO (Utilizado para essa simulação)
  * PHABMACS

Para o desenvolvimento de um novo embaixador para um federado, a documentação
está [neste link](https://www.eclipse.org/mosaic/docs/extending_mosaic/simulator_coupling/).
Como trabalho futuro, pode ser desenvolvido um embaixador para dialogar com o 
broker e fazer a simulação em um único passo, sem ter que gerar arquivos de
logs e posteriormente executar uma segunda simulação.

## Instalação
O MOSAIC é desenvolvido em Java, não necessita de uma instalação muito complicada.
Basta realizar o download do framework [neste link](https://www.eclipse.org/mosaic/download/),
descompactar e já está tudo pronto. No link indicado, vão ser encontrados os seguintes
arquivos:
* eclipse-mosaic-XX.X.xip (Framework propriamente dito)
* ns3-federate-XX.X.zip (Federado do simulador de rede ns3, só precisa dele se
for utilizar o ns3 para a simulação de redes)
* omnetpp-federate-XX.X.zip (Federadod do simulador de rede omnet++, idem ao 
caso anterior)
* scenario-convert-XX.X.zip (Arquivo do tipo jar que realiza operações para 
montagem de um mapa em um cenário. Só é necessário se for construir ou alterar
mapas de cenários)

Após descompactar o framework, como para a simulação de mobilidade está sendo
utilizado o simulador SUMO, é necessário o download do mesmo e instalação 
independente do MOSAIC. Na minha instalação, utilizo o SUMO versão 1.7.0. 
Antigamente, o simulador VSimRTI tinha restrições da versão do SUMO que poderia
ser utilizada. No entanto, desconheço se há a mesma restrição para o MOSAIC.
O simulador SUMO pode ser obtido [neste link](https://sumo.dlr.de/docs/Downloads.php) e 
sua instalação segue o arquivo README.md contido no arquivo baixado. Resumindo,
a instalação do SUMO passa pelos seguintes passos:
* sudo apt-get install cmake python g++ libxerces-c-dev libfox-1.6-dev libgdal-dev libproj-dev libgl2ps-dev swig
* Entrar no diretório extraído
* export SUMO_HOME="$PWD"
* mkdir build/cmake-build && cd build/cmake-build
* cmake ../..
* make -j$(nproc)
* Adicionar o SUMO_HOME no .profile do usuário
  * export SUMO_HOME="diretório extraído"

## Cenários
Ao desempactor o framework do MOSAIC serão obtidos uma série de arquivos e 
diretórios. Os mais importantes são os seguintes:
* etc (Guarda o arquivo runtime.json que têm as configurações do framework. 
Inclusive, a alteração entre aparecer a interface do SUMO ou não é realizada
nesse arquivo trocando as linhas abaixo)
```
"classname": "org.eclipse.mosaic.fed.sumo.ambassador.SumoAmbassador",
por
"classname": "org.eclipse.mosaic.fed.sumo.ambassador.SumoGuiAmbassador",
```
* logs (Diretório aonde ficarão os diretórios com a estrutura de logs gerada
após uma simulação ser executada. Os arquivos interessantes para esse experimento
estão na seguinte localização logs/log-XXXXXXXX-XXXXXX-BlockchainVehicleUnit/apps/veh_X/IntelligentVehicleWitnessNoMerge.log)
* mosaic.sh (Script para execução de uma simulação)
* scenarios (Diretório que guarda cada um dos cenários configurados)

A execução de uma simulação no framework MOSAIC é realizada por cenários. Um 
cenário possui arquivos referentes a aplicação que será executada em cada um
dos elementos (veículos, RSUs, sinais de trânsito e outros), referentes ao 
consumo de bateria, uso da rede celular, condições ambientais como neve,
rotas e tráfego, simulador de rede ns3/omnet++/sns e do simulador de mobilidade
sumo. Cada conjunto de arquivos estão separados nos seus respectivos diretórios:
* application (Aplicação)
  * application_config.json (Arquivo de configuração da aplicação)
  * mapa.db (Arquivo de banco de dados com informações do mapa)
  * aplicação.jar (Arquivo jar da aplicação criada que será executada)
  * OutrasLibs.jar (Toda biblioteca que utilizarmos a mais na aplicação precisa
  ter seu jar adicionado aqui)
* battery (Bateria)
  * battery_config.json (Arquivo de configuração da bateria)
* cell (Rede de celular)
  * cell_config.json (Arquivos de configuração da rede de celular)
  * network.json (Arquivos de configuração da rede de celular)
  * regions.json (Arquivos de configuração da rede de celular)
* environment (Meio ambiente)
  * environment_config.json (Arquivos de configuração do meio ambiente)
* mapping (Mobilidade, rotas tráfego)
  * mapping_config.json (Arquivo aonde são configurados os tipos de veículos,
  as rotas que serão usadas e os fluxos)
* ns3 (Simulador de redes ns3)
  * configTechnologies.xml  
  * confWifi.xml  
  * ns3_config.json  
  * ns3_federate_config.xml
* omnetpp (Simulador de redes omnet++)
  * omnetpp_config.json  
  * omnetpp.ini
* sns (Simulador de redes SNS)
  * sns_config.json
* sumo (Simulador de mobilidade SUMO)
  * mapa.con.xml (XML com as conexões do mapa)
  * mapa.edg.xml (XML com as arestas do mapa)
  * mapa.net.xml (XML com a rede do mapa)
  * mapa.nod.xml (XML com os nós do mapa)
  * mapa.rou.xml (XML com as rotas do mapa)
  * mapa.sumocfg (Arquivo de configuração do mapa para o SUMO)

### Criando Um Cenário
Para criar um cenário, deve-se seguir uma sequência de passos:
* Criando um mapa físico
* Criando rotas no mapa
* Copiando arquivos de mapa
* Configurando a simulação
* Criando a aplicação

#### Criando Um Mapa Físico
O mapa físico pode ser criado de diversas formas. Podem ser utilizadas aplicações
embutidas no SUMO para a criação da rede, redes geradas pelo SUMO como o 
modelo Manhattan ou spider grid, redes baseadas em mapas reais que é o que 
faremos aqui. No site [Open Street Map](http://openstreetmap.org) há um mapa mundial
que é mantido pela comunidade. Neste mapa, são colocadas as mais diversas informações
sobre as cidades, incluindo sinalizações de trânsito e até mesmo dados sobre
as construções. Inclusive, caso haja algum ponto errado no mapa da região 
desejada, este pode ser consertado no próprio site, basta fazer um cadastro.

Para a exportação do mapa, basta entrar no site, clicar em exportar, em seguida
em selecionar outra area manualmente. Selecione a área do mapa desejada e clique
em exportar. Será gerado um arquivo na extensão osm. Os arquivos osm são 
consumidos pelo scenario-convert-XX.X.jar obtido no site de download do framework MOSAIC.
A utilização do jar é com a seguinte linha de comando:
```
java -jar scenario-convert-XX.X.jar --osm2sumo -i mapa.osm
```
Como retorno, serão criados os diversos arquivos acerca da rede que se deseja
próprios para o uso no sumo. 

Observação: Caso se deseje ajeitar algum ponto errado no mapa com o netedit
fornecido pelo SUMO, deve-se fazer antes de adicionar as rotas para que não
sejam utilizados pontos que depois possam ser excluídos. Cabe ressaltar, que
os semáforos gerados normalmente não funcionam. Logo, deve-se editar o arquivo
mapa.net.xml com o netedit corrigindo as junções, removendo semáforos defeituosos
e adicionando os grupos de semáforos de maneira adequada.

#### Criando Rotas no Mapa
O próximo passo é adicionar cada uma das rotas 
que se vai utilizar ao banco de dados que está sendo gerado. Para cada rota
que se deseja, se deve pegar as coordenadas GPS (latitude, longitude) dos pontos
que compõem a rota. Para simplificar essa etapa, foram pegos somente os pontos
de início e final das rotas, deixando que o binário encontrasse o melhor caminho 
entre os dois. As coordenadas GPS podem ser obtidas no próprio site que gera o
mapa clicando com o botão direito sobre o local desejado. Para adicionar a rota
na base, usa-se o comando scenario-convert-XX.X.jar:
```
java -jar scenario-convert-XX.X.jar -d mapa.db -g --route-begin-latlon "-22.98649","-43.20438" --route-end-latlon "-22.9863","-43.20797"
```

Cada adição de rota cria um arquivo novo com o nome mapa.rou-X.xml contendo a 
nova rota e as demais adicionadas anteriormente. Ao final da adição de rotas,
basta que se utilize o último arquivo gerado, renomeando para mapa.rou.xml e 
removendo os demais.

#### Copiando Arquivos de Mapa
Com todos os arquivos de mapas devidamente gerados, copiar o arquivo do banco 
de dados gerado mapa.db para o diretório application da pasta do cenário e os
arquivos a seguir para a pasta sumo do cenário:
* mapa.con.xml
* mapa.edg.xml
* mapa.net.xml
* mapa.nod.xml
* mapa.rou.xml
* mapa.sumocfg

#### Configurando a Simulação
A simulação é configurada basicamente em duas etapas. A primeira é no arquivo
scenario_config.json situado na raiz do diretório do cenário. Este arquivo
possui pontos importantes como:
* id (Nome da simulação)
* duration (Duração da simulação em segundos)
* randomSeed (Seed utilizado para aleatoriedade)
* projection (Indica as coordenadas do centro do mapa e o offset que representa
o sistema geodésico utilizado para o mapa)
* network (Configurações da rede que será utilizada na simulação)
* federates (Quais federados serão utilizados. É nesse ponto que se decide qual
simulador de rede será utilizado. Lembrando que se for selecionado o ns3 ou omnet++
é necessário fazer a instalação dos mesmos no equipamento e do embaixador no
framework).

O segundo arquivo importante fica no diretório mapping e se chama mapping_config.json.
Este arquivo define os protótipos de unidades que serão utilizadas na simulação,
incluindo a classe java que será executada no código. Ou seja, aqui é definido
o entry point do código que se está desenvolvendo. Neste mesmo arquivo, são 
definidas os veículos que participarão da simulação, indicando dados como:
* Início do fluxo
* Número da rota
* Número máximo de veículos
* Forma que os veículos serão inseridos no mapa
* Quais os tipos de veículos em cada rota pelos protótipos definidos
* ...

#### Criando a Aplicação
Para o desenvolvimento da aplicação, utiliza-se a linguagem de programação 
JAVA com o MAVEN para a obtenção das bibliotecas atualizadas de desenvolvimento.
A classe que será definida como o entry point no mapping_config.json precisa
implementar uma série de métodos que serão utilizados na simulação.
* public void onStartup() -> (Método executado quando o veículo é ligado)
* public void processEvent(Event event) -> (Método executado toda vez que um evento
agendado ocorre. Durante o desenvolvimento, pode-se agendar diversos eventos
para representar timeouts)
* public void onMessageReceived(ReceivedV2xMessage message) -> (Método executado quando
o veículo recebe uma mensagem)
* public void onShutdown() -> (Método executado quando o veículo está para
ser desligado)
* public void onVehicleUpdated(VehicleData previous, VehicleData updated) -> 
(Método executado quando há mudanças nos sensores do veículo. Terá dois conjunto
 de dados, um antes e outro depois da mudança)
* public void onAckowledgementReceived(ReceivedAcknowledgement receivedAcknowledgement) -> 
(Método executado quando o veículo recebe um ACK de uma mensagem)
* public void onCamBuilding(CamBuilder camBuilder) -> (Referente a mensagens CAM)
* public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) -> 
(Quando uma mensagem é transmitida)

Ao término da construção da aplicação, como o código está usando o MAVEN para
poder utilizar as bibliotecas necessárias, precisa utilizar o comando seguir
para compilar e gerar o jar da aplicação. Cabe ressaltar, que é necessário que
o maven esteja instalado no equipamento.
```
mvn clean install
```
O arquivo jar será criado na pasta target e deverá ser copiado para o diretório
application do cenário que está sendo criado.

## Executando uma Simulação
Durante o desenvolvimento da aplicação de clustering veicular, foi necessária
a definição de parâmetros de simulação. Para que a alteração do código não fosse
necessária a cada modificação dos parâmetros, foi criado um arquivo chamado 
param.json que possui um dicionário com os possíveis parâmetros que podem ser
configurados para cada simulação.
```
{
	"natural": false,
	"unit": 1,
	"interval": 1000000000
}
```
A chave "natural" é indica se os veículos irão enviar transações ao ledger no
momento que detetam uma mudança nos seus sensores. Caso esteja com o valor 
true, os demais parâmetros são ignorados. O parâmetro "unit" indica a unidade
que será representado o intervalo. O valor de 1 indica que a unidade será o
nanossegundo que é a unidade mínima da simulação. O parâmetro "interval" indica
a cada quantas units será enviada uma transação para o ledger.

Para a execução da simulação, utiliza-se o comando:
```
./mosaic.sh -s <Nome do diretório do cenário>
```

Todos os arquivos são gerados na pasta log. Então, caso aconteça qualquer erro,
deve-se procurar nos arquivos gerados o problema ocorrido. Do mesmo modo, caso
tudo tenha saído de maneira adequada, os resultados estarão na mesma pasta.


# Dispatcher Broker
O Dispatcher Broker é um reprodutor de simulação a partir de arquivos de logs. 
O objetivo é reproduzir os eventos ocorridos durante uma simulação em tempo real
para poder executar uma nova simulação desconectada da primeira. Ou seja, consiste
em uma forma de conectar duas simulaçõe no tempo em execuções independentes. 

Possui 3 módulos principais:
* Leitor de arquivos
* Gerenciador de eventos (Dispatcher)
* Executor de comandos (Broker)

O leitor de arquivos está desenvolvido para extrair os dados contidos nos arquivos
de logs gerados pela simulação de clustering veicular realizada pelo MOSAIC. Como
saída são disponibilizadas linhas do tempo que consistem em listas de dicionários.
Cada dicionário é um evento ocorrido durante a simulação.

O módulo gerenciador de eventos cria uma thread e uma fila para cada linha do tempo
que é adicionada a ele. As threads representam os brokers e terão a funcionalidade
de consumir os eventos que forem colocados nas filas, executando os comandos necessários
nos containers do hyperledger. A cada linha do tempo adicionada, todos os eventos são
agendados para acontecer no momento que foram registrados nos arquivos de logs. Após o 
início da simulação, cada ocorrência do evento é tratada com a adição deste à fila do 
respectivo broker.

Um executor de comandos é uma thread que monitora sua respectiva fila, consumindo os
eventos que forem inseridos em ordem e executando os comandos no ledger. Cabe ressaltar,
que o monitoramento das filas dos brokers irá indicar a carga do sistema. Ou seja, filas
grandes indicam que os eventos acontecem mais rápido do que podem ser processados no 
hyperledger. Ausência de filas indica que o sistema está sem muita carga. 

Juntamente com o executor de comandos, há uma classe chamada Hyperledger. Essa classe
possui um método para o tratamento de cada um dos eventos gerados, mapeando-os nos 
comandos necessários para o hyperledger. A implementação dessa pode ser feita
de forma difernte para os diferentes tipos de blockchain que se deseja utilizar.
O mapeamento dos eventos nas funções se encontra a seguir:
* Antes de iniciar a simulação -> initialize_ledger()
* Veículo estando em SOLE e recebendo um RESP. É quando se torna líder e um 
grupo é criado -> create_channel(leader, membro, group_id)
* Veículo estando em LEADER e recebendo RESP. É quando um novo membro foi
adicionado ao grupo -> join_channel(member, group_id)
* Veículo estando em LEADER e timeout do membro. É quando um membro sai do 
grupo -> pause_peer(member, group_id)
* Veículo estando em LEADER e realizando uma transação. -> transaction(true,
leader, group_id, transaction)
* Veículo estando em ASP e recebendo um ACK. É quando um membro começa a fazer
parte de um grupo ou retorna a um grupo -> resume_peer(member, group_id)
* Veículo estando em MEMBER e timeout do líder ->  pause_peer(member, group_id)
* Veículo estando em MEMBER e realizando transação. -> transaction(false,
member, group_id, transaction)
* Após finalizar a simulação -> finalize_ledger()





# IROHA
- Comandos úteis:
	- Acessando um container
		docker exec -it veh_X /bin/bash
	- Acessando a API para os assets do blockchain
		cd user_keys
		iroha-cli -account_name admin@test
	- Script para gerar chaves
		python3 key_gen.py
	- Ligando e desligando a blockchain
		- Estando na pasta Cenario
			- docker-compose up 
			- docker-compose up -d (para liberar o terminal)
			- docker-compose down


# Arquivos:
- docker-compose.yml
	- Arquivo com a definição de todas os containers para serem executados na rede
	- Não preciso expor as portas pq estou criando uma rede doss containers e fazendo eles se falarem pelo nome do host
- utils
	- key_gen.py
		- Script para gerar pares de chaves para usuários e para máquinas
	- transactions.py
		- Script para simular transações pela linha de comando
	- add_peer.py
		- Script para adicionar um peer pela linha de comando
	- remove_peer.py
		- Script para remover um peer pela linha de comando
- conf/veh_X
	- config.docker
		- Arquivo que carrega as configurações do nó da blockchain
		- Contém a configuração da localização do banco de dados, do diretório temporário, portas e outros tunnings
	- genesis.block
		- É o arquivo que representa o bloco gênesis da rede. 
		- Contém as seguintes informações:
			- Peers
			- Roles com as permissões, inclusive de adicionar e remover peer
			- Domains
			- Assets
			- Accounts
	- veh_X.priv e veh_X.pub
		- Arquivos com as chaves pública e privada dos nós
	- user_keys/*
		- Arquivos com as chaves dos usuários admin@test e test@test


# Usando a API da blockchain para testar
- Criando um asset, adicionando quantidade e transferindo
	- 1
	- 14, novo, test, 2
	- 1
	- 16
	- novo#test
	- 500000
	- 1
	- 5
	- admin@test
	- test@test
	- novo#test
	- 300000
	- 2
	- localhost
	- 50051
- Consultando
	- 2
	- 8
	- test@test
	- novo#test
	- 1
	- localhost
	- 50051

# Simulando adicionar e remover um peer
- Em IROHA/Cenario tem um arquivo docker-compose.yml
	- docker-compose up
	- Isso irá iniciar a rede com 4 peers
- Em IROHA/Cenario/addpeer tem outro arquivo docker-compose.yml
	- docker-compose up
	- Isso irá iniciar o quinto peer que não conseguirá fazer parte do consenso ainda. Ele inicia, exibe algumas informações e para.
- No host ir até o diretório IROHA/Cenario/utils
	- python3 add_peer.py
	- Irá adicionar o novo peer a rede já existente
- Para realizar algumas transações
	- No host ir até o diretório IROHA/Cenario/utils
	- python3 transactions.py
	- Realiza as seguintes operações:
		- create_domain_and_asset()
		- add_coin_to_admin()
		- create_account_userone()
		- transfer_coin_from_admin_to_userone()
		- userone_grants_to_admin_set_account_detail_permission()
		- set_age_to_userone()
		- get_coin_info()
		- get_account_assets()
		- get_userone_details()
- No host ir até o diretório IROHA/Cenario/utils
	- python3 remove_peer.py
	- Irá remover o novo peer da rede em execução

# Blocos gravados
- Os volumes do docker são criados na seguinte localização
	- /var/lib/docker/volumes/
- Os blocos são sequências de números que indicam o índice do bloco
- Cada bloco é um arquivo JSON
	
# Estrutura de inserção de registros
- Domain: Posso definir o domínio do experimento, por exemplo traffic
- Account: Cada veículo é um account. Exemplo: veh_0@traffic, veh_1@traffic ...
- Transaction: Conjunto de comandos aplicado de uma vez só
    - Commands: É um comando qualquer dentre a lista de possíveis
        - Add asset quantity
        - Add peer
        - Add signatory
        - Append role
        - Call engine
        - Create account
        - Create asset
        - Create domain
        - Create role		
        - Detach role
        - Grant permission
        - Remove peer
        - Remove signatory
        - Revoke permission
        - Set account detail
        - Set account quorum
        - Subtract asset quantity
        - Transfer asset
        - Compare and Set Account Detail
        - Set setting value
- Query: Utilizada para consultar valores na blockchain
    - Importantes:
        - Get Account Detail
        - Get Account Transactions
- SOLUÇÃO
    - Posso ir registrando os accounts details com grupos de set_acc_detail em transações
    - No final terei o histórico nos blocos, apesar do get_acc_detail somente mostrar o último valor.

# PARÂMETROS
- max_proposal_size: Número máximo de transações por bloco. Se precisar de grande
TPS, aumentar esse valor. O inicial é 10.
- proposal_delay: Quanto os nós esperam para propor os blocos. Se muito baixo, não
adianta o anterior ser alto. Se muito alto, perde desempenho. Deve ser maior que
o proposal_creation_timeout.
- proposal_creation_timeout: Tempo entre dois rounds do consenso. O padrão é 3000.
Se o iroha não vai ficar ocioso, eu posso diminuir ele.
- vote_delay: Tempo para esperar a votação. Mínimo 1 segundo. Aumentar em caso de 
rede lenta.
- 

# Como executar o experimento completo
- First Stage (MOSAIC)
  - Duração da simulação:
    - Em /home/andre/MOSAIC21/scenarios/MosaicBroker/scenario_config.json
    - Modificar a chave "duration" para variar o tempo de simulação.
    - ATENÇÃO: O tempo de duração inclui a formação do grupo e os resultados do primeiro artigo não inclui
    o tempo que leva para formar o grupo.
  - Tamanho dos grupos:
    - Em /home/andre/MOSAIC21/scenarios/MosaicBroker/mapping/mapping_config.json
    - Na lista que está na chave "vehicles"
      - Adicionar uma entrada para cada veículo da simulação
      - Deixar eles todos na mesma rota
  - TPS
    - Em /home/andre/MOSAIC21/param.json
    - Trocar o valor da chave "interval"
    - A conta para saber qual valor colocar em interval é a seguinte:
      - 1000000000 / X = param.json
      - Exemplo: 1000000000 / 40 tps = 25000000
      - Tabela:
      - 1000000000 = 1TPS
      - 500000000 = 2TPS
      - 333333333 = 3TPS
      - 250000000 = 4TPS
      - 200000000 = 5TPS
      - 125000000 = 8TPS
      - 100000000 = 10TPS
      - 50000000 = 20TPS
      - 40000000 = 25TPS
      - 25000000 = 40TPS
      - 20000000 = 50TPS
      - 10000000 = 100TPS
  - Execução:
    - Em /home/andre/MOSAIC21/etc
    - Copiar o arquivo runtimeGUI.json para runtime.json para ativar o modo gráfico
    - Verificar se os grupos formados estão corretos
      - Se não forma mais de um grupo 
      - Se o grupo não fica variando
    - Ao terminar a verificação, copiar runtimeDFLT.json para runtime.json para desativar o modo gráfico
    - Realizar 20 execuções para gerar 20 pastas na pasta /home/andre/MOSAIC21/logs
    - Em /home/andre/MOSAIC21
    - ./mosaic.sh -s MosaicBroker
  - Copia dos arquivos
    - Em /home/andre/MOSAIC21/logs
    - Renomear a pasta de logs para identificar o experimento
    - Executar ./rename.sh <nome da pasta de log gerada>
    - Os arquivos serão jogados em /home/andre/PycharmProjects/DispatcherBroker/input
- Second Stage (DispatcherBroker - Hyperledger Iroha)
  - Abrir 4 terminais
    - No primeiro: executar top e depois apertar a tecla 1 para monitorar os processadores
    - No segundo: executar docker stats
    - No terceiro: ir em /home/andre/PycharmProjects/DispatcherBroker/iroha_module/Cenario/
      - Executar docker-compose up <lista de containers para levantar>
      - Exemplo: docker-compose up veh_0 veh_1 veh_2 veh_3
      - Apertar ctrl + C quando o quarto terminal finalizar
    - No quarto: ir em /home/andre/PycharmProjects/DispatcherBroker/
      - Após levantar os containers do terceiro terminal
      - Executar python3 main.py -i <nome da pasta de input>
  - Em /home/andre/PycharmProjects/DispatcherBroker/iroha_module/Cenario
    - Executar ./prepare.sh
    - Isso irá gerar um output.tar.gz que já contem todos os arquivos necessários
    - Executar reset.sh para reiniciar ao estado necessário para uma nova execução
  - Em /home/andre/TransactionCounter/output/
    - Executar copy_and_prepare.sh <nome do experimento>
  - Pelo Pycharm do TransactionCounter
    - Acrescentar uma nova linha do experimento a main 
    - Dar o play na main

# Documentação
- Site original: https://www.hyperledger.org/use/iroha
- Git hub do código do Iroha: https://github.com/hyperledger/iroha
- Stackoverflow do Iroha: https://stackoverflow.com/questions/tagged/hyperledger-iroha
- Read the docs (Manual): https://iroha.readthedocs.io/en/main/maintenance/index.html
- https://github.com/hyperledger/iroha-python
- https://chat.hyperledger.org/channel/iroha
- https://t.me/hyperledgeriroha
- https://arxiv.org/pdf/1809.00554.pdf
- https://katacoda.com/hyperledger-iroha/scenarios/iroha-transfer-asset
- https://www.youtube.com/watch?v=mzuAbalxOKo
- https://www.youtube.com/channel/UCYlK9OrZo9hvNYFuf0vrwww
- https://wiki.hyperledger.org/display/iroha/Hyperledger+Iroha
- https://codeberg.org/diva.exchange/iroha-explorer
- https://github.com/turuslan/blockchain-explorer/tree/iroha-explorer-integration/iroha



# MÉTRICAS
- Abrir o código no MOSAIC e ver como faço para fixar o número de transações que os veículos fazem por segundo
  - Com isso conseguirei fixar o workload para poder avaliar diferentes cenários
  - Começar com 1 TPS por veículo
- Preciso montar tabelas com resultados dos cenários
  - Linhas: Duração dos grupos
  - Colunas: Tamanho dos grupos
  - Altura ou outra tabela: Workload em TPS dos veículos
- Após o término da simulação terei de fazer um script em python para
  - Varrer todos os blocos gerados excluindo o bloco gênesis
  - Em cada bloco contar quantas transações estão consolidadas nele
    - É um json, basta eu pegar o array de transactions e ver o length
- Com todas essas tabelas montadas poderei achar como se comporta o algoritmo e o Iroha
  - Tentar encontrar aonde conseguimos o maior número de transações consolidadas antes da rede começar a degradar.

# PRÓXIMOS PASSOS:
- Testes
  - Utilizar o cenário que já tenho
  - Colocar 3 veículos para executar a mesma rota
  - Subir o TPS dos veículos
  - Rodar o Iroha
    - Acompanhar processadores do Virtual Box com top
    - Acompanhar processadores dos containers com docker stats
    - Verificar parâmetros do vmstat do linux como alternativa para o top
    - Verificar se o CPU e a Memória estão muito altas em relação a taxa de transações consolidadas
    sobre transações totais. Isso vai me dar uma curva workload(tps) X taxa(consolidada/total)
  - Os testes mostraram que não tenho CPU nem memória suficientes para executar a simulação
    - Talvez em um cluster com um container em cada núcleo
    - Talvez utilizar uma máquina externa como cliente com o DispatcherBroker para poder fazer a simulação
- Terminar de ler a documentação
- Buscar artigos de referência que usam o Iroha e que testam benchmark do Iroha
  - Tentar achar o paper que tem o valor ótimo de transactions per second do Iroha
    - Tentar achar um artigo no google scholar
    - With even distribution we received quite good results - with 300k transactions sent in 5 minutes. 
    Commit took from 2 seconds to 2 minutes. Please note that results always depend on number of peers 
    in your network, its speed and parameters of the hosts on which the peers run.
    - Em https://iroha.readthedocs.io/en/main/faq/index.html indica que o TPS depende das configurações que é para usar
    o diretório test/load para medir os resultados
      - Posso executar isso, ver a melhor performance do iroha e depois medir a do meu algoritmo
      - Desse modo, eu sei se está sendo limitado pelo meu ou pelo iroha.
- Criar um cenário do manhattan grid com semáforo
  - Colocar o número de veículos que eu desejar para o cenário
  - Todos eles vão fazer parte do mesmo grupo
  - Fazer uma rota que dure muito tempo com os veículos agrupados
  - A duração será dada pelo término da simulação
  - Com isso, consigo controlar workload dos veículos, duração e tamanho dos grupos criados.
- Métricas
  - Contar transações consolidadas nos blocos
  - Imprimir ao término da simulação quantas transações foram enviadas pelo dispatcher_broker
  
- Utilizar as métricas do próprio Iroha para poder ver o número de transações consolidadas
  - https://iroha.readthedocs.io/en/main/maintenance/metrics.html
- Colocar no finalize_ledger da classe hyperledger
  - Uma chamada da query GetPendingTransactions para o iroha_api
  - Imprimir o resultado junto com a linha final indicando as transações que ficaram pendentes
  - https://iroha.readthedocs.io/en/main/develop/api/queries.html
- Tem uma query chamada GetPeers que pode ser útil para monitorar a adição e subtração de peers no funcionamento
  - Posso colocar isso nas chamadas add_peer e remove_peer

# TODO
- Colocar as queues no arquivo de saída na pasta output
- Verifica se a chamada do net send do Iroha está bloqueando o subprocesso ou se ele envia a transação e continua
- Separar o experimento em uma máquina de containers e outra dos clientes e reprodução real time da simulação
  - Servidor
    - Containers rodando
    - Precisa ouvir por um sinal de término da simulação para parar os containers
    - Retornar o resultado pela rede
  - Cliente
    - Executa o MOSAIC
    - Reproduz os logs do mosaic em tempo real submetendo as transações para o servidor
    - No final da simulação, envia um sinal de término
    - Copia os volumes gerados 
    - Executa o terceiro estágio em cima do que foi copiado

# CRITICAL SEARCH
- 9 veículos
  - Até 5 tps
    - Não enfileira no Dispatcher
    - Não usa 100% do CPU
    - Iroha consolida tudo
  - De 6 Até 16 tps
    - Não enfileira no Dispatcher
    - Não usa 100% do CPU
    - Iroha perde até 10% das transações (Máx 9 * 16 = 144tps)
  - De 17 até 24 tps
    - Não enfileira no Dispatcher
    - Usa 100% do CPU
    - Iroha perde mais de 10% das transações
  - Acima de 25 tps
    - Enfileira no Dispatcher
    - Usa 100% do CPU
    - Iroha perde mais de 10% das transações
    
- 3 veículos
  - Até 10 tps
    - Não enfileira no Dispatcher
    - Não usa 100% do CPU
    - Iroha consolida tudo
  - De 11 até 61 tps 
    - Não enfileira no Dispatcher
    - Não usa 100% do CPU
    - Iroha perde até 10% das transações (Máx 3 * 61 = 183tps)
  - De 62 até 89 tps 
    - Não enfileira no Dispatcher
    - Não usa 100% do CPU
    - Iroha perde mais de 10% das transações
  - Acima de 90 tps 
    - Enfileira no Dispatcher
    - Não usa 100% do CPU
    - Iroha perde mais de 10% das transações

- Com 3 e 4 veículos ele não usa 100% de CPU nunca