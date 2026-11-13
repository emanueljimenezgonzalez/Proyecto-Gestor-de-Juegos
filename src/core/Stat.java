package core;

/** Representa el resultado obtenido por una persona en una partida. */
public class Stat {
    private String clave;
    private String nombre;
    private int valor;

    public Stat(String clave, String nombre, int valor) {
        this.clave = clave;
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public int getValor() { return valor; }

    public void setClave(String clave) { this.clave = clave; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setValor(int valor) { this.valor = valor; }

    @Override
    public String toString() {
        return "Stat{" + "clave='" + clave + '\'' + ", nombre='" + nombre + '\'' + ", valor=" + valor + '}';
    }
}
