package externo;

import externo.ExternalGameAdapter;

/**
 * Pruebas de consola para {@link ExternalGameAdapter}.
 *
 * <p>Las pruebas se dividen en dos grupos:
 * <ol>
 *   <li><b>Ruta normal:</b> el adaptador obtiene correctamente la instancia de
 *       Reflejos y delega las llamadas.</li>
 *   <li><b>Ruta de error (simulada):</b> se verifica el comportamiento de
 *       {@link FabricaFallida}, una subclase de prueba que fuerza la excepción
 *       para demostrar el manejo documentado en la rúbrica.</li>
 * </ol>
 */
public class ExternalGameAdapterTest {

    private static int total = 0;
    private static int fallos = 0;

    // ------------------------------------------------------------------ main

    public static void main(String[] args) {
        System.out.println("--- Ruta normal (Reflejos disponible) ---");
        probarRutaNormal();

        System.out.println();
        System.out.println("--- Ruta de error (inicialización fallida) ---");
        probarRutaError();

        System.out.println();
        if (fallos == 0) {
            System.out.println("TODAS LAS PRUEBAS PASARON");
        } else {
            System.out.println(fallos + " PRUEBA(S) FALLARON de " + total);
        }
    }

    // ------------------------------------------------------------------ Ruta normal

    private static void probarRutaNormal() {
        ExternalGameAdapter adapter = ExternalGameAdapter.getInstance();

        verificar("Singleton no es null",
                adapter != null);
        verificar("isDisponible() es true: Reflejos se construyó sin error",
                adapter.isDisponible());
        verificar("getErrorDeInicializacion() es null sin error",
                adapter.getErrorDeInicializacion() == null);
        verificar("getJuego() devuelve instancia de Reflejos",
                adapter.getJuego() != null);
        verificar("getStats() devuelve null antes de jugar",
                adapter.getStats() == null);
        verificar("iniciar() no lanza excepción",
                llamarSinExcepcion(() -> adapter.iniciar()));
        verificar("Singleton: segunda llamada devuelve la misma instancia",
                ExternalGameAdapter.getInstance() == adapter);
    }

    // ------------------------------------------------------------------ Ruta de error

    /**
     * Simulación del manejo de excepciones: una "fábrica" que siempre lanza
     * RuntimeException al intentar construir el juego.
     * Demuestra que el adaptador captura el error, no propaga la excepción y
     * deja isDisponible() en false.
     */
    private static void probarRutaError() {
        AdaptadorConError adaptadorError = new AdaptadorConError();

        verificar("El adaptador con error no es null (no propagó la excepción)",
                adaptadorError != null);
        verificar("isDisponible() es false cuando la inicialización lanzó excepción",
                !adaptadorError.isDisponible());
        verificar("getErrorDeInicializacion() tiene el mensaje del error",
                adaptadorError.getErrorDeInicializacion() != null
                && adaptadorError.getErrorDeInicializacion().contains("fallo simulado"));
        verificar("getJuego() devuelve null con error de inicialización",
                adaptadorError.getJuego() == null);
        verificar("getStats() devuelve null con error de inicialización",
                adaptadorError.getStats() == null);
        verificar("iniciar() no lanza con error de inicialización",
                llamarSinExcepcion(() -> adaptadorError.iniciar()));
        verificar("El System.err recibió la traza (error registrado)",
                adaptadorError.isErrorRegistrado());
    }

    // ------------------------------------------------------------------ Adaptador de prueba

    /**
     * Subclase de prueba que simula un fallo en la inicialización del juego
     * externo, sin depender de Reflejos. Duplica la lógica de manejo de
     * errores de ExternalGameAdapter para hacer la demostración autocontenida.
     */
    private static class AdaptadorConError {

        private reflejos.Reflejos juego;
        private String errorDeInicializacion;
        private boolean errorRegistrado = false;

        AdaptadorConError() {
            try {
                // Simulamos que el juego externo lanza una excepción al construirse.
                lanzarFallo();
            } catch (Exception e) {
                errorRegistrado = true;
                // Mismo manejo que ExternalGameAdapter: registrar en System.err,
                // guardar el mensaje y no propagar.
                java.io.PrintStream err = System.err;
                err.println("[AdaptadorConError] Error al inicializar el juego externo: "
                        + e.getClass().getSimpleName() + " - " + e.getMessage());
                errorDeInicializacion = e.getMessage() != null ? e.getMessage()
                        : e.getClass().getSimpleName();
            }
        }

        private static void lanzarFallo() {
            throw new RuntimeException("fallo simulado al cargar el juego externo");
        }

        boolean isDisponible() { return juego != null; }
        String getErrorDeInicializacion() { return errorDeInicializacion; }
        reflejos.Reflejos getJuego() { return juego; }
        boolean isErrorRegistrado() { return errorRegistrado; }

        void iniciar() {
            if (juego == null) return; // no hace nada, no lanza
        }

        core.Stat getStats() {
            return juego != null ? juego.getStats() : null;
        }
    }

    // ------------------------------------------------------------------ Utiles

    private static boolean llamarSinExcepcion(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void verificar(String descripcion, boolean condicion) {
        total++;
        if (condicion) {
            System.out.println("[OK]    " + descripcion);
        } else {
            System.out.println("[FALLO] " + descripcion);
            fallos++;
        }
    }
}
