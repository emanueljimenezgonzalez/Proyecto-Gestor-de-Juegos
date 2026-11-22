package core;

/** Identidad compartida durante una ejecución del gestor; no guarda datos en disco. */
public final class PlayerSession {
    private static String nombre;
    private PlayerSession() { }
    public static void iniciar(String jugador) { nombre = PlayerNames.validar(jugador); }
    public static boolean activa() { return nombre != null; }
    public static String getNombre() {
        if (nombre == null) throw new IllegalStateException("No hay jugador en la sesión.");
        return nombre;
    }
}
