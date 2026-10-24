package com.murilo.enemigo.model;

import java.util.ArrayList;

/**
 * Resumo de uma sessão terminada (lição ou revisão), devolvido pelo ProgressoManager.
 * A QuizActivity copia os campos para os extras da Intent do Resultado.
 */
public class ResultadoSessao {

    private String modo;
    private int acertos;
    private int total;
    private int xpGanho;
    private int bonus;
    private int ofensiva;
    private boolean metaBatidaAgora;
    private boolean desbloqueouProxima;
    private int vidasRecuperadas;
    private ArrayList<String> novasConquistas = new ArrayList<>();
    private int nivel;
    private boolean subiuDeNivel;
    private boolean ofensivaAumentou;

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public int getAcertos() {
        return acertos;
    }

    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    /** XP dos acertos, sem o bônus. */
    public int getXpGanho() {
        return xpGanho;
    }

    public void setXpGanho(int xpGanho) {
        this.xpGanho = xpGanho;
    }

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    /** Ofensiva depois da sessão. */
    public int getOfensiva() {
        return ofensiva;
    }

    public void setOfensiva(int ofensiva) {
        this.ofensiva = ofensiva;
    }

    public boolean isMetaBatidaAgora() {
        return metaBatidaAgora;
    }

    public void setMetaBatidaAgora(boolean metaBatidaAgora) {
        this.metaBatidaAgora = metaBatidaAgora;
    }

    public boolean isDesbloqueouProxima() {
        return desbloqueouProxima;
    }

    public void setDesbloqueouProxima(boolean desbloqueouProxima) {
        this.desbloqueouProxima = desbloqueouProxima;
    }

    public int getVidasRecuperadas() {
        return vidasRecuperadas;
    }

    public void setVidasRecuperadas(int vidasRecuperadas) {
        this.vidasRecuperadas = vidasRecuperadas;
    }

    /** Ids das conquistas desbloqueadas nesta sessão (RN33). */
    public ArrayList<String> getNovasConquistas() {
        return novasConquistas;
    }

    public void setNovasConquistas(ArrayList<String> novasConquistas) {
        this.novasConquistas = novasConquistas;
    }

    /** Nível depois da sessão. */
    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public boolean isSubiuDeNivel() {
        return subiuDeNivel;
    }

    public void setSubiuDeNivel(boolean subiuDeNivel) {
        this.subiuDeNivel = subiuDeNivel;
    }

    /** true na primeira sessão do dia: a ofensiva mostrada antes era menor. */
    public boolean isOfensivaAumentou() {
        return ofensivaAumentou;
    }

    public void setOfensivaAumentou(boolean ofensivaAumentou) {
        this.ofensivaAumentou = ofensivaAumentou;
    }
}
