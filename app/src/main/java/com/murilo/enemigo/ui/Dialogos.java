package com.murilo.enemigo.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.ui.componente.MascoteView;
import com.murilo.enemigo.util.Constantes;

/**
 * Diálogos e atalhos repetidos em mais de uma tela.
 */
public final class Dialogos {

    private Dialogos() {
    }

    /** "Sem vidas" com o tempo para a próxima vida (Trilha e Resultado). */
    public static void mostrarSemVidas(Activity activity) {
        mostrarSemVidas(activity, () -> abrirRevisao(activity));
    }

    /**
     * @param irParaRevisao o que o "Ir para Revisão" faz (o Resultado passa antes pelas
     *                      celebrações que faltam)
     */
    public static void mostrarSemVidas(Activity activity, Runnable irParaRevisao) {
        int minutos = ProgressoManager.getInstance(activity).getMinutosParaProximaVida();
        semVidas(activity, activity.getString(R.string.sem_vidas_titulo),
                activity.getString(R.string.sem_vidas_mensagem, minutos))
                .setPositiveButton(R.string.btn_ir_revisao, (dialog, which) -> irParaRevisao.run())
                .setNegativeButton(R.string.btn_ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Diálogo de vidas com o Grifo triste e desbotado no topo, "sem tinta". Os botões ficam
     * por conta de quem chama.
     */
    // O título de um diálogo não tem pai na hora de inflar: quem posiciona é o próprio diálogo.
    @SuppressLint("InflateParams")
    public static MaterialAlertDialogBuilder semVidas(Activity activity, CharSequence titulo,
                                                      CharSequence mensagem) {
        View topo = LayoutInflater.from(activity).inflate(R.layout.dialogo_titulo_mascote, null);
        ((TextView) topo.findViewById(R.id.txt_titulo_dialogo)).setText(titulo);
        MascoteView mascote = topo.findViewById(R.id.mascote_dialogo);
        mascote.setSemTinta(true);
        // Treme quando o diálogo aparece (o post espera a view entrar na tela).
        mascote.post(mascote::tremer);
        return new MaterialAlertDialogBuilder(activity, R.style.ThemeOverlay_ENEMigo_Dialogo_Icone)
                .setCustomTitle(topo)
                .setMessage(mensagem);
    }

    /** Volta para a MainActivity já existente, na aba Revisão, fechando as telas acima dela. */
    public static void abrirRevisao(Context context) {
        context.startActivity(intentRevisao(context));
    }

    public static Intent intentRevisao(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constantes.EXTRA_ABRIR_REVISAO, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
}
