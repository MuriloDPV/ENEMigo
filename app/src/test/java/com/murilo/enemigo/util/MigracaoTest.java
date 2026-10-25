package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MigracaoTest {

    /**
     * Versão antiga com duas lições de 5 (LIN-01 e LIN-02) reagrupadas em três lições atuais:
     * LIN-01 (3 da antiga LIN-01), LIN-02 (2 da LIN-01 + 1 da LIN-02) e LIN-03 (4 da LIN-02).
     */
    private static final Map<String, String> IDS_V1 = new HashMap<>();
    private static final Map<String, List<String>> ATUAIS = new HashMap<>();

    /**
     * Versão 2 com lições de 3: a LIN-01 antiga perde uma questão (saiu do banco) e a atual LIN-02
     * ganha uma questão nova, que nenhuma lição antiga tinha.
     */
    private static final Map<String, String> IDS_V2 = new HashMap<>();
    private static final Map<String, List<String>> ATUAIS_V3 = new HashMap<>();

    static {
        IDS_V1.put("LIN-01-01", "LIN-01-01");
        IDS_V1.put("LIN-01-02", "LIN-01-02");
        IDS_V1.put("LIN-01-03", "LIN-01-03");
        IDS_V1.put("LIN-01-04", "LIN-02-01");
        IDS_V1.put("LIN-01-05", "LIN-02-02");
        IDS_V1.put("LIN-02-01", "LIN-02-03");
        IDS_V1.put("LIN-02-02", "LIN-03-01");
        IDS_V1.put("LIN-02-03", "LIN-03-02");
        IDS_V1.put("LIN-02-04", "LIN-03-03");
        IDS_V1.put("LIN-02-05", "LIN-03-04");
        ATUAIS.put("LIN-01", Arrays.asList("LIN-01-01", "LIN-01-02", "LIN-01-03"));
        ATUAIS.put("LIN-02", Arrays.asList("LIN-02-01", "LIN-02-02", "LIN-02-03"));
        ATUAIS.put("LIN-03", Arrays.asList("LIN-03-01", "LIN-03-02", "LIN-03-03", "LIN-03-04"));

        IDS_V2.put("LIN-01-01", "LIN-01-01");
        IDS_V2.put("LIN-01-02", "");
        IDS_V2.put("LIN-01-03", "LIN-01-02");
        IDS_V2.put("LIN-02-01", "LIN-01-03");
        IDS_V2.put("LIN-02-02", "LIN-02-01");
        IDS_V2.put("LIN-02-03", "LIN-02-02");
        ATUAIS_V3.put("LIN-01", Arrays.asList("LIN-01-01", "LIN-01-02", "LIN-01-03"));
        ATUAIS_V3.put("LIN-02", Arrays.asList("LIN-02-01", "LIN-02-02", "LIN-02-03"));
    }

    private static Map<String, Integer> melhores(Object... pares) {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < pares.length; i += 2) {
            m.put((String) pares[i], (Integer) pares[i + 1]);
        }
        return m;
    }

    private static Set<String> conjunto(String... ids) {
        return new HashSet<>(Arrays.asList(ids));
    }

    @Test
    public void licaoDaQuestao_tiraONumeroDaQuestao() {
        assertEquals("LIN-01", Migracao.licaoDaQuestao("LIN-01-03"));
        assertEquals("MAT-04", Migracao.licaoDaQuestao("MAT-04-05"));
    }

    @Test
    public void licoesAntigas_saemDoMapa() {
        assertEquals(conjunto("LIN-01", "LIN-02"), Migracao.licoesAntigas(IDS_V1));
    }

    @Test
    public void tamanhos_contamAsQuestoesQueSairamDoBanco() {
        assertEquals(Integer.valueOf(3), Migracao.tamanhos(IDS_V2).get("LIN-01"));
        assertEquals(Integer.valueOf(3), Migracao.tamanhos(IDS_V2).get("LIN-02"));
    }

    @Test
    public void nadaFeito_nadaConcluido() {
        assertEquals(Collections.emptySet(),
                Migracao.licoesConcluidas(IDS_V1, melhores(), ATUAIS));
    }

    @Test
    public void soAPrimeiraConcluida_soALicaoQueVeioInteiraDelaConclui() {
        // A LIN-02 atual tem uma questão da antiga LIN-02, que não foi concluída.
        assertEquals(conjunto("LIN-01"),
                Migracao.licoesConcluidas(IDS_V1, melhores("LIN-01", 4), ATUAIS));
    }

    @Test
    public void duasConcluidas_todasConcluem() {
        assertEquals(conjunto("LIN-01", "LIN-02", "LIN-03"),
                Migracao.licoesConcluidas(IDS_V1, melhores("LIN-01", 3, "LIN-02", 5), ATUAIS));
    }

    @Test
    public void feitaComMenosDeTresDeCinco_naoContaComoConcluida() {
        assertEquals(conjunto("LIN-01"),
                Migracao.licoesConcluidas(IDS_V1, melhores("LIN-01", 5, "LIN-02", 2), ATUAIS));
    }

    @Test
    public void licaoAntigaDeTres_concluiComDoisAcertos() {
        assertEquals(conjunto("LIN-01"),
                Migracao.licoesConcluidas(IDS_V2, melhores("LIN-01", 2, "LIN-02", 2), ATUAIS_V3));
    }

    @Test
    public void licaoComQuestaoNova_naoConclui() {
        // A LIN-02 atual tem a LIN-02-03, que não estava em nenhuma lição antiga.
        assertEquals(conjunto("LIN-01"),
                Migracao.licoesConcluidas(IDS_V2, melhores("LIN-01", 3, "LIN-02", 3), ATUAIS_V3));
    }

    @Test
    public void questaoSemIdAntigo_naoConclui() {
        Map<String, List<String>> atuais = new HashMap<>(ATUAIS);
        atuais.put("LIN-04", Arrays.asList("LIN-01-01", "LIN-04-01", "LIN-04-02"));
        assertEquals(conjunto("LIN-01", "LIN-02", "LIN-03"),
                Migracao.licoesConcluidas(IDS_V1, melhores("LIN-01", 5, "LIN-02", 5), atuais));
    }

    @Test
    public void semBuracos_paraNaPrimeiraLicaoQueFalta() {
        List<String> linguagens = Arrays.asList("LIN-01", "LIN-02", "LIN-03");
        List<String> matematica = Arrays.asList("MAT-01", "MAT-02");
        assertEquals(conjunto("LIN-01", "MAT-01", "MAT-02"),
                Migracao.semBuracos(conjunto("LIN-01", "LIN-03", "MAT-01", "MAT-02"),
                        Arrays.asList(linguagens, matematica)));
    }

    @Test
    public void semBuracos_primeiraNaoConcluida_nadaFica() {
        assertEquals(Collections.emptySet(),
                Migracao.semBuracos(conjunto("LIN-02", "LIN-03"),
                        Collections.singletonList(Arrays.asList("LIN-01", "LIN-02", "LIN-03"))));
    }

    @Test
    public void caderno_trocaOsIdsEDescartaOsDesconhecidos() {
        assertEquals(conjunto("LIN-02-01", "LIN-03-04"),
                Migracao.converterCaderno(conjunto("LIN-01-04", "LIN-02-05", "XYZ-99-01"), IDS_V1));
    }

    @Test
    public void caderno_questaoQueSaiuDoBanco_saiDoCaderno() {
        assertEquals(conjunto("LIN-01-01"),
                Migracao.converterCaderno(conjunto("LIN-01-01", "LIN-01-02"), IDS_V2));
    }
}
