package com.murilo.enemigo.data;

import android.content.Context;
import android.util.Log;

import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.util.Constantes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lê o assets/questoes.json uma única vez, valida e guarda tudo em memória.
 * Singleton: use getInstance(context).
 */
public final class QuestaoRepository {

    private static final String TAG = "QuestaoRepository";
    private static final String ARQUIVO = "questoes.json";
    private static final int NUM_ALTERNATIVAS = 5;
    private static final char BOM = (char) 0xFEFF;

    private static QuestaoRepository instancia;

    private final Map<Area, List<Licao>> licoesPorArea = new EnumMap<>(Area.class);
    private final Map<String, Licao> licoesPorId = new HashMap<>();
    private final Map<String, Questao> questoesPorId = new HashMap<>();
    private boolean carregadoComSucesso;

    private QuestaoRepository(Context context) {
        for (Area area : Area.values()) {
            licoesPorArea.put(area, new ArrayList<>());
        }
        carregar(context);
    }

    public static synchronized QuestaoRepository getInstance(Context context) {
        if (instancia == null) {
            instancia = new QuestaoRepository(context.getApplicationContext());
        }
        return instancia;
    }

    private void carregar(Context context) {
        try {
            processar(lerArquivo(context));
            carregadoComSucesso = true;
        } catch (IOException e) {
            Log.e(TAG, "Não foi possível ler " + ARQUIVO, e);
            limpar();
        } catch (JSONException e) {
            Log.e(TAG, "Erro de sintaxe em " + ARQUIVO, e);
            limpar();
        }
    }

    private String lerArquivo(Context context) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream in = context.getAssets().open(ARQUIVO);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                sb.append(linha).append('\n');
            }
        }
        // Alguns editores salvam o UTF-8 com BOM, que o JSONObject não aceita.
        if (sb.length() > 0 && sb.charAt(0) == BOM) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /** Converte o texto do JSON nas listas em memória, ignorando o que for inválido. */
    private void processar(String json) throws JSONException {
        JSONObject raiz = new JSONObject(json);
        JSONArray areas = raiz.getJSONArray("areas");
        Set<String> idsQuestoes = new HashSet<>();

        for (int i = 0; i < areas.length(); i++) {
            JSONObject areaJson = areas.getJSONObject(i);
            String areaId = areaJson.optString("id", "");
            Area area = Area.fromId(areaId);
            if (area == null) {
                Log.w(TAG, "Área desconhecida ignorada: " + areaId);
                continue;
            }

            JSONArray licoes = areaJson.optJSONArray("licoes");
            if (licoes == null) {
                continue;
            }
            List<Licao> listaDaArea = licoesPorArea.get(area);
            for (int j = 0; j < licoes.length(); j++) {
                JSONObject licaoJson = licoes.optJSONObject(j);
                if (licaoJson == null) {
                    Log.w(TAG, "Lição inválida na posição " + j + " de " + areaId);
                    continue;
                }
                // A ordem é a posição entre as lições válidas, para a trilha não ter buracos.
                Licao licao = lerLicao(licaoJson, area, listaDaArea.size(), idsQuestoes);
                if (licao != null) {
                    listaDaArea.add(licao);
                    licoesPorId.put(licao.getId(), licao);
                    for (Questao q : licao.getQuestoes()) {
                        questoesPorId.put(q.getId(), q);
                    }
                }
            }
        }
    }

    /** Devolve a lição montada, ou null se ela tiver que ser ignorada. */
    private Licao lerLicao(JSONObject json, Area area, int ordem, Set<String> idsQuestoes) {
        String id = json.optString("id", "").trim();
        if (id.isEmpty() || licoesPorId.containsKey(id)) {
            Log.w(TAG, "Lição com id vazio ou repetido ignorada: " + id);
            return null;
        }
        String titulo = json.optString("titulo", "").trim();

        List<Questao> questoes = new ArrayList<>();
        JSONArray questoesJson = json.optJSONArray("questoes");
        if (questoesJson != null) {
            for (int k = 0; k < questoesJson.length(); k++) {
                JSONObject qJson = questoesJson.optJSONObject(k);
                Questao q = qJson == null ? null : lerQuestao(qJson, area, id, idsQuestoes);
                if (q != null) {
                    questoes.add(q);
                }
            }
        }

        if (questoes.size() < Constantes.MIN_QUESTOES_POR_LICAO
                || questoes.size() > Constantes.MAX_QUESTOES_POR_LICAO) {
            Log.w(TAG, "Lição ignorada (" + questoes.size() + " questões válidas): " + id);
            return null;
        }
        return new Licao(id, area, ordem, titulo, Collections.unmodifiableList(questoes));
    }

    /** Devolve a questão, ou null se for inválida. */
    private Questao lerQuestao(JSONObject json, Area area, String licaoId, Set<String> idsQuestoes) {
        String id = json.optString("id", "").trim();
        String enunciado = json.optString("enunciado", "").trim();
        String letra = json.optString("correta", "").trim();
        JSONArray alternativasJson = json.optJSONArray("alternativas");

        boolean valida = !id.isEmpty()
                && !idsQuestoes.contains(id)
                && !enunciado.isEmpty()
                && alternativasJson != null
                && alternativasJson.length() == NUM_ALTERNATIVAS
                && letra.length() == 1
                && letra.charAt(0) >= 'A' && letra.charAt(0) <= 'E';
        if (!valida) {
            Log.w(TAG, "Questão inválida: " + id);
            return null;
        }

        String[] alternativas = new String[NUM_ALTERNATIVAS];
        for (int i = 0; i < NUM_ALTERNATIVAS; i++) {
            alternativas[i] = alternativasJson.optString(i, "");
        }

        idsQuestoes.add(id);
        return new Questao(
                id,
                area,
                licaoId,
                json.optString("texto", "").trim(),
                enunciado,
                alternativas,
                letra.charAt(0) - 'A',
                json.optString("explicacao", ""),
                json.optString("fonte", ""));
    }

    private void limpar() {
        for (List<Licao> lista : licoesPorArea.values()) {
            lista.clear();
        }
        licoesPorId.clear();
        questoesPorId.clear();
        carregadoComSucesso = false;
    }

    /** Lições da área na ordem da trilha. Nunca devolve null. */
    public List<Licao> getLicoes(Area area) {
        List<Licao> lista = area == null ? null : licoesPorArea.get(area);
        return lista == null ? Collections.emptyList() : Collections.unmodifiableList(lista);
    }

    /** Lição pelo id, ou null se não existir. */
    public Licao getLicao(String id) {
        return id == null ? null : licoesPorId.get(id);
    }

    /** Questão pelo id, ou null se não existir. */
    public Questao getQuestao(String id) {
        return id == null ? null : questoesPorId.get(id);
    }

    /** Próxima lição da mesma área, ou null se esta for a última. */
    public Licao getLicaoSeguinte(Licao licao) {
        List<Licao> lista = getLicoes(licao.getArea());
        int proxima = licao.getOrdem() + 1;
        return proxima < lista.size() ? lista.get(proxima) : null;
    }

    public int getTotalLicoes(Area area) {
        return getLicoes(area).size();
    }

    /** false se o arquivo não existe ou tem erro de sintaxe. */
    public boolean isCarregadoComSucesso() {
        return carregadoComSucesso;
    }
}
