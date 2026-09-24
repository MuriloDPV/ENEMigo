package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.util.Constantes;

/**
 * Aba Início: barra de status, meta diária e os 4 cartões das áreas.
 * Tudo é atualizado no onResume, para refletir o que mudou numa lição.
 */
public class InicioFragment extends Fragment {

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

        configurarClique(view, R.id.card_linguagens, Area.LINGUAGENS);
        configurarClique(view, R.id.card_humanas, Area.HUMANAS);
        configurarClique(view, R.id.card_natureza, Area.NATUREZA);
        configurarClique(view, R.id.card_matematica, Area.MATEMATICA);
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
                .setText(getString(R.string.saudacao, progresso.getNome()));

        // Meta diária (RN18: o XP de hoje zera quando muda o dia)
        int meta = progresso.getMetaDiaria();
        int xpHoje = progresso.getXpHoje();
        TextView txtMeta = view.findViewById(R.id.txt_meta);
        if (xpHoje >= meta) {
            txtMeta.setText(R.string.meta_batida);
        } else {
            txtMeta.setText(getString(R.string.meta_progresso, xpHoje, meta));
        }
        LinearProgressIndicator progressMeta = view.findViewById(R.id.progress_meta);
        progressMeta.setMax(meta);
        progressMeta.setProgress(Math.min(xpHoje, meta));

        atualizarCard(view, R.id.txt_prog_linguagens, Area.LINGUAGENS, progresso);
        atualizarCard(view, R.id.txt_prog_humanas, Area.HUMANAS, progresso);
        atualizarCard(view, R.id.txt_prog_natureza, Area.NATUREZA, progresso);
        atualizarCard(view, R.id.txt_prog_matematica, Area.MATEMATICA, progresso);
    }

    private void configurarClique(View raiz, int cardId, Area area) {
        raiz.findViewById(cardId).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TrilhaActivity.class);
            intent.putExtra(Constantes.EXTRA_AREA_ID, area.name());
            startActivity(intent);
        });
    }

    /** "2/3 lições" ou "Em breve" se a área não tiver lições. */
    private void atualizarCard(View raiz, int progressoId, Area area, ProgressoManager progresso) {
        int total = QuestaoRepository.getInstance(requireContext()).getTotalLicoes(area);
        TextView txtProgresso = raiz.findViewById(progressoId);
        if (total == 0) {
            txtProgresso.setText(R.string.area_em_breve);
        } else {
            txtProgresso.setText(getString(R.string.progresso_area,
                    progresso.getLicoesConcluidas(area), total));
        }
    }
}
