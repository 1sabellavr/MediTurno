// DECLARACIÓN DEL PAQUETE
package co.edu.ue.mediturno.api;

//es el configurador de la conexión a internet de la app

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * CLASE ApiClient
 * Se encarga de inicializar y proveer una única instancia de Retrofit para toda la aplicación
 */
public class ApiClient {

    // IP predeterminada del emulador de Android Studio para acceder al localhost del computador
    // Si pruebo en celu conectado por USB, cambiar "10.0.2.2" por la IP local del PC
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private static Retrofit retrofit = null;

    /**
     * Método público y estático que devuelve la interfaz ApiService lista para realizar peticiones.
     */
    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Establece la URL base de la API
                    .addConverterFactory(GsonConverterFactory.create()) // Convierte automáticamente JSON <-> Java
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}