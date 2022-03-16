import pandas as pd
import json
import os
import statistics


OUTPUT_DIR = './output/'


# Função que realiza a leitura de todos os arquivos e monta os respectivos Dataframes
def tabulate(directory):
    # Criação dos dicionários que serão preenchidos no loop for
    df_total = {}
    df_consolidated = {}
    df_queues = {}
    df_blocks = {}
    df_latency = {}

    # Realizando o loop por todos os arquivos contidos no diretório indicado
    for item in os.listdir(directory):
        # Realiza o parser do nome do arquivo retornando um dicionário
        name = parse_dir_name(item)

        # Realiza o parser dos dados do arquivo retornando um dicionário
        data = parse_file(f'{directory}/{item}')

        # Adicionando o valor do experimento corrente ao dicionário criado fora do loop for
        # Aqui passamos o nome do experimento, os dados, o dicionário que será alterado e a informação
        # que desejamos colocar no dicionário
        df_total = search_value(name, data, df_total, 'total_transactions')
        df_consolidated = search_value(name, data, df_consolidated, 'consolidated_transactions')
        df_queues = search_value(name, data, df_queues, 'queues')
        df_blocks = search_value(name, data, df_blocks, 'blocks')
        df_latency = search_value(name, data, df_latency, 'latency')

    # Após o final da leitura dos arquivos, temos dicionários complexos que serão transformados em
    # Dataframes do pandas.
    totals = create_dataframes(df_total)
    consolidateds = create_dataframes(df_consolidated)
    queues = create_dataframes(df_queues)
    blocks = create_dataframes(df_blocks)
    latency = create_dataframes(df_latency)

    # As métricas tps e porcentagem são obtidas a partir de contas realizadas com outros dataframes
    percentages = {}
    tps = {}
    iroha_pending = {}
    for key in totals.keys():
        # Removendo colunas de experimentos que sejam toda de NaN
        # Isso acontece quando uma duração não executa para 3, 5, 7 e 9 veículos
        totals[key] = totals[key].dropna(axis=1, how='all')
        consolidateds[key] = consolidateds[key].dropna(axis=1, how='all')
        queues[key] = queues[key].dropna(axis=1, how='all')
        blocks[key] = blocks[key].dropna(axis=1, how='all')
        latency[key] = latency[key].dropna(axis=1, how='all')

        # Regra de 3 para calcular porcentagem
        percentages[key] = (100*consolidateds[key])/totals[key]

        # Divisão do número de transações consolidadas pela duração do experimento
        # Para a duração preciso retirar o 's' do '30s', por exemplo.
        tps[key] = consolidateds[key]/(int(key[0:-1]))

        # Transações que ficaram pendentes no Iroha
        # Iroha Pending = Total - Consolidadas - Enfileiradas
        iroha_pending[key] = totals[key] - consolidateds[key] - queues[key]

    # Dando nome a coluna de transações
    totals = name_index(totals)
    consolidateds = name_index(consolidateds)
    percentages = name_index(percentages)
    tps = name_index(tps)
    queues = name_index(queues)
    blocks = name_index(blocks)
    iroha_pending = name_index(iroha_pending)
    latency = explode_dataframe_dict(latency)

    # Impressão dos resultados
    print(f'TOTAL TRANSACTIONS:\n{totals}\n\n')
    print(f'CONSOLIDATED TRANSACTIONS: \n{consolidateds}\n\n')
    print(f'CONSOLIDATED TRANSACTIONS PERCENTAGE: \n{percentages}\n\n')
    print(f'CONSOLIDATED TRANSACTIONS PER SECOND: \n{tps}\n\n')
    print(f'FINAL QUEUE SIZE: \n{queues}\n\n')
    print(f'IROHA PENDING: \n{iroha_pending}\n\n')
    print(f'TOTAL OF BLOCKS: \n{blocks}\n\n')
    print(f'TRANSACTION LATENCY: \n{latency}\n\n')

    # Salvando em xlsx para poder usar no flourish posteriormente passar para pyplot
    save_to_xlsx(totals, 'TotalTransactions')
    save_to_xlsx(consolidateds, 'ConsolidatedTransactions')
    save_to_xlsx(percentages, 'ConsolidatedTransactionsPercentage')
    save_to_xlsx(tps, 'ConsolidatedTransactionsPerSecond')
    save_to_xlsx(queues, 'FinalQueueSize')
    save_to_xlsx(blocks, 'Blocks')
    save_to_xlsx(iroha_pending, 'IrohaPendingTransactions')
    save_to_xlsx(latency, 'TransactionLatency', True)

    return totals, consolidateds, percentages, tps, queues, blocks, iroha_pending, latency


# Realizando o parser do nome do arquivo para identificar o experimento
def parse_dir_name(name):
    # Tratamento do nome
    name = name.split('/')
    name = name[-1].split('_')
    name[-1] = name[-1].split('.')[0]

    # Criação do dicionário de retorno
    name = {
        'vehicles_number': name[0],
        'duration': name[1],
        'tps': int(name[2][:-3])
    }

    return name


# Leitura de um arquivo de output json para uma variável
def parse_file(file):
    # Leitura do arquivo json e retorno do dicionário
    with open(file, 'r') as f:
        data = json.load(f)

    return data


# Função para pegar o valor desejado dentro do arquivo lido
def search_value(name, data, df_data, searched_key):
    # Esse primeiro bloco de ifs cria os dicionários que eu ainda não tenho.
    # Isso evita que tenhamos um erro de invalid key
    if name['duration'] not in df_data:
        df_data[name['duration']] = {}
    if name['vehicles_number'] not in df_data[name['duration']]:
        df_data[name['duration']][name['vehicles_number']] = {}

    # O if decide por qual métrica está sendo pega do arquivo.
    # A variável data é uma lista com resultados de cada uma das repetições dentro do arquivo.
    # Eu faço um list comprehension pegando os valores que queremos diante da métrica apresentada.
    # Nessa lista resultante eu aplico a média estatística.
    if searched_key == 'consolidated_transactions':
        df_data[name['duration']][name['vehicles_number']][name['tps']] = statistics.mean([max(x[searched_key].values()) for x in data])
    elif searched_key == 'total_transactions':
        df_data[name['duration']][name['vehicles_number']][name['tps']] = statistics.mean([int(x[searched_key]) for x in data])
    elif searched_key == 'queues':
        df_data[name['duration']][name['vehicles_number']][name['tps']] = statistics.mean([sum(x[searched_key].values()) for x in data])
    elif searched_key == 'blocks':
        df_data[name['duration']][name['vehicles_number']][name['tps']] = statistics.mean([max(x[searched_key].values()) for x in data])
    elif searched_key == 'latency':
        latency = []
        for repeat in data:
            # Para cada repetição, eu olho o veículo que consolidou mais transações.
            # Dado que todos os blocos são iguais, a latência das transações registradas serão iguais.
            # Só preciso contabilizar as latências do veículo que consolidou mais por repetição.
            max_veh_consolidated = max(repeat['consolidated_transactions'], key=repeat['consolidated_transactions'].get)
            latency += repeat[searched_key][max_veh_consolidated]
            # for vehicle in repeat[searched_key].values():
            #     latency += vehicle
        latency.sort()
        df_data[name['duration']][name['vehicles_number']][name['tps']] = latency

    return df_data


# Função para criar um Dataframe a partir de um dicionário
def create_dataframes(df_data):
    # Dicionário de retorno
    durations = {}

    # Criando o dataframe a partir do dicionário que montamos até aqui
    df = pd.DataFrame.from_dict(df_data)

    # Foi criado um Dataframe que tem o número de veículos nas linhas e as durações nas colunas
    # Dentro das células eu tenho um json pq o tps ainda varia. Preciso normalizar isso.
    # A solução foi criar um dicionário em que as keys são as durações e os values são os Dataframes do experimento
    # contento número de veículos nas linhas e TPS nas colunas.
    for key in df:
        # Ordeno as linhas e as colunas para ficar melhor.
        # No entanto, o 100TPS está ficando como a primeira coluna por causa da ordem alfabética.
        durations[key] = pd.json_normalize(df[key]).set_index(df.index).sort_index(axis=0).sort_index(axis=1).T

    return durations


# Função para dar o nome ao índice
def name_index(data_dict):
    for key in data_dict:
        cols = []
        for col in data_dict[key].columns:
            cols.append(col)

        data_dict[key] = data_dict[key].reset_index()
        cols.insert(0, 'Transactions Per Second')
        data_dict[key].columns = cols
        # data_dict[key] = data_dict[key].set_index('Transactions Per Second')

    return data_dict


# Função para explodir as tabelas de lantências em várias com múltiplas linhas
def explode_dataframe_dict(dataframe_dict):
    # Para cada duração
    for key in dataframe_dict:
        # Cada coluna de número de veículos será um dataframe separado explodido
        exploded = {}
        for column in dataframe_dict[key].columns:
            exploded[column] = dataframe_dict[key][[column]].copy().explode(column)

        # Acertando o index e dando nome a coluna
        exploded = name_index(exploded)

        # Adicionando à chave de duração
        dataframe_dict[key] = exploded

    return dataframe_dict


# Função para salvar os Dataframes contidos no dicionário de saída em arquivos
def save_to_xlsx(data, data_name, recursive=False):
    if not recursive:
        for key in data:
            # O nome dos arquivos de saída serão gerados com a duração e o nome da tabela
            data[key].to_excel(f'{OUTPUT_DIR}/{key}_{data_name}.xlsx')
    else:
        for key in data:
            for n_veh in data[key]:
                data[key][n_veh].to_excel(f'{OUTPUT_DIR}/{key}_{n_veh}_{data_name}.xlsx')
