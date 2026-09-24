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
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.ui.adapter.LicaoAdapter;
import com.murilo.enemigo.util.Constantes;

import java.util.List;

/**
 * Trilha de lições de uma área. Por enquanto todas aparecem como disponíveis (estados na Aula 3).
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

        List<Licao> licoes = QuestaoRepository.getInstance(this).getLicoes(area);

        TextView txtResumo = findViewById(R.id.txt_resumo);
        if (licoes.isEmpty()) {
            txtResumo.setText(R.string.trilha_vazia);
        } else {
            txtResumo.setText(getString(R.string.resumo_trilha, LICOES_CONCLUIDAS_MOCK, licoes.size()));
        }

        RecyclerView rvLicoes = findViewById(R.id.rv_licoes);
        rvLicoes.setLayoutManager(new LinearLayoutManager(this));
        rvLicoes.setAdapter(new LicaoAdapter(licoes, corArea, this::abrirQuiz));
    }

    private void abrirQuiz(Licao licao) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra(Constantes.EXTRA_LICAO_ID, licao.getId());
        intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_LICAO);
        startActivity(intent);
    }
}
