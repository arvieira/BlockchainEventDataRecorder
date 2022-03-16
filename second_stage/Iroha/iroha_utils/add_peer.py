import os
import binascii
from iroha import primitive_pb2, IrohaCrypto, Iroha, IrohaGrpc

from docker_network import get_container_ip

IROHA_HOST_ADDR = os.getenv('IROHA_HOST_ADDR', '127.0.0.1')
IROHA_PORT = os.getenv('IROHA_PORT', '50051')
net = IrohaGrpc('{}:{}'.format(IROHA_HOST_ADDR, IROHA_PORT))

ADMIN_ACCOUNT_ID = 'admin@test'
ADMIN_PRIVATE_KEY = '061759b6995c7ccdbcd1b770f032faeb4d1890b7e9d3811bd27e1b04f3caa934'
iroha = Iroha(ADMIN_ACCOUNT_ID)


def send_transaction_and_print_status(transaction):
    hex_hash = binascii.hexlify(IrohaCrypto.hash(transaction))
    print('Transaction hash = {}, creator = {}'.format(
        hex_hash, transaction.payload.reduced_payload.creator_account_id))
    net.send_tx(transaction)
    for status in net.tx_status_stream(transaction):
        print(status)


def add_peer():
    peer1 = primitive_pb2.Peer()
    ip = get_container_ip('veh_4')
    port = '10001'
    peer1.address = f'{ip}:{port}'
    peer1.peer_key = 'c192da83862df8075a36226a2eda4f92ea0ab11af36ec475fed6e414faf79d42'
    tx = iroha.transaction([iroha.command('AddPeer', peer=peer1)], creator_account=ADMIN_ACCOUNT_ID, quorum=1)

    tx = IrohaCrypto.sign_transaction(tx, ADMIN_PRIVATE_KEY)

    send_transaction_and_print_status(tx)


add_peer()
