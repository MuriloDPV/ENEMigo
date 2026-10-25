package com.murilo.enemigo.notificacao;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.murilo.enemigo.R;
import com.murilo.enemigo.ui.MainActivity;
import com.murilo.enemigo.util.Lembrete;

import java.time.LocalDate;
import java.util.Locale;

/**
 * A notificação do lembrete: o canal, o texto com a Lia e o toque que abre o app.
 */
public final class NotificacaoLembrete {

    private static final String CANAL = "lembrete_diario";
    private static final int ID = 1;

    private NotificacaoLembrete() {
    }

    /**
     * O Android deixa mostrar? No 13+ depende da permissão; antes dele, o aluno pode ter
     * desligado as notificações do app nas configurações do sistema.
     */
    public static boolean permitida(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * Mostra o lembrete. Sem a permissão (o aluno negou), não faz nada.
     *
     * @param ofensiva ofensiva em risco (0 = não fala dela)
     */
    public static void mostrar(Context context, int ofensiva, LocalDate hoje) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        criarCanal(context);

        String titulo = ofensiva > 0
                ? context.getResources().getQuantityString(R.plurals.lembrete_titulo_ofensiva, ofensiva, ofensiva)
                : context.getString(R.string.lembrete_titulo);
        String[] frases = context.getResources().getStringArray(R.array.lembrete_frases);
        String frase = String.format(Locale.getDefault(), frases[Lembrete.indiceFrase(hoje, frases.length)],
                context.getString(R.string.mascote_nome));

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(context, CANAL)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setColor(ContextCompat.getColor(context, R.color.marca_primaria))
                .setLargeIcon(desenharLia(context))
                .setContentTitle(titulo)
                .setContentText(frase)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(abrirApp(context))
                .setAutoCancel(true);
        NotificationManagerCompat.from(context).notify(ID, notificacao.build());
    }

    /** Canal próprio: o aluno pode silenciar só o lembrete nas configurações do Android. */
    public static void criarCanal(Context context) {
        NotificationChannelCompat canal = new NotificationChannelCompat.Builder(CANAL,
                NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.lembrete_canal_nome))
                .setDescription(context.getString(R.string.lembrete_canal_descricao))
                .build();
        NotificationManagerCompat.from(context).createNotificationChannel(canal);
    }

    /** O toque abre o app como o ícone da tela inicial: se já estava aberto, volta para onde estava. */
    private static PendingIntent abrirApp(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /** A Lia de orelha em pé no ícone grande da notificação. */
    private static Bitmap desenharLia(Context context) {
        int lado = context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        Bitmap bitmap = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888);
        Drawable lia = AppCompatResources.getDrawable(context, R.drawable.mascote_neutro);
        if (lia != null) {
            lia.setBounds(0, 0, lado, lado);
            lia.draw(new Canvas(bitmap));
        }
        return bitmap;
    }
}
