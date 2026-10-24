package com.murilo.enemigo.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * As figuras do ENEM são desenhadas com "multiplicar" e as fórmulas têm tinta escura. Por isso o
 * cartão de papel do texto-base e as alternativas com figura/fórmula precisam ficar claros também
 * no modo escuro. Este teste lê as paletas (values e values-night) e confere isso, para ninguém
 * escurecer o papel sem querer ao trocar a marca.
 */
public class PapelModoEscuroTest {

    private static final Path RES = Paths.get("src", "main", "res");
    private static final String[] FUNDOS_PAPEL = {"papel", "papel_selecao", "papel_acerto", "papel_erro"};
    private static final Pattern COR = Pattern.compile("<color name=\"([a-z_]+)\">#([0-9A-Fa-f]{6})</color>");

    @Test
    public void papelFicaClaroNosDoisModos() throws IOException {
        for (String pasta : new String[]{"values", "values-night"}) {
            Map<String, Integer> cores = lerCores(pasta);
            for (String nome : FUNDOS_PAPEL) {
                Integer cor = cores.get(nome);
                assertNotNull(pasta + ": falta a cor " + nome, cor);
                assertTrue(pasta + ": " + nome + " escuro demais para a tinta das figuras",
                        luminancia(cor) >= 0.7);
                double contraste = contraste(cores.get("papel_texto"), cor);
                assertTrue(pasta + ": papel_texto sobre " + nome + " com contraste " + contraste,
                        contraste >= 7);
            }
        }
    }

    @Test
    public void textoBaseUsaOCartaoDePapel() throws IOException {
        String quiz = ler(RES.resolve("layout/activity_quiz.xml"));
        int card = quiz.indexOf("@+id/card_texto_base");
        assertTrue("activity_quiz sem card_texto_base", card >= 0);
        String trecho = quiz.substring(card, quiz.indexOf('>', card));
        assertTrue("o texto-base precisa do fundo @drawable/bg_papel", trecho.contains("@drawable/bg_papel"));
        assertTrue("bg_papel precisa usar @color/papel",
                ler(RES.resolve("drawable/bg_papel.xml")).contains("@color/papel\""));
    }

    private static Map<String, Integer> lerCores(String pasta) throws IOException {
        Map<String, Integer> cores = new HashMap<>();
        Matcher m = COR.matcher(ler(RES.resolve(pasta).resolve("colors.xml")));
        while (m.find()) {
            cores.put(m.group(1), Integer.parseInt(m.group(2), 16));
        }
        return cores;
    }

    private static String ler(Path arquivo) throws IOException {
        return new String(Files.readAllBytes(arquivo), StandardCharsets.UTF_8);
    }

    /** Luminância relativa (WCAG), de 0 (preto) a 1 (branco). */
    private static double luminancia(int rgb) {
        double r = canal((rgb >> 16) & 0xFF);
        double g = canal((rgb >> 8) & 0xFF);
        double b = canal(rgb & 0xFF);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double canal(int valor) {
        double c = valor / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    private static double contraste(int a, int b) {
        double la = luminancia(a);
        double lb = luminancia(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }
}
