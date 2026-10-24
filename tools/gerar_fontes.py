"""Gera as fontes do app (app/src/main/res/font) a partir das fontes variáveis do Google Fonts.

O Android 8 não escolhe sozinho o peso de uma fonte variável, então o script cria um arquivo
estático para cada peso usado e recorta só os caracteres que o app precisa (português, símbolos
de matemática e tudo que aparece no questoes.json e no strings.xml), para o APK ficar leve.

Para trocar a fonte da marca, mude FONTES e INSTANCIAS e ajuste os res/font/marca_*.xml.

uso: pip install fonttools && python tools/gerar_fontes.py
"""
import io
import urllib.request
from pathlib import Path

from fontTools import subset
from fontTools.ttLib import TTFont
from fontTools.varLib import instancer

RAIZ = Path(__file__).resolve().parent.parent
FONTES_RES = RAIZ / "app" / "src" / "main" / "res" / "font"
LICENCAS = RAIZ / "app" / "src" / "main" / "assets" / "licencas"
QUESTOES = RAIZ / "app" / "src" / "main" / "assets" / "questoes.json"
STRINGS = RAIZ / "app" / "src" / "main" / "res" / "values" / "strings.xml"

GOOGLE_FONTS = "https://raw.githubusercontent.com/google/fonts/main/ofl/"
# nome -> (fonte variável, licença no repositório do Google Fonts)
FONTES = {
    "PlusJakartaSans": ("plusjakartasans/PlusJakartaSans%5Bwght%5D.ttf", "plusjakartasans/OFL.txt"),
    "Literata": ("literata/Literata%5Bopsz,wght%5D.ttf", "literata/OFL.txt"),
}
# (fonte, arquivo gerado em res/font, valores dos eixos)
INSTANCIAS = [
    ("PlusJakartaSans", "plus_jakarta_sans_regular.ttf", {"wght": 400}),
    ("PlusJakartaSans", "plus_jakarta_sans_semibold.ttf", {"wght": 600}),
    ("PlusJakartaSans", "plus_jakarta_sans_bold.ttf", {"wght": 700}),
    ("PlusJakartaSans", "plus_jakarta_sans_extrabold.ttf", {"wght": 800}),
    ("Literata", "literata_regular.ttf", {"wght": 400, "opsz": 12}),
    ("Literata", "literata_bold.ttf", {"wght": 700, "opsz": 12}),
]
# Latim, pontuação, sobrescritos, moedas, setas, operadores matemáticos e formas geométricas
FAIXAS = [(0x20, 0x7E), (0xA0, 0xFF), (0x100, 0x17F), (0x2000, 0x206F), (0x2070, 0x209F),
          (0x20A0, 0x20CF), (0x2190, 0x21FF), (0x2200, 0x22FF), (0x25A0, 0x25FF)]


def baixar(caminho):
    req = urllib.request.Request(GOOGLE_FONTS + caminho, headers={"User-Agent": "ENEMigo-escolar"})
    with urllib.request.urlopen(req, timeout=60) as r:
        return r.read()


def main():
    texto = QUESTOES.read_text(encoding="utf-8") + STRINGS.read_text(encoding="utf-8")
    unicodes = {ord(c) for c in texto}
    for a, b in FAIXAS:
        unicodes.update(range(a, b + 1))

    variaveis = {}
    LICENCAS.mkdir(parents=True, exist_ok=True)
    for nome, (fonte, licenca) in FONTES.items():
        variaveis[nome] = baixar(fonte)
        (LICENCAS / f"OFL-{nome}.txt").write_bytes(baixar(licenca))

    for nome, arquivo, eixos in INSTANCIAS:
        estatica = instancer.instantiateVariableFont(TTFont(io.BytesIO(variaveis[nome])), eixos,
                                                     updateFontNames=False)
        buf = io.BytesIO()
        estatica.save(buf)
        buf.seek(0)
        fonte = TTFont(buf)
        opcoes = subset.Options()
        opcoes.layout_features = ["*"]
        opcoes.name_IDs = ["*"]
        opcoes.notdef_outline = True
        recorte = subset.Subsetter(opcoes)
        recorte.populate(unicodes=unicodes)
        recorte.subset(fonte)
        destino = FONTES_RES / arquivo
        fonte.save(destino)
        print(f"{arquivo}: {destino.stat().st_size // 1024} KB")


if __name__ == "__main__":
    main()
