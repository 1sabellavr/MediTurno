package co.edu.ue.mediturno.util;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * DataStoreHelper: Gestiona el almacenamiento asíncrono y seguro de datos de sesión.
 * A diferencia de SharedPreferences, DataStore opera de forma asíncrona mediante hilos secundarios
 * y evita bloqueos en la interfaz gráfica (UI Thread).
 */
public class DataStoreHelper {

    private static RxDataStore<Preferences> dataStore;

    // Definición de Claves para DataStore
    private static final Preferences.Key<String> KEY_TOKEN_SESION = PreferencesKeys.stringKey("token_sesion");
    private static final Preferences.Key<String> KEY_EMAIL_USUARIO = PreferencesKeys.stringKey("email_usuario");

    // Instancia Singleton de DataStore
    public static synchronized RxDataStore<Preferences> getInstance(Context context) {
        if (dataStore == null) {
            dataStore = new RxPreferenceDataStoreBuilder(
                    context.getApplicationContext(),
                    "mediturno_datastore"
            ).build();
        }
        return dataStore;
    }

    /**
     * Guardar datos de sesión (Token de Firebase y Email) de forma asíncrona.
     */
    public static void guardarSesion(Context context, String token, String email) {
        getInstance(context).updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.set(KEY_TOKEN_SESION, token);
            mutablePreferences.set(KEY_EMAIL_USUARIO, email);
            return Single.just(mutablePreferences);
        });
    }

    /**
     * Obtener el token de sesión almacenado.
     */
    public static Flowable<String> obtenerTokenSesion(Context context) {
        return getInstance(context).data().map(prefs -> {
            String token = prefs.get(KEY_TOKEN_SESION);
            return token != null ? token : "";
        });
    }

    /**
     * Borrar la sesión almacenada (al cerrar sesión).
     */
    public static void cerrarSesion(Context context) {
        getInstance(context).updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.clear();
            return Single.just(mutablePreferences);
        });
    }
}