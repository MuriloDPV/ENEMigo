package com.murilo.enemigo.util;

/**
 * Chaves, extras e números do jogo. Mudar uma regra = mudar um número aqui.
 */
public final class Constantes {

    private Constantes() {
    }

    // SharedPreferences
    public static final String PREFS_NOME = "enemigo_prefs";

    // Chaves do SharedPreferences (seção 9.5)
    public static final String CHAVE_NOME = "nome";
    public static final String CHAVE_META_DIARIA = "meta_diaria";
    public static final String CHAVE_ONBOARDING_OK = "onboarding_ok";
    public static final String CHAVE_XP_TOTAL = "xp_total";
    public static final String CHAVE_XP_HOJE = "xp_hoje";
    public static final String CHAVE_DATA_XP_HOJE = "data_xp_hoje";
    public static final String CHAVE_OFENSIVA = "ofensiva";
    public static final String CHAVE_MAIOR_OFENSIVA = "maior_ofensiva";
    public static final String CHAVE_ULTIMA_DATA_ESTUDO = "ultima_data_estudo";
    public static final String CHAVE_VIDAS = "vidas";
    public static final String CHAVE_ULTIMA_RECARGA_VIDA = "ultima_recarga_vida";
    public static final String PREFIXO_MELHOR = "melhor_";
    public static final String CHAVE_CADERNO_ERROS = "caderno_erros";
    public static final String CHAVE_CONQUISTAS = "conquistas";
    public static final String CHAVE_LICOES_PERFEITAS = "licoes_perfeitas";
    public static final String CHAVE_TOTAL_RESPONDIDAS = "total_respondidas";
    public static final String CHAVE_TOTAL_ACERTOS = "total_acertos";
    public static final String CHAVE_ULTIMA_AREA = "ultima_area";
    public static final String CHAVE_VIBRACAO = "vibracao";

    // Extras das Intents
    public static final String EXTRA_AREA_ID = "EXTRA_AREA_ID";
    public static final String EXTRA_LICAO_ID = "EXTRA_LICAO_ID";
    public static final String EXTRA_MODO = "EXTRA_MODO";
    public static final String EXTRA_ACERTOS = "EXTRA_ACERTOS";
    public static final String EXTRA_TOTAL = "EXTRA_TOTAL";
    public static final String EXTRA_XP = "EXTRA_XP";
    public static final String EXTRA_BONUS = "EXTRA_BONUS";
    public static final String EXTRA_OFENSIVA = "EXTRA_OFENSIVA";
    public static final String EXTRA_META_BATIDA = "EXTRA_META_BATIDA";
    public static final String EXTRA_DESBLOQUEOU = "EXTRA_DESBLOQUEOU";
    public static final String EXTRA_VIDAS_RECUPERADAS = "EXTRA_VIDAS_RECUPERADAS";
    public static final String EXTRA_CELEBRACOES = "EXTRA_CELEBRACOES";
    public static final String EXTRA_ABRIR_REVISAO = "ABRIR_REVISAO";

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
    public static final int QUESTOES_POR_REVISAO = 5;
    public static final int XP_POR_NIVEL = 100;

    // Metas diárias aceitas (RN02)
    public static final int META_CASUAL = 20;
    public static final int META_REGULAR = 50;
    public static final int META_INTENSO = 100;
    public static final int META_PADRAO = META_REGULAR;

    // Nome do aluno
    public static final int NOME_MIN = 2;
    public static final int NOME_MAX = 20;
}
