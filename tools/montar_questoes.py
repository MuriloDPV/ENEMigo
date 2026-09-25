"""Monta app/src/main/assets/questoes.json com questões reais do ENEM (API enem.dev).

Matemática ficou de fora de propósito: o app mostra a área como "Em breve".

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
        ("Esporte e práticas corporais", [
            ("2020-35", "A", "O texto mostra que colocar seleções femininas no videogame ajuda a divulgar o futebol feminino, “um grande passo” na luta pela igualdade de gênero."),
            ("2020-36", "D", "Pela definição, luta tem contato proposital, troca de ações de ataque e defesa e regras. A proposta deve ser um combate intencional com ações regulamentadas, sem agressão e sem vencedor definido antes."),
            ("2021-31", "A", "As rodas de uretano deram aderência e facilitaram as manobras, e até iniciantes passaram a andar de skate. Com campeonatos, marcas e lojas, a prática se democratizou."),
            ("2023-6", "D", "A palavra “juntos” entrou no lema “para ressaltar a solidariedade”. O lema olímpico passou a se associar à cooperação."),
            ("2023-42", "E", "O lazer ativo se concentra nos mais ricos e a inatividade é maior entre quem tem menos renda. O fator central é a desigualdade entre classes sociais."),
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
        ("Corpo humano e saúde", [
            ("2019-133", "A", "O rim filtra o sangue e elimina a ureia, resíduo tóxico do metabolismo. É isso que a hemodiálise faz hoje e que o rim biônico fará."),
            ("2020-108", "C", "O revestimento só libera o remédio em pH perto do neutro. Assim ele atravessa intacto o estômago ácido e só se abre no intestino, sem ser afetado pelas secreções gástricas."),
            ("2021-114", "B", "O reflexo patelar é um arco reflexo: o estímulo vai até a medula espinal, que manda a resposta direto ao músculo, sem passar pelo cérebro."),
            ("2022-104", "E", "A eritropoietina estimula a produção de hemácias. Com mais hemácias, o sangue leva mais oxigênio aos músculos."),
            ("2023-106", "C", "O RNAm entra nas células e é traduzido pelos ribossomos em uma proteína do vírus. O sistema imune reconhece essa proteína como antígeno e cria defesas."),
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
