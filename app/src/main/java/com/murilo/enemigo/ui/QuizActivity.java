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
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.model.ResultadoSessao;
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.TextoRico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quiz nos modos LICAO (5 questões da lição, erro tira vida) e REVISAO (até 5 questões do
 * caderno de erros, acerto devolve vida). No fim registra a sessão e abre o Resultado.
 */
public class QuizActivity extends AppCompatActivity {

    private static final String ESTADO_INDICE = "indice";
    private static final String ESTADO_SELECIONADA = "selecionada";
    private static final String ESTADO_ACERTOS = "acertos";
    private static final String ESTADO_VERIFICADA = "verificada";
    private static final String ESTADO_IDS_REVISAO = "ids_revisao";
    private static final String ESTADO_VIDAS_RECUPERADAS = "vidas_recuperadas";

    private final MaterialCardView[] cardsAlternativas = new MaterialCardView[5];

    private ProgressoManager progresso;
    private boolean modoRevisao;
    private Licao licao;
    private List<Questao> questoes;
    private int indiceAtual;
    private int selecionada = -1;
    private int acertos;
    private boolean verificada;
    private int vidasRecuperadas;

    private ScrollView scroll;
    private LinearProgressIndicator progress;
    private TextView txtContador;
    private TextView txtVidas;
    private View cardTextoBase;
    private TextView txtTextoBase;
    private TextView txtDicaFigura;
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

        progresso = ProgressoManager.getInstance(this);
        modoRevisao = Constantes.MODO_REVISAO.equals(getIntent().getStringExtra(Constantes.EXTRA_MODO));

        if (modoRevisao) {
            // Na recriação da tela, mantém as mesmas questões sorteadas.
            ArrayList<String> ids = savedInstanceState == null
                    ? null : savedInstanceState.getStringArrayList(ESTADO_IDS_REVISAO);
            questoes = ids == null ? sortearRevisao() : buscarQuestoes(ids);
            if (questoes.isEmpty()) {
                Toast.makeText(this, R.string.caderno_vazio_toast, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            licao = QuestaoRepository.getInstance(this)
                    .getLicao(getIntent().getStringExtra(Constantes.EXTRA_LICAO_ID));
            if (licao == null || licao.getQuestoes().isEmpty()) {
                Toast.makeText(this, R.string.licao_nao_encontrada, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            questoes = licao.getQuestoes();
        }

        scroll = findViewById(R.id.scroll_quiz);
        progress = findViewById(R.id.progress_quiz);
        txtContador = findViewById(R.id.txt_contador);
        txtVidas = findViewById(R.id.txt_vidas_quiz);
        cardTextoBase = findViewById(R.id.card_texto_base);
        txtTextoBase = findViewById(R.id.txt_texto_base);
        txtDicaFigura = findViewById(R.id.txt_dica_figura);
        txtEnunciado = findViewById(R.id.txt_enunciado);
        txtFonte = findViewById(R.id.txt_fonte);
        btnVerificar = findViewById(R.id.btn_verificar);
        painelFeedback = findViewById(R.id.painel_feedback);
        txtFeedbackTitulo = findViewById(R.id.txt_feedback_titulo);
        txtExplicacao = findViewById(R.id.txt_explicacao);
        btnContinuar = findViewById(R.id.btn_continuar);

        progress.setMax(questoes.size());

        // Na revisão errar não tira vida, então o contador fica escondido.
        txtVidas.setVisibility(modoRevisao ? View.GONE : View.VISIBLE);
        atualizarVidas();

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
            vidasRecuperadas = savedInstanceState.getInt(ESTADO_VIDAS_RECUPERADAS, 0);
            int salvaSelecionada = savedInstanceState.getInt(ESTADO_SELECIONADA, -1);
            boolean salvaVerificada = savedInstanceState.getBoolean(ESTADO_VERIFICADA, false);
            mostrarQuestao();
            if (salvaSelecionada >= 0) {
                selecionar(salvaSelecionada);
                if (salvaVerificada) {
                    // Acertos, vidas e caderno já foram atualizados; mostrarFeedback só pinta.
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
        outState.putInt(ESTADO_VIDAS_RECUPERADAS, vidasRecuperadas);
        if (modoRevisao && questoes != null) {
            ArrayList<String> ids = new ArrayList<>();
            for (Questao q : questoes) {
                ids.add(q.getId());
            }
            outState.putStringArrayList(ESTADO_IDS_REVISAO, ids);
        }
    }

    /** RN30: até 5 questões sorteadas do caderno. RN31: ids que não existem mais são descartados. */
    private List<Questao> sortearRevisao() {
        QuestaoRepository repositorio = QuestaoRepository.getInstance(this);
        List<Questao> lista = new ArrayList<>();
        for (String id : progresso.getCaderno()) {
            Questao q = repositorio.getQuestao(id);
            if (q == null) {
                progresso.removerDoCaderno(id);
            } else {
                lista.add(q);
            }
        }
        Collections.shuffle(lista);
        return new ArrayList<>(lista.subList(0, Math.min(Constantes.QUESTOES_POR_REVISAO, lista.size())));
    }

    private List<Questao> buscarQuestoes(List<String> ids) {
        QuestaoRepository repositorio = QuestaoRepository.getInstance(this);
        List<Questao> lista = new ArrayList<>();
        for (String id : ids) {
            Questao q = repositorio.getQuestao(id);
            if (q != null) {
                lista.add(q);
            }
        }
        return lista;
    }

    private void atualizarVidas() {
        if (!modoRevisao) {
            txtVidas.setText(getString(R.string.status_vidas, progresso.getVidas()));
        }
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
            TextoComImagens.exibir(txtTextoBase, questao.getTexto(), false);
        }
        TextoComImagens.exibir(txtEnunciado, questao.getEnunciado(), false);
        boolean temFigura = TextoRico.temFigura(questao.getTexto())
                || TextoRico.temFigura(questao.getEnunciado());
        txtDicaFigura.setVisibility(temFigura ? View.VISIBLE : View.GONE);
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
            TextoComImagens.exibir(card.findViewById(R.id.txt_texto), alternativas[i], true);
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
        Questao questao = questoes.get(indiceAtual);
        if (questao.isCorreta(selecionada)) {
            acertos++;
            if (modoRevisao) {
                // RN25: sai do caderno e devolve 1 vida (sem passar de 5).
                progresso.removerDoCaderno(questao.getId());
                if (progresso.getVidas() < Constantes.VIDAS_MAX) {
                    progresso.ganharVida();
                    vidasRecuperadas++;
                }
            }
        } else if (!modoRevisao) {
            // RN11: a vida sai na hora, para não dar para "fugir" fechando o app.
            progresso.perderVida();
            progresso.adicionarAoCaderno(questao.getId());
            atualizarVidas();
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
            // RN13: sem vidas no meio da lição, ela termina sem ser registrada (RN12).
            if (!modoRevisao && progresso.getVidas() == 0) {
                mostrarVidasAcabaram();
                return;
            }
            indiceAtual++;
            mostrarQuestao();
        } else {
            btnContinuar.setEnabled(false);
            abrirResultado();
        }
    }

    private void mostrarVidasAcabaram() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.vidas_acabaram_titulo)
                .setMessage(R.string.vidas_acabaram_mensagem)
                .setCancelable(false)
                .setPositiveButton(R.string.btn_ir_revisao, (dialog, which) -> {
                    Dialogos.abrirRevisao(this);
                    finish();
                })
                .setNegativeButton(R.string.sair_licao_sair, (dialog, which) -> finish())
                .show();
    }

    /** Registra a sessão (XP, ofensiva, meta, desbloqueio) e passa o resumo para o Resultado. */
    private void abrirResultado() {
        ResultadoSessao resultado = modoRevisao
                ? progresso.registrarRevisao(acertos, questoes.size(), vidasRecuperadas)
                : progresso.registrarLicao(licao, acertos);

        Intent intent = new Intent(this, ResultadoActivity.class);
        if (licao != null) {
            intent.putExtra(Constantes.EXTRA_LICAO_ID, licao.getId());
        }
        intent.putExtra(Constantes.EXTRA_MODO, resultado.getModo());
        intent.putExtra(Constantes.EXTRA_ACERTOS, resultado.getAcertos());
        intent.putExtra(Constantes.EXTRA_TOTAL, resultado.getTotal());
        intent.putExtra(Constantes.EXTRA_XP, resultado.getXpGanho());
        intent.putExtra(Constantes.EXTRA_BONUS, resultado.getBonus());
        intent.putExtra(Constantes.EXTRA_OFENSIVA, resultado.getOfensiva());
        intent.putExtra(Constantes.EXTRA_META_BATIDA, resultado.isMetaBatidaAgora());
        intent.putExtra(Constantes.EXTRA_DESBLOQUEOU, resultado.isDesbloqueouProxima());
        intent.putExtra(Constantes.EXTRA_VIDAS_RECUPERADAS, resultado.getVidasRecuperadas());
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
