package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.murilo.enemigo.model.ResultadoSessao;
import com.murilo.enemigo.util.Celebracoes.Celebracao;
import com.murilo.enemigo.util.Celebracoes.Tipo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CelebracoesTest {

    /** Lição 3/5 sem nada de especial. */
    private static ResultadoSessao licaoComum() {
        ResultadoSessao r = new ResultadoSessao();
        r.setModo(Constantes.MODO_LICAO);
        r.setAcertos(3);
        r.setTotal(5);
        r.setXpGanho(30);
        r.setOfensiva(2);
        r.setNivel(1);
        return r;
    }

    private static List<Celebracao> lista(Celebracao... celebracoes) {
        return Arrays.asList(celebracoes);
    }

    @Test
    public void licaoComum_semCelebracao() {
        assertTrue(Celebracoes.montar(licaoComum()).isEmpty());
    }

    @Test
    public void licaoPerfeita_comOBonus() {
        ResultadoSessao r = licaoComum();
        r.setAcertos(5);
        r.setBonus(5);
        assertEquals(lista(new Celebracao(Tipo.LICAO_PERFEITA, "5")), Celebracoes.montar(r));
    }

    @Test
    public void ofensivaAumentou_comOsDias() {
        ResultadoSessao r = licaoComum();
        r.setOfensiva(4);
        r.setOfensivaAumentou(true);
        assertEquals(lista(new Celebracao(Tipo.OFENSIVA, "4")), Celebracoes.montar(r));
    }

    @Test
    public void subiuDeNivel_comONivelNovo() {
        ResultadoSessao r = licaoComum();
        r.setNivel(3);
        r.setSubiuDeNivel(true);
        assertEquals(lista(new Celebracao(Tipo.NIVEL, "3")), Celebracoes.montar(r));
    }

    @Test
    public void tudoJunto_naOrdemPerfeitaOfensivaNivelConquistas() {
        ResultadoSessao r = licaoComum();
        r.setAcertos(5);
        r.setBonus(5);
        r.setOfensiva(1);
        r.setOfensivaAumentou(true);
        r.setNivel(2);
        r.setSubiuDeNivel(true);
        r.setNovasConquistas(new ArrayList<>(Arrays.asList("PRIMEIRA_LICAO", "LICAO_PERFEITA")));
        assertEquals(lista(
                new Celebracao(Tipo.LICAO_PERFEITA, "5"),
                new Celebracao(Tipo.OFENSIVA, "1"),
                new Celebracao(Tipo.NIVEL, "2"),
                new Celebracao(Tipo.CONQUISTA, "PRIMEIRA_LICAO"),
                new Celebracao(Tipo.CONQUISTA, "LICAO_PERFEITA")), Celebracoes.montar(r));
    }

    @Test
    public void revisao_nuncaEhLicaoPerfeita() {
        ResultadoSessao r = licaoComum();
        r.setModo(Constantes.MODO_REVISAO);
        r.setAcertos(5);
        r.setXpGanho(25);
        assertTrue(Celebracoes.montar(r).isEmpty());
    }

    @Test
    public void subiuDeNivel_contaPelaTrocaDeCentena() {
        assertTrue(Celebracoes.subiuDeNivel(95, 105));
        assertTrue(Celebracoes.subiuDeNivel(190, 245));
        assertFalse(Celebracoes.subiuDeNivel(100, 155));
        assertFalse(Celebracoes.subiuDeNivel(0, 99));
    }

    @Test
    public void texto_idaEVolta() {
        for (Celebracao c : lista(new Celebracao(Tipo.LICAO_PERFEITA, "5"),
                new Celebracao(Tipo.OFENSIVA, "12"), new Celebracao(Tipo.NIVEL, "7"),
                new Celebracao(Tipo.CONQUISTA, "XP_500"))) {
            assertEquals(c, Celebracao.deTexto(c.paraTexto()));
        }
    }

    @Test
    public void filaInteira_idaEVoltaPelaIntent() {
        List<Celebracao> fila = lista(new Celebracao(Tipo.OFENSIVA, "3"),
                new Celebracao(Tipo.CONQUISTA, "OFENSIVA_3"));
        assertEquals(Arrays.asList("OFENSIVA:3", "CONQUISTA:OFENSIVA_3"), Celebracoes.paraTextos(fila));
        assertEquals(fila, Celebracoes.deTextos(Celebracoes.paraTextos(fila)));
    }

    @Test
    public void texto_invalido_viraNull() {
        assertNull(Celebracao.deTexto(null));
        assertNull(Celebracao.deTexto(""));
        assertNull(Celebracao.deTexto("OFENSIVA"));
        assertNull(Celebracao.deTexto("OFENSIVA:"));
        assertNull(Celebracao.deTexto("FOGOS:3"));
    }

    @Test
    public void listaDeTextos_ignoraOsInvalidos() {
        List<String> textos = Arrays.asList("NIVEL:2", "lixo", "CONQUISTA:XP_100");
        assertEquals(lista(new Celebracao(Tipo.NIVEL, "2"), new Celebracao(Tipo.CONQUISTA, "XP_100")),
                Celebracoes.deTextos(textos));
        assertTrue(Celebracoes.deTextos(null).isEmpty());
    }

    @Test
    public void licaoPerfeita_vemAntesDoResultado_oRestoDepois() {
        List<Celebracao> todas = lista(
                new Celebracao(Tipo.LICAO_PERFEITA, "5"),
                new Celebracao(Tipo.OFENSIVA, "1"),
                new Celebracao(Tipo.CONQUISTA, "PRIMEIRA_LICAO"));
        assertEquals(lista(new Celebracao(Tipo.LICAO_PERFEITA, "5")), Celebracoes.antesDoResultado(todas));
        assertEquals(lista(new Celebracao(Tipo.OFENSIVA, "1"), new Celebracao(Tipo.CONQUISTA, "PRIMEIRA_LICAO")),
                Celebracoes.depoisDoResultado(todas));
    }
}
