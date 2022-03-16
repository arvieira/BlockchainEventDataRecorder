#!/usr/bin/env python3
#
# Copyright Soramitsu Co., Ltd. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0
#

from iroha import Iroha, IrohaGrpc
from iroha import IrohaCrypto
import os
import sys
import time
import uuid

if sys.version_info[0] < 3:
    raise Exception('Python 3 or a more recent version is required.')

IROHA_HOST_ADDR = os.getenv('IROHA_HOST_ADDR', '127.0.0.1')
IROHA_PORT = os.getenv('IROHA_PORT', '50051')
ADMIN_ACCOUNT_ID = os.getenv('ADMIN_ACCOUNT_ID', 'admin@test')
ADMIN_PRIVATE_KEY = os.getenv(
    'ADMIN_PRIVATE_KEY', 'f101537e319568c765b2cc89698325604991dca57b9716b58016b253506cab70')

iroha = Iroha(ADMIN_ACCOUNT_ID)
net = IrohaGrpc('{}:{}'.format(IROHA_HOST_ADDR, IROHA_PORT))


def send_tx():
    rand_name = uuid.uuid4().hex
    rand_key = IrohaCrypto.private_key()
    domain = ADMIN_ACCOUNT_ID.split('@')[1]
    tx = iroha.transaction([
        iroha.command('CreateAccount',
                      account_name=rand_name,
                      domain_id=domain,
                      public_key=rand_key)
    ])
    IrohaCrypto.sign_transaction(tx, ADMIN_PRIVATE_KEY)
    net.send_tx(tx)
    print('tx is sent')


if __name__ == '__main__':
    query = iroha.blocks_query()
    IrohaCrypto.sign_query(query, ADMIN_PRIVATE_KEY)
    blocks = net.send_blocks_stream_query(
        query, timeout=120)  # timeout in seconds
    send_tx()
    print(next(blocks))