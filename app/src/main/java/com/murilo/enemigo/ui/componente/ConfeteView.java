package com.murilo.enemigo.ui.componente;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.murilo.enemigo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Confete desenhado na própria View, sem Lottie e sem imagens: papeizinhos nas cores da marca
 * saem dos dois cantos de baixo, sobem, giram, "viram" no ar e caem devagar com a gravidade.
 * Não recebe toques (fica por cima da tela de celebração).
 */
public class ConfeteView extends View {

    private static final int QUANTIDADE = 150;
    /** Em dp por segundo (ao quadrado, na gravidade). */
    private static final float GRAVIDADE = 1500f;
    private static final float QUEDA_MAXIMA = 300f;
    private static final float BALANCO = 28f;
    private static final long DURACAO_MAXIMA_MS = 5000;
    private static final long SUMIR_MS = 700;

    private static final int RETANGULO = 0;
    private static final int CIRCULO = 1;
    private static final int TIRA = 2;

    /** Um papelzinho: posição e velocidade em px, ângulos em graus e "fase" da virada. */
    private static final class Papel {
        float x;
        float y;
        float vx;
        float vy;
        float angulo;
        float giro;
        float fase;
        float velocidadeFase;
        float largura;
        float altura;
        int cor;
        int forma;
    }

    private final List<Papel> papeis = new ArrayList<>();
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF retangulo = new RectF();
    private final Random sorteio = new Random();
    private final float dp;
    private final int[] cores;
    private long inicio;
    private long ultimoQuadro;

    public ConfeteView(@NonNull Context context) {
        this(context, null);
    }

    public ConfeteView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        dp = getResources().getDisplayMetrics().density;
        // Cores de função da marca (sem o azul primário, que é o fundo da celebração).
        int[] ids = {R.color.marca_acento, R.color.marca_acerto, R.color.marca_erro, R.color.marca_ofensiva,
                R.color.area_natureza, R.color.area_matematica, R.color.texto_sobre_cor};
        cores = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            cores[i] = ContextCompat.getColor(context, ids[i]);
        }
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    /** Solta uma rajada de confete. Com "remover animações" ligado no celular, não faz nada. */
    public void soltar() {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            return;
        }
        if (getWidth() == 0) {
            // Ainda sem tamanho: espera o layout.
            post(this::soltar);
            return;
        }
        papeis.clear();
        for (int i = 0; i < QUANTIDADE; i++) {
            papeis.add(novoPapel(i % 2 == 0));
        }
        inicio = SystemClock.uptimeMillis();
        ultimoQuadro = inicio;
        postInvalidateOnAnimation();
    }

    private Papel novoPapel(boolean daEsquerda) {
        Papel p = new Papel();
        p.x = daEsquerda ? -8 * dp : getWidth() + 8 * dp;
        p.y = getHeight() * (0.78f + sorteio.nextFloat() * 0.18f);
        // Sai para cima e para dentro, entre 55° e 85° acima do chão.
        double angulo = Math.toRadians(55 + sorteio.nextFloat() * 30);
        float velocidade = (1100 + sorteio.nextFloat() * 900) * dp;
        p.vx = (float) (Math.cos(angulo) * velocidade) * (daEsquerda ? 1 : -1);
        p.vy = -(float) (Math.sin(angulo) * velocidade);
        p.angulo = sorteio.nextFloat() * 360;
        p.giro = (sorteio.nextFloat() - 0.5f) * 720;
        p.fase = (float) (sorteio.nextFloat() * Math.PI * 2);
        p.velocidadeFase = 5 + sorteio.nextFloat() * 9;
        p.cor = cores[sorteio.nextInt(cores.length)];
        float forma = sorteio.nextFloat();
        if (forma < 0.15f) {
            p.forma = CIRCULO;
            p.largura = (5 + sorteio.nextFloat() * 3) * dp;
            p.altura = p.largura;
        } else if (forma < 0.35f) {
            p.forma = TIRA;
            p.largura = (12 + sorteio.nextFloat() * 6) * dp;
            p.altura = 2.5f * dp;
        } else {
            p.forma = RETANGULO;
            p.largura = (7 + sorteio.nextFloat() * 5) * dp;
            p.altura = (4 + sorteio.nextFloat() * 3) * dp;
        }
        return p;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (papeis.isEmpty()) {
            return;
        }
        long agora = SystemClock.uptimeMillis();
        float dt = Math.min(0.05f, (agora - ultimoQuadro) / 1000f);
        ultimoQuadro = agora;
        long passou = agora - inicio;
        // Nos últimos instantes o confete vai sumindo, em vez de desaparecer de uma vez.
        float opacidade = Math.min(1f, (DURACAO_MAXIMA_MS - passou) / (float) SUMIR_MS);

        boolean algumNaTela = false;
        for (Papel p : papeis) {
            // Resistência do ar, gravidade e queda lenta de papel, balançando de um lado para o outro.
            p.vx *= 1 - 1.3f * dt;
            p.vy = p.vy * (1 - 0.8f * dt) + GRAVIDADE * dp * dt;
            p.vy = Math.min(p.vy, QUEDA_MAXIMA * dp);
            p.fase += p.velocidadeFase * dt;
            p.x += p.vx * dt + (float) Math.sin(p.fase) * BALANCO * dp * dt;
            p.y += p.vy * dt;
            p.angulo += p.giro * dt;
            if (p.y > getHeight() + 20 * dp) {
                continue;
            }
            algumNaTela = true;

            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.angulo);
            // Achatar pelo cosseno da fase faz o papel parecer virar no ar.
            canvas.scale(1f, (float) Math.cos(p.fase));
            pincel.setColor(p.cor);
            pincel.setAlpha((int) (255 * Math.max(0f, opacidade)));
            if (p.forma == CIRCULO) {
                canvas.drawCircle(0, 0, p.largura / 2, pincel);
            } else {
                retangulo.set(-p.largura / 2, -p.altura / 2, p.largura / 2, p.altura / 2);
                canvas.drawRoundRect(retangulo, p.altura / 3, p.altura / 3, pincel);
            }
            canvas.restore();
        }

        if (algumNaTela && passou < DURACAO_MAXIMA_MS) {
            postInvalidateOnAnimation();
        } else {
            papeis.clear();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        papeis.clear();
        super.onDetachedFromWindow();
    }
}
