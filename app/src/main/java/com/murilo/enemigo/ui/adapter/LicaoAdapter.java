package com.murilo.enemigo.ui.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.model.EstadoLicao;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.ui.componente.CartaoRelevo;
import com.murilo.enemigo.util.Constantes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Trilha de uma área: separadores de unidade e os nós das lições em zigue-zague, com o estado de
 * cada uma (concluída, atual ou bloqueada). A linha pontilhada entre os nós é a LinhaTrilha.
 */
public class LicaoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnLicaoClickListener {
        void onLicaoClick(Licao licao, EstadoLicao estado);
    }

    /** Lições por unidade da trilha. É só agrupamento visual: o desbloqueio continua lição a lição. */
    public static final int LICOES_POR_UNIDADE = 2;

    private static final int TIPO_UNIDADE = 0;
    private static final int TIPO_LICAO = 1;

    /** Um item da lista: o separador de uma unidade ou uma lição. */
    private static final class Item {
        final int unidade;
        final String temas;
        final Licao licao;

        Item(int unidade, String temas, Licao licao) {
            this.unidade = unidade;
            this.temas = temas;
            this.licao = licao;
        }
    }

    private final List<Item> itens = new ArrayList<>();
    private final Area area;
    private final ProgressoManager progresso;
    private final OnLicaoClickListener listener;
    private final Set<LicaoViewHolder> animados = new HashSet<>();

    public LicaoAdapter(List<Licao> licoes, Area area, ProgressoManager progresso,
                        OnLicaoClickListener listener) {
        this.area = area;
        this.progresso = progresso;
        this.listener = listener;
        for (int i = 0; i < licoes.size(); i++) {
            if (i % LICOES_POR_UNIDADE == 0) {
                // O separador mostra os temas das lições da unidade.
                List<String> temas = new ArrayList<>();
                for (int j = i; j < Math.min(i + LICOES_POR_UNIDADE, licoes.size()); j++) {
                    temas.add(licoes.get(j).getTitulo());
                }
                itens.add(new Item(i / LICOES_POR_UNIDADE + 1, String.join(" · ", temas), null));
            }
            itens.add(new Item(0, null, licoes.get(i)));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return itens.get(position).licao == null ? TIPO_UNIDADE : TIPO_LICAO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TIPO_UNIDADE) {
            return new UnidadeViewHolder(inflater.inflate(R.layout.item_unidade, parent, false));
        }
        LicaoViewHolder holder = new LicaoViewHolder(inflater.inflate(R.layout.item_licao, parent, false));
        // O rótulo ocupa a metade da tela do lado oposto ao nó.
        int largura = parent.getWidth() > 0 ? parent.getWidth()
                : parent.getResources().getDisplayMetrics().widthPixels;
        float densidade = parent.getResources().getDisplayMetrics().density;
        holder.rotulo.getLayoutParams().width = Math.round(largura / 2f - 8 * densidade);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = itens.get(position);
        if (holder instanceof UnidadeViewHolder) {
            mostrarUnidade((UnidadeViewHolder) holder, item);
        } else {
            mostrarLicao((LicaoViewHolder) holder, item.licao);
        }
    }

    private void mostrarUnidade(UnidadeViewHolder h, Item item) {
        Context c = h.itemView.getContext();
        int corSobre = cor(c, area.getCorSobreRes());
        ((CartaoRelevo) h.itemView).setCorSombra(cor(c, area.getCorEscuraRes()));
        h.face.setCardBackgroundColor(cor(c, area.getCorRes()));
        h.txtUnidade.setText(c.getString(R.string.unidade_titulo, item.unidade));
        h.txtUnidade.setTextColor(corSobre);
        h.txtUnidade.setAlpha(0.8f);
        h.txtTemas.setText(item.temas);
        h.txtTemas.setTextColor(corSobre);
    }

    private void mostrarLicao(LicaoViewHolder h, Licao licao) {
        Context c = h.itemView.getContext();
        EstadoLicao estado = progresso.getEstado(licao);
        int numero = licao.getOrdem() + 1;

        // Zigue-zague: lições pares à esquerda, ímpares à direita; o rótulo vai do outro lado.
        boolean esquerda = licao.getOrdem() % 2 == 0;
        float deslocamento = c.getResources().getDimension(R.dimen.deslocamento_no);
        h.grupoNo.setTranslationX(esquerda ? -deslocamento : deslocamento);
        h.balao.setTranslationX(esquerda ? -deslocamento : deslocamento);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) h.rotulo.getLayoutParams();
        lp.gravity = Gravity.CENTER_VERTICAL | (esquerda ? Gravity.END : Gravity.START);
        h.rotulo.setLayoutParams(lp);
        int alinhamento = esquerda ? View.TEXT_ALIGNMENT_VIEW_START : View.TEXT_ALIGNMENT_VIEW_END;
        h.txtNumero.setTextAlignment(alinhamento);
        h.txtTitulo.setTextAlignment(alinhamento);
        h.txtDetalhe.setTextAlignment(alinhamento);

        h.txtNumero.setText(c.getString(R.string.licao_numero, numero));
        h.txtTitulo.setText(licao.getTitulo());

        int face;
        int sombra;
        int corIcone;
        int icone;
        String detalhe;
        if (estado == EstadoLicao.CONCLUIDA) {
            face = cor(c, area.getCorRes());
            sombra = cor(c, area.getCorEscuraRes());
            corIcone = cor(c, area.getCorSobreRes());
            icone = R.drawable.ic_check;
            detalhe = c.getString(R.string.licao_melhor, progresso.getMelhor(licao.getId()),
                    Constantes.QUESTOES_POR_LICAO);
        } else if (estado == EstadoLicao.DISPONIVEL) {
            face = cor(c, area.getCorRes());
            sombra = cor(c, area.getCorEscuraRes());
            corIcone = cor(c, area.getCorSobreRes());
            icone = R.drawable.ic_estrela_cheia;
            detalhe = null;
        } else {
            face = cor(c, R.color.bloqueado);
            sombra = cor(c, R.color.bloqueado_sombra);
            corIcone = cor(c, R.color.bloqueado_icone);
            icone = R.drawable.ic_cadeado;
            detalhe = c.getString(R.string.licao_bloqueada);
        }
        h.no.setCorSombra(sombra);
        h.faceNo.setCardBackgroundColor(face);
        h.imgNo.setImageResource(icone);
        h.imgNo.setImageTintList(ColorStateList.valueOf(corIcone));
        h.txtTitulo.setTextColor(cor(c, estado == EstadoLicao.BLOQUEADA
                ? R.color.texto_secundario : R.color.texto_principal));
        h.txtDetalhe.setVisibility(detalhe == null ? View.GONE : View.VISIBLE);
        h.txtDetalhe.setText(detalhe);

        boolean atual = estado == EstadoLicao.DISPONIVEL;
        h.balao.setVisibility(atual ? View.VISIBLE : View.GONE);
        h.txtBalao.setTextColor(cor(c, area.getCorEscuraRes()));
        h.anel.setBackgroundTintList(ColorStateList.valueOf(cor(c, area.getCorRes())));
        animar(h, atual);

        // A linha pontilhada usa o estado para pintar o caminho já percorrido.
        h.no.setTag(R.id.tag_estado_licao, estado);
        String descricao = c.getString(R.string.cd_licao, numero, licao.getTitulo());
        h.no.setContentDescription(detalhe == null ? descricao : descricao + ". " + detalhe);
        h.no.setOnClickListener(v -> listener.onLicaoClick(licao, estado));
    }

    /** Lição atual: o anel pulsa e o balão flutua. As outras ficam paradas. */
    private void animar(LicaoViewHolder h, boolean atual) {
        parar(h);
        if (!atual || !ValueAnimator.areAnimatorsEnabled()) {
            return;
        }
        ObjectAnimator pulso = ObjectAnimator.ofPropertyValuesHolder(h.anel,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.8f, 1.25f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.8f, 1.25f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.7f, 0f));
        pulso.setDuration(1400);
        pulso.setRepeatCount(ValueAnimator.INFINITE);
        pulso.setInterpolator(new DecelerateInterpolator());

        float subida = -6 * h.itemView.getResources().getDisplayMetrics().density;
        ObjectAnimator flutuar = ObjectAnimator.ofFloat(h.balao, View.TRANSLATION_Y, 0f, subida);
        flutuar.setDuration(900);
        flutuar.setRepeatCount(ValueAnimator.INFINITE);
        flutuar.setRepeatMode(ValueAnimator.REVERSE);
        flutuar.setInterpolator(new AccelerateDecelerateInterpolator());

        pulso.start();
        flutuar.start();
        h.animacoes = new Animator[]{pulso, flutuar};
        animados.add(h);
    }

    private void parar(LicaoViewHolder h) {
        if (h.animacoes != null) {
            for (Animator a : h.animacoes) {
                a.cancel();
            }
            h.animacoes = null;
        }
        h.anel.setAlpha(0f);
        h.balao.setTranslationY(0f);
        animados.remove(h);
    }

    /** Para as animações quando a trilha sai da tela (o onResume da trilha volta a ligar). */
    public void pararAnimacoes() {
        for (LicaoViewHolder h : new ArrayList<>(animados)) {
            parar(h);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof LicaoViewHolder) {
            parar((LicaoViewHolder) holder);
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    private static int cor(Context c, int corRes) {
        return ContextCompat.getColor(c, corRes);
    }

    static class UnidadeViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView face;
        final TextView txtUnidade;
        final TextView txtTemas;

        UnidadeViewHolder(@NonNull View itemView) {
            super(itemView);
            face = itemView.findViewById(R.id.face);
            txtUnidade = itemView.findViewById(R.id.txt_unidade);
            txtTemas = itemView.findViewById(R.id.txt_temas);
        }
    }

    static class LicaoViewHolder extends RecyclerView.ViewHolder {
        final View balao;
        final TextView txtBalao;
        final View grupoNo;
        final View anel;
        final CartaoRelevo no;
        final MaterialCardView faceNo;
        final ImageView imgNo;
        final View rotulo;
        final TextView txtNumero;
        final TextView txtTitulo;
        final TextView txtDetalhe;
        Animator[] animacoes;

        LicaoViewHolder(@NonNull View itemView) {
            super(itemView);
            balao = itemView.findViewById(R.id.balao);
            txtBalao = itemView.findViewById(R.id.txt_balao);
            grupoNo = itemView.findViewById(R.id.grupo_no);
            anel = itemView.findViewById(R.id.anel);
            no = itemView.findViewById(R.id.no);
            faceNo = itemView.findViewById(R.id.face_no);
            imgNo = itemView.findViewById(R.id.img_no);
            rotulo = itemView.findViewById(R.id.rotulo);
            txtNumero = itemView.findViewById(R.id.txt_numero);
            txtTitulo = itemView.findViewById(R.id.txt_titulo);
            txtDetalhe = itemView.findViewById(R.id.txt_detalhe);
        }
    }
}
