"""Gera os VectorDrawables da Lia (a raposa mascote), do ícone do app e do logo.

A Lia é montada aqui com formas simples (elipses, curvas e traços) num quadro de 120x120 e
cada expressão sai num arquivo. Ícone e logo usam a mesma cabeça, então mexer no desenho
num lugar só atualiza tudo. Os desenhos só usam cores com nome (@color/mascote_*), que são
apelidos das cores da marca em values/colors.xml (o CoresDaMarcaTest confere).

Uso (da raiz do projeto):
    python tools/gerar_mascote.py

Saída (em app/src/main/res/drawable):
    mascote_neutro, mascote_feliz, mascote_comemorando, mascote_triste, mascote_pensando,
    mascote_sem_vidas, ic_launcher_foreground, ic_launcher_monocromatico e marca_logo (.xml)
"""
from pathlib import Path

DRAWABLE = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res" / "drawable"


# ---------- peças de VectorDrawable ----------

def num(v):
    """Número curto: 12.0 vira 12, 0.30000001 vira 0.3."""
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return "0" if s == "-0" else s


def elipse(cx, cy, rx, ry):
    return (f"M{num(cx - rx)},{num(cy)} a{num(rx)},{num(ry)} 0 1,0 {num(2 * rx)},0 "
            f"a{num(rx)},{num(ry)} 0 1,0 {num(-2 * rx)},0 Z")


def cor(nome):
    return f"@color/mascote_{nome}"


def preenche(d, nome, alfa=None, borda=None, largura=None, comentario=None):
    attrs = [("fillColor", cor(nome))]
    if alfa is not None:
        attrs.append(("fillAlpha", num(alfa)))
    attrs.append(("pathData", d))
    if borda:
        attrs += [("strokeColor", cor(borda)), ("strokeLineJoin", "round"),
                  ("strokeWidth", num(largura))]
    return {"tipo": "path", "attrs": attrs, "comentario": comentario}


def traco(d, nome, largura, alfa=None, comentario=None):
    attrs = [("pathData", d), ("strokeColor", cor(nome))]
    if alfa is not None:
        attrs.append(("strokeAlpha", num(alfa)))
    attrs += [("strokeLineCap", "round"), ("strokeLineJoin", "round"), ("strokeWidth", num(largura))]
    return {"tipo": "path", "attrs": attrs, "comentario": comentario}


def grupo(filhos, recorte=None, comentario=None, **transf):
    attrs = [(k, num(v)) for k, v in transf.items()]
    return {"tipo": "group", "attrs": attrs, "filhos": filhos, "recorte": recorte,
            "comentario": comentario}


def xml(pecas, nivel=1):
    linhas = []
    pad = "    " * nivel
    for p in pecas:
        if p.get("comentario"):
            linhas.append(f"{pad}<!-- {p['comentario']} -->")
        attrs = "".join(f"\n{pad}    android:{k}=\"{v}\"" for k, v in p["attrs"])
        if p["tipo"] == "path":
            linhas.append(f"{pad}<path{attrs} />")
        else:
            linhas.append(f"{pad}<group{attrs}>")
            if p["recorte"]:
                linhas.append(f"{pad}    <clip-path android:pathData=\"{p['recorte']}\" />")
            linhas.append(xml(p["filhos"], nivel + 1))
            linhas.append(f"{pad}</group>")
    return "\n".join(linhas)


def salvar(nome, descricao, pecas, tamanho=120, viewport=120):
    texto = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        f"<!-- {descricao}\n"
        "     Gerado por tools/gerar_mascote.py: mude lá e rode de novo. -->\n"
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'    android:width="{tamanho}dp"\n'
        f'    android:height="{tamanho}dp"\n'
        f'    android:viewportWidth="{viewport}"\n'
        f'    android:viewportHeight="{viewport}">\n'
        f"{xml(pecas)}\n"
        "</vector>\n"
    )
    (DRAWABLE / f"{nome}.xml").write_text(texto, encoding="utf-8", newline="\n")
    print("ok", nome)


# ---------- rosto ----------

OLHOS = (46, 74)
OLHOS_Y = 41
BOCHECHAS = (35, 85)
BOCHECHAS_Y = 53


def olhos(tipo):
    y = OLHOS_Y
    pecas = []
    for x in OLHOS:
        if tipo == "aberto":
            pecas += [preenche(elipse(x, y, 4.4, 5.2), "tinta"),
                      preenche(elipse(x + 1.6, y - 1.9, 1.6, 1.6), "brilho")]
        elif tipo == "feliz":
            pecas.append(traco(f"M{num(x - 4.6)},{num(y + 1.6)} Q{x},{num(y - 4.4)} {num(x + 4.6)},{num(y + 1.6)}", "tinta", 2.8))
        elif tipo == "fechado":
            pecas.append(traco(f"M{num(x - 4.4)},{y} Q{x},{num(y + 3.6)} {num(x + 4.4)},{y}", "tinta", 2.6))
        elif tipo == "triste":
            pecas += [preenche(elipse(x, y + 1, 3.96, 4.58), "tinta"),
                      preenche(elipse(x + 1.3, y - 0.6, 1.4, 1.4), "brilho")]
        elif tipo == "pensando":
            pecas += [preenche(elipse(x, y, 4.4, 5.2), "tinta"),
                      preenche(elipse(x - 0.4, y - 2.8, 1.7, 1.7), "brilho")]
    xl, xr = OLHOS
    if tipo == "triste":
        pecas += [traco(f"M{xl - 6},{y - 7} L{xl + 4},{num(y - 10.4)}", "tinta", 2.5),
                  traco(f"M{xr + 6},{y - 7} L{xr - 4},{num(y - 10.4)}", "tinta", 2.5)]
    elif tipo == "pensando":
        pecas += [traco(f"M{xl - 5},{y - 9} L{num(xl + 4.5)},{num(y - 9.4)}", "tinta", 2.5),
                  traco(f"M{xr - 5},{y - 10} Q{xr},{y - 14} {xr + 5},{y - 11}", "tinta", 2.5)]
    return pecas


BOCAS = {
    "sorriso": "M60,67 V69 M55.5,68.8 Q57.8,71.6 60,69 Q62.2,71.6 64.5,68.8",
    "sorrisao": "M60,67 V69 M54,68 Q57,73 60,69 Q63,73 66,68",
    "triste": "M60,67 V68.5 M55.5,72 Q60,67.8 64.5,72",
    "lado": "M60,67 V69 M57,71 Q61.5,71 65,69",
    "cansada": "M60,67 V69 M56.5,71 H63.5",
}

CABECA = ("M60,78 C52,78 44,70 36,62 C28,54 24,46 26,38 C28,28 40,24 60,24 "
          "C80,24 92,28 94,38 C96,46 92,54 84,62 C76,70 68,78 60,78 Z")
MASCARA = ("M29,47 C40,45 52,50 60,59 C68,50 80,45 91,47 C89,55 81,62 73,69 "
           "C69,73 64,76.5 60,76.5 C56,76.5 51,73 47,69 C39,62 31,55 29,47 Z")
ORELHAS = (("M30,40 L33,9 Q34,6 36.5,8 L56,28 Z", "M35.5,33 L36.8,15.5 L48.5,27.5 Z", 40),
           ("M90,40 L87,9 Q86,6 83.5,8 L64,28 Z", "M84.5,33 L83.2,15.5 L71.5,27.5 Z", 80))


def cabeca(tipo_olhos, boca, orelhas_caidas=False, lagrima=False):
    pecas = []
    for i, (fora, dentro, px) in enumerate(ORELHAS):
        orelha = [preenche(fora, "pelo", borda="pelo_sombra", largura=2.5),
                  preenche(dentro, "tinta", alfa=0.85)]
        if orelhas_caidas:
            pecas.append(grupo(orelha, rotation=-28 if i == 0 else 28, pivotX=px, pivotY=34))
        else:
            pecas += orelha
    pecas[0]["comentario"] = "Orelhas caídas, sem energia" if orelhas_caidas else "Orelhas"
    pecas += [
        preenche(CABECA, "pelo", borda="pelo_sombra", largura=2.5, comentario="Cabeça"),
        preenche(MASCARA, "mascara", comentario="Máscara clara do focinho"),
        preenche(elipse(BOCHECHAS[0], BOCHECHAS_Y, 4, 2.6), "bochecha", alfa=0.3),
        preenche(elipse(BOCHECHAS[1], BOCHECHAS_Y, 4, 2.6), "bochecha", alfa=0.3),
    ]
    rosto = olhos(tipo_olhos)
    rosto[0]["comentario"] = "Rosto"
    pecas += rosto
    pecas += [preenche(elipse(60, 63.5, 4.8, 3.5), "tinta"),
              preenche(elipse(58.4, 62.4, 1.4, 1), "brilho", alfa=0.6)]
    if boca == "aberta":
        pecas += [preenche("M53.5,67.8 Q60,69 66.5,67.8 Q65.5,76.5 60,76.5 Q54.5,76.5 53.5,67.8 Z", "tinta"),
                  preenche(elipse(60, 73.8, 3.8, 2), "lingua")]
    else:
        pecas.append(traco(BOCAS[boca], "tinta", 2.3 if boca == "sorrisao" else 2.1))
    if lagrima:
        pecas.append(lagrima_peca(39, 49))
    return pecas


def lagrima_peca(x, y):
    d = (f"M{x},{y} C{x},{y} {num(x + 3.2)},{num(y + 4.3)} {num(x + 3.2)},{num(y + 6.1)} "
         f"A3.2,3.2 0 0 1 {num(x - 3.2)},{num(y + 6.1)} C{num(x - 3.2)},{num(y + 4.3)} {x},{y} {x},{y} Z")
    p = preenche(d, "brilho", alfa=0.95, comentario="Lágrima")
    p["attrs"] += [("strokeAlpha", "0.7"), ("strokeColor", cor("lagrima")), ("strokeWidth", "1.2")]
    return p


# ---------- corpo ----------

CAUDA = ("M70,100 C92,106 108,92 104,72 C102,60 94,52 86,52 C92,62 92,74 84,84 "
         "C80,90 74,94 70,96 Z")
CAUDA_CAIDA = "M70,104 C84,109 100,109 110,103 C114,99 110,94 104,95 C94,97 82,98 72,96 Z"

BRACOS = {
    "baixo": ("M43,77 Q36,85 36,94", "M77,77 Q84,85 84,94"),
    "cima": ("M43,77 Q33,67 33,55", "M77,77 Q87,67 87,55"),
    "caido": ("M44,79 Q39,89 41,98", "M76,79 Q81,89 79,98"),
    "queixo": ("M43,77 Q36,85 36,94", "M77,79 Q87,75 72,71"),
}


def sombra_chao():
    return preenche(elipse(60, 112, 24, 4), "tinta", alfa=0.12, comentario="Sombra no chão")


def cauda(caida=False):
    d = CAUDA_CAIDA if caida else CAUDA
    if caida:
        ponta = [preenche(elipse(108, 99, 9, 9), "mascara")]
    else:
        ponta = [grupo([preenche(elipse(98, 56, 13, 9), "mascara")], rotation=30, pivotX=98, pivotY=56)]
    return [
        preenche(d, "pelo", comentario="Cauda caída no chão" if caida else "Cauda, com a ponta clara"),
        grupo(ponta, recorte=d),
        traco(d, "pelo_sombra", 2.5),
    ]


def braco(d):
    """Braço no tom do pelo com contorno: lê como pata mesmo pequeno e no modo escuro."""
    return [traco(d, "pelo_sombra", 7), traco(d, "pelo", 4)]


def corpo():
    return [
        preenche(elipse(51, 107.5, 6.5, 4.5), "pelo_sombra", comentario="Pés"),
        preenche(elipse(69, 107.5, 6.5, 4.5), "pelo_sombra"),
        preenche("M56,68 H64 A16,16 0 0 1 80,84 V91 A16,16 0 0 1 64,107 H56 A16,16 0 0 1 40,91 V84 A16,16 0 0 1 56,68 Z",
                 "pelo", borda="pelo_sombra", largura=2.5, comentario="Corpo, peito claro e lenço azul-tinta"),
        preenche(elipse(60, 91, 11, 13), "mascara"),
        preenche("M41,67 Q60,75 79,67 L79.5,73.5 Q60,81.5 40.5,73.5 Z", "lenco"),
        preenche("M63,74.5 L71,88 L59,84.5 Z", "lenco_sombra"),
    ]


def estrela(x, y, r):
    k = 0.22 * r
    d = (f"M{x},{num(y - r)} Q{num(x + k)},{num(y - k)} {num(x + r)},{y} Q{num(x + k)},{num(y + k)} {x},{num(y + r)} "
         f"Q{num(x - k)},{num(y + k)} {num(x - r)},{y} Q{num(x - k)},{num(y - k)} {x},{num(y - r)} Z")
    return preenche(d, "estrela", borda="estrela_borda", largura=1.2)


def brilhos():
    pecas = [estrela(14, 34, 7), estrela(106, 24, 6), estrela(108, 66, 4.5), estrela(12, 76, 4)]
    pecas[0]["comentario"] = "Brilhos da comemoração"
    return pecas


def pensamento():
    pecas = [preenche(elipse(94, 30, 2, 2), "pensamento", alfa=0.6),
             preenche(elipse(100, 21, 3, 3), "pensamento", alfa=0.6),
             preenche(elipse(108, 10, 4, 4), "pensamento", alfa=0.6)]
    pecas[0]["comentario"] = "Bolinhas de pensamento"
    return pecas


def zzz():
    pecas = [traco("M92,26 h5 l-5,5 h5", "pensamento", 1.8, alfa=0.8),
             traco("M100,14 h7 l-7,7 h7", "pensamento", 1.8, alfa=0.8)]
    pecas[0]["comentario"] = "Zzz"
    return pecas


EXPRESSOES = {
    # nome: (descrição, olhos, boca, braços, extras)
    "neutro": ("Lia neutra: olhos abertos, sorriso pequeno e braços para baixo.",
               "aberto", "sorriso", "baixo", None),
    "feliz": ("Lia feliz: olhos fechados de alegria e sorriso largo.",
              "feliz", "sorrisao", "baixo", None),
    "comemorando": ("Lia comemorando: braços para cima, boca aberta e brilhos em volta.",
                    "feliz", "aberta", "cima", brilhos),
    "triste": ("Lia triste: sobrancelhas caídas, boca para baixo e uma lágrima.",
               "triste", "triste", "caido", None),
    "pensando": ("Lia pensando: olhos para cima, sobrancelha erguida e a pata no queixo.",
                 "pensando", "lado", "queixo", pensamento),
}


def lia(nome):
    _, tipo_olhos, boca, pose, extras = EXPRESSOES[nome]
    tras, frente = BRACOS[pose]
    pecas = [sombra_chao()] + cauda()
    if pose == "queixo":
        pecas += braco(tras)
    else:
        pecas += braco(tras) + braco(frente)
    pecas[len(cauda()) + 1]["comentario"] = "Braços (atrás do corpo)"
    pecas += corpo()
    pecas += cabeca(tipo_olhos, boca, lagrima=(nome == "triste"))
    if pose == "queixo":
        pata = braco(frente)
        pata[0]["comentario"] = "Pata no queixo (na frente do rosto)"
        pecas += pata
        pecas.append(preenche(elipse(71.5, 71, 3.8, 3.8), "pelo", borda="pelo_sombra", largura=1.6))
    if extras:
        pecas += extras()
    return pecas


def lia_sem_vidas():
    """Sem vidas: orelhas caídas, olhos fechados e cauda no chão (o MascoteView ainda desbota)."""
    pecas = [sombra_chao()] + cauda(caida=True)
    pecas += braco(BRACOS["caido"][0]) + braco(BRACOS["caido"][1])
    pecas += corpo()
    pecas.append(grupo(cabeca("fechado", "cansada", orelhas_caidas=True),
                       comentario="Cabeça um pouco tombada",
                       rotation=6, pivotX=60, pivotY=70, translateY=3))
    pecas += zzz()
    return pecas


# ---------- ícone e logo ----------

# Cabeça feliz no quadro de 108 do ícone adaptativo: o ponto (60,40) da cabeça vai para o meio
# (54,54) com escala 0,78. Assim até a ponta das orelhas cabe na zona segura (círculo de 66dp).
ICONE = dict(pivotX=60, pivotY=40, scaleX=0.78, scaleY=0.78, translateX=-6, translateY=14)


def cabeca_icone():
    return grupo(cabeca("feliz", "sorrisao"), comentario="Cabeça feliz da Lia, na zona segura", **ICONE)


def inverter(segmentos):
    """Inverte um caminho de segmentos absolutos [(x0,y0), ('C', c1, c2, p) | ('L', p) | ('Q', c, p)]."""
    inicio, resto = segmentos[0], segmentos[1:]
    pontos = [inicio] + [s[-1] for s in resto]
    saida = [pontos[-1]]
    for i in range(len(resto) - 1, -1, -1):
        s, destino = resto[i], pontos[i]
        if s[0] == "C":
            saida.append(("C", s[2], s[1], destino))
        elif s[0] == "Q":
            saida.append(("Q", s[1], destino))
        else:
            saida.append(("L", destino))
    return saida


def caminho(segmentos):
    p = lambda xy: f"{num(xy[0])},{num(xy[1])}"
    d = f"M{p(segmentos[0])}"
    for s in segmentos[1:]:
        d += f" {s[0]}" + " ".join(p(xy) for xy in s[1:])
    return d + " Z"


MASCARA_SEG = [(29, 47), ("C", (40, 45), (52, 50), (60, 59)), ("C", (68, 50), (80, 45), (91, 47)),
               ("C", (89, 55), (81, 62), (73, 69)), ("C", (69, 73), (64, 76.5), (60, 76.5)),
               ("C", (56, 76.5), (51, 73), (47, 69)), ("C", (39, 62), (31, 55), (29, 47))]


def monocromatico():
    """Silhueta para o ícone temático do Android 13+: só o alfa conta.
    Cabeça e orelhas cheias, máscara a meia-tinta, e olhos, nariz e boca vazados
    (os furos são desenhados no sentido contrário, então a regra nonZero os deixa vazios)."""
    def olho(x, y=OLHOS_Y):
        # crescente do olho feliz, no sentido anti-horário (furo)
        return (f"M{num(x - 3.5)},{num(y + 1.3)} Q{x},{num(y - 2.6)} {num(x + 3.5)},{num(y + 1.3)} "
                f"L{num(x + 5.9)},{num(y + 2.1)} Q{x},{num(y - 6.4)} {num(x - 5.9)},{num(y + 2.1)} Z")
    orelha_dir = "M90,40 L64,28 L83.5,8 Q86,6 87,9 Z"  # mesmo sentido (horário) da cabeça
    furo_mascara = caminho(inverter(MASCARA_SEG))
    nariz = elipse(60, 63.5, 4.8, 3.5)  # arcos com sweep 0: anti-horário
    boca = "M53.6,67.6 Q60,76 66.4,67.6 L64.6,68.6 Q60,73.2 55.4,68.6 Z"
    cheio = " ".join([ORELHAS[0][0], orelha_dir, CABECA, furo_mascara, olho(OLHOS[0]), olho(OLHOS[1])])
    meia = " ".join([caminho(MASCARA_SEG), nariz, boca])
    branco = "@color/texto_sobre_cor"
    return [grupo([
        {"tipo": "path", "attrs": [("fillColor", branco), ("pathData", cheio)], "comentario": "Cabeça e orelhas, com olhos e máscara vazados"},
        {"tipo": "path", "attrs": [("fillColor", branco), ("fillAlpha", "0.55"), ("pathData", meia)], "comentario": "Máscara a meia-tinta, com nariz e boca vazados"},
    ], **ICONE)]


def logo():
    fundo = {"tipo": "path", "attrs": [("fillColor", "@color/marca_primaria"),
                                       ("pathData", "M12,0 H36 A12,12 0 0 1 48,12 V36 A12,12 0 0 1 36,48 H12 A12,12 0 0 1 0,36 V12 A12,12 0 0 1 12,0 Z")]}
    # 0,6667 e -12 levam a parte visível do ícone (18 a 90 dos 108) para o quadro de 48.
    return [fundo, grupo([cabeca_icone()], scaleX=0.6667, scaleY=0.6667, translateX=-12, translateY=-12)]


def main():
    for nome, (descricao, *_resto) in EXPRESSOES.items():
        salvar(f"mascote_{nome}", descricao + " Cores: mascote_* em values/colors.xml.", lia(nome))
    salvar("mascote_sem_vidas", "Lia sem vidas: orelhas caídas, olhos fechados e cauda no chão. "
           "O MascoteView ainda a deixa desbotada.", lia_sem_vidas())
    salvar("ic_launcher_foreground", "Ícone: a cabeça feliz da Lia (o fundo azul-tinta fica no adaptive-icon). "
           "A abertura usa este mesmo desenho.", [cabeca_icone()], tamanho=108, viewport=108)
    salvar("ic_launcher_monocromatico", "Ícone temático (Android 13+): silhueta da cabeça da Lia.",
           monocromatico(), tamanho=108, viewport=108)
    salvar("marca_logo", "Logo da marca dentro do app: a cabeça do ícone num quadrado arredondado.",
           logo(), tamanho=48, viewport=48)


if __name__ == "__main__":
    main()
