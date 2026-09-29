package co.edu.ue.mediturno.receiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.util.NotificacionHelper;

public class RecordatorioReceiver extends BroadcastReceiver {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_MEDICO = "medico";
    public static final String EXTRA_HORA = "hora";

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificacionHelper.crearCanal(context);

        int id = intent.getIntExtra(EXTRA_ID, 0);
        String medico = intent.getStringExtra(EXTRA_MEDICO);
        String hora = intent.getStringExtra(EXTRA_HORA);

        Notification notificacion = new NotificationCompat.Builder(
                context, NotificacionHelper.CANAL_ID)
                .setSmallIcon(R.drawable.ic_citas)
                .setContentTitle(context.getString(R.string.notif_titulo))
                .setContentText(context.getString(R.string.notif_texto, medico, hora))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat.from(context).notify(id, notificacion);
    }
}