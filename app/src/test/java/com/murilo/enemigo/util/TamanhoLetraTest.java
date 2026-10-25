package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TamanhoLetraTest {

    private static final float DELTA = 0.0001f;

    @Test
    public void media_eOPadraoEMantemOTamanhoDoLayout() {
        assertEquals(TamanhoLetra.MEDIA, TamanhoLetra.PADRAO);
        assertEquals(1f, TamanhoLetra.escala(TamanhoLetra.MEDIA), DELTA);
        assertEquals(16f, TamanhoLetra.tamanho(16f, TamanhoLetra.MEDIA), DELTA);
    }

    @Test
    public void pequena_eGrande_mudamOTamanho() {
        assertEquals(14f, TamanhoLetra.tamanho(16f, TamanhoLetra.PEQUENA), DELTA);
        assertEquals(20f, TamanhoLetra.tamanho(16f, TamanhoLetra.GRANDE), DELTA);
    }

    @Test
    public void cadaNivel_eMaiorQueOAnterior() {
        assertTrue(TamanhoLetra.escala(TamanhoLetra.PEQUENA) < TamanhoLetra.escala(TamanhoLetra.MEDIA));
        assertTrue(TamanhoLetra.escala(TamanhoLetra.MEDIA) < TamanhoLetra.escala(TamanhoLetra.GRANDE));
    }

    @Test
    public void nivelInvalido_voltaParaOPadrao() {
        assertEquals(TamanhoLetra.PADRAO, TamanhoLetra.limitar(-1));
        assertEquals(TamanhoLetra.PADRAO, TamanhoLetra.limitar(3));
        assertEquals(TamanhoLetra.GRANDE, TamanhoLetra.limitar(TamanhoLetra.GRANDE));
        assertEquals(1f, TamanhoLetra.escala(99), DELTA);
    }
}
