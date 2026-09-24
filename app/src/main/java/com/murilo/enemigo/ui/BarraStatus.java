package com.murilo.enemigo.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;

/**
 * Preenche a barra 🔥 ⭐ ❤️ (layout_status) com o progresso salvo.
 * Usada no Início e na Trilha.
 */
public final class BarraStatus {

    private BarraStatus() {
    }

    public static void atualizar(View status) {
        Context context = status.getContext();
        ProgressoManager progresso = ProgressoManager.getInstance(context);
        ((TextView) status.findViewById(R.id.txt_ofensiva))
                .setText(context.getString(R.string.status_ofensiva, progresso.getOfensivaAtual()));
        ((TextView) status.findViewById(R.id.txt_xp))
                .setText(context.getString(R.string.status_xp, progresso.getXpTotal()));
        ((TextView) status.findViewById(R.id.txt_vidas))
                .setText(context.getString(R.string.status_vidas, progresso.getVidas()));
    }
}
