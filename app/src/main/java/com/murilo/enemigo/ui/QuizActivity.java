package com.murilo.enemigo.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.util.Constantes;

import java.util.List;

/**
 * Quiz no modo LICAO: responde as 5 questões, corrige e abre o Resultado.
 * Nada é salvo ainda (Aula 3).
 */
public class QuizActivity extends AppCompatActivity {

    // MOCK: vidas fixas. Na Aula 3 vêm do ProgressoManager.
    private static final int VIDAS_MOCK = 5;

    private static final String ESTADO_INDICE = "indice";
    private static final String ESTADO_SELECIONADA = "selecionada";
    private static final String ESTADO_ACERTOS = "acertos";
    private static final String ESTADO_VERIFICADA = "verificada";

    private final MaterialCardView[] cardsAlternativas = new MaterialCardView[5];

    private String licaoId;
    private List<Questao> questoes;
    private int indiceAtual;
    private int selecionada = -1;
    private int acertos;
    private boolean verificada;

    private ScrollView scroll;
    private LinearProgressIndicator progress;
    private TextView txtContador;
    private View cardTextoBase;
    private TextView txtTextoBase;
    private TextView txtEnunciado;
    private TextView txtFonte;
    private MaterialButton btnVerificar;
    private View painelFeedback;
    private TextView txtFeedbackTitulo;
    private TextView txtExplicacao;
    private MaterialButton btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        licaoId = getIntent().getStringExtra(Constantes.EXTRA_LICAO_ID);
        Licao licao = QuestaoRepository.getInstance(this).getLicao(licaoId);
        if (licao == null || licao.getQuestoes().isEmpty()) {
            Toast.makeText(this, R.string.licao_nao_encontrada, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        questoes = licao.getQuestoes();

        scroll = findViewById(R.id.scroll_quiz);
        progress = findViewById(R.id.progress_quiz);
        txtContador = findViewById(R.id.txt_contador);
        cardTextoBase = findViewById(R.id.card_texto_base);
        txtTextoBase = findViewById(R.id.txt_texto_base);
        txtEnunciado = findViewById(R.id.txt_enunciado);
        txtFonte = findViewById(R.id.txt_fonte);
        btnVerificar = findViewById(R.id.btn_verificar);
        painelFeedback = findViewById(R.id.painel_feedback);
        txtFeedbackTitulo = findViewById(R.id.txt_feedback_titulo);
        txtExplicacao = findViewById(R.id.txt_explicacao);
        btnContinuar = findViewById(R.id.btn_continuar);

        progress.setMax(questoes.size());

        TextView txtVidas = findViewById(R.id.txt_vidas_quiz);
        txtVidas.setText(getString(R.string.status_vidas, VIDAS_MOCK));

        int[] ids = {R.id.alt_a, R.id.alt_b, R.id.alt_c, R.id.alt_d, R.id.alt_e};
        for (int i = 0; i < ids.length; i++) {
            MaterialCardView card = findViewById(ids[i]);
            ((TextView) card.findViewById(R.id.txt_letra)).setText(String.valueOf((char) ('A' + i)));
            final int indice = i;
            card.setOnClickListener(v -> selecionar(indice));
            cardsAlternativas[i] = card;
        }

        btnVerificar.setOnClickListener(v -> verificar());
        btnContinuar.setOnClickListener(v -> continuar());
        findViewById(R.id.btn_fechar).setOnClickListener(v -> confirmarSaida());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmarSaida();
            }
        });

        // Se a tela foi recriada pelo sistema (ex.: troca de tema), retoma de onde estava.
        if (savedInstanceState != null) {
            indiceAtual = Math.min(savedInstanceState.getInt(ESTADO_INDICE, 0), questoes.size() - 1);
            acertos = savedInstanceState.getInt(ESTADO_ACERTOS, 0);
            int salvaSelecionada = savedInstanceState.getInt(ESTADO_SELECIONADA, -1);
            boolean salvaVerificada = savedInstanceState.getBoolean(ESTADO_VERIFICADA, false);
            mostrarQuestao();
            if (salvaSelecionada >= 0) {
                selecionar(salvaSelecionada);
                if (salvaVerificada) {
                    // Os acertos já contam esta questão; mostrarFeedback não soma de novo.
                    mostrarFeedback();
                }
            }
        } else {
            mostrarQuestao();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_INDICE, indiceAtual);
        outState.putInt(ESTADO_SELECIONADA, selecionada);
        outState.putInt(ESTADO_ACERTOS, acertos);
        outState.putBoolean(ESTADO_VERIFICADA, verificada);
    }

    /** Preenche a tela com a questão atual e volta tudo ao estado inicial. */
    private void mostrarQuestao() {
        Questao questao = questoes.get(indiceAtual);
        selecionada = -1;
        verificada = false;

        progress.setProgress(indiceAtual);
        txtContador.setText(getString(R.string.quiz_contador, indiceAtual + 1, questoes.size()));

        if (questao.getTexto().isEmpty()) {
            cardTextoBase.setVisibility(View.GONE);
        } else {
            cardTextoBase.setVisibility(View.VISIBLE);
            txtTextoBase.setText(questao.getTexto());
        }
        txtEnunciado.setText(questao.getEnunciado());
        if (questao.getFonte().isEmpty()) {
            txtFonte.setVisibility(View.GONE);
        } else {
            txtFonte.setVisibility(View.VISIBLE);
            txtFonte.setText(getString(R.string.quiz_fonte, questao.getFonte()));
        }

        String[] alternativas = questao.getAlternativas();
        int cinza = ContextCompat.getColor(this, R.color.cinza_borda);
        int branco = ContextCompat.getColor(this, R.color.fundo);
        for (int i = 0; i < cardsAlternativas.length; i++) {
            MaterialCardView card = cardsAlternativas[i];
            ((TextView) card.findViewById(R.id.txt_texto)).setText(alternativas[i]);
            aplicarEstilo(card, cinza, branco);
            card.setClickable(true);
        }

        painelFeedback.setVisibility(View.GONE);
        btnVerificar.setVisibility(View.VISIBLE);
        btnVerificar.setEnabled(false);
        btnContinuar.setEnabled(true);
        scroll.scrollTo(0, 0);
    }

    /** Pinta a alternativa escolhida de azul e volta as outras ao normal. */
    private void selecionar(int indice) {
        if (verificada) {
            return;
        }
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

    private void verificar() {
        if (verificada || selecionada < 0) {
            return;
        }
        // Desabilita na hora para evitar clique duplo.
        btnVerificar.setEnabled(false);
        if (questoes.get(indiceAtual).isCorreta(selecionada)) {
            acertos++;
        }
        mostrarFeedback();
    }

    /** Trava as alternativas, pinta certo/errado e mostra o painel com a explicação. */
    private void mostrarFeedback() {
        verificada = true;
        Questao questao = questoes.get(indiceAtual);
        boolean acertou = questao.isCorreta(selecionada);

        for (MaterialCardView card : cardsAlternativas) {
            card.setClickable(false);
        }

        int verde = ContextCompat.getColor(this, R.color.verde_primario);
        int verdeClaro = ContextCompat.getColor(this, R.color.verde_claro);
        aplicarEstilo(cardsAlternativas[questao.getCorreta()], verde, verdeClaro);

        int corPainel;
        int corTitulo;
        int corBotao;
        if (acertou) {
            corPainel = verdeClaro;
            corTitulo = ContextCompat.getColor(this, R.color.verde_escuro);
            corBotao = verde;
            txtFeedbackTitulo.setText(R.string.feedback_acerto);
        } else {
            int vermelho = ContextCompat.getColor(this, R.color.vermelho);
            int vermelhoClaro = ContextCompat.getColor(this, R.color.vermelho_claro);
            aplicarEstilo(cardsAlternativas[selecionada], vermelho, vermelhoClaro);
            corPainel = vermelhoClaro;
            corTitulo = ContextCompat.getColor(this, R.color.vermelho_escuro);
            corBotao = vermelho;
            txtFeedbackTitulo.setText(getString(R.string.feedback_erro, questao.getLetraCorreta()));
        }

        painelFeedback.setBackgroundColor(corPainel);
        txtFeedbackTitulo.setTextColor(corTitulo);
        btnContinuar.setBackgroundTintList(ColorStateList.valueOf(corBotao));
        txtExplicacao.setText(questao.getExplicacao());

        btnVerificar.setVisibility(View.GONE);
        painelFeedback.setVisibility(View.VISIBLE);
    }

    private void continuar() {
        if (indiceAtual < questoes.size() - 1) {
            indiceAtual++;
            mostrarQuestao();
        } else {
            btnContinuar.setEnabled(false);
            abrirResultado();
        }
    }

    private void abrirResultado() {
        int total = questoes.size();
        int xp = acertos * Constantes.XP_ACERTO;
        int bonus = acertos == total ? Constantes.XP_BONUS_PERFEITA : 0;

        Intent intent = new Intent(this, ResultadoActivity.class);
        intent.putExtra(Constantes.EXTRA_LICAO_ID, licaoId);
        intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_LICAO);
        intent.putExtra(Constantes.EXTRA_ACERTOS, acertos);
        intent.putExtra(Constantes.EXTRA_TOTAL, total);
        intent.putExtra(Constantes.EXTRA_XP, xp);
        intent.putExtra(Constantes.EXTRA_BONUS, bonus);
        startActivity(intent);
        finish();
    }

    private void confirmarSaida() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sair_licao_titulo)
                .setMessage(R.string.sair_licao_mensagem)
                .setPositiveButton(R.string.sair_licao_sair, (dialog, which) -> finish())
                .setNegativeButton(R.string.sair_licao_ficar, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void aplicarEstilo(MaterialCardView card, int corBorda, int corFundo) {
        card.setStrokeColor(corBorda);
        card.setCardBackgroundColor(corFundo);
    }
}
