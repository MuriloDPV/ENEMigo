"""Monta app/src/main/assets/questoes.json com questões reais do ENEM (API enem.dev).

Uso: python tools/montar_questoes.py
Lê o bruto de tools/enem_raw/{ano}.json (gerado por baixar_enem.py).
A letra "cega" de cada questão é a resposta dada antes de consultar o gabarito;
o script avisa quando ela diverge do gabarito oficial e não grava nada nesse caso.

Imagens e fórmulas (Matemática):
- As figuras da API são baixadas para app/src/main/assets/imagens/ e o texto ganha
  um marcador [[imagem:arquivo.png]] no lugar delas.
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
"""
import json
import re
import sys
import urllib.request
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
RAW = Path(__file__).parent / "enem_raw"
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

# área -> [(título da lição, [(ano-index, letra_cega, explicação), ...]), ...]
# Cada lição tem de 3 a 5 questões e mira ALVO_LICAO_S pela estimativa de tempo. A ordem
# segue a das lições da versão 1 (LICOES_V1), para a migração do progresso salvo não deixar
# buraco na trilha: o validar_questoes.py confere isso.
AREAS = [
    ("LINGUAGENS", "LIN", [
        ("Tecnologia e comunicação", [
            ("2019-29", "C", "Para Lévy, a rede é um laço entre pessoas em que as comunidades ajudam seus membros a aprender. O saber só circula porque os próprios usuários contribuem com esforço, pesquisa e participação."),
            ("2022-11", "A", "Os autores dizem que toda nova técnica exige “uma longa iniciação numa nova linguagem”. O maior impacto é a dificuldade de se apropriar dela, e não uma aprendizagem imediata."),
            ("2023-9", "E", "O texto cita cavalos, ordens mendicantes, jograis, peregrinos, vagabundos e correios organizados. A eficácia vinha da diversidade de meios disponíveis naquela época."),
        ]),
        ("Usos da língua", [
            ("2020-41", "A", "Policarpo escreve um requerimento formal ao Congresso. A norma-padrão é exigida por essa situação de comunicação (documento oficial), mesmo sendo o pedido tão pitoresco."),
            ("2022-14", "D", "O Texto I diz que o léxico guarda as experiências de um povo. O Texto II mostra expressões como “ovelha negra”, que carregam o preconceito racial presente na história da sociedade."),
            ("2023-21", "D", "O dicionário traduz termos populares (defruço, chanha, piloura) para termos médicos. Assim o médico entende melhor os sintomas que o paciente descreve."),
        ]),
        ("Recursos de linguagem", [
            ("2019-24", "C", "A ladainha é uma oração feita de pedidos repetidos, e a canção imita essa forma. O convite “Vamos pedir piedade” e o pedido “Lhes dê grandeza” chamam o interlocutor a participar: é a sequência injuntiva."),
            ("2022-6", "E", "Metáforas do dia a dia, como “a vida engata uma primeira e sai em disparada”, aproximam o texto de uma conversa e criam o tom descontraído e pouco formal."),
            ("2022-33", "A", "O trecho é uma longa lista de objetos, pessoas e ações do funeral, separados por vírgulas. É essa enumeração que faz o próprio narrador dizer que aquilo “parece um simples inventário”."),
        ]),
        ("Leitura literária", [
            ("2019-13", "B", "Pinote foi rejeitado enquanto era futurista e só voltou a ser aceito quando virou “passadista”. A peça ironiza um público conservador, preso aos modelos consagrados."),
            ("2020-11", "B", "A paisagem ganha traços ameaçadores (sombras, monstros, serpentes, árvores que choram) que espelham o medo de Maria. O estado emocional da personagem e a paisagem se confundem."),
            ("2022-31", "A", "O Romance de 30 retrata com crueza a realidade social do Nordeste. Aqui os retirantes da seca aparecem como “esqueletos redivivos”, sem nenhuma idealização."),
            ("2023-31", "C", "O fim do amor pediria um cenário solene, como os da tradição lírica. Mas acontece numa cena banal, à mesa do café, enquanto o outro recolhe as migalhas de pão: a comoção do amor frustrado se dilui no cotidiano."),
        ]),
        ("Esporte e práticas corporais", [
            ("2020-36", "D", "Pela definição, luta tem contato proposital, troca de ações de ataque e defesa e regras. A proposta deve ser um combate intencional com ações regulamentadas, sem agressão e sem vencedor definido antes."),
            ("2021-31", "A", "As rodas de uretano deram mais aderência, trouxeram novas manobras e permitiram que até pessoas inexperientes começassem a praticar. O skate chegou a muito mais gente, e daí vieram campeonatos, marcas e lojas: é a democratização da prática."),
            ("2023-6", "D", "A palavra “juntos” entrou no lema “para ressaltar a solidariedade”. O lema olímpico passou a se associar à cooperação."),
            ("2023-42", "E", "Segundo o texto, as práticas de lazer se concentram “nas populações mais abastadas”, e os mais pobres se exercitam sobretudo no deslocamento para estudar ou trabalhar. Gênero, idade e raça também aparecem, mas o eixo do texto é a “relação inversa e perversa” com as condições socioeconômicas: a desigualdade entre classes."),
        ]),
        ("Internet e igualdade", [
            ("2020-43", "D", "Os exemplos (software que eliminou candidatos por sexo e origem, anúncios de antecedentes criminais para nomes comuns entre negros) mostram a tecnologia reproduzindo o preconceito que já existe na sociedade."),
            ("2023-39", "C", "A definição mudou por causa de uma petição on-line criada por um cidadão e apoiada por outros. Foi o uso público da petição, a mobilização, que impulsionou a mudança."),
            ("2020-35", "A", "O texto mostra que colocar seleções femininas no videogame ajuda a divulgar o futebol feminino, “um grande passo” na luta pela igualdade de gênero."),
        ]),
    ]),
    ("HUMANAS", "HUM", [
        ("Política e poder", [
            ("2019-65", "A", "Maquiavel separa a moral ideal (como se deveria agir) da moral efetiva (o que o governante precisa fazer). Pelo bem comum, o chefe de Estado pode até mentir."),
            ("2021-73", "A", "O suicídio de Vargas provocou enorme comoção e protestos populares contra seus adversários. Por isso o texto diz que ao tiro “não se seguiu o silêncio que cerca a derrota”."),
            ("2023-80", "A", "O regime militar reprimiu as Ligas Camponesas e criou sindicatos rurais de orientação conservadora. O objetivo era controlar as tensões políticas no campo."),
        ]),
        ("Filosofia e conhecimento", [
            ("2022-86", "C", "Os pré-socráticos buscavam a arché, o princípio que explica a origem e a ordem do cosmos. Empédocles faz isso com os quatro elementos movidos pelo Amor e pelo Ódio."),
            ("2021-82", "B", "Sócrates conduz a conversa com perguntas para chegar à definição do que é a virtude. Esse jogo de perguntas e respostas é o método dialético."),
            ("2020-89", "D", "Hume é empirista: a razão sozinha não conclui nada sobre os fatos, só a experiência mostra causas e efeitos. O conhecimento vem da vivência dos fenômenos."),
            ("2023-54", "A", "Descartes separa a mente (pensamento) do corpo (matéria): é o dualismo. Merleau-Ponty se opõe a isso ao perceber a raiva dentro do próprio corpo."),
        ]),
        ("Escravidão e racismo", [
            ("2020-81", "C", "O jesuíta defende que a Companhia “deve ter e adquirir escravos” para os serviços dos colégios. O texto justifica o uso do trabalho escravo na Colônia."),
            ("2019-81", "B", "No fim do século XVII começou a mineração em Minas Gerais. O Rio de Janeiro virou o principal porto de chegada dos escravizados levados para as minas de ouro."),
            ("2021-52", "D", "A Lei Aberdeen (1845) permitia à Inglaterra apreender navios negreiros. Os britânicos resistiam a revogá-la porque queriam manter proibido o tráfico de escravos para o Brasil."),
            ("2022-48", "A", "O relato mostra um guarda branco espancando e amarrando um homem negro, como no tempo da escravidão. É o racismo enraizado nas estruturas da sociedade."),
        ]),
        ("Cidadania e direitos", [
            ("2019-62", "E", "Num Estado laico e democrático, cada pessoa pode seguir a religião que quiser. Agredir adeptos das religiões de matriz africana restringe essa liberdade de credo."),
            ("2021-66", "B", "O perfil dos presos (jovens, negros, com pouca escolaridade) revela exclusão social. Políticas de inclusão, como educação e emprego, atacam a raiz do problema."),
            ("2022-83", "B", "Apátrida é a pessoa sem nacionalidade reconhecida por nenhum país. As irmãs não puderam ser registradas nem no Líbano, onde nasceram, nem na Síria, país dos pais."),
            ("2023-60", "D", "Controlar o dinheiro, esconder documentos ou destruir pertences da mulher são formas de violência patrimonial, uma das previstas na Lei Maria da Penha."),
        ]),
        ("Geografia física", [
            ("2019-89", "D", "Utqiagvik fica muito ao norte, acima do Círculo Polar Ártico. Nessa latitude, no inverno, o Sol passa semanas sem aparecer (é a noite polar)."),
            ("2020-82", "A", "As cinzas e lavas dos vulcões deixam o solo rico em minerais e muito fértil. Por isso as pessoas continuam morando perto deles, mesmo com o risco."),
            ("2020-84", "E", "Quando a placa oceânica mergulha sob a continental, as rochas são comprimidas e dobradas, formando cordilheiras jovens: os dobramentos modernos, como os Andes."),
            ("2021-71", "A", "Rochas são desgastadas, os fragmentos são transportados e depositados nas partes baixas, formando novas rochas. Esse caminho é o ciclo sedimentar."),
            ("2023-53", "B", "O Himalaia se formou, e ainda cresce, pelo choque entre as placas Indiana e Euroasiática. É o encontro de placas tectônicas."),
        ]),
    ]),
    ("NATUREZA", "NAT", [
        ("Ecologia", [
            ("2019-97", "D", "Cutias da mesma espécie disputam o mesmo recurso, as sementes enterradas. Uma rouba a reserva da outra: é uma relação de competição."),
            ("2020-95", "C", "Corredores ecológicos ligam os fragmentos de mata. Os animais voltam a circular e as populações se reencontram, o que evita a extinção nas “ilhas”."),
            ("2021-127", "C", "As preguiças-gigantes comiam os frutos e espalhavam as sementes. Com a extinção delas, a dispersão das sementes diminuiu e a flora ficou mais restrita."),
            ("2022-99", "A", "A jaqueira, espécie exótica, se espalha e ocupa o lugar da vegetação nativa. Ao substituir parte da flora original, reduz a biodiversidade."),
            ("2023-91", "D", "As bombas de sementes fazem brotar plantas nativas com flores. Isso aumenta a oferta de néctar e pólen, que são o alimento das abelhas."),
        ]),
        ("Hereditariedade", [
            ("2019-110", "D", "Genes próximos no mesmo cromossomo tendem a ser herdados juntos (ligação gênica). Isso contraria a segregação independente proposta por Mendel."),
            ("2021-132", "A", "Pais sem a doença que são portadores têm 1 chance em 4 de ter filho afetado (Aa × Aa → 1/4 aa). Esse é o padrão da herança recessiva."),
            ("2023-125", "E", "Cada pessoa com trissomia forma gametas com um ou dois cromossomos 21. Juntando: 1+1 dá zigoto normal, 1+2 trissômico e 2+2 tetrassômico."),
        ]),
        ("Física no cotidiano", [
            ("2020-111", "B", "O fone capta o ruído e emite uma onda sonora oposta a ele. As duas ondas se somam e se anulam: é a interferência destrutiva."),
            ("2020-133", "C", "A carcaça metálica do carro funciona como uma gaiola de Faraday. As cargas ficam na superfície externa e o campo elétrico lá dentro é nulo."),
            ("2023-108", "D", "A bobina do fogão gera um campo magnético variável, que induz corrente elétrica no fundo metálico da panela (indução eletromagnética)."),
        ]),
        ("Biotecnologia", [
            ("2020-115", "B", "A soja recebeu um gene de outra espécie para produzir a cianovirina-N. Um organismo que recebe gene de outra espécie é transgênico."),
            ("2022-98", "E", "O vírus leva uma cópia normal do gene RPE65 às células da retina. Elas passam a expressar esse gene e a produzir a enzima normal."),
            ("2023-106", "C", "O RNAm entra nas células e é traduzido pelos ribossomos em uma proteína do vírus. O sistema imune reconhece essa proteína como antígeno e cria defesas."),
        ]),
        ("Química no cotidiano", [
            ("2021-97", "E", "Óleo (um éster, triglicerídeo) reage com soda cáustica (base forte) e forma sabão e glicerol. Essa reação é a saponificação."),
            ("2022-126", "B", "Depois da coagulação, a sujeira forma flocos pesados que descem para o fundo dos tanques. É na decantação que sai a maior parte dos sólidos."),
            ("2020-108", "C", "O revestimento só libera o remédio em pH perto do neutro. Assim ele atravessa intacto o estômago ácido e só se abre no intestino, sem ser afetado pelas secreções gástricas."),
        ]),
        ("Corpo humano e saúde", [
            ("2019-133", "A", "O rim filtra o sangue e elimina a ureia, resíduo tóxico do metabolismo. É isso que a hemodiálise faz hoje e que o rim biônico fará."),
            ("2021-114", "B", "O reflexo patelar é um arco reflexo: o estímulo vai até a medula espinal, que manda a resposta direto ao músculo, sem passar pelo cérebro."),
            ("2022-104", "E", "A eritropoietina estimula a produção de hemácias. Com mais hemácias, o sangue leva mais oxigênio aos músculos."),
        ]),
    ]),
    ("MATEMATICA", "MAT", [
        ("Porcentagem e proporção", [
            ("2019-138", "E", "O transporte custa um quinto do valor do objeto, ou seja, 20%. Para o gasto total não passar de 100% do valor, sobram no máximo 80% para a recompensa."),
            ("2022-164", "B", "Em 60 dias há 60 × 1 440 = 86 400 minutos, o que dá 86 400 ÷ 48 = 1 800 acionamentos. Cada um libera 360 ÷ 1 800 = 0,2 mL."),
            ("2023-161", "D", "Cada parcela é 1 800 ÷ 3 = 600, e a primeira é paga na hora. Sobram 900 financiados: 900 = 600/x + 600/x², com x = 1 + i. Daí 3x² − 2x − 2 = 0 e x = (2 + √28)/6 ≈ 1,215, ou seja, 21,5% ao mês."),
        ]),
        ("Áreas e escala", [
            ("2022-161", "A", "No desenho, a varanda tem 16 × 5 + 13,4 × 4 = 133,6 cm². Na escala 1 : 50, as áreas ficam 50² = 2 500 vezes maiores: 334 000 cm² = 33,4 m²."),
            ("2023-155", "D", "A piscina tem lado √400 = 20 m. Com 5 m de calçada de cada lado, o quadrado todo tem 30 m de lado e 900 m². A calçada ocupa 900 − 400 = 500 m²."),
            ("2019-147", "B", "Cada placa é meio círculo de raio 20 cm sobre um quadrado de 40 cm de lado: 3,14 × 20² ÷ 2 + 40² = 628 + 1 600 = 2 228 cm². As dez placas somam 22 280 cm²."),
        ]),
        ("Volumes e superfícies", [
            ("2020-161", "B", "Volumes iguais: πR²h = πr²(h/4). Então r² = 4R² e r = 2R. Altura quatro vezes menor pede um raio duas vezes maior."),
            ("2021-170", "C", "A caneca é um tronco de cone: V = πh(R² + Rr + r²)/3 = 3 × 12 × (25 + 20 + 16)/3 = 12 × 61 = 732 cm³, ou seja, 732 mL."),
            ("2023-165", "E", "Pisos: 3 × 1,0 × 0,25 = 0,75 m². Espelhos: 3 × 1,0 × 0,20 = 0,60 m². Cada parede lateral tem 0,25 × (0,60 + 0,40 + 0,20) = 0,30 m², e são duas. Total: 0,75 + 0,60 + 0,60 = 1,95 m²."),
        ]),
        ("Frações e probabilidade", [
            ("2020-170", "C", "São 4 + 6 + 6 = 16 partes, e cada sócio deve ficar com 16/3. Antônio precisa de 16/3 − 4 = 4/3 de parte, 2/3 vindos de cada sócio. Como cada um tem 6 partes, isso é (2/3) ÷ 6 = 1/9 do capital dele."),
            ("2023-168", "A", "Para o 50: a unidade 0 sai com chance 1/10 e restam as dezenas 1 a 5 (5 bolas), então 1/10 × 1/5 = 1/50. Para o 02: a unidade 2 sai com 1/10 e restam as dezenas 0 a 5 (6 bolas), então 1/10 × 1/6 = 1/60."),
            ("2022-141", "E", "Restam 46 números. Na próxima rodada Pedro só ganha se sair o 12 (1/46). Na outra, ganha se sair o 12 depois de outro número (45 casos), ou 05 e 45, ou 11 e 19, em qualquer ordem (2 + 2 casos): 49/(46 × 45)."),
        ]),
        ("Tabelas e gráficos", [
            ("2023-137", "C", "Média ponderada pelo número de funcionários: (75 × 2 000 + 25 × 7 000) ÷ 100 = 325 000 ÷ 100 = 3 250 reais."),
            ("2022-149", "B", "São 1 000 famílias, então a mediana é a média entre a 500ª e a 501ª. Acumulando, até 1 criança há exatamente 500 famílias, e a 501ª já tem 2. Mediana: (1 + 2) ÷ 2 = 1,5."),
            ("2022-138", "D", "Só entram os meses com lucro: jan. 3, mar. 1, maio 5, jun. 3, jul. 2, set. 4, nov. 1 e dez. 4. Em ordem: 1, 1, 2, 3, 3, 4, 4, 5. A mediana é (3 + 3) ÷ 2 = 3."),
            ("2021-162", "A", "Até 5 000 o imposto é zero. Em 10 000 ele vale 10% de 5 000 = 500 e, em 15 000, 500 + 30% de 5 000 = 2 000. O segundo trecho de subida é bem mais íngreme."),
        ]),
        ("Funções e inequações", [
            ("2019-168", "D", "Além do gerente, há X − 1 diaristas, e cada um recebe 2 × 80 = 160 reais por semana. Y = 1 000 + 160(X − 1) = 160X + 840."),
            ("2021-173", "C", "O menor número de lavagens sai quando todas são completas: 300 ÷ 35 ≈ 8,6. Oito lavagens dão só 280 reais, então são necessárias 9."),
            ("2019-139", "A", "Até 6 m³ o valor é fixo (12 reais): trecho horizontal. De 6 a 10 m³ sobe 3 reais por m³ e, de 10 a 15 m³, 6 reais por m³, então o último trecho é o mais inclinado."),
            ("2019-165", "E", "O volume de sangue é 8% da massa, ou seja, 0,08m litros. A concentração é q ÷ 0,08m, e ela prejudica a saúde quando passa de 0,4."),
        ]),
    ]),
]

# Agrupamento da versão 1 do banco (4 lições de 5 por área). Serve só para migrar o progresso
# salvo: o questoes.json leva em "ids_v1" o id antigo de cada questão -> id atual.
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

# Alternativas que na API são imagem de fórmula, redigitadas em LaTeX (mathtext do matplotlib).
# Conferidas uma a uma contra as imagens originais.
FORMULAS = {
    "2020-170": [r"\dfrac{1}{2}", r"\dfrac{1}{3}", r"\dfrac{1}{9}", r"\dfrac{2}{3}", r"\dfrac{4}{3}"],
    "2020-161": [r"\dfrac{R}{2}", r"2R", r"4R", r"5R", r"16R"],
    "2023-168": [rf"\dfrac{{1}}{{{a}}}\ \mathrm{{e}}\ \dfrac{{1}}{{{b}}}"
                 for a, b in [(50, 60), (50, 50), (50, 10), (55, 54), (100, 100)]],
    "2022-141": [r"\dfrac{1}{46}+\dfrac{1}{45}"] + [rf"\dfrac{{1}}{{46}}+\dfrac{{{n}}}{{46\times 45}}"
                                                   for n in (2, 8, 43, 49)],
    "2019-165": [r"\dfrac{q}{0{,}8m} > 0{,}4", r"\dfrac{0{,}4m}{q} > 0{,}8", r"\dfrac{q}{0{,}4m} > 0{,}8",
                 r"\dfrac{0{,}08m}{q} > 0{,}4", r"\dfrac{q}{0{,}08m} > 0{,}4"],
}

# Erros de OCR da API (símbolo perdido, unidade trocada, palavra cortada, parágrafo
# grudado). Cada trecho antigo precisa aparecer exatamente uma vez no campo. O campo é
# "context", "alternativesIntroduction" ou a letra da alternativa ("A" a "E").
CORRECOES = {
    "2019-24": [("context", "Rio de Janeiro:  \nUniversal Music", "Rio de Janeiro: Universal Music")],
    "2019-29": [("context", "cultura_  \n_informática", "cultura informática")],
    "2019-139": [("context", "consumo de até 6 m²", "consumo de até 6 m³"),
                 ("context", "15 m² por mês", "15 m³ por mês"),
                 ("context", "a 10 m³. Sabe-se", "a 10 m³.\n\nSabe-se")],
    "2019-147": [("context", "seu patio", "seu pátio"),
                 ("context", "conforme lustrado", "conforme ilustrado")],
    "2020-36": [("context", "regidas por regras", "regida por regras"),
                ("context", "situacionais.\n\n**Movimento**", "situacionais. **Movimento**")],
    "2020-41": [("context", "BARRETO. L.", "BARRETO, L."),
                ("context", "26 jun. 2012", "26 jun. 2012.")],
    "2020-43": [("context", "60 de 2.000", "60 de 2 000")],
    "2021-31": [("context", "ARMBRUST, I; LAURD, G, A, A.", "ARMBRUST, I.; LAURO, F. A. A."),
                ("context", "Motriz, jul-set, 2010", "Motriz, jul.-set. 2010")],
    "2021-162": [("context", "imposto devido (l)", "imposto devido (I)")],
    "2021-170": [("alternativesIntroduction", "em milimitro", "em mililitro")],
    "2021-173": [("alternativesIntroduction", "o lava rápido", "o lava-rápido")],
    "2022-6": [("context", "http:/porumavidasimples", "http://porumavidasimples")],
    "2022-11": [("context", "o\xa0 nascimento", "o nascimento")],
    "2022-14": [("context", "FIORIN, J. L Língua", "FIORIN, J. L. Língua"),
                ("context", "n. 2. mar-set 2014", "n. 2, mar.-set. 2014."),
                ("context", "brasil.elpais.com Acesso", "brasil.elpais.com. Acesso")],
    "2022-33": [("context", "caixão a prego", "caixão, a prego"),
                ("context", "corrêas", "correias"),
                ("context", "inventário, eram", "inventário eram"),
                ("context", "Memórias Póstumas", "Memórias póstumas"),
                ("context", "domíniopúblico", "dominiopublico"),
                ("context", "25 jul, 2022", "25 jul. 2022"),
                ("alternativesIntroduction", "permite o Machado de Assis considerar o capítulo de Memórias Póstumas",
                 "permite a Machado de Assis considerar um capítulo de Memórias póstumas"),
                ("alternativesIntroduction", "inventário é a:", "inventário é a")],
    "2022-138": [("context", "no  \ndecorrer", "no decorrer")],
    "2023-6": [("alternativesIntroduction", "“juntos” mais rápido", "“juntos”: mais rápido"),
               ("alternativesIntroduction", "forte – juntos", "forte — juntos"),
               ("alternativesIntroduction", "ge.globo.com Acesso em: 10 nov. 2021 (adaptado)",
                "ge.globo.com. Acesso em: 10 nov. 2021 (adaptado).")],
    "2023-9": [("context", "18 jun. 2018 (adaptado)", "18 jun. 2018 (adaptado).")],
    "2023-21": [("alternativesIntroduction", "gl.globo.com", "g1.globo.com"),
                ("alternativesIntroduction", "médica, Pesquisadores", "médica, pesquisadores")],
    # Na API cada verso virou parágrafo e alguns versos foram emendados; no caderno é um bloco só.
    "2023-31": [("context", "Entre a toalha branca e um bule de café\n\nseria inapropriado dizer\n\neu não te amo mais."
                 "\n\nEra necessário algo mais solene,\n\num jardim japonês para as perdas pensadas,\n\num noturno de "
                 "tempestade para arrebentar de dor,\n\numa praia de pedras para chorar em silêncio,\n\numa cama alta "
                 "para o incenso da despedida,\n\numa janela dando para o abismo.\n\nNo entanto você abaixa os olhos"
                 "\n\ne recolhe lentamente as migalhas de pão\n\nsobre a mesa posta para dois.",
                 "Entre a toalha branca e um bule de café\nseria inapropriado dizer\neu não te amo mais.\nEra "
                 "necessário algo mais solene,\num jardim japonês\npara as perdas pensadas,\num noturno de tempestade"
                 "\npara arrebentar de dor,\numa praia de pedras para chorar\nem silêncio, uma cama alta\npara o "
                 "incenso da despedida,\numa janela\ndando para o abismo.\nNo entanto você abaixa os olhos\ne recolhe "
                 "lentamente as migalhas de pão\nsobre a mesa posta para dois.")],
    "2023-42": [("context", "PC/A e residentes", "PC/AF e residentes"),
                ("alternativesIntroduction", "atividades fisicas", "atividades físicas")],
    "2023-161": [("context", "aproximação para 28.", "aproximação para √28.")],
}

# Alternativas que têm ponto final no caderno e vieram sem ele da API.
SEM_PONTO = {
    "2022-33": "ABCDE",
    "2023-39": "E",
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
        *paragrafos, enunciado = re.split(r"\n+", enunciado)
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


def estimar_segundos(area_id, dados):
    """Leitura a PALAVRAS_POR_MINUTO + resolução da área (+ EXTRA_FIGURA_S se tiver figura/fórmula)."""
    texto = "\n".join([dados["texto"], dados["enunciado"], *dados["alternativas"]])
    palavras = len(PALAVRA.findall(MARCADOR.sub(" ", texto)))
    extra = EXTRA_FIGURA_S if MARCADOR.search(texto) else 0
    return round(palavras * 60 / PALAVRAS_POR_MINUTO + RESOLUCAO_S[area_id] + extra)


def ids_v1():
    """Id da questão na versão 1 do banco ("LIN-01-03") -> id atual, para migrar o progresso."""
    atual = {}
    for _, sigla, licoes in AREAS:
        for li, (_, questoes) in enumerate(licoes, start=1):
            for qi, (chave, _, _) in enumerate(questoes, start=1):
                atual[chave] = f"{sigla}-{li:02d}-{qi:02d}"
    return {f"{sigla}-{li:02d}-{qi:02d}": atual[chave]
            for sigla, licoes in LICOES_V1.items()
            for li, chaves in enumerate(licoes, start=1)
            for qi, chave in enumerate(chaves, start=1)}


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
                if cega != q["correctAlternative"]:
                    divergentes.append(f"{chave}: cega {cega}, oficial {q['correctAlternative']}")
                dados, rec = campos(q, chave)
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
        print("DIVERGÊNCIAS com o gabarito oficial (nada foi gravado):")
        print("\n".join(divergentes))
        sys.exit(1)
    IMAGENS.mkdir(exist_ok=True)
    for tipo, arquivo, origem in recursos:
        if tipo == "imagem":
            baixar_imagem(origem, IMAGENS / arquivo)
        else:
            renderizar_formula(origem, IMAGENS / arquivo)
    banco = {"versao": 2, "areas": areas, "ids_v1": ids_v1()}
    SAIDA.write_text(json.dumps(banco, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"OK: {SAIDA} ({len(recursos)} imagens/fórmulas em {IMAGENS.name}/)")


if __name__ == "__main__":
    main()
