package com.murilo.enemigo.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * ImageView com zoom: pinça para ampliar, arrastar para mover e toque duplo para alternar
 * entre a imagem inteira e 2,5x. A imagem nunca sai da tela.
 */
public class ZoomImageView extends AppCompatImageView {

    private static final float ZOOM_MAX = 5f;
    private static final float ZOOM_TOQUE_DUPLO = 2.5f;

    private final Matrix matriz = new Matrix();
    private final float[] valores = new float[9];
    private final RectF area = new RectF();
    private float escalaInicial = 1f;
    private ScaleGestureDetector pinca;
    private GestureDetector gestos;

    public ZoomImageView(Context context) {
        super(context);
        iniciar(context);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        iniciar(context);
    }

    private void iniciar(Context context) {
        setScaleType(ScaleType.MATRIX);
        pinca = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                ampliar(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }
        });
        gestos = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float dx, float dy) {
                matriz.postTranslate(-dx, -dy);
                limitar();
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                float alvo = zoomAtual() > 1.01f ? 1f : ZOOM_TOQUE_DUPLO;
                ampliar(alvo / zoomAtual(), e.getX(), e.getY());
                return true;
            }
        });
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        encaixar();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        encaixar();
    }

    /** Volta para a imagem inteira, centralizada. */
    private void encaixar() {
        Drawable d = getDrawable();
        if (d == null || getWidth() == 0 || getHeight() == 0 || matriz == null) {
            return;
        }
        float largura = d.getIntrinsicWidth();
        float altura = d.getIntrinsicHeight();
        escalaInicial = Math.min(getWidth() / largura, getHeight() / altura);
        matriz.setScale(escalaInicial, escalaInicial);
        matriz.postTranslate((getWidth() - largura * escalaInicial) / 2,
                (getHeight() - altura * escalaInicial) / 2);
        setImageMatrix(matriz);
    }

    private float zoomAtual() {
        matriz.getValues(valores);
        return valores[Matrix.MSCALE_X] / escalaInicial;
    }

    private void ampliar(float fator, float focoX, float focoY) {
        float atual = zoomAtual();
        float novo = Math.max(1f, Math.min(ZOOM_MAX, atual * fator));
        matriz.postScale(novo / atual, novo / atual, focoX, focoY);
        limitar();
    }

    /** Centraliza o que é menor que a tela e não deixa sobrar borda no que é maior. */
    private void limitar() {
        Drawable d = getDrawable();
        if (d == null) {
            return;
        }
        area.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        matriz.mapRect(area);
        matriz.postTranslate(ajuste(area.left, area.right, getWidth()),
                ajuste(area.top, area.bottom, getHeight()));
        setImageMatrix(matriz);
    }

    private static float ajuste(float inicio, float fim, int tamanhoTela) {
        if (fim - inicio <= tamanhoTela) {
            return (tamanhoTela - (fim - inicio)) / 2 - inicio;
        }
        if (inicio > 0) {
            return -inicio;
        }
        if (fim < tamanhoTela) {
            return tamanhoTela - fim;
        }
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pinca.onTouchEvent(event);
        gestos.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
