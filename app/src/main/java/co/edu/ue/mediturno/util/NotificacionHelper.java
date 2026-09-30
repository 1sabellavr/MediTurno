package co.edu.ue.mediturno.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import co.edu.ue.mediturno.R;
import co.edu.ue.mediturno.model.Cita;
import co.edu.ue.mediturno.receiver.RecordatorioReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class NotificacionHelper {

    public static final String CANAL_ID = "recordatorios_citas";

    // Minutos antes de la cita en que llega el aviso.
    // Para probar rápido, cámbialo a 1 y agenda una cita para dentro de 3 minutos.
    public static final int MINUTOS_ANTES = 60;

    private NotificacionHelper() {
    }

    public static void crearCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    context.getString(R.string.canal_nombre),
                    NotificationManager.IMPORTANCE_HIGH);
            canal.setDescription(context.getString(R.string.canal_descripcion));

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    public static boolean programarRecordatorio(Context context, Cita cita) {
        // SharedPreferences: si el usuario desactivó los recordatorios, no se programa nada.
        if (!PreferenciasHelper.obtenerNotificacionesActivas(context)) {
            return false;
        }

        Long momentoCita = momentoDeLaCita(cita);
        if (momentoCita == null) {
            return false;
        }

        long momentoAviso = momentoCita - MINUTOS_ANTES * 60L * 1000L;
        if (momentoAviso <= System.currentTimeMillis()) {
            return false;
        }

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return false;
        }

        Intent intent = new Intent(context, RecordatorioReceiver.class);
        intent.putExtra(RecordatorioReceiver.EXTRA_ID, cita.getId());
        intent.putExtra(RecordatorioReceiver.EXTRA_MEDICO, cita.getMedico());
        intent.putExtra(RecordatorioReceiver.EXTRA_HORA, cita.getHora());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, cita.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momentoAviso, pendingIntent);
        return true;
    }

    public static void cancelarRecordatorio(Context context, int idCita) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, RecordatorioReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, idCita, intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static Long momentoDeLaCita(Cita cita) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            Date fechaHora = formato.parse(cita.getFecha() + " " + cita.getHora());
            return fechaHora != null ? fechaHora.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }
}