package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.ui.componente.MascoteView;
import com.murilo.enemigo.util.ValidacaoUtil;

/**
 * Primeiro acesso: pede o nome e a meta diária. Só aparece uma vez (RN01).
 */
public class OnboardingActivity extends AppCompatActivity {

    private TextInputLayout tilNome;
    private TextInputEditText edtNome;
    private RadioGroup rgMeta;
    private MascoteView mascote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // O Grifo se apresenta e fala do app
        ((TextView) findViewById(R.id.txt_titulo))
                .setText(getString(R.string.onboarding_titulo, getString(R.string.mascote_nome)));
        ((TextView) findViewById(R.id.txt_subtitulo))
                .setText(getString(R.string.onboarding_subtitulo, getString(R.string.app_name)));
        mascote = findViewById(R.id.mascote);
        mascote.balancar();

        tilNome = findViewById(R.id.til_nome);
        edtNome = findViewById(R.id.edt_nome);
        rgMeta = findViewById(R.id.rg_meta);

        // Escolher a meta deixa o Grifo animado.
        rgMeta.setOnCheckedChangeListener((grupo, id) -> {
            mascote.setExpressao(MascoteView.Expressao.FELIZ);
            mascote.pular();
        });

        // Some com o erro (e com a cara triste) assim que o aluno volta a digitar.
        edtNome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilNome.setError(null);
                mascote.setExpressao(MascoteView.Expressao.FELIZ);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btn_comecar).setOnClickListener(v -> comecar());
    }

    private void comecar() {
        Editable texto = edtNome.getText();
        String nome = texto == null ? "" : texto.toString().trim();

        int erro = ValidacaoUtil.validarNome(nome);
        if (erro != 0) {
            tilNome.setError(getString(erro));
            mascote.setExpressao(MascoteView.Expressao.TRISTE);
            mascote.tremer();
            return;
        }

        int meta = OpcoesMeta.metaMarcada(rgMeta);
        if (meta == 0) {
            Toast.makeText(this, R.string.erro_meta, Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressoManager.getInstance(this).concluirOnboarding(nome, meta);
        startActivity(new Intent(this, MainActivity.class));
        // O Voltar da MainActivity não deve voltar para cá.
        finish();
    }
}
