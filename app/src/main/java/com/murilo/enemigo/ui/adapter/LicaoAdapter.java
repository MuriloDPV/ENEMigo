package com.murilo.enemigo.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.model.Licao;

import java.util.List;

/**
 * Lista as lições de uma trilha. Até a Aula 3, todas aparecem como disponíveis.
 */
public class LicaoAdapter extends RecyclerView.Adapter<LicaoAdapter.LicaoViewHolder> {

    public interface OnLicaoClickListener {
        void onLicaoClick(Licao licao);
    }

    private final List<Licao> licoes;
    private final int corArea;
    private final OnLicaoClickListener listener;

    /**
     * @param corArea cor já resolvida (não o id do recurso)
     */
    public LicaoAdapter(List<Licao> licoes, int corArea, OnLicaoClickListener listener) {
        this.licoes = licoes;
        this.corArea = corArea;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LicaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_licao, parent, false);
        return new LicaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LicaoViewHolder holder, int position) {
        Licao licao = licoes.get(position);
        int numero = licao.getOrdem() + 1;

        holder.txtCirculo.setText(String.valueOf(numero));
        holder.txtCirculo.setBackgroundTintList(ColorStateList.valueOf(corArea));
        holder.txtNumero.setText(holder.itemView.getContext()
                .getString(R.string.licao_numero, numero));
        holder.txtTitulo.setText(licao.getTitulo());
        holder.txtDetalhe.setText(R.string.licao_comecar);
        holder.txtDetalhe.setTextColor(corArea);

        holder.itemView.setOnClickListener(v -> listener.onLicaoClick(licao));
    }

    @Override
    public int getItemCount() {
        return licoes.size();
    }

    static class LicaoViewHolder extends RecyclerView.ViewHolder {
        final TextView txtCirculo;
        final TextView txtNumero;
        final TextView txtTitulo;
        final TextView txtDetalhe;

        LicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCirculo = itemView.findViewById(R.id.txt_circulo);
            txtNumero = itemView.findViewById(R.id.txt_numero);
            txtTitulo = itemView.findViewById(R.id.txt_titulo);
            txtDetalhe = itemView.findViewById(R.id.txt_detalhe);
        }
    }
}
