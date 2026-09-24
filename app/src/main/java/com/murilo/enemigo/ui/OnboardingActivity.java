package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.util.Constantes;

import java.util.regex.Pattern;

/**
 * Primeiro acesso: pede o nome e a meta diária. Só aparece uma vez (RN01).
 */
public class OnboardingActivity extends AppCompatActivity {

    /** Só letras (inclusive acentuadas) e espaços. */
    private static final Pattern SO_LETRAS = Pattern.compile("^[\\p{L} ]+$");

    private TextInputLayout tilNome;
    private TextInputEditText edtNome;
    private RadioGroup rgMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        tilNome = findViewById(R.id.til_nome);
        edtNome = findViewById(R.id.edt_nome);
        rgMeta = findViewById(R.id.rg_meta);

        // Some com o erro assim que o aluno volta a digitar.
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

        findViewById(R.id.btn_comecar).setOnClickListener(v -> comecar());
    }

    private void comecar() {
        Editable texto = edtNome.getText();
        String nome = texto == null ? "" : texto.toString().trim();

        int erro = validarNome(nome);
        if (erro != 0) {
            tilNome.setError(getString(erro));
            return;
        }

        int meta = metaEscolhida();
        if (meta == 0) {
            Toast.makeText(this, R.string.erro_meta, Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressoManager.getInstance(this).concluirOnboarding(nome, meta);
        startActivity(new Intent(this, MainActivity.class));
        // O Voltar da MainActivity não deve voltar para cá.
        finish();
    }

    /** Devolve o id da mensagem de erro, ou 0 se o nome (já sem espaços nas pontas) for válido. */
    private int validarNome(String nome) {
        if (nome.isEmpty()) {
            return R.string.erro_nome_vazio;
        }
        if (nome.length() < Constantes.NOME_MIN) {
            return R.string.erro_nome_curto;
        }
        if (nome.length() > Constantes.NOME_MAX) {
            return R.string.erro_nome_longo;
        }
        if (!SO_LETRAS.matcher(nome).matches()) {
            return R.string.erro_nome_letras;
        }
        return 0;
    }

    /** 20, 50 ou 100; 0 se nenhuma opção estiver marcada. */
    private int metaEscolhida() {
        int id = rgMeta.getCheckedRadioButtonId();
        if (id == R.id.rb_casual) {
            return Constantes.META_CASUAL;
        } else if (id == R.id.rb_regular) {
            return Constantes.META_REGULAR;
        } else if (id == R.id.rb_intenso) {
            return Constantes.META_INTENSO;
        }
        return 0;
    }
}
