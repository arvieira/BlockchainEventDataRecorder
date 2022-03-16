import subprocess
import binascii

import docker
from iroha import primitive_pb2, IrohaCrypto, Iroha, IrohaGrpc
from colorama import Fore


class IrohaApi:
    # Contas dos usuários do blockchain
    IROHA_CLIENTS = {
        'veh_0': {
            'ACCOUNT_ID': 'veh_0@traffic',
            'PRIVATE_KEY': '08e817983304b25a52d22083c266c095bfa5ad4d78466ce0bb722aac6833e149',
            'PORT': '50051'
        },
        'veh_1': {
            'ACCOUNT_ID': 'veh_1@traffic',
            'PRIVATE_KEY': 'a99e80aa80cbd6b16d9d54d87f5c6e1f389e1ca0f9646c2e3ab33f18e449f85f',
            'PORT': '50052'
        },
        'veh_2': {
            'ACCOUNT_ID': 'veh_2@traffic',
            'PRIVATE_KEY': '2883356b484c49fbfb3f2dac03c25f7dea4dc65f7c68e913955bb99507391938',
            'PORT': '50053'
        },
        'veh_3': {
            'ACCOUNT_ID': 'veh_3@traffic',
            'PRIVATE_KEY': '9194b5d71ea2d638857ec26da5d1ecec5fdb08e52f215c1184fb654f640eb28f',
            'PORT': '50054'
        },
        'veh_4': {
            'ACCOUNT_ID': 'veh_4@traffic',
            'PRIVATE_KEY': '38c7ecaa377d1d9e1152e09705e607d9452fe1368e7264e77ed3626003a34c5c',
            'PORT': '50055'
        },
        'veh_5': {
            'ACCOUNT_ID': 'veh_5@traffic',
            'PRIVATE_KEY': '18219e88fa24c976d1544b5eb5dbdd2d76d1e9014dca479a1dc0ed42a34959e0',
            'PORT': '50056'
        },
        'veh_6': {
            'ACCOUNT_ID': 'veh_6@traffic',
            'PRIVATE_KEY': '61a4c92d5ac53a8afe8546932329312ffe809618f89a4c00238561e0a259f8e9',
            'PORT': '50057'
        },
        'veh_7': {
            'ACCOUNT_ID': 'veh_7@traffic',
            'PRIVATE_KEY': '1e401f5eea27c61b53e598c2fff56cf3e403e62f4219660d0007908a69ed3871',
            'PORT': '50058'
        },
        'veh_8': {
            'ACCOUNT_ID': 'veh_8@traffic',
            'PRIVATE_KEY': '53244807a1dcee949ea5ab8f39d6901c4e8475ffb2198b19883bb2c445d38609',
            'PORT': '50059'
        },
    }

    # Chaves públicas dos veículos (peers) veh_0, veh_1 ...
    VEHICLE_PUBLIC_KEYS = {
        'veh_0': 'c632f1d062992067eed213ea8ed39180caebc80b2ce63ceb4d342c8e7bf2d45a',
        'veh_1': '3757de153d4c7f813c8f37e14e9f79e15532cc66199e07a4ff50bc41cac999f6',
        'veh_2': '486640bd901e5b5eda7053826af16023425d2c32093af68395cf4fec6cca9dc8',
        'veh_3': 'c2a51b640026d579943dd3a674f67cbacc48870c4e2c0f0e585b5ddf44ce49c6',
        'veh_4': 'c192da83862df8075a36226a2eda4f92ea0ab11af36ec475fed6e414faf79d42',
        'veh_5': '57d530a63ed10664ee991858a686c334bc3ac4ffb5de47c06c4726aad7413dec',
        'veh_6': '40b210954fdeb3beb87373b0a783e54c7d44b57d481246a8e46215b232fdb246',
        'veh_7': 'db2427295aa29d2ff0da67a7b478c6458407acf3b1a5abae2d5854872c0dbe2c',
        'veh_8': 'f831b6914ca2735e6adee474022d200793bb03b221d1a08d7a74fee1eb2bb81f',
    }

    # Tipos de transações
    ADD_PEER = 'add_peer'
    REMOVE_PEER = 'remove_peer'
    TRANSACTION = 'transaction'

    # Construtor.
    # Na criação da API, preciso indicar quem é o líder para adição e remoção de veículos
    def __init__(self, leader):
        self.leader = leader

    # Liga os quatro veículos que irão participar da simulação.
    # Não utilizar. Ligar a rede antes de iniciar a simulação
    @staticmethod
    def initialize_ledger():
        subprocess.Popen(['docker-compose', 'up'])

    # Desliga todos os veículos
    # Não utilizar. Desligar a rede após terminar a simulação
    @staticmethod
    def finalize_ledger():
        subprocess.run(['docker-compose', 'down'])

    # Remove todos os volumes.
    # O uso dessa função apaga a blockchain gerada.
    @staticmethod
    def clear_ledger():
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh0'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh1'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh2'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh3'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh4'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh5'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh6'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh7'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_blockstore_veh8'])
        subprocess.run(['docker', 'volume', 'rm', 'cenario_psql_storage'])

    # Função utilizada para pegar o IP de um container pelo nome
    @staticmethod
    def get_container_ip(container_name):
        client = docker.DockerClient()
        container = client.containers.get(container_name)
        ip_add = container.attrs['NetworkSettings']['Networks']['iroha-net']['IPAddress']
        return ip_add

    # Função utilizada para enviar uma transação para o ledger e imprimir o status
    def send_transaction_and_print_status(self, client, tx, transaction_type, target=None):
        # Simulação local
        net = IrohaGrpc('{}:{}'.format('127.0.0.1', self.IROHA_CLIENTS[client]['PORT']))
        net.send_tx(tx)

        # Comentado para não bloquear o envio de transações aguardando o fluxo de resposta
        # hex_hash = binascii.hexlify(IrohaCrypto.hash(tx))
        # for status in net.tx_status_stream(tx):
        #     if target:
        #         print(Fore.MAGENTA + f"[{hex_hash}] {tx.payload.reduced_payload.creator_account_id} realizando um {transaction_type}({target}): {status}")
        #     else:
        #         print(Fore.MAGENTA + f"[{hex_hash}] {tx.payload.reduced_payload.creator_account_id} realizando um {transaction_type}: {status}")

    # Função para adicionar um peer no ledger
    def add_peer(self, vehicle_to_add):
        if self.leader:
            peer1 = primitive_pb2.Peer()
            ip = self.get_container_ip(vehicle_to_add)
            port = '10001'
            peer1.address = f'{ip}:{port}'
            peer1.peer_key = self.VEHICLE_PUBLIC_KEYS[vehicle_to_add]

            account_id = self.IROHA_CLIENTS[self.leader]['ACCOUNT_ID']
            iroha = Iroha(account_id)

            tx = iroha.transaction([iroha.command('AddPeer', peer=peer1)], creator_account=account_id, quorum=1)
            tx = IrohaCrypto.sign_transaction(tx, self.IROHA_CLIENTS[self.leader]['PRIVATE_KEY'])

            self.send_transaction_and_print_status(self.leader, tx, self.ADD_PEER, vehicle_to_add)
        else:
            print('É necessário criar um canal e informar o líder antes de adicionar peers.')

    # Função para remover um peer do ledger
    def remove_peer(self, vehicle_to_remove):
        if self.leader:
            peer_public_key = self.VEHICLE_PUBLIC_KEYS[vehicle_to_remove]

            account_id = self.IROHA_CLIENTS[self.leader]['ACCOUNT_ID']
            iroha = Iroha(account_id)

            tx = iroha.transaction([iroha.command('RemovePeer', public_key=peer_public_key)], creator_account=account_id,
                                   quorum=1)
            tx = IrohaCrypto.sign_transaction(tx, self.IROHA_CLIENTS[self.leader]['PRIVATE_KEY'])

            self.send_transaction_and_print_status(self.leader, tx, self.REMOVE_PEER, vehicle_to_remove)
        else:
            print('É necessário criar um canal e informar o líder antes de remover peers.')

    # Realiza uma transação no ledger
    def do_transaction(self, client, transaction=None):
        # Os usuários estão criados no bloco gênesis
        account_id = self.IROHA_CLIENTS[client]['ACCOUNT_ID']
        iroha = Iroha(account_id)

        # Tratando a transação recebida do MOSAIC
        if transaction and 'SimEvent' in transaction.keys():
            tx = iroha.transaction([
                iroha.command('SetAccountDetail', account_id=account_id, key=detail,
                              value=str(transaction['SimEvent'][detail]))
                for detail in transaction['SimEvent'].keys()
            ])

            IrohaCrypto.sign_transaction(tx, self.IROHA_CLIENTS[client]['PRIVATE_KEY'])
            self.send_transaction_and_print_status(client, tx, self.TRANSACTION)
        else:
            print('Transação sem SimEvent.')


# A inicialização e a finalização serão feitas antes e depois da simulação
# IrohaApi.initialize_ledger()
# IrohaApi.finalize_ledger()
# IrohaApi.clear_ledger()

# Criando um objeto da classe para realizar os testes
# iroha_api = IrohaApi(leader='veh_0')

# Exemplo de como realizar uma transação no ledger.
# Serão recebidas transações com esse formato, mas o importante é ter a SimEvent.
# O client é para qual peer será enviada a transação. Pelo funcionamento do Iroha, pode ser qualquer um.
# No entanto, deve ser enviado para o peer que representa o veículo que está gerando a transação para fins de simulação.
# transaction_example = {
#     'SimEvent': {
#         'throttle': 0,
#         'stopped': False,
#         'rightLight': False,
#         'latitude': -22.987866322239014,
#         'leftLight': False,
#         'speed': 9.434572255529929,
#         'brake': 0,
#         'brakeLight': False,
#         'backdrive': False,
#         'id': 'veh_0',
#         'lane': 0,
#         'timestamp': 7000000000,
#         'longitude': -43.211778931420845
#     },
#     'TransactionTimestamp': 7000000000,
#     'PublicKey': 'EC Public Key [9f:d9:9b:91:df:0b:71:c1:4a:7c:06:64:f1:74:d6:a5:25:d0:37:d2]\n            X: 160c8c54a1aea2480476d4ff9f26de643f745a264fecebfcb9dcd3a853250de3\n            Y: 6ccf3a50623663c52816783ae7e0536a63aac2df479772aa80f15840761e6a6e\n',
#     'Signature': 'MEYCIQCJltOEySXkUOGOyAhGO8iFACxb8rtyav50+79y7IdwkgIhAI9leLeEDSSE3MpN3ok0aPdibGClxV07pvi7ZXZTpOKp',
#     'Hash': '7a83a5c4e38f5474e4f96688401c18d2a2704244caabcbb2705347ac99b09538',
#     'OriginId': 'veh_0'
# }
# iroha_api.do_transaction('veh_3', transaction_example)

# Adicionando ou removendo um peer.
# iroha_api.add_peer('veh_1')
# iroha_api.add_peer('veh_2')
# iroha_api.add_peer('veh_3')
# iroha_api.remove_peer('veh_3')
