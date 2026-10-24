package com.murilo.enemigo.ui;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Retorno de som e vibração: acerto, erro (com vibração curta, C10) e lição concluída.
 * Os sons ficam em res/raw e são gerados por tools/gerar_sons.py. Respeita as opções das
 * Configurações. Crie um por tela e chame liberar() no onDestroy.
 */
public final class Retorno {

    private static final float VOLUME = 0.9f;
    private static final long VIBRACAO_MS = 70;

    private final Context context;
    private final SoundPool sons;
    private final int somAcerto;
    private final int somErro;
    private final int somLicao;
    private final Set<Integer> carregados = new HashSet<>();
    /** Som pedido antes de terminar de carregar: toca assim que carregar. */
    private int pendente;

    public Retorno(Context context) {
        this.context = context.getApplicationContext();
        AudioAttributes atributos = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sons = new SoundPool.Builder().setMaxStreams(2).setAudioAttributes(atributos).build();
        sons.setOnLoadCompleteListener((pool, id, status) -> {
            if (status == 0) {
                carregados.add(id);
                if (id == pendente) {
                    pendente = 0;
                    tocar(id);
                }
            }
        });
        somAcerto = sons.load(this.context, R.raw.som_acerto, 1);
        somErro = sons.load(this.context, R.raw.som_erro, 1);
        somLicao = sons.load(this.context, R.raw.som_licao, 1);
    }

    public void acerto() {
        tocar(somAcerto);
    }

    public void erro() {
        tocar(somErro);
        vibrar();
    }

    public void licaoConcluida() {
        tocar(somLicao);
    }

    public void liberar() {
        sons.release();
    }

    private void tocar(int som) {
        if (!PreferenciasManager.getInstance(context).isSomAtivo()) {
            return;
        }
        if (carregados.contains(som)) {
            sons.play(som, VOLUME, VOLUME, 1, 0, 1f);
        } else {
            pendente = som;
        }
    }

    private void vibrar() {
        if (!PreferenciasManager.getInstance(context).isVibracaoAtiva()) {
            return;
        }
        Vibrator vibrador;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager gerente = context.getSystemService(VibratorManager.class);
            vibrador = gerente == null ? null : gerente.getDefaultVibrator();
        } else {
            vibrador = context.getSystemService(Vibrator.class);
        }
        if (vibrador != null && vibrador.hasVibrator()) {
            vibrador.vibrate(VibrationEffect.createOneShot(VIBRACAO_MS, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }
}
