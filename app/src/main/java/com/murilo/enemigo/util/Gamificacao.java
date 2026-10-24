package com.murilo.enemigo.util;

import com.murilo.enemigo.model.EstadoLicao;

import java.time.LocalDate;

/**
 * Regras do jogo como funções puras: recebem valores (inclusive data e hora) e devolvem
 * resultados. Sem Context e sem SharedPreferences, para dar para testar com JUnit.
 */
public final class Gamificacao {

    private static final long MS_POR_MINUTO = 60_000L;
    private static final long MS_RECARGA = Constantes.MINUTOS_RECARGA * MS_POR_MINUTO;

    private Gamificacao() {
    }

    // ---------------------------------------------------------------- XP, nível e meta

    /** RN17: nível = xp / 100 + 1. */
    public static int calcularNivel(int xp) {
        return Math.max(0, xp) / Constantes.XP_POR_NIVEL + 1;
    }

    /** RN17: XP dentro do nível atual (0 a 99). */
    public static int calcularXpNoNivel(int xp) {
        return Math.max(0, xp) % Constantes.XP_POR_NIVEL;
    }

    /** RN14: +10 por acerto, sem o bônus. */
    public static int calcularXpLicao(int acertos) {
        return Math.max(0, acertos) * Constantes.XP_ACERTO;
    }

    /** RN14: +5 só com todas certas. */
    public static int calcularBonus(int acertos, int total) {
        return total > 0 && acertos == total ? Constantes.XP_BONUS_PERFEITA : 0;
    }

    /** RN15: +5 por acerto na revisão. */
    public static int calcularXpRevisao(int acertos) {
        return Math.max(0, acertos) * Constantes.XP_ACERTO_REVISAO;
    }

    /** RN18: o XP de hoje só vale se foi ganho hoje. */
    public static int calcularXpHoje(LocalDate dataXpHoje, LocalDate hoje, int xpHoje) {
        return hoje.equals(dataXpHoje) ? Math.max(0, xpHoje) : 0;
    }

    /** RN19: a meta foi atingida nesta sessão (e não antes). */
    public static boolean metaBatidaAgora(int xpHojeAntes, int xpHojeDepois, int meta) {
        return xpHojeAntes < meta && xpHojeDepois >= meta;
    }

    /** RN02: só 20, 50 ou 100. */
    public static boolean isMetaValida(int meta) {
        return meta == Constantes.META_CASUAL
                || meta == Constantes.META_REGULAR
                || meta == Constantes.META_INTENSO;
    }

    /** Taxa de acerto em % (arredondada), ou -1 se nada foi respondido. */
    public static int calcularTaxaAcerto(int acertos, int respondidas) {
        if (respondidas <= 0) {
            return -1;
        }
        return Math.round(acertos * 100f / respondidas);
    }

    // ---------------------------------------------------------------- Trilha e desbloqueio

    /** RN06: acertos que concluem uma lição de {@code total} questões (60%, arredondado para cima). */
    public static int acertosParaConcluir(int total) {
        return (Math.max(0, total) * Constantes.PERCENTUAL_PARA_CONCLUIR + 99) / 100;
    }

    /** RN06: {@code acertos} de {@code total} conclui a lição? (-1 = nunca feita, não conclui) */
    public static boolean concluiu(int acertos, int total) {
        return total > 0 && acertos >= acertosParaConcluir(total);
    }

    /**
     * RN05/RN06.
     *
     * @param melhor         melhor resultado desta lição (-1 = nunca feita)
     * @param total          questões desta lição
     * @param melhorAnterior melhor resultado da lição anterior (ignorado se ordem == 0)
     * @param totalAnterior  questões da lição anterior
     */
    public static EstadoLicao calcularEstado(int ordem, int melhor, int total,
                                             int melhorAnterior, int totalAnterior) {
        if (concluiu(melhor, total)) {
            return EstadoLicao.CONCLUIDA;
        }
        if (ordem == 0 || concluiu(melhorAnterior, totalAnterior)) {
            return EstadoLicao.DISPONIVEL;
        }
        return EstadoLicao.BLOQUEADA;
    }

    /** RN07: o melhor só sobe. */
    public static int calcularNovoMelhor(int melhorAnterior, int acertos) {
        return Math.max(melhorAnterior, acertos);
    }

    /** RN09: liberou a próxima lição pela primeira vez nesta sessão. */
    public static boolean desbloqueouProxima(int melhorAnterior, int acertos, int total,
                                             boolean existeSeguinte) {
        return existeSeguinte && !concluiu(melhorAnterior, total) && concluiu(acertos, total);
    }

    // ---------------------------------------------------------------- Ofensiva

    /**
     * RN27: ofensiva depois de terminar uma sessão hoje.
     * Data futura (relógio do celular voltou) não altera a ofensiva.
     */
    public static int calcularNovaOfensiva(LocalDate ultima, LocalDate hoje, int ofensivaAtual) {
        if (ultima == null) {
            return 1;
        }
        if (ultima.equals(hoje) || ultima.isAfter(hoje)) {
            return Math.max(1, ofensivaAtual);
        }
        if (ultima.plusDays(1).equals(hoje)) {
            return ofensivaAtual + 1;
        }
        return 1;
    }

    /**
     * Data de estudo a salvar depois da sessão. Nunca volta no tempo: se o relógio
     * foi atrasado, mantém a data mais recente para a ofensiva não zerar depois.
     */
    public static LocalDate calcularNovaDataEstudo(LocalDate ultima, LocalDate hoje) {
        if (ultima != null && ultima.isAfter(hoje)) {
            return ultima;
        }
        return hoje;
    }

    /**
     * RN28: ofensiva mostrada na tela. Zera se o aluno pulou um dia
     * (última data antes de ontem). Data futura mantém o valor.
     */
    public static int ofensivaParaExibir(LocalDate ultima, LocalDate hoje, int ofensiva) {
        if (ultima == null) {
            return 0;
        }
        if (ultima.isBefore(hoje.minusDays(1))) {
            return 0;
        }
        return Math.max(0, ofensiva);
    }

    /** RN29. */
    public static boolean estudouHoje(LocalDate ultima, LocalDate hoje) {
        return hoje.equals(ultima);
    }

    // ---------------------------------------------------------------- Vidas

    /** Vidas e o momento de referência da recarga. Imutável. */
    public static final class EstadoVidas {
        public final int vidas;
        public final long ultimaRecarga;

        public EstadoVidas(int vidas, long ultimaRecarga) {
            this.vidas = vidas;
            this.ultimaRecarga = ultimaRecarga;
        }
    }

    /**
     * RN20/RN22: aplica a recuperação pelo tempo. Com 5 vidas o relógio fica parado.
     * Se o relógio do celular voltou (ultimaRecarga no futuro), recomeça a contar de agora.
     */
    public static EstadoVidas recarregarVidas(int vidas, long ultimaRecarga, long agora) {
        int v = limitarVidas(vidas);
        if (v >= Constantes.VIDAS_MAX) {
            return new EstadoVidas(Constantes.VIDAS_MAX, ultimaRecarga);
        }
        long referencia = ultimaRecarga > agora ? agora : ultimaRecarga;
        long ganhas = (agora - referencia) / MS_RECARGA;
        if (ganhas > 0) {
            v = (int) Math.min(Constantes.VIDAS_MAX, v + ganhas);
            referencia += ganhas * MS_RECARGA;
        }
        return new EstadoVidas(v, referencia);
    }

    /** RN11/RN21: perde 1 vida (mín. 0). Se estava com 5, o relógio começa agora. */
    public static EstadoVidas perderVida(int vidas, long ultimaRecarga, long agora) {
        EstadoVidas atual = recarregarVidas(vidas, ultimaRecarga, agora);
        long referencia = atual.vidas >= Constantes.VIDAS_MAX ? agora : atual.ultimaRecarga;
        return new EstadoVidas(Math.max(0, atual.vidas - 1), referencia);
    }

    /** RN25: ganha 1 vida (máx. 5). */
    public static EstadoVidas ganharVida(int vidas, long ultimaRecarga, long agora) {
        EstadoVidas atual = recarregarVidas(vidas, ultimaRecarga, agora);
        return new EstadoVidas(Math.min(Constantes.VIDAS_MAX, atual.vidas + 1), atual.ultimaRecarga);
    }

    /** RN23: minutos que faltam para a próxima vida (use depois de recarregarVidas). */
    public static int minutosParaProximaVida(long ultimaRecarga, long agora) {
        if (ultimaRecarga > agora) {
            return Constantes.MINUTOS_RECARGA;
        }
        long passouMinutos = (agora - ultimaRecarga) / MS_POR_MINUTO;
        return (int) (Constantes.MINUTOS_RECARGA - passouMinutos % Constantes.MINUTOS_RECARGA);
    }

    private static int limitarVidas(int vidas) {
        return Math.max(0, Math.min(Constantes.VIDAS_MAX, vidas));
    }
}
