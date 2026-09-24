package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.murilo.enemigo.model.EstadoLicao;

import org.junit.Test;

import java.time.LocalDate;

public class GamificacaoTest {

    private static final long MINUTO = 60_000L;
    private static final long RECARGA = Constantes.MINUTOS_RECARGA * MINUTO;
    private static final long AGORA = 1_800_000_000_000L;
    private static final LocalDate HOJE = LocalDate.of(2026, 9, 24);

    // ------------------------------------------------------------ Ofensiva

    @Test
    public void ofensiva_primeiraSessao_viraUm() {
        assertEquals(1, Gamificacao.calcularNovaOfensiva(null, HOJE, 0));
    }

    @Test
    public void ofensiva_mesmoDia_naoMuda() {
        assertEquals(4, Gamificacao.calcularNovaOfensiva(HOJE, HOJE, 4));
    }

    @Test
    public void ofensiva_diaSeguinte_somaUm() {
        assertEquals(5, Gamificacao.calcularNovaOfensiva(HOJE.minusDays(1), HOJE, 4));
    }

    @Test
    public void ofensiva_pulouUmDia_recomecaEmUm() {
        assertEquals(1, Gamificacao.calcularNovaOfensiva(HOJE.minusDays(2), HOJE, 4));
    }

    @Test
    public void ofensiva_dataDoCelularVoltou_naoMuda() {
        // Último estudo "amanhã" = o relógio foi atrasado.
        assertEquals(4, Gamificacao.calcularNovaOfensiva(HOJE.plusDays(1), HOJE, 4));
    }

    @Test
    public void ofensiva_dataDoCelularVoltou_dataDeEstudoNaoRetrocede() {
        LocalDate amanha = HOJE.plusDays(1);
        assertEquals(amanha, Gamificacao.calcularNovaDataEstudo(amanha, HOJE));
        assertEquals(HOJE, Gamificacao.calcularNovaDataEstudo(HOJE.minusDays(1), HOJE));
        assertEquals(HOJE, Gamificacao.calcularNovaDataEstudo(null, HOJE));
    }

    @Test
    public void ofensivaExibida_hojeOuOntem_mantem() {
        assertEquals(3, Gamificacao.ofensivaParaExibir(HOJE, HOJE, 3));
        assertEquals(3, Gamificacao.ofensivaParaExibir(HOJE.minusDays(1), HOJE, 3));
    }

    @Test
    public void ofensivaExibida_pulouUmDia_zera() {
        assertEquals(0, Gamificacao.ofensivaParaExibir(HOJE.minusDays(2), HOJE, 3));
        assertEquals(0, Gamificacao.ofensivaParaExibir(null, HOJE, 3));
    }

    @Test
    public void ofensivaExibida_dataFutura_mantem() {
        assertEquals(3, Gamificacao.ofensivaParaExibir(HOJE.plusDays(3), HOJE, 3));
    }

    @Test
    public void estudouHoje_soNoMesmoDia() {
        assertTrue(Gamificacao.estudouHoje(HOJE, HOJE));
        assertFalse(Gamificacao.estudouHoje(HOJE.minusDays(1), HOJE));
        assertFalse(Gamificacao.estudouHoje(null, HOJE));
    }

    // ------------------------------------------------------------ Vidas

    @Test
    public void vidas_menosDeUmIntervalo_naoRecarrega() {
        Gamificacao.EstadoVidas e = Gamificacao.recarregarVidas(3, AGORA - 29 * MINUTO, AGORA);
        assertEquals(3, e.vidas);
        assertEquals(AGORA - 29 * MINUTO, e.ultimaRecarga);
    }

    @Test
    public void vidas_umIntervalo_recarregaUma() {
        Gamificacao.EstadoVidas e = Gamificacao.recarregarVidas(3, AGORA - RECARGA, AGORA);
        assertEquals(4, e.vidas);
        assertEquals(AGORA, e.ultimaRecarga);
    }

    @Test
    public void vidas_variosIntervalos_acumulaEGuardaOResto() {
        // 95 min com 30 min por vida = 3 vidas e sobram 5 min para a próxima.
        Gamificacao.EstadoVidas e = Gamificacao.recarregarVidas(1, AGORA - 95 * MINUTO, AGORA);
        assertEquals(4, e.vidas);
        assertEquals(AGORA - 5 * MINUTO, e.ultimaRecarga);
        assertEquals(25, Gamificacao.minutosParaProximaVida(e.ultimaRecarga, AGORA));
    }

    @Test
    public void vidas_recargaNaoPassaDeCinco() {
        Gamificacao.EstadoVidas e = Gamificacao.recarregarVidas(2, AGORA - 10 * RECARGA, AGORA);
        assertEquals(Constantes.VIDAS_MAX, e.vidas);
    }

    @Test
    public void vidas_comCinco_relogioParado() {
        Gamificacao.EstadoVidas e = Gamificacao.recarregarVidas(5, 0L, AGORA);
        assertEquals(5, e.vidas);
        assertEquals(0L, e.ultimaRecarga);
    }

    @Test
    public void vidas_valorEstranhoSalvo_limitadoEntreZeroECinco() {
        assertEquals(5, Gamificacao.recarregarVidas(9, AGORA, AGORA).vidas);
        assertEquals(0, Gamificacao.recarregarVidas(-3, AGORA, AGORA).vidas);
    }

    @Test
    public void vidas_relogioAtrasado_naoGanhaNemPerde() {
        Gamificacao.EstadoVidas e = Gamificacao.recarregarVidas(2, AGORA + 5 * RECARGA, AGORA);
        assertEquals(2, e.vidas);
        assertEquals(AGORA, e.ultimaRecarga);
    }

    @Test
    public void perderVida_comCinco_iniciaRelogio() {
        Gamificacao.EstadoVidas e = Gamificacao.perderVida(5, 0L, AGORA);
        assertEquals(4, e.vidas);
        assertEquals(AGORA, e.ultimaRecarga);
    }

    @Test
    public void perderVida_comMenosDeCinco_mantemRelogio() {
        long inicio = AGORA - 10 * MINUTO;
        Gamificacao.EstadoVidas e = Gamificacao.perderVida(3, inicio, AGORA);
        assertEquals(2, e.vidas);
        assertEquals(inicio, e.ultimaRecarga);
    }

    @Test
    public void perderVida_naoFicaNegativa() {
        assertEquals(0, Gamificacao.perderVida(0, AGORA, AGORA).vidas);
    }

    @Test
    public void ganharVida_naoPassaDeCinco() {
        assertEquals(5, Gamificacao.ganharVida(5, 0L, AGORA).vidas);
        assertEquals(4, Gamificacao.ganharVida(3, AGORA, AGORA).vidas);
    }

    @Test
    public void minutosParaProximaVida_contaRegressiva() {
        assertEquals(30, Gamificacao.minutosParaProximaVida(AGORA, AGORA));
        assertEquals(18, Gamificacao.minutosParaProximaVida(AGORA - 12 * MINUTO, AGORA));
        assertEquals(30, Gamificacao.minutosParaProximaVida(AGORA + MINUTO, AGORA));
    }

    // ------------------------------------------------------------ Desbloqueio

    @Test
    public void desbloqueio_tresDeCinco_liberaProxima() {
        assertEquals(EstadoLicao.DISPONIVEL, Gamificacao.calcularEstado(1, -1, 3));
        assertTrue(Gamificacao.desbloqueouProxima(-1, 3, true));
    }

    @Test
    public void desbloqueio_doisDeCinco_naoLibera() {
        assertEquals(EstadoLicao.BLOQUEADA, Gamificacao.calcularEstado(1, -1, 2));
        assertFalse(Gamificacao.desbloqueouProxima(-1, 2, true));
    }

    @Test
    public void desbloqueio_primeiraLicao_sempreDisponivel() {
        assertEquals(EstadoLicao.DISPONIVEL, Gamificacao.calcularEstado(0, -1, -1));
        assertEquals(EstadoLicao.DISPONIVEL, Gamificacao.calcularEstado(0, 2, -1));
    }

    @Test
    public void desbloqueio_melhorTresOuMais_concluida() {
        assertEquals(EstadoLicao.CONCLUIDA, Gamificacao.calcularEstado(0, 3, -1));
        assertEquals(EstadoLicao.CONCLUIDA, Gamificacao.calcularEstado(2, 5, 4));
    }

    @Test
    public void desbloqueio_jaLiberadaAntes_naoContaDeNovo() {
        assertFalse(Gamificacao.desbloqueouProxima(4, 5, true));
    }

    @Test
    public void desbloqueio_ultimaLicao_naoTemProxima() {
        assertFalse(Gamificacao.desbloqueouProxima(-1, 5, false));
    }

    @Test
    public void melhor_soSobe() {
        assertEquals(4, Gamificacao.calcularNovoMelhor(4, 2));
        assertEquals(5, Gamificacao.calcularNovoMelhor(4, 5));
        assertEquals(0, Gamificacao.calcularNovoMelhor(-1, 0));
    }

    // ------------------------------------------------------------ XP e nível

    @Test
    public void xp_semBonus() {
        assertEquals(40, Gamificacao.calcularXpLicao(4));
        assertEquals(0, Gamificacao.calcularBonus(4, 5));
        assertEquals(0, Gamificacao.calcularXpLicao(0));
    }

    @Test
    public void xp_comBonusNaLicaoPerfeita() {
        int total = Gamificacao.calcularXpLicao(5) + Gamificacao.calcularBonus(5, 5);
        assertEquals(55, total);
    }

    @Test
    public void xp_revisaoValeCincoPorAcerto() {
        assertEquals(15, Gamificacao.calcularXpRevisao(3));
    }

    @Test
    public void nivel_aCadaCemXp() {
        assertEquals(1, Gamificacao.calcularNivel(0));
        assertEquals(1, Gamificacao.calcularNivel(99));
        assertEquals(2, Gamificacao.calcularNivel(100));
        assertEquals(4, Gamificacao.calcularNivel(335));
        assertEquals(35, Gamificacao.calcularXpNoNivel(335));
    }

    @Test
    public void xpHoje_zeraQuandoMudaODia() {
        assertEquals(30, Gamificacao.calcularXpHoje(HOJE, HOJE, 30));
        assertEquals(0, Gamificacao.calcularXpHoje(HOJE.minusDays(1), HOJE, 30));
        assertEquals(0, Gamificacao.calcularXpHoje(null, HOJE, 30));
    }

    @Test
    public void meta_batidaSoNaSessaoQueCruza() {
        assertTrue(Gamificacao.metaBatidaAgora(40, 50, 50));
        assertFalse(Gamificacao.metaBatidaAgora(50, 90, 50));
        assertFalse(Gamificacao.metaBatidaAgora(10, 40, 50));
    }

    @Test
    public void meta_soAceitaVinteCinquentaECem() {
        assertTrue(Gamificacao.isMetaValida(20));
        assertTrue(Gamificacao.isMetaValida(50));
        assertTrue(Gamificacao.isMetaValida(100));
        assertFalse(Gamificacao.isMetaValida(37));
    }

    @Test
    public void taxaDeAcerto() {
        assertEquals(-1, Gamificacao.calcularTaxaAcerto(0, 0));
        assertEquals(67, Gamificacao.calcularTaxaAcerto(2, 3));
    }

    // ------------------------------------------------------------ DataUtil

    @Test
    public void dataUtil_textoIso() {
        assertEquals("2026-09-24", DataUtil.paraTexto(HOJE));
        assertEquals(HOJE, DataUtil.deTexto("2026-09-24"));
        assertEquals(null, DataUtil.deTexto(""));
        assertEquals(null, DataUtil.deTexto("24/09/2026"));
    }
}
