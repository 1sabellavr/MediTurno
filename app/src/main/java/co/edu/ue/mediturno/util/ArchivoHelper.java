package co.edu.ue.mediturno.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * ArchivoHelper: Permite guardar y leer archivos planos (.txt) en la memoria interna privada de la app.
 */
public class ArchivoHelper {

    private static final String TAG = "ArchivoHelper";

    /**
     * Escribe un texto en un archivo plano en la memoria del dispositivo.
     */
    public static boolean guardarComprobante(Context context, String nombreArchivo, String contenido) {
        try (FileOutputStream fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE)) {
            fos.write(contenido.getBytes());
            Log.d(TAG, "Archivo guardado con éxito: " + nombreArchivo);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error al escribir archivo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lee el contenido de un archivo almacenado previamente.
     */
    public static String leerComprobante(Context context, String nombreArchivo) {
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(nombreArchivo);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader reader = new BufferedReader(isr)) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea).append("\n");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al leer archivo: " + e.getMessage());
            return "";
        }
        return sb.toString().trim();
    }
}