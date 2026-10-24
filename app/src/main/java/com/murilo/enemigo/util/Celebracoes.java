package com.murilo.enemigo.util;

import com.murilo.enemigo.model.ResultadoSessao;

import java.util.ArrayList;
import java.util.List;

/**
 * Fila das telas de celebração do fim da sessão: lição perfeita, ofensiva aumentada, nível novo
 * e cada conquista nova, nessa ordem. Funções puras (testadas em CelebracoesTest).
 * A fila vai de uma tela para outra na Intent, como textos "TIPO:valor".
 */
public final class Celebracoes {

    private Celebracoes() {
    }

    public enum Tipo {
        LICAO_PERFEITA, OFENSIVA, NIVEL, CONQUISTA
    }

    /** Uma tela de celebração. O valor é o bônus, os dias, o nível ou o id da conquista. */
    public static final class Celebracao {

        public final Tipo tipo;
        public final String valor;

        public Celebracao(Tipo tipo, String valor) {
            this.tipo = tipo;
            this.valor = valor;
        }

        public String paraTexto() {
            return tipo.name() + ":" + valor;
        }

        /** O contrário de paraTexto; null se o texto não for válido. */
        public static Celebracao deTexto(String texto) {
            if (texto == null) {
                return null;
            }
            int separador = texto.indexOf(':');
            if (separador <= 0 || separador == texto.length() - 1) {
                return null;
            }
            for (Tipo tipo : Tipo.values()) {
                if (tipo.name().equals(texto.substring(0, separador))) {
                    return new Celebracao(tipo, texto.substring(separador + 1));
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object outro) {
            if (!(outro instanceof Celebracao)) {
                return false;
            }
            Celebracao c = (Celebracao) outro;
            return tipo == c.tipo && valor.equals(c.valor);
        }

        @Override
        public int hashCode() {
            return 31 * tipo.hashCode() + valor.hashCode();
        }

        @Override
        public String toString() {
            return paraTexto();
        }
    }

    /** As celebrações de uma sessão terminada, na ordem em que aparecem. */
    public static List<Celebracao> montar(ResultadoSessao resultado) {
        List<Celebracao> fila = new ArrayList<>();
        // O bônus só existe na lição com todas certas (RN14); na revisão é sempre 0.
        if (Constantes.MODO_LICAO.equals(resultado.getModo()) && resultado.getBonus() > 0) {
            fila.add(new Celebracao(Tipo.LICAO_PERFEITA, String.valueOf(resultado.getBonus())));
        }
        if (resultado.isOfensivaAumentou()) {
            fila.add(new Celebracao(Tipo.OFENSIVA, String.valueOf(resultado.getOfensiva())));
        }
        if (resultado.isSubiuDeNivel()) {
            fila.add(new Celebracao(Tipo.NIVEL, String.valueOf(resultado.getNivel())));
        }
        for (String id : resultado.getNovasConquistas()) {
            fila.add(new Celebracao(Tipo.CONQUISTA, id));
        }
        return fila;
    }

    /** RN17: subiu quando o XP passou de uma centena para outra. */
    public static boolean subiuDeNivel(int xpAntes, int xpDepois) {
        return Gamificacao.calcularNivel(xpDepois) > Gamificacao.calcularNivel(xpAntes);
    }

    public static ArrayList<String> paraTextos(List<Celebracao> fila) {
        ArrayList<String> textos = new ArrayList<>();
        for (Celebracao c : fila) {
            textos.add(c.paraTexto());
        }
        return textos;
    }

    /** Lê a fila da Intent; textos inválidos são ignorados. */
    public static List<Celebracao> deTextos(List<String> textos) {
        List<Celebracao> fila = new ArrayList<>();
        if (textos != null) {
            for (String texto : textos) {
                Celebracao c = Celebracao.deTexto(texto);
                if (c != null) {
                    fila.add(c);
                }
            }
        }
        return fila;
    }

    /** A lição perfeita aparece logo que a lição acaba, antes do Resultado. */
    public static List<Celebracao> antesDoResultado(List<Celebracao> todas) {
        List<Celebracao> antes = new ArrayList<>();
        for (Celebracao c : todas) {
            if (c.tipo == Tipo.LICAO_PERFEITA) {
                antes.add(c);
            }
        }
        return antes;
    }

    /** Ofensiva, nível e conquistas aparecem depois do Resultado, no CONTINUAR. */
    public static List<Celebracao> depoisDoResultado(List<Celebracao> todas) {
        List<Celebracao> depois = new ArrayList<>();
        for (Celebracao c : todas) {
            if (c.tipo != Tipo.LICAO_PERFEITA) {
                depois.add(c);
            }
        }
        return depois;
    }
}
