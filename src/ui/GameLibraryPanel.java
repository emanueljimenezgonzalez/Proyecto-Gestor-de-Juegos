package ui;

import core.GameManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


import java.util.Objects;
import java.util.function.Consumer;

/** Selector compartido por Inicio y Jugar. Cada opción procede del catálogo real. */
public final class GameLibraryPanel extends JPanel implements Scrollable {
    private final GameManager manager;
    private final Consumer<String> abrirJuego;
    private final Runnable observador = () -> SwingUtilities.invokeLater(this::actualizar);
    private boolean observando;
    private boolean activo = true;
    private int columnas = 2;

    public GameLibraryPanel(GameManager manager, Consumer<String> abrirJuego) {
        this.manager = Objects.requireNonNull(manager);
        this.abrirJuego = Objects.requireNonNull(abrirJuego);
        setOpaque(false);
        observar();
        actualizar();
    }

    public void actualizar() {
        if (!activo) return;
        removeAll();
        if (manager.getJuegosDisponibles().isEmpty()) {
            setLayout(new BorderLayout());
            JLabel vacío = new JLabel("Todavía no hay juegos disponibles.");
            vacío.setForeground(new Color(183, 194, 185));
            vacío.setBorder(new EmptyBorder(20, 0, 20, 0));
            add(vacío, BorderLayout.NORTH);
        } else {
            setLayout(new GridLayout(0, columnas, 18, 18));
            for (String nombre : manager.getJuegosDisponibles()) add(crearOpción(nombre));
        }
        revalidate();
        repaint();
    }

    private JPanel crearOpción(String nombre) {
        JPanel opción = new JPanel(new BorderLayout(14, 16));
        opción.setBackground(new Color(20, 31, 23));
        opción.setBorder(new EmptyBorder(22, 22, 22, 22));
        opción.setName("juego:" + nombre);
        JPanel texto = new JPanel(new BorderLayout(0, 10));
        texto.setOpaque(false);
        JLabel título = new JLabel(nombre);
        título.setFont(RetroTypography.fuente(Font.BOLD, 26));
        título.setForeground(Color.WHITE);
        título.setToolTipText(nombre);
        título.putClientProperty("html.disable", Boolean.TRUE);
        texto.add(título, BorderLayout.NORTH);
        String descripción = manager.getDescripción(nombre);
        if (!descripción.isEmpty()) {
            JTextArea detalle = new JTextArea(descripción);
            detalle.setFont(RetroTypography.fuente(Font.PLAIN, 14));
            detalle.setForeground(new Color(183, 194, 185));
            detalle.setOpaque(false);
            detalle.setEditable(false);
            detalle.setFocusable(false);
            detalle.setLineWrap(true);
            detalle.setWrapStyleWord(true);
            texto.add(detalle, BorderLayout.CENTER);
        }
        JLabel icono = new JLabel(new GameIcon(nombre));
        opción.add(icono, BorderLayout.WEST);
        opción.add(texto, BorderLayout.CENTER);
        JButton jugar = UiSupport.boton("Jugar");
        jugar.getAccessibleContext().setAccessibleName("Jugar " + nombre);
        jugar.setToolTipText("Abrir " + nombre);
        jugar.addActionListener(e -> abrirJuego.accept(nombre));
        opción.add(jugar, BorderLayout.SOUTH);
        return opción;
    }

    @Override public void doLayout() {
        int nuevasColumnas = getWidth() > 0 && getWidth() < 660 ? 1 : 2;
        if (columnas != nuevasColumnas) {
            columnas = nuevasColumnas;
            if (getLayout() instanceof GridLayout) ((GridLayout) getLayout()).setColumns(columnas);
            revalidate();
        }
        super.doLayout();
    }

    @Override public Dimension getPreferredSize() {
        int cantidad = manager.getJuegosDisponibles().size();
        int filas = Math.max(1, (cantidad + columnas - 1) / columnas);
        return new Dimension(780, cantidad == 0 ? 80 : filas * 192 + (filas - 1) * 18);
    }

    private void observar() {
        if (!observando) { manager.agregarObservador(observador); observando = true; }
    }

    @Override public void addNotify() {
        super.addNotify();
        activo = true;
        observar();
        actualizar();
    }

    @Override public void removeNotify() {
        desconectar();
        super.removeNotify();
    }

    public void desconectar() {
        activo = false;
        if (observando) { manager.quitarObservador(observador); observando = false; }
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int orientación, int dirección) { return 24; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int orientación, int dirección) { return Math.max(24, r.height - 24); }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }

    private static final class GameIcon implements Icon {
        private final String nombre;
        GameIcon(String nombre) { this.nombre = nombre; }
        @Override public int getIconWidth() { return 48; }
        @Override public int getIconHeight() { return 48; }
        @Override public void paintIcon(Component componente, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (nombre.equalsIgnoreCase("Tetris")) {
                g.setColor(new Color(179, 124, 235));
                g.fillRect(17, 9, 12, 12);
                for (int i = 0; i < 3; i++) g.fillRect(3 + i * 14, 23, 12, 12);
            } else if (nombre.equalsIgnoreCase("Snake")) {
                g.setColor(new Color(91, 235, 139));
                g.fillRoundRect(3, 9, 14, 28, 5, 5);
                g.fillRoundRect(3, 25, 28, 12, 5, 5);
                g.fillRoundRect(19, 9, 26, 14, 5, 5);
                g.fillRect(19, 17, 12, 16);
                g.setColor(new Color(7, 12, 8));
                g.fillOval(36, 12, 4, 4);
            } else {
                g.setColor(new Color(91, 235, 139));
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(3, 12, 42, 26, 12, 12);
                g.drawLine(11, 25, 23, 25);
                g.drawLine(17, 19, 17, 31);
                g.fillOval(31, 19, 4, 4);
                g.fillOval(36, 27, 4, 4);
            }
            g.dispose();
        }
    }
}
