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

    // Dirección de la API. Cambiar según dónde se pruebe:
    // - Emulador de Android Studio: "http://10.0.2.2:8080/"
    // - Teléfono real (misma red Wi-Fi): "http://IP_DEL_PC:8080/"  (la IP sale de ipconfig)
    private static final String BASE_URL = "http://10.57.30.113:8080/";

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