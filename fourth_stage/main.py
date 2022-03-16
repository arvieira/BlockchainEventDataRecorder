import math
import sys
from dataframer import tabulate
from designer import draw_line_chart, draw_stacked_chart, draw_histogram, draw_grouped_stacked

# Função principal
if __name__ == '__main__':
    # Inicia a tabulação passando o diretório que contem os outputs das simulações
    totals, consolidateds, percentages, tps, queues, blocks, iroha_pending, latency = tabulate(sys.argv[1])

    # Inicia a criação dos desenhos
    # Eu posso ter várias durações para os experimentos.
    # Cada key é uma duração
    for key in totals:
        # Total de transações
        draw_line_chart(
            f'{key}_TotalTransactions.png',
            f'Total Transactions for {key} Seconds Experiment',
            'Transactions Per Second',
            'Total Transactions',
            totals[key], 0
        )

        # Transações consolidadas
        draw_line_chart(
            f'{key}_ConsolidatedTransactions.png',
            f'Consolidated Transactions for {key} Seconds Experiment',
            'Transactions Per Second',
            'Consolidated Transactions',
            consolidateds[key], 0
        )

        # Porcentagem de transações consolidadas
        draw_line_chart(
            f'{key}_ConsolidatedTransactionsPercentage.png',
            f'Consolidated Transactions Percentage for {key} Seconds Experiment',
            'Transactions Per Second',
            'Consolidated Transactions Percentage',
            percentages[key], 0
        )

        # Transações consolidadas por segundo
        draw_line_chart(
            f'{key}_ConsolidatedTransactionsPerSecond.png',
            f'Consolidated Transactions Per Second for {key} Seconds Experiment',
            'Transactions Per Second',
            'Consolidated Transactions Per Second',
            tps[key], 0
        )

        # Blocos gerados
        draw_line_chart(
            f'{key}_Blocks.png',
            f'Number of Blocks Generated for {key} Seconds Experiment',
            'Transactions Per Second',
            'Number of Blocks',
            blocks[key], 0
        )

        # Imprimindo os gráficos de barras empilhadas
        # Cada número de veículos é um gráfico independente
        for veh in totals[key].columns[1:]:
            # Temos 3 estados possíveis para uma transação no experimento
            # - Ela foi consolidada no ledger
            # - Ela ficou enfileirada no DispatcherBroker e não foi enviada ao Iroha
            # - Ela foi enviada ao Iroha mas este não a consolidou
            # Esses gráficos podem indicar quem é o responsável pelas perdas de transações
            draw_stacked_chart(
                f'{key}_{veh}_TransactionsReport.png',
                f'Transactions Report for {veh[0]} Vehicles {key} Seconds Experiment',
                'Transactions Per Second', 'Transactions',
                consolidateds[key], queues[key], iroha_pending[key], totals[key], veh, 0)

        for veh in latency[key]:
            for transactions_per_second in latency[key][veh]['Transactions Per Second'].unique():
                data = latency[key][veh].loc[latency[key][veh]['Transactions Per Second'] == transactions_per_second][veh]

                # Um bin para cada valor único
                # bins = len(latency[key][veh]
                #     .loc[latency[key][veh]['Transactions Per Second'] == transactions_per_second][veh].unique())

                # Número fixo de bins
                bins = 10

                # Bins de largura fixa
                # bins = math.ceil((data.max() - data.min())/100)

                draw_histogram(
                    f'{key}_{veh}_{transactions_per_second}_LatencyHistogram.png',
                    f'Frequency of Latency for {veh}, {transactions_per_second}tps and {key} Experiment',
                    'Latency(ms)', 'Frequency',
                    data,
                    bins
                )

        # Salvando o arquivo do grouped stacked chart em formato svg vetorial
        draw_grouped_stacked(f'{key}_GroupedStacked.svg', consolidateds[key], queues[key], iroha_pending[key],
                             totals[key], 10, 20, 16, -10, key)
