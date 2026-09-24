package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.EstadoLicao;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.ui.adapter.LicaoAdapter;
import com.murilo.enemigo.util.Constantes;

import java.util.List;

/**
 * Trilha de lições de uma área, com os estados calculados a partir do progresso salvo.
 */
public class TrilhaActivity extends AppCompatActivity {

    private Area area;
    private List<Licao> licoes;
    private ProgressoManager progresso;
    private LicaoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trilha);

        area = Area.fromId(getIntent().getStringExtra(Constantes.EXTRA_AREA_ID));
        if (area == null) {
            Toast.makeText(this, R.string.area_nao_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progresso = ProgressoManager.getInstance(this);
        int corArea = ContextCompat.getColor(this, area.getCorRes());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(area.getNome());
        toolbar.setBackgroundColor(corArea);
        toolbar.setNavigationOnClickListener(v -> finish());

        licoes = QuestaoRepository.getInstance(this).getLicoes(area);
        adapter = new LicaoAdapter(licoes, corArea, progresso, this::aoTocarLicao);

        RecyclerView rvLicoes = findViewById(R.id.rv_licoes);
        rvLicoes.setLayoutManager(new LinearLayoutManager(this));
        rvLicoes.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BarraStatus.atualizar(findViewById(R.id.status));

        TextView txtResumo = findViewById(R.id.txt_resumo);
        if (licoes.isEmpty()) {
            txtResumo.setText(R.string.trilha_vazia);
        } else {
            txtResumo.setText(getString(R.string.resumo_trilha,
                    progresso.getLicoesConcluidas(area), licoes.size()));
        }
        // Os estados podem ter mudado depois de uma lição.
        adapter.notifyItemRangeChanged(0, licoes.size());
    }

    private void aoTocarLicao(Licao licao, EstadoLicao estado) {
        if (estado == EstadoLicao.BLOQUEADA) {
            Toast.makeText(this, R.string.licao_bloqueada_toast, Toast.LENGTH_SHORT).show();
        } else if (progresso.getVidas() == 0) {
            Dialogos.mostrarSemVidas(this);
        } else {
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra(Constantes.EXTRA_LICAO_ID, licao.getId());
            intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_LICAO);
            startActivity(intent);
        }
    }
}
