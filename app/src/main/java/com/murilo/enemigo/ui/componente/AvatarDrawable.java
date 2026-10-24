package com.murilo.enemigo.ui.componente;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Avatar;
import com.murilo.enemigo.model.Avatar.Parte;

import java.util.ArrayList;
import java.util.List;

/**
 * Desenha o avatar: o fundo colorido e as peças vetoriais (avatar_*) empilhadas, recortadas num
 * círculo. Pele e cabelo são peças brancas que recebem a cor com tint, então qualquer tom
 * funciona sem desenhar de novo. Use AvatarDrawable.criar(context, avatar).
 */
public final class AvatarDrawable extends Drawable {

    // Uma lista por parte, na ordem das opções do model/Avatar.
    private static final int[] PELES = {R.color.avatar_pele_1, R.color.avatar_pele_2,
            R.color.avatar_pele_3, R.color.avatar_pele_4, R.color.avatar_pele_5, R.color.avatar_pele_6,
            R.color.avatar_pele_7, R.color.avatar_pele_8};
    private static final int[] ROSTOS = {R.drawable.avatar_rosto_redondo, R.drawable.avatar_rosto_oval,
            R.drawable.avatar_rosto_quadrado};
    private static final int[] CABELOS = {R.drawable.avatar_cabelo_curto, R.drawable.avatar_cabelo_cacheado,
            R.drawable.avatar_cabelo_crespo, R.drawable.avatar_cabelo_longo, R.drawable.avatar_cabelo_coque,
            R.drawable.avatar_cabelo_raspado};
    /** Parte de trás do cabelo (fica atrás do rosto e dos ombros); 0 = não tem. */
    private static final int[] CABELOS_TRAS = {0, 0, R.drawable.avatar_cabelo_crespo_tras,
            R.drawable.avatar_cabelo_longo_tras, R.drawable.avatar_cabelo_coque_tras, 0};
    private static final int[] CORES_CABELO = {R.color.avatar_cabelo_1, R.color.avatar_cabelo_2,
            R.color.avatar_cabelo_3, R.color.avatar_cabelo_4, R.color.avatar_cabelo_5, R.color.avatar_cabelo_6};
    private static final int[] OLHOS = {R.drawable.avatar_olhos_redondos, R.drawable.avatar_olhos_sorridentes,
            R.drawable.avatar_olhos_grandes, R.drawable.avatar_olhos_cilios};
    /** 0 = nenhum. */
    private static final int[] ACESSORIOS = {0, R.drawable.avatar_acessorio_oculos,
            R.drawable.avatar_acessorio_bone, R.drawable.avatar_acessorio_fone};
    private static final int[] FUNDOS = {R.color.avatar_fundo_1, R.color.avatar_fundo_2,
            R.color.avatar_fundo_3, R.color.avatar_fundo_4, R.color.avatar_fundo_5, R.color.avatar_fundo_6,
            R.color.avatar_fundo_7, R.color.avatar_fundo_8};

    private final Paint pincelFundo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borracha = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path foraDoCirculo = new Path();
    private final List<Drawable> camadas;
    private final int tamanho;
    private int alpha = 255;

    private AvatarDrawable(int corFundo, List<Drawable> camadas, int tamanho) {
        this.camadas = camadas;
        this.tamanho = tamanho;
        pincelFundo.setColor(corFundo);
        borracha.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        foraDoCirculo.setFillType(Path.FillType.INVERSE_WINDING);
    }

    public static AvatarDrawable criar(Context context, Avatar avatar) {
        int pele = ContextCompat.getColor(context, PELES[avatar.get(Parte.PELE)]);
        int cabelo = ContextCompat.getColor(context, CORES_CABELO[avatar.get(Parte.COR_CABELO)]);
        int estilo = avatar.get(Parte.CABELO);

        // De trás para a frente: cabelo de trás, pescoço, camiseta, rosto, olhos, cabelo e acessório.
        List<Drawable> camadas = new ArrayList<>();
        if (CABELOS_TRAS[estilo] != 0) {
            camadas.add(peca(context, CABELOS_TRAS[estilo], cabelo));
        }
        camadas.add(peca(context, R.drawable.avatar_pescoco, pele));
        camadas.add(peca(context, R.drawable.avatar_pescoco_sombra, null));
        camadas.add(peca(context, R.drawable.avatar_corpo, null));
        camadas.add(peca(context, ROSTOS[avatar.get(Parte.ROSTO)], pele));
        camadas.add(peca(context, R.drawable.avatar_detalhes, null));
        camadas.add(peca(context, OLHOS[avatar.get(Parte.OLHOS)], null));
        camadas.add(peca(context, R.drawable.avatar_sobrancelhas, cabelo));
        camadas.add(peca(context, CABELOS[estilo], cabelo));
        int acessorio = ACESSORIOS[avatar.get(Parte.ACESSORIO)];
        if (acessorio != 0) {
            camadas.add(peca(context, acessorio, null));
        }
        int tamanho = Math.round(120 * context.getResources().getDisplayMetrics().density);
        return new AvatarDrawable(ContextCompat.getColor(context, FUNDOS[avatar.get(Parte.FUNDO)]),
                camadas, tamanho);
    }

    /** Cor de amostra de uma opção de cor (pele, cabelo ou fundo), para o editor. */
    public static int corDaOpcao(Context context, Parte parte, int opcao) {
        int[] cores = parte == Parte.PELE ? PELES : parte == Parte.COR_CABELO ? CORES_CABELO : FUNDOS;
        return ContextCompat.getColor(context, cores[opcao]);
    }

    /** As partes que são só uma cor (no editor viram bolinhas de cor). */
    public static boolean ehCor(Parte parte) {
        return parte == Parte.PELE || parte == Parte.COR_CABELO || parte == Parte.FUNDO;
    }

    private static Drawable peca(Context context, int desenho, @Nullable Integer cor) {
        Drawable peca = AppCompatResources.getDrawable(context, desenho);
        if (peca == null) {
            throw new IllegalStateException("peça do avatar não encontrada: " + desenho);
        }
        // mutate(): cada avatar com o seu tint, sem mudar as outras cópias do mesmo desenho.
        peca = peca.mutate();
        if (cor != null) {
            peca.setTint(cor);
        }
        return peca;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect area = getBounds();
        if (area.isEmpty()) {
            return;
        }
        // Desenha numa camada à parte e apaga o que ficou fora do círculo (borda suave).
        int camada = canvas.saveLayerAlpha(area.left, area.top, area.right, area.bottom, alpha);
        canvas.drawRect(area, pincelFundo);
        for (Drawable peca : camadas) {
            peca.setBounds(area);
            peca.draw(canvas);
        }
        foraDoCirculo.reset();
        foraDoCirculo.addCircle(area.exactCenterX(), area.exactCenterY(),
                Math.min(area.width(), area.height()) / 2f, Path.Direction.CW);
        canvas.drawPath(foraDoCirculo, borracha);
        canvas.restoreToCount(camada);
    }

    @Override
    public int getIntrinsicWidth() {
        return tamanho;
    }

    @Override
    public int getIntrinsicHeight() {
        return tamanho;
    }

    @Override
    public void setAlpha(int alpha) {
        this.alpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter filtro) {
        for (Drawable peca : camadas) {
            peca.setColorFilter(filtro);
        }
        pincelFundo.setColorFilter(filtro);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
