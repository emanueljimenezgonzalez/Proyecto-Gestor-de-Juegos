package externo;

import core.GameFunction;
import core.Stat;
import reflejos.Reflejos;

/**
 * Adaptador del juego externo "Reflejos" (creado por otro grupo) al contrato
 * del Gestor de Juegos.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Inicializar {@link Reflejos} de forma segura: si la construcción de
 *       la instancia lanza cualquier excepción, la captura, la registra en
 *       {@code System.err} y guarda el mensaje para mostrárselo al usuario sin
 *       cerrar la aplicación (uso documentado de manejo de excepciones).</li>
 *   <li>Exponer {@code getJuego()} para que {@code Main} pueda obtener el
 *       {@link javax.swing.JInternalFrame} concreto de Reflejos y registrarlo
 *       en el escritorio y en el sistema de récords.</li>
 * </ul>
 *
 * <p><b>Patrón Singleton:</b> solo existe una instancia de este adaptador;
 * la instancia del juego externo también es Singleton (ver {@link Reflejos}).
 *
 * <p><b>Manejo de excepciones:</b> cualquier {@link Exception} que surja al
 * llamar a {@link Reflejos#getInstancia()} se captura aquí:
 * <ol>
 *   <li>Se imprime la traza en {@code System.err} para facilitar la depuración.</li>
 *   <li>El mensaje queda disponible en {@link #getErrorDeInicializacion()}.</li>
 *   <li>{@link #isDisponible()} devuelve {@code false} para que quien use este
 *       adaptador sepa que no puede mostrar el juego.</li>
 *   <li>La aplicación principal <em>no</em> se cierra.</li>
 * </ol>
 */
public class ExternalGameAdapter implements GameFunction {

    private static ExternalGameAdapter instance;

    private Reflejos reflejos;
    private String errorDeInicializacion;

    // ------------------------------------------------------------------ Singleton

    /** Constructor privado: la inicialización de Reflejos se hace aquí y se
     *  protege con try-catch para que un fallo no propague la excepción. */
    private ExternalGameAdapter() {
        try {
            reflejos = Reflejos.getInstancia();
        } catch (Exception e) {
            // Registramos la causa completa para facilitar la depuración.
            System.err.println("[ExternalGameAdapter] Error al inicializar el juego externo 'Reflejos': "
                    + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace(System.err);
            errorDeInicializacion = e.getMessage() != null ? e.getMessage()
                    : e.getClass().getSimpleName();
        }
    }

    /**
     * Devuelve la única instancia del adaptador.
     * Si la construcción de Reflejos falló, {@link #isDisponible()} será
     * {@code false} y {@link #getErrorDeInicializacion()} tendrá el mensaje.
     */
    public static synchronized ExternalGameAdapter getInstance() {
        if (instance == null) {
            instance = new ExternalGameAdapter();
        }
        return instance;
    }

    // ------------------------------------------------------------------ Consultas

    /**
     * @return {@code true} si Reflejos se inicializó correctamente y está
     *         listo para jugar.
     */
    public boolean isDisponible() {
        return reflejos != null;
    }

    /**
     * @return el mensaje del error de inicialización, o {@code null} si no hubo
     *         error.
     */
    public String getErrorDeInicializacion() {
        return errorDeInicializacion;
    }

    /**
     * Devuelve la instancia de {@link Reflejos}, que extiende
     * {@link javax.swing.JInternalFrame}.
     * El llamador debe comprobar {@link #isDisponible()} antes de usar este
     * método.
     *
     * @return la instancia del juego externo, o {@code null} si falló la
     *         inicialización.
     */
    public Reflejos getJuego() {
        return reflejos;
    }

    // ------------------------------------------------------------------ GameFunction

    /**
     * Delega la llamada a {@link Reflejos#iniciar()}.
     * Si la inicialización falló, no hace nada (el error ya fue registrado en
     * el constructor).
     */
    @Override
    public void iniciar() {
        if (reflejos != null) {
            reflejos.iniciar();
        }
    }

    /**
     * Delega a {@link Reflejos#getStats()}.
     *
     * @return el resultado de la última partida terminada, o {@code null}.
     */
    @Override
    public Stat getStats() {
        return reflejos != null ? reflejos.getStats() : null;
    }
}
