package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

public class TempoEstudoTest {

    private static final long SEGUNDO = 1000L;
    private static final long MINUTO = 60 * SEGUNDO;
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 24);

    @Test
    public void minutosEstimados_arredondaENuncaZera() {
        assertEquals(4, TempoEstudo.minutosEstimados(233));  // 3 min 53 s
        assertEquals(5, TempoEstudo.minutosEstimados(308));  // 5 min 8 s
        assertEquals(4, TempoEstudo.minutosEstimados(240));
        assertEquals(1, TempoEstudo.minutosEstimados(10));
    }

    @Test
    public void minutosEstimados_noRitmoDoAluno() {
        assertEquals(4, TempoEstudo.minutosEstimados(240, 1.0));
        assertEquals(6, TempoEstudo.minutosEstimados(240, 1.5));
        assertEquals(2, TempoEstudo.minutosEstimados(240, 0.5));
    }

    @Test
    public void tempoValido_ateDezMinutos() {
        assertTrue(TempoEstudo.isTempoValido(45 * SEGUNDO));
        assertTrue(TempoEstudo.isTempoValido(10 * MINUTO));
        assertFalse(TempoEstudo.isTempoValido(10 * MINUTO + 1));
        assertFalse(TempoEstudo.isTempoValido(0));
        assertFalse(TempoEstudo.isTempoValido(-5 * SEGUNDO));
    }

    @Test
    public void media_emSegundos_ouMenosUmSemDados() {
        assertEquals(-1, TempoEstudo.mediaSegundos(0, 0));
        assertEquals(75, TempoEstudo.mediaSegundos(150 * SEGUNDO, 2));
        assertEquals(68, TempoEstudo.mediaSegundos(135 * SEGUNDO, 2)); // 67,5 arredonda para cima
    }

    @Test
    public void ritmo_soDepoisDeDezQuestoes() {
        // 9 questões no dobro do tempo: ainda vale a estimativa.
        assertEquals(1.0, TempoEstudo.fatorRitmo(9 * 120 * SEGUNDO, 9 * 60, 9), 1e-9);
        assertEquals(2.0, TempoEstudo.fatorRitmo(10 * 120 * SEGUNDO, 10 * 60, 10), 1e-9);
        assertEquals(0.8, TempoEstudo.fatorRitmo(10 * 48 * SEGUNDO, 10 * 60, 10), 1e-9);
    }

    @Test
    public void ritmo_limitadoEntreMeioEDois() {
        assertEquals(2.0, TempoEstudo.fatorRitmo(10 * 600 * SEGUNDO, 10 * 60, 10), 1e-9);
        assertEquals(0.5, TempoEstudo.fatorRitmo(10 * 5 * SEGUNDO, 10 * 60, 10), 1e-9);
        assertEquals(1.0, TempoEstudo.fatorRitmo(10 * 60 * SEGUNDO, 0, 10), 1e-9);
    }

    @Test
    public void tempoHoje_zeraQuandoMudaODia() {
        assertEquals(5 * MINUTO, TempoEstudo.tempoHojeMs(HOJE, HOJE, 5 * MINUTO));
        assertEquals(0, TempoEstudo.tempoHojeMs(HOJE.minusDays(1), HOJE, 5 * MINUTO));
        assertEquals(0, TempoEstudo.tempoHojeMs(null, HOJE, 5 * MINUTO));
    }

    @Test
    public void minutos_arredondados() {
        assertEquals(0, TempoEstudo.minutos(20 * SEGUNDO));
        assertEquals(1, TempoEstudo.minutos(40 * SEGUNDO));
        assertEquals(12, TempoEstudo.minutos(12 * MINUTO + 10 * SEGUNDO));
    }
}
