package com.murilo.enemigo.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.murilo.enemigo.util.TextoRico;

/**
 * Desenha uma figura ou fórmula no meio do texto de um TextView.
 * - Fórmula: escalada pelo tamanho da fonte (um pouco maior) e centralizada na linha do texto.
 * - Figura: 1 px da imagem = 1 dp (sem passar da largura do TextView), apoiada na linha de base;
 *   com preencherLargura, ocupa a largura toda do TextView.
 */
class ImagemSpan extends ReplacementSpan {

    private final Bitmap bitmap;
    private final boolean formula;
    private final boolean preencherLargura;
    private final float densidade;
    private final int larguraMax;
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF destino = new RectF();

    ImagemSpan(Bitmap bitmap, boolean formula, boolean preencherLargura, float densidade, int larguraMax) {
        this.bitmap = bitmap;
        this.formula = formula;
        this.preencherLargura = preencherLargura;
        this.densidade = densidade;
        this.larguraMax = larguraMax;
        if (!formula) {
            // As figuras do ENEM têm fundo branco. "Multiplicar" faz o branco sumir no fundo do
            // card (cinza, azul da seleção, verde ou vermelho) e mantém as linhas e cores.
            pincel.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        }
    }

    private float escala(Paint paint) {
        if (!formula && preencherLargura) {
            return (float) larguraMax / bitmap.getWidth();
        }
        float escala = formula
                ? paint.getTextSize() * TextoRico.AUMENTO_FORMULA / TextoRico.PX_POR_EM
                : densidade;
        return Math.min(escala, (float) larguraMax / bitmap.getWidth());
    }

    /** Topo da imagem em relação à linha de base (negativo = acima). */
    private float topo(Paint paint, float altura) {
        if (!formula) {
            return -altura;
        }
        Paint.FontMetrics fm = paint.getFontMetrics();
        float centroDaLinha = (fm.ascent + fm.descent) / 2;
        return centroDaLinha - altura / 2;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                       @Nullable Paint.FontMetricsInt fm) {
        float escala = escala(paint);
        float altura = bitmap.getHeight() * escala;
        if (fm != null) {
            Paint.FontMetricsInt texto = paint.getFontMetricsInt();
            int topo = Math.round(topo(paint, altura));
            int base = Math.round(topo + altura);
            fm.ascent = Math.min(texto.ascent, topo);
            fm.descent = Math.max(texto.descent, base);
            fm.top = fm.ascent;
            fm.bottom = fm.descent;
        }
        return Math.round(bitmap.getWidth() * escala);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        float escala = escala(paint);
        float altura = bitmap.getHeight() * escala;
        float topo = y + topo(paint, altura);
        destino.set(x, topo, x + bitmap.getWidth() * escala, topo + altura);
        canvas.drawBitmap(bitmap, null, destino, pincel);
    }
}
