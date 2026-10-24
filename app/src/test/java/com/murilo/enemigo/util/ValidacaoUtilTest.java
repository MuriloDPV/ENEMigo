package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;

import com.murilo.enemigo.R;

import org.junit.Test;

public class ValidacaoUtilTest {

    @Test
    public void nomeValido() {
        assertEquals(0, ValidacaoUtil.validarNome("Ana"));
        assertEquals(0, ValidacaoUtil.validarNome("Ana Clara"));
    }

    @Test
    public void nomeComAcento_valido() {
        assertEquals(0, ValidacaoUtil.validarNome("Júlia Conceição"));
    }

    @Test
    public void espacosNasPontas_saoIgnorados() {
        assertEquals(0, ValidacaoUtil.validarNome("  Bia  "));
    }

    @Test
    public void nomeVazio() {
        assertEquals(R.string.erro_nome_vazio, ValidacaoUtil.validarNome(""));
        assertEquals(R.string.erro_nome_vazio, ValidacaoUtil.validarNome("   "));
        assertEquals(R.string.erro_nome_vazio, ValidacaoUtil.validarNome(null));
    }

    @Test
    public void nomeCurto() {
        assertEquals(R.string.erro_nome_curto, ValidacaoUtil.validarNome("A"));
    }

    @Test
    public void nomeLongo_maisDe20() {
        assertEquals(0, ValidacaoUtil.validarNome("Abcdefghij Abcdefghi"));
        assertEquals(R.string.erro_nome_longo, ValidacaoUtil.validarNome("Abcdefghij Abcdefghij"));
    }

    @Test
    public void nomeComNumeroOuSimbolo() {
        assertEquals(R.string.erro_nome_letras, ValidacaoUtil.validarNome("Ana1"));
        assertEquals(R.string.erro_nome_letras, ValidacaoUtil.validarNome("Ana!"));
    }
}
