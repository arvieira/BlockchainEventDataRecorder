import threading
import time

from second_stage.DispatcherBroker.broker.hyperledger import Hyperledger


# Intervalo entre as verificações da fila de eventos do veículo
# Quanto menor esse intervalo, melhor é o ordenamento dos eventos
VERIFY_INTERVAL = 0.0001


# Classe que constrói uma thread para o tratamento dos eventos na fila de um veículo
# Irá falar com o hyperledger
class Broker(threading.Thread):

    # Construtor
    def __init__(self, vehicle, manager, *args, **kwargs):
        super(Broker, self).__init__(*args, **kwargs)
        self.vehicle = vehicle
        self.manager = manager
        self.hyperledger = Hyperledger(self)
        self.stop_event = threading.Event()

    # Método chamado quando se inicia a thread no thread.start()
    def run(self):
        # Enquanto não estiver desligada
        while not self.is_stopped():
            # Pega o próximo evento
            event = self.manager.get_next_event(self.vehicle)

            # Se tinha um evento, trata ele
            if event:
                # Executando o comando no ledger
                self.hyperledger.execute(event)

            # Aguarda o intervalo definido lá em cima para a próxima verificação da fila
            time.sleep(VERIFY_INTERVAL)

    # Método para parar a thread
    def stop(self):
        self.stop_event.set()

    # Método para verificar se a thread está parada
    def is_stopped(self):
        return self.stop_event.is_set()
