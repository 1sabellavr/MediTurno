package co.edu.ue.mediturno.api;//define cuáles caminos o URLs de la API vas a consumir

// importamos los modelos que creamos en la carpeta models
import co.edu.ue.mediturno.model.Cita;
import co.edu.ue.mediturno.model.Medicamento;
import co.edu.ue.mediturno.model.Usuario;

// librerias de retrofit y listas para peticiones
import java.util.List;

import co.edu.ue.mediturno.model.PuntoAtencion;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

// interfaz donde se definen las rutas de la api
public interface ApiService {

    // login de usuario
    @POST("api/auth/login")
    Call<Usuario> loginUser(@Body Usuario loginData);

    // para traer el inventario de medicamentos
    @GET("api/inventario")
    Call<List<Medicamento>> obtenerInventario();

    // traer todas las citas o turnos
    @GET("api/citas")
    Call<List<Cita>> obtenerCitas();

    // guardar una cita nueva
    @POST("api/citas")
    Call<Cita> crearCita(@Body Cita nuevaCita);

    // trae punto de atencion
    @GET("api/puntos-atencion")
    Call<List<PuntoAtencion>> obtenerPuntosAtencion();
}