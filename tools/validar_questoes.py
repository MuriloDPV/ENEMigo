"""Valida app/src/main/assets/questoes.json contra a spec (seção 9.3) e contra o bruto da API.

Também confere as imagens e fórmulas de Matemática em app/src/main/assets/imagens/.

Uso: python tools/validar_questoes.py   (sai com código 1 se houver erro)
"""
import json
import re
import struct
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from montar_questoes import (AREAS as AREAS_MONTAGEM, CORRECOES, FORMULAS, IMAGEM_MD, IMAGENS,  # noqa: E402
                             SAIDA, campos, carregar_bruto)

AREAS = {"LINGUAGENS": "LIN", "HUMANAS": "HUM", "NATUREZA": "NAT", "MATEMATICA": "MAT"}
DISCIPLINAS = {"LIN": "linguagens", "HUM": "ciencias-humanas", "NAT": "ciencias-natureza",
               "MAT": "matematica"}
LICOES_POR_AREA = 4
TOTAL = 80
CAMPOS = {"id", "texto", "enunciado", "alternativas", "correta", "explicacao", "fonte"}
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

if set(dados) != {"versao", "areas"}:
    erro(f"raiz com chaves inesperadas: {set(dados)}")
if sorted(a["id"] for a in dados["areas"]) != sorted(AREAS):
    erro(f"áreas esperadas exatamente uma vez: {sorted(AREAS)}")

ids_licao, ids_questao, fontes, arquivos_usados = set(), set(), set(), set()
total = 0
for area in dados["areas"]:
    sigla = AREAS.get(area["id"])
    if len(area["licoes"]) != LICOES_POR_AREA:
        erro(f"{area['id']}: {len(area['licoes'])} lições (esperado {LICOES_POR_AREA})")
    for li, licao in enumerate(area["licoes"], start=1):
        lid = licao["id"]
        if lid != f"{sigla}-{li:02d}" or lid in ids_licao:
            erro(f"id de lição inválido/repetido: {lid}")
        ids_licao.add(lid)
        if not licao.get("titulo", "").strip():
            erro(f"{lid}: sem título")
        if len(licao["questoes"]) != 5:
            erro(f"{lid}: {len(licao['questoes'])} questões (esperado 5)")
        for qi, q in enumerate(licao["questoes"], start=1):
            total += 1
            qid = q.get("id")
            if set(q) != CAMPOS:
                erro(f"{qid}: campos {set(q) ^ CAMPOS}")
            if qid != f"{lid}-{qi:02d}" or qid in ids_questao:
                erro(f"id de questão inválido/repetido: {qid}")
            ids_questao.add(qid)
            if not q["enunciado"].strip() or not q["explicacao"].strip():
                erro(f"{qid}: enunciado ou explicação vazios")
            if len(q["alternativas"]) != 5 or not all(a.strip() for a in q["alternativas"]):
                erro(f"{qid}: precisa de 5 alternativas não vazias")
            if q["correta"] not in list("ABCDE"):
                erro(f"{qid}: correta inválida {q['correta']!r}")

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
            if q["correta"] != b["correctAlternative"]:
                erro(f"{qid}: gabarito {q['correta']} ≠ oficial {b['correctAlternative']}")
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

if total != TOTAL:
    erro(f"total de questões {total} (esperado {TOTAL})")

orfas = sorted(p.name for p in IMAGENS.iterdir() if p.name not in arquivos_usados) if IMAGENS.is_dir() else []
if orfas:
    erro(f"arquivos em {IMAGENS.name}/ que nenhuma questão usa: {orfas}")
chaves_montagem = {c for _, _, licoes in AREAS_MONTAGEM for _, qs in licoes for c, _, _ in qs}
for nome, tabela in (("FORMULAS", FORMULAS), ("CORRECOES", CORRECOES)):
    for chave in sorted(set(tabela) - chaves_montagem):
        erro(f"{nome} tem {chave}, que não está no banco")
for chave, lista in FORMULAS.items():
    if len(lista) != 5:
        erro(f"FORMULAS[{chave}] precisa de 5 fórmulas")

if erros:
    print(f"{len(erros)} erro(s):")
    print("\n".join(erros))
    sys.exit(1)
print(f"OK: {total} questões, {len(ids_licao)} lições, {len({k[:4] for k in fontes})} anos, "
      f"{len(arquivos_usados)} imagens/fórmulas; gabaritos e alternativas conferem com o bruto da API.")
