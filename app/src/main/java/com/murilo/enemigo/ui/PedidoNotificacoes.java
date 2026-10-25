package com.murilo.enemigo.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.murilo.enemigo.R;
import com.murilo.enemigo.data.PreferenciasManager;
import com.murilo.enemigo.data.ProgressoManager;
import com.murilo.enemigo.ui.componente.MascoteView;
import com.murilo.enemigo.util.Lembrete;

/**
 * Pede a permissão de notificações do Android 13+ na hora certa: depois da primeira lição
 * concluída, com a Lia explicando antes para que serve. Pede uma vez só: se o aluno responder
 * "Agora não" ou negar no Android, fica assim (dá para ligar depois nas Configurações).
 * Crie no onCreate (o pedido precisa ser registrado antes de a tela aparecer) e chame
 * talvezPedir() no onResume.
 */
public final class PedidoNotificacoes {

    private final AppCompatActivity activity;
    private final ActivityResultLauncher<String> pedido;
    private AlertDialog explicacao;

    public PedidoNotificacoes(AppCompatActivity activity) {
        this.activity = activity;
        // A resposta não muda nada aqui: o lembrete segue agendado e só aparece se o Android deixar.
        pedido = activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                concedida -> {
                });
    }

    public void talvezPedir() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || (explicacao != null && explicacao.isShowing())) {
            return;
        }
        PreferenciasManager preferencias = PreferenciasManager.getInstance(activity);
        if (preferencias.isNotificacaoPedida() || !preferencias.isLembreteAtivo()
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
                || ProgressoManager.getInstance(activity).getTotalLicoesConcluidas() == 0) {
            return;
        }
        String horario = Lembrete.paraHora(preferencias.getHorarioLembrete()).toString();
        // Só conta como pedido quando ele responde: se a tela for recriada, a explicação volta.
        explicacao = Dialogos.comLia(activity, MascoteView.Expressao.FELIZ,
                        activity.getString(R.string.notificacao_pedido_titulo),
                        activity.getString(R.string.notificacao_pedido_mensagem,
                                activity.getString(R.string.mascote_nome), horario))
                .setPositiveButton(R.string.notificacao_pedido_sim, (dialog, which) -> {
                    preferencias.marcarNotificacaoPedida();
                    pedido.launch(Manifest.permission.POST_NOTIFICATIONS);
                })
                .setNegativeButton(R.string.notificacao_pedido_nao,
                        (dialog, which) -> preferencias.marcarNotificacaoPedida())
                .setOnCancelListener(dialog -> preferencias.marcarNotificacaoPedida())
                .show();
    }
}
