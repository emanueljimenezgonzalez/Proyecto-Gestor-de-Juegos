package snake;

import core.GameFunction;
import core.Stat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/** Juego Snake. Implementa GameFunction y Singleton. */
public class SnakeGame extends JInternalFrame implements GameFunction {
    private static final int CELL_SIZE = 20;
    private static final int COLUMNS = 25;
    private static final int ROWS = 20;
    private static final int INITIAL_DELAY = 135;

    private static SnakeGame instance;
    private final Deque<Point> snake = new ArrayDeque<>();
    private final Random random = new Random();
    private final BoardPanel boardPanel = new BoardPanel();
    private final JLabel scoreLabel = new JLabel("Puntaje: 0");
    private final JLabel statusLabel = new JLabel("Presione Iniciar para jugar", SwingConstants.CENTER);
    private final Timer timer;

    private Point food;
    private Direction direction = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;
    private String playerName = "Jugador";
    private int score;
    private boolean gameOver = true;
    private Stat stats = new Stat("Puntaje", "Jugador", 0);

    private enum Direction {
        UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
        final int dx;
        final int dy;
        Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
    }

    /** Constructor privado para garantizar una única instancia. */
    private SnakeGame() {
        super("Snake", true, true, true, true);
        setLayout(new BorderLayout(8, 8));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(new Dimension(COLUMNS * CELL_SIZE + 30, ROWS * CELL_SIZE + 115));

        JPanel topPanel = new JPanel(new BorderLayout());
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 15f));
        topPanel.add(scoreLabel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.CENTER);
        topPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        add(topPanel, BorderLayout.NORTH);

        boardPanel.setPreferredSize(new Dimension(COLUMNS * CELL_SIZE, ROWS * CELL_SIZE));
        add(boardPanel, BorderLayout.CENTER);

        JButton startButton = new JButton("Iniciar / Nueva partida");
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(25, 145, 70));
        startButton.setOpaque(true);
        startButton.setContentAreaFilled(true);
        startButton.setBorderPainted(false);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        startButton.addActionListener(event -> iniciar());
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(12, 20, 14));
        bottomPanel.add(startButton);
        add(bottomPanel, BorderLayout.SOUTH);

        timer = new Timer(INITIAL_DELAY, this::moveSnake);
        configurarTeclas();
        prepararTablero();
    }

    public static synchronized SnakeGame getInstance() {
        if (instance == null) {
            instance = new SnakeGame();
        }
        return instance;
    }

    @Override
    public void iniciar() {
        String enteredName = JOptionPane.showInputDialog(this, "Nombre del jugador:", playerName);
        if (enteredName == null) {
            return;
        }
        if (!enteredName.isBlank()) {
            playerName = enteredName.trim();
        }

        timer.stop();
        score = 0;
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        gameOver = false;
        prepararTablero();
        scoreLabel.setText("Puntaje: 0");
        statusLabel.setText("Use las flechas del teclado para moverse");
        boardPanel.requestFocusInWindow();
        timer.start();
        boardPanel.repaint();
    }

    private void prepararTablero() {
        snake.clear();
        int startX = COLUMNS / 2;
        int startY = ROWS / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));
        colocarComida();
        boardPanel.repaint();
    }

    private void colocarComida() {
        Point candidate;
        do {
            candidate = new Point(random.nextInt(COLUMNS), random.nextInt(ROWS));
        } while (snake.contains(candidate));
        food = candidate;
    }

    private void configurarTeclas() {
        boardPanel.setFocusable(true);
        boardPanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "up");
        boardPanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "down");
        boardPanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        boardPanel.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        boardPanel.getActionMap().put("up", new DirectionAction(Direction.UP));
        boardPanel.getActionMap().put("down", new DirectionAction(Direction.DOWN));
        boardPanel.getActionMap().put("left", new DirectionAction(Direction.LEFT));
        boardPanel.getActionMap().put("right", new DirectionAction(Direction.RIGHT));
    }

    private class DirectionAction extends javax.swing.AbstractAction {
        private final Direction requested;
        DirectionAction(Direction requested) { this.requested = requested; }
        @Override
        public void actionPerformed(ActionEvent event) {
            if (!gameOver && !(requested.dx == -direction.dx && requested.dy == -direction.dy)) {
                nextDirection = requested;
            }
        }
    }

    private void moveSnake(ActionEvent event) {
        direction = nextDirection;
        Point head = snake.peekFirst();
        Point newHead = new Point(head.x + direction.dx, head.y + direction.dy);

        boolean eating = newHead.equals(food);
        Point tail = snake.peekLast();
        boolean hitsBody = snake.contains(newHead) && !(newHead.equals(tail) && !eating);
        if (newHead.x < 0 || newHead.x >= COLUMNS || newHead.y < 0 || newHead.y >= ROWS || hitsBody) {
            terminarPartida();
            return;
        }

        snake.addFirst(newHead);
        if (eating) {
            score += 10;
            scoreLabel.setText("Puntaje: " + score);
            colocarComida();
        } else {
            snake.removeLast();
        }
        boardPanel.repaint();
    }

    private void terminarPartida() {
        timer.stop();
        gameOver = true;
        stats = new Stat("Puntaje", playerName, score);
        statusLabel.setText("Partida terminada. Puntaje: " + score);
        boardPanel.repaint();
        JOptionPane.showMessageDialog(this,
                "Jugador: " + playerName + "\nPuntaje obtenido: " + score,
                "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public Stat getStats() {
        return stats;
    }

    private class BoardPanel extends JPanel {
        BoardPanel() {
            setBackground(new Color(7, 12, 8));
            setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            graphics.setColor(new Color(30, 54, 35));
            for (int x = 0; x <= COLUMNS; x++) {
                graphics.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, ROWS * CELL_SIZE);
            }
            for (int y = 0; y <= ROWS; y++) {
                graphics.drawLine(0, y * CELL_SIZE, COLUMNS * CELL_SIZE, y * CELL_SIZE);
            }

            if (food != null) {
                graphics.setColor(new Color(248, 250, 248));
                graphics.fillOval(food.x * CELL_SIZE + 3, food.y * CELL_SIZE + 3, CELL_SIZE - 6, CELL_SIZE - 6);
            }

            boolean first = true;
            for (Point segment : snake) {
                graphics.setColor(first ? new Color(110, 245, 145) : new Color(28, 190, 94));
                graphics.fillRoundRect(segment.x * CELL_SIZE + 1, segment.y * CELL_SIZE + 1,
                        CELL_SIZE - 2, CELL_SIZE - 2, 6, 6);
                first = false;
            }
        }
    }
}
