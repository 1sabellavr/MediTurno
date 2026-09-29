package co.edu.ue.mediturno.model;

public class Medicamento {

    private int id;
    private String nombre;
    private String descripcion;
    private int cantidad;
    private String fechaVencimiento;
    private String laboratorio;
    private int stock;
    private String epsRequerida;

    public Medicamento() {
    }

    public Medicamento(int id, String nombre, String descripcion, int cantidad,
                       String fechaVencimiento) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.fechaVencimiento = fechaVencimiento;
        this.stock = cantidad;
    }

    public Medicamento(int id, String nombre, String laboratorio, String epsRequerida, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.laboratorio = laboratorio;
        this.epsRequerida = epsRequerida;
        this.stock = stock;
        this.cantidad = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
        this.cantidad = stock;
    }

    public String getEpsRequerida() {
        return epsRequerida;
    }

    public void setEpsRequerida(String epsRequerida) {
        this.epsRequerida = epsRequerida;
    }
}
