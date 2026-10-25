package com.murilo.enemigo.ui;

import android.util.TypedValue;
import android.widget.TextView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.util.TamanhoLetra;

/**
 * Aplica o tamanho de letra escolhido (TamanhoLetra) num TextView. O tamanho do layout fica
 * guardado na tag da View na primeira vez, então trocar de nível várias vezes não acumula.
 * As fórmulas acompanham sozinhas, porque o ImagemSpan as escala pelo tamanho da fonte.
 */
final class LetraAjustavel {

    private LetraAjustavel() {
    }

    static void aplicar(int nivel, TextView... textos) {
        for (TextView tv : textos) {
            Object base = tv.getTag(R.id.tag_tamanho_base);
            if (!(base instanceof Float)) {
                base = tv.getTextSize();
                tv.setTag(R.id.tag_tamanho_base, base);
            }
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, TamanhoLetra.tamanho((Float) base, nivel));
        }
    }
}
