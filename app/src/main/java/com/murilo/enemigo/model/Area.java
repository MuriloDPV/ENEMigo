package com.murilo.enemigo.model;

import com.murilo.enemigo.R;

/**
 * As 4 áreas do ENEM. São fixas, por isso viram um enum.
 * Cada área tem a cor do cartão, a cor escura (sombra e textos), a cor do conteúdo sobre o
 * cartão e a ilustração do canto.
 */
public enum Area {
    LINGUAGENS("Linguagens", "LIN", R.color.area_linguagens, R.color.area_linguagens_escura,
            R.color.area_linguagens_sobre, R.drawable.ilustracao_linguagens),
    HUMANAS("Ciências Humanas", "HUM", R.color.area_humanas, R.color.area_humanas_escura,
            R.color.area_humanas_sobre, R.drawable.ilustracao_humanas),
    NATUREZA("Ciências da Natureza", "NAT", R.color.area_natureza, R.color.area_natureza_escura,
            R.color.area_natureza_sobre, R.drawable.ilustracao_natureza),
    MATEMATICA("Matemática", "MAT", R.color.area_matematica, R.color.area_matematica_escura,
            R.color.area_matematica_sobre, R.drawable.ilustracao_matematica);

    private final String nome;
    private final String sigla;
    private final int corRes;
    private final int corEscuraRes;
    private final int corSobreRes;
    private final int ilustracaoRes;

    Area(String nome, String sigla, int corRes, int corEscuraRes, int corSobreRes, int ilustracaoRes) {
        this.nome = nome;
        this.sigla = sigla;
        this.corRes = corRes;
        this.corEscuraRes = corEscuraRes;
        this.corSobreRes = corSobreRes;
        this.ilustracaoRes = ilustracaoRes;
    }

    /** Id usado no JSON e nas Intents (LINGUAGENS, HUMANAS...). */
    public String getId() {
        return name();
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public int getCorRes() {
        return corRes;
    }

    public int getCorEscuraRes() {
        return corEscuraRes;
    }

    public int getCorSobreRes() {
        return corSobreRes;
    }

    public int getIlustracaoRes() {
        return ilustracaoRes;
    }

    /** Devolve a área pelo id, ou null se o id não existir. */
    public static Area fromId(String id) {
        if (id == null) {
            return null;
        }
        for (Area area : values()) {
            if (area.name().equals(id)) {
                return area;
            }
        }
        return null;
    }
}
