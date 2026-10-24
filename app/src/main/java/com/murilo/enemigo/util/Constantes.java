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
    public static final String CHAVE_SOM = "som";
    public static final String CHAVE_AVATAR = "avatar";
    /** Versão do formato do progresso salvo: abaixo de VERSAO_PROGRESSO, roda a migração (RN37). */
    public static final String CHAVE_VERSAO_PROGRESSO = "versao_progresso";
    public static final int VERSAO_PROGRESSO = 2;
    // Tempo real das questões (RN38): somas, para não guardar uma lista que cresce sem fim
    public static final String CHAVE_TEMPO_TOTAL_MS = "tempo_total_ms";
    public static final String CHAVE_TEMPO_QUESTOES = "tempo_questoes";
    public static final String CHAVE_TEMPO_ESTIMADO_S = "tempo_estimado_s";
    public static final String CHAVE_TEMPO_HOJE_MS = "tempo_hoje_ms";
    public static final String CHAVE_DATA_TEMPO_HOJE = "data_tempo_hoje";

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
    /** % de acertos que conclui a lição e libera a próxima (arredondado para cima: 2 de 3, 3 de 5). */
    public static final int PERCENTUAL_PARA_CONCLUIR = 60;
    public static final int MIN_QUESTOES_POR_LICAO = 3;
    public static final int MAX_QUESTOES_POR_LICAO = 5;
    public static final int QUESTOES_POR_REVISAO = 5;
    public static final int XP_POR_NIVEL = 100;

    // Tempo real (RN38/RN39)
    /** Acima disso o aluno largou a questão: o tempo não é registrado. */
    public static final long TEMPO_MAX_QUESTAO_MS = 10 * 60_000L;
    /** Questões medidas antes de a trilha ajustar a estimativa ao ritmo do aluno. */
    public static final int QUESTOES_PARA_RITMO = 10;
    public static final double FATOR_RITMO_MIN = 0.5;
    public static final double FATOR_RITMO_MAX = 2.0;

    // Metas diárias aceitas (RN02)
    public static final int META_CASUAL = 20;
    public static final int META_REGULAR = 50;
    public static final int META_INTENSO = 100;
    public static final int META_PADRAO = META_REGULAR;

    // Nome do aluno
    public static final int NOME_MIN = 2;
    public static final int NOME_MAX = 20;
}
