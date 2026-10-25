package com.murilo.enemigo.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.notificacao.AgendaLembrete;
import com.murilo.enemigo.notificacao.NotificacaoLembrete;
import com.murilo.enemigo.util.Constantes;
import com.murilo.enemigo.util.Lembrete;
import com.murilo.enemigo.util.ValidacaoUtil;

import java.time.LocalTime;

/**
 * Configurações (C5): nome, meta diária, som, vibração, lembrete diário e o reset do progresso.
 * Nada muda até o SALVAR; sair sem salvar deixa tudo como estava.
 */
public class ConfiguracoesActivity extends AppCompatActivity {

    private static final String ESTADO_HORARIO = "horario_lembrete";
    private static final String TAG_RELOGIO = "relogio_lembrete";

    private TextInputLayout tilNome;
    private TextInputEditText edtNome;
    private RadioGroup rgMeta;
    private MaterialSwitch switchSom;
    private MaterialSwitch switchVibracao;
    private MaterialSwitch switchLembrete;
    private View linhaHorario;
    private TextView txtHorario;
    private TextView txtNotificacoesBloqueadas;
    /** Horário escolhido na tela (minutos desde a meia-noite), gravado só no SALVAR. */
    private int horarioLembrete;
    private ActivityResultLauncher<String> pedidoNotificacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        tilNome = findViewById(R.id.til_nome);
        edtNome = findViewById(R.id.edt_nome);
        rgMeta = findViewById(R.id.rg_meta);
        switchSom = findViewById(R.id.switch_som);
        switchVibracao = findViewById(R.id.switch_vibracao);
        switchLembrete = findViewById(R.id.switch_lembrete);
        linhaHorario = findViewById(R.id.linha_horario);
        txtHorario = findViewById(R.id.txt_horario);
        txtNotificacoesBloqueadas = findViewById(R.id.txt_notificacoes_bloqueadas);
        pedidoNotificacoes = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                concedida -> atualizarLembrete());

        // Preenche com o que está salvo (na recriação da tela, os campos guardam o que foi mexido).
        if (savedInstanceState == null) {
            ProgressoManager progresso = ProgressoManager.getInstance(this);
            PreferenciasManager preferencias = PreferenciasManager.getInstance(this);
            edtNome.setText(progresso.getNome());
            OpcoesMeta.marcar(rgMeta, progresso.getMetaDiaria());
            switchSom.setChecked(preferencias.isSomAtivo());
            switchVibracao.setChecked(preferencias.isVibracaoAtiva());
            switchLembrete.setChecked(preferencias.isLembreteAtivo());
            horarioLembrete = preferencias.getHorarioLembrete();
        } else {
            horarioLembrete = savedInstanceState.getInt(ESTADO_HORARIO, Constantes.LEMBRETE_HORARIO_PADRAO);
        }

        switchLembrete.setOnCheckedChangeListener((botao, ligado) -> atualizarLembrete());
        linhaHorario.setOnClickListener(v -> escolherHorario());
        txtNotificacoesBloqueadas.setOnClickListener(v -> permitirNotificacoes());
        // O relógio aberto sobrevive à recriação da tela, mas o ouvinte dele não.
        MaterialTimePicker relogioAberto =
                (MaterialTimePicker) getSupportFragmentManager().findFragmentByTag(TAG_RELOGIO);
        if (relogioAberto != null) {
            ouvir(relogioAberto);
        }

        edtNome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilNome.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ((TextView) findViewById(R.id.txt_sobre))
                .setText(getString(R.string.config_sobre, getString(R.string.app_name), versao()));

        findViewById(R.id.btn_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.btn_salvar).setOnClickListener(v -> salvar());
        findViewById(R.id.btn_resetar).setOnClickListener(v -> confirmarReset());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Pode estar voltando das configurações do Android, com as notificações liberadas.
        atualizarLembrete();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ESTADO_HORARIO, horarioLembrete);
    }

    /** Horário apagado com o lembrete desligado; o aviso de bloqueio só aparece com ele ligado. */
    private void atualizarLembrete() {
        boolean ligado = switchLembrete.isChecked();
        txtHorario.setText(Lembrete.paraHora(horarioLembrete).toString());
        linhaHorario.setEnabled(ligado);
        linhaHorario.setAlpha(ligado ? 1f : 0.4f);
        txtNotificacoesBloqueadas.setVisibility(
                ligado && !NotificacaoLembrete.permitida(this) ? View.VISIBLE : View.GONE);
    }

    private void escolherHorario() {
        // Toque duplo: o primeiro relógio já basta.
        if (getSupportFragmentManager().findFragmentByTag(TAG_RELOGIO) != null) {
            return;
        }
        LocalTime atual = Lembrete.paraHora(horarioLembrete);
        MaterialTimePicker relogio = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(atual.getHour())
                .setMinute(atual.getMinute())
                .setTitleText(R.string.config_lembrete_relogio)
                .build();
        ouvir(relogio);
        relogio.show(getSupportFragmentManager(), TAG_RELOGIO);
    }

    private void ouvir(MaterialTimePicker relogio) {
        relogio.addOnPositiveButtonClickListener(v -> {
            horarioLembrete = Lembrete.horario(relogio.getHour(), relogio.getMinute());
            atualizarLembrete();
        });
    }

    /**
     * Toque no aviso de bloqueio. No Android 13+, se ainda dá para perguntar, pede a permissão;
     * se o aluno já recusou de vez (ou no Android mais antigo), abre as notificações do app nas
     * configurações do Android. A decisão é sempre dele.
     */
    private void permitirNotificacoes() {
        PreferenciasManager preferencias = PreferenciasManager.getInstance(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && (!preferencias.isNotificacaoPedida()
                || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))) {
            preferencias.marcarNotificacaoPedida();
            pedidoNotificacoes.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
    }

    private void salvar() {
        Editable texto = edtNome.getText();
        String nome = texto == null ? "" : texto.toString();
        int erro = ValidacaoUtil.validarNome(nome);
        if (erro != 0) {
            tilNome.setError(getString(erro));
            return;
        }
        ProgressoManager progresso = ProgressoManager.getInstance(this);
        progresso.setNome(nome);
        progresso.setMetaDiaria(OpcoesMeta.metaMarcada(rgMeta));
        PreferenciasManager preferencias = PreferenciasManager.getInstance(this);
        preferencias.setSomAtivo(switchSom.isChecked());
        preferencias.setVibracaoAtiva(switchVibracao.isChecked());
        preferencias.setLembreteAtivo(switchLembrete.isChecked());
        preferencias.setHorarioLembrete(horarioLembrete);
        // Remarca no horário novo (ou cancela, se desligou).
        AgendaLembrete.reagendar(this);
        Toast.makeText(this, R.string.config_salvas, Toast.LENGTH_SHORT).show();
        finish();
    }

    /** RN34: confirmação obrigatória antes de apagar tudo. */
    private void confirmarReset() {
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.reset_titulo)
                .setMessage(R.string.reset_mensagem)
                .setNegativeButton(R.string.reset_cancelar, (d, which) -> d.dismiss())
                .setPositiveButton(R.string.reset_confirmar, (d, which) -> resetar())
                .show();
        // "Apagar tudo" em vermelho: é a ação que não tem volta.
        dialogo.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.marca_erro_texto));
    }

    /** Apaga o progresso e volta ao primeiro acesso, sem deixar as telas antigas na pilha. */
    private void resetar() {
        ProgressoManager.getInstance(this).resetarTudo();
        // O lembrete volta ao padrão (ligado, 19h) e só avisa depois do novo primeiro acesso.
        AgendaLembrete.reagendar(this);
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String versao() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ? pm.getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(0))
                    : pm.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
