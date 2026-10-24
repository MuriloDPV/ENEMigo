package com.murilo.enemigo.util;

import com.murilo.enemigo.model.Conquista;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Condições das conquistas como funções puras (testadas em ConquistasTest).
 */
public final class Conquistas {

    private Conquistas() {
    }

    /** O que as condições precisam saber do progresso, já atualizado pela sessão. */
    public static final class Estado {
        int licoesTerminadas;
        int ofensiva;
        int xpTotal;
        int licoesPerfeitas;
        int licoesMatematicaConcluidas;
        int totalLicoesMatematica;

        /** Lições feitas pelo menos uma vez, com qualquer nota. */
        public Estado licoesTerminadas(int valor) {
            licoesTerminadas = valor;
            return this;
        }

        public Estado ofensiva(int valor) {
            ofensiva = valor;
            return this;
        }

        public Estado xpTotal(int valor) {
            xpTotal = valor;
            return this;
        }

        /** Quantas vezes acertou todas as questões de uma lição. */
        public Estado licoesPerfeitas(int valor) {
            licoesPerfeitas = valor;
            return this;
        }

        /** Lições de Matemática concluídas (60% de acertos ou mais). */
        public Estado licoesMatematicaConcluidas(int valor) {
            licoesMatematicaConcluidas = valor;
            return this;
        }

        public Estado totalLicoesMatematica(int valor) {
            totalLicoesMatematica = valor;
            return this;
        }
    }

    /** A condição de cada conquista (RN32: o estado já tem o XP e a ofensiva da sessão). */
    public static boolean atingiu(Conquista conquista, Estado estado) {
        switch (conquista) {
            case PRIMEIRA_LICAO:
                return estado.licoesTerminadas >= 1;
            case OFENSIVA_3:
                return estado.ofensiva >= 3;
            case OFENSIVA_7:
                return estado.ofensiva >= 7;
            case XP_100:
                return estado.xpTotal >= 100;
            case XP_500:
                return estado.xpTotal >= 500;
            case LICAO_PERFEITA:
                return estado.licoesPerfeitas >= 1;
            case MATEMATICA_PRIMEIRA:
                return estado.licoesMatematicaConcluidas >= 1;
            case MATEMATICA_COMPLETA:
                // Área sem lições ("Em breve") não conta como completa.
                return estado.totalLicoesMatematica > 0
                        && estado.licoesMatematicaConcluidas >= estado.totalLicoesMatematica;
            default:
                return false;
        }
    }

    /**
     * RN33: as conquistas atingidas que ainda não estavam desbloqueadas, na ordem da lista.
     * Ids salvos que não existem mais são ignorados.
     */
    public static List<Conquista> novas(Estado estado, Set<String> jaDesbloqueadas) {
        List<Conquista> novas = new ArrayList<>();
        for (Conquista conquista : Conquista.values()) {
            if (!jaDesbloqueadas.contains(conquista.getId()) && atingiu(conquista, estado)) {
                novas.add(conquista);
            }
        }
        return novas;
    }
}
