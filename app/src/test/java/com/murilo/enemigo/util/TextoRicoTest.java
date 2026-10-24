package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class TextoRicoTest {

    @Test
    public void textoSemMarcadorViraUmTrechoSo() {
        List<TextoRico.Trecho> trechos = TextoRico.separar("Qual é a média?");
        assertEquals(1, trechos.size());
        assertEquals(TextoRico.Tipo.TEXTO, trechos.get(0).tipo);
        assertEquals("Qual é a média?", trechos.get(0).conteudo);
        assertFalse(TextoRico.temMarcador("Qual é a média?"));
    }

    @Test
    public void textoVazioOuNuloNaoTemTrechos() {
        assertTrue(TextoRico.separar("").isEmpty());
        assertTrue(TextoRico.separar(null).isEmpty());
        assertFalse(TextoRico.temMarcador(null));
        assertEquals("", TextoRico.semMarcadores(null));
    }

    @Test
    public void figuraEmLinhaPropriaFicaSozinhaNaLinha() {
        List<TextoRico.Trecho> trechos =
                TextoRico.separar("Observe a figura.\n\n[[imagem:2023-155-t1.png]]\n\nQual é a área?");
        assertEquals(3, trechos.size());
        assertEquals("Observe a figura.\n\n", trechos.get(0).conteudo);
        assertEquals(TextoRico.Tipo.IMAGEM, trechos.get(1).tipo);
        assertEquals("2023-155-t1.png", trechos.get(1).conteudo);
        assertTrue(trechos.get(1).sozinhaNaLinha);
        assertEquals("\n\nQual é a área?", trechos.get(2).conteudo);
    }

    @Test
    public void alternativaSoComFormula() {
        List<TextoRico.Trecho> trechos = TextoRico.separar("[[formula:2020-170-c.png]]");
        assertEquals(1, trechos.size());
        assertEquals(TextoRico.Tipo.FORMULA, trechos.get(0).tipo);
        assertEquals("2020-170-c.png", trechos.get(0).conteudo);
        assertTrue(trechos.get(0).sozinhaNaLinha);
    }

    @Test
    public void formulaNoMeioDoTextoNaoFicaSozinhaNaLinha() {
        List<TextoRico.Trecho> trechos = TextoRico.separar("sendo [[formula:i0.png]] a intensidade");
        assertEquals(3, trechos.size());
        assertEquals(TextoRico.Tipo.FORMULA, trechos.get(1).tipo);
        assertFalse(trechos.get(1).sozinhaNaLinha);
    }

    @Test
    public void duasFigurasSeguidas() {
        List<TextoRico.Trecho> trechos =
                TextoRico.separar("[[imagem:a.png]]\n[[imagem:b.png]]");
        assertEquals(3, trechos.size());
        assertTrue(trechos.get(0).sozinhaNaLinha);
        assertEquals("\n", trechos.get(1).conteudo);
        assertTrue(trechos.get(2).sozinhaNaLinha);
    }

    @Test
    public void marcadorIncompletoContinuaTexto() {
        assertFalse(TextoRico.temMarcador("[[imagem:a.png]"));
        assertFalse(TextoRico.temMarcador("[[video:a.mp4]]"));
        assertEquals(TextoRico.Tipo.TEXTO, TextoRico.separar("[[imagem:]]").get(0).tipo);
    }

    @Test
    public void temFiguraIgnoraFormulas() {
        assertTrue(TextoRico.temFigura("Veja: [[imagem:a.png]]"));
        assertFalse(TextoRico.temFigura("[[formula:a.png]]"));
        assertFalse(TextoRico.temFigura("só texto"));
    }

    @Test
    public void semMarcadoresTrocaPorPalavra() {
        assertEquals("Veja [figura] e [fórmula].",
                TextoRico.semMarcadores("Veja [[imagem:a.png]] e [[formula:b.png]]."));
        assertEquals("só texto", TextoRico.semMarcadores("só texto"));
    }
}
