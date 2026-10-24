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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Questao;
import com.murilo.enemigo.ui.adapter.CadernoAdapter;
import com.murilo.enemigo.ui.componente.MascoteView;
import com.murilo.enemigo.util.Constantes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aba Revisão: caderno de erros e o botão que abre o Quiz no modo REVISAO.
 */
public class RevisaoFragment extends Fragment {

    private final CadernoAdapter adapter = new CadernoAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_revisao, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvCaderno = view.findViewById(R.id.rv_caderno);
        rvCaderno.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCaderno.setAdapter(adapter);

        view.findViewById(R.id.btn_revisar).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), QuizActivity.class);
            intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_REVISAO);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view == null) {
            return;
        }

        List<Questao> questoes = carregarCaderno();
        adapter.setQuestoes(questoes);

        boolean vazio = questoes.isEmpty();
        view.findViewById(R.id.rv_caderno).setVisibility(vazio ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.layout_vazio).setVisibility(vazio ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.btn_revisar).setEnabled(!vazio);
        MascoteView mascote = view.findViewById(R.id.mascote_vazio);
        if (vazio) {
            mascote.balancar();
        } else {
            mascote.parar();
        }

        TextView txtContador = view.findViewById(R.id.txt_contador);
        if (vazio) {
            txtContador.setText(R.string.caderno_contador_vazio);
        } else {
            txtContador.setText(getResources().getQuantityString(
                    R.plurals.caderno_contador, questoes.size(), questoes.size()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Fora da tela, a Lia do caderno vazio para de balançar.
        View view = getView();
        if (view != null) {
            ((MascoteView) view.findViewById(R.id.mascote_vazio)).parar();
        }
    }

    /** Questões do caderno em ordem de id. RN31: ids que não existem mais saem do caderno. */
    private List<Questao> carregarCaderno() {
        ProgressoManager progresso = ProgressoManager.getInstance(requireContext());
        QuestaoRepository repositorio = QuestaoRepository.getInstance(requireContext());

        List<String> ids = new ArrayList<>(progresso.getCaderno());
        Collections.sort(ids);
        List<Questao> questoes = new ArrayList<>();
        for (String id : ids) {
            Questao q = repositorio.getQuestao(id);
            if (q == null) {
                progresso.removerDoCaderno(id);
            } else {
                questoes.add(q);
            }
        }
        return questoes;
    }
}
