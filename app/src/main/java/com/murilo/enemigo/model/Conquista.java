package com.murilo.enemigo.model;

import com.murilo.enemigo.R;

/**
 * As conquistas do app (C3), na ordem em que aparecem no Perfil, cada uma com a sua medalha
 * vetorial. O id salvo no SharedPreferences é o nome da constante (ex.: "PRIMEIRA_LICAO").
 * As condições ficam em util/Conquistas.
 */
public enum Conquista {
    PRIMEIRA_LICAO(R.string.conquista_primeira_licao, R.string.conquista_primeira_licao_desc,
            R.drawable.medalha_primeira_licao),
    OFENSIVA_3(R.string.conquista_ofensiva_3, R.string.conquista_ofensiva_3_desc,
            R.drawable.medalha_ofensiva_3),
    OFENSIVA_7(R.string.conquista_ofensiva_7, R.string.conquista_ofensiva_7_desc,
            R.drawable.medalha_ofensiva_7),
    XP_100(R.string.conquista_xp_100, R.string.conquista_xp_100_desc, R.drawable.medalha_xp_100),
    XP_500(R.string.conquista_xp_500, R.string.conquista_xp_500_desc, R.drawable.medalha_xp_500),
    LICAO_PERFEITA(R.string.conquista_licao_perfeita, R.string.conquista_licao_perfeita_desc,
            R.drawable.medalha_licao_perfeita),
    MATEMATICA_PRIMEIRA(R.string.conquista_matematica_primeira,
            R.string.conquista_matematica_primeira_desc, R.drawable.medalha_matematica_primeira),
    MATEMATICA_COMPLETA(R.string.conquista_matematica_completa,
            R.string.conquista_matematica_completa_desc, R.drawable.medalha_matematica_completa);

    private final int tituloRes;
    private final int descricaoRes;
    private final int medalhaRes;

    Conquista(int tituloRes, int descricaoRes, int medalhaRes) {
        this.tituloRes = tituloRes;
        this.descricaoRes = descricaoRes;
        this.medalhaRes = medalhaRes;
    }

    /** Medalha vetorial (res/drawable/medalha_*). */
    public int getMedalhaRes() {
        return medalhaRes;
    }

    public String getId() {
        return name();
    }

    public int getTituloRes() {
        return tituloRes;
    }

    public int getDescricaoRes() {
        return descricaoRes;
    }

    /** Devolve a conquista pelo id, ou null se o id não existir. */
    public static Conquista fromId(String id) {
        if (id == null) {
            return null;
        }
        for (Conquista conquista : values()) {
            if (conquista.name().equals(id)) {
                return conquista;
            }
        }
        return null;
    }
}
