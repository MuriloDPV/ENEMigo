package com.murilo.enemigo.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.util.ValidacaoUtil;

/**
 * Configurações (C5): nome, meta diária, som, vibração e o reset do progresso.
 * Nada muda até o SALVAR; sair sem salvar deixa tudo como estava.
 */
public class ConfiguracoesActivity extends AppCompatActivity {

    private TextInputLayout tilNome;
    private TextInputEditText edtNome;
    private RadioGroup rgMeta;
    private MaterialSwitch switchSom;
    private MaterialSwitch switchVibracao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        tilNome = findViewById(R.id.til_nome);
        edtNome = findViewById(R.id.edt_nome);
        rgMeta = findViewById(R.id.rg_meta);
        switchSom = findViewById(R.id.switch_som);
        switchVibracao = findViewById(R.id.switch_vibracao);

        // Preenche com o que está salvo (na recriação da tela, os campos guardam o que foi mexido).
        if (savedInstanceState == null) {
            ProgressoManager progresso = ProgressoManager.getInstance(this);
            PreferenciasManager preferencias = PreferenciasManager.getInstance(this);
            edtNome.setText(progresso.getNome());
            OpcoesMeta.marcar(rgMeta, progresso.getMetaDiaria());
            switchSom.setChecked(preferencias.isSomAtivo());
            switchVibracao.setChecked(preferencias.isVibracaoAtiva());
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
