package co.edu.ue.mediturno.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ManagerDatabase extends SQLiteOpenHelper {

    // Nombre del archivo físico de la base de datos local
    private static final String DATA_BASE = "mediturnodb";
    // Versión de la base de datos (incrementar si cambia la estructura de las tablas)
    // Versión 2: se agrega la tabla de tratamientos.
    private static final int VERSION = 2;
    public ManagerDatabase(@Nullable Context context) {
        super(context, DATA_BASE, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase database) {
        // Crear tabla de Citas
        database.execSQL(UserContract.CREATE_TABLE_CITAS);
        // Crear tabla de Medicamentos
        database.execSQL(UserContract.CREATE_TABLE_MEDICAMENTOS);
        // Crear tabla de Tratamientos
        database.execSQL(UserContract.CREATE_TABLE_TRATAMIENTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Quien ya tenía la base en versión 1 conserva sus datos y solo recibe la tabla nueva.
        if (oldVersion < 2) {
            db.execSQL(UserContract.CREATE_TABLE_TRATAMIENTOS);
        }
    }
}