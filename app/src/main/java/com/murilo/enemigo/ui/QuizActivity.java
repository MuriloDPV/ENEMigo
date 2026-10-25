package com.murilo.enemigo.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.model.ResultadoSessao;
import com.murilo.enemigo.ui.componente.BotaoRelevo;
import com.murilo.enemigo.ui.componente.CartaoRelevo;
import com.murilo.enemigo.ui.componente.MascoteView;
import com.murilo.enemigo.util.Celebracoes;
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.TextoRico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quiz nos modos LICAO (as questões da lição, erro tira vida) e REVISAO (até 5 questões do
 * caderno de erros, acerto devolve vida). No fim registra a sessão e abre o Resultado.
 */
public class QuizActivity extends AppCompatActivity {

    private static final String ESTADO_INDICE = "indice";
    private static final String ESTADO_SELECIONADA = "selecionada";
    private static final String ESTADO_ACERTOS = "acertos";
    private static final String ESTADO_VERIFICADA = "verificada";
    private static final String ESTADO_IDS_REVISAO = "ids_revisao";
    private static final String ESTADO_VIDAS_RECUPERADAS = "vidas_recuperadas";
    private static final String ESTADO_TEMPO_QUESTAO = "tempo_questao";
    private static final String ESTADO_TEXTO_RECOLHIDO = "texto_recolhido";
    /** Itens do menu do "Aa", na ordem dos níveis de TamanhoLetra (pequena, média, grande). */
    private static final int[] ITENS_LETRA = {R.id.letra_pequena, R.id.letra_media, R.id.letra_grande};

    // Estados visuais de uma alternativa
    private static final int NORMAL = 0;
    private static final int SELECIONADA = 1;
    private static final int CORRETA = 2;
    private static final int ERRADA = 3;

    private final CartaoRelevo[] cardsAlternativas = new CartaoRelevo[5];
    private final TextView[] textosAlternativas = new TextView[5];
    /** Alternativas com figura ou fórmula ficam sempre em "papel" claro (inclusive no modo escuro). */
    private final boolean[] alternativaEmPapel = new boolean[5];

    private ProgressoManager progresso;
    private PreferenciasManager preferencias;
    private boolean modoRevisao;
    private Licao licao;
    private List<Questao> questoes;
    private int indiceAtual;
    private int selecionada = -1;
    private int acertos;
    private boolean verificada;
    private int vidasRecuperadas;
    /** O aluno recolheu o texto-base desta questão (a próxima começa com ele aberto). */
    private boolean textoRecolhido;
    // Cronômetro da questão atual (RN38): só conta com a tela aberta e para no VERIFICAR.
    private long tempoQuestaoMs;
    private long retomadoEm = -1;

    private ScrollView scroll;
    private LinearProgressIndicator progress;
    private TextView txtContador;
    private TextView txtVidas;
    private View cardTextoBase;
    private TextView txtTextoBase;
    private View btnRecolherTexto;
    private View faixaTextoRecolhido;
    private TextView txtTextoRecolhido;
    private TextView txtDicaFigura;
    private TextView txtEnunciado;
    private TextView txtFonte;
    private BotaoRelevo btnVerificar;
    private View divisorInferior;
    private View painelFeedback;
    private MascoteView mascoteFeedback;
    private Retorno retorno;
    private TextView txtFeedbackTitulo;
    private TextView txtExplicacao;
    private BotaoRelevo btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        progresso = ProgressoManager.getInstance(this);
        preferencias = PreferenciasManager.getInstance(this);
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
        btnRecolherTexto = findViewById(R.id.btn_recolher_texto);
        faixaTextoRecolhido = findViewById(R.id.faixa_texto_recolhido);
        txtTextoRecolhido = findViewById(R.id.txt_texto_recolhido);
        txtDicaFigura = findViewById(R.id.txt_dica_figura);
        txtEnunciado = findViewById(R.id.txt_enunciado);
        txtFonte = findViewById(R.id.txt_fonte);
        btnVerificar = findViewById(R.id.btn_verificar);
        divisorInferior = findViewById(R.id.divisor_inferior);
        painelFeedback = findViewById(R.id.painel_feedback);
        mascoteFeedback = findViewById(R.id.mascote_feedback);
        retorno = new Retorno(this);
        txtFeedbackTitulo = findViewById(R.id.txt_feedback_titulo);
        txtExplicacao = findViewById(R.id.txt_explicacao);
        btnContinuar = findViewById(R.id.btn_continuar);

        progress.setMax(questoes.size());

        // Na revisão errar não tira vida, então o contador fica escondido.
        txtVidas.setVisibility(modoRevisao ? View.GONE : View.VISIBLE);
        atualizarVidas();

        int[] ids = {R.id.alt_a, R.id.alt_b, R.id.alt_c, R.id.alt_d, R.id.alt_e};
        for (int i = 0; i < ids.length; i++) {
            CartaoRelevo card = findViewById(ids[i]);
            ((TextView) card.findViewById(R.id.txt_letra)).setText(String.valueOf((char) ('A' + i)));
            final int indice = i;
            card.setOnClickListener(v -> selecionar(indice));
            cardsAlternativas[i] = card;
            textosAlternativas[i] = card.findViewById(R.id.txt_texto);
        }
        aplicarTamanhoLetra();

        btnVerificar.setOnClickListener(v -> verificar());
        btnContinuar.setOnClickListener(v -> continuar());
        btnRecolherTexto.setOnClickListener(v -> recolherTexto(true));
        findViewById(R.id.btn_tamanho_letra).setOnClickListener(this::escolherTamanhoLetra);
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
            recolherTexto(savedInstanceState.getBoolean(ESTADO_TEXTO_RECOLHIDO, false));
            tempoQuestaoMs = savedInstanceState.getLong(ESTADO_TEMPO_QUESTAO, 0L);
            if (salvaSelecionada >= 0) {
                selecionar(salvaSelecionada);
                if (salvaVerificada) {
                    // Acertos, vidas e caderno já foram atualizados; mostrarFeedback só pinta.
                    mostrarFeedback(false);
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
        outState.putLong(ESTADO_TEMPO_QUESTAO, tempoDaQuestao());
        outState.putBoolean(ESTADO_TEXTO_RECOLHIDO, textoRecolhido);
        if (modoRevisao && questoes != null) {
            ArrayList<String> ids = new ArrayList<>();
            for (Questao q : questoes) {
                ids.add(q.getId());
            }
            outState.putStringArrayList(ESTADO_IDS_REVISAO, ids);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        retomadoEm = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onDestroy() {
        // Pode ser null se a tela fechou logo no começo (lição não encontrada, caderno vazio).
        if (retorno != null) {
            retorno.liberar();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Em segundo plano o cronômetro para (RN38).
        tempoQuestaoMs = tempoDaQuestao();
        retomadoEm = -1;
    }

    /** Tempo de tela da questão atual até agora; depois do VERIFICAR fica parado. */
    private long tempoDaQuestao() {
        if (verificada || retomadoEm < 0) {
            return tempoQuestaoMs;
        }
        return tempoQuestaoMs + SystemClock.elapsedRealtime() - retomadoEm;
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
            int vidas = progresso.getVidas();
            txtVidas.setText(String.valueOf(vidas));
            txtVidas.setContentDescription(getResources().getQuantityString(R.plurals.cd_vidas, vidas, vidas));
        }
    }

    /** Preenche a tela com a questão atual e volta tudo ao estado inicial. */
    private void mostrarQuestao() {
        Questao questao = questoes.get(indiceAtual);
        selecionada = -1;
        verificada = false;
        tempoQuestaoMs = 0;
        if (retomadoEm >= 0) {
            retomadoEm = SystemClock.elapsedRealtime();
        }

        progress.setProgressCompat(indiceAtual, true);
        txtContador.setText(getString(R.string.quiz_contador, indiceAtual + 1, questoes.size()));

        if (questao.getTexto().isEmpty()) {
            cardTextoBase.setVisibility(View.GONE);
        } else {
            cardTextoBase.setVisibility(View.VISIBLE);
            TextoComImagens.exibir(txtTextoBase, questao.getTexto(), false);
            txtTextoRecolhido.setText(primeiraLinha(questao.getTexto()));
        }
        textoRecolhido = false;
        recolherTexto(false);
        TextoComImagens.exibir(txtEnunciado, questao.getEnunciado(), false);
        // Enunciado com figura/fórmula também vai para o papel, para a tinta escura aparecer.
        if (TextoRico.temMarcador(questao.getEnunciado())) {
            txtEnunciado.setBackgroundResource(R.drawable.bg_papel);
            txtEnunciado.setTextColor(cor(R.color.papel_texto));
            int p = getResources().getDimensionPixelSize(R.dimen.espaco_cards);
            txtEnunciado.setPadding(p, p, p, p);
        } else {
            txtEnunciado.setBackground(null);
            txtEnunciado.setTextColor(cor(R.color.texto_principal));
            txtEnunciado.setPadding(0, 0, 0, 0);
        }
        if (questao.getFonte().isEmpty()) {
            txtFonte.setVisibility(View.GONE);
        } else {
            txtFonte.setVisibility(View.VISIBLE);
            txtFonte.setText(questao.getFonte());
            txtFonte.setContentDescription(getString(R.string.quiz_fonte, questao.getFonte()));
        }

        String[] alternativas = questao.getAlternativas();
        for (int i = 0; i < cardsAlternativas.length; i++) {
            CartaoRelevo card = cardsAlternativas[i];
            alternativaEmPapel[i] = TextoRico.temMarcador(alternativas[i]);
            TextoComImagens.exibir(textosAlternativas[i], alternativas[i], true);
            pintar(i, NORMAL);
            card.setAlpha(1f);
            card.setClickable(true);
        }

        painelFeedback.setVisibility(View.GONE);
        mascoteFeedback.parar();
        divisorInferior.setVisibility(View.VISIBLE);
        btnVerificar.setVisibility(View.VISIBLE);
        btnVerificar.setEnabled(false);
        btnContinuar.setEnabled(true);
        scroll.scrollTo(0, 0);
    }

    /**
     * Recolhe o texto-base (fica só a faixa com a primeira linha, e um toque no cartão abre de
     * novo) ou abre. Ao recolher, volta ao topo para o comando e as alternativas aparecerem.
     */
    private void recolherTexto(boolean recolher) {
        boolean mudou = recolher != textoRecolhido;
        textoRecolhido = recolher;
        txtTextoBase.setVisibility(recolher ? View.GONE : View.VISIBLE);
        btnRecolherTexto.setVisibility(recolher ? View.GONE : View.VISIBLE);
        faixaTextoRecolhido.setVisibility(recolher ? View.VISIBLE : View.GONE);
        if (recolher) {
            cardTextoBase.setOnClickListener(v -> recolherTexto(false));
            cardTextoBase.setContentDescription(
                    getString(R.string.cd_texto_recolhido, txtTextoRecolhido.getText()));
            if (mudou) {
                scroll.post(() -> scroll.smoothScrollTo(0, 0));
            }
        } else {
            cardTextoBase.setOnClickListener(null);
            cardTextoBase.setClickable(false);
            cardTextoBase.setContentDescription(null);
        }
        // A dica da figura só faz sentido com a figura à vista.
        Questao questao = questoes.get(indiceAtual);
        boolean temFigura = (!recolher && TextoRico.temFigura(questao.getTexto()))
                || TextoRico.temFigura(questao.getEnunciado());
        txtDicaFigura.setVisibility(temFigura ? View.VISIBLE : View.GONE);
    }

    /** Primeira linha com texto do texto-base, para a faixa do texto recolhido. */
    private static String primeiraLinha(String texto) {
        for (String linha : TextoRico.semMarcadores(texto).split("\n")) {
            if (!linha.trim().isEmpty()) {
                return linha.trim();
            }
        }
        return "";
    }

    /** Menu do "Aa": a letra muda na hora e fica salva para as próximas questões. */
    private void escolherTamanhoLetra(View botao) {
        PopupMenu menu = new PopupMenu(this, botao);
        menu.inflate(R.menu.menu_tamanho_letra);
        Menu itens = menu.getMenu();
        itens.findItem(ITENS_LETRA[preferencias.getTamanhoLetra()]).setChecked(true);
        menu.setOnMenuItemClickListener(item -> {
            for (int nivel = 0; nivel < ITENS_LETRA.length; nivel++) {
                if (ITENS_LETRA[nivel] == item.getItemId()) {
                    preferencias.setTamanhoLetra(nivel);
                    aplicarTamanhoLetra();
                    return true;
                }
            }
            return false;
        });
        menu.show();
    }

    /** Texto-base, enunciado, alternativas e explicação no tamanho escolhido (TamanhoLetra). */
    private void aplicarTamanhoLetra() {
        int nivel = preferencias.getTamanhoLetra();
        LetraAjustavel.aplicar(nivel, txtTextoBase, txtEnunciado, txtExplicacao);
        LetraAjustavel.aplicar(nivel, textosAlternativas);
    }

    /** Destaca a alternativa escolhida e volta as outras ao normal. */
    private void selecionar(int indice) {
        if (verificada) {
            return;
        }
        selecionada = indice;
        for (int i = 0; i < cardsAlternativas.length; i++) {
            pintar(i, i == selecionada ? SELECIONADA : NORMAL);
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
        tempoQuestaoMs = tempoDaQuestao();
        progresso.registrarTempoQuestao(questao, tempoQuestaoMs);
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
        // Som de acerto ou de erro (o erro também vibra, C10), se estiverem ligados.
        if (questao.isCorreta(selecionada)) {
            retorno.acerto();
        } else {
            retorno.erro();
        }
        mostrarFeedback(true);
    }

    /** Trava as alternativas, pinta certo/errado e mostra o painel com a explicação. */
    private void mostrarFeedback(boolean animar) {
        verificada = true;
        Questao questao = questoes.get(indiceAtual);
        boolean acertou = questao.isCorreta(selecionada);

        for (int i = 0; i < cardsAlternativas.length; i++) {
            CartaoRelevo card = cardsAlternativas[i];
            card.setClickable(false);
            // As alternativas que não importam mais ficam apagadas (menos as de figura/fórmula,
            // que precisam continuar legíveis no papel claro).
            boolean envolvida = i == questao.getCorreta() || i == selecionada;
            card.setAlpha(envolvida || alternativaEmPapel[i] ? 1f : 0.55f);
        }
        pintar(questao.getCorreta(), CORRETA);

        int corForte;
        int corPainel;
        int corTitulo;
        if (acertou) {
            corForte = cor(R.color.marca_acerto);
            corPainel = cor(R.color.marca_acerto_suave);
            corTitulo = cor(R.color.marca_acerto_texto);
            txtFeedbackTitulo.setText(R.string.feedback_acerto);
            mascoteFeedback.setExpressao(MascoteView.Expressao.FELIZ);
            btnContinuar.setCores(corForte, Color.TRANSPARENT, cor(R.color.marca_acerto_escuro));
        } else {
            pintar(selecionada, ERRADA);
            corForte = cor(R.color.marca_erro);
            corPainel = cor(R.color.marca_erro_suave);
            corTitulo = cor(R.color.marca_erro_texto);
            txtFeedbackTitulo.setText(getString(R.string.feedback_erro, questao.getLetraCorreta()));
            mascoteFeedback.setExpressao(MascoteView.Expressao.TRISTE);
            btnContinuar.setCores(corForte, Color.TRANSPARENT, cor(R.color.marca_erro_escuro));
        }

        painelFeedback.setBackgroundColor(corPainel);
        txtFeedbackTitulo.setTextColor(corTitulo);
        txtExplicacao.setText(questao.getExplicacao());

        btnVerificar.setVisibility(View.GONE);
        divisorInferior.setVisibility(View.GONE);
        painelFeedback.setVisibility(View.VISIBLE);
        if (animar) {
            // O painel sobe de baixo da tela e, quando chega, a Lia pula ou balança a cabeça.
            painelFeedback.post(() -> {
                painelFeedback.setTranslationY(painelFeedback.getHeight());
                painelFeedback.animate()
                        .translationY(0f)
                        .setDuration(260)
                        .setInterpolator(new DecelerateInterpolator(1.6f))
                        .withEndAction(() -> {
                            if (acertou) {
                                mascoteFeedback.pular();
                            } else {
                                mascoteFeedback.tremer();
                            }
                        })
                        .start();
            });
        }
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

    /** A Lia fica sem energia (orelhas caídas e desbotada), como as vidas. */
    private void mostrarVidasAcabaram() {
        Dialogos.semVidas(this, getString(R.string.vidas_acabaram_titulo),
                        getString(R.string.vidas_acabaram_mensagem))
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
        intent.putStringArrayListExtra(Constantes.EXTRA_CELEBRACOES,
                Celebracoes.paraTextos(Celebracoes.montar(resultado)));
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

    /**
     * Cores da alternativa em cada estado. As que têm figura ou fórmula usam a versão "papel",
     * que continua clara no modo escuro (a tinta das imagens é escura).
     */
    private void pintar(int indice, int estado) {
        boolean papel = alternativaEmPapel[indice];
        int face;
        int borda;
        if (estado == SELECIONADA) {
            face = cor(papel ? R.color.papel_selecao : R.color.marca_primaria_suave);
            borda = cor(R.color.marca_primaria);
        } else if (estado == CORRETA) {
            face = cor(papel ? R.color.papel_acerto : R.color.marca_acerto_suave);
            borda = cor(R.color.marca_acerto);
        } else if (estado == ERRADA) {
            face = cor(papel ? R.color.papel_erro : R.color.marca_erro_suave);
            borda = cor(R.color.marca_erro);
        } else {
            face = cor(papel ? R.color.papel : R.color.superficie);
            borda = cor(papel ? R.color.papel_borda : R.color.superficie_borda);
        }

        CartaoRelevo card = cardsAlternativas[indice];
        MaterialCardView faceAlt = card.findViewById(R.id.face_alt);
        faceAlt.setCardBackgroundColor(face);
        faceAlt.setStrokeColor(borda);
        card.setCorSombra(borda);

        textosAlternativas[indice].setTextColor(cor(papel ? R.color.papel_texto : R.color.texto_principal));

        // Letra: círculo vazado no normal, preenchido com a cor do estado nos outros.
        TextView letra = card.findViewById(R.id.txt_letra);
        GradientDrawable circulo = new GradientDrawable();
        circulo.setShape(GradientDrawable.OVAL);
        if (estado == NORMAL) {
            circulo.setColor(Color.TRANSPARENT);
            circulo.setStroke(getResources().getDimensionPixelSize(R.dimen.largura_borda), borda);
            letra.setTextColor(cor(papel ? R.color.papel_texto_secundario : R.color.texto_secundario));
        } else {
            circulo.setColor(borda);
            letra.setTextColor(cor(R.color.texto_sobre_cor));
        }
        letra.setBackground(circulo);
    }

    private int cor(int corRes) {
        return ContextCompat.getColor(this, corRes);
    }
}
