package com.murilo.enemigo.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Conquista;
import com.murilo.enemigo.ui.componente.ConfeteView;
import com.murilo.enemigo.ui.componente.MascoteView;
import com.murilo.enemigo.util.Celebracoes;
import com.murilo.enemigo.util.Celebracoes.Celebracao;
import com.murilo.enemigo.util.Constantes;

import java.util.List;

/**
 * Celebrações em tela cheia, uma depois da outra: lição perfeita, ofensiva aumentada, nível novo
 * e conquista nova. Cada uma tem confete, o Grifo comemorando e um som. CONTINUAR vai para a
 * próxima; o Voltar pula todas. No fim, abre a tela "próxima" se ela veio na Intent.
 */
public class CelebracaoActivity extends AppCompatActivity {

    private static final String EXTRA_PROXIMA = "EXTRA_PROXIMA";
    private static final String ESTADO_INDICE = "indice";

    private List<Celebracao> fila;
    private int indice;
    private Retorno retorno;
    private ObjectAnimator giroRaios;

    private View circuloDestaque;
    private ImageView imgDestaque;
    private TextView txtDestaque;
    private ImageView imgMedalha;
    private TextView txtTitulo;
    private TextView txtSubtitulo;
    private TextView txtFala;
    private MascoteView mascote;
    private ConfeteView confete;

    /**
     * Abre a fila de celebrações. Sem nenhuma, abre direto a próxima tela (se houver).
     *
     * @param proxima tela para abrir no fim (ou null para só voltar)
     */
    public static void abrir(Activity origem, List<Celebracao> celebracoes, Intent proxima) {
        if (celebracoes.isEmpty()) {
            if (proxima != null) {
                origem.startActivity(proxima);
            }
            return;
        }
        Intent intent = new Intent(origem, CelebracaoActivity.class);
        intent.putStringArrayListExtra(Constantes.EXTRA_CELEBRACOES, Celebracoes.paraTextos(celebracoes));
        if (proxima != null) {
            intent.putExtra(EXTRA_PROXIMA, proxima);
        }
        origem.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_celebracao);

        fila = Celebracoes.deTextos(getIntent().getStringArrayListExtra(Constantes.EXTRA_CELEBRACOES));
        if (fila.isEmpty()) {
            terminar();
            return;
        }
        indice = savedInstanceState == null ? 0
                : Math.min(savedInstanceState.getInt(ESTADO_INDICE, 0), fila.size() - 1);

        // Barras do sistema com ícones claros sobre o azul.
        WindowInsetsControllerCompat barras = WindowCompat.getInsetsController(getWindow(),
                getWindow().getDecorView());
        barras.setAppearanceLightStatusBars(false);
        barras.setAppearanceLightNavigationBars(false);

        circuloDestaque = findViewById(R.id.circulo_destaque);
        imgDestaque = findViewById(R.id.img_destaque);
        txtDestaque = findViewById(R.id.txt_destaque);
        imgMedalha = findViewById(R.id.img_medalha);
        txtTitulo = findViewById(R.id.txt_titulo);
        txtSubtitulo = findViewById(R.id.txt_subtitulo);
        txtFala = findViewById(R.id.txt_fala);
        mascote = findViewById(R.id.mascote);
        confete = findViewById(R.id.confete);
        retorno = new Retorno(this);

        // Os raios giram devagar o tempo todo.
        giroRaios = ObjectAnimator.ofFloat(findViewById(R.id.img_raios), View.ROTATION, 0f, 360f);
        giroRaios.setDuration(24000);
        giroRaios.setRepeatCount(ValueAnimator.INFINITE);
        giroRaios.setInterpolator(new LinearInterpolator());
        giroRaios.start();

        findViewById(R.id.btn_continuar).setOnClickListener(v -> avancar());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                terminar();
            }
        });

        mostrar(fila.get(indice), savedInstanceState == null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_INDICE, indice);
    }

    @Override
    protected void onDestroy() {
        if (giroRaios != null) {
            giroRaios.cancel();
        }
        if (retorno != null) {
            retorno.liberar();
        }
        super.onDestroy();
    }

    private void avancar() {
        if (indice < fila.size() - 1) {
            indice++;
            mostrar(fila.get(indice), true);
        } else {
            terminar();
        }
    }

    private void terminar() {
        Intent proxima = IntentCompat.getParcelableExtra(getIntent(), EXTRA_PROXIMA, Intent.class);
        if (proxima != null) {
            startActivity(proxima);
        }
        finish();
    }

    /** Preenche a tela com uma celebração e, se for nova, toca o som e solta o confete. */
    private void mostrar(Celebracao c, boolean nova) {
        circuloDestaque.setVisibility(View.VISIBLE);
        imgMedalha.setVisibility(View.GONE);
        imgDestaque.setVisibility(View.VISIBLE);
        imgDestaque.setImageTintList(null);
        txtDestaque.setVisibility(View.GONE);

        switch (c.tipo) {
            case LICAO_PERFEITA:
                pintarCirculo(R.color.marca_acento);
                imgDestaque.setImageResource(R.drawable.ic_trofeu);
                imgDestaque.setImageTintList(ColorStateList.valueOf(cor(R.color.marca_sobre_acento)));
                txtTitulo.setText(R.string.celebracao_perfeita_titulo);
                txtSubtitulo.setText(getString(R.string.celebracao_perfeita_subtitulo, numero(c.valor)));
                txtFala.setText(R.string.celebracao_perfeita_fala);
                break;
            case OFENSIVA:
                int dias = numero(c.valor);
                pintarCirculo(R.color.texto_sobre_cor);
                imgDestaque.setImageResource(R.drawable.ic_fogo);
                txtTitulo.setText(getResources().getQuantityString(R.plurals.celebracao_ofensiva_titulo, dias, dias));
                txtSubtitulo.setText(R.string.celebracao_ofensiva_subtitulo);
                txtFala.setText(R.string.celebracao_ofensiva_fala);
                break;
            case NIVEL:
                int nivel = numero(c.valor);
                pintarCirculo(R.color.marca_acento);
                imgDestaque.setVisibility(View.GONE);
                txtDestaque.setVisibility(View.VISIBLE);
                txtDestaque.setText(String.valueOf(nivel));
                txtDestaque.setTextColor(cor(R.color.marca_sobre_acento));
                txtTitulo.setText(R.string.celebracao_nivel_titulo);
                txtSubtitulo.setText(getString(R.string.celebracao_nivel_subtitulo, nivel));
                txtFala.setText(R.string.celebracao_nivel_fala);
                break;
            case CONQUISTA:
            default:
                Conquista conquista = Conquista.fromId(c.valor);
                circuloDestaque.setVisibility(View.GONE);
                imgMedalha.setVisibility(View.VISIBLE);
                txtTitulo.setText(R.string.celebracao_conquista_titulo);
                if (conquista != null) {
                    imgMedalha.setImageResource(conquista.getMedalhaRes());
                    txtSubtitulo.setText(nomeEDescricao(conquista));
                }
                txtFala.setText(R.string.celebracao_conquista_fala);
                break;
        }

        mascote.setExpressao(MascoteView.Expressao.COMEMORANDO);
        mascote.comemorar();
        if (nova) {
            // Uma tela, um som: a lição perfeita tem o som de lição concluída.
            if (c.tipo == Celebracoes.Tipo.LICAO_PERFEITA) {
                retorno.licaoConcluida();
            } else {
                retorno.acerto();
            }
            animarEntrada();
            confete.soltar();
        }
    }

    /** O destaque "estoura" na tela e os textos sobem um pouco. */
    private void animarEntrada() {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            return;
        }
        View destaque = imgMedalha.getVisibility() == View.VISIBLE ? imgMedalha : circuloDestaque;
        destaque.setScaleX(0.3f);
        destaque.setScaleY(0.3f);
        destaque.setAlpha(0f);
        destaque.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(480)
                .setInterpolator(new OvershootInterpolator(2.2f)).start();
        float subida = 16 * getResources().getDisplayMetrics().density;
        for (View texto : new View[]{txtTitulo, txtSubtitulo}) {
            texto.setTranslationY(subida);
            texto.setAlpha(0f);
            texto.animate().translationY(0f).alpha(texto == txtSubtitulo ? 0.9f : 1f)
                    .setStartDelay(150).setDuration(320).start();
        }
    }

    private void pintarCirculo(int corRes) {
        circuloDestaque.setBackgroundTintList(ColorStateList.valueOf(cor(corRes)));
    }

    /** Nome da conquista em negrito e a descrição embaixo. */
    private CharSequence nomeEDescricao(Conquista conquista) {
        SpannableStringBuilder texto = new SpannableStringBuilder(getString(conquista.getTituloRes()));
        texto.setSpan(new StyleSpan(Typeface.BOLD), 0, texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        texto.append('\n').append(getString(conquista.getDescricaoRes()));
        return texto;
    }

    private static int numero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int cor(int corRes) {
        return ContextCompat.getColor(this, corRes);
    }
}
