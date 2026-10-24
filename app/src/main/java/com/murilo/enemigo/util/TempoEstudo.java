package com.murilo.enemigo.util;

import java.time.LocalDate;

/**
 * Tempo estimado das lições e tempo real das questões, como funções puras (TempoEstudoTest).
 * A estimativa de cada questão vem pronta do questoes.json ("segundos_estimados").
 */
public final class TempoEstudo {

    private TempoEstudo() {
    }

    /** Minutos para mostrar na trilha ("~4 min"): arredondado, nunca menos de 1. */
    public static int minutosEstimados(int segundos) {
        return Math.max(1, Math.round(segundos / 60f));
    }

    /** RN37: a estimativa no ritmo do aluno (fator 1 = a estimativa do JSON). */
    public static int minutosEstimados(int segundos, double fatorRitmo) {
        return minutosEstimados((int) Math.round(segundos * fatorRitmo));
    }

    /** RN36: só vale tempo positivo e de até 10 min; mais que isso o aluno largou a questão. */
    public static boolean isTempoValido(long ms) {
        return ms > 0 && ms <= Constantes.TEMPO_MAX_QUESTAO_MS;
    }

    /** Tempo médio por questão em segundos (arredondado), ou -1 se nada foi medido. */
    public static int mediaSegundos(long totalMs, int questoes) {
        if (questoes <= 0) {
            return -1;
        }
        return Math.round(totalMs / 1000f / questoes);
    }

    /**
     * RN37: quanto o aluno é mais lento (> 1) ou mais rápido (< 1) que a estimativa, entre 0,5 e 2.
     * Vale 1 até ele ter {@link Constantes#QUESTOES_PARA_RITMO} questões medidas.
     *
     * @param totalMs   tempo real somado das questões medidas
     * @param estimadoS estimativa somada das mesmas questões
     */
    public static double fatorRitmo(long totalMs, long estimadoS, int questoes) {
        if (questoes < Constantes.QUESTOES_PARA_RITMO || estimadoS <= 0) {
            return 1.0;
        }
        double fator = totalMs / 1000.0 / estimadoS;
        return Math.max(Constantes.FATOR_RITMO_MIN, Math.min(Constantes.FATOR_RITMO_MAX, fator));
    }

    /** O tempo de hoje só vale se foi somado hoje (como o XP de hoje). */
    public static long tempoHojeMs(LocalDate data, LocalDate hoje, long ms) {
        return hoje.equals(data) ? Math.max(0L, ms) : 0L;
    }

    /** Milissegundos -> minutos inteiros, arredondado. */
    public static int minutos(long ms) {
        return Math.round(ms / 60_000f);
    }
}
