package co.edu.ue.mediturno.models;


/**
 * CLASE MODELO: Cita
 * Representa un turno o cita de entrega de medicamentos registrada por un paciente (CRUD Citas).
 * Facilita el intercambio de datos entre la App Android, SQLite/SharedPreferences y el Backend PostgreSQL.
 */
public class Cita {
    // 2. ATRIBUTOS (VARIABLES DE INSTANCIA)
    // Datos clave que conforman un tiquete de atención en MedTurn.
    private int id;
    private int idUsuario;
    private String medicamento;
    private String fecha;
    private String hora;
    private String puntoAtencion;
    private String estado; // "PENDIENTE", "ATENDIDO", "CANCELADO"

    // Método encargado de inicializar el objeto Cita cuando se crea un nuevo turno
    // o cuando se recibe el listado de citas
    public Cita(int id, int idUsuario, String medicamento, String fecha, String hora, String puntoAtencion, String estado) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.medicamento = medicamento;
        this.fecha = fecha;
        this.hora = hora;
        this.puntoAtencion = puntoAtencion;
        this.estado = estado;
    }

    // Métodos públicos que permiten extraer la información de la cita para pintar los componentes gráficos.

    // Obtiene el ID numérico de la cita
    public int getId() {
        return id;
    }

    // Obtiene el ID del usuario dueño de la cita
    public int getIdUsuario() {
        return idUsuario;
    }

    // Obtiene el nombre del medicamento a reclamar
    public String getMedicamento() {
        return medicamento;
    }

    // Obtiene la fecha agendada
    public String getFecha() {
        return fecha;
    }

    // Obtiene la hora agendada (Útil para programar alarmas/notificaciones locales)
    public String getHora() {
        return hora;
    }

    // Obtiene la sede o punto de atención físico
    public String getPuntoAtencion() {
        return puntoAtencion;
    }

    // Obtiene el estado actual de la cita ("PENDIENTE", "ATENDIDO", etc.)
    public String getEstado() {
        return estado;
    }
}