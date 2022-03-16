# Classe para executar os comandos no Hyperledger

from colorama import Fore

from second_stage.Iroha.iroha_api import IrohaApi


class Hyperledger:
    # Construtor
    def __init__(self, thread=None):
        self.iroha_api = IrohaApi(leader='veh_0')
        self.running_thread = thread

    # Método chamado para executar um evento no ledger
    def execute(self, event):
        # Pega o comando que será executado no evento
        command = event['command']

        # Conforme o comando, chama o respectivo método para o tratamento
        if command == 'CREATE CHANNEL':
            self.create_channel(event['leader'], event['member'], event['group'])
        elif command == 'JOIN PEER':
            self.join_channel(event['member'], event['group'])
        elif command == 'PAUSE PEER':
            self.pause_peer(event['member'], event['group'])
        elif command == 'JOIN/RESUME CHANNEL':
            self.resume_peer(event['member'], event['group'])
        elif command == 'TRANSACTION':
            if 'leader' in event:
                self.transaction(True, event['leader'], event['group'], event['transaction'])
            else:
                self.transaction(False, event['member'], event['group'], event['transaction'])
        elif command == 'SWITCH OFF':
            if 'leader' in event:
                self.shutdown(True, event['leader'])
            else:
                self.shutdown(False, event['member'])

            return False

        return True

    # Método chamado antes de iniciar a simulação para ligar todos os containers e
    # fazer o que mais precisar antes do início.
    @staticmethod
    def initialize_ledger():
        # A inicialização do docker-compose up será feita antes da simulação
        print(Fore.WHITE + 'Initializing containers and blockchain...')

    # Método para criar um channel no ledger
    def create_channel(self, leader, member, group):
        print(Fore.GREEN + f"Creating channel {group}: Leader {leader}, Member {member}")
        # self.iroha_api.set_leader(leader)

    # Método para adicionar um peer no channel
    def join_channel(self, member, group):
        print(Fore.BLUE + f"Joining Member {member} to channel {group}")
        self.iroha_api.add_peer(member)

    # Método para pausar um peer
    def pause_peer(self, member, group):
        print(Fore.RED + f"Pausing Member {member} in channel {group}")
        self.iroha_api.remove_peer(member)

    # Método para reiniciar um peer
    def resume_peer(self, member, group):
        print(Fore.BLUE + f"Resuming Member {member} in channel {group}")
        self.iroha_api.add_peer(member)

    # Método para submeter uma transação no ledger
    def transaction(self, is_leader, vehicle, group, transaction):
        # if is_leader:
        #     print(Fore.YELLOW + f"Submitting Leader transaction from {vehicle} to channel {group}")
        #     # print(f"Submitting Leader transaction from {vehicle} to channel {group}: {transaction}")
        # else:
        #     print(Fore.YELLOW + f"Submitting Member transaction from {vehicle} to channel {group}")
        #     # print(f"Submitting Member transaction from {vehicle} to channel {group}: {transaction}")

        self.iroha_api.do_transaction(vehicle, transaction)

    # Método para desligar o veículo no final da simulação
    # Esse método será responsável por parar a thread do veículo
    def shutdown(self, is_leader, vehicle):
        if is_leader:
            print(Fore.YELLOW + f"Leader {vehicle} shutting down.")
        else:
            print(Fore.YELLOW + f"Member {vehicle} shutting down.")

        if self.running_thread:
            self.running_thread.stop()

    # Método chamado no final da simulação para encerrar o ledger e os containers.
    @staticmethod
    def finalize_ledger(transaction_counter):
        # O Iroha será finalizado após o termino da simulação
        print(Fore.WHITE + 'Finalizing containers and blockchain...')
        print(f'Total Number of Transactions: {transaction_counter}')

        # Escrevendo o total de transações em um arquivo
        with open('./Total.txt', 'w') as f:
            f.write(str(transaction_counter))

    # Método chamado no final da simulação para encerrar o ledger e os containers.
    @staticmethod
    def contabilize_ledger(transaction_counter):
        # Contabilizando as transações antes de iniciar as simulações
        print(Fore.WHITE + f'Total Number of Transactions: {transaction_counter}')

        # Escrevendo o total de transações em um arquivo
        with open('./Total.txt', 'w') as f:
            f.write(str(transaction_counter))
