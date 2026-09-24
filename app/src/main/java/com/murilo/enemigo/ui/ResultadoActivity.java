package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.util.Constantes;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado da sessão. Só exibe o que recebeu nos extras: o progresso já foi salvo pela Quiz.
 */
public class ResultadoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        Intent recebida = getIntent();
        boolean modoRevisao = Constantes.MODO_REVISAO.equals(
                recebida.getStringExtra(Constantes.EXTRA_MODO));
        String licaoId = recebida.getStringExtra(Constantes.EXTRA_LICAO_ID);
        int acertos = recebida.getIntExtra(Constantes.EXTRA_ACERTOS, 0);
        int total = recebida.getIntExtra(Constantes.EXTRA_TOTAL, Constantes.QUESTOES_POR_LICAO);
        int xp = recebida.getIntExtra(Constantes.EXTRA_XP, 0);
        int bonus = recebida.getIntExtra(Constantes.EXTRA_BONUS, 0);
        int ofensiva = recebida.getIntExtra(Constantes.EXTRA_OFENSIVA, 0);
        boolean metaBatida = recebida.getBooleanExtra(Constantes.EXTRA_META_BATIDA, false);
        boolean desbloqueou = recebida.getBooleanExtra(Constantes.EXTRA_DESBLOQUEOU, false);
        int vidasRecuperadas = recebida.getIntExtra(Constantes.EXTRA_VIDAS_RECUPERADAS, 0);

        TextView txtEmoji = findViewById(R.id.txt_emoji);
        TextView txtTitulo = findViewById(R.id.txt_titulo);
        if (acertos == total) {
            txtEmoji.setText(R.string.resultado_emoji_perfeito);
            txtTitulo.setText(R.string.resultado_perfeito);
        } else if (acertos >= Constantes.ACERTOS_PARA_DESBLOQUEAR) {
            txtEmoji.setText(R.string.resultado_emoji_concluida);
            txtTitulo.setText(R.string.resultado_concluida);
        } else {
            txtEmoji.setText(R.string.resultado_emoji_quase);
            txtTitulo.setText(R.string.resultado_quase);
        }

        ((TextView) findViewById(R.id.txt_acertos))
                .setText(getString(R.string.resultado_acertos, acertos, total));
        ((TextView) findViewById(R.id.txt_xp_ganho))
                .setText(getString(R.string.resultado_xp, xp + bonus));

        ((TextView) findViewById(R.id.txt_ofensiva_resultado)).setText(getResources()
                .getQuantityString(R.plurals.resultado_ofensiva, ofensiva, ofensiva));

        // Mensagens, uma por linha
        List<String> mensagens = new ArrayList<>();
        if (bonus > 0) {
            mensagens.add(getString(R.string.resultado_bonus, bonus));
        }
        if (metaBatida) {
            mensagens.add(getString(R.string.resultado_meta_batida));
        }
        if (modoRevisao) {
            mensagens.add(mensagemRevisao(acertos, vidasRecuperadas));
        } else if (desbloqueou) {
            mensagens.add(getString(R.string.resultado_desbloqueou));
        } else if (acertos < Constantes.ACERTOS_PARA_DESBLOQUEAR && temProximaLicao(licaoId)) {
            mensagens.add(getString(R.string.resultado_faca_acertos));
        }
        if (!mensagens.isEmpty()) {
            TextView txtMensagens = findViewById(R.id.txt_mensagens);
            txtMensagens.setText(String.join("\n", mensagens));
            txtMensagens.setVisibility(View.VISIBLE);
        }

        // CONTINUAR e o Voltar do sistema fecham esta tela e voltam para a Trilha ou a Revisão.
        findViewById(R.id.btn_continuar).setOnClickListener(v -> finish());

        View btnRefazer = findViewById(R.id.btn_refazer);
        if (modoRevisao || licaoId == null) {
            btnRefazer.setVisibility(View.GONE);
        } else {
            btnRefazer.setOnClickListener(v -> refazer(licaoId));
        }
    }

    private void refazer(String licaoId) {
        if (ProgressoManager.getInstance(this).getVidas() == 0) {
            Dialogos.mostrarSemVidas(this);
            return;
        }
        findViewById(R.id.btn_refazer).setEnabled(false);
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra(Constantes.EXTRA_LICAO_ID, licaoId);
        intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_LICAO);
        startActivity(intent);
        finish();
    }

    private String mensagemRevisao(int removidas, int vidas) {
        if (removidas == 0) {
            return getString(R.string.resultado_revisao_nenhuma);
        }
        String questoes = getResources().getQuantityString(R.plurals.qtd_questoes, removidas, removidas);
        if (vidas == 0) {
            return getString(R.string.resultado_revisao_sem_vidas, questoes);
        }
        return getString(R.string.resultado_revisao, questoes,
                getResources().getQuantityString(R.plurals.qtd_vidas, vidas, vidas));
    }

    private boolean temProximaLicao(String licaoId) {
        QuestaoRepository repositorio = QuestaoRepository.getInstance(this);
        Licao licao = repositorio.getLicao(licaoId);
        return licao != null && repositorio.getLicaoSeguinte(licao) != null;
    }
}
