package com.murilo.enemigo.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Datas guardadas como texto ISO ("2026-09-24"). Sem nada do Android.
 */
public final class DataUtil {

    private DataUtil() {
    }

    public static LocalDate hoje() {
        return LocalDate.now();
    }

    public static LocalDate ontem() {
        return hoje().minusDays(1);
    }

    /** "" se a data for null. */
    public static String paraTexto(LocalDate data) {
        return data == null ? "" : data.toString();
    }

    /** null se o texto for vazio ou inválido. */
    public static LocalDate deTexto(String texto) {
        if (texto == null || texto.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
