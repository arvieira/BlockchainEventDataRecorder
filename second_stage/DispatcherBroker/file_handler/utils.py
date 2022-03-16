import getopt
import json

# Conjunto de comandos conhecidos que podem aparecer nos logs
import os
import sys

commands = [
    'CREATE CHANNEL',
    'JOIN PEER',
    'PAUSE PEER',
    'TRANSACTION',
    'JOIN/RESUME CHANNEL',
    'SWITCH OFF'
]


# Função para ler um arquivo de log e retornar uma timeline em formato de lista de
# dicionários
def read_file(filename):
    # Abrindo o arquivo para leitura
    with open(filename, 'r') as reader:
        # Lista que será retornada
        timeline = []

        # Lendo o arquivo linha por linha
        line = reader.readline().strip()
        while line != '':
            # Se um dos comandos conhecidos estiver na linha, ele a trata
            for command in commands:
                if command in line:
                    # Adicionando o dicionário referente ao comando ao timeline
                    timeline.append(parse_line(line))

            # Lendo próxima linha
            line = reader.readline().strip()

        # Retorna a lista de dicionários de comandos obtida
        return timeline


# Método para tratar uma linha que tenha um comando e retornar um dicionário do mesmo
def parse_line(line):
    # Divide a linha na parte do simulador e os dados que interessam
    # Pega os dados que interessam e divide na parte do json e do timestamp
    first_split = line.split(' - ')[1].split('(at simulation time ')

    # Realizando a carga do json
    event = json.loads(first_split[0])

    # Terminando de tratar o timestamp para ser um inteiro
    # Já passando para segundos para utilização no time.sleep()
    event['timestamp'] = int(
        first_split[1].replace(' s)', '').replace(')', '').replace('.', '').replace(',', '')) / 1000000000

    # Retornando o dicionário do evento obtido na linha
    return event


def parse_params(argv):
    help_info = 'python main.py -i <inputdir>'

    try:
        opts, args = getopt.getopt(argv, "hi:", ["idir="])
    except getopt.GetoptError:
        print(f'Error: {help_info}')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print(help_info)
            sys.exit()
        elif opt in ("-i", "--idir"):
            input_dir = arg

    return input_dir


def read_dir(directory):
    vehicle_files = {}

    for item in os.listdir(directory):
        vehicle_files[item.replace('.log', '')] = f'{directory}{item}'

    return vehicle_files
