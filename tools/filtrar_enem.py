"""Filtra do bruto as questões aproveitáveis e grava tools/enem_raw/candidatas.json.

- Linguagens, Humanas e Natureza: só questões 100% texto (sem imagem e sem fórmula).
- Matemática: imagem é permitida (o app mostra figuras e fórmulas). Cada candidata ganha
  "_tipo": "texto", "figura" (imagem no enunciado) ou "alternativas-imagem" (alternativas
  que são figura ou fórmula; as fórmulas precisam ser redigitadas em FORMULAS no montar).
"""
import json
import re
from pathlib import Path

PASTA = Path(__file__).parent / "enem_raw"
IMAGEM = re.compile(r"!\[|<img|\.(png|jpe?g|svg|gif)\b", re.I)
LATEX = re.compile(r"\$|\\\(|\\\[|\\[a-zA-Z]+|\^\{|_\{")


def textos(q):
    yield q.get("context") or ""
    yield q.get("alternativesIntroduction") or ""
    for a in q.get("alternatives") or []:
        yield a.get("text") or ""


def estrutura_ok(q):
    """5 alternativas A-E, gabarito coerente e enunciado não vazio."""
    alts = q.get("alternatives") or []
    return (
        not q.get("language")
        and len(alts) == 5
        and [a.get("letter") for a in alts] == list("ABCDE")
        and q.get("correctAlternative") in list("ABCDE")
        and sum(1 for a in alts if a.get("isCorrect")) == 1
        and next(a["letter"] for a in alts if a.get("isCorrect")) == q["correctAlternative"]
        and bool((q.get("alternativesIntroduction") or "").strip())
    )


def valida(q):
    alts = q.get("alternatives") or []
    return (
        estrutura_ok(q)
        and not q.get("files")
        and all(a.get("file") is None and (a.get("text") or "").strip() for a in alts)
        and not any(IMAGEM.search(t) or LATEX.search(t) for t in textos(q))
    )


def tipo_matematica(q):
    """Tipo da questão de Matemática aproveitável, ou None se ela não servir."""
    alts = q.get("alternatives") or []
    if q.get("discipline") != "matematica" or not estrutura_ok(q):
        return None
    if not all(a.get("file") or (a.get("text") or "").strip() for a in alts):
        return None
    if any(a.get("file") for a in alts):
        return "alternativas-imagem"
    if q.get("files") or any(IMAGEM.search(t) for t in textos(q)):
        return "figura"
    return "texto"


if __name__ == "__main__":
    import sys
    sys.stdout.reconfigure(encoding="utf-8")  # o console do Windows não é UTF-8
    todas, boas = [], []
    for arq in sorted(PASTA.glob("20*.json")):
        todas += json.loads(arq.read_text(encoding="utf-8"))
    for q in todas:
        tipo = tipo_matematica(q)
        if tipo or (q.get("discipline") != "matematica" and valida(q)):
            q["_tam"] = sum(len(t) for t in textos(q))
            if tipo:
                q["_tipo"] = tipo
            boas.append(q)
    (PASTA / "candidatas.json").write_text(json.dumps(boas, ensure_ascii=False, indent=1), encoding="utf-8")
    from collections import Counter
    print("total", len(todas), "válidas", len(boas))
    print(Counter((q["discipline"]) for q in boas))
    print(Counter((q["discipline"]) for q in boas if q["_tam"] <= 1200))
    print("matemática por tipo:", Counter(q["_tipo"] for q in boas if "_tipo" in q))
