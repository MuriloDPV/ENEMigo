package com.murilo.enemigo.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;

/**
 * Preenche as pílulas de ofensiva, XP e vidas (layout_status) com o progresso salvo.
 * Usada no Início e na Trilha.
 */
public final class BarraStatus {

    private BarraStatus() {
    }

    public static void atualizar(View status) {
        Context context = status.getContext();
        ProgressoManager progresso = ProgressoManager.getInstance(context);
        int ofensiva = progresso.getOfensivaAtual();
        int xp = progresso.getXpTotal();
        int vidas = progresso.getVidas();

        // RN29: o fogo fica colorido se já estudou hoje e cinza se ainda não.
        boolean aceso = progresso.estudouHoje();
        TextView txtOfensiva = status.findViewById(R.id.txt_ofensiva);
        TextViewCompat.setCompoundDrawableTintList(txtOfensiva, aceso ? null
                : ColorStateList.valueOf(ContextCompat.getColor(context, R.color.bloqueado_icone)));
        txtOfensiva.setTextColor(ContextCompat.getColor(context,
                aceso ? R.color.marca_ofensiva_texto : R.color.texto_secundario));
        txtOfensiva.setText(String.valueOf(ofensiva));
        txtOfensiva.setContentDescription(context.getResources()
                .getQuantityString(R.plurals.cd_ofensiva, ofensiva, ofensiva));

        TextView txtXp = status.findViewById(R.id.txt_xp);
        txtXp.setText(String.valueOf(xp));
        txtXp.setContentDescription(context.getString(R.string.cd_xp, xp));

        TextView txtVidas = status.findViewById(R.id.txt_vidas);
        txtVidas.setText(String.valueOf(vidas));
        txtVidas.setContentDescription(context.getResources()
                .getQuantityString(R.plurals.cd_vidas, vidas, vidas));
    }
}
