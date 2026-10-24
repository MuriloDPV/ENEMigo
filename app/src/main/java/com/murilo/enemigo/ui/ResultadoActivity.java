package com.murilo.enemigo.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.card.MaterialCardView;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.data.QuestaoRepository;
import com.murilo.enemigo.model.Conquista;
import com.murilo.enemigo.model.Licao;
import com.murilo.enemigo.ui.componente.CartaoRelevo;
import com.murilo.enemigo.util.Celebracoes;
import com.murilo.enemigo.util.Celebracoes.Celebracao;
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.Gamificacao;

import java.util.List;

/**
 * Resultado da sessão. Só exibe o que recebeu nos extras: o progresso já foi salvo pela Quiz.
 */
public class ResultadoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado);

        Intent recebida = getIntent();
        boolean modoRevisao = Constantes.MODO_REVISAO.equals(
                recebida.getStringExtra(Constantes.EXTRA_MODO));
        String licaoId = recebida.getStringExtra(Constantes.EXTRA_LICAO_ID);
        int acertos = recebida.getIntExtra(Constantes.EXTRA_ACERTOS, 0);
        int total = recebida.getIntExtra(Constantes.EXTRA_TOTAL, 0);
        boolean concluiu = Gamificacao.concluiu(acertos, total);
        int xp = recebida.getIntExtra(Constantes.EXTRA_XP, 0);
        int bonus = recebida.getIntExtra(Constantes.EXTRA_BONUS, 0);
        int ofensiva = recebida.getIntExtra(Constantes.EXTRA_OFENSIVA, 0);
        boolean metaBatida = recebida.getBooleanExtra(Constantes.EXTRA_META_BATIDA, false);
        boolean desbloqueou = recebida.getBooleanExtra(Constantes.EXTRA_DESBLOQUEOU, false);
        int vidasRecuperadas = recebida.getIntExtra(Constantes.EXTRA_VIDAS_RECUPERADAS, 0);

        // Selo: troféu (perfeito), check (concluída) ou alvo (quase lá)
        TextView txtTitulo = findViewById(R.id.txt_titulo);
        if (acertos == total) {
            txtTitulo.setText(R.string.resultado_perfeito);
            mostrarSelo(R.drawable.ic_trofeu, R.color.marca_acento, R.color.marca_acento_suave,
                    R.color.marca_sobre_acento);
        } else if (concluiu) {
            txtTitulo.setText(R.string.resultado_concluida);
            mostrarSelo(R.drawable.ic_check, R.color.marca_acerto, R.color.marca_acerto_suave,
                    R.color.texto_sobre_cor);
        } else {
            txtTitulo.setText(R.string.resultado_quase);
            mostrarSelo(R.drawable.ic_alvo, R.color.marca_primaria, R.color.marca_primaria_suave,
                    R.color.texto_sobre_cor);
        }

        ((TextView) findViewById(R.id.txt_acertos))
                .setText(getString(R.string.resultado_acertos, acertos, total));

        preencherCartao(findViewById(R.id.card_xp), R.string.resultado_rotulo_xp,
                getString(R.string.resultado_xp, xp + bonus), R.drawable.ic_estrela,
                R.color.marca_acento, R.color.marca_acento_escuro, R.color.marca_sobre_acento,
                R.color.marca_acento_texto);
        preencherCartao(findViewById(R.id.card_ofensiva), R.string.resultado_rotulo_ofensiva,
                getResources().getQuantityString(R.plurals.resultado_ofensiva, ofensiva, ofensiva),
                R.drawable.ic_fogo, R.color.marca_ofensiva, R.color.marca_ofensiva_escura,
                R.color.texto_sobre_cor, R.color.marca_ofensiva_texto);

        // Mensagens, uma por linha
        if (bonus > 0) {
            adicionarMensagem(getString(R.string.resultado_bonus, bonus), R.drawable.ic_estrela, 0);
        }
        if (metaBatida) {
            adicionarMensagem(getString(R.string.resultado_meta_batida), R.drawable.ic_alvo,
                    R.color.marca_acento_texto);
        }
        if (modoRevisao) {
            adicionarMensagem(mensagemRevisao(acertos, vidasRecuperadas), R.drawable.ic_coracao, 0);
        } else if (desbloqueou) {
            adicionarMensagem(getString(R.string.resultado_desbloqueou), R.drawable.ic_cadeado_aberto,
                    R.color.marca_acerto);
        } else if (!concluiu && temProximaLicao(licaoId)) {
            adicionarMensagem(getString(R.string.resultado_faca_acertos,
                            Gamificacao.acertosParaConcluir(total), total),
                    R.drawable.ic_cadeado, R.color.texto_secundario);
        }
        // Aviso das conquistas novas (RN33), uma linha para cada
        List<Celebracao> celebracoes = Celebracoes.deTextos(
                recebida.getStringArrayListExtra(Constantes.EXTRA_CELEBRACOES));
        for (Celebracao c : celebracoes) {
            Conquista conquista = c.tipo == Celebracoes.Tipo.CONQUISTA ? Conquista.fromId(c.valor) : null;
            if (conquista != null) {
                adicionarMensagem(getString(R.string.resultado_conquista,
                        getString(conquista.getTituloRes())), R.drawable.ic_trofeu, R.color.marca_acento_texto);
            }
        }

        // CONTINUAR e o Voltar do sistema fecham esta tela e voltam para a Trilha ou a Revisão.
        findViewById(R.id.btn_continuar).setOnClickListener(v -> finish());

        View btnRefazer = findViewById(R.id.btn_refazer);
        if (modoRevisao || licaoId == null) {
            btnRefazer.setVisibility(View.GONE);
        } else {
            btnRefazer.setOnClickListener(v -> refazer(licaoId));
        }
    }

    private void mostrarSelo(int icone, int corRes, int corHaloRes, int corIconeRes) {
        findViewById(R.id.halo_resultado).setBackgroundTintList(ColorStateList.valueOf(cor(corHaloRes)));
        findViewById(R.id.circulo_resultado).setBackgroundTintList(ColorStateList.valueOf(cor(corRes)));
        ImageView img = findViewById(R.id.img_resultado);
        img.setImageResource(icone);
        img.setImageTintList(ColorStateList.valueOf(cor(corIconeRes)));
    }

    private void preencherCartao(View cartao, int rotulo, String valor, int icone, int corRes,
                                 int corSombraRes, int corRotuloRes, int corValorRes) {
        ((CartaoRelevo) cartao).setCorSombra(cor(corSombraRes));
        ((MaterialCardView) cartao.findViewById(R.id.face)).setCardBackgroundColor(cor(corRes));
        TextView txtRotulo = cartao.findViewById(R.id.txt_rotulo);
        txtRotulo.setText(rotulo);
        txtRotulo.setTextColor(cor(corRotuloRes));
        TextView txtValor = cartao.findViewById(R.id.txt_valor);
        txtValor.setCompoundDrawablesRelativeWithIntrinsicBounds(icone, 0, 0, 0);
        txtValor.setText(valor);
        txtValor.setTextColor(cor(corValorRes));
    }

    /** @param corIconeRes 0 para manter as cores do próprio ícone */
    private void adicionarMensagem(String texto, int icone, int corIconeRes) {
        ViewGroup lista = findViewById(R.id.lista_mensagens);
        TextView linha = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.item_mensagem_resultado, lista, false);
        linha.setText(texto);
        linha.setCompoundDrawablesRelativeWithIntrinsicBounds(icone, 0, 0, 0);
        if (corIconeRes != 0) {
            TextViewCompat.setCompoundDrawableTintList(linha, ColorStateList.valueOf(cor(corIconeRes)));
        }
        lista.addView(linha);
        lista.setVisibility(View.VISIBLE);
    }

    private void refazer(String licaoId) {
        if (ProgressoManager.getInstance(this).getVidas() == 0) {
            Dialogos.mostrarSemVidas(this);
            return;
        }
        findViewById(R.id.btn_refazer).setEnabled(false);
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra(Constantes.EXTRA_LICAO_ID, licaoId);
        intent.putExtra(Constantes.EXTRA_MODO, Constantes.MODO_LICAO);
        startActivity(intent);
        finish();
    }

    private String mensagemRevisao(int removidas, int vidas) {
        if (removidas == 0) {
            return getString(R.string.resultado_revisao_nenhuma);
        }
        String questoes = getResources().getQuantityString(R.plurals.qtd_questoes, removidas, removidas);
        if (vidas == 0) {
            return getString(R.string.resultado_revisao_sem_vidas, questoes);
        }
        return getString(R.string.resultado_revisao, questoes,
                getResources().getQuantityString(R.plurals.qtd_vidas, vidas, vidas));
    }

    private boolean temProximaLicao(String licaoId) {
        QuestaoRepository repositorio = QuestaoRepository.getInstance(this);
        Licao licao = repositorio.getLicao(licaoId);
        return licao != null && repositorio.getLicaoSeguinte(licao) != null;
    }

    private int cor(int corRes) {
        return ContextCompat.getColor(this, corRes);
    }
}
