package com.murilo.enemigo.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.murilo.enemigo.R;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.util.Constantes;

/**
 * "Casca" do app: troca entre Início, Revisão e Perfil pela barra inferior.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private boolean abrirRevisaoPendente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // RN01: primeiro acesso vai para o onboarding.
        if (!ProgressoManager.getInstance(this).isOnboardingConcluido()) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                mostrarFragment(new InicioFragment());
            } else if (id == R.id.nav_revisao) {
                mostrarFragment(new RevisaoFragment());
            } else if (id == R.id.nav_perfil) {
                mostrarFragment(new PerfilFragment());
            }
            return true;
        });

        // Tocar de novo na aba que já está aberta não faz nada.
        bottomNav.setOnItemReselectedListener(item -> {
        });

        if (savedInstanceState == null) {
            if (consumirPedidoDeRevisao(getIntent())) {
                bottomNav.setSelectedItemId(R.id.nav_revisao);
            } else {
                mostrarFragment(new InicioFragment());
            }
        }
    }

    /** Chega aqui pelo "Ir para Revisão" (a MainActivity já estava aberta embaixo). */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        abrirRevisaoPendente = consumirPedidoDeRevisao(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A troca de aba fica para o onResume, quando já é seguro trocar fragments.
        if (abrirRevisaoPendente) {
            abrirRevisaoPendente = false;
            bottomNav.setSelectedItemId(R.id.nav_revisao);
        }
    }

    private boolean consumirPedidoDeRevisao(Intent intent) {
        boolean abrir = intent.getBooleanExtra(Constantes.EXTRA_ABRIR_REVISAO, false);
        intent.removeExtra(Constantes.EXTRA_ABRIR_REVISAO);
        return abrir;
    }

    private void mostrarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
