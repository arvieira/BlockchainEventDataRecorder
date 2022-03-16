import json
import os
import sched
import time
import multiprocessing as mp

from second_stage.DispatcherBroker.broker.hyperledger import Hyperledger


# Classe que irá fazer o papel de módulo de gerenciamento dos eventos
# Receberá as linhas do tempo e irá agendar o acontecimento dos eventos para adicionar nas filas
# dos containers.
class Manager:
    # Construtor
    def __init__(self):
        # Criando o pool de veículos da simulação
        self.vehicles_pool = {}

        # Criando o pool de processos
        self.process_pool = {}

        # Criando o agendador de eventos
        self.scheduler = sched.scheduler(time.time, time.sleep)

        # Contador de transações
        self.transaction_counter = 0

        # Tamanho final da fila dos processos
        self.final_queue_size = {}

    # Método para adicionar uma timeline
    def add_timeline(self, vehicle, timeline):
        # Criando a fila do veículo e adicionando ao pool de veículos
        self.vehicles_pool[vehicle] = mp.Queue()

        # Para cada evento da timeline, eu agendo a adição dele na fila do veículo
        for event in timeline:
            self.scheduler.enter(event['timestamp'], 1, self.add_to_queue, kwargs={'vehicle': vehicle, 'event': event})

            # Contando quantas transações fora realizadas
            if event['command'] == 'TRANSACTION':
                self.transaction_counter += 1

        # Criando o subprocesso que irá tratar a fila do veículo
        self.process_pool[vehicle] = mp.Process(target=run, args=(self.vehicles_pool[vehicle],))

    # Método interno para adicionar um evento que ocorreu na fila do veículo
    def add_to_queue(self, vehicle, event):
        # Adiciona o evento à fila do veículo
        self.vehicles_pool[vehicle].put(event)

        # Se o evento for um SWITCH OFF, imprime a fila e termina o processo
        if event['command'] == 'SWITCH OFF':
            # Preciso remover 1 que é equivalente ao comando switch off que não vai ser tratada e ficará na
            # fila. Isso ocorre pq eu adiciono o evento e mato o subprocesso em seguida.
            self.final_queue_size[vehicle] = self.vehicles_pool[vehicle].qsize() - 1
            self.process_pool[vehicle].terminate()

            if len(self.vehicles_pool) == len(self.final_queue_size):
                # Imprimindo a fila de processos
                print(f'Queues: {self.final_queue_size}')

                # Escrevendo as filas no arquivo
                with open('./Queues.txt', 'w') as f:
                    json.dump(self.final_queue_size, f)

            if len(mp.active_children()) == 1:
                # Finalizando o ledger
                os._exit(0)

    # Inicia a simulação
    def start_simulation(self):
        # Inicia tudo dos containers para começar a simulação
        Hyperledger().initialize_ledger()
        Hyperledger().contabilize_ledger(self.transaction_counter)

        # Inicia os processos de consumo dos eventos nas filas
        for process in self.process_pool.keys():
            self.process_pool[process].daemon = True
            self.process_pool[process].start()

        # Inicia o agendador de eventos
        self.scheduler.run()

    # Finaliza a simulação
    def stop_simulation(self):
        # Esse join indica que o programa só termina quando todos os processos desligaram os veículos
        for process in self.process_pool.keys():
            self.process_pool[process].join()


# Função que será executada por cada um dos subprocessos
# Ele recebe uma fila que ficará monitorando para a recepção de novos eventos
# Quando encontra um evento, ele executa o mesmo no ledger
def run(queue):
    # Variável que guarda a API para executar os comandos no ledger utilizado
    hyperledger = Hyperledger()

    # Variável que controla a execução do processo
    running = True

    # Loop de monitoramento
    while running:
        # Get bloqueante do próximo evento da fila
        event = queue.get()

        # Executando o comando no ledger
        running = hyperledger.execute(event)
