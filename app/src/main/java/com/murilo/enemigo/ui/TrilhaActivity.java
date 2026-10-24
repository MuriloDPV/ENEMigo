package com.murilo.enemigo.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.EstadoLicao;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.ui.adapter.LicaoAdapter;
import com.murilo.enemigo.ui.adapter.LinhaTrilha;
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
        int corSobre = ContextCompat.getColor(this, area.getCorSobreRes());

        // Cabeçalho e barra de status na cor da área; ícones escuros quando o texto da área é escuro.
        findViewById(R.id.cabecalho).setBackgroundColor(corArea);
        getWindow().setStatusBarColor(corArea);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(ColorUtils.calculateLuminance(corSobre) < 0.5);

        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        btnVoltar.setImageTintList(ColorStateList.valueOf(corSobre));
        btnVoltar.setOnClickListener(v -> finish());

        TextView txtTitulo = findViewById(R.id.txt_titulo);
        txtTitulo.setText(area.getNome());
        txtTitulo.setTextColor(corSobre);
        ((TextView) findViewById(R.id.txt_resumo)).setTextColor(ColorUtils.setAlphaComponent(corSobre, 220));

        LinearProgressIndicator progressTrilha = findViewById(R.id.progress_trilha);
        progressTrilha.setIndicatorColor(corSobre);
        progressTrilha.setTrackColor(ColorUtils.setAlphaComponent(corSobre, 60));

        licoes = QuestaoRepository.getInstance(this).getLicoes(area);
        adapter = new LicaoAdapter(licoes, area, progresso, this::aoTocarLicao);

        RecyclerView rvLicoes = findViewById(R.id.rv_licoes);
        rvLicoes.setLayoutManager(new LinearLayoutManager(this));
        rvLicoes.addItemDecoration(new LinhaTrilha(this, corArea));
        rvLicoes.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BarraStatus.atualizar(findViewById(R.id.status));

        TextView txtResumo = findViewById(R.id.txt_resumo);
        LinearProgressIndicator progressTrilha = findViewById(R.id.progress_trilha);
        int concluidas = progresso.getLicoesConcluidas(area);
        if (licoes.isEmpty()) {
            txtResumo.setText(R.string.trilha_vazia);
        } else {
            txtResumo.setText(getString(R.string.resumo_trilha, concluidas, licoes.size()));
        }
        progressTrilha.setMax(Math.max(1, licoes.size()));
        progressTrilha.setProgressCompat(concluidas, true);
        // Os estados podem ter mudado depois de uma lição.
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.pararAnimacoes();
        }
    }

    private void aoTocarLicao(Licao licao, EstadoLicao estado) {
        if (estado == EstadoLicao.BLOQUEADA) {
            Toast.makeText(this, getString(R.string.licao_bloqueada_toast,
                    Constantes.PERCENTUAL_PARA_CONCLUIR), Toast.LENGTH_SHORT).show();
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
