package com.murilo.enemigo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê os marcadores de imagem que o tools/montar_questoes.py coloca no texto das questões:
 * [[imagem:arquivo.png]] para figuras e [[formula:arquivo.png]] para fórmulas.
 * Os arquivos ficam em assets/imagens/. Classe pura (sem Android) para poder ter testes.
 */
public final class TextoRico {

    /** Pasta dos arquivos dentro de assets. */
    public static final String PASTA_IMAGENS = "imagens/";

    /**
     * As fórmulas são renderizadas a 16 pt e 288 dpi, então 1 em (o tamanho da fonte) = 64 px.
     * O app escala a imagem para o em ficar do tamanho da fonte do TextView.
     */
    public static final float PX_POR_EM = 64f;

    /**
     * Nos números empilhados de uma fração o algarismo fica menor que no texto corrido, então a
     * fórmula é desenhada um pouco maior que a fonte para ficar fácil de ler no celular.
     */
    public static final float AUMENTO_FORMULA = 1.35f;

    private static final Pattern MARCADOR = Pattern.compile("\\[\\[(imagem|formula):([^\\]]+)]]");

    public enum Tipo { TEXTO, IMAGEM, FORMULA }

    /** Um pedaço do texto: texto comum, ou o nome do arquivo de uma imagem/fórmula. */
    public static final class Trecho {
        public final Tipo tipo;
        public final String conteudo;
        /** true se a imagem ocupa a linha inteira (figura que deve ficar centralizada). */
        public final boolean sozinhaNaLinha;

        Trecho(Tipo tipo, String conteudo, boolean sozinhaNaLinha) {
            this.tipo = tipo;
            this.conteudo = conteudo;
            this.sozinhaNaLinha = sozinhaNaLinha;
        }
    }

    private TextoRico() {
    }

    public static boolean temMarcador(String texto) {
        return texto != null && MARCADOR.matcher(texto).find();
    }

    /** true se o texto tem pelo menos uma figura (fórmulas não contam). */
    public static boolean temFigura(String texto) {
        for (Trecho t : separar(texto)) {
            if (t.tipo == Tipo.IMAGEM) {
                return true;
            }
        }
        return false;
    }

    /** Separa o texto em trechos, na ordem. Texto sem marcador vira um único trecho TEXTO. */
    public static List<Trecho> separar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return Collections.emptyList();
        }
        List<Trecho> trechos = new ArrayList<>();
        Matcher m = MARCADOR.matcher(texto);
        int fim = 0;
        while (m.find()) {
            if (m.start() > fim) {
                trechos.add(new Trecho(Tipo.TEXTO, texto.substring(fim, m.start()), false));
            }
            boolean inicioDaLinha = m.start() == 0 || texto.charAt(m.start() - 1) == '\n';
            boolean fimDaLinha = m.end() == texto.length() || texto.charAt(m.end()) == '\n';
            Tipo tipo = "imagem".equals(m.group(1)) ? Tipo.IMAGEM : Tipo.FORMULA;
            trechos.add(new Trecho(tipo, m.group(2), inicioDaLinha && fimDaLinha));
            fim = m.end();
        }
        if (fim < texto.length()) {
            trechos.add(new Trecho(Tipo.TEXTO, texto.substring(fim), false));
        }
        return trechos;
    }

    /** Versão só texto, para listas e lugares que não mostram imagem. */
    public static String semMarcadores(String texto) {
        if (texto == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Trecho t : separar(texto)) {
            if (t.tipo == Tipo.TEXTO) {
                sb.append(t.conteudo);
            } else {
                sb.append(t.tipo == Tipo.IMAGEM ? "[figura]" : "[fórmula]");
            }
        }
        return sb.toString();
    }
}
