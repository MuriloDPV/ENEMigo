package com.murilo.enemigo.util;

/**
 * Chaves, extras e números do jogo. Mudar uma regra = mudar um número aqui.
 */
public final class Constantes {

    private Constantes() {
    }

    // SharedPreferences
    public static final String PREFS_NOME = "enemigo_prefs";

    // Extras das Intents
    public static final String EXTRA_AREA_ID = "EXTRA_AREA_ID";
    public static final String EXTRA_LICAO_ID = "EXTRA_LICAO_ID";
    public static final String EXTRA_MODO = "EXTRA_MODO";

    // Modos do Quiz
    public static final String MODO_LICAO = "LICAO";
    public static final String MODO_REVISAO = "REVISAO";

    // Regras do jogo
    public static final int XP_ACERTO = 10;
    public static final int XP_BONUS_PERFEITA = 5;
    public static final int XP_ACERTO_REVISAO = 5;
    public static final int VIDAS_MAX = 5;
    public static final int MINUTOS_RECARGA = 30;
    public static final int ACERTOS_PARA_DESBLOQUEAR = 3;
    public static final int QUESTOES_POR_LICAO = 5;
    public static final int XP_POR_NIVEL = 100;
}
