package com.murilo.enemigo.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.model.Avatar;
import com.murilo.enemigo.model.Avatar.Parte;
import com.murilo.enemigo.ui.componente.AvatarDrawable;
import com.murilo.enemigo.ui.componente.BotaoRelevo;

import java.util.Random;

/**
 * Editor do avatar, com prévia ao vivo: escolhe a parte nos chips e a opção na grade. SORTEAR
 * monta um aleatório; SALVAR grava. No primeiro acesso (depois do onboarding) dá para pular.
 */
public class AvatarActivity extends AppCompatActivity {

    private static final String EXTRA_PRIMEIRO_ACESSO = "EXTRA_PRIMEIRO_ACESSO";
    private static final String ESTADO_AVATAR = "avatar";
    private static final String ESTADO_PARTE = "parte";

    private final OpcoesAdapter adapter = new OpcoesAdapter();
    private boolean primeiroAcesso;
    private Avatar avatar;
    private Parte parte = Parte.PELE;
    private ImageView imgAvatar;

    public static Intent criarIntent(Context context, boolean primeiroAcesso) {
        return new Intent(context, AvatarActivity.class).putExtra(EXTRA_PRIMEIRO_ACESSO, primeiroAcesso);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);

        primeiroAcesso = getIntent().getBooleanExtra(EXTRA_PRIMEIRO_ACESSO, false);
        if (savedInstanceState == null) {
            avatar = PreferenciasManager.getInstance(this).getAvatar();
        } else {
            avatar = Avatar.deTexto(savedInstanceState.getString(ESTADO_AVATAR));
            parte = Parte.values()[savedInstanceState.getInt(ESTADO_PARTE, 0)];
        }

        imgAvatar = findViewById(R.id.img_avatar);
        RecyclerView rvOpcoes = findViewById(R.id.rv_opcoes);
        rvOpcoes.setLayoutManager(new GridLayoutManager(this, 4));
        rvOpcoes.setAdapter(adapter);
        montarChips();

        BotaoRelevo btnSalvar = findViewById(R.id.btn_salvar);
        if (primeiroAcesso) {
            // Logo depois do onboarding: dá para pular (fica o avatar padrão).
            ((TextView) findViewById(R.id.txt_titulo)).setText(R.string.avatar_titulo_primeiro_acesso);
            findViewById(R.id.txt_subtitulo).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_voltar).setVisibility(View.GONE);
            View btnPular = findViewById(R.id.btn_pular);
            btnPular.setVisibility(View.VISIBLE);
            btnPular.setOnClickListener(v -> sair());
            btnSalvar.setText(R.string.btn_pronto);
        } else {
            findViewById(R.id.btn_voltar).setOnClickListener(v -> sair());
        }
        btnSalvar.setOnClickListener(v -> salvar());
        findViewById(R.id.btn_sortear).setOnClickListener(v -> {
            avatar = Avatar.sortear(new Random());
            atualizar(true);
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sair();
            }
        });

        atualizar(false);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_AVATAR, avatar.paraTexto());
        outState.putInt(ESTADO_PARTE, parte.ordinal());
    }

    /** Um chip para cada parte (Pele, Rosto, Cabelo...). */
    private void montarChips() {
        ChipGroup grupo = findViewById(R.id.grupo_partes);
        String[] nomes = getResources().getStringArray(R.array.avatar_partes);
        for (Parte p : Parte.values()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_parte_avatar, grupo, false);
            chip.setId(View.generateViewId());
            chip.setText(nomes[p.ordinal()]);
            chip.setTag(p);
            grupo.addView(chip);
            if (p == parte) {
                grupo.check(chip.getId());
            }
        }
        grupo.setOnCheckedStateChangeListener((g, ids) -> {
            if (!ids.isEmpty()) {
                trocarParte((Parte) g.findViewById(ids.get(0)).getTag());
            }
        });
    }

    private void trocarParte(Parte nova) {
        int antes = adapter.getItemCount();
        parte = nova;
        adapter.notifyItemRangeRemoved(0, antes);
        adapter.notifyItemRangeInserted(0, adapter.getItemCount());
    }

    private void escolher(int opcao) {
        if (avatar.get(parte) != opcao) {
            avatar = avatar.com(parte, opcao);
            atualizar(true);
        }
    }

    /** Redesenha a prévia (com um pulinho) e as opções, que mostram o avatar atual. */
    private void atualizar(boolean animar) {
        imgAvatar.setImageDrawable(AvatarDrawable.criar(this, avatar));
        adapter.notifyItemRangeChanged(0, adapter.getItemCount());
        if (animar) {
            imgAvatar.setScaleX(0.9f);
            imgAvatar.setScaleY(0.9f);
            imgAvatar.animate().scaleX(1f).scaleY(1f).setDuration(260)
                    .setInterpolator(new OvershootInterpolator(3f)).start();
        }
    }

    private void salvar() {
        PreferenciasManager.getInstance(this).setAvatar(avatar);
        if (primeiroAcesso) {
            sair();
        } else {
            Toast.makeText(this, R.string.avatar_salvo, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /** No primeiro acesso, segue para o app; senão, só fecha (sem salvar). */
    private void sair() {
        if (primeiroAcesso) {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    /** As opções da parte escolhida: avatares pequenos (formas) ou bolinhas (cores). */
    private class OpcoesAdapter extends RecyclerView.Adapter<OpcoesAdapter.OpcaoViewHolder> {

        @NonNull
        @Override
        public OpcaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OpcaoViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_opcao_avatar, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OpcaoViewHolder holder, int position) {
            Context context = holder.itemView.getContext();
            boolean escolhida = avatar.get(parte) == position;
            String descricao;
            if (AvatarDrawable.ehCor(parte)) {
                GradientDrawable bolinha = new GradientDrawable();
                bolinha.setShape(GradientDrawable.OVAL);
                bolinha.setColor(AvatarDrawable.corDaOpcao(context, parte, position));
                bolinha.setStroke(dp(2), ContextCompat.getColor(context, R.color.superficie_borda));
                holder.imgOpcao.setImageDrawable(bolinha);
                descricao = getString(R.string.cd_avatar_opcao_cor, nomeDaParte(), position + 1);
            } else {
                holder.imgOpcao.setImageDrawable(AvatarDrawable.criar(context, avatar.com(parte, position)));
                descricao = getString(R.string.cd_avatar_opcao, nomeDaParte(), nomeDaOpcao(position));
            }
            if (escolhida) {
                GradientDrawable anel = new GradientDrawable();
                anel.setShape(GradientDrawable.OVAL);
                anel.setColor(Color.TRANSPARENT);
                anel.setStroke(dp(3), ContextCompat.getColor(context, R.color.marca_primaria));
                holder.anel.setBackground(anel);
                descricao = getString(R.string.cd_avatar_escolhida, descricao);
            } else {
                holder.anel.setBackground(null);
            }
            holder.itemView.setContentDescription(descricao);
            holder.itemView.setOnClickListener(v -> {
                int opcao = holder.getAdapterPosition();
                if (opcao != RecyclerView.NO_POSITION) {
                    escolher(opcao);
                }
            });
        }

        @Override
        public int getItemCount() {
            return parte.opcoes;
        }

        private String nomeDaParte() {
            return getResources().getStringArray(R.array.avatar_partes)[parte.ordinal()];
        }

        private String nomeDaOpcao(int opcao) {
            int nomes;
            if (parte == Parte.ROSTO) {
                nomes = R.array.avatar_rostos;
            } else if (parte == Parte.CABELO) {
                nomes = R.array.avatar_cabelos;
            } else if (parte == Parte.OLHOS) {
                nomes = R.array.avatar_olhos;
            } else {
                nomes = R.array.avatar_acessorios;
            }
            return getResources().getStringArray(nomes)[opcao];
        }

        class OpcaoViewHolder extends RecyclerView.ViewHolder {
            final View anel;
            final ImageView imgOpcao;

            OpcaoViewHolder(@NonNull View itemView) {
                super(itemView);
                anel = itemView.findViewById(R.id.anel);
                imgOpcao = itemView.findViewById(R.id.img_opcao);
            }
        }
    }
}
