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
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Area;
import com.murilo.enemigo.util.Constantes;

/**
 * Aba Início: barra de status, meta diária e os 4 cartões das áreas.
 */
public class InicioFragment extends Fragment {

    // MOCK (Aula 1): valores fixos. Na Aula 3 vêm do ProgressoManager.
    private static final String NOME_MOCK = "estudante";
    private static final int OFENSIVA_MOCK = 3;
    private static final int XP_TOTAL_MOCK = 240;
    private static final int VIDAS_MOCK = 5;
    private static final int XP_HOJE_MOCK = 32;
    private static final int META_MOCK = 50;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Barra de status
        View status = view.findViewById(R.id.status);
        TextView txtOfensiva = status.findViewById(R.id.txt_ofensiva);
        TextView txtXp = status.findViewById(R.id.txt_xp);
        TextView txtVidas = status.findViewById(R.id.txt_vidas);
        txtOfensiva.setText(getString(R.string.status_ofensiva, OFENSIVA_MOCK));
        txtXp.setText(getString(R.string.status_xp, XP_TOTAL_MOCK));
        txtVidas.setText(getString(R.string.status_vidas, VIDAS_MOCK));

        // Saudação e meta diária
        TextView txtSaudacao = view.findViewById(R.id.txt_saudacao);
        txtSaudacao.setText(getString(R.string.saudacao, NOME_MOCK));

        TextView txtMeta = view.findViewById(R.id.txt_meta);
        txtMeta.setText(getString(R.string.meta_progresso, XP_HOJE_MOCK, META_MOCK));

        LinearProgressIndicator progressMeta = view.findViewById(R.id.progress_meta);
        progressMeta.setMax(META_MOCK);
        progressMeta.setProgress(XP_HOJE_MOCK);

        // Cartões das áreas (ou o aviso de erro, se o JSON não pôde ser lido)
        QuestaoRepository repositorio = QuestaoRepository.getInstance(requireContext());
        boolean carregou = repositorio.isCarregadoComSucesso();
        view.findViewById(R.id.grade_areas).setVisibility(carregou ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.txt_erro_questoes).setVisibility(carregou ? View.GONE : View.VISIBLE);

        configurarCard(view, R.id.card_linguagens, R.id.txt_prog_linguagens, Area.LINGUAGENS);
        configurarCard(view, R.id.card_humanas, R.id.txt_prog_humanas, Area.HUMANAS);
        configurarCard(view, R.id.card_natureza, R.id.txt_prog_natureza, Area.NATUREZA);
        configurarCard(view, R.id.card_matematica, R.id.txt_prog_matematica, Area.MATEMATICA);
    }

    private void configurarCard(View raiz, int cardId, int progressoId, Area area) {
        int total = QuestaoRepository.getInstance(requireContext()).getTotalLicoes(area);
        TextView txtProgresso = raiz.findViewById(progressoId);
        if (total == 0) {
            txtProgresso.setText(R.string.area_em_breve);
        } else {
            txtProgresso.setText(getResources()
                    .getQuantityString(R.plurals.total_licoes_area, total, total));
        }

        raiz.findViewById(cardId).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), TrilhaActivity.class);
            intent.putExtra(Constantes.EXTRA_AREA_ID, area.name());
            startActivity(intent);
        });
    }
}
