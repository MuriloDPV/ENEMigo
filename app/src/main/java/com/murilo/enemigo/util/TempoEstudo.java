package com.murilo.enemigo.util;

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
}
