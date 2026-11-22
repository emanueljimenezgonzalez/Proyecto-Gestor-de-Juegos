package snake;

import core.GameFunction;
import core.PausableGame;
import core.PlayerNames;
import core.Stat;
import ui.UiSupport;
import ui.GameBackground;
import ui.RetroTypography;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** Snake con controles locales, pausa segura y un único resultado por partida. */
public class SnakeGame extends JInternalFrame implements GameFunction, PausableGame {
    private static final int COLUMNS = 25;
    private static final int ROWS = 20;
    private static final int INITIAL_DELAY = 135;
    private static SnakeGame instance;
    private final Deque<Point> snake = new ArrayDeque<>();
    private final Random random = new Random();
    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel scoreLabel = new JLabel("Puntaje: 0");
    private final JTextArea statusLabel = new JTextArea(2, 25);
    private final JTextField nombre = new JTextField("Jugador", 16);
    private final JLabel errorNombre = new JLabel(" ");
    private final JButton pausa = UiSupport.boton("Pausar");
    private final List<JButton> controles = new ArrayList<>();
    private final Timer timer;
    private Point food;
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private String playerName = "Jugador";
    private int score;
    private boolean gameOver = true;
    private boolean paused;
    private boolean turnQueued;
    private Stat stats;

    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx;
        final int dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    private SnakeGame() {
        super("Snake", true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(620, 700);
        setMinimumSize(new Dimension(520, 540));
        JPanel contenido = new GameBackground("snake", new BorderLayout(10, 10));
        contenido.setBackground(new Color(9, 14, 10));
        contenido.setBorder(new EmptyBorder(14, 16, 14, 16));
        setContentPane(contenido);
        JPanel cabecera = new JPanel();
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        JLabel título = new JLabel("Snake");
        título.setForeground(Color.WHITE);
        título.setFont(RetroTypography.fuente(Font.BOLD, 25));
        título.setAlignmentX(Component.LEFT_ALIGNMENT);
        cabecera.add(título);
        cabecera.add(Box.createVerticalStrut(10));
        JPanel jugador = new JPanel(new BorderLayout(10, 0));
        jugador.setOpaque(false);
        jugador.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etiqueta = new JLabel("Jugador");
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setLabelFor(nombre);
        nombre.getAccessibleContext().setAccessibleName("Nombre del jugador de Snake");
        nombre.getAccessibleContext().setAccessibleDescription("Entre 1 y 120 caracteres.");
        jugador.add(etiqueta, BorderLayout.WEST);
        jugador.add(nombre, BorderLayout.CENTER);
        if (core.PlayerSession.activa()) {
            nombre.setText(core.PlayerSession.getNombre());
            etiqueta.setText("Jugador: " + core.PlayerSession.getNombre());
            nombre.setVisible(false);
        }
        cabecera.add(jugador);
        UiSupport.validarNombreAlEditar(nombre, errorNombre);
        nombre.addActionListener(e -> transferFocus());
        errorNombre.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorNombre.setFont(RetroTypography.fuente(Font.PLAIN, 13));
        cabecera.add(errorNombre);
        errorNombre.setVisible(!core.PlayerSession.activa());
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(RetroTypography.fuente(Font.BOLD, 16));
        cabecera.add(scoreLabel);
        contenido.add(cabecera, BorderLayout.NORTH);
        contenido.add(boardPanel, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        botones.setOpaque(false);
        JButton nueva = UiSupport.boton("▶ Jugar");
        nueva.setToolTipText("Alt+N: iniciar o reiniciar una partida");
        nueva.addActionListener(e -> iniciar());
        pausa.addActionListener(e -> alternarPausa());
        JButton ayuda = UiSupport.boton("Controles");
        ayuda.setToolTipText("F1: consultar los controles; la partida se pausa");
        ayuda.addActionListener(e -> mostrarAyuda());
        JButton inicio = UiSupport.boton("Volver al inicio");
        inicio.setToolTipText("Volver al Inicio y conservar la partida en pausa");
        inicio.addActionListener(e -> {
            pausarPartida();
            firePropertyChange("volverInicio", false, true);
        });
        botones.add(nueva); botones.add(pausa); botones.add(inicio); botones.add(ayuda);
        JPanel direcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        direcciones.setOpaque(false);
        añadirDirección(direcciones, "Izquierda", Direction.LEFT);
        añadirDirección(direcciones, "Arriba", Direction.UP);
        añadirDirección(direcciones, "Abajo", Direction.DOWN);
        añadirDirección(direcciones, "Derecha", Direction.RIGHT);
        JPanel acciones = new JPanel(new BorderLayout(0, 6));
        acciones.setOpaque(false);
        acciones.add(botones, BorderLayout.NORTH);
        JPanel indicaciones = new JPanel(new BorderLayout(0, 5));
        indicaciones.setOpaque(false);
        indicaciones.add(direcciones, BorderLayout.NORTH);
        JLabel resumen = new JLabel("Flechas / WASD: mover   ·   P: pausa   ·   F1: ayuda", SwingConstants.CENTER);
        resumen.setForeground(new Color(183, 194, 185));
        resumen.setFont(RetroTypography.fuente(Font.PLAIN, 13));
        indicaciones.add(resumen, BorderLayout.SOUTH);
        acciones.add(indicaciones, BorderLayout.SOUTH);
        pie.add(acciones, BorderLayout.NORTH);
        statusLabel.setEditable(false);
        statusLabel.setFocusable(false);
        statusLabel.setLineWrap(true);
        statusLabel.setWrapStyleWord(true);
        statusLabel.setOpaque(false);
        statusLabel.setForeground(new Color(183, 194, 185));
        statusLabel.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        statusLabel.getAccessibleContext().setAccessibleName("Estado de la partida de Snake");
        pie.add(statusLabel, BorderLayout.SOUTH);
        contenido.add(pie, BorderLayout.SOUTH);
        timer = new Timer(INITIAL_DELAY, this::moveSnake);
        configurarTeclas();
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override public void internalFrameDeactivated(InternalFrameEvent e) { pausarPartida(); }
            @Override public void internalFrameIconified(InternalFrameEvent e) { pausarPartida(); }
            @Override public void internalFrameClosing(InternalFrameEvent e) { pausarPartida(); }
        });
        addComponentListener(new ComponentAdapter() {
            @Override public void componentHidden(ComponentEvent e) { pausarPartida(); }
        });
        prepararTablero();
        actualizar("Pulsa ▶ Jugar cuando estés listo.");
    }

    public static synchronized SnakeGame getInstance() {
        if (instance == null) instance = new SnakeGame();
        return instance;
    }

    @Override public void iniciar() {
        UiSupport.exigirSwing();
        String jugador;
        try { jugador = core.PlayerSession.activa() ? core.PlayerSession.getNombre() : PlayerNames.validar(nombre.getText()); }
        catch (IllegalArgumentException e) { UiSupport.errorNombre(nombre, errorNombre, e.getMessage()); return; }
        UiSupport.errorNombre(nombre, errorNombre, null);
        if (!gameOver) {
            pausarPartida();
            if (!UiSupport.confirmarDescarte(this,
                    "La partida actual se descartará sin registrar un resultado.", "Nueva partida")) return;
        }
        iniciarPartida(jugador);
    }

    public void iniciarPartida(String jugador) {
        UiSupport.exigirSwing();
        String válido = PlayerNames.validar(jugador);
        timer.stop();
        playerName = válido;
        nombre.setText(válido);
        UiSupport.errorNombre(nombre, errorNombre, null);
        score = 0;
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        turnQueued = false;
        gameOver = false;
        paused = false;
        stats = null;
        prepararTablero();
        actualizar("Partida en curso · " + playerName);
        boardPanel.requestFocusInWindow();
        timer.restart();
    }

    @Override public void pausarPartida() {
        UiSupport.exigirSwing();
        if (!gameOver && !paused) {
            paused = true;
            timer.stop();
            actualizar("Partida en pausa. Pulsa Reanudar para continuar.");
        }
    }

    @Override public boolean tienePartidaEnCurso() { return !gameOver; }

    private void alternarPausa() {
        if (gameOver) return;
        if (!paused) pausarPartida();
        else {
            paused = false;
            timer.restart();
            actualizar("Partida en curso · " + playerName);
        }
        boardPanel.requestFocusInWindow();
    }

    private void mostrarAyuda() {
        pausarPartida();
        UiSupport.mostrarAyuda(this, "Controles de Snake",
                "Flechas o W/A/S/D: cambiar de dirección.\nP o Espacio: pausar o reanudar.\nEsc: pausar.\nAlt+N: nueva partida (pide confirmar si ya estás jugando).\nTab y Mayús+Tab: recorrer los controles.\nEnter o Espacio: activar el botón enfocado.\n\nCome la comida circular y evita las paredes y tu cuerpo.\nCada comida suma 10 puntos. Al llenar el tablero ganas.\nCambiar de ventana pausa la partida; reanuda cuando estés listo.");
    }

    private void prepararTablero() {
        snake.clear();
        int x = COLUMNS / 2, y = ROWS / 2;
        snake.add(new Point(x, y)); snake.add(new Point(x - 1, y)); snake.add(new Point(x - 2, y));
        colocarComida();
    }

    private void colocarComida() {
        List<Point> libres = new ArrayList<>();
        for (int y = 0; y < ROWS; y++) for (int x = 0; x < COLUMNS; x++) {
            Point punto = new Point(x, y);
            if (!snake.contains(punto)) libres.add(punto);
        }
        food = libres.isEmpty() ? null : libres.get(random.nextInt(libres.size()));
    }

    private void configurarTeclas() {
        enlazar("UP", "up", Direction.UP); enlazar("W", "w", Direction.UP);
        enlazar("DOWN", "down", Direction.DOWN); enlazar("S", "s", Direction.DOWN);
        enlazar("LEFT", "left", Direction.LEFT); enlazar("A", "a", Direction.LEFT);
        enlazar("RIGHT", "right", Direction.RIGHT); enlazar("D", "d", Direction.RIGHT);
        UiSupport.atajo(boardPanel, JComponent.WHEN_FOCUSED, "P", "pausa", this::alternarPausa);
        UiSupport.atajo(boardPanel, JComponent.WHEN_FOCUSED, "SPACE", "pausaEspacio", this::alternarPausa);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ESCAPE", "pausar", this::pausarPartida);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "F1", "ayuda", this::mostrarAyuda);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "alt N", "nuevaPartida", this::iniciar);
    }

    private void enlazar(String tecla, String nombreAcción, Direction dirección) {
        boardPanel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(tecla), nombreAcción);
        boardPanel.getActionMap().put(nombreAcción, new DirectionAction(dirección));
    }

    private void añadirDirección(JPanel panel, String texto, Direction dirección) {
        JButton botón = UiSupport.boton(texto);
        botón.addActionListener(e -> {
            new DirectionAction(dirección).actionPerformed(e);
            boardPanel.requestFocusInWindow();
        });
        controles.add(botón);
        panel.add(botón);
    }

    private class DirectionAction extends AbstractAction {
        private final Direction requested;
        DirectionAction(Direction requested) { this.requested = requested; }
        @Override public void actionPerformed(ActionEvent event) {
            if (!gameOver && !paused && !turnQueued && requested != direction
                    && !(requested.dx == -direction.dx && requested.dy == -direction.dy)) {
                nextDirection = requested;
                turnQueued = true;
            }
        }
    }

    private void moveSnake(ActionEvent event) {
        if (gameOver || paused) return;
        if (!isVisible() || !isSelected()) { pausarPartida(); return; }
        direction = nextDirection;
        turnQueued = false;
        Point head = snake.peekFirst();
        Point newHead = new Point(head.x + direction.dx, head.y + direction.dy);
        boolean eating = newHead.equals(food);
        Point tail = snake.peekLast();
        boolean hitsBody = snake.contains(newHead) && !(newHead.equals(tail) && !eating);
        if (newHead.x < 0 || newHead.x >= COLUMNS || newHead.y < 0 || newHead.y >= ROWS || hitsBody) {
            terminarPartida(); return;
        }
        snake.addFirst(newHead);
        if (eating) {
            score += 10;
            colocarComida();
            if (food == null) { finalizarPartida("Tablero completo. ¡Ganaste!"); return; }
        } else snake.removeLast();
        actualizar("Partida en curso · " + playerName);
    }

    private void terminarPartida() { finalizarPartida("Partida terminada"); }

    private void finalizarPartida(String mensaje) {
        if (gameOver) return;
        timer.stop();
        gameOver = true;
        paused = false;
        stats = new Stat("Puntaje", playerName, score);
        firePropertyChange("resultadoPartida", null, getStats());
        actualizar(mensaje + " · Puntaje: " + score);
    }

    private void actualizar(String mensaje) {
        scoreLabel.setText("Puntaje: " + score);
        statusLabel.setText(mensaje);
        nombre.setEditable(gameOver);
        nombre.setToolTipText(gameOver ? "Nombre del jugador, de 1 a 120 caracteres."
                : "El nombre se conserva hasta terminar o reiniciar la partida.");
        pausa.setEnabled(!gameOver);
        pausa.setText(paused ? "Reanudar · P" : "Pausar · P");
        pausa.setToolTipText(gameOver ? "Inicia una partida para poder pausarla." : "P: pausar o reanudar");
        for (JButton botón : controles) {
            botón.setEnabled(!gameOver && !paused);
            botón.setToolTipText(gameOver ? "Inicia una partida para moverte." : paused ? "Reanuda la partida para moverte." : botón.getText());
        }
        boardPanel.getAccessibleContext().setAccessibleDescription(mensaje + ". Puntaje: " + score);
        boardPanel.repaint();
    }

    @Override public Stat getStats() {
        return stats == null ? null : new Stat(stats.getClave(), stats.getNombre(), stats.getValor());
    }

    private class BoardPanel extends JPanel {
        BoardPanel() {
            setBackground(new Color(7, 12, 8));
            setOpaque(false);
            setFocusable(true);
            setPreferredSize(new Dimension(500, 400));
            getAccessibleContext().setAccessibleName("Tablero de Snake");
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); }
            });
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            int lado = Math.max(1, Math.min((getWidth() - 8) / COLUMNS, (getHeight() - 8) / ROWS));
            int x = (getWidth() - COLUMNS * lado) / 2, y = (getHeight() - ROWS * lado) / 2;
            g.setColor(new Color(7, 12, 8));
            g.fillRect(x, y, COLUMNS * lado, ROWS * lado);
            g.setColor(new Color(30, 54, 35));
            for (int c = 0; c <= COLUMNS; c++) g.drawLine(x + c * lado, y, x + c * lado, y + ROWS * lado);
            for (int f = 0; f <= ROWS; f++) g.drawLine(x, y + f * lado, x + COLUMNS * lado, y + f * lado);
            if (food != null) {
                g.setColor(Color.WHITE);
                g.fillOval(x + food.x * lado + 2, y + food.y * lado + 2, Math.max(1, lado - 4), Math.max(1, lado - 4));
            }
            boolean first = true;
            for (Point segment : snake) {
                g.setColor(first ? new Color(110, 245, 145) : new Color(28, 190, 94));
                g.fillRoundRect(x + segment.x * lado + 1, y + segment.y * lado + 1, lado - 2, lado - 2, 4, 4);
                if (first) {
                    g.setColor(new Color(7, 12, 8));
                    g.fillOval(x + segment.x * lado + lado / 2, y + segment.y * lado + lado / 3, Math.max(2, lado / 5), Math.max(2, lado / 5));
                }
                first = false;
            }
            if (paused || gameOver) {
                g.setColor(new Color(7, 12, 8, 200));
                g.fillRect(x, y, COLUMNS * lado, ROWS * lado);
                String mensaje = paused ? "En pausa" : stats == null ? "Listo para jugar" : "Partida terminada";
                g.setFont(RetroTypography.fuente(Font.BOLD, 22));
                g.setColor(Color.WHITE);
                g.drawString(mensaje, x + (COLUMNS * lado - g.getFontMetrics().stringWidth(mensaje)) / 2, y + ROWS * lado / 2);
            }
            g.setColor(hasFocus() ? new Color(181, 255, 201) : new Color(73, 96, 78));
            g.drawRect(x, y, COLUMNS * lado, ROWS * lado);
            g.dispose();
        }
    }
}
