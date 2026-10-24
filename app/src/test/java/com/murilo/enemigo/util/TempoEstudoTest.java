package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TempoEstudoTest {

    @Test
    public void minutosEstimados_arredondaENuncaZera() {
        assertEquals(4, TempoEstudo.minutosEstimados(233));  // 3 min 53 s
        assertEquals(5, TempoEstudo.minutosEstimados(308));  // 5 min 8 s
        assertEquals(4, TempoEstudo.minutosEstimados(240));
        assertEquals(1, TempoEstudo.minutosEstimados(10));
    }
}
