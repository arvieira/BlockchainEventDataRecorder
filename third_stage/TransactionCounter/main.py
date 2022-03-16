import json
import os
import sys


def list_dir(directory):
    return os.listdir(directory)


def parse_json_list(directory, files):
    transactions = 0
    blocks = 0
    latency = []

    for file in files:
        if file != '0000000000000001.json':
            data = json.load(open(f'{directory}/{file}'))

            transactions += len(data['blockV1']['payload']['transactions'])
            blocks += 1

            block_creation_time = data['blockV1']['payload']['createdTime']
            for transaction in data['blockV1']['payload']['transactions']:
                latency.append(int(block_creation_time) - int(transaction['payload']['reducedPayload']['createdTime']))

    return transactions, blocks, latency


def count_transactions(exp_name, directory, output_directory, run):
    directory = directory + '/'
    print(f'{exp_name}: ')

    sums = {}
    blocks = {}
    latency = {}
    total = 0

    vehicle_dirs = list_dir(directory)
    for vehicle_dir in vehicle_dirs:
        file = directory + vehicle_dir
        if vehicle_dir == 'Total.txt':
            total = open(file).readline().rstrip('\n')
            print(f"Total transactions: {total}")
        elif vehicle_dir == 'Queues.txt':
            queues = json.load(open(file))
            print(f'Queues: {queues}')
        else:
            counted, _, _ = parse_json_list(file, list_dir(file))
            if counted != 0:
                vehicle = vehicle_dir.replace('/', '')
                sums[vehicle], blocks[vehicle], latency[vehicle] = parse_json_list(file, list_dir(file))

    print(f'Consolidated Transactions: {sums}')
    print(f'Blocks: {blocks}\n')
    # print(f'Latencys: {latency}\n')

    if run == '1':
        f = open(f'{output_directory}/{exp_name}.json', 'w')
        f.write('[')
    else:
        f = open(f'{output_directory}/{exp_name}.json', 'a')
        f.write(', ')

    result = {
        'run': run,
        'total_transactions': total,
        'blocks': blocks,
        'consolidated_transactions': sums,
        'queues': queues,
        'latency': latency
    }

    json.dump(result, f)
    f.close()


if __name__ == '__main__':
    count_transactions(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
