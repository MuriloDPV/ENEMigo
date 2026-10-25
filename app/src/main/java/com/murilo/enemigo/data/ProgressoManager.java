package com.murilo.enemigo.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.Conquista;
import com.murilo.enemigo.model.EstadoLicao;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.model.ResultadoSessao;
import com.murilo.enemigo.util.Celebracoes;
import com.murilo.enemigo.util.Conquistas;
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.DataUtil;
import com.murilo.enemigo.util.Gamificacao;
import com.murilo.enemigo.util.Migracao;
import com.murilo.enemigo.util.TempoEstudo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Todo o progresso do aluno, salvo no SharedPreferences. As telas nunca acessam o
 * SharedPreferences direto: sempre passam por aqui. Os cálculos ficam na Gamificacao.
 * Singleton: use getInstance(context).
 */
public final class ProgressoManager {

    private static ProgressoManager instancia;

    private final SharedPreferences prefs;
    private final QuestaoRepository repositorio;

    private ProgressoManager(Context context) {
        prefs = context.getSharedPreferences(Constantes.PREFS_NOME, Context.MODE_PRIVATE);
        repositorio = QuestaoRepository.getInstance(context);
        migrarProgresso();
    }

    public static synchronized ProgressoManager getInstance(Context context) {
        if (instancia == null) {
            instancia = new ProgressoManager(context.getApplicationContext());
        }
        return instancia;
    }

    // ---------------------------------------------------------------- Perfil

    public String getNome() {
        return prefs.getString(Constantes.CHAVE_NOME, "");
    }

    public void setNome(String nome) {
        prefs.edit().putString(Constantes.CHAVE_NOME, nome.trim()).apply();
    }

    /** 20, 50 ou 100. Qualquer outro valor salvo volta para 50. */
    public int getMetaDiaria() {
        int meta = prefs.getInt(Constantes.CHAVE_META_DIARIA, Constantes.META_PADRAO);
        return Gamificacao.isMetaValida(meta) ? meta : Constantes.META_PADRAO;
    }

    public void setMetaDiaria(int meta) {
        if (Gamificacao.isMetaValida(meta)) {
            prefs.edit().putInt(Constantes.CHAVE_META_DIARIA, meta).apply();
        }
    }

    public boolean isOnboardingConcluido() {
        return prefs.getBoolean(Constantes.CHAVE_ONBOARDING_OK, false);
    }

    public void concluirOnboarding(String nome, int meta) {
        prefs.edit()
                .putString(Constantes.CHAVE_NOME, nome.trim())
                .putInt(Constantes.CHAVE_META_DIARIA,
                        Gamificacao.isMetaValida(meta) ? meta : Constantes.META_PADRAO)
                .putBoolean(Constantes.CHAVE_ONBOARDING_OK, true)
                .apply();
    }

    // ---------------------------------------------------------------- XP e nível

    public int getXpTotal() {
        return Math.max(0, prefs.getInt(Constantes.CHAVE_XP_TOTAL, 0));
    }

    public int getNivel() {
        return Gamificacao.calcularNivel(getXpTotal());
    }

    /** XP ganho hoje (0 se o último XP foi em outro dia). */
    public int getXpHoje() {
        LocalDate dataXp = DataUtil.deTexto(prefs.getString(Constantes.CHAVE_DATA_XP_HOJE, ""));
        return Gamificacao.calcularXpHoje(dataXp, DataUtil.hoje(),
                prefs.getInt(Constantes.CHAVE_XP_HOJE, 0));
    }

    // ---------------------------------------------------------------- Vidas

    /** Vidas atuais, já com a recuperação pelo tempo aplicada e salva. */
    public int getVidas() {
        return salvarVidas(Gamificacao.recarregarVidas(lerVidas(), lerUltimaRecarga(), agora()));
    }

    /** Tira 1 vida e salva na hora (RN11). */
    public int perderVida() {
        return salvarVidas(Gamificacao.perderVida(lerVidas(), lerUltimaRecarga(), agora()));
    }

    /** Devolve 1 vida (máx. 5). */
    public int ganharVida() {
        return salvarVidas(Gamificacao.ganharVida(lerVidas(), lerUltimaRecarga(), agora()));
    }

    public int getMinutosParaProximaVida() {
        getVidas();
        return Gamificacao.minutosParaProximaVida(lerUltimaRecarga(), agora());
    }

    private int lerVidas() {
        return prefs.getInt(Constantes.CHAVE_VIDAS, Constantes.VIDAS_MAX);
    }

    private long lerUltimaRecarga() {
        return prefs.getLong(Constantes.CHAVE_ULTIMA_RECARGA_VIDA, 0L);
    }

    private int salvarVidas(Gamificacao.EstadoVidas estado) {
        if (estado.vidas != lerVidas() || estado.ultimaRecarga != lerUltimaRecarga()) {
            prefs.edit()
                    .putInt(Constantes.CHAVE_VIDAS, estado.vidas)
                    .putLong(Constantes.CHAVE_ULTIMA_RECARGA_VIDA, estado.ultimaRecarga)
                    .apply();
        }
        return estado.vidas;
    }

    private long agora() {
        return System.currentTimeMillis();
    }

    // ---------------------------------------------------------------- Ofensiva

    /** Ofensiva para mostrar. Se o aluno pulou um dia, zera e salva o zero (RN28). */
    public int getOfensivaAtual() {
        int salva = prefs.getInt(Constantes.CHAVE_OFENSIVA, 0);
        int exibida = Gamificacao.ofensivaParaExibir(lerUltimaDataEstudo(), DataUtil.hoje(), salva);
        if (exibida != salva) {
            prefs.edit().putInt(Constantes.CHAVE_OFENSIVA, exibida).apply();
        }
        return exibida;
    }

    public int getMaiorOfensiva() {
        return Math.max(0, prefs.getInt(Constantes.CHAVE_MAIOR_OFENSIVA, 0));
    }

    public boolean estudouHoje() {
        return Gamificacao.estudouHoje(lerUltimaDataEstudo(), DataUtil.hoje());
    }

    /** Último dia com sessão concluída, ou null se nunca estudou (usado pelo lembrete). */
    public LocalDate getUltimaDataEstudo() {
        return lerUltimaDataEstudo();
    }

    private LocalDate lerUltimaDataEstudo() {
        return DataUtil.deTexto(prefs.getString(Constantes.CHAVE_ULTIMA_DATA_ESTUDO, ""));
    }

    // ---------------------------------------------------------------- Lições

    /** Melhor número de acertos da lição, ou -1 se nunca foi feita. */
    public int getMelhor(String licaoId) {
        return prefs.getInt(Constantes.PREFIXO_MELHOR + licaoId, -1);
    }

    /** RN05/RN06: calculado a partir do melhor, nunca salvo. */
    public EstadoLicao getEstado(Licao licao) {
        int melhorAnterior = -1;
        int totalAnterior = 0;
        if (licao.getOrdem() > 0) {
            List<Licao> lista = repositorio.getLicoes(licao.getArea());
            int anterior = licao.getOrdem() - 1;
            if (anterior < lista.size()) {
                melhorAnterior = getMelhor(lista.get(anterior).getId());
                totalAnterior = lista.get(anterior).getQuestoes().size();
            }
        }
        return Gamificacao.calcularEstado(licao.getOrdem(), getMelhor(licao.getId()),
                licao.getQuestoes().size(), melhorAnterior, totalAnterior);
    }

    /**
     * Tempo estimado da lição em minutos, já no ritmo do aluno depois de 10 questões medidas
     * (RN39), ou 0 se o JSON não tiver a estimativa.
     */
    public int getMinutosEstimados(Licao licao) {
        int segundos = licao.getSegundosEstimados();
        return segundos <= 0 ? 0 : TempoEstudo.minutosEstimados(segundos, getFatorRitmo());
    }

    public int getLicoesConcluidas(Area area) {
        int concluidas = 0;
        for (Licao licao : repositorio.getLicoes(area)) {
            if (Gamificacao.concluiu(getMelhor(licao.getId()), licao.getQuestoes().size())) {
                concluidas++;
            }
        }
        return concluidas;
    }

    public int getTotalLicoesConcluidas() {
        int total = 0;
        for (Area area : Area.values()) {
            total += getLicoesConcluidas(area);
        }
        return total;
    }

    // ---------------------------------------------------------------- Caderno de erros

    /** Cópia do caderno: pode alterar à vontade, não muda o que está salvo. */
    public Set<String> getCaderno() {
        return new HashSet<>(prefs.getStringSet(Constantes.CHAVE_CADERNO_ERROS, new HashSet<>()));
    }

    public void adicionarAoCaderno(String questaoId) {
        Set<String> caderno = getCaderno();
        if (caderno.add(questaoId)) {
            prefs.edit().putStringSet(Constantes.CHAVE_CADERNO_ERROS, caderno).apply();
        }
    }

    public void removerDoCaderno(String questaoId) {
        Set<String> caderno = getCaderno();
        if (caderno.remove(questaoId)) {
            prefs.edit().putStringSet(Constantes.CHAVE_CADERNO_ERROS, caderno).apply();
        }
    }

    // ---------------------------------------------------------------- Estatísticas

    public int getTotalRespondidas() {
        return Math.max(0, prefs.getInt(Constantes.CHAVE_TOTAL_RESPONDIDAS, 0));
    }

    public int getTotalAcertos() {
        return Math.max(0, prefs.getInt(Constantes.CHAVE_TOTAL_ACERTOS, 0));
    }

    // ---------------------------------------------------------------- Tempo de estudo

    /**
     * RN38: soma o tempo de uma questão respondida (do aparecer ao VERIFICAR, sem o tempo em
     * segundo plano). Tempo inválido, como mais de 10 min, é descartado.
     */
    public void registrarTempoQuestao(Questao questao, long ms) {
        if (!TempoEstudo.isTempoValido(ms)) {
            return;
        }
        prefs.edit()
                .putLong(Constantes.CHAVE_TEMPO_TOTAL_MS, lerTempoTotalMs() + ms)
                .putInt(Constantes.CHAVE_TEMPO_QUESTOES, lerQuestoesCronometradas() + 1)
                .putLong(Constantes.CHAVE_TEMPO_ESTIMADO_S,
                        prefs.getLong(Constantes.CHAVE_TEMPO_ESTIMADO_S, 0L) + questao.getSegundosEstimados())
                .putLong(Constantes.CHAVE_TEMPO_HOJE_MS, getTempoHojeMs() + ms)
                .putString(Constantes.CHAVE_DATA_TEMPO_HOJE, DataUtil.paraTexto(DataUtil.hoje()))
                .apply();
    }

    /** Tempo médio por questão em segundos, ou -1 se nenhuma foi medida. */
    public int getTempoMedioSegundos() {
        return TempoEstudo.mediaSegundos(lerTempoTotalMs(), lerQuestoesCronometradas());
    }

    /** Minutos respondendo questões hoje (lições e revisões). */
    public int getMinutosHoje() {
        return TempoEstudo.minutos(getTempoHojeMs());
    }

    /** RN39: ritmo real / estimado (1 até ter 10 questões medidas). */
    public double getFatorRitmo() {
        return TempoEstudo.fatorRitmo(lerTempoTotalMs(),
                prefs.getLong(Constantes.CHAVE_TEMPO_ESTIMADO_S, 0L), lerQuestoesCronometradas());
    }

    private long getTempoHojeMs() {
        LocalDate data = DataUtil.deTexto(prefs.getString(Constantes.CHAVE_DATA_TEMPO_HOJE, ""));
        return TempoEstudo.tempoHojeMs(data, DataUtil.hoje(), prefs.getLong(Constantes.CHAVE_TEMPO_HOJE_MS, 0L));
    }

    private long lerTempoTotalMs() {
        return Math.max(0L, prefs.getLong(Constantes.CHAVE_TEMPO_TOTAL_MS, 0L));
    }

    private int lerQuestoesCronometradas() {
        return Math.max(0, prefs.getInt(Constantes.CHAVE_TEMPO_QUESTOES, 0));
    }

    // ---------------------------------------------------------------- Sessões

    /** Chamado ao terminar todas as questões de uma lição. Aplica XP, meta, melhor e ofensiva. */
    public ResultadoSessao registrarLicao(Licao licao, int acertos) {
        int total = licao.getQuestoes().size();
        int xp = Gamificacao.calcularXpLicao(acertos);
        int bonus = Gamificacao.calcularBonus(acertos, total);

        ResultadoSessao resultado = new ResultadoSessao();
        resultado.setModo(Constantes.MODO_LICAO);
        resultado.setAcertos(acertos);
        resultado.setTotal(total);
        resultado.setXpGanho(xp);
        resultado.setBonus(bonus);

        SharedPreferences.Editor editor = prefs.edit();
        registrarSessao(editor, resultado, xp + bonus, acertos, total);

        int melhorAnterior = getMelhor(licao.getId());
        editor.putInt(Constantes.PREFIXO_MELHOR + licao.getId(),
                Gamificacao.calcularNovoMelhor(melhorAnterior, acertos));
        resultado.setDesbloqueouProxima(Gamificacao.desbloqueouProxima(
                melhorAnterior, acertos, total, repositorio.getLicaoSeguinte(licao) != null));

        if (bonus > 0) {
            editor.putInt(Constantes.CHAVE_LICOES_PERFEITAS,
                    prefs.getInt(Constantes.CHAVE_LICOES_PERFEITAS, 0) + 1);
        }
        editor.putString(Constantes.CHAVE_ULTIMA_AREA, licao.getArea().name());
        editor.apply();
        resultado.setNovasConquistas(verificarConquistas());
        return resultado;
    }

    /** Chamado ao terminar uma revisão. As vidas já foram devolvidas durante o quiz. */
    public ResultadoSessao registrarRevisao(int acertos, int respondidas, int vidasRecuperadas) {
        int xp = Gamificacao.calcularXpRevisao(acertos);

        ResultadoSessao resultado = new ResultadoSessao();
        resultado.setModo(Constantes.MODO_REVISAO);
        resultado.setAcertos(acertos);
        resultado.setTotal(respondidas);
        resultado.setXpGanho(xp);
        resultado.setVidasRecuperadas(vidasRecuperadas);

        SharedPreferences.Editor editor = prefs.edit();
        registrarSessao(editor, resultado, xp, acertos, respondidas);
        editor.apply();
        resultado.setNovasConquistas(verificarConquistas());
        return resultado;
    }

    /** Parte comum das duas sessões: XP, meta diária, ofensiva e totais. */
    private void registrarSessao(SharedPreferences.Editor editor, ResultadoSessao resultado,
                                 int xpGanho, int acertos, int respondidas) {
        LocalDate hoje = DataUtil.hoje();

        // XP e meta (RN16, RN18, RN19)
        int meta = getMetaDiaria();
        int xpHojeAntes = getXpHoje();
        int xpHojeDepois = xpHojeAntes + xpGanho;
        int xpAntes = getXpTotal();
        editor.putInt(Constantes.CHAVE_XP_TOTAL, xpAntes + xpGanho);
        editor.putInt(Constantes.CHAVE_XP_HOJE, xpHojeDepois);
        editor.putString(Constantes.CHAVE_DATA_XP_HOJE, DataUtil.paraTexto(hoje));
        resultado.setMetaBatidaAgora(Gamificacao.metaBatidaAgora(xpHojeAntes, xpHojeDepois, meta));
        resultado.setNivel(Gamificacao.calcularNivel(xpAntes + xpGanho));
        resultado.setSubiuDeNivel(Celebracoes.subiuDeNivel(xpAntes, xpAntes + xpGanho));

        // Ofensiva (RN26, RN27)
        LocalDate ultima = lerUltimaDataEstudo();
        int ofensivaSalva = prefs.getInt(Constantes.CHAVE_OFENSIVA, 0);
        int ofensiva = Gamificacao.calcularNovaOfensiva(ultima, hoje, ofensivaSalva);
        editor.putInt(Constantes.CHAVE_OFENSIVA, ofensiva);
        editor.putInt(Constantes.CHAVE_MAIOR_OFENSIVA, Math.max(getMaiorOfensiva(), ofensiva));
        editor.putString(Constantes.CHAVE_ULTIMA_DATA_ESTUDO,
                DataUtil.paraTexto(Gamificacao.calcularNovaDataEstudo(ultima, hoje)));
        resultado.setOfensiva(ofensiva);
        // Compara com a ofensiva que o aluno via antes da sessão (já zerada se pulou um dia).
        resultado.setOfensivaAumentou(
                ofensiva > Gamificacao.ofensivaParaExibir(ultima, hoje, ofensivaSalva));

        // Totais para as estatísticas
        editor.putInt(Constantes.CHAVE_TOTAL_RESPONDIDAS, getTotalRespondidas() + respondidas);
        editor.putInt(Constantes.CHAVE_TOTAL_ACERTOS, getTotalAcertos() + acertos);
    }

    // ---------------------------------------------------------------- Conquistas

    /** Ids das conquistas desbloqueadas (cópia: pode alterar à vontade). */
    public Set<String> getConquistas() {
        return new HashSet<>(prefs.getStringSet(Constantes.CHAVE_CONQUISTAS, new HashSet<>()));
    }

    /**
     * RN32/RN33: chamado no fim da sessão, depois de salvar XP e ofensiva. Salva as conquistas
     * novas (uma conquista nunca é perdida) e devolve os ids delas.
     */
    private ArrayList<String> verificarConquistas() {
        Set<String> desbloqueadas = getConquistas();
        Conquistas.Estado estado = new Conquistas.Estado()
                .licoesTerminadas(getTotalLicoesTerminadas())
                .ofensiva(prefs.getInt(Constantes.CHAVE_OFENSIVA, 0))
                .xpTotal(getXpTotal())
                .licoesPerfeitas(prefs.getInt(Constantes.CHAVE_LICOES_PERFEITAS, 0))
                .licoesMatematicaConcluidas(getLicoesConcluidas(Area.MATEMATICA))
                .totalLicoesMatematica(repositorio.getTotalLicoes(Area.MATEMATICA));

        ArrayList<String> novas = new ArrayList<>();
        for (Conquista conquista : Conquistas.novas(estado, desbloqueadas)) {
            novas.add(conquista.getId());
        }
        if (!novas.isEmpty()) {
            desbloqueadas.addAll(novas);
            prefs.edit().putStringSet(Constantes.CHAVE_CONQUISTAS, desbloqueadas).apply();
        }
        return novas;
    }

    /** Lições feitas pelo menos uma vez, com qualquer nota. */
    private int getTotalLicoesTerminadas() {
        int total = 0;
        for (Area area : Area.values()) {
            for (Licao licao : repositorio.getLicoes(area)) {
                if (getMelhor(licao.getId()) >= 0) {
                    total++;
                }
            }
        }
        return total;
    }

    // ---------------------------------------------------------------- Migração

    /**
     * RN37: o progresso salvo com o banco da versão 1 (4 lições de 5) passa para as lições atuais.
     * Conta como concluída a lição nova cujas questões estavam todas em lições concluídas, com a
     * nota mínima (ex.: 2/3); o caderno troca os ids antigos pelos novos. Roda uma vez só.
     */
    private void migrarProgresso() {
        if (prefs.getInt(Constantes.CHAVE_VERSAO_PROGRESSO, 1) >= Constantes.VERSAO_PROGRESSO) {
            return;
        }
        Map<String, String> idsV1 = repositorio.getIdsV1();
        if (idsV1.isEmpty()) {
            return; // o banco não carregou: tenta de novo na próxima abertura
        }
        Map<String, Integer> melhorV1 = new HashMap<>();
        for (String licao : Migracao.licoesV1(idsV1)) {
            if (prefs.contains(Constantes.PREFIXO_MELHOR + licao)) {
                melhorV1.put(licao, prefs.getInt(Constantes.PREFIXO_MELHOR + licao, -1));
            }
        }
        Map<String, Licao> licoes = new HashMap<>();
        Map<String, List<String>> questoesPorLicao = new HashMap<>();
        for (Area area : Area.values()) {
            for (Licao licao : repositorio.getLicoes(area)) {
                List<String> ids = new ArrayList<>();
                for (Questao q : licao.getQuestoes()) {
                    ids.add(q.getId());
                }
                licoes.put(licao.getId(), licao);
                questoesPorLicao.put(licao.getId(), ids);
            }
        }

        // No mesmo editor vale a última operação de cada chave: os remove() vêm antes, então uma
        // lição nova com o mesmo id de uma antiga ("LIN-01") fica com o valor do put().
        SharedPreferences.Editor editor = prefs.edit();
        for (String licao : melhorV1.keySet()) {
            editor.remove(Constantes.PREFIXO_MELHOR + licao);
        }
        for (String id : Migracao.licoesConcluidas(idsV1, melhorV1, questoesPorLicao)) {
            editor.putInt(Constantes.PREFIXO_MELHOR + id,
                    Gamificacao.acertosParaConcluir(licoes.get(id).getQuestoes().size()));
        }
        Set<String> caderno = getCaderno();
        if (!caderno.isEmpty()) {
            editor.putStringSet(Constantes.CHAVE_CADERNO_ERROS, Migracao.converterCaderno(caderno, idsV1));
        }
        editor.putInt(Constantes.CHAVE_VERSAO_PROGRESSO, Constantes.VERSAO_PROGRESSO);
        editor.apply();
    }

    /** RN34: apaga todas as chaves. As questões (assets) não são afetadas. */
    public void resetarTudo() {
        // Sem a versão, a próxima abertura acharia que o progresso novo é da versão 1.
        prefs.edit().clear().putInt(Constantes.CHAVE_VERSAO_PROGRESSO, Constantes.VERSAO_PROGRESSO).apply();
    }
}
