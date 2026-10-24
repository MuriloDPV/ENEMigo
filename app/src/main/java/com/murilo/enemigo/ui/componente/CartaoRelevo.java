package com.murilo.enemigo.ui.componente;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.murilo.enemigo.R;

/**
 * Cartão com sombra sólida embaixo. O primeiro filho é a "face" (em geral um MaterialCardView);
 * quando o cartão é clicável e está pressionado, a face desce e cobre a sombra.
 * Usado nos cartões das áreas, nas alternativas do quiz e nos nós da trilha.
 */
public class CartaoRelevo extends FrameLayout {

    private int corSombra;
    private float raio;
    private int relevo;

    public CartaoRelevo(@NonNull Context context) {
        this(context, null);
    }

    public CartaoRelevo(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CartaoRelevo);
        corSombra = a.getColor(R.styleable.CartaoRelevo_corSombra,
                ContextCompat.getColor(context, R.color.superficie_borda));
        raio = a.getDimension(R.styleable.CartaoRelevo_raio,
                getResources().getDimension(R.dimen.raio_cartao));
        relevo = a.getDimensionPixelSize(R.styleable.CartaoRelevo_relevo,
                getResources().getDimensionPixelSize(R.dimen.relevo));
        a.recycle();

        // A face ocupa tudo menos a faixa da sombra.
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + relevo);
        atualizarSombra();
    }

    public void setCorSombra(int cor) {
        if (cor != corSombra) {
            corSombra = cor;
            atualizarSombra();
        }
    }

    private void atualizarSombra() {
        setBackground(Relevo.sombra(corSombra, raio, relevo));
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        View face = getChildCount() > 0 ? getChildAt(0) : null;
        if (face != null) {
            face.setTranslationY(isPressed() ? relevo : 0);
        }
    }
}
