package co.edu.ue.mediturno.model;

/**
 * CLASE MODELO: Cita
 * Representa un turno o cita de entrega de medicamentos registrada por un paciente.
 */
public class Cita {
    private int id;
    private int idUsuario;
    private String medicamento;
    private String fecha;
    private String hora;
    private String puntoAtencion;
    private String estado; // "PENDIENTE", "ATENDIDO", "CANCELADO"

    public Cita(int id, int idUsuario, String medicamento, String fecha, String hora, String puntoAtencion, String estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.medicamento = medicamento;
        this.fecha = fecha;
        this.hora = hora;
        this.puntoAtencion = puntoAtencion;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getPuntoAtencion() {
        return puntoAtencion;
    }

    public void setPuntoAtencion(String puntoAtencion) {
        this.puntoAtencion = puntoAtencion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
