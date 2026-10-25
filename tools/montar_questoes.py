"""Monta app/src/main/assets/questoes.json com questões reais do ENEM (API enem.dev).

Uso: python tools/montar_questoes.py
Lê o bruto de tools/enem_raw/{ano}.json (gerado por baixar_enem.py).
A letra "cega" de cada questão é a resposta dada antes de consultar o gabarito. O script
confere a letra cega, o gabarito oficial do INEP (GABARITO_INEP) e o da API, que erra em
alguns anos: se as três não baterem, ou se a questão estiver em ANULADAS, nada é gravado.

Tamanho: para a leitura no celular, cada questão (texto-base + comando + alternativas) tem no
máximo LIMITE_PALAVRAS palavras; acima disso o script também não grava nada.

Imagens e fórmulas (Matemática):
- As figuras da API são baixadas para app/src/main/assets/imagens/ e o texto ganha
  um marcador [[imagem:arquivo.png]] no lugar delas. As que vieram ruins da API foram
  recortadas da prova oficial por recortar_figuras.py e ficam em tools/figuras/: essas
  são copiadas no lugar do download.
- A API não tem LaTeX: as fórmulas vêm como PNGs minúsculos (uns 90 x 70 px) que
  ficam borrados no celular. Por isso as alternativas que são fórmula foram
  redigitadas em LaTeX (FORMULAS) e são renderizadas aqui em alta resolução com o
  matplotlib, virando [[formula:arquivo.png]]. Precisa de: pip install -r tools/requirements.txt

Lições: cada questão leva o tempo estimado em "segundos_estimados" (ver PALAVRAS_POR_MINUTO e
RESOLUCAO_S), e as lições agrupam de 3 a 5 questões para dar uns 4 minutos (ALVO_LICAO_S).

Texto como no caderno: o texto da API foi conferido contra a prova oficial do INEP (caderno azul).
CORRECOES conserta os erros de OCR, SEM_PONTO devolve o ponto final que a API tirou de algumas
alternativas, e ajustar_inicial() devolve a minúscula das alternativas que continuam a frase do
comando (a API põe maiúscula em todas). Quando a API joga texto-base, fonte e comando todos no
enunciado (context vazio), campos() separa: o último parágrafo é o comando.

Migração: o questoes.json leva em "ids_antigos" o id de cada questão em cada versão antiga do
banco (LICOES_V1, LICOES_V2) -> id atual, ou "" se a questão saiu do banco.
"""
import json
import re
import shutil
import sys
import urllib.request
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
RAW = Path(__file__).parent / "enem_raw"
FIGURAS = Path(__file__).parent / "figuras"  # recortes da prova oficial (recortar_figuras.py)
ASSETS = RAIZ / "app" / "src" / "main" / "assets"
SAIDA = ASSETS / "questoes.json"
IMAGENS = ASSETS / "imagens"

IMAGEM_MD = re.compile(r"!\[[^\]]*\]\(([^)\s]+)\)")
# 16 pt a 288 dpi: 1 em = 64 px. O app escala a fórmula pelo tamanho da fonte do texto
# (TextoRico.PX_POR_EM), então ela fica nítida até em telas de 4x.
FORMULA_PT = 16
FORMULA_DPI = 288
FORMULA_FONTE = "stixsans"
COR_TEXTO = (0x3C, 0x3C, 0x3C)  # @color/texto_principal

# Tempo estimado de uma questão = leitura (texto-base + enunciado + alternativas) + resolução.
# Vai para o JSON em "segundos_estimados" e é o que a trilha mostra ("~4 min"). Mudou um número?
# Rode este script e o validar_questoes.py, que avisa se alguma lição saiu do alvo.
PALAVRAS_POR_MINUTO = 180
RESOLUCAO_S = {"LINGUAGENS": 20, "HUMANAS": 30, "NATUREZA": 30, "MATEMATICA": 75}
EXTRA_FIGURA_S = 15  # questão com pelo menos uma figura ou fórmula
ALVO_LICAO_S = 4 * 60  # quanto uma lição deve levar, pela estimativa
MIN_QUESTOES, MAX_QUESTOES = 3, 5  # questões por lição
PALAVRA = re.compile(r"\w+(?:[-’']\w+)*")
MARCADOR = re.compile(r"\[\[(?:imagem|formula):[^\]]+\]\]")
# Palavras por questão (texto-base + comando + alternativas). Linguagens tem mais folga porque
# quase não há questão só de texto com até 100 palavras nessa área.
LIMITE_PALAVRAS = {"LINGUAGENS": 140, "HUMANAS": 100, "NATUREZA": 100, "MATEMATICA": 100}

# área -> [(título da lição, [(ano-index, letra_cega, explicação), ...]), ...]
# Cada lição tem de 3 a 5 questões e mira ALVO_LICAO_S pela estimativa de tempo.
AREAS = [
    ("LINGUAGENS", "LIN", [
        ("Funções da linguagem", [
            ("2019-24", "C", "A ladainha é uma oração feita de pedidos repetidos, e a canção imita essa forma. O convite “Vamos pedir piedade” e o pedido “Lhes dê grandeza” chamam o interlocutor a participar: é a sequência injuntiva."),
            ("2023-28", "C", "O eu lírico fala da própria voz: o nó na garganta, o grito no lugar da fala e o “Calo.” do fim. O que ele expressa é a dificuldade de se comunicar."),
            ("2022-26", "C", "Na função emotiva, quem fala mostra o que sente, em primeira pessoa. Em “A cidade não mora mais em mim”, o eu lírico revela que já não se sente parte da cidade."),
            ("2013-100", "D", "“Pro céu”, “Levanta aí”, “Se liga aí” e “pode crer” são jeitos de falar do dia a dia. Essa linguagem coloquial deixa a letra espontânea, como uma conversa."),
        ]),
        ("Palavras e sentidos", [
            ("2016-102", "A", "Mandinga passou a significar feitiçaria por causa de como os navegadores portugueses viam os africanos daquela costa. O sentido nasceu de um contexto histórico e social."),
            ("2017-15", "E", "O menino escreve Gerimum com g porque foi assim que o lugar dele foi registrado. A grafia mostra o laço afetivo com a terra de origem, e não uma regra da norma."),
            ("2017-27", "B", "Cada par junta palavras quase iguais que servem para situações diferentes: “gestante” na fila preferencial, “salvar” no computador. A diferença está no uso de cada uma."),
            ("2011-125", "B", "Para o texto, o jeito de falar português é “a melhor expressão da brasilidade”. A língua aparece como parte central da identidade nacional."),
        ]),
        ("Gêneros e construção do texto", [
            ("2016-135", "B", "A frase “O PM pensou que…” fica sem fim, e o leitor completa o que não foi dito: o policial confundiu a brincadeira e atirou. As reticências marcam essa informação implícita."),
            ("2013-132", "C", "A pergunta era sobre a palavra certa, “lagarto” ou “largato”. O compadre responde sobre o bicho, como se a dúvida fosse de visão, e ignora o sentido linguístico da pergunta."),
            ("2016-104", "D", "Depois dos dois pontos vem a explicação do “erro”: a essência do conto está entre o texto e o leitor. Os dois pontos abrem um argumento que esclarece o que veio antes."),
            ("2016-125", "A", "O poema copia a forma de uma receita: “Tome-se”, “Deita-se”, ingredientes e quantidades. Ele leva o tom de instrução (prescritivo) da receita para dentro da poesia."),
        ]),
        ("Leitura literária", [
            ("2020-39", "C", "O hino descreve o céu azul, as matas verdes e o Cruzeiro do Sul que estão na bandeira, com palavras como “formoso” e “puríssimo”. Descrever assim é valorizar esses elementos."),
            ("2014-100", "D", "“O que ela quer da gente é coragem” é uma frase curta que resume uma verdade sobre a vida. É assim que funciona um aforismo."),
            ("2017-30", "D", "O eu lírico se vê nos outros e diz que só “estamos em paz” quando estamos “em nós”. A identidade se forma na relação com o outro, colocando-se no lugar dele: é a empatia."),
            ("2018-36", "B", "Para o eu lírico, a volta do rio era a imagem de “uma cobra de vidro”. Com o nome técnico, enseada, essa imagem pessoal se perdeu: “o nome empobreceu a imagem”."),
        ]),
        ("Corpo, arte e cultura", [
            ("2013-97", "D", "Huizinga define o jogo como algo com fim em si mesmo, cheio de tensão e alegria e diferente da vida comum. A fruição vem desse caráter lúdico, e não da competição ou da técnica."),
            ("2019-35", "A", "Anorexia e bulimia são transtornos alimentares ligados ao medo de engordar e a uma imagem distorcida do próprio corpo. Elas nascem justamente da preocupação exagerada com o peso."),
            ("2014-123", "C", "No choro e no samba, o cavaquinho e o violão são a base do conjunto. Nos outros pares, a música se apoia em outros instrumentos, como os tambores do maracatu e do carimbó, a sanfona do baião e do xote e os metais do frevo."),
            ("2019-25", "B", "Uma escola de samba desfilou em homenagem ao Rei do Baião. O evento junta dois gêneros brasileiros: o samba-enredo e o baião."),
        ]),
    ]),
    ("HUMANAS", "HUM", [
        ("Filosofia e ética", [
            ("2018-79", "C", "Para Epicuro, quem não se contenta com pouco nunca fica satisfeito. A máxima valoriza a temperança: dominar os desejos e ficar com o necessário."),
            ("2022-88", "E", "Para Sêneca, a filosofia ensina a manter o ânimo firme até diante da morte e da doença. Isso só é possível controlando as emoções: é a moderação das paixões."),
            ("2021-82", "B", "Sócrates conduz a conversa com perguntas para chegar à definição do que é a virtude. Esse jogo de perguntas e respostas é o método dialético."),
            ("2016-1", "B", "Para Schopenhauer, satisfazer um desejo é como a esmola dada ao mendigo: alivia hoje, e a fome volta amanhã. A felicidade está na resignação, em não depender do mundo de fora: a independência interior."),
        ]),
        ("Política e cidadania", [
            ("2019-79", "A", "Nas cidades-estado antigas, ser cidadão com plenos direitos dependia da posse de terra. Onde poucos eram donos de terra, como nas aristocracias e oligarquias, havia poucos cidadãos."),
            ("2020-87", "E", "Na democracia direta de Atenas, os próprios cidadãos decidiam na assembleia, sem eleger representantes. O sistema abriu a eles a participação no exercício do poder."),
            ("2014-14", "B", "Para Habermas, uma norma só vale se todos os afetados por ela chegarem a um acordo num diálogo. É a razão comunicativa, que exige consenso."),
            ("2022-83", "B", "Apátrida é a pessoa sem nacionalidade reconhecida por nenhum país. As irmãs não puderam ser registradas nem no Líbano, onde nasceram, nem na Síria, país dos pais."),
        ]),
        ("Terra, trabalho e Estado no Brasil", [
            ("2019-81", "B", "No século XVIII, a mineração de ouro em Minas Gerais passou a exigir cada vez mais escravizados. O Rio de Janeiro, principal porto de ligação com as minas, virou a grande porta de entrada do tráfico, enquanto a Bahia se estabilizou."),
            ("2017-80", "C", "Depois da Lei de Terras, só quem tinha dinheiro podia ter terra. Ex-escravizados e pobres ficaram de fora, e a terra continuou nas mãos de poucos: a concentração fundiária."),
            ("2019-80", "D", "Na Revolta da Vacina, o povo reagiu à vacinação obrigatória imposta sem diálogo, com agentes entrando nas casas. Questionava o arbítrio do governo sobre a vida das pessoas."),
            ("2017-50", "C", "Para os povos indígenas, a terra é a base do modo de vida, da língua e das tradições. Por isso a luta pela demarcação liga o território à sobrevivência da cultura."),
        ]),
        ("Geografia física", [
            ("2014-13", "B", "Meio-dia nos Estados Unidos e pôr do sol na França ao mesmo tempo: são os fusos horários. Eles existem porque a Terra gira em torno do próprio eixo, a rotação."),
            ("2020-84", "E", "Quando a placa oceânica mergulha sob a continental, as rochas são comprimidas e dobradas, formando cordilheiras jovens: os dobramentos modernos, como os Andes."),
            ("2021-71", "A", "Rochas são desgastadas, os fragmentos são transportados e depositados nas partes baixas, formando novas rochas. Esse caminho é o ciclo sedimentar."),
            ("2015-30", "B", "A água da chuva acumulada no solo deixa a encosta mais pesada e instável. Onde a drenagem é eficiente, a água escoa e o deslizamento fica menos provável."),
        ]),
        ("Sociedade e economia", [
            ("2020-78", "C", "O toyotismo produz só o que o mercado pede, sem grandes estoques (o just in time). Ajustar a produção à demanda cortava custos num momento de crise."),
            ("2019-88", "C", "No bônus demográfico, há muita gente em idade de trabalhar. Para aproveitar essa fase, o Estado precisa investir na qualificação da mão de obra."),
            ("2015-12", "E", "Muitas indústrias saíram das grandes metrópoles atrás de custos menores em outras cidades e regiões. Os empregos foram junto, e o crescimento dos grandes centros desacelerou."),
            ("2023-64", "D", "Ela fala do clima esquentando, dos animais sumindo e dos rios morrendo. São efeitos da exploração predatória da natureza, um problema que atinge o mundo todo."),
        ]),
    ]),
    ("NATUREZA", "NAT", [
        ("Ecologia", [
            ("2013-80", "B", "As algas fazem fotossíntese: são os produtores do manguezal. A produção primária, que sustenta o resto do ecossistema, depende delas."),
            ("2019-97", "D", "Cutias da mesma espécie disputam o mesmo recurso, as sementes enterradas. Uma rouba a reserva da outra: é uma relação de competição."),
            ("2021-127", "C", "As preguiças-gigantes comiam os frutos e espalhavam as sementes. Com a extinção delas, a dispersão das sementes diminuiu e a flora ficou mais restrita."),
            ("2017-111", "E", "O mercúrio não é eliminado e vai se acumulando a cada nível da cadeia alimentar. Os botos estão no topo, então concentram mais: é a biomagnificação (ou magnificação trófica)."),
        ]),
        ("Física no cotidiano", [
            ("2015-86", "D", "A mesma nota tem a mesma frequência. O que diferencia a flauta do piano é o timbre, ou seja, a forma da onda de cada instrumento."),
            ("2013-52", "E", "Os aparelhos eletrônicos emitem ondas de frequência próxima à do rádio do avião. Ondas de frequências próximas podem interferir na comunicação dos pilotos."),
            ("2023-108", "D", "A bobina do fogão gera um campo magnético variável, que induz corrente elétrica no fundo metálico da panela (indução eletromagnética)."),
            ("2020-109", "E", "Na panela de pressão, a temperatura da água depende da pressão, que a válvula mantém. Com o vapor já saindo, fogo alto não esquenta mais: só gasta gás à toa."),
        ]),
        ("Química no cotidiano", [
            ("2013-81", "A", "As impurezas grudam nas partículas de sulfato de alumínio, que ficam suspensas na água. Das alternativas, só a flotação separa esse material: bolhas de ar se prendem aos flocos e os levam até a superfície, de onde são retirados."),
            ("2022-126", "B", "Depois da coagulação, a sujeira forma flocos pesados que descem para o fundo dos tanques. É na decantação que sai a maior parte dos sólidos."),
            ("2020-92", "D", "Os polímeros biodegradáveis são decompostos por microrganismos em pouco tempo. Os convencionais levam centenas de anos para se degradar no ambiente."),
            ("2023-92", "B", "A palidez é sinal de anemia, muitas vezes por falta de ferro. A panela de ferro e o prego soltavam ferro no feijão, o que ajudava a evitar a doença."),
        ]),
        ("Genética e biotecnologia", [
            ("2020-115", "B", "A soja recebeu um gene de outra espécie para produzir a cianovirina-N. Um organismo que recebe gene de outra espécie é transgênico."),
            ("2015-74", "A", "Boyer colocou na bactéria o trecho de DNA humano com a receita da insulina. A bactéria passou a ler esse gene e a fabricar a proteína."),
            ("2015-66", "E", "O cariótipo tem 47 cromossomos, com um 18 a mais. Quando sobra ou falta um cromossomo, a alteração é numérica, do tipo aneuploidia."),
            ("2014-89", "D", "As células do cordão umbilical são células-tronco. Elas podem se diferenciar em vários tipos de células especializadas, o que permite recuperar tecidos."),
        ]),
        ("Corpo humano e saúde", [
            ("2019-133", "A", "O rim filtra o sangue e elimina a ureia, resíduo tóxico do metabolismo. É isso que a hemodiálise faz hoje e que o rim biônico fará."),
            ("2021-114", "B", "O martelo bate no tendão logo abaixo da patela, e o impulso nervoso vai até a medula espinal, que manda a resposta direto ao músculo da coxa. É um arco reflexo: a medula coordena a resposta sem depender do encéfalo."),
            ("2015-67", "D", "Com sede e sem água, o corpo libera mais ADH e os rins reabsorvem mais água. Sai menos urina, e ela fica mais concentrada em sais."),
            ("2018-96", "B", "Os ácidos biliares têm uma parte que se liga à água e outra que se liga à gordura. Como um detergente, quebram a gordura em gotinhas, o que facilita a digestão."),
        ]),
    ]),
    ("MATEMATICA", "MAT", [
        ("Porcentagem e proporção", [
            ("2022-164", "B", "Um dia tem 24 × 60 = 1 440 minutos, então 60 dias têm 86 400 minutos, ou 86 400 ÷ 48 = 1 800 acionamentos. Cada um libera 360 ÷ 1 800 = 0,2 mL."),
            ("2021-165", "D", "A cada 20 km o carro gastava 20 ÷ 16 = 1,25 L. Com o motor novo gasta 1,25 − 0,1 = 1,15 L, e 20 ÷ 1,15 ≈ 17,4 km/L."),
            ("2022-178", "E", "Com o dobro do diâmetro, o volume fica 2³ = 8 vezes maior. Os 150 docinhos grandes valem 150 × 8 = 1 200 pequenos, ou 1 200 ÷ 50 = 24 porções."),
        ]),
        ("Áreas e escala", [
            ("2022-161", "A", "No desenho, o L se divide em dois retângulos: 16 × 5 = 80 cm² e 4 × (18,4 − 5) = 4 × 13,4 = 53,6 cm², total de 133,6 cm². Na escala 1 : 50, as áreas ficam 50² = 2 500 vezes maiores: 133,6 × 2 500 = 334 000 cm². Como 1 m² = 10 000 cm², são 33,4 m²."),
            ("2023-155", "D", "A piscina tem lado √400 = 20 m. Com 5 m de calçada de cada lado, o quadrado todo tem 30 m de lado e 900 m². A calçada ocupa 900 − 400 = 500 m²."),
            ("2020-138", "B", "A caixa real tem 28 080 L = 28 080 000 cm³, e a da maquete, 2 × 3,51 × 4 = 28,08 cm³. O volume real é 1 000 000 = 100³ vezes maior, então as medidas são 100 vezes maiores: escala 1 : 100."),
        ]),
        ("Volumes e superfícies", [
            ("2020-161", "B", "A altura de B é 25%, ou um quarto, da de A. Volumes iguais: πR²h = πr²(h/4), então r² = 4R² e r = 2R: com um quarto da altura, o raio precisa dobrar."),
            ("2021-170", "C", "A caneca é um tronco de cone de raios R = 5 cm e r = 4 cm (metade dos diâmetros). V = πh(R² + Rr + r²)/3 = 3 × 12 × (25 + 20 + 16)/3 = 12 × 61 = 732 cm³, ou seja, 732 mL."),
            ("2023-165", "E", "Pisos e espelhos somam 3 × 1,0 × 0,25 + 3 × 1,0 × 0,20 = 0,75 + 0,60 = 1,35 m². Cada parede lateral é uma escadinha de três colunas de 0,25 m de largura e 0,20, 0,40 e 0,60 m de altura: 0,25 × 1,20 = 0,30 m², e há uma de cada lado. Total: 1,35 + 2 × 0,30 = 1,95 m²."),
        ]),
        ("Frações e contagem", [
            ("2020-170", "C", "São 4 + 6 + 6 = 16 partes, e cada sócio deve ficar com 16/3. Antônio precisa de 16/3 − 4 = 4/3 de parte, 2/3 vindos de cada sócio. Como cada um tem 6 partes, isso é (2/3) ÷ 6 = 1/9 do capital dele."),
            ("2019-156", "C", "Cada canhoto precisa de um parceiro destro: o primeiro tem 6 opções e o segundo, 5. Os 4 que sobram formam duas duplas de 3 jeitos: um deles escolhe o parceiro entre 3, e os outros dois ficam juntos. Total: 6 × 5 × 3 = 90."),
            ("2020-141", "A", "De 200 a 299, o 2 aparece na centena 100 vezes. Na dezena (x20 a x29) e na unidade (x02, x12…) ele aparece 10 vezes em cada uma das 3 centenas: 30 + 30 = 60. Total: 160 peças."),
        ]),
        ("Tabelas e gráficos", [
            ("2023-137", "C", "Média ponderada pelo número de funcionários: (75 × 2 000 + 25 × 7 000) ÷ 100 = 325 000 ÷ 100 = 3 250 reais."),
            ("2022-149", "B", "São 1 000 famílias, então a mediana é a média entre a 500ª e a 501ª. Acumulando, até 1 criança há exatamente 500 famílias, e a 501ª já tem 2. Mediana: (1 + 2) ÷ 2 = 1,5."),
            ("2022-138", "D", "Só entram os meses com lucro: jan. 3, mar. 1, maio 5, jun. 3, jul. 2, set. 4, nov. 1 e dez. 4. Em ordem: 1, 1, 2, 3, 3, 4, 4, 5. A mediana é (3 + 3) ÷ 2 = 3."),
            ("2021-162", "A", "Até 5 000 o imposto é zero. Em 10 000 ele vale 10% de 5 000 = 500 e, em 15 000, 500 + 30% de 5 000 = 2 000. O segundo trecho de subida é bem mais íngreme."),
        ]),
        ("Funções e equações", [
            ("2019-168", "D", "Além do gerente, há X − 1 diaristas, e cada um recebe 2 × 80 = 160 reais por semana. Y = 1 000 + 160(X − 1) = 160X + 840."),
            ("2021-173", "C", "O menor número de lavagens sai quando todas são completas: 300 ÷ 35 ≈ 8,6. Oito lavagens dão só 280 reais, então são necessárias 9."),
            ("2023-162", "D", "A parte fixa é 500, e cada metro cúbico custa 250, então x metros cúbicos custam 250x. Somando: y = 250x + 500."),
            ("2023-175", "B", "Seja a o preço do tíquete azul e v o do vermelho. Duas cartelas azuis e uma vermelha têm 18 tíquetes azuis e 9 vermelhos: 18a + 9v = 32,40. E a − v = v + 0,05 dá a = 2v + 0,05. Substituindo: 36v + 0,90 + 9v = 32,40, então 45v = 31,50 e v = 0,70. A cartela vermelha custa 9 × 0,70 = 6,30."),
        ]),
    ]),
]

# Agrupamentos das versões antigas do banco, só para migrar o progresso salvo: o questoes.json
# leva em "ids_antigos" o id de cada questão em cada versão -> id atual ("" se ela saiu).
# Versão 1: 4 lições de 5 por área.
LICOES_V1 = {
    "LIN": [
        ["2019-29", "2020-43", "2022-11", "2023-9", "2023-39"],
        ["2019-24", "2020-41", "2022-6", "2022-14", "2023-21"],
        ["2019-13", "2020-11", "2022-31", "2022-33", "2023-31"],
        ["2020-35", "2020-36", "2021-31", "2023-6", "2023-42"],
    ],
    "HUM": [
        ["2020-81", "2019-81", "2021-52", "2021-73", "2023-80"],
        ["2022-86", "2021-82", "2019-65", "2020-89", "2023-54"],
        ["2019-62", "2021-66", "2022-48", "2022-83", "2023-60"],
        ["2019-89", "2020-82", "2020-84", "2021-71", "2023-53"],
    ],
    "NAT": [
        ["2019-97", "2020-95", "2021-127", "2022-99", "2023-91"],
        ["2019-110", "2020-115", "2021-132", "2022-98", "2023-125"],
        ["2020-111", "2020-133", "2021-97", "2022-126", "2023-108"],
        ["2019-133", "2020-108", "2021-114", "2022-104", "2023-106"],
    ],
    "MAT": [
        ["2019-138", "2022-164", "2020-170", "2022-161", "2023-161"],
        ["2023-155", "2019-147", "2020-161", "2021-170", "2023-165"],
        ["2023-137", "2022-149", "2022-138", "2023-168", "2022-141"],
        ["2019-168", "2021-173", "2019-139", "2021-162", "2019-165"],
    ],
}
# Versão 2 (release 1.0-demo): lições de 3 a 5 questões, uns 4 minutos cada.
LICOES_V2 = {
    "LIN": [
        ["2019-29", "2022-11", "2023-9"],
        ["2020-41", "2022-14", "2023-21"],
        ["2019-24", "2022-6", "2022-33"],
        ["2019-13", "2020-11", "2022-31", "2023-31"],
        ["2020-36", "2021-31", "2023-6", "2023-42"],
        ["2020-43", "2023-39", "2020-35"],
    ],
    "HUM": [
        ["2019-65", "2021-73", "2023-80"],
        ["2022-86", "2021-82", "2020-89", "2023-54"],
        ["2020-81", "2019-81", "2021-52", "2022-48"],
        ["2019-62", "2021-66", "2022-83", "2023-60"],
        ["2019-89", "2020-82", "2020-84", "2021-71", "2023-53"],
    ],
    "NAT": [
        ["2019-97", "2020-95", "2021-127", "2022-99", "2023-91"],
        ["2019-110", "2021-132", "2023-125"],
        ["2020-111", "2020-133", "2023-108"],
        ["2020-115", "2022-98", "2023-106"],
        ["2021-97", "2022-126", "2020-108"],
        ["2019-133", "2021-114", "2022-104"],
    ],
    "MAT": [
        ["2019-138", "2022-164", "2023-161"],
        ["2022-161", "2023-155", "2019-147"],
        ["2020-161", "2021-170", "2023-165"],
        ["2020-170", "2023-168", "2022-141"],
        ["2023-137", "2022-149", "2022-138", "2021-162"],
        ["2019-168", "2021-173", "2019-139", "2019-165"],
    ],
}
VERSOES_ANTIGAS = {"1": LICOES_V1, "2": LICOES_V2}
VERSAO = 3  # versão deste banco; o app guarda a do progresso para saber de onde migrar

# Gabarito oficial do INEP (PDF do gabarito do caderno azul de cada ano) das questões do banco.
# A API enem.dev erra o gabarito de algumas questões (ex.: 2021-15, 2021-139 e 2022-143), por
# isso a letra da API só vale quando bate com esta.
GABARITO_INEP = {
    "2011-125": "B", "2013-52": "E", "2013-80": "B", "2013-81": "A", "2013-97": "D",
    "2013-100": "D", "2013-132": "C", "2014-13": "B", "2014-14": "B", "2014-89": "D",
    "2014-100": "D", "2014-123": "C", "2015-12": "E", "2015-30": "B", "2015-66": "E",
    "2015-67": "D", "2015-74": "A", "2015-86": "D", "2016-1": "B", "2016-102": "A", "2016-104": "D",
    "2016-125": "A", "2016-135": "B", "2017-15": "E", "2017-27": "B", "2017-30": "D",
    "2017-50": "C", "2017-80": "C", "2017-111": "E", "2018-36": "B", "2018-79": "C",
    "2018-96": "B", "2019-24": "C", "2019-25": "B", "2019-35": "A", "2019-79": "A", "2019-80": "D",
    "2019-81": "B", "2019-88": "C", "2019-97": "D", "2019-133": "A", "2019-156": "C",
    "2019-168": "D", "2020-39": "C", "2020-78": "C", "2020-84": "E", "2020-87": "E", "2020-92": "D",
    "2020-109": "E", "2020-115": "B", "2020-138": "B", "2020-141": "A", "2020-161": "B",
    "2020-170": "C", "2021-71": "A", "2021-82": "B", "2021-114": "B", "2021-127": "C",
    "2021-162": "A", "2021-165": "D", "2021-170": "C", "2021-173": "C", "2022-26": "C",
    "2022-83": "B", "2022-88": "E", "2022-126": "B", "2022-138": "D", "2022-149": "B",
    "2022-161": "A", "2022-164": "B", "2022-178": "E", "2023-28": "C", "2023-64": "D",
    "2023-92": "B", "2023-108": "D", "2023-137": "C", "2023-155": "D", "2023-162": "D",
    "2023-165": "E", "2023-175": "B",
}

# Questões anuladas pelo INEP ou abandonadas na correção pela TRI (IN_ITEM_ABAN = 1 nos
# microdados, tabela ITENS_PROVA, caderno azul) nos anos que o banco usa. Nunca entram no banco.
ANULADAS = {
    "2009-101",
    "2011-71",
    "2014-51", "2014-53", "2014-126", "2014-159",
    "2016-25", "2016-113", "2016-129",
    "2018-163",
    "2019-33", "2019-48", "2019-90", "2019-122", "2019-145",
    "2020-135", "2020-157",
    "2021-24", "2021-43", "2021-118", "2021-138",
    "2022-64", "2022-112", "2022-113", "2022-119", "2022-134", "2022-141", "2022-157",
    "2023-161", "2023-164",
}

# Alternativas que na API são imagem de fórmula, redigitadas em LaTeX (mathtext do matplotlib).
# Conferidas uma a uma contra as imagens originais.
FORMULAS = {
    "2020-170": [r"\dfrac{1}{2}", r"\dfrac{1}{3}", r"\dfrac{1}{9}", r"\dfrac{2}{3}", r"\dfrac{4}{3}"],
    "2020-161": [r"\dfrac{R}{2}", r"2R", r"4R", r"5R", r"16R"],
}

# Erros de OCR da API (símbolo perdido, unidade trocada, palavra cortada, parágrafo
# grudado). Cada trecho antigo precisa aparecer exatamente uma vez no campo. O campo é
# "context", "alternativesIntroduction" ou a letra da alternativa ("A" a "E").
CORRECOES = {
    "2011-125": [("context", "Ano II, n° 6", "Ano II, nº 6")],
    "2013-52": [("context", "todos o aparelhos", "todos os aparelhos")],
    "2013-132": [("context", "frente do carro.  \nUm dos", "frente do carro. Um dos")],
    # Quando o comando é um "complete a frase", a API põe ":" no fim e maiúscula nas alternativas.
    "2014-14": [("alternativesIntroduction", "pelo(a):", "pelo(a)"),
                ("D", "que aumento o poder", "que aumenta o poder")],
    # No caderno, a frase sobre o romance abre o comando, não fecha o texto-base.
    "2014-100": [("context", "\n\nNo romance _Grande sertão: veredas_, o protagonista Riobaldo narra sua "
                  "trajetória de jagunço.", ""),
                 ("alternativesIntroduction", "A leitura do trecho permite identificar que o desabafo de "
                  "Riobaldo se aproxima de um(a):", "No romance _Grande sertão: veredas_, o protagonista "
                  "Riobaldo narra sua trajetória de jagunço. A leitura do trecho permite identificar que o "
                  "desabafo de Riobaldo se aproxima de um(a)")],
    "2015-74": [("A", "A sequência", "a sequência"),
                ("B", "A proteína", "a proteína"),
                ("D", "O RNA", "o RNA")],
    "2016-102": [("context", "habitavam – é que", "habitavam — é que"),
                 ("context", "nativo,\xa0_mandinga_\xa0designava", "nativo, _manding_ designava")],
    "2016-125": [("context", "**Receita**  \nTome-se", "**Receita**\n\nTome-se")],
    "2016-135": [("context", "**L.J.C.**  \n– 5 tiros?  \n– É.  \n– Brincando de pegador?  \n– É. O PM pensou "
                  "que…  \n– Hoje?  \n– Cedinho.",
                  "**L.J.C.**\n— 5 tiros?\n— É.\n— Brincando de pegador?\n— É. O PM pensou que…\n— Hoje?\n"
                  "— Cedinho."),
                 ("D", "A eliminação", "a eliminação"),
                 ("E", "A interrupção", "a interrupção")],
    # Na API cada verso virou parágrafo; no caderno é um bloco só, sem título separado.
    "2017-15": [("context", "**Sítio Gerimum**\n\nEste é o meu lugar (…)\n\nMeu Gerimum é com g\n\nVocê pode ter "
                 "estranhado\n\nGerimum em abundância\n\nAqui era plantado\n\nE com a letra g\n\nMeu lugar foi "
                 "registrado.",
                 "Sítio Gerimum\nEste é o meu lugar [...]\nMeu Gerimum é com g\nVocê pode ter estranhado\nGerimum "
                 "em abundância\nAqui era plantado\nE com a letra g\nMeu lugar foi registrado."),
                ("context", "2013 (fragmento)", "2013 (fragmento)."),
                ("alternativesIntroduction", "“Gerimum”grafada", "“Gerimum” grafada")],
    "2017-27": [("context", "no Computador", "no computador"),
                ("context", "xarope.\n\nHortelã", "xarope. Hortelã"),
                ("context", "2014 (adaptado)", "2014 (adaptado).")],
    # Estrofes como no caderno: a API tinha uma quebra a mais e duas a menos.
    "2017-30": [("context", "**Contranarciso**  \nem mim", "**Contranarciso**\n\nem mim"),
                ("context", "e outro\n\nenfim", "e outro  \nenfim"),
                ("context", "centenas  \no outro", "centenas\n\no outro"),
                ("context", "e você  \nassim como", "e você\n\nassim como"),
                ("context", "Leminsky P.", "LEMINSKI, P.")],
    "2017-80": [("context", "Edusp. 2009.", "Edusp, 2009.")],
    "2017-111": [("context", "Disponível em:\xa0 http", "Disponível em: http")],
    "2018-36": [("context", "O rio que fazia uma volta atrás de nossa casa era a\xa0imagem de um vidro mole que fazia "
                 "uma volta atrás\xa0de casa.  \nPassou um homem e disse: Essa volta que o\xa0rio faz por trás de sua "
                 "casa se chama enseada.\xa0Não era mais a imagem de uma cobra de vidro que\xa0fazia uma volta atrás "
                 "de casa.",
                 "O rio que fazia uma volta atrás de nossa casa era a\nimagem de um vidro mole que fazia uma volta "
                 "atrás\nde casa.\nPassou um homem e disse: Essa volta que o\nrio faz por trás de sua casa se chama "
                 "enseada.\nNão era mais a imagem de uma cobra de vidro que\nfazia uma volta atrás de casa.")],
    "2018-96": [("context", "natureza apoiar", "natureza apolar")],
    # Texto-base, fonte e comando vêm todos no enunciado: as quebras duplas separam os três.
    "2019-35": [("alternativesIntroduction", "corpo”.\nSILVA", "corpo”.\n\nSILVA"),
                ("alternativesIntroduction", "acerca da\ngestação", "acerca da gestação"),
                ("alternativesIntroduction", "Autores\nAssociados", "Autores Associados"),
                ("alternativesIntroduction", "UFSC, 2001\n\n\n\nA", "UFSC, 2001.\n\nA")],
    "2019-24": [("context", "Rio de Janeiro:  \nUniversal Music", "Rio de Janeiro: Universal Music")],
    "2019-25": [("context", "Carnaval 2012.  \nA penúltima", "Carnaval 2012.\n\nA penúltima"),
                ("context", "rj.gov.br.  \nAcesso", "rj.gov.br. Acesso")],
    "2019-80": [("alternativesIntroduction", "questionava:", "questionava"),
                ("A", "A alta", "a alta"),
                ("B", "A política", "a política"),
                ("D", "O arbítrio", "o arbítrio")],
    "2019-81": [("context", "século XVII,", "século XVIII,")],
    "2019-88": [("alternativesIntroduction", "estímulo à:", "estímulo à")],
    "2019-97": [("context", "enterradas por outra,", "enterradas por outras,")],
    "2019-156": [("context", "vôlei de praia.\n\nEles", "vôlei de praia. Eles")],
    "2019-168": [("context", "R$ 1000,00 por semana.  \nOs outros", "R$ 1 000,00 por semana.\n\nOs outros"),
                 ("context", "Cada um trabalha", "Cada um deles trabalha"),
                 ("B", "80X + 1000", "80X + 1 000"),
                 ("C", "80X + 1080", "80X + 1 080"),
                 ("E", "160X + 1000", "160X + 1 000")],
    "2020-39": [("context", "**BILAC, O**.: **BRAGA, F**.", "BILAC, O.; BRAGA, F.")],
    "2020-78": [("context", "“O toyotismo", "O toyotismo"),
                ("context", "acumulação.”\n\n(ANTUNES", "acumulação.\n\nANTUNES"),
                ("context", "Boitempo. 2009. Adaptado.)", "Boitempo, 2009 (adaptado).")],
    "2020-115": [("context", "(OLIVEIRA, M.", "OLIVEIRA, M."),
                 ("context", "abr. 2013.)", "abr. 2013.")],
    "2020-138": [("context", "a caixa d’água", "a caixa-d’água"),
                 ("context", "2 cm x 3,51 cm x 4 cm", "2 cm × 3,51 cm × 4 cm"),
                 ("context", "Dado: 1 dm3 = 1 L", "Dado: 1 dm³ = 1 L.")],
    "2021-82": [("context", "PUC-RIO", "PUC-Rio")],
    "2021-127": [("context", "Jardineiro da pesada", "Jardineiros da pesada"),
                 ("D", "quantidade de semente por", "quantidade de sementes por")],
    "2021-162": [("context", "imposto devido (l)", "imposto devido (I)")],
    "2021-165": [("context", "a cada 10 km", "a cada 20 km")],
    "2021-170": [("alternativesIntroduction", "em milimitro", "em mililitro"),
                 ("D", "2196", "2 196"),
                 ("E", "2928", "2 928")],
    "2021-173": [("alternativesIntroduction", "o lava rápido", "o lava-rápido"),
                 ("context", "R$35,00", "R$ 35,00"),
                 ("context", "R$300,00", "R$ 300,00")],
    "2022-26": [("context", "\n\nAssentamento  \nZanza daqui", "\n\nZanza daqui"),
                ("context", "Vamos embora  \nVer o capim", "Vamos embora\n\nVer o capim"),
                ("context", "Vamos embora  \nQuando eu morrer", "Vamos embora\n\nQuando eu morrer"),
                ("D", "\"Onde", "“Onde")],
    "2022-83": [("context", "Nascidas o Líbano", "Nascidas no Líbano"),
                ("context", "agenciabrasi.ebc", "agenciabrasil.ebc")],
    "2022-88": [("context", "**Sêneca, L.", "**SÊNECA, L.")],
    "2022-138": [("context", "no  \ndecorrer", "no decorrer")],
    "2022-178": [("context", "diâmetro.  \nA cozinheira", "diâmetro. A cozinheira"),
                 ("context", "dessa encomenda", "dessa encomenda.")],
    # Texto-base, fonte e comando vêm todos no enunciado: as quebras duplas separam os três.
    "2023-28": [("alternativesIntroduction", "Calo.\nESTRELA", "Calo.\n\nESTRELA"),
                ("alternativesIntroduction", "(fragmento).\nA função", "(fragmento).\n\nA função")],
    "2023-137": [("A", "2.000,00", "2 000,00"),
                 ("B", "2.500,00", "2 500,00"),
                 ("C", "3.250,00", "3 250,00"),
                 ("D", "4.500,00", "4 500,00"),
                 ("E", "9.000,00", "9 000,00")],
    "2023-155": [("A", "1.000", "1 000")],
    "2023-162": [("A", "Y = 250x", "y = 250x"),
                 ("B", "Y = 500x", "y = 500x"),
                 ("C", "Y = 750x", "y = 750x"),
                 ("D", "Y = 250x + 500", "y = 250x + 500"),
                 ("E", "Y = 500x + 250", "y = 500x + 250")],
}

# Alternativas que têm ponto final no caderno e vieram sem ele da API.
SEM_PONTO = {
    "2013-52": "E",
    "2017-111": "DE",
    "2021-82": "ABCDE",
    "2021-165": "ABCDE",
    "2022-88": "ABCDE",
    "2022-161": "ABCDE",
    "2023-28": "B",
    "2023-92": "D",
}

# Pontuação que fecha o comando. Sem ela, o comando continua nas alternativas.
FIM_DE_FRASE = (".", "?", "!", ":")


def texto_puro(md):
    """Markdown da API -> texto puro, mantendo as quebras de linha."""
    if not md:
        return ""
    t = md.replace("\r\n", "\n")
    t = re.sub(r"\[([^\]]*)\]\([^)]*\)", r"\1", t)          # [texto](url) -> texto
    t = t.replace("**", "").replace("__", "")
    t = re.sub(r"(?<![\w/])_([^_\n]+)_(?![\w/])", r"\1", t)  # _itálico_
    t = re.sub(r"^#+\s*", "", t, flags=re.M)                 # títulos
    t = re.sub(r"^\s*[*\-]\s+", "• ", t, flags=re.M)         # listas
    t = re.sub(r"\\([\\`*_{}\[\]()#+\-.!=])", r"\1", t)      # escapes (1\. -> 1.)
    t = "\n".join(linha.rstrip() for linha in t.split("\n"))
    t = re.sub(r"\n{3,}", "\n\n", t)
    return t.strip()


def carregar_bruto():
    bruto = {}
    for arq in sorted(RAW.glob("20*.json")):
        for q in json.loads(arq.read_text(encoding="utf-8")):
            bruto.setdefault(f"{q['year']}-{q['index']}", q)
    return bruto


def corrigido(q, chave, campo):
    """Campo do bruto (ou alternativa, pela letra) com as CORRECOES de OCR aplicadas."""
    if campo in ("A", "B", "C", "D", "E"):
        valor = q["alternatives"]["ABCDE".index(campo)].get("text") or ""
    else:
        valor = q.get(campo) or ""
    for c, antigo, novo in CORRECOES.get(chave, []):
        if c == campo:
            if valor.count(antigo) != 1:
                raise ValueError(f"{chave}: correção {antigo!r} aparece {valor.count(antigo)} vez(es)")
            valor = valor.replace(antigo, novo)
    return valor


def ajustar_inicial(alternativa, enunciado):
    """Minúscula no começo da alternativa que continua a frase do comando, como no caderno.

    Só muda palavra com maiúscula seguida de minúscula: sigla e "Y = 80X + 920" ficam como
    estão. Uma alternativa que comece com nome próprio precisaria de exceção aqui.
    """
    if enunciado.rstrip().endswith(FIM_DE_FRASE) or len(alternativa) < 2:
        return alternativa
    if alternativa[0].isupper() and alternativa[1].islower():
        return alternativa[0].lower() + alternativa[1:]
    return alternativa


def converter(md, nome_base, recursos):
    """texto_puro() que troca cada ![](url) por [[imagem:{nome_base}N.ext]]."""
    urls = []

    def guardar(m):
        urls.append(m.group(1))
        return f"\x00{len(urls) - 1}\x00"

    t = texto_puro(IMAGEM_MD.sub(guardar, md))
    for i, url in enumerate(urls):
        arquivo = f"{nome_base}{i + 1}{Path(url).suffix.lower()}"
        recursos.append(("imagem", arquivo, url))
        t = t.replace(f"\x00{i}\x00", f"[[imagem:{arquivo}]]")
    return t


def campos(q, chave):
    """Texto, enunciado e alternativas como o app recebe, mais as imagens/fórmulas a gerar.

    Recursos: ("imagem", arquivo, url) para baixar ou ("formula", arquivo, latex) para renderizar.
    """
    recursos = []
    texto = converter(corrigido(q, chave, "context"), f"{chave}-t", recursos)
    enunciado = converter(corrigido(q, chave, "alternativesIntroduction"), f"{chave}-e", recursos)
    if not texto and "\n" in enunciado:
        # Texto-base, fonte e comando vieram juntos no enunciado: o comando é o último parágrafo.
        # Com quebra dupla (poema, ver CORRECOES), só ela separa parágrafos e os versos ficam juntos.
        separador = r"\n{2,}" if "\n\n" in enunciado else r"\n+"
        *paragrafos, enunciado = re.split(separador, enunciado)
        texto = "\n\n".join(paragrafos)
    formulas = FORMULAS.get(chave)
    alternativas = []
    for i, a in enumerate(q["alternatives"]):
        if not a.get("file"):
            if formulas:
                raise ValueError(f"{chave}: FORMULAS para alternativa que já é texto")
            alternativa = ajustar_inicial(texto_puro(corrigido(q, chave, a["letter"])), enunciado)
            if a["letter"] in SEM_PONTO.get(chave, "") and not alternativa.endswith("."):
                alternativa += "."
            alternativas.append(alternativa)
        elif formulas:
            arquivo = f"{chave}-{a['letter'].lower()}.png"
            recursos.append(("formula", arquivo, formulas[i]))
            alternativas.append(f"[[formula:{arquivo}]]")
        else:
            arquivo = f"{chave}-{a['letter'].lower()}{Path(a['file']).suffix.lower()}"
            recursos.append(("imagem", arquivo, a["file"]))
            alternativas.append(f"[[imagem:{arquivo}]]")
    return {"texto": texto, "enunciado": enunciado, "alternativas": alternativas}, recursos


def contar_palavras(dados):
    """Palavras do texto-base, do enunciado e das alternativas (figuras e fórmulas não contam)."""
    texto = "\n".join([dados["texto"], dados["enunciado"], *dados["alternativas"]])
    return len(PALAVRA.findall(MARCADOR.sub(" ", texto)))


def estimar_segundos(area_id, dados):
    """Leitura a PALAVRAS_POR_MINUTO + resolução da área (+ EXTRA_FIGURA_S se tiver figura/fórmula)."""
    texto = "\n".join([dados["texto"], dados["enunciado"], *dados["alternativas"]])
    extra = EXTRA_FIGURA_S if MARCADOR.search(texto) else 0
    return round(contar_palavras(dados) * 60 / PALAVRAS_POR_MINUTO + RESOLUCAO_S[area_id] + extra)


def ids_atuais():
    """Questão do ENEM ("2019-24") -> id atual no banco ("LIN-01-01")."""
    return {chave: f"{sigla}-{li:02d}-{qi:02d}"
            for _, sigla, licoes in AREAS
            for li, (_, questoes) in enumerate(licoes, start=1)
            for qi, (chave, _, _) in enumerate(questoes, start=1)}


def ids_antigos():
    """Versão antiga -> {id da questão naquela versão -> id atual, ou "" se ela saiu do banco}.

    O app conta o tamanho de cada lição antiga por esse mapa, então ele leva todas as questões.
    """
    atual = ids_atuais()
    return {versao: {f"{sigla}-{li:02d}-{qi:02d}": atual.get(chave, "")
                     for sigla, licoes in agrupamento.items()
                     for li, chaves in enumerate(licoes, start=1)
                     for qi, chave in enumerate(chaves, start=1)}
            for versao, agrupamento in VERSOES_ANTIGAS.items()}


def copiar_figura(origem, destino):
    """Figura recortada da prova oficial no lugar da que a API entrega."""
    if destino.exists() and destino.read_bytes() == origem.read_bytes():
        return
    shutil.copyfile(origem, destino)
    print(f"  copiada {destino.name} (prova oficial)")


def baixar_imagem(url, destino):
    if destino.exists():
        return
    req = urllib.request.Request(url, headers={"User-Agent": "ENEMigo-escolar"})
    with urllib.request.urlopen(req, timeout=30) as r:
        destino.write_bytes(r.read())
    print(f"  baixada {destino.name}")


def renderizar_formula(latex, destino):
    """LaTeX -> PNG transparente na cor do texto do app, com 2 px de folga nas bordas."""
    import numpy as np  # só quem regenera as fórmulas precisa do matplotlib
    from matplotlib import mathtext
    from matplotlib.font_manager import FontProperties
    from PIL import Image

    fonte = FontProperties(size=FORMULA_PT, math_fontfamily=FORMULA_FONTE)
    raster = mathtext.MathTextParser("agg").parse(f"${latex}$", dpi=FORMULA_DPI, prop=fonte)
    alfa = np.pad(np.asarray(raster.image), 2)
    rgba = np.zeros((*alfa.shape, 4), np.uint8)
    rgba[..., :3] = COR_TEXTO
    rgba[..., 3] = alfa
    Image.fromarray(rgba).save(destino, optimize=True)


def main():
    sys.stdout.reconfigure(encoding="utf-8")  # o console do Windows não é UTF-8
    bruto = carregar_bruto()
    divergentes, areas, recursos = [], [], []
    for area_id, sigla, licoes in AREAS:
        area = {"id": area_id, "licoes": []}
        for li, (titulo, questoes) in enumerate(licoes, start=1):
            licao_id = f"{sigla}-{li:02d}"
            licao = {"id": licao_id, "titulo": titulo, "questoes": []}
            for qi, (chave, cega, explicacao) in enumerate(questoes, start=1):
                q = bruto[chave]
                inep = GABARITO_INEP.get(chave)
                if not cega == inep == q["correctAlternative"]:
                    divergentes.append(f"{chave}: cega {cega}, INEP {inep}, API {q['correctAlternative']}")
                if chave in ANULADAS:
                    divergentes.append(f"{chave}: anulada ou abandonada pela TRI")
                dados, rec = campos(q, chave)
                if contar_palavras(dados) > LIMITE_PALAVRAS[area_id]:
                    divergentes.append(f"{chave}: {contar_palavras(dados)} palavras "
                                       f"(máximo {LIMITE_PALAVRAS[area_id]})")
                recursos += rec
                licao["questoes"].append({
                    "id": f"{licao_id}-{qi:02d}",
                    **dados,
                    "correta": q["correctAlternative"],
                    "explicacao": explicacao,
                    "fonte": f"ENEM {q['year']} – Questão {q['index']}",
                    "segundos_estimados": estimar_segundos(area_id, dados),
                })
            area["licoes"].append(licao)
        areas.append(area)

    if divergentes:
        print("PROBLEMAS no gabarito, nas anuladas ou no tamanho (nada foi gravado):")
        print("\n".join(divergentes))
        sys.exit(1)
    IMAGENS.mkdir(exist_ok=True)
    for tipo, arquivo, origem in recursos:
        if tipo == "imagem" and (FIGURAS / arquivo).exists():
            copiar_figura(FIGURAS / arquivo, IMAGENS / arquivo)
        elif tipo == "imagem":
            baixar_imagem(origem, IMAGENS / arquivo)
        else:
            renderizar_formula(origem, IMAGENS / arquivo)
    banco = {"versao": VERSAO, "areas": areas, "ids_antigos": ids_antigos()}
    SAIDA.write_text(json.dumps(banco, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"OK: {SAIDA} ({len(recursos)} imagens/fórmulas em {IMAGENS.name}/)")


if __name__ == "__main__":
    main()
