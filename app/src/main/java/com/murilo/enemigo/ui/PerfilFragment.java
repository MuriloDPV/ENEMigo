package com.murilo.enemigo.ui;

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
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.Gamificacao;

/**
 * Aba Perfil (versão básica da Aula 3): nome, nível, barra de XP e estatísticas.
 */
public class PerfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) {
            return;
        }
        ProgressoManager progresso = ProgressoManager.getInstance(requireContext());
        int xp = progresso.getXpTotal();
        int nivel = progresso.getNivel();
        int xpNoNivel = Gamificacao.calcularXpNoNivel(xp);

        ((TextView) view.findViewById(R.id.txt_nome)).setText(progresso.getNome());
        ((TextView) view.findViewById(R.id.txt_nivel)).setText(getString(R.string.perfil_nivel, nivel));
        LinearProgressIndicator progressNivel = view.findViewById(R.id.progress_nivel);
        progressNivel.setMax(Constantes.XP_POR_NIVEL);
        progressNivel.setProgress(xpNoNivel);
        ((TextView) view.findViewById(R.id.txt_xp_proximo)).setText(getString(
                R.string.perfil_xp_proximo, xpNoNivel, Constantes.XP_POR_NIVEL, nivel + 1));

        int respondidas = progresso.getTotalRespondidas();
        int taxa = Gamificacao.calcularTaxaAcerto(progresso.getTotalAcertos(), respondidas);

        preencher(view, R.id.stat_xp, String.valueOf(xp), R.string.stat_xp_total);
        preencher(view, R.id.stat_ofensiva, String.valueOf(progresso.getOfensivaAtual()),
                R.string.stat_ofensiva);
        preencher(view, R.id.stat_maior_ofensiva, String.valueOf(progresso.getMaiorOfensiva()),
                R.string.stat_maior_ofensiva);
        preencher(view, R.id.stat_licoes, String.valueOf(progresso.getTotalLicoesConcluidas()),
                R.string.stat_licoes);
        preencher(view, R.id.stat_respondidas, String.valueOf(respondidas), R.string.stat_respondidas);
        preencher(view, R.id.stat_taxa,
                taxa < 0 ? getString(R.string.stat_sem_valor) : getString(R.string.stat_taxa_valor, taxa),
                R.string.stat_taxa);
    }

    private void preencher(View raiz, int cardId, String valor, int rotuloRes) {
        View card = raiz.findViewById(cardId);
        ((TextView) card.findViewById(R.id.txt_valor)).setText(valor);
        ((TextView) card.findViewById(R.id.txt_rotulo)).setText(rotuloRes);
    }
}
