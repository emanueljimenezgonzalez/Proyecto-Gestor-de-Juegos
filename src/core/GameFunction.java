package core;

/** Contrato que deben implementar todos los juegos del proyecto. */
public interface GameFunction {
    void iniciar();
    Stat getStats();
}
