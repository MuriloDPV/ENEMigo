package com.murilo.enemigo.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Leva o progresso salvo com o banco da versão 1 (4 lições de 5 por área) para as lições atuais.
 * Funções puras (testadas em MigracaoTest); quem lê e grava o SharedPreferences é o ProgressoManager.
 */
public final class Migracao {

    /** Na versão 1 a lição concluía com 3 acertos de 5. */
    public static final int ACERTOS_V1 = 3;

    private Migracao() {
    }

    /** Lição de uma questão pelo id: "LIN-01-03" -> "LIN-01". */
    public static String licaoDaQuestao(String questaoId) {
        int fim = questaoId.lastIndexOf('-');
        return fim < 0 ? questaoId : questaoId.substring(0, fim);
    }

    /** Ids das lições da versão 1, tirados do mapa (as chaves "melhor_" que a migração lê e apaga). */
    public static Set<String> licoesV1(Map<String, String> idsV1) {
        Set<String> licoes = new HashSet<>();
        for (String antigo : idsV1.keySet()) {
            licoes.add(licaoDaQuestao(antigo));
        }
        return licoes;
    }

    /**
     * Lições atuais que já contam como concluídas: todas as questões delas estavam em lições
     * concluídas na versão 1.
     *
     * @param idsV1        id da questão na versão 1 -> id atual
     * @param melhorV1     melhor resultado salvo de cada lição da versão 1 (as não feitas ficam de fora)
     * @param licoesAtuais id de cada lição atual -> ids das questões dela
     */
    public static Set<String> licoesConcluidas(Map<String, String> idsV1, Map<String, Integer> melhorV1,
                                               Map<String, List<String>> licoesAtuais) {
        Map<String, String> atualParaV1 = new HashMap<>();
        for (Map.Entry<String, String> par : idsV1.entrySet()) {
            atualParaV1.put(par.getValue(), par.getKey());
        }
        Set<String> concluidas = new HashSet<>();
        for (Map.Entry<String, List<String>> licao : licoesAtuais.entrySet()) {
            boolean todas = !licao.getValue().isEmpty();
            for (String questao : licao.getValue()) {
                String antigo = atualParaV1.get(questao);
                Integer melhor = antigo == null ? null : melhorV1.get(licaoDaQuestao(antigo));
                if (melhor == null || melhor < ACERTOS_V1) {
                    todas = false;
                    break;
                }
            }
            if (todas) {
                concluidas.add(licao.getKey());
            }
        }
        return concluidas;
    }

    /** Caderno de erros com os ids atuais. Id que não está no mapa sai (a questão não existe mais). */
    public static Set<String> converterCaderno(Set<String> caderno, Map<String, String> idsV1) {
        Set<String> convertido = new HashSet<>();
        for (String antigo : caderno) {
            String atual = idsV1.get(antigo);
            if (atual != null) {
                convertido.add(atual);
            }
        }
        return convertido;
    }
}
