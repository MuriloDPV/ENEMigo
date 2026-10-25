package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LembreteTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 9, 24);
    private static final LocalDate ONTEM = HOJE.minusDays(1);
    private static final int AS_19H = Lembrete.horario(19, 0);
    private static final long HORA_MS = 3_600_000L;
    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private static LocalDateTime hojeAs(int hora, int minuto) {
        return HOJE.atTime(hora, minuto);
    }

    private static boolean notifica(LocalDate ultimaDataEstudo, LocalDateTime agora) {
        return Lembrete.deveNotificar(true, true, ultimaDataEstudo, agora, AS_19H);
    }

    // ------------------------------------------------------------ Notificar ou não

    @Test
    public void naoEstudouHoje_noHorario_notifica() {
        assertTrue(notifica(ONTEM, hojeAs(19, 0)));
    }

    @Test
    public void nuncaEstudou_noHorario_notifica() {
        assertTrue(notifica(null, hojeAs(19, 5)));
    }

    @Test
    public void ofensivaJaPerdida_aindaNotifica() {
        assertTrue(notifica(HOJE.minusDays(5), hojeAs(19, 0)));
    }

    @Test
    public void estudouHoje_naoNotifica() {
        assertFalse(notifica(HOJE, hojeAs(19, 0)));
    }

    @Test
    public void relogioVoltou_dataDeEstudoNoFuturo_naoNotifica() {
        assertFalse(notifica(HOJE.plusDays(1), hojeAs(19, 0)));
    }

    @Test
    public void lembreteDesligado_naoNotifica() {
        assertFalse(Lembrete.deveNotificar(false, true, ONTEM, hojeAs(19, 0), AS_19H));
    }

    @Test
    public void antesDoPrimeiroAcesso_naoNotifica() {
        assertFalse(Lembrete.deveNotificar(true, false, null, hojeAs(19, 0), AS_19H));
    }

    @Test
    public void antesDoHorario_naoNotifica() {
        assertFalse(notifica(ONTEM, hojeAs(18, 59)));
    }

    @Test
    public void atrasadoDentroDaTolerancia_notifica() {
        assertTrue(notifica(ONTEM, hojeAs(21, 59)));
    }

    @Test
    public void atrasadoAlemDaTolerancia_naoNotifica() {
        assertFalse(notifica(ONTEM, hojeAs(22, 0)));
    }

    @Test
    public void madrugadaDoDiaSeguinte_naoNotifica() {
        // Lembrete das 23h que só rodou depois da meia-noite: já é outro dia.
        int as23h = Lembrete.horario(23, 0);
        assertFalse(Lembrete.deveNotificar(true, true, ONTEM, HOJE.plusDays(1).atTime(0, 30), as23h));
    }

    @Test
    public void horarioTarde_toleranciaNaoPassaDaMeiaNoite() {
        int as23h = Lembrete.horario(23, 0);
        assertTrue(Lembrete.deveNotificar(true, true, ONTEM, hojeAs(23, 59), as23h));
    }

    // ------------------------------------------------------------ Próximo disparo

    @Test
    public void proximo_antesDoHorario_ehHoje() {
        ZonedDateTime agora = hojeAs(18, 0).atZone(SAO_PAULO);
        assertEquals(HORA_MS, Lembrete.msAteProximo(agora, AS_19H));
    }

    @Test
    public void proximo_exatamenteNoHorario_ehAmanha() {
        ZonedDateTime agora = hojeAs(19, 0).atZone(SAO_PAULO);
        assertEquals(24 * HORA_MS, Lembrete.msAteProximo(agora, AS_19H));
    }

    @Test
    public void proximo_depoisDoHorario_ehAmanha() {
        ZonedDateTime agora = hojeAs(20, 30).atZone(SAO_PAULO);
        assertEquals(22 * HORA_MS + HORA_MS / 2, Lembrete.msAteProximo(agora, AS_19H));
    }

    @Test
    public void proximo_ignoraSegundosQuebrados() {
        ZonedDateTime agora = hojeAs(18, 59).plusSeconds(30).atZone(SAO_PAULO);
        assertEquals(30_000L, Lembrete.msAteProximo(agora, AS_19H));
    }

    // ------------------------------------------------------------ Frases e horário

    @Test
    public void frase_diasSeguidosNuncaRepetem() {
        for (int i = 0; i < 30; i++) {
            LocalDate dia = HOJE.plusDays(i);
            assertNotEquals(Lembrete.indiceFrase(dia, 6), Lembrete.indiceFrase(dia.plusDays(1), 6));
        }
    }

    @Test
    public void frase_ficaDentroDaLista() {
        for (int i = -10; i < 10; i++) {
            int indice = Lembrete.indiceFrase(LocalDate.ofEpochDay(i), 6);
            assertTrue(indice >= 0 && indice < 6);
        }
    }

    @Test
    public void horario_foraDaFaixa_voltaParaOPadrao() {
        assertEquals(Constantes.LEMBRETE_HORARIO_PADRAO, Lembrete.limitarHorario(-1));
        assertEquals(Constantes.LEMBRETE_HORARIO_PADRAO, Lembrete.limitarHorario(24 * 60));
        assertEquals(Lembrete.horario(7, 30), Lembrete.limitarHorario(Lembrete.horario(7, 30)));
    }

    @Test
    public void horario_paraHora() {
        assertEquals("19:00", Lembrete.paraHora(AS_19H).toString());
        assertEquals("07:05", Lembrete.paraHora(Lembrete.horario(7, 5)).toString());
    }
}
