"""Filtra do bruto só as questões 100% texto e grava tools/enem_raw/candidatas.json."""
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


def valida(q):
    alts = q.get("alternatives") or []
    return (
        not q.get("language")
        and not q.get("files")
        and len(alts) == 5
        and [a.get("letter") for a in alts] == list("ABCDE")
        and all(a.get("file") is None and (a.get("text") or "").strip() for a in alts)
        and q.get("correctAlternative") in list("ABCDE")
        and sum(1 for a in alts if a.get("isCorrect")) == 1
        and next(a["letter"] for a in alts if a.get("isCorrect")) == q["correctAlternative"]
        and (q.get("alternativesIntroduction") or "").strip()
        and not any(IMAGEM.search(t) or LATEX.search(t) for t in textos(q))
    )


if __name__ == "__main__":
    todas, boas = [], []
    for arq in sorted(PASTA.glob("20*.json")):
        todas += json.loads(arq.read_text(encoding="utf-8"))
    for q in todas:
        if valida(q):
            q["_tam"] = sum(len(t) for t in textos(q))
            boas.append(q)
    (PASTA / "candidatas.json").write_text(json.dumps(boas, ensure_ascii=False, indent=1), encoding="utf-8")
    from collections import Counter
    print("total", len(todas), "válidas", len(boas))
    print(Counter((q["discipline"]) for q in boas))
    print(Counter((q["discipline"]) for q in boas if q["_tam"] <= 1200))
