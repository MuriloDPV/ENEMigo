package com.murilo.enemigo.ui.componente;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.murilo.enemigo.R;

/**
 * Botão com sombra sólida que "afunda" ao ser tocado. As cores vêm do estilo
 * (Widget.ENEMigo.Botao.*) ou de setCores. Desabilitado, fica cinza.
 */
public class BotaoRelevo extends AppCompatButton {

    private int corFace;
    private int corBorda;
    private int corSombra;
    private float raio;
    private int relevo;
    private int larguraBorda;

    public BotaoRelevo(@NonNull Context context) {
        this(context, null);
    }

    public BotaoRelevo(@NonNull Context context, AttributeSet attrs) {
        // Sem estilo padrão do tema: o visual vem só do style do XML e do fundo desenhado aqui.
        super(context, attrs, 0);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BotaoRelevo);
        corFace = a.getColor(R.styleable.BotaoRelevo_corFace,
                ContextCompat.getColor(context, R.color.marca_primaria));
        corBorda = a.getColor(R.styleable.BotaoRelevo_corBorda, Color.TRANSPARENT);
        corSombra = a.getColor(R.styleable.BotaoRelevo_corSombra,
                ContextCompat.getColor(context, R.color.marca_primaria_escura));
        raio = a.getDimension(R.styleable.BotaoRelevo_raio,
                getResources().getDimension(R.dimen.raio_botao));
        relevo = a.getDimensionPixelSize(R.styleable.BotaoRelevo_relevo,
                getResources().getDimensionPixelSize(R.dimen.relevo));
        a.recycle();
        larguraBorda = getResources().getDimensionPixelSize(R.dimen.largura_borda);

        // O texto fica centralizado na face, que é mais baixa que o botão inteiro.
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom() + relevo);
        setBackgroundTintList(null);
        atualizarFundo();
    }

    /** Troca as cores (ex.: CONTINUAR verde no acerto e vermelho no erro). */
    public void setCores(int face, int borda, int sombra) {
        corFace = face;
        corBorda = borda;
        corSombra = sombra;
        atualizarFundo();
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean mudou = enabled != isEnabled();
        super.setEnabled(enabled);
        if (mudou) {
            atualizarFundo();
        }
    }

    private void atualizarFundo() {
        if (isEnabled()) {
            setBackground(Relevo.criar(corFace, corBorda, corSombra, raio, relevo, larguraBorda));
        } else {
            Context c = getContext();
            setBackground(Relevo.criar(ContextCompat.getColor(c, R.color.desativado), Color.TRANSPARENT,
                    ContextCompat.getColor(c, R.color.desativado_sombra), raio, relevo, larguraBorda));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Pressionado, o texto desce junto com a face.
        if (isPressed()) {
            canvas.save();
            canvas.translate(0, relevo);
            super.onDraw(canvas);
            canvas.restore();
        } else {
            super.onDraw(canvas);
        }
    }
}
