package com.murilo.enemigo.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Questao;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista as questões do caderno de erros. Tocar num item não faz nada nesta versão.
 */
public class CadernoAdapter extends RecyclerView.Adapter<CadernoAdapter.CadernoViewHolder> {

    private final List<Questao> questoes = new ArrayList<>();

    public void setQuestoes(List<Questao> novas) {
        int antes = questoes.size();
        questoes.clear();
        notifyItemRangeRemoved(0, antes);
        questoes.addAll(novas);
        notifyItemRangeInserted(0, questoes.size());
    }

    @NonNull
    @Override
    public CadernoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_caderno, parent, false);
        return new CadernoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CadernoViewHolder holder, int position) {
        Questao questao = questoes.get(position);
        int cor = ContextCompat.getColor(holder.itemView.getContext(), questao.getArea().getCorRes());
        holder.txtEtiqueta.setText(questao.getArea().getNome());
        holder.txtEtiqueta.setBackgroundTintList(ColorStateList.valueOf(cor));
        holder.txtEnunciado.setText(questao.getEnunciado());
        holder.txtFonte.setText(questao.getFonte());
    }

    @Override
    public int getItemCount() {
        return questoes.size();
    }

    static class CadernoViewHolder extends RecyclerView.ViewHolder {
        final TextView txtEtiqueta;
        final TextView txtEnunciado;
        final TextView txtFonte;

        CadernoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEtiqueta = itemView.findViewById(R.id.txt_etiqueta);
            txtEnunciado = itemView.findViewById(R.id.txt_enunciado);
            txtFonte = itemView.findViewById(R.id.txt_fonte);
        }
    }
}
