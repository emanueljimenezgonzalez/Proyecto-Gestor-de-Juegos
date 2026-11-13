package core;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Registra los juegos disponibles y administra sus ventanas internas. */
public class GameManager {
    private final Map<String, Supplier<? extends JInternalFrame>> gameFactories = new LinkedHashMap<>();
    private final Map<String, JInternalFrame> gameInstances = new LinkedHashMap<>();

    /** Registra un juego. Cada fábrica debe devolver su instancia Singleton. */
    public void registrarJuego(String nombre, Supplier<? extends JInternalFrame> factory) {
        if (nombre == null || nombre.isBlank() || factory == null) {
            throw new IllegalArgumentException("El nombre y la fábrica del juego son obligatorios.");
        }
        gameFactories.put(nombre, factory);
    }

    public Set<String> getJuegosDisponibles() {
        return Set.copyOf(gameFactories.keySet());
    }

    /** Abre o vuelve a mostrar la única instancia registrada del juego. */
    public JInternalFrame abrirJuego(String nombre, JDesktopPane desktop) {
        Supplier<? extends JInternalFrame> factory = gameFactories.get(nombre);
        if (factory == null) {
            throw new IllegalArgumentException("No existe un juego registrado con el nombre: " + nombre);
        }

        JInternalFrame frame = gameInstances.computeIfAbsent(nombre, key -> factory.get());
        if (frame.getDesktopPane() == null) {
            desktop.add(frame);
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
