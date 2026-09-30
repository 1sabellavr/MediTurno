package co.edu.ue.mediturno.model;

public class Tratamiento {

    private int id;
    private String medicamento;
    private String dosis;
    private String hora;
    private String notas;

    public Tratamiento(int id, String medicamento, String dosis, String hora, String notas) {
        this.id = id;
        this.medicamento = medicamento;
        this.dosis = dosis;
        this.hora = hora;
        this.notas = notas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(String medicamento) {
        this.medicamento = medicamento;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}