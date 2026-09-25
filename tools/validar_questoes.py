"""Valida app/src/main/assets/questoes.json contra a spec (seção 9.3) e contra o bruto da API.

Uso: python tools/validar_questoes.py   (sai com código 1 se houver erro)
"""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from montar_questoes import SAIDA, carregar_bruto, texto_puro  # noqa: E402

AREAS = {"LINGUAGENS": "LIN", "HUMANAS": "HUM", "NATUREZA": "NAT"}  # Matemática fora de propósito
LICOES_POR_AREA = 4
CAMPOS = {"id", "texto", "enunciado", "alternativas", "correta", "explicacao", "fonte"}
RESTO_MD = re.compile(r"\*\*|!\[|\]\(|\$|\\[a-zA-Z(\[]|^#", re.M)
FONTE = re.compile(r"^ENEM (\d{4}) – Questão (\d+)$")

erros = []


def erro(msg):
    erros.append(msg)


raw_bytes = SAIDA.read_bytes()
if raw_bytes.startswith(b"\xef\xbb\xbf"):
    erro("arquivo com BOM")
dados = json.loads(raw_bytes.decode("utf-8"))  # falha aqui = JSON inválido
bruto = carregar_bruto()

if set(dados) != {"versao", "areas"}:
    erro(f"raiz com chaves inesperadas: {set(dados)}")
if sorted(a["id"] for a in dados["areas"]) != sorted(AREAS):
    erro(f"áreas esperadas exatamente uma vez: {sorted(AREAS)}")

ids_licao, ids_questao, fontes = set(), set(), set()
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
            for campo in ("texto", "enunciado", *q["alternativas"]):
                valor = q[campo] if campo in ("texto", "enunciado") else campo
                if RESTO_MD.search(valor):
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
            if b["discipline"] != {"LIN": "linguagens", "HUM": "ciencias-humanas",
                                   "NAT": "ciencias-natureza", "MAT": "matematica"}[sigla]:
                erro(f"{qid}: disciplina no bruto é {b['discipline']}")
            if b.get("language") or b.get("files") or any(a.get("file") for a in b["alternatives"]):
                erro(f"{qid}: bruto tem língua estrangeira ou arquivo")
            if q["correta"] != b["correctAlternative"]:
                erro(f"{qid}: gabarito {q['correta']} ≠ oficial {b['correctAlternative']}")
            certa = [a["letter"] for a in b["alternatives"] if a["isCorrect"]]
            if certa != [q["correta"]]:
                erro(f"{qid}: isCorrect do bruto aponta {certa}")
            if [a["letter"] for a in b["alternatives"]] != list("ABCDE"):
                erro(f"{qid}: letras do bruto fora de A–E")
            for letra, alt_app, alt_raw in zip("ABCDE", q["alternativas"], b["alternatives"]):
                if alt_app != texto_puro(alt_raw["text"]):
                    erro(f"{qid}: alternativa {letra} difere do bruto")
            if q["enunciado"] != texto_puro(b["alternativesIntroduction"]):
                erro(f"{qid}: enunciado difere do bruto")
            if q["texto"] != texto_puro(b.get("context")):
                erro(f"{qid}: texto difere do bruto")

if total != 60:
    erro(f"total de questões {total} (esperado 60)")

if erros:
    print(f"{len(erros)} erro(s):")
    print("\n".join(erros))
    sys.exit(1)
print(f"OK: {total} questões, {len(ids_licao)} lições, {len({k[:4] for k in fontes})} anos, "
      f"gabaritos e alternativas conferem com o bruto da API.")
