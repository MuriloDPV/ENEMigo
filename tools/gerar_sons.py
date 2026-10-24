"""Gera os sons curtos do app (acerto, erro e lição concluída) por síntese.

Os sons são feitos aqui mesmo, nota por nota, só com a biblioteca padrão do Python: nada é
baixado da internet e não há licença de terceiros. O resultado é sempre o mesmo arquivo.

Uso (da raiz do projeto):
    python tools/gerar_sons.py

Saída: app/src/main/res/raw/som_acerto.wav, som_erro.wav e som_licao.wav
(WAV mono, 16 bits, 22.050 Hz).
"""
import math
import struct
import wave
from pathlib import Path

TAXA = 22050
PASTA = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res" / "raw"


def nota(freq, duracao, decaimento, harmonicos=((1, 1.0),), ataque=0.004, curva=0.0):
    """Uma nota: soma de senos (harmônicos) com ataque curto e queda exponencial.

    curva: quanto a afinação cai até o fim da nota (0.03 = 3%), para o "bonk" do erro.
    """
    amostras = []
    fase = [0.0] * len(harmonicos)
    total = int(TAXA * duracao)
    soltura = 0.015  # o fim de cada nota desce até zero, para não estalar
    for i in range(total):
        t = i / TAXA
        f = freq * (1 - curva * t / duracao)
        envelope = (min(1.0, t / ataque) * math.exp(-t / decaimento)
                    * min(1.0, (duracao - t) / soltura))
        valor = 0.0
        for k, (multiplo, peso) in enumerate(harmonicos):
            fase[k] += 2 * math.pi * f * multiplo / TAXA
            valor += peso * math.sin(fase[k])
        amostras.append(valor * envelope)
    return amostras


def misturar(trilha, amostras, inicio):
    """Soma as amostras na trilha a partir de 'inicio' segundos."""
    pos = int(inicio * TAXA)
    falta = pos + len(amostras) - len(trilha)
    if falta > 0:
        trilha.extend([0.0] * falta)
    for i, valor in enumerate(amostras):
        trilha[pos + i] += valor


def salvar(nome, trilha, pico=0.7):
    """Normaliza (pico de -3 dB), suaviza o fim para não estalar e grava o WAV."""
    maior = max(abs(v) for v in trilha) or 1.0
    fim = int(0.02 * TAXA)
    dados = bytearray()
    for i, valor in enumerate(trilha):
        if i >= len(trilha) - fim:
            valor *= (len(trilha) - i) / fim
        dados += struct.pack("<h", int(round(valor / maior * pico * 32767)))
    PASTA.mkdir(parents=True, exist_ok=True)
    with wave.open(str(PASTA / nome), "wb") as arquivo:
        arquivo.setnchannels(1)
        arquivo.setsampwidth(2)
        arquivo.setframerate(TAXA)
        arquivo.writeframes(bytes(dados))
    print("ok", nome, "%.2f s" % (len(trilha) / TAXA))


# Timbres: marimba (fundamental + harmônico 4, que some rápido) e sino (harmônicos 2 e 3 suaves)
MARIMBA = ((1, 1.0), (4, 0.18))
SINO = ((1, 1.0), (2, 0.25), (3, 0.08))
TRIANGULAR = ((1, 1.0), (3, 0.11), (5, 0.04))


def som_acerto():
    """Dois toques de marimba subindo uma quinta (Lá5 e Mi6): curto e alegre."""
    trilha = []
    misturar(trilha, nota(880.0, 0.20, 0.08, MARIMBA), 0.0)
    misturar(trilha, nota(1318.5, 0.30, 0.11, MARIMBA), 0.075)
    salvar("som_acerto.wav", trilha)


def som_erro():
    """Dois "bonks" graves descendo (Mi bemol 4 e Si bemol 3), com a afinação caindo um pouco."""
    trilha = []
    misturar(trilha, nota(311.1, 0.16, 0.07, TRIANGULAR, ataque=0.006, curva=0.04), 0.0)
    misturar(trilha, nota(233.1, 0.30, 0.12, TRIANGULAR, ataque=0.006, curva=0.05), 0.12)
    salvar("som_erro.wav", trilha)


def som_licao():
    """Arpejo de Dó maior em sino (Dó5, Mi5, Sol5, Dó6) e um acorde que fica soando no fim."""
    trilha = []
    for i, freq in enumerate((523.25, 659.25, 783.99)):
        misturar(trilha, nota(freq, 0.35, 0.16, SINO), i * 0.09)
    misturar(trilha, nota(1046.5, 0.80, 0.32, SINO), 0.27)
    for freq in (1318.5, 1568.0):
        misturar(trilha, [v * 0.35 for v in nota(freq, 0.70, 0.30, SINO)], 0.30)
    salvar("som_licao.wav", trilha)


if __name__ == "__main__":
    som_acerto()
    som_erro()
    som_licao()
