package co.edu.ue.mediturno.api;

import co.edu.ue.mediturno.model.Cita;
import co.edu.ue.mediturno.model.Medicamento;
import co.edu.ue.mediturno.model.PuntoAtencion;
import co.edu.ue.mediturno.model.Usuario;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Rutas de la API de MediTurno (Spring Boot + PostgreSQL)
public interface ApiService {

    // ---------- Usuarios ----------
    @GET("api/usuarios")
    Call<List<Usuario>> obtenerUsuarios();

    // Busca el perfil (y el rol) de un usuario por su correo. Se usa al iniciar sesión.
    @GET("api/usuarios/por-correo")
    Call<Usuario> obtenerUsuarioPorCorreo(@Query("correo") String correo);

    @POST("api/usuarios")
    Call<Usuario> crearUsuario(@Body Usuario usuario);

    @PUT("api/usuarios/{id}")
    Call<Usuario> actualizarUsuario(@Path("id") int id, @Body Usuario usuario);

    @DELETE("api/usuarios/{id}")
    Call<Void> eliminarUsuario(@Path("id") int id);

    // ---------- Inventario (medicamentos) ----------
    @GET("api/inventario")
    Call<List<Medicamento>> obtenerInventario();

    @POST("api/inventario")
    Call<Medicamento> crearMedicamento(@Body Medicamento medicamento);

    @PUT("api/inventario/{id}")
    Call<Medicamento> actualizarMedicamento(@Path("id") int id, @Body Medicamento medicamento);

    @DELETE("api/inventario/{id}")
    Call<Void> eliminarMedicamento(@Path("id") int id);

    // ---------- Citas ----------
    @GET("api/citas")
    Call<List<Cita>> obtenerCitas();

    @POST("api/citas")
    Call<Cita> crearCita(@Body Cita cita);

    @PUT("api/citas/{id}")
    Call<Cita> actualizarCita(@Path("id") int id, @Body Cita cita);

    @PUT("api/citas/{id}/cancelar")
    Call<Cita> cancelarCita(@Path("id") int id);

    @DELETE("api/citas/{id}")
    Call<Void> eliminarCita(@Path("id") int id);

    // ---------- Puntos de atención ----------
    @GET("api/puntos-atencion")
    Call<List<PuntoAtencion>> obtenerPuntosAtencion();
}