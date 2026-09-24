package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.murilo.enemigo.R;
import com.murilo.enemigo.util.Constantes;

/**
 * Resultado da lição. Só exibe o que recebeu nos extras.
 */
public class ResultadoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        Intent recebida = getIntent();
        String licaoId = recebida.getStringExtra(Constantes.EXTRA_LICAO_ID);
        int acertos = recebida.getIntExtra(Constantes.EXTRA_ACERTOS, 0);
        int total = recebida.getIntExtra(Constantes.EXTRA_TOTAL, Constantes.QUESTOES_POR_LICAO);
        int xp = recebida.getIntExtra(Constantes.EXTRA_XP, 0);
        int bonus = recebida.getIntExtra(Constantes.EXTRA_BONUS, 0);

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
                .setText(getString(R.string.resultado_xp, xp));

        if (bonus > 0) {
            TextView txtMensagens = findViewById(R.id.txt_mensagens);
            txtMensagens.setText(getString(R.string.resultado_bonus, bonus));
            txtMensagens.setVisibility(View.VISIBLE);
        }

        // CONTINUAR e o Voltar do sistema fecham esta tela e voltam para a Trilha.
        findViewById(R.id.btn_continuar).setOnClickListener(v -> finish());

        View btnRefazer = findViewById(R.id.btn_refazer);
        if (licaoId == null) {
            btnRefazer.setVisibility(View.GONE);
        } else {
            btnRefazer.setOnClickListener(v -> {
                btnRefazer.setEnabled(false);
                Intent intent = new Intent(this, QuizActivity.class);
                intent.putExtra(Constantes.EXTRA_LICAO_ID, licaoId);
                intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_LICAO);
                startActivity(intent);
                finish();
            });
        }
    }
}
