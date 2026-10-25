package com.murilo.enemigo.notificacao;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.util.Lembrete;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Agenda o lembrete diário no WorkManager: uma tarefa única que, ao rodar, já marca a do dia
 * seguinte. O WorkManager guarda a agenda e a refaz sozinho quando o celular reinicia, sem
 * alarme exato nem internet (alguns minutos de atraso não atrapalham um lembrete).
 */
public final class AgendaLembrete {

    private static final String TRABALHO = "lembrete_ofensiva";

    private AgendaLembrete() {
    }

    /** Ao abrir o app: garante o lembrete agendado (ou nenhum, se desligado) sem mexer no já marcado. */
    public static void sincronizar(Context context) {
        agendar(context, ExistingWorkPolicy.KEEP);
    }

    /** Depois de mudar o horário, de resetar ou de o lembrete rodar: marca de novo a partir de agora. */
    public static void reagendar(Context context) {
        agendar(context, ExistingWorkPolicy.REPLACE);
    }

    private static void agendar(Context context, ExistingWorkPolicy politica) {
        WorkManager workManager = WorkManager.getInstance(context);
        PreferenciasManager preferencias = PreferenciasManager.getInstance(context);
        if (!preferencias.isLembreteAtivo()) {
            workManager.cancelUniqueWork(TRABALHO);
            return;
        }
        long atraso = Lembrete.msAteProximo(ZonedDateTime.now(), preferencias.getHorarioLembrete());
        OneTimeWorkRequest pedido = new OneTimeWorkRequest.Builder(LembreteWorker.class)
                .setInitialDelay(atraso, TimeUnit.MILLISECONDS)
                .build();
        workManager.enqueueUniqueWork(TRABALHO, politica, pedido);
    }
}
