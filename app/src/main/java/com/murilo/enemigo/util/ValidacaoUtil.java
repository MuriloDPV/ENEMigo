package com.murilo.enemigo.util;

import com.murilo.enemigo.R;

import java.util.regex.Pattern;

/**
 * Validação do nome do aluno, usada no Onboarding e nas Configurações (testada em ValidacaoUtilTest).
 */
public final class ValidacaoUtil {

    /** Só letras (inclusive acentuadas) e espaços. */
    private static final Pattern SO_LETRAS = Pattern.compile("^[\\p{L} ]+$");

    private ValidacaoUtil() {
    }

    /**
     * Devolve o id da mensagem de erro, ou 0 se o nome for válido. Os espaços nas pontas não
     * contam (o nome é salvo sem eles).
     */
    public static int validarNome(String nome) {
        String limpo = nome == null ? "" : nome.trim();
        if (limpo.isEmpty()) {
            return R.string.erro_nome_vazio;
        }
        if (limpo.length() < Constantes.NOME_MIN) {
            return R.string.erro_nome_curto;
        }
        if (limpo.length() > Constantes.NOME_MAX) {
            return R.string.erro_nome_longo;
        }
        if (!SO_LETRAS.matcher(limpo).matches()) {
            return R.string.erro_nome_letras;
        }
        return 0;
    }
}
