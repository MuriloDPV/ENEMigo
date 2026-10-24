package com.murilo.enemigo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.murilo.enemigo.model.Conquista;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConquistasTest {

    private static final Set<String> NENHUMA = Collections.emptySet();

    /** Aluno novo: nada feito, Matemática com 4 lições. */
    private static Conquistas.Estado novo() {
        return new Conquistas.Estado().totalLicoesMatematica(4);
    }

    private static List<Conquista> lista(Conquista... conquistas) {
        return Arrays.asList(conquistas);
    }

    @Test
    public void alunoNovo_nenhumaConquista() {
        assertTrue(Conquistas.novas(novo(), NENHUMA).isEmpty());
    }

    @Test
    public void primeiraLicao_valeComQualquerNota() {
        // Terminou a lição (mesmo com 0 acertos): a lição conta como feita.
        assertEquals(lista(Conquista.PRIMEIRA_LICAO),
                Conquistas.novas(novo().licoesTerminadas(1), NENHUMA));
    }

    @Test
    public void ofensiva_doisDias_aindaNao() {
        assertTrue(Conquistas.novas(novo().ofensiva(2), NENHUMA).isEmpty());
    }

    @Test
    public void ofensiva_tresDias() {
        assertEquals(lista(Conquista.OFENSIVA_3), Conquistas.novas(novo().ofensiva(3), NENHUMA));
    }

    @Test
    public void ofensiva_seisDias_soADeTres() {
        assertEquals(lista(Conquista.OFENSIVA_3), Conquistas.novas(novo().ofensiva(6), NENHUMA));
    }

    @Test
    public void ofensiva_seteDias_semNenhumaAntes_ganhaAsDuas() {
        assertEquals(lista(Conquista.OFENSIVA_3, Conquista.OFENSIVA_7),
                Conquistas.novas(novo().ofensiva(7), NENHUMA));
    }

    @Test
    public void xp_99_aindaNao() {
        assertTrue(Conquistas.novas(novo().xpTotal(99), NENHUMA).isEmpty());
    }

    @Test
    public void xp_100() {
        assertEquals(lista(Conquista.XP_100), Conquistas.novas(novo().xpTotal(100), NENHUMA));
    }

    @Test
    public void xp_499_soADe100() {
        assertEquals(lista(Conquista.XP_100), Conquistas.novas(novo().xpTotal(499), NENHUMA));
    }

    @Test
    public void xp_500_ganhaAsDuas() {
        assertEquals(lista(Conquista.XP_100, Conquista.XP_500),
                Conquistas.novas(novo().xpTotal(500), NENHUMA));
    }

    @Test
    public void licaoPerfeita() {
        assertEquals(lista(Conquista.LICAO_PERFEITA),
                Conquistas.novas(novo().licoesPerfeitas(1), NENHUMA));
    }

    @Test
    public void matematica_umaLicaoConcluida() {
        assertEquals(lista(Conquista.MATEMATICA_PRIMEIRA),
                Conquistas.novas(novo().licoesMatematicaConcluidas(1), NENHUMA));
    }

    @Test
    public void matematica_tresDeQuatro_aindaNaoCompleta() {
        assertEquals(lista(Conquista.MATEMATICA_PRIMEIRA),
                Conquistas.novas(novo().licoesMatematicaConcluidas(3), NENHUMA));
    }

    @Test
    public void matematica_todasAsLicoes_ganhaAsDuas() {
        assertEquals(lista(Conquista.MATEMATICA_PRIMEIRA, Conquista.MATEMATICA_COMPLETA),
                Conquistas.novas(novo().licoesMatematicaConcluidas(4), NENHUMA));
    }

    @Test
    public void matematica_semLicoesNoBanco_naoCompleta() {
        // Área vazia ("Em breve"): 0 de 0 não pode contar como completa.
        Conquistas.Estado estado = new Conquistas.Estado().totalLicoesMatematica(0);
        assertTrue(Conquistas.novas(estado, NENHUMA).isEmpty());
    }

    @Test
    public void jaDesbloqueada_naoVoltaComoNova() {
        // RN33: só entram as que não estavam desbloqueadas antes da sessão.
        Set<String> salvas = new HashSet<>(Arrays.asList("PRIMEIRA_LICAO", "XP_100"));
        assertEquals(lista(Conquista.XP_500),
                Conquistas.novas(novo().licoesTerminadas(9).xpTotal(520), salvas));
    }

    @Test
    public void variasNaMesmaSessao_vemNaOrdemDaLista() {
        // Primeira lição feita com 5/5: ganha "Primeiro passo" e "Gabaritou!" juntas.
        Conquistas.Estado estado = novo().licoesTerminadas(1).licoesPerfeitas(1).xpTotal(55)
                .ofensiva(1).licoesMatematicaConcluidas(1);
        assertEquals(lista(Conquista.PRIMEIRA_LICAO, Conquista.LICAO_PERFEITA,
                Conquista.MATEMATICA_PRIMEIRA), Conquistas.novas(estado, NENHUMA));
    }

    @Test
    public void idDesconhecidoNoSalvo_eIgnorado() {
        Set<String> salvas = new HashSet<>(Collections.singletonList("CONQUISTA_QUE_SAIU"));
        assertEquals(lista(Conquista.PRIMEIRA_LICAO),
                Conquistas.novas(novo().licoesTerminadas(1), salvas));
    }

    @Test
    public void tudoFeito_todasDesbloqueadas() {
        Conquistas.Estado estado = novo().licoesTerminadas(16).ofensiva(7).xpTotal(900)
                .licoesPerfeitas(3).licoesMatematicaConcluidas(4);
        assertEquals(Arrays.asList(Conquista.values()), Conquistas.novas(estado, NENHUMA));
    }

    @Test
    public void conquista_porId() {
        assertEquals(Conquista.OFENSIVA_7, Conquista.fromId("OFENSIVA_7"));
        assertNull(Conquista.fromId("NAO_EXISTE"));
        assertNull(Conquista.fromId(null));
    }

    @Test
    public void conquistas_saoOito() {
        assertEquals(8, Conquista.values().length);
    }
}
