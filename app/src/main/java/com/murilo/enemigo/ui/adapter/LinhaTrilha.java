package com.murilo.enemigo.ui.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.model.EstadoLicao;

/**
 * Linha pontilhada que liga os nós da trilha, desenhada por baixo dos itens. O trecho que leva a
 * uma lição já liberada fica na cor da área; o resto, em cinza.
 */
public class LinhaTrilha extends RecyclerView.ItemDecoration {

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path caminho = new Path();
    private final int corFeita;
    private final int corPendente;

    public LinhaTrilha(Context context, int corArea) {
        float d = context.getResources().getDisplayMetrics().density;
        corFeita = corArea;
        corPendente = ContextCompat.getColor(context, R.color.linha_trilha);
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeWidth(7 * d);
        pincel.setStrokeCap(Paint.Cap.ROUND);
        // Traço quase nulo + ponta redonda = bolinhas.
        pincel.setPathEffect(new DashPathEffect(new float[]{0.1f, 15 * d}, 0));
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        float[] anterior = null;
        Object estadoAnterior = null;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View no = parent.getChildAt(i).findViewById(R.id.no);
            if (no == null) {
                continue;
            }
            float[] centro = centro(no, parent);
            if (anterior != null) {
                caminho.reset();
                caminho.moveTo(anterior[0], anterior[1]);
                float meio = (centro[1] - anterior[1]) / 2;
                caminho.cubicTo(anterior[0], anterior[1] + meio, centro[0], centro[1] - meio,
                        centro[0], centro[1]);
                // Se a lição de cima foi concluída, a de baixo já está liberada.
                pincel.setColor(estadoAnterior == EstadoLicao.CONCLUIDA ? corFeita : corPendente);
                canvas.drawPath(caminho, pincel);
            }
            anterior = centro;
            estadoAnterior = no.getTag(R.id.tag_estado_licao);
        }
    }

    /** Centro da face do nó nas coordenadas da RecyclerView (inclui o deslocamento do zigue-zague). */
    private static float[] centro(View no, RecyclerView parent) {
        float x = no.getWidth() / 2f;
        float y = no.getWidth() / 2f;
        View v = no;
        while (v != parent && v != null) {
            x += v.getX();
            y += v.getY();
            v = (View) v.getParent();
        }
        return new float[]{x, y};
    }
}
