package com.murilo.enemigo.model;

import java.util.List;

/**
 * Uma lição da trilha de uma área, com 5 questões.
 */
public class Licao {

    private final String id;
    private final Area area;
    private final int ordem;
    private final String titulo;
    private final List<Questao> questoes;

    public Licao(String id, Area area, int ordem, String titulo, List<Questao> questoes) {
        this.id = id;
        this.area = area;
        this.ordem = ordem;
        this.titulo = titulo;
        this.questoes = questoes;
    }

    /** Ex.: "MAT-01". */
    public String getId() {
        return id;
    }

    public Area getArea() {
        return area;
    }

    /** Posição na trilha: 0, 1, 2... */
    public int getOrdem() {
        return ordem;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<Questao> getQuestoes() {
        return questoes;
    }
}
