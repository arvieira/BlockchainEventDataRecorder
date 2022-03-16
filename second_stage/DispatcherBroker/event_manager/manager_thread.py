import sched
import time

from second_stage.DispatcherBroker.broker.broker import Broker
from second_stage.DispatcherBroker.broker.hyperledger import Hyperledger


# Classe que irá fazer o papel de módulo de gerenciamento dos eventos
# Receberá as linhas do tempo e irá agendar o acontecimento dos eventos para adicionar nas filas
# dos containers.
class Manager:
    # Construtor
    def __init__(self):
        # Criando o pool de veículos da simulação
        self.vehicles_pool = {}

        # Criando o pool de threads
        self.thread_pool = []

        # Criando o agendador de eventos
        self.scheduler = sched.scheduler(time.time, time.sleep)

    # Método para adicionar uma timeline
    def add_timeline(self, vehicle, timeline):
        # Criando a fila do veículo e adicionando ao pool de veículos
        self.vehicles_pool[vehicle] = []

        # Criando a thread e adicionando ao pool de threads
        self.thread_pool.append(Broker(vehicle=vehicle, manager=self))

        # Contador de transações
        transaction_counter = 0

        # Para cada evento da timeline, eu agendo a adição dele na fila do veículo
        for event in timeline:
            self.scheduler.enter(event['timestamp'], 1, self.add_to_queue, kwargs={"vehicle": vehicle, "event": event})

            # Contando quantas transações fora realizadas
            if event['command'] == 'TRANSACTION':
                transaction_counter += 1

        return transaction_counter

    # Método interno para adicionar um evento que ocorreu na fila do veículo
    def add_to_queue(self, vehicle, event):
        # Verifica se o veículo está no pool
        if vehicle in self.vehicles_pool.keys():
            # Se estiver, adiciona o evento à fila do veículo
            self.vehicles_pool[vehicle].append(event)

            # Imprimindo a adição dos eventos à fila em tempo real
            # print(f"{vehicle} -> Adicionando evento na fila: {event}")

    # Inicia a simulação
    def start_simulation(self):
        # Inicia tudo dos containers para começar a simulação
        Hyperledger().initialize_ledger()

        # Inicia as threads de consumo dos eventos nas filas
        for thread in self.thread_pool:
            thread.start()

        # Inicia o agendador de eventos
        self.scheduler.run()

    # Finaliza a simulação
    def stop_simulation(self, transaction_counter):
        simulating = True

        # Realiza uma tentativa por segundo para finalizar a simulação
        while simulating:
            # Verifica se o agendador já agendou tudo e se as todas as filas dos veículos estão vazias
            # if self.scheduler.empty() and self.is_all_queues_empty():
            #     # Parando todas as threads
            #     for thread in self.thread_pool:
            #         thread.stop()
            #
            #     # Finaliza tudo dos containers para finalizar a simulação
            #     Hyperledger().finalize_ledger()
            #     break

            # Ao final da simulação, o último evento é um shutdown do veículo.
            # Esse evento está chamando o comando stop da thread.
            # Para saber se a simulação terminou, eu verifico se todas as threads já estão paradas
            simulating = False
            for thread in self.thread_pool:
                if not thread.is_stopped():
                    simulating = True

            # Aguardando um segundo para verificar novamente
            time.sleep(1)

        Hyperledger().finalize_ledger(transaction_counter)

    # Verifica se todas as filas estão vazias
    def is_all_queues_empty(self):
        # Variável de retorno
        empty = True

        # Olha todas as filas
        for queue in self.vehicles_pool:
            # Se alguma delas não estiver vazia, retorna falso
            if self.vehicles_pool[queue]:
                empty = False

        # Retorno da verificação
        return empty

    # Retorna o próximo evento na fila de um veículo
    def get_next_event(self, vehicle):
        if self.vehicles_pool[vehicle]:
            return self.vehicles_pool[vehicle].pop(0)
        else:
            return None
