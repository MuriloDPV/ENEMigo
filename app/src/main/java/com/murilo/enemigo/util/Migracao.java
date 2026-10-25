package com.murilo.enemigo.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Leva o progresso salvo com um banco antigo (versão 1: 4 lições de 5; versão 2: lições de 3 a 5)
 * para as lições atuais. O mapa de cada versão vem do questoes.json ("ids_antigos"): id da questão
 * naquela versão -> id atual, ou "" se a questão saiu do banco.
 * Funções puras (testadas em MigracaoTest); quem lê e grava o SharedPreferences é o ProgressoManager.
 */
public final class Migracao {

    private Migracao() {
    }

    /** Lição de uma questão pelo id: "LIN-01-03" -> "LIN-01". */
    public static String licaoDaQuestao(String questaoId) {
        int fim = questaoId.lastIndexOf('-');
        return fim < 0 ? questaoId : questaoId.substring(0, fim);
    }

    /** Ids das lições antigas, tirados do mapa (as chaves "melhor_" que a migração lê e apaga). */
    public static Set<String> licoesAntigas(Map<String, String> idsAntigos) {
        return tamanhos(idsAntigos).keySet();
    }

    /** Número de questões de cada lição antiga (o mapa leva todas, até as que saíram do banco). */
    public static Map<String, Integer> tamanhos(Map<String, String> idsAntigos) {
        Map<String, Integer> tamanhos = new HashMap<>();
        for (String antigo : idsAntigos.keySet()) {
            String licao = licaoDaQuestao(antigo);
            Integer atual = tamanhos.get(licao);
            tamanhos.put(licao, atual == null ? 1 : atual + 1);
        }
        return tamanhos;
    }

    /**
     * Lições atuais que já contam como concluídas: todas as questões delas estavam em lições
     * antigas concluídas (com a nota mínima daquela lição, ex.: 3 de 5). Questão nova no banco
     * nunca foi vista, então a lição que tem uma não conclui.
     *
     * @param idsAntigos   id da questão na versão antiga -> id atual ("" se saiu)
     * @param melhorAntigo melhor resultado salvo de cada lição antiga (as não feitas ficam de fora)
     * @param licoesAtuais id de cada lição atual -> ids das questões dela
     */
    public static Set<String> licoesConcluidas(Map<String, String> idsAntigos, Map<String, Integer> melhorAntigo,
                                               Map<String, List<String>> licoesAtuais) {
        Map<String, Integer> tamanhos = tamanhos(idsAntigos);
        Set<String> vencidas = new HashSet<>();
        for (Map.Entry<String, String> par : idsAntigos.entrySet()) {
            String licao = licaoDaQuestao(par.getKey());
            Integer melhor = melhorAntigo.get(licao);
            if (!par.getValue().isEmpty() && melhor != null
                    && Gamificacao.concluiu(melhor, tamanhos.get(licao))) {
                vencidas.add(par.getValue());
            }
        }
        Set<String> concluidas = new HashSet<>();
        for (Map.Entry<String, List<String>> licao : licoesAtuais.entrySet()) {
            if (!licao.getValue().isEmpty() && vencidas.containsAll(licao.getValue())) {
                concluidas.add(licao.getKey());
            }
        }
        return concluidas;
    }

    /**
     * Corta as concluídas de cada trilha na primeira lição que não está concluída: a trilha não
     * fica com uma lição concluída depois de uma que ainda falta fazer.
     *
     * @param trilhas ids das lições de cada área, na ordem da trilha
     */
    public static Set<String> semBuracos(Set<String> concluidas, Collection<List<String>> trilhas) {
        Set<String> resultado = new HashSet<>();
        for (List<String> trilha : trilhas) {
            for (String licao : trilha) {
                if (!concluidas.contains(licao)) {
                    break;
                }
                resultado.add(licao);
            }
        }
        return resultado;
    }

    /** Caderno de erros com os ids atuais. Questão que saiu do banco (ou id desconhecido) sai do caderno. */
    public static Set<String> converterCaderno(Set<String> caderno, Map<String, String> idsAntigos) {
        Set<String> convertido = new HashSet<>();
        for (String antigo : caderno) {
            String atual = idsAntigos.get(antigo);
            if (atual != null && !atual.isEmpty()) {
                convertido.add(atual);
            }
        }
        return convertido;
    }
}
