package com.murilo.enemigo.model;

import com.murilo.enemigo.R;

/**
 * As 4 áreas do ENEM. São fixas, por isso viram um enum.
 */
public enum Area {
    LINGUAGENS("Linguagens", "LIN", R.color.area_linguagens, R.drawable.ic_area_linguagens),
    HUMANAS("Ciências Humanas", "HUM", R.color.area_humanas, R.drawable.ic_area_humanas),
    NATUREZA("Ciências da Natureza", "NAT", R.color.area_natureza, R.drawable.ic_area_natureza),
    MATEMATICA("Matemática", "MAT", R.color.area_matematica, R.drawable.ic_area_matematica);

    private final String nome;
    private final String sigla;
    private final int corRes;
    private final int iconeRes;

    Area(String nome, String sigla, int corRes, int iconeRes) {
        this.nome = nome;
        this.sigla = sigla;
        this.corRes = corRes;
        this.iconeRes = iconeRes;
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

    public int getIconeRes() {
        return iconeRes;
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
