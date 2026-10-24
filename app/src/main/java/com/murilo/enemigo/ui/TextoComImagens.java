package com.murilo.enemigo.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.murilo.enemigo.R;
import com.murilo.enemigo.util.TextoRico;

import java.io.IOException;
import java.io.InputStream;

/**
 * Mostra num TextView o texto de uma questão, trocando os marcadores [[imagem:...]] e
 * [[formula:...]] pelas imagens de assets/imagens/. Texto sem marcador vai direto no setText,
 * então as questões só de texto continuam exatamente como antes.
 */
final class TextoComImagens {

    private static final String TAG = "TextoComImagens";
    private static final char OBJETO = '￼';

    private static LruCache<String, Bitmap> cache;

    private TextoComImagens() {
    }

    /**
     * @param emAlternativa true dentro do card de uma alternativa: lá as figuras ocupam a largura
     *                      toda (para os 5 gráficos ficarem do mesmo tamanho) e o toque seleciona
     *                      a alternativa. Fora dela, tocar na figura abre a figura ampliada.
     */
    static void exibir(TextView tv, String texto, boolean emAlternativa) {
        tv.setTag(R.id.tag_texto_rico, texto);
        if (!TextoRico.temMarcador(texto)) {
            tv.setMovementMethod(null);
            tv.setText(texto);
            return;
        }
        if (tv.getWidth() == 0) {
            // Antes do primeiro layout a largura ainda é 0 e as figuras não têm como ser escaladas.
            tv.setText(TextoRico.semMarcadores(texto));
            tv.post(() -> {
                if (texto.equals(tv.getTag(R.id.tag_texto_rico))) {
                    montar(tv, texto, emAlternativa);
                }
            });
            return;
        }
        montar(tv, texto, emAlternativa);
    }

    private static void montar(TextView tv, String texto, boolean emAlternativa) {
        int largura = tv.getWidth() - tv.getTotalPaddingLeft() - tv.getTotalPaddingRight();
        if (largura <= 0) {
            largura = tv.getResources().getDisplayMetrics().widthPixels;
        }
        float densidade = tv.getResources().getDisplayMetrics().density;
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean temClicavel = false;

        for (TextoRico.Trecho trecho : TextoRico.separar(texto)) {
            if (trecho.tipo == TextoRico.Tipo.TEXTO) {
                sb.append(trecho.conteudo);
                continue;
            }
            boolean formula = trecho.tipo == TextoRico.Tipo.FORMULA;
            Bitmap bitmap = carregar(tv.getContext(), trecho.conteudo);
            if (bitmap == null) {
                sb.append(formula ? "[fórmula]" : "[figura]");
                continue;
            }
            int inicio = sb.length();
            sb.append(OBJETO);
            int fim = sb.length();
            sb.setSpan(new ImagemSpan(bitmap, formula, emAlternativa, densidade, largura), inicio, fim,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!formula && trecho.sozinhaNaLinha) {
                sb.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), inicio, fim,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (!formula && !emAlternativa) {
                sb.setSpan(new AmpliarSpan(trecho.conteudo), inicio, fim, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                temClicavel = true;
            }
        }
        if (temClicavel) {
            tv.setMovementMethod(LinkMovementMethod.getInstance());
            tv.setHighlightColor(android.graphics.Color.TRANSPARENT);
        } else {
            tv.setMovementMethod(null);
        }
        tv.setText(sb);
    }

    /** Lê a imagem de assets/imagens/ (com cache), ou null se ela não existir. */
    static Bitmap carregar(Context context, String arquivo) {
        if (cache == null) {
            int bytes = (int) Math.min(Runtime.getRuntime().maxMemory() / 8, 24L * 1024 * 1024);
            cache = new LruCache<String, Bitmap>(bytes) {
                @Override
                protected int sizeOf(String chave, Bitmap bitmap) {
                    return bitmap.getByteCount();
                }
            };
        }
        Bitmap bitmap = cache.get(arquivo);
        if (bitmap != null) {
            return bitmap;
        }
        try (InputStream in = context.getAssets().open(TextoRico.PASTA_IMAGENS + arquivo)) {
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            Log.e(TAG, "Imagem não encontrada: " + arquivo, e);
            return null;
        }
        if (bitmap != null) {
            cache.put(arquivo, bitmap);
        }
        return bitmap;
    }

    /** Toque na figura: abre em tela cheia com zoom. Não muda a cor nem sublinha nada. */
    private static final class AmpliarSpan extends ClickableSpan {
        private final String arquivo;

        AmpliarSpan(String arquivo) {
            this.arquivo = arquivo;
        }

        @Override
        public void onClick(@NonNull View widget) {
            ImagemAmpliada.mostrar(widget.getContext(), arquivo);
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            // Sem a cor/sublinhado de link.
        }
    }
}
