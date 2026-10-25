"""Valida app/src/main/assets/questoes.json contra a spec (seção 9.3) e contra o bruto da API.

Também confere o gabarito oficial do INEP, as anuladas, o limite de palavras, as imagens e
fórmulas de Matemática em app/src/main/assets/imagens/, o tempo estimado de cada lição e o mapa
"ids_antigos" usado para migrar o progresso salvo.

Uso: python tools/validar_questoes.py   (sai com código 1 se houver erro)
"""
import json
import re
import struct
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from montar_questoes import (ALVO_LICAO_S, ANULADAS, AREAS as AREAS_MONTAGEM, CORRECOES,  # noqa: E402
                             FIGURAS, FORMULAS, GABARITO_INEP, IMAGEM_MD, IMAGENS, LIMITE_PALAVRAS,
                             MAX_QUESTOES, MIN_QUESTOES, SAIDA, SEM_PONTO, VERSAO, VERSOES_ANTIGAS,
                             campos, carregar_bruto, contar_palavras, estimar_segundos, ids_antigos)

AREAS = {"LINGUAGENS": "LIN", "HUMANAS": "HUM", "NATUREZA": "NAT", "MATEMATICA": "MAT"}
DISCIPLINAS = {"LIN": "linguagens", "HUM": "ciencias-humanas", "NAT": "ciencias-natureza",
               "MAT": "matematica"}
TOTAL = 80
CAMPOS = {"id", "texto", "enunciado", "alternativas", "correta", "explicacao", "fonte",
          "segundos_estimados"}
# Tempo das lições (pela estimativa). Nenhuma passa do teto, e a média da área fica perto do
# alvo. Quando nem com o máximo de lições a área chega lá (Matemática: 3 questões já passam de
# 5 min), as duas regras não se aplicam: ela fica com o maior número de lições possível.
TETO_LICAO_S = 6 * 60
FOLGA_MEDIA_S = 60
TOKEN = re.compile(r"\[\[(imagem|formula):([\w.\-]+)\]\]")
RESTO_MD = re.compile(r"\*\*|!\[|\]\(|(?<!R)\$|\\[a-zA-Z(\[]|^#|\[\[|\]\]|[​\x00]", re.M)
FONTE = re.compile(r"^ENEM (\d{4}) – Questão (\d+)$")

sys.stdout.reconfigure(encoding="utf-8")  # o console do Windows não é UTF-8
erros = []


def erro(msg):
    erros.append(msg)


def tamanho_png(caminho):
    """(largura, altura) lidos do cabeçalho IHDR, ou None se não for PNG."""
    cabecalho = caminho.read_bytes()[:24]
    if cabecalho[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    return struct.unpack(">II", cabecalho[16:24])


raw_bytes = SAIDA.read_bytes()
if raw_bytes.startswith(b"\xef\xbb\xbf"):
    erro("arquivo com BOM")
dados = json.loads(raw_bytes.decode("utf-8"))  # falha aqui = JSON inválido
bruto = carregar_bruto()

if set(dados) != {"versao", "areas", "ids_antigos"}:
    erro(f"raiz com chaves inesperadas: {set(dados)}")
if dados.get("versao") != VERSAO:
    erro(f"versao {dados.get('versao')!r} (esperado {VERSAO})")
if sorted(a["id"] for a in dados["areas"]) != sorted(AREAS):
    erro(f"áreas esperadas exatamente uma vez: {sorted(AREAS)}")

ids_licao, ids_questao, fontes, arquivos_usados = set(), set(), set(), set()
total = 0
resumo = []
for area in dados["areas"]:
    sigla = AREAS.get(area["id"])
    tempos = []
    for li, licao in enumerate(area["licoes"], start=1):
        lid = licao["id"]
        if lid != f"{sigla}-{li:02d}" or lid in ids_licao:
            erro(f"id de lição inválido/repetido: {lid}")
        ids_licao.add(lid)
        if not licao.get("titulo", "").strip():
            erro(f"{lid}: sem título")
        if not MIN_QUESTOES <= len(licao["questoes"]) <= MAX_QUESTOES:
            erro(f"{lid}: {len(licao['questoes'])} questões (esperado de {MIN_QUESTOES} a {MAX_QUESTOES})")
        tempos.append(sum(q.get("segundos_estimados", 0) for q in licao["questoes"]))
        for qi, q in enumerate(licao["questoes"], start=1):
            total += 1
            qid = q.get("id")
            if set(q) != CAMPOS:
                erro(f"{qid}: campos {set(q) ^ CAMPOS}")
            if qid != f"{lid}-{qi:02d}" or qid in ids_questao:
                erro(f"id de questão inválido/repetido: {qid}")
            ids_questao.add(qid)
            if q.get("segundos_estimados") != estimar_segundos(area["id"], q):
                erro(f"{qid}: segundos_estimados {q.get('segundos_estimados')} ≠ "
                     f"{estimar_segundos(area['id'], q)} da fórmula")
            if not q["enunciado"].strip() or not q["explicacao"].strip():
                erro(f"{qid}: enunciado ou explicação vazios")
            if len(q["alternativas"]) != 5 or not all(a.strip() for a in q["alternativas"]):
                erro(f"{qid}: precisa de 5 alternativas não vazias")
            if q["correta"] not in list("ABCDE"):
                erro(f"{qid}: correta inválida {q['correta']!r}")
            if contar_palavras(q) > LIMITE_PALAVRAS[area["id"]]:
                erro(f"{qid}: {contar_palavras(q)} palavras (máximo {LIMITE_PALAVRAS[area['id']]})")

            # Marcadores de imagem/fórmula: só em Matemática, e cada arquivo tem que existir.
            for valor in (q["texto"], q["enunciado"], *q["alternativas"]):
                for tipo, arquivo in TOKEN.findall(valor):
                    arquivos_usados.add(arquivo)
                    caminho = IMAGENS / arquivo
                    if sigla != "MAT":
                        erro(f"{qid}: imagem fora de Matemática ({arquivo})")
                    if not caminho.is_file():
                        erro(f"{qid}: {arquivo} não existe em {IMAGENS.name}/")
                    elif not tamanho_png(caminho):
                        erro(f"{qid}: {arquivo} não é um PNG válido")
                if RESTO_MD.search(TOKEN.sub("", valor)):
                    erro(f"{qid}: sobra de markdown/LaTeX/imagem: {valor[:60]!r}")

            # Confronto com o bruto da API
            m = FONTE.match(q["fonte"])
            if not m:
                erro(f"{qid}: fonte fora do padrão: {q['fonte']!r}")
                continue
            chave = f"{m.group(1)}-{m.group(2)}"
            if chave in fontes:
                erro(f"{qid}: questão do ENEM repetida ({chave})")
            fontes.add(chave)
            b = bruto.get(chave)
            if b is None:
                erro(f"{qid}: {chave} não existe no bruto")
                continue
            if b["discipline"] != DISCIPLINAS[sigla]:
                erro(f"{qid}: disciplina no bruto é {b['discipline']}")
            if b.get("language"):
                erro(f"{qid}: bruto é de língua estrangeira")
            if sigla != "MAT" and (b.get("files") or any(a.get("file") for a in b["alternatives"])):
                erro(f"{qid}: bruto tem arquivo fora de Matemática")
            if q["correta"] != GABARITO_INEP.get(chave):
                erro(f"{qid}: gabarito {q['correta']} ≠ INEP {GABARITO_INEP.get(chave)}")
            if q["correta"] != b["correctAlternative"]:
                erro(f"{qid}: gabarito {q['correta']} ≠ API {b['correctAlternative']}")
            if chave in ANULADAS:
                erro(f"{qid}: {chave} foi anulada ou abandonada pela TRI")
            certa = [a["letter"] for a in b["alternatives"] if a["isCorrect"]]
            if certa != [q["correta"]]:
                erro(f"{qid}: isCorrect do bruto aponta {certa}")
            if [a["letter"] for a in b["alternatives"]] != list("ABCDE"):
                erro(f"{qid}: letras do bruto fora de A–E")

            esperado, _ = campos(b, chave)
            for letra, alt_app, alt_esp, alt_raw in zip("ABCDE", q["alternativas"], esperado["alternativas"],
                                                        b["alternatives"]):
                if alt_app != alt_esp:
                    erro(f"{qid}: alternativa {letra} difere do bruto")
                # A alternativa é imagem/fórmula exatamente quando o bruto é imagem.
                if bool(alt_raw.get("file")) != bool(TOKEN.fullmatch(alt_app)):
                    erro(f"{qid}: alternativa {letra} trocou imagem por texto (ou o contrário)")
            for campo in ("texto", "enunciado"):
                if q[campo] != esperado[campo]:
                    erro(f"{qid}: {campo} difere do bruto")
            imagens_bruto = len(IMAGEM_MD.findall((b.get("context") or "") + (b.get("alternativesIntroduction") or "")))
            imagens_app = len(TOKEN.findall(q["texto"] + q["enunciado"]))
            if imagens_bruto != imagens_app:
                erro(f"{qid}: {imagens_bruto} imagem(ns) no bruto e {imagens_app} no app")

    # Tempo das lições da área (ver TETO_LICAO_S e FOLGA_MEDIA_S)
    if tempos:
        questoes_area = sum(len(licao["questoes"]) for licao in area["licoes"])
        media = sum(tempos) / len(tempos)
        sem_saida = len(tempos) == questoes_area // MIN_QUESTOES and media > ALVO_LICAO_S + FOLGA_MEDIA_S
        if sem_saida:
            for licao in area["licoes"]:
                if len(licao["questoes"]) > MIN_QUESTOES + 1:
                    erro(f"{licao['id']}: área acima do alvo, então as lições ficam com "
                         f"{MIN_QUESTOES} ou {MIN_QUESTOES + 1} questões")
        else:
            if media > ALVO_LICAO_S + FOLGA_MEDIA_S:
                erro(f"{area['id']}: média de {media / 60:.1f} min por lição; divida em mais lições")
            for licao, segundos in zip(area["licoes"], tempos):
                if segundos > TETO_LICAO_S:
                    erro(f"{licao['id']}: {segundos / 60:.1f} min, acima do teto de {TETO_LICAO_S // 60} min")
        resumo.append(f"  {area['id']}: {len(tempos)} lições, {min(tempos) / 60:.1f} a "
                      f"{max(tempos) / 60:.1f} min (média {media / 60:.1f})")

# Mapa da migração: em cada versão antiga, todas as questões dela, cada uma levada a uma questão
# atual diferente ou a "" (saiu do banco).
mapa = dados.get("ids_antigos", {})
if mapa != ids_antigos():
    erro("ids_antigos diferente do que o montar_questoes.py gera a partir de VERSOES_ANTIGAS")
for versao, ids in mapa.items():
    atuais = [atual for atual in ids.values() if atual]
    if len(atuais) != len(set(atuais)) or not set(atuais) <= ids_questao:
        erro(f"ids_antigos[{versao}] leva duas questões antigas à mesma atual ou a uma que não existe")
    tamanho = sum(len(chaves) for licoes in VERSOES_ANTIGAS[versao].values() for chaves in licoes)
    if len(ids) != tamanho:
        erro(f"ids_antigos[{versao}] tem {len(ids)} questões; a versão {versao} tinha {tamanho}")
if set(mapa) != {str(v) for v in range(1, VERSAO)}:
    erro(f"ids_antigos precisa ter as versões 1 a {VERSAO - 1}")

if total != TOTAL:
    erro(f"total de questões {total} (esperado {TOTAL})")

orfas = sorted(p.name for p in IMAGENS.iterdir() if p.name not in arquivos_usados) if IMAGENS.is_dir() else []
if orfas:
    erro(f"arquivos em {IMAGENS.name}/ que nenhuma questão usa: {orfas}")
# Figuras recortadas da prova oficial: cada uma substitui uma figura usada e tem que estar no app.
for figura in sorted(FIGURAS.glob("*.png")) if FIGURAS.is_dir() else []:
    if figura.name not in arquivos_usados:
        erro(f"tools/figuras/{figura.name} não substitui nenhuma figura do banco")
    elif (IMAGENS / figura.name).read_bytes() != figura.read_bytes():
        erro(f"{figura.name} no app difere de tools/figuras/ (rode o montar_questoes.py)")
chaves_montagem = {c for _, _, licoes in AREAS_MONTAGEM for _, qs in licoes for c, _, _ in qs}
for nome, tabela in (("FORMULAS", FORMULAS), ("CORRECOES", CORRECOES), ("SEM_PONTO", SEM_PONTO),
                     ("GABARITO_INEP", GABARITO_INEP)):
    for chave in sorted(set(tabela) - chaves_montagem):
        erro(f"{nome} tem {chave}, que não está no banco")
for chave, letras in SEM_PONTO.items():
    if not letras or set(letras) - set("ABCDE"):
        erro(f"SEM_PONTO[{chave}] precisa de letras de A a E")
for chave, lista in FORMULAS.items():
    if len(lista) != 5:
        erro(f"FORMULAS[{chave}] precisa de 5 fórmulas")

if erros:
    print(f"{len(erros)} erro(s):")
    print("\n".join(erros))
    sys.exit(1)
print(f"OK: {total} questões, {len(ids_licao)} lições, {len({k[:4] for k in fontes})} anos, "
      f"{len(arquivos_usados)} imagens/fórmulas; gabaritos conferem com o INEP e a API, e o texto com o bruto.")
print("Tempo estimado por lição:")
print("\n".join(resumo))
