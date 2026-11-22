package core;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.function.Supplier;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Registra los juegos disponibles y administra sus ventanas internas. */
public class GameManager {
    private final Map<String, Supplier<? extends JInternalFrame>> gameFactories = new LinkedHashMap<>();
    private final Map<String, JInternalFrame> gameInstances = new LinkedHashMap<>();
    private final Map<String, String> descripciones = new LinkedHashMap<>();
    private final List<Runnable> observadores = new CopyOnWriteArrayList<>();

    /** Registra un juego. Cada fábrica debe devolver su instancia Singleton. */
    public void registrarJuego(String nombre, Supplier<? extends JInternalFrame> factory) {
        registrarJuego(nombre, "", factory);
    }

    /** Descripción opcional; las pantallas se actualizan a partir del mismo catálogo. */
    public void registrarJuego(String nombre, String descripción, Supplier<? extends JInternalFrame> factory) {
        if (nombre == null || nombre.isBlank() || factory == null) {
            throw new IllegalArgumentException("El nombre y la fábrica del juego son obligatorios.");
        }
        gameFactories.put(nombre, factory);
        descripciones.put(nombre, descripción == null ? "" : descripción.trim());
        for (Runnable observador : observadores) observador.run();
    }

    public Set<String> getJuegosDisponibles() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(gameFactories.keySet()));
    }

    public String getDescripción(String nombre) { return descripciones.getOrDefault(nombre, ""); }
    public void agregarObservador(Runnable observador) { observadores.add(java.util.Objects.requireNonNull(observador)); }
    public void quitarObservador(Runnable observador) { observadores.remove(observador); }

    /** Abre o vuelve a mostrar la única instancia registrada del juego. */
    public JInternalFrame abrirJuego(String nombre, JDesktopPane desktop) {
        Supplier<? extends JInternalFrame> factory = gameFactories.get(nombre);
        if (factory == null) {
            throw new IllegalArgumentException("No existe un juego registrado con el nombre: " + nombre);
        }

        JInternalFrame frame = gameInstances.computeIfAbsent(nombre, key -> {
            JInternalFrame juego = factory.get();
            if (juego == null) throw new IllegalStateException("El juego no devolvió una ventana.");
            return juego;
        });
        if (frame.getDesktopPane() == null) {
            desktop.add(frame);
        }
        // Mantener los controles dentro del escritorio, también en ventanas pequeñas.
        if (!frame.isMaximum() && desktop.getWidth() > 0 && desktop.getHeight() > 0) {
            int ancho = Math.min(frame.getWidth(), desktop.getWidth());
            int alto = Math.min(frame.getHeight(), desktop.getHeight());
            frame.setBounds(Math.max(0, Math.min(frame.getX(), desktop.getWidth() - ancho)),
                    Math.max(0, Math.min(frame.getY(), desktop.getHeight() - alto)), ancho, alto);
        }
        frame.setVisible(true);
        try {
            frame.setSelected(true);
            frame.toFront();
        } catch (java.beans.PropertyVetoException ignored) {
            // La ventana sigue disponible aunque Swing rechace el cambio de selección.
        }
        return frame;
    }

    public JInternalFrame getInstancia(String nombre) {
        return gameInstances.get(nombre);
    }
}
