"""Baixa as questões do ENEM da API pública enem.dev para tools/enem_raw/{ano}.json."""
import json
import sys
import time
import urllib.request
from pathlib import Path

BASE = "https://api.enem.dev/v1"
PASTA = Path(__file__).parent / "enem_raw"
PAUSA = 2.0  # segundos entre requisições (limite da API: 10 por janela)


def get(url):
    for tentativa in range(5):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "ENEMigo-escolar"})
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.loads(r.read().decode("utf-8"))
        except Exception as e:  # 429 ou rede: espera e tenta de novo
            espera = 15 * (tentativa + 1)
            print(f"  erro {e} em {url}; nova tentativa em {espera}s", file=sys.stderr)
            time.sleep(espera)
    raise RuntimeError(f"falhou: {url}")


def baixar(ano):
    destino = PASTA / f"{ano}.json"
    if destino.exists():
        print(f"{ano}: já baixado")
        return
    questoes, offset = [], 0
    while True:
        dados = get(f"{BASE}/exams/{ano}/questions?limit=50&offset={offset}")
        questoes += dados["questions"]
        print(f"{ano}: {len(questoes)}/{dados['metadata']['total']}")
        time.sleep(PAUSA)
        if not dados["metadata"]["hasMore"]:
            break
        offset += 50
    destino.write_text(json.dumps(questoes, ensure_ascii=False, indent=1), encoding="utf-8")


if __name__ == "__main__":
    PASTA.mkdir(exist_ok=True)
    for ano in sys.argv[1:] or ["2023", "2022", "2021", "2020", "2019"]:
        baixar(int(ano))
