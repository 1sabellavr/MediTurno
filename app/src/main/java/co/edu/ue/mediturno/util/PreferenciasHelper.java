package co.edu.ue.mediturno.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenciasHelper {

    private static final String PREF_NAME = "mediturno_prefs";
    private static final String KEY_NOTIFICACIONES = "notificaciones_activas";
    private static final String KEY_ULTIMO_ROL = "ultimo_rol";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void guardarNotificacionesActivas(Context context, boolean activas) {
        getPrefs(context).edit().putBoolean(KEY_NOTIFICACIONES, activas).apply();
    }

    public static boolean obtenerNotificacionesActivas(Context context) {
        return getPrefs(context).getBoolean(KEY_NOTIFICACIONES, true);
    }

    public static void guardarUltimoRol(Context context, String rol) {
        getPrefs(context).edit().putString(KEY_ULTIMO_ROL, rol).apply();
    }

    public static String obtenerUltimoRol(Context context) {
        return getPrefs(context).getString(KEY_ULTIMO_ROL, "PACIENTE");
    }
}