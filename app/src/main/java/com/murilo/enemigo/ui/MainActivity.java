package com.murilo.enemigo.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.murilo.enemigo.R;

/**
 * "Casca" do app: troca entre Início, Revisão e Perfil pela barra inferior.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

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
            mostrarFragment(new InicioFragment());
        }
    }

    private void mostrarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
