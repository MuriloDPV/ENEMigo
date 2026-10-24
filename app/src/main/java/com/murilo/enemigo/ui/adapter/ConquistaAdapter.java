package com.murilo.enemigo.ui.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Conquista;

import java.util.HashSet;
import java.util.Set;

/**
 * Grade de conquistas do Perfil (C3): medalha, título e descrição. As bloqueadas ficam com a
 * medalha cinza e o cadeado. Tocar numa conquista avisa o ouvinte (o Perfil mostra o detalhe).
 */
public class ConquistaAdapter extends RecyclerView.Adapter<ConquistaAdapter.ConquistaViewHolder> {

    public interface AoTocar {
        void aoTocar(Conquista conquista, boolean desbloqueada);
    }

    private final Conquista[] conquistas = Conquista.values();
    private final Set<String> desbloqueadas = new HashSet<>();
    private final AoTocar aoTocar;

    public ConquistaAdapter(AoTocar aoTocar) {
        this.aoTocar = aoTocar;
    }

    public void setDesbloqueadas(Set<String> ids) {
        desbloqueadas.clear();
        desbloqueadas.addAll(ids);
        notifyItemRangeChanged(0, conquistas.length);
    }

    /** Medalha colorida (desbloqueada) ou cinza e apagada (bloqueada). */
    public static void pintarMedalha(ImageView img, Conquista conquista, boolean desbloqueada) {
        img.setImageResource(conquista.getMedalhaRes());
        if (desbloqueada) {
            img.clearColorFilter();
            img.setImageAlpha(255);
        } else {
            ColorMatrix cinza = new ColorMatrix();
            cinza.setSaturation(0f);
            img.setColorFilter(new ColorMatrixColorFilter(cinza));
            img.setImageAlpha(115);
        }
    }

    @NonNull
    @Override
    public ConquistaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conquista, parent, false);
        return new ConquistaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConquistaViewHolder holder, int position) {
        Conquista conquista = conquistas[position];
        Context context = holder.itemView.getContext();
        boolean desbloqueada = desbloqueadas.contains(conquista.getId());

        pintarMedalha(holder.imgMedalha, conquista, desbloqueada);
        holder.imgCadeado.setVisibility(desbloqueada ? View.GONE : View.VISIBLE);
        holder.txtTitulo.setText(conquista.getTituloRes());
        holder.txtTitulo.setTextColor(ContextCompat.getColor(context,
                desbloqueada ? R.color.texto_principal : R.color.texto_secundario));
        holder.txtDescricao.setText(conquista.getDescricaoRes());

        String titulo = context.getString(conquista.getTituloRes());
        String descricao = context.getString(conquista.getDescricaoRes());
        holder.itemView.setContentDescription(context.getString(desbloqueada
                ? R.string.cd_conquista_desbloqueada : R.string.cd_conquista_bloqueada, titulo, descricao));
        holder.itemView.setOnClickListener(v -> aoTocar.aoTocar(conquista, desbloqueada));
    }

    @Override
    public int getItemCount() {
        return conquistas.length;
    }

    static class ConquistaViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgMedalha;
        final ImageView imgCadeado;
        final TextView txtTitulo;
        final TextView txtDescricao;

        ConquistaViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMedalha = itemView.findViewById(R.id.img_medalha);
            imgCadeado = itemView.findViewById(R.id.img_cadeado);
            txtTitulo = itemView.findViewById(R.id.txt_titulo);
            txtDescricao = itemView.findViewById(R.id.txt_descricao);
        }
    }
}
