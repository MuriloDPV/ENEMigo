"""Recorta da prova oficial do INEP (caderno azul) as figuras que vieram ruins da API enem.dev.

Uso: python tools/recortar_figuras.py
Baixa a prova para tools/enem_raw/provas/ (se ainda não estiver lá), renderiza o trecho de cada
figura de RECORTES em alta resolução, tira a margem branca e grava o PNG em tools/figuras/.
O montar_questoes.py usa as figuras de tools/figuras/ no lugar das da API.
Precisa de: pip install -r tools/requirements.txt (pymupdf)
"""
import sys
import urllib.request
from pathlib import Path

PASTA = Path(__file__).parent
PROVAS = PASTA / "enem_raw" / "provas"
SAIDA = PASTA / "figuras"
ESCALA = 5  # 360 dpi: o gráfico de uma alternativa fica com uns 700 px de largura
MARGEM_PX = 12
BRANCO = 245  # pixel mais claro que isso conta como fundo

URL_PROVAS = {
    "2021_PV_impresso_D2_CD7.pdf":
        "https://download.inep.gov.br/enem/provas_e_gabaritos/2021_PV_impresso_D2_CD7.pdf",
}

# figura -> (prova, página impressa no rodapé, retângulo em pontos (x0, y0, x1, y1)).
# O retângulo só precisa conter a figura sem pegar a letra da alternativa nem a figura vizinha:
# a margem branca sai depois.
RECORTES = {
    # 2021-162: na API a alternativa A (a correta) tinha metade da resolução das outras e traço
    # cinza, o que entregava a resposta.
    "2021-162-a.png": ("2021_PV_impresso_D2_CD7.pdf", 27, (50, 196, 212, 358)),
    "2021-162-b.png": ("2021_PV_impresso_D2_CD7.pdf", 27, (50, 429, 212, 533)),
    "2021-162-c.png": ("2021_PV_impresso_D2_CD7.pdf", 27, (50, 596, 212, 700)),
    "2021-162-d.png": ("2021_PV_impresso_D2_CD7.pdf", 27, (302, 196, 467, 358)),
    "2021-162-e.png": ("2021_PV_impresso_D2_CD7.pdf", 27, (302, 429, 467, 533)),
}


def prova(nome):
    caminho = PROVAS / nome
    if not caminho.exists():
        PROVAS.mkdir(parents=True, exist_ok=True)
        req = urllib.request.Request(URL_PROVAS[nome], headers={"User-Agent": "ENEMigo-escolar"})
        with urllib.request.urlopen(req, timeout=60) as r:
            caminho.write_bytes(r.read())
        print(f"  baixada {nome}")
    return caminho


def recortar(pdf, pagina, retangulo, destino):
    import pymupdf  # só quem recorta figuras precisa do PyMuPDF
    from PIL import Image, ImageOps

    with pymupdf.open(pdf) as doc:
        pix = doc[pagina - 1].get_pixmap(matrix=pymupdf.Matrix(ESCALA, ESCALA),
                                         clip=pymupdf.Rect(*retangulo), colorspace=pymupdf.csGRAY)
    img = Image.frombytes("L", (pix.width, pix.height), pix.samples)
    caixa = ImageOps.invert(img).point(lambda v: 255 if v > 255 - BRANCO else 0).getbbox()
    if caixa is None:
        raise ValueError(f"{destino.name}: retângulo vazio")
    x0, y0, x1, y1 = caixa
    img = img.crop((max(0, x0 - MARGEM_PX), max(0, y0 - MARGEM_PX),
                    min(img.width, x1 + MARGEM_PX), min(img.height, y1 + MARGEM_PX)))
    img.save(destino, optimize=True)
    print(f"  {destino.name}: {img.width} x {img.height} px")


def main():
    sys.stdout.reconfigure(encoding="utf-8")  # o console do Windows não é UTF-8
    SAIDA.mkdir(exist_ok=True)
    for arquivo, (nome, pagina, retangulo) in RECORTES.items():
        recortar(prova(nome), pagina, retangulo, SAIDA / arquivo)


if __name__ == "__main__":
    main()
