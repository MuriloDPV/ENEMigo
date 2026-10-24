package com.murilo.enemigo.ui.componente;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.murilo.enemigo.R;

/**
 * O Grifo, mascote do app (um marca-texto). Mostra uma das 5 expressões e faz animações leves
 * com os animators do Android: balançar (esperando), pular (acerto e comemoração) e tremer (erro).
 * Os desenhos usam as cores da marca (mascote_* em colors.xml), então acompanham a troca de marca.
 */
public class MascoteView extends AppCompatImageView {

    public enum Expressao {
        NEUTRO(R.drawable.mascote_neutro),
        FELIZ(R.drawable.mascote_feliz),
        COMEMORANDO(R.drawable.mascote_comemorando),
        TRISTE(R.drawable.mascote_triste),
        PENSANDO(R.drawable.mascote_pensando);

        private final int desenho;

        Expressao(int desenho) {
            this.desenho = desenho;
        }
    }

    /** Onde ficam os pés no desenho (112 de 120): é dali que ele balança e amassa. */
    private static final float PES = 112f / 120f;

    private Expressao expressao;
    private ObjectAnimator animacao;
    /** Depois de um pulo ou tremida, volta a balançar se estava balançando antes. */
    private boolean voltarABalancar;

    public MascoteView(@NonNull Context context) {
        this(context, null);
    }

    public MascoteView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MascoteView);
        int indice = a.getInt(R.styleable.MascoteView_expressao, 0);
        a.recycle();
        // Decorativo: quem explica a tela é o texto ao lado dele.
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setExpressao(Expressao.values()[indice]);
    }

    public void setExpressao(Expressao nova) {
        if (nova != expressao) {
            expressao = nova;
            setImageResource(nova.desenho);
        }
    }

    public Expressao getExpressao() {
        return expressao;
    }

    /** Balança de um lado para o outro e descansa um pouco. Repete até parar(). */
    public void balancar() {
        voltarABalancar = true;
        iniciar(criarBalanco());
    }

    private ObjectAnimator criarBalanco() {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofKeyframe(
                View.ROTATION,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(0.12f, -6f),
                Keyframe.ofFloat(0.30f, 5f),
                Keyframe.ofFloat(0.45f, -3f),
                Keyframe.ofFloat(0.58f, 0f),
                Keyframe.ofFloat(1f, 0f)));
        a.setDuration(2600);
        a.setRepeatCount(ValueAnimator.INFINITE);
        return a;
    }

    /** Um pulinho, amassando ao tomar impulso e ao cair. */
    public void pular() {
        iniciarUmaVez(criarPulo());
    }

    /** Pula sem parar (telas de comemoração). */
    public void comemorar() {
        voltarABalancar = false;
        ObjectAnimator a = criarPulo();
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setStartDelay(150);
        iniciar(a);
    }

    /** Tremidinha rápida, como quem balança a cabeça (erro). */
    public void tremer() {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofKeyframe(
                View.ROTATION,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(0.15f, -8f),
                Keyframe.ofFloat(0.35f, 7f),
                Keyframe.ofFloat(0.55f, -5f),
                Keyframe.ofFloat(0.75f, 3f),
                Keyframe.ofFloat(1f, 0f)));
        a.setDuration(560);
        iniciarUmaVez(a);
    }

    /** Para a animação e volta à posição normal. */
    public void parar() {
        voltarABalancar = false;
        cancelarAnimacao();
    }

    private void cancelarAnimacao() {
        if (animacao != null) {
            ObjectAnimator antiga = animacao;
            animacao = null;
            antiga.cancel();
        }
        setRotation(0f);
        setTranslationY(0f);
        setScaleX(1f);
        setScaleY(1f);
    }

    /** Sem vidas: fica desbotado, como um marca-texto sem tinta. */
    public void setSemTinta(boolean semTinta) {
        if (semTinta) {
            ColorMatrix matriz = new ColorMatrix();
            matriz.setSaturation(0.15f);
            setColorFilter(new ColorMatrixColorFilter(matriz));
            setImageAlpha(170);
        } else {
            clearColorFilter();
            setImageAlpha(255);
        }
    }

    private ObjectAnimator criarPulo() {
        // Pulo proporcional ao tamanho do mascote (antes do layout, conta como 96dp).
        float tamanho = getHeight() > 0 ? getHeight() : 96 * getResources().getDisplayMetrics().density;
        Keyframe topo = Keyframe.ofFloat(0.5f, -0.14f * tamanho);
        topo.setInterpolator(new DecelerateInterpolator());
        Keyframe chao = Keyframe.ofFloat(0.82f, 0f);
        chao.setInterpolator(new AccelerateInterpolator());
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y,
                        Keyframe.ofFloat(0f, 0f), Keyframe.ofFloat(0.18f, 0f), topo, chao,
                        Keyframe.ofFloat(1f, 0f)),
                PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                        Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(0.18f, 0.88f),
                        Keyframe.ofFloat(0.5f, 1.05f), Keyframe.ofFloat(0.82f, 0.92f),
                        Keyframe.ofFloat(1f, 1f)),
                PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                        Keyframe.ofFloat(0f, 1f), Keyframe.ofFloat(0.18f, 1.08f),
                        Keyframe.ofFloat(0.5f, 0.97f), Keyframe.ofFloat(0.82f, 1.06f),
                        Keyframe.ofFloat(1f, 1f)));
        a.setDuration(720);
        return a;
    }

    private void iniciar(ObjectAnimator nova) {
        cancelarAnimacao();
        // Com "remover animações" ligado no celular, ele fica parado.
        if (!ValueAnimator.areAnimatorsEnabled()) {
            return;
        }
        animacao = nova;
        nova.start();
    }

    /** Animação de uma vez só; no fim, volta a balançar se estava balançando. */
    private void iniciarUmaVez(ObjectAnimator nova) {
        nova.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Cancelada por outra animação: a nova é que manda.
                if (animacao == animation) {
                    animacao = null;
                    if (voltarABalancar) {
                        iniciar(criarBalanco());
                    }
                }
            }
        });
        iniciar(nova);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setPivotX(w / 2f);
        setPivotY(h * PES);
    }

    @Override
    protected void onDetachedFromWindow() {
        parar();
        super.onDetachedFromWindow();
    }
}
