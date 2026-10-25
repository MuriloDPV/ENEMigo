package com.murilo.enemigo.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Regras do lembrete diário da ofensiva como funções puras (testadas com JUnit).
 * O agendamento e a notificação em si ficam no pacote notificacao.
 */
public final class Lembrete {

    private static final int MINUTOS_POR_DIA = 24 * 60;

    private Lembrete() {
    }

    /**
     * Notificar agora? Só com o lembrete ligado, depois do primeiro acesso, se o aluno ainda
     * não estudou hoje e no próprio dia: a partir do horário e com até
     * {@link Constantes#LEMBRETE_TOLERANCIA_HORAS} de atraso (nada de aviso de madrugada).
     *
     * @param ultimaDataEstudo último dia com sessão concluída (null = nunca)
     * @param horario          minutos desde a meia-noite
     */
    public static boolean deveNotificar(boolean ativo, boolean onboardingConcluido,
                                        LocalDate ultimaDataEstudo, LocalDateTime agora, int horario) {
        if (!ativo || !onboardingConcluido) {
            return false;
        }
        // Data no futuro = o relógio voltou; a ofensiva não está em risco (RN27).
        if (ultimaDataEstudo != null && !ultimaDataEstudo.isBefore(agora.toLocalDate())) {
            return false;
        }
        LocalTime alvo = paraHora(horario);
        LocalTime hora = agora.toLocalTime();
        return !hora.isBefore(alvo)
                && Duration.between(alvo, hora).toHours() < Constantes.LEMBRETE_TOLERANCIA_HORAS;
    }

    /** Milissegundos até o próximo horário do lembrete, sempre depois de agora (hoje ou amanhã). */
    public static long msAteProximo(ZonedDateTime agora, int horario) {
        ZonedDateTime proximo = agora.with(paraHora(horario));
        if (!proximo.isAfter(agora)) {
            proximo = agora.plusDays(1).with(paraHora(horario));
        }
        return Duration.between(agora, proximo).toMillis();
    }

    /** Uma frase por dia, em rodízio: dias seguidos nunca repetem a mesma. */
    public static int indiceFrase(LocalDate hoje, int quantidade) {
        return (int) Math.floorMod(hoje.toEpochDay(), (long) quantidade);
    }

    /** Horário salvo fora da faixa de um dia volta para o padrão. */
    public static int limitarHorario(int horario) {
        return horario >= 0 && horario < MINUTOS_POR_DIA ? horario : Constantes.LEMBRETE_HORARIO_PADRAO;
    }

    public static int horario(int hora, int minuto) {
        return hora * 60 + minuto;
    }

    /** Minutos desde a meia-noite viram hora do dia (19 * 60 = 19:00). */
    public static LocalTime paraHora(int horario) {
        int minutos = limitarHorario(horario);
        return LocalTime.of(minutos / 60, minutos % 60);
    }
}
