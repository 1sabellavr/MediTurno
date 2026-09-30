package co.edu.ue.mediturno.manager;


public class UserContract {

    // Constructor privado para evitar que la clase sea instanciada
    private UserContract() {}

    // TABLA 1: CITAS
    public static final String TABLA_CITAS = "citas";
    public static final String COL_CITA_ID = "id";
    public static final String COL_CITA_PACIENTE = "paciente";
    public static final String COL_CITA_MEDICO = "medico";
    public static final String COL_CITA_FECHA = "fecha";
    public static final String COL_CITA_HORA = "hora";
    public static final String COL_CITA_MOTIVO = "motivo";
    public static final String COL_CITA_ESTADO = "estado";

    public static final String CREATE_TABLE_CITAS =
            "CREATE TABLE " + TABLA_CITAS + " (" +
                    COL_CITA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CITA_PACIENTE + " TEXT NOT NULL, " +
                    COL_CITA_MEDICO + " TEXT NOT NULL, " +
                    COL_CITA_FECHA + " TEXT NOT NULL, " +
                    COL_CITA_HORA + " TEXT NOT NULL, " +
                    COL_CITA_MOTIVO + " TEXT, " +
                    COL_CITA_ESTADO + " TEXT NOT NULL);";

    // TABLA 2: MEDICAMENTOS
    public static final String TABLA_MEDICAMENTOS = "medicamentos";
    public static final String COL_MED_ID = "id";
    public static final String COL_MED_NOMBRE = "nombre";
    public static final String COL_MED_DESCRIPCION = "descripcion";
    public static final String COL_MED_CANTIDAD = "cantidad";
    public static final String COL_MED_FECHA_VENCIMIENTO = "fecha_vencimiento";

    public static final String CREATE_TABLE_MEDICAMENTOS =
            "CREATE TABLE " + TABLA_MEDICAMENTOS + " (" +
                    COL_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MED_NOMBRE + " TEXT NOT NULL, " +
                    COL_MED_DESCRIPCION + " TEXT, " +
                    COL_MED_CANTIDAD + " INTEGER NOT NULL, " +
                    COL_MED_FECHA_VENCIMIENTO + " TEXT NOT NULL);";

    // TABLA 3: TRATAMIENTOS (medicamentos que cada usuario toma; se guardan solo en el teléfono)
    public static final String TABLA_TRATAMIENTOS = "tratamientos";
    public static final String COL_TRAT_ID = "id";
    public static final String COL_TRAT_USUARIO = "usuario";
    public static final String COL_TRAT_MEDICAMENTO = "medicamento";
    public static final String COL_TRAT_DOSIS = "dosis";
    public static final String COL_TRAT_HORA = "hora";
    public static final String COL_TRAT_NOTAS = "notas";

    public static final String CREATE_TABLE_TRATAMIENTOS =
            "CREATE TABLE " + TABLA_TRATAMIENTOS + " (" +
                    COL_TRAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TRAT_USUARIO + " TEXT NOT NULL, " +
                    COL_TRAT_MEDICAMENTO + " TEXT NOT NULL, " +
                    COL_TRAT_DOSIS + " TEXT NOT NULL, " +
                    COL_TRAT_HORA + " TEXT NOT NULL, " +
                    COL_TRAT_NOTAS + " TEXT);";
}