package co.edu.ue.mediturno.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import co.edu.ue.mediturno.manager.ManagerDatabase;
import co.edu.ue.mediturno.manager.UserContract;
import co.edu.ue.mediturno.model.Cita;

public class CitaRepository {
    private final ManagerDatabase dbHelper;

    public CitaRepository(Context context) {
        this.dbHelper = new ManagerDatabase(context);
    }

    // ==========================================
    // 1. CREATE (Insertar nueva cita)
    // ==========================================
    public long insertarCita(Cita cita) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Mapeo de valores que se enviarán a la base de datos
        ContentValues values = new ContentValues();
        values.put(UserContract.COL_CITA_PACIENTE, cita.getPaciente());
        values.put(UserContract.COL_CITA_MEDICO, cita.getMedico());
        values.put(UserContract.COL_CITA_FECHA, cita.getFecha());
        values.put(UserContract.COL_CITA_HORA, cita.getHora());
        values.put(UserContract.COL_CITA_MOTIVO, cita.getMotivo());
        values.put(UserContract.COL_CITA_ESTADO, cita.getEstado());

        // Inserta la fila y retorna el ID autogenerado (-1 si ocurre un error)
        long idGenerado = db.insert(UserContract.TABLA_CITAS, null, values);
        db.close(); // Cerrar conexión
        return idGenerado;
    }

    // ==========================================
    // 2. READ (Consultar todas las citas)
    // ==========================================
    public List<Cita> obtenerTodasLasCitas() {
        List<Cita> listaCitas = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SELECT * FROM citas ORDER BY id DESC
        Cursor cursor = db.query(
                UserContract.TABLA_CITAS,
                null,
                null,
                null,
                null,
                null,
                UserContract.COL_CITA_ID + " DESC"
        );

        // Recorrer los resultados obtenidos en el Cursor
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_ID));
                String paciente = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_PACIENTE));
                String medico = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_MEDICO));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_FECHA));
                String hora = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_HORA));
                String motivo = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_MOTIVO));
                String estado = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.COL_CITA_ESTADO));

                listaCitas.add(new Cita(id, paciente, medico, fecha, hora, motivo, estado, null));
            } while (cursor.moveToNext());
        }

        cursor.close(); // Liberar memoria del Cursor
        db.close();     // Cerrar conexión
        return listaCitas;
    }

    // ==========================================
    // 3. UPDATE (Actualizar cita existente)
    // ==========================================
    public int actualizarCita(Cita cita) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContract.COL_CITA_PACIENTE, cita.getPaciente());
        values.put(UserContract.COL_CITA_MEDICO, cita.getMedico());
        values.put(UserContract.COL_CITA_FECHA, cita.getFecha());
        values.put(UserContract.COL_CITA_HORA, cita.getHora());
        values.put(UserContract.COL_CITA_MOTIVO, cita.getMotivo());
        values.put(UserContract.COL_CITA_ESTADO, cita.getEstado());

        // UPDATE citas SET ... WHERE id = ?
        int filasAfectadas = db.update(
                UserContract.TABLA_CITAS,
                values,
                UserContract.COL_CITA_ID + " = ?",
                new String[]{String.valueOf(cita.getId())}
        );

        db.close();
        return filasAfectadas;
    }

    // ==========================================
    // 4. DELETE (Eliminar cita por ID)
    // ==========================================
    public int eliminarCita(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // DELETE FROM citas WHERE id = ?
        int filasAfectadas = db.delete(
                UserContract.TABLA_CITAS,
                UserContract.COL_CITA_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();
        return filasAfectadas;
    }
}

