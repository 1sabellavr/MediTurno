package co.edu.ue.mediturno.models;

public class puntoAtencion {
    private int id;
    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;

    public puntoAtencion(int id, String nombre, String direccion, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
}