package com.murilo.enemigo.ui;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.model.Conquista;
import com.murilo.enemigo.ui.adapter.ConquistaAdapter;
import com.murilo.enemigo.ui.componente.AvatarDrawable;
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.Gamificacao;

import java.util.Set;

/**
 * Aba Perfil: nome, nível, barra de XP, estatísticas e a grade de conquistas.
 * A engrenagem abre as Configurações.
 */
public class PerfilFragment extends Fragment {

    private final ConquistaAdapter adapterConquistas = new ConquistaAdapter(this::mostrarConquista);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_configuracoes).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ConfiguracoesActivity.class)));
        view.findViewById(R.id.btn_avatar).setOnClickListener(v ->
                startActivity(AvatarActivity.criarIntent(requireContext(), false)));

        RecyclerView rvConquistas = view.findViewById(R.id.rv_conquistas);
        rvConquistas.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        rvConquistas.setAdapter(adapterConquistas);
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

        ((ImageView) view.findViewById(R.id.img_avatar)).setImageDrawable(AvatarDrawable.criar(
                requireContext(), PreferenciasManager.getInstance(requireContext()).getAvatar()));
        ((TextView) view.findViewById(R.id.txt_nome)).setText(progresso.getNome());
        ((TextView) view.findViewById(R.id.txt_nivel)).setText(getString(R.string.perfil_nivel, nivel));
        LinearProgressIndicator progressNivel = view.findViewById(R.id.progress_nivel);
        progressNivel.setMax(Constantes.XP_POR_NIVEL);
        progressNivel.setProgressCompat(xpNoNivel, true);
        ((TextView) view.findViewById(R.id.txt_xp_proximo)).setText(getString(
                R.string.perfil_xp_proximo, xpNoNivel, Constantes.XP_POR_NIVEL, nivel + 1));

        int respondidas = progresso.getTotalRespondidas();
        int taxa = Gamificacao.calcularTaxaAcerto(progresso.getTotalAcertos(), respondidas);

        preencher(view, R.id.stat_xp, String.valueOf(xp), R.string.stat_xp_total,
                R.drawable.ic_estrela, 0);
        preencher(view, R.id.stat_ofensiva, String.valueOf(progresso.getOfensivaAtual()),
                R.string.stat_ofensiva, R.drawable.ic_fogo, 0);
        preencher(view, R.id.stat_maior_ofensiva, String.valueOf(progresso.getMaiorOfensiva()),
                R.string.stat_maior_ofensiva, R.drawable.ic_trofeu, R.color.marca_acento_texto);
        preencher(view, R.id.stat_licoes, String.valueOf(progresso.getTotalLicoesConcluidas()),
                R.string.stat_licoes, R.drawable.ic_check, R.color.marca_acerto);
        preencher(view, R.id.stat_respondidas, String.valueOf(respondidas), R.string.stat_respondidas,
                R.drawable.ic_caderno, R.color.marca_primaria_texto);
        preencher(view, R.id.stat_taxa,
                taxa < 0 ? getString(R.string.stat_sem_valor) : getString(R.string.stat_taxa_valor, taxa),
                R.string.stat_taxa, R.drawable.ic_alvo, R.color.marca_erro);

        // Tempo real medido no quiz (RN38)
        int media = progresso.getTempoMedioSegundos();
        preencher(view, R.id.stat_tempo_medio,
                media < 0 ? getString(R.string.stat_sem_valor) : getString(R.string.stat_segundos_valor, media),
                R.string.stat_tempo_medio, R.drawable.ic_relogio, R.color.marca_ofensiva_texto);
        preencher(view, R.id.stat_minutos_hoje,
                getString(R.string.stat_minutos_valor, progresso.getMinutosHoje()),
                R.string.stat_minutos_hoje, R.drawable.ic_relogio, R.color.marca_acento_texto);

        // Conquistas (C3): acesas as desbloqueadas, cinza as outras
        Set<String> desbloqueadas = progresso.getConquistas();
        int quantas = 0;
        for (Conquista c : Conquista.values()) {
            if (desbloqueadas.contains(c.getId())) {
                quantas++;
            }
        }
        ((TextView) view.findViewById(R.id.txt_conquistas_titulo)).setText(
                getString(R.string.perfil_conquistas, quantas, Conquista.values().length));
        view.findViewById(R.id.txt_conquistas_vazio).setVisibility(quantas == 0 ? View.VISIBLE : View.GONE);
        adapterConquistas.setDesbloqueadas(desbloqueadas);
    }

    /** Medalha grande, título, descrição e se já foi desbloqueada. */
    @SuppressLint("InflateParams") // o conteúdo de um diálogo não tem pai na hora de inflar
    private void mostrarConquista(Conquista conquista, boolean desbloqueada) {
        View conteudo = LayoutInflater.from(requireContext()).inflate(R.layout.dialogo_conquista, null);
        ConquistaAdapter.pintarMedalha(conteudo.findViewById(R.id.img_medalha), conquista, desbloqueada);
        ((TextView) conteudo.findViewById(R.id.txt_titulo)).setText(conquista.getTituloRes());
        ((TextView) conteudo.findViewById(R.id.txt_descricao)).setText(conquista.getDescricaoRes());
        TextView situacao = conteudo.findViewById(R.id.txt_situacao);
        situacao.setText(desbloqueada ? R.string.conquista_desbloqueada : R.string.conquista_bloqueada);
        situacao.setTextColor(ContextCompat.getColor(requireContext(),
                desbloqueada ? R.color.marca_acerto_texto : R.color.texto_secundario));
        situacao.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                desbloqueada ? R.color.marca_acerto_suave : R.color.superficie_variante)));
        new MaterialAlertDialogBuilder(requireContext())
                .setView(conteudo)
                .setPositiveButton(R.string.btn_ok, (d, which) -> d.dismiss())
                .show();
    }

    /** @param corIconeRes 0 para manter as cores do próprio ícone */
    private void preencher(View raiz, int cardId, String valor, int rotuloRes, int icone, int corIconeRes) {
        View card = raiz.findViewById(cardId);
        ((TextView) card.findViewById(R.id.txt_valor)).setText(valor);
        ((TextView) card.findViewById(R.id.txt_rotulo)).setText(rotuloRes);
        ImageView img = card.findViewById(R.id.img_icone);
        img.setImageResource(icone);
        img.setImageTintList(corIconeRes == 0 ? null
                : ColorStateList.valueOf(ContextCompat.getColor(requireContext(), corIconeRes)));
    }
}
