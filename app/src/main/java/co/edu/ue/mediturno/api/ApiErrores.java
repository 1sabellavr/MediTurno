package co.edu.ue.mediturno.api;

import android.content.Context;

import co.edu.ue.mediturno.R;

import retrofit2.Response;

// Convierte una respuesta de error de la API en un mensaje para mostrar al usuario.
public final class ApiErrores {

    private ApiErrores() {
    }

    public static String mensaje(Context context, Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String texto = response.errorBody().string().trim();
                // La API responde con texto plano en los errores controlados (400, 409).
                if (!texto.isEmpty() && texto.length() < 200 && !texto.startsWith("{")) {
                    return texto;
                }
            }
        } catch (Exception e) {
            // Si no se puede leer el cuerpo, se usa el mensaje general.
        }
        return context.getString(R.string.error_servidor, response.code());
    }
}