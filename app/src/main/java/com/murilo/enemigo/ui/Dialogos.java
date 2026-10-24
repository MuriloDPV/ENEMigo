package com.murilo.enemigo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.util.Constantes;

/**
 * Diálogos e atalhos repetidos em mais de uma tela.
 */
public final class Dialogos {

    private Dialogos() {
    }

    /** "Sem vidas" com o tempo para a próxima vida (Trilha e Resultado). */
    public static void mostrarSemVidas(Activity activity) {
        int minutos = ProgressoManager.getInstance(activity).getMinutosParaProximaVida();
        new MaterialAlertDialogBuilder(activity, R.style.ThemeOverlay_ENEMigo_Dialogo_Icone)
                .setIcon(R.drawable.ic_coracao)
                .setTitle(R.string.sem_vidas_titulo)
                .setMessage(activity.getString(R.string.sem_vidas_mensagem, minutos))
                .setPositiveButton(R.string.btn_ir_revisao, (dialog, which) -> abrirRevisao(activity))
                .setNegativeButton(R.string.btn_ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /** Volta para a MainActivity já existente, na aba Revisão, fechando as telas acima dela. */
    public static void abrirRevisao(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constantes.EXTRA_ABRIR_REVISAO, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }
}
