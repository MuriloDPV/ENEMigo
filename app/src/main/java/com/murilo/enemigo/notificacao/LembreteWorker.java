package com.murilo.enemigo.notificacao;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.util.Lembrete;

import java.time.LocalDateTime;

/**
 * Roda no horário do lembrete: notifica só se a regra deixar (Lembrete.deveNotificar) e
 * já marca o próximo.
 */
public class LembreteWorker extends Worker {

    public LembreteWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PreferenciasManager preferencias = PreferenciasManager.getInstance(context);
        ProgressoManager progresso = ProgressoManager.getInstance(context);
        LocalDateTime agora = LocalDateTime.now();
        if (Lembrete.deveNotificar(preferencias.isLembreteAtivo(), progresso.isOnboardingConcluido(),
                progresso.getUltimaDataEstudo(), agora, preferencias.getHorarioLembrete())) {
            // RN28: se ele não estudou ontem, a ofensiva já zerou e o texto não fala dela.
            NotificacaoLembrete.mostrar(context, progresso.getOfensivaAtual(), agora.toLocalDate());
        }
        // Amanhã no mesmo horário (ou hoje mais tarde, se rodou adiantado).
        AgendaLembrete.reagendar(context);
        return Result.success();
    }
}
