package com.murilo.enemigo.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;

import com.murilo.enemigo.R;

/**
 * Figura da questão em tela cheia, com zoom de pinça e toque duplo.
 * Ajuda a ler os números pequenos das figuras do ENEM no celular.
 */
final class ImagemAmpliada {

    private ImagemAmpliada() {
    }

    static void mostrar(Context context, String arquivo) {
        Bitmap bitmap = TextoComImagens.carregar(context, arquivo);
        if (bitmap == null) {
            return;
        }
        Dialog dialog = new Dialog(context, R.style.Theme_ENEMigo_ImagemTelaCheia);
        dialog.setContentView(R.layout.dialog_imagem);
        ZoomImageView imagem = dialog.findViewById(R.id.img_ampliada);
        imagem.setImageBitmap(bitmap);
        dialog.findViewById(R.id.btn_fechar_imagem).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
