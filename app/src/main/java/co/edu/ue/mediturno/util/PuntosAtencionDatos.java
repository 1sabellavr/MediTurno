package co.edu.ue.mediturno.util;

import co.edu.ue.mediturno.model.PuntoAtencion;

import java.util.ArrayList;
import java.util.List;

public final class PuntosAtencionDatos {

    private PuntosAtencionDatos() {
    }

    public static List<PuntoAtencion> obtenerTodos() {
        // TODO-API: GET /puntos-atencion
        // Recibe: lista de PuntoAtencion (id, nombre, dirección, latitud y longitud).
        // Reemplazar estos datos de prueba por la respuesta de la API. Las direcciones y
        // coordenadas de abajo son de ejemplo.
        List<PuntoAtencion> puntos = new ArrayList<>();
        puntos.add(new PuntoAtencion(1, "Sede Norte",
                "Calle 127 # 15-20, Bogotá", 4.7105, -74.0410));
        puntos.add(new PuntoAtencion(2, "Sede Centro",
                "Carrera 7 # 32-16, Bogotá", 4.6180, -74.0680));
        puntos.add(new PuntoAtencion(3, "Sede Sur",
                "Avenida Boyacá # 38-25 Sur, Bogotá", 4.5980, -74.1450));
        return puntos;
    }

    public static PuntoAtencion buscarPorId(int id) {
        for (PuntoAtencion punto : obtenerTodos()) {
            if (punto.getId() == id) {
                return punto;
            }
        }
        return null;
    }
}