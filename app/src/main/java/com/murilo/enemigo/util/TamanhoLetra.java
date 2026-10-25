package com.murilo.enemigo.util;

/**
 * Os 3 tamanhos de letra das questões (texto-base, enunciado, alternativas e explicação),
 * escolhidos no quiz ou nas Configurações. Cada nível multiplica o tamanho que está no layout,
 * então o "médio" é exatamente o de antes. Classe pura, testada em TamanhoLetraTest.
 */
public final class TamanhoLetra {

    public static final int PEQUENA = 0;
    public static final int MEDIA = 1;
    public static final int GRANDE = 2;
    public static final int PADRAO = MEDIA;

    // 16sp do texto-base vira 14, 16 ou 20sp.
    private static final float[] ESCALAS = {0.875f, 1f, 1.25f};

    private TamanhoLetra() {
    }

    /** Nível salvo fora de 0 a 2 (ex.: preferência corrompida) volta para o padrão. */
    public static int limitar(int nivel) {
        return nivel < PEQUENA || nivel > GRANDE ? PADRAO : nivel;
    }

    public static float escala(int nivel) {
        return ESCALAS[limitar(nivel)];
    }

    /** Tamanho do layout (em qualquer unidade) já no nível escolhido. */
    public static float tamanho(float tamanhoDoLayout, int nivel) {
        return tamanhoDoLayout * escala(nivel);
    }
}
