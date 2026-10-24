package com.murilo.enemigo.model;

/**
 * Uma questão de múltipla escolha (A a E).
 */
public class Questao {

    private final String id;
    private final Area area;
    private final String licaoId;
    private final String texto;
    private final String enunciado;
    private final String[] alternativas;
    private final int correta;
    private final String explicacao;
    private final String fonte;
    private final int segundosEstimados;

    public Questao(String id, Area area, String licaoId, String texto, String enunciado,
                   String[] alternativas, int correta, String explicacao, String fonte,
                   int segundosEstimados) {
        this.id = id;
        this.area = area;
        this.licaoId = licaoId;
        this.texto = texto;
        this.enunciado = enunciado;
        this.alternativas = alternativas;
        this.correta = correta;
        this.explicacao = explicacao;
        this.fonte = fonte;
        this.segundosEstimados = segundosEstimados;
    }

    public String getId() {
        return id;
    }

    public Area getArea() {
        return area;
    }

    public String getLicaoId() {
        return licaoId;
    }

    /** Texto-base; vazio se a questão não tiver. */
    public String getTexto() {
        return texto;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String[] getAlternativas() {
        return alternativas;
    }

    /** Índice da correta: 0 = A ... 4 = E. */
    public int getCorreta() {
        return correta;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public String getFonte() {
        return fonte;
    }

    /** Tempo estimado para ler e responder (vem do JSON; 0 se não tiver). */
    public int getSegundosEstimados() {
        return segundosEstimados;
    }

    public boolean isCorreta(int indice) {
        return indice == correta;
    }

    /** Letra da alternativa correta ("A" a "E"). */
    public String getLetraCorreta() {
        return String.valueOf((char) ('A' + correta));
    }
}
