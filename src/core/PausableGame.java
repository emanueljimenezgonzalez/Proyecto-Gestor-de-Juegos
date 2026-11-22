package core;

/** Capacidad adicional; mantiene intacto el contrato obligatorio GameFunction. */
public interface PausableGame {
    void pausarPartida();
    boolean tienePartidaEnCurso();
}
