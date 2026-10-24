package com.murilo.enemigo.model;

import java.util.Arrays;
import java.util.Random;

/**
 * Avatar montável do aluno: uma escolha (índice) para cada parte. Imutável: com() devolve um
 * avatar novo. Salvo como texto ("2,0,0,1,0,0,0"). Os desenhos e as cores de cada índice ficam
 * em ui/componente/AvatarDrawable. Testado em AvatarTest.
 */
public final class Avatar {

    /** As partes do avatar, na ordem em que aparecem no texto salvo, com quantas opções cada uma tem. */
    public enum Parte {
        PELE(8), ROSTO(3), CABELO(6), COR_CABELO(6), OLHOS(4), ACESSORIO(4), FUNDO(8);

        public final int opcoes;

        Parte(int opcoes) {
            this.opcoes = opcoes;
        }
    }

    /** Pele média, rosto redondo, cabelo curto castanho, olhos redondos, sem acessório. */
    private static final int[] PADRAO = {3, 0, 0, 1, 0, 0, 0};

    private final int[] escolhas;

    private Avatar(int[] escolhas) {
        this.escolhas = escolhas;
    }

    public static Avatar padrao() {
        return new Avatar(PADRAO.clone());
    }

    public static Avatar sortear(Random sorteio) {
        int[] escolhas = new int[Parte.values().length];
        for (Parte parte : Parte.values()) {
            escolhas[parte.ordinal()] = sorteio.nextInt(parte.opcoes);
        }
        return new Avatar(escolhas);
    }

    /**
     * O contrário de paraTexto. Texto vazio ou estragado vira o padrão; um número fora da faixa
     * volta ao padrão só naquela parte.
     */
    public static Avatar deTexto(String texto) {
        Parte[] partes = Parte.values();
        if (texto == null) {
            return padrao();
        }
        String[] pedacos = texto.split(",");
        if (pedacos.length != partes.length) {
            return padrao();
        }
        int[] escolhas = PADRAO.clone();
        try {
            for (Parte parte : partes) {
                int escolha = Integer.parseInt(pedacos[parte.ordinal()].trim());
                if (escolha >= 0 && escolha < parte.opcoes) {
                    escolhas[parte.ordinal()] = escolha;
                }
            }
        } catch (NumberFormatException e) {
            return padrao();
        }
        return new Avatar(escolhas);
    }

    public int get(Parte parte) {
        return escolhas[parte.ordinal()];
    }

    /** Um avatar igual a este, com outra escolha só nesta parte. */
    public Avatar com(Parte parte, int escolha) {
        if (escolha < 0 || escolha >= parte.opcoes) {
            throw new IllegalArgumentException(parte + " não tem a opção " + escolha);
        }
        int[] novas = escolhas.clone();
        novas[parte.ordinal()] = escolha;
        return new Avatar(novas);
    }

    public String paraTexto() {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < escolhas.length; i++) {
            if (i > 0) {
                texto.append(',');
            }
            texto.append(escolhas[i]);
        }
        return texto.toString();
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Avatar && Arrays.equals(escolhas, ((Avatar) outro).escolhas);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(escolhas);
    }

    @Override
    public String toString() {
        return "Avatar(" + paraTexto() + ")";
    }
}
