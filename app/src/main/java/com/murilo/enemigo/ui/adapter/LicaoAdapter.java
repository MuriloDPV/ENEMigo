package com.murilo.enemigo.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.model.EstadoLicao;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.util.Constantes;

import java.util.List;

/**
 * Lista as lições de uma trilha com o estado de cada uma (concluída, disponível ou bloqueada).
 */
public class LicaoAdapter extends RecyclerView.Adapter<LicaoAdapter.LicaoViewHolder> {

    public interface OnLicaoClickListener {
        void onLicaoClick(Licao licao, EstadoLicao estado);
    }

    private final List<Licao> licoes;
    private final int corArea;
    private final ProgressoManager progresso;
    private final OnLicaoClickListener listener;

    /**
     * @param corArea cor já resolvida (não o id do recurso)
     */
    public LicaoAdapter(List<Licao> licoes, int corArea, ProgressoManager progresso,
                        OnLicaoClickListener listener) {
        this.licoes = licoes;
        this.corArea = corArea;
        this.progresso = progresso;
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
        Context context = holder.itemView.getContext();
        Licao licao = licoes.get(position);
        EstadoLicao estado = progresso.getEstado(licao);
        int numero = licao.getOrdem() + 1;

        holder.txtNumero.setText(context.getString(R.string.licao_numero, numero));
        holder.txtTitulo.setText(licao.getTitulo());

        int cor;
        if (estado == EstadoLicao.CONCLUIDA) {
            cor = corArea;
            holder.txtCirculo.setText(R.string.circulo_concluida);
            holder.txtDetalhe.setText(context.getString(R.string.licao_melhor,
                    progresso.getMelhor(licao.getId()), Constantes.QUESTOES_POR_LICAO));
        } else if (estado == EstadoLicao.DISPONIVEL) {
            cor = corArea;
            holder.txtCirculo.setText(String.valueOf(numero));
            holder.txtDetalhe.setText(R.string.licao_comecar);
        } else {
            cor = ContextCompat.getColor(context, R.color.cinza_bloqueado);
            holder.txtCirculo.setText(R.string.circulo_bloqueada);
            holder.txtDetalhe.setText(R.string.licao_bloqueada);
        }
        holder.txtCirculo.setBackgroundTintList(ColorStateList.valueOf(cor));
        holder.txtDetalhe.setTextColor(cor);

        holder.itemView.setOnClickListener(v -> listener.onLicaoClick(licao, estado));
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
