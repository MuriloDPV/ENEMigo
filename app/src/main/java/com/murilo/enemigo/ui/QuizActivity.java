package com.murilo.enemigo.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.util.Constantes;

/**
 * Quiz. Na Aula 1 mostra uma questão fixa e só faz a seleção visual (sem corrigir).
 */
public class QuizActivity extends AppCompatActivity {

    // MOCK (Aula 1): vidas fixas. Na Aula 3 vêm do ProgressoManager.
    private static final int VIDAS_MOCK = 5;

    private final MaterialCardView[] cardsAlternativas = new MaterialCardView[5];
    private MaterialButton btnVerificar;
    private int selecionada = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Questao questao = criarQuestaoFixa();

        findViewById(R.id.btn_fechar).setOnClickListener(v -> finish());

        LinearProgressIndicator progress = findViewById(R.id.progress_quiz);
        progress.setMax(Constantes.QUESTOES_POR_LICAO);
        progress.setProgress(0);

        TextView txtVidas = findViewById(R.id.txt_vidas_quiz);
        txtVidas.setText(getString(R.string.status_vidas, VIDAS_MOCK));

        TextView txtContador = findViewById(R.id.txt_contador);
        txtContador.setText(getString(R.string.quiz_contador, 1, Constantes.QUESTOES_POR_LICAO));

        View cardTextoBase = findViewById(R.id.card_texto_base);
        TextView txtTextoBase = findViewById(R.id.txt_texto_base);
        if (questao.getTexto() == null || questao.getTexto().isEmpty()) {
            cardTextoBase.setVisibility(View.GONE);
        } else {
            cardTextoBase.setVisibility(View.VISIBLE);
            txtTextoBase.setText(questao.getTexto());
        }

        ((TextView) findViewById(R.id.txt_enunciado)).setText(questao.getEnunciado());
        ((TextView) findViewById(R.id.txt_fonte))
                .setText(getString(R.string.quiz_fonte, questao.getFonte()));

        btnVerificar = findViewById(R.id.btn_verificar);
        btnVerificar.setEnabled(false);

        int[] ids = {R.id.alt_a, R.id.alt_b, R.id.alt_c, R.id.alt_d, R.id.alt_e};
        for (int i = 0; i < ids.length; i++) {
            MaterialCardView card = findViewById(ids[i]);
            ((TextView) card.findViewById(R.id.txt_letra)).setText(String.valueOf((char) ('A' + i)));
            ((TextView) card.findViewById(R.id.txt_texto)).setText(questao.getAlternativas()[i]);

            final int indice = i;
            card.setOnClickListener(v -> selecionar(indice));
            cardsAlternativas[i] = card;
        }
    }

    /** Pinta a alternativa escolhida de azul e volta as outras ao normal. */
    private void selecionar(int indice) {
        selecionada = indice;
        int cinza = ContextCompat.getColor(this, R.color.cinza_borda);
        int branco = ContextCompat.getColor(this, R.color.fundo);
        int azul = ContextCompat.getColor(this, R.color.azul_selecao);
        int azulClaro = ContextCompat.getColor(this, R.color.azul_claro);

        for (int i = 0; i < cardsAlternativas.length; i++) {
            if (i == selecionada) {
                aplicarEstilo(cardsAlternativas[i], azul, azulClaro);
            } else {
                aplicarEstilo(cardsAlternativas[i], cinza, branco);
            }
        }
        btnVerificar.setEnabled(true);
    }

    private void aplicarEstilo(MaterialCardView card, int corBorda, int corFundo) {
        card.setStrokeColor(corBorda);
        card.setCardBackgroundColor(corFundo);
    }

    /** MOCK (Aula 1): questão fixa. Na Aula 2 vem do QuestaoRepository pelo EXTRA_LICAO_ID. */
    private Questao criarQuestaoFixa() {
        return new Questao(
                "MAT-01-01",
                Area.MATEMATICA,
                "MAT-01",
                "",
                "Uma loja anunciou uma promoção: todos os produtos com 20% de desconto e, "
                        + "para pagamento via Pix, mais 10% de desconto sobre o preço já promocional. "
                        + "Um cliente comprou, pagando via Pix, um tênis que custava R$ 250,00 antes "
                        + "da promoção. Quanto ele pagou pelo tênis?",
                new String[]{"R$ 175,00", "R$ 180,00", "R$ 187,50", "R$ 195,00", "R$ 225,00"},
                1,
                "Descontos sucessivos se multiplicam: 250 × 0,80 = 200 e 200 × 0,90 = 180. "
                        + "O erro comum é somar os descontos (30%), o que daria R$ 175,00.",
                "Autoral (estilo ENEM)"
        );
    }
}
