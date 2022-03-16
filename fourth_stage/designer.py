import statistics

import matplotlib.pyplot as plt
from matplotlib import font_manager as fm
import pandas as pd
from dataframer import OUTPUT_DIR
import altair as alt


# Função para desenhar pontos e valores no gráfico de linhas
def draw_dots(data, ax, padding, key, color):
    for i, j in zip(data['Transactions Per Second'], data[key]):
        plt.scatter(i, j, s=50, c=color)
        z = j - padding
        ax.annotate(str(round(j, 2)), xy=(i, z), horizontalalignment='center', verticalalignment='center', color=color,
                    bbox=dict(boxstyle='round,pad=0.1', fc='w', ec='none'))


# # Função para desenhar pontos e valores no gráfico de linhas
# def draw_dots_bigger(data, ax, padding, key, color, fontsize):
#     for i, j in zip(data['Transactions Per Second'], data[key]):
#         plt.scatter(i, j, s=50, c=color)
#         z = j - padding
#         ax.annotate(str(round(j, 2)), xy=(i, z), fontsize=fontsize, horizontalalignment='center', verticalalignment='center', color=color,
#                     bbox=dict(boxstyle='round,pad=0.1', fc='w', ec='none'))


# Função para fazer os ajustes finais do floursish e imprimir
def adjust_and_print(fig_name, title, xlabel, ylabel, ax, legend=True, xticks=None):
    # Esse bloco inteiro serve para alinhar o título e colocar a fonte
    bbox = ax.get_yticklabels()[-1].get_window_extent()
    x, _ = ax.transAxes.inverted().transform([bbox.x0, bbox.y0])
    prop = fm.FontProperties(family=['serif'], weight=600, size=17)
    ax.set_title(title, ha='left', x=x, fontproperties=prop, pad=40)

    # Desenhando a legenda semelhante ao flourish
    if legend:
        ax.legend(frameon=False, loc='lower left', bbox_to_anchor=(x, 1), ncol=4)

    # Definindo os títulos dos eixos na cor cinza
    plt.xlabel(xlabel, color='grey')
    plt.ylabel(ylabel, color='grey')

    # Colocando o limite inferior no 0 para não dar distorção no gráfico
    ax.set_ylim([0, None])

    # Rotacionando os ticks e definindo a cor como cinza
    if xticks:
        plt.xticks(xticks, rotation=45, color='grey')
    else:
        plt.xticks(rotation=45, color='grey')
    plt.yticks(color='grey')

    # Desenhando o grid na cor cinza
    # Colocando os eixos cinza
    # Enviando o grid para trás das linhas
    plt.grid(axis='y', color='#dddddd')
    ax.spines['bottom'].set_color('#dddddd')
    ax.spines['top'].set_color('#dddddd')
    ax.spines['right'].set_color('#dddddd')
    ax.spines['left'].set_color('#dddddd')
    ax.set_axisbelow(True)

    # Salvando a figura no diretório de saída
    plt.savefig(f'{OUTPUT_DIR}/{fig_name}', format='png')

    # Exibindo o gráfico
    plt.show()


# # Função para fazer os ajustes finais do floursish e imprimir
# def adjust_and_print_bigger(fig_name, title, xlabel, ylabel, ax, fontsize, legend=True, xticks=None):
#     # Esse bloco inteiro serve para alinhar o título e colocar a fonte
#     bbox = ax.get_yticklabels()[-1].get_window_extent()
#     x, _ = ax.transAxes.inverted().transform([bbox.x0, bbox.y0])
#     prop = fm.FontProperties(family=['serif'], weight=600, size=fontsize+7)
#     ax.set_title(title, ha='left', x=x, fontproperties=prop, pad=40)
#
#     # Desenhando a legenda semelhante ao flourish
#     if legend:
#         ax.legend(frameon=False, loc='lower left', bbox_to_anchor=(x, 1), ncol=4, fontsize=fontsize)
#
#     # Definindo os títulos dos eixos na cor cinza
#     plt.xlabel(xlabel, color='grey', fontsize=fontsize)
#     plt.ylabel(ylabel, color='grey', fontsize=fontsize)
#
#     # Colocando o limite inferior no 0 para não dar distorção no gráfico
#     ax.set_ylim([0, None])
#
#     # Rotacionando os ticks e definindo a cor como cinza
#     if xticks:
#         plt.xticks(xticks, rotation=45, color='grey', fontsize=fontsize)
#     else:
#         plt.xticks(rotation=45, color='grey', fontsize=fontsize)
#     plt.yticks(color='grey', fontsize=fontsize)
#
#     # Desenhando o grid na cor cinza
#     # Colocando os eixos cinza
#     # Enviando o grid para trás das linhas
#     plt.grid(axis='y', color='#dddddd')
#     ax.spines['bottom'].set_color('#dddddd')
#     ax.spines['top'].set_color('#dddddd')
#     ax.spines['right'].set_color('#dddddd')
#     ax.spines['left'].set_color('#dddddd')
#     ax.set_axisbelow(True)
#
#     # Salvando a figura no diretório de saída
#     plt.savefig(f'{OUTPUT_DIR}/{fig_name}', format='pdf')
#
#     # Exibindo o gráfico
#     plt.show()


# Função que desenha o gráfico de linhas
# O padding_top é a distância entre o ponto na linha e o valor
def draw_line_chart(fig_name, title, xlabel, ylabel, data, padding_top):
    # Definindo o tamanho da imagem
    plt.rcParams["figure.figsize"] = (12, 8)

    # Essa parte de subplots está sendo usada somente para poder posicionar o título depois
    fig, ax = plt.subplots()

    # Mapa de cores para as linhas
    color_map = ['C10', 'C1', 'C2', 'C4']
    for i, veh_number in enumerate(data.columns[1:]):
        # Realizando a plotagem das linhas nas cores corretas segundo o flourish
        ax.plot(data['Transactions Per Second'], data[veh_number], color_map[i],
                label=f'{veh_number[0]} {veh_number[1:]}')

        # Desenhando os pontos e os valores na linha
        draw_dots(data, ax, padding_top, veh_number, color_map[i])
        # draw_dots_bigger(data, ax, padding_top, veh_number, color_map[i], 15)

    # Esse bloco inteiro serve para alinhar o título e colocar a fonte
    fig.canvas.draw()

    # Ajustando e imprimindo o gráfico
    adjust_and_print(fig_name, title, xlabel, ylabel, ax, legend=True,
                            xticks=list(data['Transactions Per Second']))
    # adjust_and_print_bigger(fig_name, title, xlabel, ylabel, ax, 15, legend=True, xticks=list(data['Transactions Per Second']))


# Função para desenhar o gráfico de barras empilhadas
# O padding_top é a distância do valor total e a barra na parte superior em letra preta
def draw_stacked_chart(fig_name, title, xlabel, ylabel, consolidated_transactions, queued, iroha_pending_transactions,
                       total_transactions, vehicles_number, padding_top):
    # Definindo o tamanho da imagem
    plt.rcParams["figure.figsize"] = (12, 8)

    # Criando o DataFrame com a combinação das tabelas de entrada
    data = []
    for index, value in enumerate(consolidated_transactions['Transactions Per Second']):
        data.append([value, consolidated_transactions[vehicles_number][index], queued[vehicles_number][index],
                     iroha_pending_transactions[vehicles_number][index]])
    df = pd.DataFrame(data,
                      columns=['Transactions Per Second', 'Consolidated Transactions', 'Vehicle Queued transactions',
                               'Iroha Pending Transactions'])

    # Realizando o plot direto pelo Dataframe
    ax = df.plot(x='Transactions Per Second', kind='bar', stacked=True, title='Teste',
                 color=['#80b659', '#eeb520', '#3377a0'], edgecolor='w')

    # Colocando os valores centralizados nas barras em letra branca com negrito
    for c in ax.containers:
        # Eu pego o maior total que vai ser exibido e divido a altura dele por 40.
        # Se a barra for menor que isso, eu não coloco o valor
        labels = [round(v.get_height(), 2) if v.get_height() > max(total_transactions[vehicles_number]) / 40 else '' for
                  v in c]
        ax.bar_label(c, labels=labels, label_type='center', color='w', fontweight=600)

    # Colocando os totais em letra preta na parte superior das barras
    for i in range(len(total_transactions['Transactions Per Second'])):
        plt.text(i, total_transactions[vehicles_number][i] + padding_top, total_transactions[vehicles_number][i],
                 ha='center', va='bottom', fontweight=600)

    # Ajustando e imprimindo o gráfico
    adjust_and_print(fig_name, title, xlabel, ylabel, ax, legend=True)


# Função para desenhar o gráfico de barras empilhadas
# O padding_top é a distância do valor total e a barra na parte superior em letra preta
def draw_histogram(fig_name, title, xlabel, ylabel, data, bins):
    # Definindo o tamanho da imagem
    plt.rcParams["figure.figsize"] = (12, 8)

    # Desenhando o histograma
    fig, ax = plt.subplots()
    _, _, bars = ax.hist(data, bins=bins, ec='w')
    plt.bar_label(bars)

    # Ajustando e imprimindo o gráfico
    adjust_and_print(fig_name, title, xlabel, ylabel, ax, legend=False)


def prepare_dataframe(data):
    # data = pd.read_excel(file)
    # data['Unnamed: 0'] = None
    # data = data.fillna('tps')
    # data['Transactions Per Second'] = data['Transactions Per Second'].apply(str) + data['Unnamed: 0']
    # data = data.drop(axis=1, columns='Unnamed: 0')
    cols = list(data.columns)
    cols[0] = ''
    data.columns = cols
    data = data.set_index('').T
    # print(data)

    return data


def prep_df(data, name):
    data = data.stack().reset_index()
    data.columns = ['c1', 'c2', 'values']
    data['Transaction Status'] = name
    # print (df)
    return data


def draw_grouped_stacked(fig_name, consolidated, queues, iroha_pending, total, fontsize, title_fontsize,
                         subtitle_font_size, title_padding, duration):
    consolidated = prepare_dataframe(consolidated)
    queues = prepare_dataframe(queues)
    iroha_pending = prepare_dataframe(iroha_pending)
    total = prepare_dataframe(total)

    iroha_pending = prep_df(iroha_pending, '1. Iroha Pending Transactions')
    consolidated = prep_df(consolidated, '3. Consolidated Transactions')
    queues = prep_df(queues, '2. Vehicle Queued Transactions')

    data = pd.concat([iroha_pending, queues, consolidated])

    alt.Chart(data).mark_bar().encode(

        # tell Altair which field to group columns on
        x=alt.X('c2:N', title='TPS', sort=None),

        # tell Altair which field to use as Y values and how to calculate
        y=alt.Y('sum(values):Q',
                axis=alt.Axis(
                    grid=True,
                    title='Transactions')),

        # tell Altair which field to use to use as the set of columns to be  represented in each group
        column=alt.Column('c1:N', title='Number of Vehicles',
                          header=alt.Header(titleFontSize=fontsize, labelFontSize=fontsize)),

        # tell Altair which field to use for color segmentation
        color=alt.Color('Transaction Status:N', legend=alt.Legend(
            orient='none',
            legendX=8, legendY=14,
            # direction='horizontal',
            titleAnchor='start'),
            scale=alt.Scale(
                # make it look pretty with an enjoyable color pallet
                range=['#3377a0', '#eeb520', '#80b659'],
            ),
        )) \
        .configure_view(
        # remove grid lines around column clusters
        strokeOpacity=0,
        width=110,
        height=300,
    ) \
        .configure_axis(labelFontSize=fontsize) \
        .configure_legend(titleFontSize=fontsize, labelFontSize=fontsize) \
        .properties(
        title=alt.TitleParams(
            text=["Transactions Report for 3, 5, 7 and 9 Vehicles"],
            subtitle=[f"{duration} Seconds Experiments"],
            color="black",
            subtitleColor="grey",
            fontSize=title_fontsize,
            subtitleFontSize=subtitle_font_size,
            dy=title_padding
        )
    ).save(f'{OUTPUT_DIR}/{fig_name}')
