package tresenraya;

import core.GameFunction;
import core.PausableGame;
import core.PlayerNames;
import core.PlayerSession;
import core.Stat;
import ui.GameBackground;
import ui.RetroTypography;
import ui.UiSupport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tres en raya para dos jugadores. Coordina turnos, reglas, interfaz y el
 * resultado de la partida. El tablero y su recorrido viven en {@link Board}.
 *
 * <p>Resultado para récords: solo se publica cuando hay ganador. La clave es
 * "Victorias", el nombre es el del ganador y el valor son sus victorias
 * acumuladas en la sesión. Un empate no publica resultado.</p>
 */
public class TicTacToeGame extends JInternalFrame implements GameFunction, PausableGame {
    public static final String CLAVE_STAT = "Victorias";

    private static final Color FONDO_CASILLA = new Color(20, 31, 23);
    private static final Color FONDO_GANADORA = new Color(25, 110, 55);
    private static final Color COLOR_X = new Color(110, 245, 145);
    private static final Color COLOR_O = new Color(179, 124, 235);
    private static final Color TEXTO_SUAVE = new Color(183, 194, 185);

    private static TicTacToeGame instance;

    private final Board board = new Board();
    private final JButton[][] botones = new JButton[Board.SIZE][Board.SIZE];
    private final JTextField campoJugador1 = new JTextField("Jugador 1", 14);
    private final JTextField campoJugador2 = new JTextField("Jugador 2", 14);
    private final JLabel errorJugadores = new JLabel(" ");
    private final JLabel marcador = new JLabel(" ");
    private final JTextArea estado = new JTextArea(2, 25);
    private final Map<String, Integer> victorias = new LinkedHashMap<>();

    private String nombreX = "Jugador 1";
    private String nombreO = "Jugador 2";
    private Cell.Mark turno = Cell.Mark.X;
    private int partidasIniciadas;
    private int jugadas;
    private boolean terminada = true;
    private Stat stats;

    private TicTacToeGame() {
        super("Tres en raya", true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(560, 680);
        setMinimumSize(new Dimension(460, 560));

        JPanel contenido = new GameBackground("tresenraya", new BorderLayout(10, 10));
        contenido.setBackground(new Color(9, 14, 10));
        contenido.setBorder(new EmptyBorder(14, 16, 14, 16));
        setContentPane(contenido);

        contenido.add(crearCabecera(), BorderLayout.NORTH);
        contenido.add(crearTablero(), BorderLayout.CENTER);
        contenido.add(crearPie(), BorderLayout.SOUTH);

        configurarTeclas();
        addComponentListener(new ComponentAdapter() {
            // Al volver a abrir la ventana tras una partida terminada se empieza con el tablero limpio.
            @Override public void componentShown(ComponentEvent e) { limpiarSiTerminada(); }
        });
        mostrarTableroVacio("Escribe los nombres y pulsa ▶ Nueva partida.");
    }

    /** Singleton: solo existe una instancia del juego. */
    public static synchronized TicTacToeGame getInstance() {
        if (instance == null) instance = new TicTacToeGame();
        return instance;
    }

    // ------------------------------------------------------------------ GameFunction

    /** Inicia una partida nueva con los nombres de los dos jugadores. */
    @Override public void iniciar() {
        UiSupport.exigirSwing();
        String jugador1;
        String jugador2;
        try {
            jugador1 = PlayerSession.activa() ? PlayerSession.getNombre() : PlayerNames.validar(campoJugador1.getText());
            jugador2 = PlayerNames.validar(campoJugador2.getText());
            if (jugador1.equalsIgnoreCase(jugador2)) {
                throw new IllegalArgumentException("Los jugadores deben tener nombres distintos.");
            }
        } catch (IllegalArgumentException e) {
            errorJugadores.setText("<html>" + e.getMessage() + "</html>");
            campoJugador2.requestFocusInWindow();
            return;
        }
        errorJugadores.setText(" ");
        if (tienePartidaEnCurso() && !UiSupport.confirmarDescarte(this,
                "La partida actual se descartará sin registrar un resultado.", "Nueva partida")) return;
        comenzarPartida(jugador1, jugador2);
    }

    /** Devuelve una copia del resultado de la última partida con ganador, o null si no lo hay. */
    @Override public Stat getStats() {
        return stats == null ? null : new Stat(stats.getClave(), stats.getNombre(), stats.getValor());
    }

    // ------------------------------------------------------------------ PausableGame

    /** No hay temporizador: la partida espera sin cambios. */
    @Override public void pausarPartida() { }

    @Override public boolean tienePartidaEnCurso() { return !terminada && jugadas > 0; }

    // ------------------------------------------------------------------ Reglas

    private void comenzarPartida(String jugador1, String jugador2) {
        board.reset();
        nombreX = jugador1;
        nombreO = jugador2;
        turno = partidasIniciadas % 2 == 0 ? Cell.Mark.X : Cell.Mark.O; // el primer turno se alterna
        partidasIniciadas++;
        jugadas = 0;
        terminada = false;
        stats = null;
        campoJugador1.setEditable(false);
        campoJugador2.setEditable(false);
        actualizarVista(null);
        actualizarEstado("Turno de " + descripcion(turno));
    }

    /** Intenta jugar en una casilla; visible en el paquete para las pruebas. */
    void jugar(int fila, int columna) {
        UiSupport.exigirSwing();
        if (terminada) {
            actualizarEstado("No hay partida en curso. Pulsa ▶ Nueva partida.");
            return;
        }
        try {
            board.place(fila, columna, turno);
        } catch (IllegalStateException e) {
            // Casilla ocupada: se informa al jugador y el turno no cambia.
            actualizarEstado("Esa casilla ya está ocupada. Sigue siendo el turno de " + descripcion(turno));
            return;
        }
        jugadas++;
        Cell[] linea = board.findWinningLine();
        if (linea != null) {
            registrarVictoria(linea);
        } else if (board.isFull()) {
            terminarEmpate();
        } else {
            turno = turno == Cell.Mark.X ? Cell.Mark.O : Cell.Mark.X;
            actualizarVista(null);
            actualizarEstado("Turno de " + descripcion(turno));
        }
    }

    private void registrarVictoria(Cell[] linea) {
        terminada = true;
        String ganador = turno == Cell.Mark.X ? nombreX : nombreO;
        int total = victorias.merge(ganador, 1, Integer::sum);
        stats = new Stat(CLAVE_STAT, ganador, total);
        campoJugador2.setEditable(true);
        campoJugador1.setEditable(!PlayerSession.activa());
        actualizarVista(linea);
        actualizarEstado("¡Ganó " + descripcion(turno) + "! Victorias de " + ganador + " en la sesión: " + total);
        firePropertyChange("resultadoPartida", null, getStats());
    }

    private void terminarEmpate() {
        terminada = true;
        stats = null;
        campoJugador2.setEditable(true);
        campoJugador1.setEditable(!PlayerSession.activa());
        actualizarVista(null);
        actualizarEstado("Empate: el tablero se llenó sin ganador. Nadie suma victoria.");
    }

    private void limpiarSiTerminada() {
        if (terminada && jugadas > 0) {
            board.reset();
            jugadas = 0;
            actualizarVista(null);
            actualizarEstado("Pulsa ▶ Nueva partida para jugar otra vez.");
        }
    }

    private String descripcion(Cell.Mark marca) {
        return (marca == Cell.Mark.X ? nombreX : nombreO) + " (" + marca.getSymbol() + ")";
    }

    // ------------------------------------------------------------------ Interfaz

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Tres en raya");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(RetroTypography.fuente(Font.BOLD, 25));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cabecera.add(titulo);
        cabecera.add(Box.createVerticalStrut(8));

        JPanel jugadores = new JPanel(new GridLayout(0, 1, 0, 6));
        jugadores.setOpaque(false);
        jugadores.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (PlayerSession.activa()) {
            campoJugador1.setText(PlayerSession.getNombre());
            campoJugador1.setEditable(false);
        }
        jugadores.add(filaJugador("X", campoJugador1, "Nombre del jugador X"));
        jugadores.add(filaJugador("O", campoJugador2, "Nombre del jugador O"));
        cabecera.add(jugadores);

        errorJugadores.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorJugadores.setFont(RetroTypography.fuente(Font.PLAIN, 13));
        errorJugadores.setForeground(new Color(255, 191, 157));
        cabecera.add(errorJugadores);

        marcador.setAlignmentX(Component.LEFT_ALIGNMENT);
        marcador.setForeground(Color.WHITE);
        marcador.setFont(RetroTypography.fuente(Font.BOLD, 14));
        cabecera.add(marcador);
        return cabecera;
    }

    private JPanel filaJugador(String marca, JTextField campo, String nombreAccesible) {
        JPanel fila = new JPanel(new BorderLayout(10, 0));
        fila.setOpaque(false);
        JLabel etiqueta = new JLabel("Jugador " + marca);
        etiqueta.setForeground(marca.equals("X") ? COLOR_X : COLOR_O);
        etiqueta.setFont(RetroTypography.fuente(Font.BOLD, 14));
        etiqueta.setLabelFor(campo);
        campo.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        campo.getAccessibleContext().setAccessibleName(nombreAccesible);
        campo.getAccessibleContext().setAccessibleDescription("Entre 1 y 120 caracteres.");
        campo.addActionListener(e -> iniciar());
        fila.add(etiqueta, BorderLayout.WEST);
        fila.add(campo, BorderLayout.CENTER);
        return fila;
    }

    private JComponent crearTablero() {
        JPanel rejilla = new JPanel(new GridLayout(Board.SIZE, Board.SIZE, 8, 8));
        rejilla.setOpaque(false);
        rejilla.getAccessibleContext().setAccessibleName("Tablero de Tres en raya");
        for (int f = 0; f < Board.SIZE; f++) {
            for (int c = 0; c < Board.SIZE; c++) {
                final int fila = f;
                final int columna = c;
                JButton boton = UiSupport.boton("");
                boton.setFont(RetroTypography.fuente(Font.BOLD, 56));
                boton.setBackground(FONDO_CASILLA);
                boton.getAccessibleContext().setAccessibleName("Casilla fila " + (f + 1) + " columna " + (c + 1));
                boton.addActionListener(e -> jugar(fila, columna));
                botones[f][c] = boton;
                rejilla.add(boton);
            }
        }
        // Tablero cuadrado que ocupa el espacio disponible.
        return new SquareHolder(rejilla);
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        JPanel botonesPie = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        botonesPie.setOpaque(false);
        JButton nueva = UiSupport.boton("▶ Nueva partida");
        nueva.setToolTipText("Alt+N: iniciar una partida nueva");
        nueva.addActionListener(e -> iniciar());
        JButton ayuda = UiSupport.boton("Reglas");
        ayuda.setToolTipText("F1: consultar las reglas");
        ayuda.addActionListener(e -> mostrarAyuda());
        JButton inicio = UiSupport.boton("Volver al inicio");
        inicio.addActionListener(e -> firePropertyChange("volverInicio", false, true));
        botonesPie.add(nueva);
        botonesPie.add(ayuda);
        botonesPie.add(inicio);
        pie.add(botonesPie, BorderLayout.NORTH);

        estado.setEditable(false);
        estado.setFocusable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.setOpaque(false);
        estado.setForeground(TEXTO_SUAVE);
        estado.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        estado.getAccessibleContext().setAccessibleName("Estado de la partida de Tres en raya");
        pie.add(estado, BorderLayout.CENTER);
        return pie;
    }

    private void configurarTeclas() {
        JRootPane raiz = getRootPane();
        for (int i = 0; i < Board.CELL_COUNT; i++) {
            final int fila = i / Board.SIZE;
            final int columna = i % Board.SIZE;
            UiSupport.atajo(raiz, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                    String.valueOf(i + 1), "casilla" + (i + 1), () -> jugar(fila, columna));
        }
        UiSupport.atajo(raiz, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "alt N", "nuevaPartida", this::iniciar);
        UiSupport.atajo(raiz, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "F1", "ayuda", this::mostrarAyuda);
    }

    private void mostrarAyuda() {
        UiSupport.mostrarAyuda(this, "Reglas de Tres en raya",
                "Dos jugadores se turnan: el jugador X usa X y el jugador O usa O.\n"
                + "Gana quien completa una fila, columna o diagonal.\n"
                + "Si el tablero se llena sin ganador, es empate.\n"
                + "Controles: clic en una casilla o teclas 1 a 9 (de izquierda a derecha y de arriba abajo).\n"
                + "Alt+N: nueva partida. El primer turno se alterna en cada partida.\n"
                + "Solo las victorias se registran en los récords.");
    }

    private void mostrarTableroVacio(String mensaje) {
        actualizarVista(null);
        actualizarEstado(mensaje);
    }

    /** Dibuja el tablero con el recorrido del iterador y resalta la línea ganadora. */
    private void actualizarVista(Cell[] lineaGanadora) {
        for (Cell cell : board) {
            JButton boton = botones[cell.getRow()][cell.getCol()];
            Cell.Mark marca = cell.getMark();
            boton.setText(marca == Cell.Mark.EMPTY ? "" : String.valueOf(marca.getSymbol()));
            boton.setForeground(marca == Cell.Mark.X ? COLOR_X : COLOR_O);
            boton.setBackground(FONDO_CASILLA);
        }
        if (lineaGanadora != null) {
            for (Cell cell : lineaGanadora) {
                botones[cell.getRow()][cell.getCol()].setBackground(FONDO_GANADORA);
            }
        }
        marcador.setText(textoMarcador());
    }

    private String textoMarcador() {
        if (victorias.isEmpty()) return "Victorias en esta sesión: ninguna todavía";
        StringBuilder texto = new StringBuilder("Victorias en esta sesión: ");
        boolean primero = true;
        for (Map.Entry<String, Integer> entrada : victorias.entrySet()) {
            if (!primero) texto.append(" · ");
            texto.append(entrada.getKey()).append(' ').append(entrada.getValue());
            primero = false;
        }
        return texto.toString();
    }

    private void actualizarEstado(String mensaje) {
        estado.setText(mensaje);
        getAccessibleContext().setAccessibleDescription(mensaje);
    }

    /** Contenedor que mantiene su contenido cuadrado. */
    private static final class SquareHolder extends JPanel {
        SquareHolder(JComponent contenido) {
            super(new BorderLayout());
            setOpaque(false);
            add(contenido, BorderLayout.CENTER);
        }

        @Override public Dimension getPreferredSize() { return new Dimension(380, 380); }

        @Override public Dimension getMinimumSize() { return new Dimension(240, 240); }

        @Override public void setBounds(int x, int y, int ancho, int alto) {
            int lado = Math.max(1, Math.min(ancho, alto));
            super.setBounds(x + (ancho - lado) / 2, y + (alto - lado) / 2, lado, lado);
        }
    }
}