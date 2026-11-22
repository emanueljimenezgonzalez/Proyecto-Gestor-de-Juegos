package tetris;

import core.GameFunction;
import core.Stat;
import core.PausableGame;
import core.PlayerNames;
import ui.UiSupport;
import ui.GameBackground;
import ui.RetroTypography;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/** Una sola ventana de Tetris. Todas sus operaciones se realizan en el hilo Swing. */
public final class TetrisGame extends JInternalFrame implements GameFunction, PausableGame {
    private static TetrisGame instancia;
    private final TetrisBoard tablero = new TetrisBoard();
    private final TetrisPanel vista = new TetrisPanel(tablero);
    private final Timer reloj;
    private final JTextField nombre = new JTextField("Jugador", 12);
    private final JLabel errorNombre = new JLabel(" ");
    private final JLabel puntaje = new JLabel("0");
    private final JLabel progreso = new JLabel("Líneas: 0 · Nivel: 1");
    private final JTextArea estado = new JTextArea(2, 20);
    private final JButton pausa = boton("Pausar");
    private final List<JButton> movimientos = new ArrayList<>();
    private final JPanel siguiente = new SiguientePanel();
    private String jugador;
    private Stat stats;
    private boolean resultadoPublicado;

    private TetrisGame() {
        super("Tetris", true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(720, 700);
        setMinimumSize(new Dimension(600, 650));
        setLocation(25, 10);
        JPanel contenido = new GameBackground("tetris", new BorderLayout(12, 6));
        contenido.setBackground(new Color(9, 14, 10));
        contenido.setBorder(new EmptyBorder(8, 12, 8, 12));
        setContentPane(contenido);
        JLabel titulo = etiqueta("Tetris", 20, Font.BOLD);
        contenido.add(titulo, BorderLayout.NORTH);
        contenido.add(vista, BorderLayout.CENTER);
        contenido.add(crearControles(), BorderLayout.EAST);
        UiSupport.validarNombreAlEditar(nombre, errorNombre);
        nombre.addActionListener(e -> transferFocus());
        estado.setEditable(false);
        estado.setFocusable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.setOpaque(false);
        estado.setForeground(new Color(183, 194, 185));
        estado.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        estado.getAccessibleContext().setAccessibleName("Estado de la partida de Tetris");
        contenido.add(estado, BorderLayout.SOUTH);

        reloj = new Timer(tablero.getIntervalo(), e -> {
            if (!isVisible() || !isSelected()) { pausarPartida(); return; }
            tablero.bajar(false);
            actualizar();
        });
        configurarTeclas();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override public void internalFrameDeactivated(InternalFrameEvent e) { pausarPartida(); }
            @Override public void internalFrameIconified(InternalFrameEvent e) { pausarPartida(); }
            @Override public void internalFrameClosing(InternalFrameEvent e) { pausarPartida(); }
        });
        addComponentListener(new ComponentAdapter() {
            @Override public void componentHidden(ComponentEvent e) { pausarPartida(); }
        });
        actualizar();
    }

    public static synchronized TetrisGame getInstance() {
        if (instancia == null) instancia = new TetrisGame();
        return instancia;
    }

    private JPanel crearControles() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setOpaque(false);
        JLabel nombreLabel = etiqueta("Jugador", 13, Font.PLAIN);
        if (core.PlayerSession.activa()) {
            nombre.setText(core.PlayerSession.getNombre());
            nombre.setVisible(false);
            nombreLabel.setText("Jugador: " + core.PlayerSession.getNombre());
            errorNombre.setVisible(false);
        }
        nombreLabel.setLabelFor(nombre);
        agregar(lateral, nombreLabel, 4);
        nombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nombre.getAccessibleContext().setAccessibleName("Nombre del jugador de Tetris");
        agregar(lateral, nombre, 2);
        errorNombre.setFont(RetroTypography.fuente(Font.PLAIN, 13));
        errorNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        errorNombre.setPreferredSize(new Dimension(240, 40));
        agregar(lateral, errorNombre, 10);
        agregar(lateral, etiqueta("Puntaje", 13, Font.PLAIN), 2);
        puntaje.setFont(RetroTypography.fuente(Font.BOLD, 28));
        puntaje.setForeground(Color.WHITE);
        puntaje.setMinimumSize(new Dimension(150, 36));
        puntaje.setPreferredSize(new Dimension(180, 36));
        puntaje.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        agregar(lateral, puntaje, 4);
        progreso.setForeground(new Color(183, 194, 185));
        progreso.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        progreso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        agregar(lateral, progreso, 10);
        agregar(lateral, etiqueta("Siguiente pieza", 13, Font.PLAIN), 2);
        siguiente.setPreferredSize(new Dimension(180, 64));
        siguiente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        siguiente.setMinimumSize(new Dimension(150, 64));
        agregar(lateral, siguiente, 10);
        JButton nueva = boton("▶ Jugar");
        nueva.setToolTipText("Alt+N: jugar; confirma antes de descartar una partida");
        nueva.addActionListener(e -> iniciar());
        agregar(lateral, nueva, 8);
        JPanel acciones = new JPanel(new GridLayout(0, 2, 6, 6));
        acciones.setOpaque(false);
        acciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        acciones.setPreferredSize(new Dimension(190, 76));
        acciones.add(movimiento("← / A", "Flecha izquierda", () -> tablero.mover(-1)));
        acciones.add(movimiento("→ / D", "Flecha derecha", () -> tablero.mover(1)));
        acciones.add(movimiento("Rotar · ↑ / W", "Flecha arriba", () -> tablero.rotar()));
        acciones.add(movimiento("Bajar · ↓ / S", "Flecha abajo", () -> tablero.bajar(true)));
        JButton caer = movimiento("Caer · Espacio", "Barra espaciadora", () -> tablero.caer());
        pausa.setToolTipText("P: pausar o reanudar");
        pausa.addActionListener(e -> alternarPausa());
        JButton inicio = boton("Inicio");
        inicio.setToolTipText("Volver al Inicio y conservar la partida en pausa");
        inicio.addActionListener(e -> {
            pausarPartida();
            firePropertyChange("volverInicio", false, true);
        });
        agregar(lateral, acciones, 8);
        agregar(lateral, caer, 8);
        JPanel navegación = new JPanel(new GridLayout(1, 2, 6, 0));
        navegación.setOpaque(false);
        navegación.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        navegación.add(pausa);
        navegación.add(inicio);
        agregar(lateral, navegación, 8);
        JButton ayuda = boton("Controles");
        ayuda.setToolTipText("F1: consultar los controles; la partida se pausa");
        ayuda.addActionListener(e -> mostrarAyuda());
        agregar(lateral, ayuda, 6);
        JLabel teclas = etiqueta("<html>← / A: izquierda · → / D: derecha<br>↑ / W: rotar · ↓ / S: bajar<br>Espacio: caída directa<br>P: pausa / reanudar · Esc: pausar<br>F1: ayuda · Alt+N: jugar</html>", 13, Font.PLAIN);
        agregar(lateral, teclas, 0);
        teclas.setOpaque(true);
        teclas.setBackground(new Color(9, 14, 10));
        teclas.setBorder(new EmptyBorder(5, 5, 5, 5));
        lateral.add(Box.createVerticalGlue());
        lateral.setPreferredSize(new Dimension(300, 530));
        return lateral;
    }

    private static void agregar(JPanel contenedor, JComponent componente, int espacio) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        contenedor.add(componente);
        if (espacio > 0) contenedor.add(Box.createVerticalStrut(espacio));
    }

    private static JLabel etiqueta(String texto, int tamaño, int estilo) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(new Color(248, 250, 248));
        etiqueta.setFont(RetroTypography.fuente(estilo, tamaño));
        return etiqueta;
    }

    private static JButton boton(String texto) {
        JButton boton = UiSupport.boton(texto);
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        return boton;
    }

    private JButton movimiento(String texto, String tecla, Runnable accion) {
        JButton boton = boton(texto);
        boton.setToolTipText(tecla);
        boton.addActionListener(e -> ejecutarMovimiento(accion));
        movimientos.add(boton);
        return boton;
    }

    private void configurarTeclas() {
        enlazar("LEFT", "izquierda", () -> ejecutarMovimiento(() -> tablero.mover(-1)));
        enlazar("RIGHT", "derecha", () -> ejecutarMovimiento(() -> tablero.mover(1)));
        enlazar("UP", "rotar", () -> ejecutarMovimiento(() -> tablero.rotar()));
        enlazar("DOWN", "bajar", () -> ejecutarMovimiento(() -> tablero.bajar(true)));
        enlazar("SPACE", "caer", () -> ejecutarMovimiento(() -> tablero.caer()));
        enlazar("P", "pausa", this::alternarPausa);
        enlazar("A", "a", () -> ejecutarMovimiento(() -> tablero.mover(-1)));
        enlazar("D", "d", () -> ejecutarMovimiento(() -> tablero.mover(1)));
        enlazar("W", "w", () -> ejecutarMovimiento(() -> tablero.rotar()));
        enlazar("S", "s", () -> ejecutarMovimiento(() -> tablero.bajar(true)));
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ESCAPE", "pausar", this::pausarPartida);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "F1", "ayuda", this::mostrarAyuda);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "alt N", "nuevaPartida", this::iniciar);
    }

    private void enlazar(String tecla, String accion, Runnable tarea) {
        vista.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(tecla), accion);
        vista.getActionMap().put(accion, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { tarea.run(); }
        });
    }

    private void ejecutarMovimiento(Runnable accion) {
        if (tablero.getEstado() != TetrisBoard.Estado.JUGANDO) return;
        accion.run();
        actualizar();
        vista.requestFocusInWindow();
    }

    @Override public void iniciar() {
        exigirSwing();
        String ingresado;
        try { ingresado = core.PlayerSession.activa() ? core.PlayerSession.getNombre() : PlayerNames.validar(nombre.getText()); }
        catch (IllegalArgumentException e) { UiSupport.errorNombre(nombre, errorNombre, e.getMessage()); return; }
        UiSupport.errorNombre(nombre, errorNombre, null);
        if (tablero.getEstado() == TetrisBoard.Estado.JUGANDO || tablero.getEstado() == TetrisBoard.Estado.PAUSADO) {
            pausarPartida();
            if (!UiSupport.confirmarDescarte(this,
                    "La partida actual se descartará sin registrar un resultado.", "Nueva partida")) return;
        }
        iniciarPartida(ingresado);
    }

    /** Inicio programático con nombre explícito; no abre diálogos ni registra la partida descartada. */
    public void iniciarPartida(String nombreJugador) {
        exigirSwing();
        String válido = PlayerNames.validar(nombreJugador);
        reloj.stop();
        jugador = válido;
        nombre.setText(jugador);
        UiSupport.errorNombre(nombre, errorNombre, null);
        stats = null;
        resultadoPublicado = false;
        tablero.iniciar();
        reloj.setInitialDelay(tablero.getIntervalo());
        reloj.setDelay(tablero.getIntervalo());
        actualizar();
        reloj.start();
        vista.requestFocusInWindow();
    }

    @Override public void pausarPartida() {
        exigirSwing();
        if (tablero.getEstado() == TetrisBoard.Estado.JUGANDO) {
            tablero.pausar();
            reloj.stop();
            actualizar();
        }
    }

    @Override public boolean tienePartidaEnCurso() {
        return tablero.getEstado() == TetrisBoard.Estado.JUGANDO || tablero.getEstado() == TetrisBoard.Estado.PAUSADO;
    }

    private void mostrarAyuda() {
        pausarPartida();
        UiSupport.mostrarAyuda(this, "Controles de Tetris",
                "Flechas o W/A/S/D: mover, rotar y bajar.\nArriba o W: rotar. Izquierda/A y Derecha/D: mover.\nAbajo o S: bajar una celda. Espacio: caída directa.\nP: pausar o reanudar. Esc: pausar.\nAlt+N: nueva partida (pide confirmar si ya estás jugando).\nTab y Mayús+Tab: recorrer los controles.\nEnter o Espacio: activar el botón enfocado.\n\nCompleta filas para eliminarlas y sumar puntos.\nLa partida termina cuando no hay espacio para la siguiente pieza.\nCambiar de ventana pausa la partida; reanuda cuando estés listo.");
    }

    private void alternarPausa() {
        if (tablero.getEstado() == TetrisBoard.Estado.JUGANDO) pausarPartida();
        else if (tablero.getEstado() == TetrisBoard.Estado.PAUSADO) {
            tablero.reanudar();
            reloj.restart();
            actualizar();
        }
        vista.requestFocusInWindow();
    }

    private void actualizar() {
        TetrisBoard.Estado situación = tablero.getEstado();
        if (situación == TetrisBoard.Estado.TERMINADO && !resultadoPublicado) {
            reloj.stop();
            resultadoPublicado = true;
            stats = new Stat("Puntaje", jugador, tablero.getPuntaje());
            firePropertyChange("resultadoPartida", null, getStats());
        }
        puntaje.setText(Integer.toString(tablero.getPuntaje()));
        progreso.setText("Líneas: " + tablero.getLíneas() + " · Nivel: " + tablero.getNivel());
        nombre.setEditable(situación == TetrisBoard.Estado.LISTO || situación == TetrisBoard.Estado.TERMINADO);
        nombre.setToolTipText(nombre.isEditable() ? "Nombre del jugador, de 1 a 120 caracteres."
                : "El nombre se conserva hasta terminar o reiniciar la partida.");
        pausa.setEnabled(situación == TetrisBoard.Estado.JUGANDO || situación == TetrisBoard.Estado.PAUSADO);
        pausa.setText(situación == TetrisBoard.Estado.PAUSADO ? "▶ Seguir · P" : "Pausa · P");
        pausa.setToolTipText(pausa.isEnabled() ? "P: pausar o reanudar" : "Inicia una partida para poder pausarla.");
        for (JButton boton : movimientos) {
            boton.setEnabled(situación == TetrisBoard.Estado.JUGANDO);
            boton.getAccessibleContext().setAccessibleDescription(boton.isEnabled() ? boton.getToolTipText()
                    : situación == TetrisBoard.Estado.PAUSADO ? "Reanuda la partida para usar este control." : "Inicia una partida para usar este control.");
        }
        switch (situación) {
            case LISTO: estado.setText("Pulsa ▶ Jugar cuando estés listo."); break;
            case JUGANDO: estado.setText("Partida en curso · " + jugador); break;
            case PAUSADO: estado.setText("Partida en pausa. Pulsa Reanudar para continuar."); break;
            case TERMINADO: estado.setText("Partida terminada · " + jugador + " · Puntaje: " + tablero.getPuntaje()); break;
            default: break;
        }
        if (reloj != null) reloj.setDelay(tablero.getIntervalo());
        vista.getAccessibleContext().setAccessibleDescription(estado.getText() + ". Puntaje: " + tablero.getPuntaje());
        siguiente.getAccessibleContext().setAccessibleName(tablero.getSiguiente() == null
                ? "Sin siguiente pieza" : "Siguiente pieza: " + tablero.getSiguiente().name());
        vista.repaint();
        siguiente.repaint();
    }

    @Override public Stat getStats() {
        return stats == null ? null : new Stat(stats.getClave(), stats.getNombre(), stats.getValor());
    }

    private static void exigirSwing() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("La ventana debe controlarse desde el hilo Swing.");
    }

    private final class SiguientePanel extends JPanel {
        SiguientePanel() { setBackground(new Color(20, 31, 23)); }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Tetromino pieza = tablero.getSiguiente();
            if (pieza == null) return;
            Graphics2D g = (Graphics2D) graphics.create();
            int minFila = 4, maxFila = 0, minColumna = 4, maxColumna = 0;
            for (int f = 0; f < pieza.getTamaño(); f++) for (int c = 0; c < pieza.getTamaño(); c++) {
                if (!pieza.ocupa(0, f, c)) continue;
                minFila = Math.min(minFila, f); maxFila = Math.max(maxFila, f);
                minColumna = Math.min(minColumna, c); maxColumna = Math.max(maxColumna, c);
            }
            int lado = 22;
            int x = (getWidth() - (maxColumna - minColumna + 1) * lado) / 2;
            int y = (getHeight() - (maxFila - minFila + 1) * lado) / 2;
            for (int f = 0; f < pieza.getTamaño(); f++) for (int c = 0; c < pieza.getTamaño(); c++)
                if (pieza.ocupa(0, f, c)) TetrisPanel.bloque(g, pieza, x + (c - minColumna) * lado,
                        y + (f - minFila) * lado, lado);
            g.dispose();
        }
    }
}
