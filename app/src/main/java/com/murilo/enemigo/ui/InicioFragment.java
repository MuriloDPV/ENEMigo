package com.murilo.enemigo.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.ui.componente.CartaoRelevo;
import com.murilo.enemigo.util.Constantes;

/**
 * Aba Início: barra de status, meta diária e os 4 cartões das áreas.
 * Tudo é atualizado no onResume, para refletir o que mudou numa lição.
 */
public class InicioFragment extends Fragment {

    private static final int[] CARTOES = {
            R.id.card_linguagens, R.id.card_humanas, R.id.card_natureza, R.id.card_matematica};
    private static final Area[] AREAS = {Area.LINGUAGENS, Area.HUMANAS, Area.NATUREZA, Area.MATEMATICA};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Cartões das áreas (ou o aviso de erro, se o JSON não pôde ser lido)
        boolean carregou = QuestaoRepository.getInstance(requireContext()).isCarregadoComSucesso();
        view.findViewById(R.id.grade_areas).setVisibility(carregou ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.txt_erro_questoes).setVisibility(carregou ? View.GONE : View.VISIBLE);

        for (int i = 0; i < CARTOES.length; i++) {
            configurarCartao(view.findViewById(CARTOES[i]), AREAS[i]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) {
            return;
        }
        ProgressoManager progresso = ProgressoManager.getInstance(requireContext());

        BarraStatus.atualizar(view.findViewById(R.id.status));

        ((TextView) view.findViewById(R.id.txt_saudacao))
                .setText(getString(R.string.saudacao_titulo, progresso.getNome()));

        // Meta diária (RN18: o XP de hoje zera quando muda o dia)
        int meta = progresso.getMetaDiaria();
        int xpHoje = progresso.getXpHoje();
        boolean batida = xpHoje >= meta;
        TextView txtMeta = view.findViewById(R.id.txt_meta);
        if (batida) {
            txtMeta.setText(R.string.meta_batida);
        } else {
            txtMeta.setText(getString(R.string.meta_progresso, xpHoje, meta));
        }
        // Meta batida: o alvo vira um check verde.
        ImageView imgMeta = view.findViewById(R.id.img_meta);
        imgMeta.setImageResource(batida ? R.drawable.ic_check : R.drawable.ic_alvo);
        imgMeta.setImageTintList(ColorStateList.valueOf(cor(batida ? R.color.texto_sobre_cor
                : R.color.marca_acento_texto)));
        view.findViewById(R.id.circulo_meta).setBackgroundTintList(ColorStateList.valueOf(
                cor(batida ? R.color.marca_acerto : R.color.marca_acento_suave)));

        LinearProgressIndicator progressMeta = view.findViewById(R.id.progress_meta);
        progressMeta.setMax(meta);
        progressMeta.setProgressCompat(Math.min(xpHoje, meta), true);

        for (int i = 0; i < CARTOES.length; i++) {
            atualizarProgresso(view.findViewById(CARTOES[i]), AREAS[i], progresso);
        }
    }

    /** Cores, nome, ilustração e clique do cartão da área. */
    private void configurarCartao(View cartao, Area area) {
        int corSobre = cor(area.getCorSobreRes());
        ((CartaoRelevo) cartao).setCorSombra(cor(area.getCorEscuraRes()));
        ((MaterialCardView) cartao.findViewById(R.id.face)).setCardBackgroundColor(cor(area.getCorRes()));
        ((ImageView) cartao.findViewById(R.id.img_ilustracao)).setImageResource(area.getIlustracaoRes());

        TextView txtNome = cartao.findViewById(R.id.txt_nome);
        txtNome.setText(area.getNome());
        txtNome.setTextColor(corSobre);

        // Etiqueta do progresso: a cor do conteúdo, bem transparente, por trás do texto.
        TextView txtProgresso = cartao.findViewById(R.id.txt_progresso);
        txtProgresso.setTextColor(corSobre);
        txtProgresso.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(corSobre, 46)));

        cartao.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TrilhaActivity.class);
            intent.putExtra(Constantes.EXTRA_AREA_ID, area.name());
            startActivity(intent);
        });
    }

    /** "2/4 lições" ou "Em breve" se a área não tiver lições. */
    private void atualizarProgresso(View cartao, Area area, ProgressoManager progresso) {
        int total = QuestaoRepository.getInstance(requireContext()).getTotalLicoes(area);
        TextView txtProgresso = cartao.findViewById(R.id.txt_progresso);
        if (total == 0) {
            txtProgresso.setText(R.string.area_em_breve);
        } else {
            txtProgresso.setText(getString(R.string.progresso_area,
                    progresso.getLicoesConcluidas(area), total));
        }
    }

    private int cor(int corRes) {
        return ContextCompat.getColor(requireContext(), corRes);
    }
}
