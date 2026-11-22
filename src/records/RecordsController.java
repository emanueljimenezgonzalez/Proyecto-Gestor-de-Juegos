package records;

import core.Stat;
import javax.swing.SwingUtilities;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Ordena las operaciones de disco en un solo hilo y devuelve la respuesta al hilo Swing. */
public final class RecordsController implements AutoCloseable {
    private final RecordsManager manager;
    private final AtomicInteger operaciones = new AtomicInteger();
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "archivos-de-records");
        hilo.setDaemon(true);
        return hilo;
    });

    public RecordsController(RecordsManager manager) { this.manager = java.util.Objects.requireNonNull(manager); }
    public RecordsManager getManager() { return manager; }
    public boolean estaOcupado() { return operaciones.get() > 0; }

    public void registrarResultado(String juegoId, Stat stat, Consumer<Exception> respuesta) {
        java.util.Objects.requireNonNull(respuesta, "Falta la respuesta de la operación.");
        if (stat == null) {
            SwingUtilities.invokeLater(() -> respuesta.accept(new IllegalArgumentException("Todavía no hay un resultado final.")));
            return;
        }
        Stat copia = new Stat(stat.getClave(), stat.getNombre(), stat.getValor());
        ejecutar(() -> manager.registrarResultado(juegoId, copia), respuesta);
    }

    public void actualizar(String juegoId, Consumer<Exception> respuesta) {
        ejecutar(() -> manager.actualizar(juegoId), respuesta);
    }

    private void ejecutar(Operacion operacion, Consumer<Exception> respuesta) {
        java.util.Objects.requireNonNull(respuesta, "Falta la respuesta de la operación.");
        operaciones.incrementAndGet();
        try {
            ejecutor.execute(() -> {
                Exception fallo = null;
                try { operacion.ejecutar(); } catch (Exception e) { fallo = e; }
                Exception resultado = fallo;
                SwingUtilities.invokeLater(() -> {
                    operaciones.decrementAndGet();
                    respuesta.accept(resultado);
                });
            });
        } catch (RejectedExecutionException e) {
            operaciones.decrementAndGet();
            SwingUtilities.invokeLater(() -> respuesta.accept(new IllegalStateException("La aplicación está cerrando; no se pudo procesar la operación.", e)));
        }
    }

    @Override public void close() { ejecutor.shutdown(); }
    @FunctionalInterface private interface Operacion { void ejecutar() throws Exception; }
}
