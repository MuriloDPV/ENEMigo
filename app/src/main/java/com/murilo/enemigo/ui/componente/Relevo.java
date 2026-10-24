package com.murilo.enemigo.ui.componente;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Fundo "em relevo": uma face arredondada apoiada numa sombra sólida mais escura, como uma tecla.
 * Pressionada, a face desce e cobre a sombra, dando a sensação de que o botão afundou.
 */
public final class Relevo {

    private Relevo() {
    }

    /**
     * @param borda cor da borda da face (Color.TRANSPARENT para não ter borda)
     * @param relevo altura da sombra, em px
     */
    public static Drawable criar(int face, int borda, int sombra, float raio, int relevo, int larguraBorda) {
        StateListDrawable estados = new StateListDrawable();
        estados.addState(new int[]{android.R.attr.state_pressed},
                camadas(face, borda, sombra, raio, relevo, larguraBorda, true));
        estados.addState(new int[]{},
                camadas(face, borda, sombra, raio, relevo, larguraBorda, false));
        return estados;
    }

    /** Só a sombra, deslocada para baixo (usada pelo CartaoRelevo, que move a própria face). */
    public static Drawable sombra(int sombra, float raio, int relevo) {
        LayerDrawable camadas = new LayerDrawable(new Drawable[]{retangulo(sombra, raio)});
        camadas.setLayerInset(0, 0, relevo, 0, 0);
        return camadas;
    }

    private static Drawable camadas(int face, int borda, int sombra, float raio, int relevo,
                                    int larguraBorda, boolean pressionado) {
        GradientDrawable frente = retangulo(face, raio);
        if (larguraBorda > 0 && borda != Color.TRANSPARENT) {
            frente.setStroke(larguraBorda, borda);
        }
        LayerDrawable camadas = new LayerDrawable(new Drawable[]{retangulo(sombra, raio), frente});
        // A sombra fica sempre embaixo; a face sobe (normal) ou desce até cobri-la (pressionado).
        camadas.setLayerInset(0, 0, relevo, 0, 0);
        if (pressionado) {
            camadas.setLayerInset(1, 0, relevo, 0, 0);
        } else {
            camadas.setLayerInset(1, 0, 0, 0, relevo);
        }
        return camadas;
    }

    private static GradientDrawable retangulo(int cor, float raio) {
        GradientDrawable forma = new GradientDrawable();
        forma.setShape(GradientDrawable.RECTANGLE);
        forma.setCornerRadius(raio);
        forma.setColor(cor);
        return forma;
    }
}
