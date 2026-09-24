package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.ui.adapter.LicaoAdapter;
import com.murilo.enemigo.util.Constantes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Trilha de lições de uma área.
 */
public class TrilhaActivity extends AppCompatActivity {

    // MOCK (Aula 1): valores fixos. Na Aula 3 vêm do ProgressoManager.
    private static final int OFENSIVA_MOCK = 3;
    private static final int XP_TOTAL_MOCK = 240;
    private static final int VIDAS_MOCK = 5;
    private static final int LICOES_CONCLUIDAS_MOCK = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trilha);

        Area area = Area.fromId(getIntent().getStringExtra(Constantes.EXTRA_AREA_ID));
        if (area == null) {
            Toast.makeText(this, R.string.area_nao_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int corArea = ContextCompat.getColor(this, area.getCorRes());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(area.getNome());
        toolbar.setBackgroundColor(corArea);
        toolbar.setNavigationOnClickListener(v -> finish());

        View status = findViewById(R.id.status);
        ((TextView) status.findViewById(R.id.txt_ofensiva))
                .setText(getString(R.string.status_ofensiva, OFENSIVA_MOCK));
        ((TextView) status.findViewById(R.id.txt_xp))
                .setText(getString(R.string.status_xp, XP_TOTAL_MOCK));
        ((TextView) status.findViewById(R.id.txt_vidas))
                .setText(getString(R.string.status_vidas, VIDAS_MOCK));

        List<Licao> licoes = criarLicoesFalsas(area);

        TextView txtResumo = findViewById(R.id.txt_resumo);
        txtResumo.setText(getString(R.string.resumo_trilha, LICOES_CONCLUIDAS_MOCK, licoes.size()));

        RecyclerView rvLicoes = findViewById(R.id.rv_licoes);
        rvLicoes.setLayoutManager(new LinearLayoutManager(this));
        rvLicoes.setAdapter(new LicaoAdapter(licoes, corArea, licao ->
                startActivity(new Intent(this, QuizActivity.class))));
    }

    /** MOCK (Aula 1): 3 lições sem questões. Na Aula 2 vêm do QuestaoRepository. */
    private List<Licao> criarLicoesFalsas(Area area) {
        String[] titulos;
        switch (area) {
            case LINGUAGENS:
                titulos = new String[]{"Variação linguística", "Gêneros textuais", "Figuras de linguagem"};
                break;
            case HUMANAS:
                titulos = new String[]{"Brasil Colônia", "Cidadania e direitos", "Globalização"};
                break;
            case NATUREZA:
                titulos = new String[]{"Mudanças de estado físico", "Ecologia", "Eletricidade"};
                break;
            default:
                titulos = new String[]{"Porcentagem", "Interpretação de gráficos", "Funções"};
                break;
        }

        List<Licao> licoes = new ArrayList<>();
        for (int i = 0; i < titulos.length; i++) {
            String id = String.format(Locale.ROOT, "%s-%02d", area.getSigla(), i + 1);
            licoes.add(new Licao(id, area, i, titulos[i], new ArrayList<>()));
        }
        return licoes;
    }
}
