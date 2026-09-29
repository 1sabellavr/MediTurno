package co.edu.ue.mediturno;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class MediTurnoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // La app usa fondos claros, por eso se fuerza el tema claro aunque el teléfono
        // esté en modo oscuro.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}