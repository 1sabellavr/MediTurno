package co.edu.ue.mediturno.model;

public class Cita {

    public static final String ESTADO_PROGRAMADA = "PROGRAMADA";
    public static final String ESTADO_CANCELADA = "CANCELADA";

    private int id;
    private String paciente;
    private String medico;
    private String fecha;
    private String hora;
    private String motivo;
    private String estado;
    private PuntoAtencion puntoAtencion;
    private Integer pacienteId;
    private Integer medicoId;

    public Cita(int id, String paciente, String medico, String fecha, String hora,
                String motivo, String estado, PuntoAtencion puntoAtencion) {
        this.id = id;
        this.paciente = paciente;
        this.medico = medico;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.estado = estado;
        this.puntoAtencion = puntoAtencion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public PuntoAtencion getPuntoAtencion() {
        return puntoAtencion;
    }

    public void setPuntoAtencion(PuntoAtencion puntoAtencion) {
        this.puntoAtencion = puntoAtencion;
    }

    public Integer getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Integer pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Integer getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(Integer medicoId) {
        this.medicoId = medicoId;
    }
}