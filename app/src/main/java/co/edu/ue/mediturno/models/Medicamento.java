// 1. DECLARACIÓN DEL PAQUETE
// Le indica a Java en qué subcarpeta (paquete) se encuentra este archivo dentro del proyecto
package co.edu.ue.mediturno.models;

/**
 * CLASE MODELO: Medicamento
 * Representa la estructura de un medicamento en el sistema (CRUD Inventario)
 * clase sirve como plantilla para convertir la respuesta JSON del servidor (API REST / PostgreSQL)
 * en un objeto manipulable dentro de Android Studio
 */
public class Medicamento {

    // 2. ATRIBUTOS (VARIABLES DE INSTANCIA / CAMPOS DE LA BASE DE DATOS)
    // Se declaran como private aplicando el principio de Encapsulamiento (buenas prácticas de POO),
    // lo que evita que otras clases modifiquen estos valores directamente sin autorización.

    private int id;// Identificador único del medicamento en la base de datos PostgreSQL
    private String nombre;// Nombre comercial o genérico del medicamento (Ej: "Ibuprofeno 500mg")
    private String laboratorio;// Empresa o laboratorio fabricante
    private int stock;// Cantidad de unidades disponibles en el punto de dispensación
    private String epsRequerida;// EPS ligada al medicamento para validar la cobertura del paciente

    // 3. CONSTRUCTOR
    // Es el método especial que se ejecuta PRIMERO al instanciar/crear un nuevo objeto Medicamento
    // Asigna los valores que vienen desde la API REST a las variables internas de esta clase
    public Medicamento(int id, String nombre, String laboratorio, int stock, String epsRequerida) {
        // La palabra reservada 'this' diferencia la variable de la clase (this.id)
        // del parámetro que recibe el constructor (id).
        this.id = id;
        this.nombre = nombre;
        this.laboratorio = laboratorio;
        this.stock = stock;
        this.epsRequerida = epsRequerida;
    }

    // 4. METODOS GETTER
    // Permiten consultar o leer el valor de las variables privadas desde otras clases
    // (por ejemplo, para mostrarlas en la pantalla de la App o validar el stock)

    // Devuelve ID del medicamento
    public int getId() {
        return id;
    }

    // Devuelve el Nombre del medicamento para mostrarlo en las listas (RecyclerView)
    public String getNombre() {
        return nombre;
    }

    // Devuelve el Laboratorio del medicamento
    public String getLaboratorio() {
        return laboratorio;
    }

    // Devuelve el Stock actual (Sirve para validar si hay disponibilidad antes de agendar una cita)
    public int getStock() {
        return stock;
    }

    // Devuelve la EPS asociada para verificar la cobertura del paciente
    public String getEpsRequerida() {
        return epsRequerida;
    }
}