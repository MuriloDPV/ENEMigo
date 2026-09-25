"""Monta app/src/main/assets/questoes.json com questões reais do ENEM (API enem.dev).

Uso: python tools/montar_questoes.py
Lê o bruto de tools/enem_raw/{ano}.json (gerado por baixar_enem.py).
A letra "cega" de cada questão é a resposta dada antes de consultar o gabarito;
o script avisa quando ela diverge do gabarito oficial e não grava nada nesse caso.
"""
import json
import re
import sys
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
RAW = Path(__file__).parent / "enem_raw"
SAIDA = RAIZ / "app" / "src" / "main" / "assets" / "questoes.json"

# área -> [(sigla-lição, título, [(ano-index, letra_cega, explicação), ...]), ...]
AREAS = [
    ("LINGUAGENS", "LIN", [
        ("Tecnologia e comunicação", [
            ("2019-29", "C", "Para Lévy, a rede é um laço entre pessoas em que as comunidades ajudam seus membros a aprender. O saber só circula porque os próprios usuários contribuem com esforço, pesquisa e participação."),
            ("2020-43", "D", "Os exemplos (software que eliminou candidatos por sexo e origem, anúncios de antecedentes criminais para nomes comuns entre negros) mostram a tecnologia reproduzindo o preconceito que já existe na sociedade."),
            ("2022-11", "A", "Os autores dizem que toda nova técnica exige “uma longa iniciação numa nova linguagem”. O maior impacto é a dificuldade de se apropriar dela, e não uma aprendizagem imediata."),
            ("2023-9", "E", "O texto cita cavalos, ordens mendicantes, jograis, peregrinos, vagabundos e correios organizados. A eficácia vinha da diversidade de meios disponíveis naquela época."),
            ("2023-39", "C", "A definição mudou por causa de uma petição on-line criada por um cidadão e apoiada por outros. Foi o uso público da petição, a mobilização, que impulsionou a mudança."),
        ]),
        ("Usos da língua", [
            ("2019-24", "C", "A ladainha é uma sequência de súplicas repetidas (“Vamos pedir piedade / Senhor, piedade”). O convite chama o interlocutor a participar da prece: é a sequência injuntiva."),
            ("2020-41", "A", "Policarpo escreve um requerimento formal ao Congresso. A norma-padrão é exigida por essa situação de comunicação (documento oficial), mesmo sendo o pedido tão pitoresco."),
            ("2022-6", "E", "Metáforas do dia a dia, como “a vida engata uma primeira e sai em disparada”, aproximam o texto de uma conversa e criam o tom descontraído e pouco formal."),
            ("2022-14", "D", "O Texto I diz que o léxico guarda as experiências de um povo. O Texto II mostra expressões como “ovelha negra”, que carregam o preconceito racial presente na história da sociedade."),
            ("2023-21", "D", "O dicionário traduz termos populares (defruço, chanha, piloura) para termos médicos. Assim o médico entende melhor os sintomas que o paciente descreve."),
        ]),
        ("Leitura literária", [
            ("2019-13", "B", "Pinote foi rejeitado enquanto era futurista e só voltou a ser aceito quando virou “passadista”. A peça ironiza um público conservador, preso aos modelos consagrados."),
            ("2020-11", "B", "A paisagem ganha traços ameaçadores (sombras, monstros, serpentes, árvores que choram) que espelham o medo de Maria. O estado emocional da personagem e a paisagem se confundem."),
            ("2022-31", "A", "O Romance de 30 retrata com crueza a realidade social do Nordeste. Aqui os retirantes da seca aparecem como “esqueletos redivivos”, sem nenhuma idealização."),
            ("2022-33", "A", "O trecho é uma longa lista de objetos, pessoas e ações do funeral, separados por vírgulas. É essa enumeração que faz o narrador chamar o capítulo de “inventário”."),
            ("2023-31", "C", "O fim do amor, que pedia um cenário solene, acontece numa cena banal à mesa do café, recolhendo migalhas. A comoção do amor frustrado é diluída no cotidiano."),
        ]),
    ]),
    ("HUMANAS", "HUM", [
        ("História do Brasil", [
            ("2020-81", "C", "O jesuíta defende que a Companhia “deve ter e adquirir escravos” para os serviços dos colégios. O texto justifica o uso do trabalho escravo na Colônia."),
            ("2019-81", "B", "No fim do século XVII começou a mineração em Minas Gerais. O Rio de Janeiro virou o principal porto de chegada dos escravizados levados para as minas de ouro."),
            ("2021-52", "D", "A Lei Aberdeen (1845) permitia à Inglaterra apreender navios negreiros. Os britânicos resistiam a revogá-la porque queriam manter proibido o tráfico de escravos para o Brasil."),
            ("2021-73", "A", "O suicídio de Vargas provocou enorme comoção e protestos populares contra seus adversários. Por isso o texto diz que ao tiro “não se seguiu o silêncio que cerca a derrota”."),
            ("2023-80", "A", "O regime militar reprimiu as Ligas Camponesas e criou sindicatos rurais de orientação conservadora. O objetivo era controlar as tensões políticas no campo."),
        ]),
        ("Filosofia", [
            ("2022-86", "C", "Os pré-socráticos buscavam a arché, o princípio que explica a origem e a ordem do cosmos. Empédocles faz isso com os quatro elementos movidos pelo Amor e pelo Ódio."),
            ("2021-82", "B", "Sócrates conduz a conversa com perguntas para chegar à definição do que é a virtude. Esse jogo de perguntas e respostas é o método dialético."),
            ("2019-65", "A", "Maquiavel separa a moral ideal (como se deveria agir) da moral efetiva (o que o governante precisa fazer). Pelo bem comum, o chefe de Estado pode até mentir."),
            ("2020-89", "D", "Hume é empirista: a razão sozinha não conclui nada sobre os fatos, só a experiência mostra causas e efeitos. O conhecimento vem da vivência dos fenômenos."),
            ("2023-54", "A", "Descartes separa a mente (pensamento) do corpo (matéria): é o dualismo. Merleau-Ponty se opõe a isso ao perceber a raiva dentro do próprio corpo."),
        ]),
        ("Cidadania e direitos", [
            ("2019-62", "E", "Num Estado laico e democrático, cada pessoa pode seguir a religião que quiser. Agredir adeptos das religiões de matriz africana restringe essa liberdade de credo."),
            ("2021-66", "B", "O perfil dos presos (jovens, negros, com pouca escolaridade) revela exclusão social. Políticas de inclusão, como educação e emprego, atacam a raiz do problema."),
            ("2022-48", "A", "O relato mostra um guarda branco espancando e amarrando um homem negro, como no tempo da escravidão. É o racismo enraizado nas estruturas da sociedade."),
            ("2022-83", "B", "Apátrida é a pessoa sem nacionalidade reconhecida por nenhum país. As irmãs não puderam ser registradas nem no Líbano, onde nasceram, nem na Síria, país dos pais."),
            ("2023-60", "D", "Controlar o dinheiro, esconder documentos ou destruir pertences da mulher são formas de violência patrimonial, uma das previstas na Lei Maria da Penha."),
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
        ("Genética e biotecnologia", [
            ("2019-110", "D", "Genes próximos no mesmo cromossomo tendem a ser herdados juntos (ligação gênica). Isso contraria a segregação independente proposta por Mendel."),
            ("2020-115", "B", "A soja recebeu um gene de outra espécie para produzir a cianovirina-N. Um organismo que recebe gene de outra espécie é transgênico."),
            ("2021-132", "A", "Pais sem a doença que são portadores têm 1 chance em 4 de ter filho afetado (Aa × Aa → 1/4 aa). Esse é o padrão da herança recessiva."),
            ("2022-98", "E", "O vírus leva uma cópia normal do gene RPE65 às células da retina. Elas passam a expressar esse gene e a produzir a enzima normal."),
            ("2023-125", "E", "Cada pessoa com trissomia forma gametas com um ou dois cromossomos 21. Juntando: 1+1 dá zigoto normal, 1+2 trissômico e 2+2 tetrassômico."),
        ]),
        ("Física e química no cotidiano", [
            ("2020-111", "B", "O fone capta o ruído e emite uma onda sonora oposta a ele. As duas ondas se somam e se anulam: é a interferência destrutiva."),
            ("2020-133", "C", "A carcaça metálica do carro funciona como uma gaiola de Faraday. As cargas ficam na superfície externa e o campo elétrico lá dentro é nulo."),
            ("2021-97", "E", "Óleo (um éster, triglicerídeo) reage com soda cáustica (base forte) e forma sabão e glicerol. Essa reação é a saponificação."),
            ("2022-126", "B", "Depois da coagulação, a sujeira forma flocos pesados que descem para o fundo dos tanques. É na decantação que sai a maior parte dos sólidos."),
            ("2023-108", "D", "A bobina do fogão gera um campo magnético variável, que induz corrente elétrica no fundo metálico da panela (indução eletromagnética)."),
        ]),
    ]),
    ("MATEMATICA", "MAT", [
        ("Porcentagem e proporção", [
            ("2019-138", "E", "O transporte custa 1/5 do valor, ou seja, 20%. Como o gasto total não pode passar de 100%, a recompensa máxima é 100% − 20% = 80%."),
            ("2022-154", "C", "Uma queda de 75% significa que 2013 teve só 25% do número de 2012. Então 28 900 = 0,25 × x, e x = 115 600."),
            ("2023-143", "B", "A lista deste ano é 400 + 10% = 440. Ela deve cair 25%, para 330: são 110 crianças a mais atendidas. Com 10 por sala, 110 ÷ 10 = 11 salas."),
            ("2021-159", "A", "Com 3 pessoas o pneu leva 4 s. Como o tempo é inversamente proporcional, com 2 pessoas leva 4 × 3/2 = 6 s. A troca só termina quando esse grupo acaba: 6 s."),
            ("2022-164", "B", "60 dias têm 60 × 24 × 60 = 86 400 minutos; 86 400 ÷ 48 = 1 800 acionamentos. Então cada um libera 360 ÷ 1 800 = 0,2 mL."),
        ]),
        ("Geometria e escalas", [
            ("2020-138", "B", "Real: 28 080 L = 28 080 000 cm³. Maquete: 2 × 3,51 × 4 = 28,08 cm³. A razão entre os volumes é 1 000 000 = 100³, logo a escala é 1 : 100."),
            ("2022-139", "A", "Na escala 1 : 50, 3,8 cm viram 1,90 m e 1,6 cm viram 0,80 m. Tirando 10 cm em cima, sobra 1,80 m; tirando 10 cm de cada lado, sobram 0,60 m."),
            ("2019-142", "E", "O novo diâmetro é 6 + 8 = 14 m (raio 7). Área nova: 3 × 7² = 147 m²; área antiga: 3 × 3² = 27 m². A ampliação é de 120 m², mais que os 100 m² do estoque."),
            ("2021-166", "B", "Dobrando comprimento e largura e mantendo a altura, o perímetro dobra (paredes: o dobro de tinta) e a área do piso fica 2 × 2 = 4 vezes maior."),
            ("2022-174", "D", "Cilindro: π × 4² × 50 = 800π cm³. Esfera de raio 0,5 cm: (4/3) × π × 0,5³ = π/6 cm³. Então cabem 800π ÷ (π/6) = 4 800 esferas."),
        ]),
        ("Probabilidade e média", [
            ("2019-159", "D", "Com n placas, a chance de não ver nenhuma é (1/2)^n, que precisa ser menor que 1/100. Isso exige n = 7 placas. Como já existe 1, faltam 6 novas."),
            ("2019-173", "E", "P(fraudulenta) = 0,20 × 0,25 + 0,80 × 0,0625 = 0,05 + 0,05 = 0,10. P(inconsistente, dado que é fraudulenta) = 0,05 ÷ 0,10 = 0,5."),
            ("2023-138", "C", "É preciso 0,20 × P(preta em B) ≤ 0,01, ou seja, P ≤ 5%. Com 4 pretas, a urna B precisa ter 80 bolinhas. Hoje tem 16 (4 pretas são 25%), então faltam 64 brancas."),
            ("2020-165", "D", "Soma atual: 15 × 1,93 = 28,95. Meta: 15 × 1,99 = 29,85. Tirando os quatro mais baixos (7,30) e pondo o de 2,02, a soma vai a 23,67. Os 3 novos somam 6,18: média 2,06 m."),
            ("2023-180", "C", "Médias de dois meses seguidos: out-nov 200, nov-dez 175, dez-jan 325, jan-fev 275 e fev-mar 150 mm. A maior é dezembro-janeiro: plantar no início de dezembro."),
        ]),
    ]),
]


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


def main():
    bruto = carregar_bruto()
    divergentes, areas = [], []
    for area_id, sigla, licoes in AREAS:
        area = {"id": area_id, "licoes": []}
        for li, (titulo, questoes) in enumerate(licoes, start=1):
            licao_id = f"{sigla}-{li:02d}"
            licao = {"id": licao_id, "titulo": titulo, "questoes": []}
            for qi, (chave, cega, explicacao) in enumerate(questoes, start=1):
                q = bruto[chave]
                if cega != q["correctAlternative"]:
                    divergentes.append(f"{chave}: cega {cega}, oficial {q['correctAlternative']}")
                licao["questoes"].append({
                    "id": f"{licao_id}-{qi:02d}",
                    "texto": texto_puro(q.get("context")),
                    "enunciado": texto_puro(q["alternativesIntroduction"]),
                    "alternativas": [texto_puro(a["text"]) for a in q["alternatives"]],
                    "correta": q["correctAlternative"],
                    "explicacao": explicacao,
                    "fonte": f"ENEM {q['year']} – Questão {q['index']}",
                })
            area["licoes"].append(licao)
        areas.append(area)

    if divergentes:
        print("DIVERGÊNCIAS com o gabarito oficial (nada foi gravado):")
        print("\n".join(divergentes))
        sys.exit(1)
    SAIDA.write_text(json.dumps({"versao": 1, "areas": areas}, ensure_ascii=False, indent=2) + "\n",
                     encoding="utf-8")
    print(f"OK: {SAIDA}")


if __name__ == "__main__":
    main()
