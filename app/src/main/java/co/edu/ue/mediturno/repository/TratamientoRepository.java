package co.edu.ue.mediturno.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import co.edu.ue.mediturno.manager.ManagerDatabase;
import co.edu.ue.mediturno.manager.UserContract;
import co.edu.ue.mediturno.model.Tratamiento;

// CRUD local del tratamiento de cada usuario (base de datos SQLite del teléfono).
public class TratamientoRepository {

    private final ManagerDatabase dbHelper;

    public TratamientoRepository(Context context) {
        this.dbHelper = new ManagerDatabase(context.getApplicationContext());
    }

    // ==========================================
    // 1. CREATE (agregar un medicamento al tratamiento)
    // ==========================================
    public long insertar(Tratamiento tratamiento, String usuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.COL_TRAT_USUARIO, usuario);
        values.put(UserContract.COL_TRAT_MEDICAMENTO, tratamiento.getMedicamento());
        values.put(UserContract.COL_TRAT_DOSIS, tratamiento.getDosis());
        values.put(UserContract.COL_TRAT_HORA, tratamiento.getHora());
        values.put(UserContract.COL_TRAT_NOTAS, tratamiento.getNotas());

        // Devuelve el id generado (-1 si ocurre un error)
        return db.insert(UserContract.TABLA_TRATAMIENTOS, null, values);
    }

    // ==========================================
    // 2. READ (consultar el tratamiento de un usuario, ordenado por hora)
    // ==========================================
    public List<Tratamiento> obtenerPorUsuario(String usuario) {
        List<Tratamiento> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                UserContract.TABLA_TRATAMIENTOS,
                null,
                UserContract.COL_TRAT_USUARIO + " = ?",
                new String[]{usuario},
                null,
                null,
                UserContract.COL_TRAT_HORA + " ASC")) {

            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow(UserContract.COL_TRAT_ID));
                String medicamento = cursor.getString(
                        cursor.getColumnIndexOrThrow(UserContract.COL_TRAT_MEDICAMENTO));
                String dosis = cursor.getString(
                        cursor.getColumnIndexOrThrow(UserContract.COL_TRAT_DOSIS));
                String hora = cursor.getString(
                        cursor.getColumnIndexOrThrow(UserContract.COL_TRAT_HORA));
                String notas = cursor.getString(
                        cursor.getColumnIndexOrThrow(UserContract.COL_TRAT_NOTAS));

                lista.add(new Tratamiento(id, medicamento, dosis, hora, notas));
            }
        }
        return lista;
    }

    // ==========================================
    // 3. UPDATE (editar un medicamento del tratamiento)
    // ==========================================
    public int actualizar(Tratamiento tratamiento) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.COL_TRAT_MEDICAMENTO, tratamiento.getMedicamento());
        values.put(UserContract.COL_TRAT_DOSIS, tratamiento.getDosis());
        values.put(UserContract.COL_TRAT_HORA, tratamiento.getHora());
        values.put(UserContract.COL_TRAT_NOTAS, tratamiento.getNotas());

        // Devuelve cuántas filas se actualizaron
        return db.update(UserContract.TABLA_TRATAMIENTOS, values,
                UserContract.COL_TRAT_ID + " = ?",
                new String[]{String.valueOf(tratamiento.getId())});
    }

    // ==========================================
    // 4. DELETE (quitar un medicamento del tratamiento)
    // ==========================================
    public int eliminar(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(UserContract.TABLA_TRATAMIENTOS,
                UserContract.COL_TRAT_ID + " = ?",
                new String[]{String.valueOf(id)});
    }
}