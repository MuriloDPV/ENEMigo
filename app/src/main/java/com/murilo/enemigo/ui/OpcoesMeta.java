package com.murilo.enemigo.ui;

import android.widget.RadioGroup;

import com.murilo.enemigo.R;
import com.murilo.enemigo.util.Constantes;

/**
 * Liga as opções de meta diária (layout_opcoes_meta) aos valores 20, 50 e 100.
 * Usado no Onboarding e nas Configurações.
 */
public final class OpcoesMeta {

    private OpcoesMeta() {
    }

    /** 20, 50 ou 100; 0 se nenhuma opção estiver marcada. */
    public static int metaMarcada(RadioGroup grupo) {
        int id = grupo.getCheckedRadioButtonId();
        if (id == R.id.rb_casual) {
            return Constantes.META_CASUAL;
        } else if (id == R.id.rb_regular) {
            return Constantes.META_REGULAR;
        } else if (id == R.id.rb_intenso) {
            return Constantes.META_INTENSO;
        }
        return 0;
    }

    public static void marcar(RadioGroup grupo, int meta) {
        if (meta == Constantes.META_CASUAL) {
            grupo.check(R.id.rb_casual);
        } else if (meta == Constantes.META_INTENSO) {
            grupo.check(R.id.rb_intenso);
        } else {
            grupo.check(R.id.rb_regular);
        }
    }
}
