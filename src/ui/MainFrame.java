package ui;

import core.GameManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Ventana principal con un panel de inicio y un área para los Internal Frames. */
public class MainFrame extends JFrame {
    private final GameManager gameManager;
    private final JDesktopPane desktop = new JDesktopPane();
    private final JPanel homePanel = new GradientPanel();
    private GameMenuFrame gameMenuFrame;
    private RecordsPlaceholderFrame recordsFrame;

    private static final Color NAVY = new Color(8, 12, 9);
    private static final Color PURPLE = new Color(28, 190, 94);
    private static final Color TEXT = new Color(248, 250, 248);
    private static final Color MUTED = new Color(183, 194, 185);

    public MainFrame(GameManager gameManager) {
        super("Arcadia | Gestor de Juegos");
        this.gameManager = gameManager;
        configurarVentana();
        crearInterfaz();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        setSize(1180, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(NAVY);
    }

    private void crearInterfaz() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(4, 7, 5));
        header.setBorder(new EmptyBorder(17, 28, 17, 28));

        JLabel brand = new JLabel("◈  ARCADIA");
        brand.setForeground(TEXT);
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitle = new JLabel("GESTOR DE JUEGOS");
        subtitle.setForeground(new Color(155, 170, 158));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.add(brand);
        brandPanel.add(subtitle);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        nav.setOpaque(false);
        JButton home = navButton("Inicio", false);
        JButton games = navButton("▶  Jugar", true);
        JButton records = navButton("♛  Récords", false);
        home.addActionListener(e -> mostrarInicio());
        games.addActionListener(e -> abrirMenuJuegos());
        records.addActionListener(e -> abrirRecords());
        nav.add(home); nav.add(games); nav.add(records);
        header.add(brandPanel, BorderLayout.WEST);
        header.add(nav, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        desktop.setBackground(NAVY);
        desktop.setBorder(null);
        homePanel.setLayout(new GridBagLayout());
        homePanel.setBorder(new EmptyBorder(45, 55, 45, 55));
        construirInicio();
        desktop.add(homePanel);
        homePanel.setBounds(0, 0, 1180, 700);
        add(desktop, BorderLayout.CENTER);
        desktop.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (homePanel.isVisible()) homePanel.setBounds(0, 0, desktop.getWidth(), desktop.getHeight());
            }
        });
    }

    private void construirInicio() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("TU CENTRO DE ENTRETENIMIENTO");
        eyebrow.setForeground(new Color(75, 230, 130));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 12));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("¿A qué vamos a jugar?");
        title.setForeground(TEXT);
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel description = new JLabel("Elige un juego, supera tu mejor marca y disfruta la partida.");
        description.setForeground(MUTED);
        description.setFont(new Font("SansSerif", Font.PLAIN, 15));
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel feature = new JPanel(new BorderLayout(22, 12));
        feature.setBackground(new Color(20, 31, 23));
        feature.setBorder(new EmptyBorder(24, 26, 24, 26));
        feature.setMaximumSize(new Dimension(820, 230));
        feature.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel featureText = new JPanel();
        featureText.setOpaque(false);
        featureText.setLayout(new BoxLayout(featureText, BoxLayout.Y_AXIS));
        JLabel tag = new JLabel("JUEGO DISPONIBLE");
        tag.setForeground(new Color(91, 235, 139));
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        JLabel gameTitle = new JLabel("Snake");
        gameTitle.setForeground(Color.WHITE);
        gameTitle.setFont(new Font("SansSerif", Font.BOLD, 30));
        JLabel gameDescription = new JLabel("Come, crece y evita chocar contigo mismo.");
        gameDescription.setForeground(MUTED);
        gameDescription.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JButton play = navButton("Jugar ahora  →", true);
        play.setAlignmentX(Component.LEFT_ALIGNMENT);
        play.addActionListener(e -> abrirJuego("Snake"));
        featureText.add(tag); featureText.add(Box.createVerticalStrut(9));
        featureText.add(gameTitle); featureText.add(Box.createVerticalStrut(5));
        featureText.add(gameDescription); featureText.add(Box.createVerticalStrut(18));
        featureText.add(play);

        JLabel icon = new JLabel("🐍", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 78));
        icon.setForeground(Color.WHITE);
        icon.setPreferredSize(new Dimension(170, 140));
        feature.add(featureText, BorderLayout.CENTER);
        feature.add(icon, BorderLayout.EAST);

        JLabel coming = new JLabel("MÁS JUEGOS SE INCORPORARÁN AL PROYECTO");
        coming.setForeground(new Color(145, 160, 148));
        coming.setFont(new Font("SansSerif", Font.BOLD, 10));
        coming.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(eyebrow);
        content.add(Box.createVerticalStrut(12));
        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(description);
        content.add(Box.createVerticalStrut(34));
        content.add(feature);
        content.add(Box.createVerticalStrut(24));
        content.add(coming);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        homePanel.add(content, gbc);
    }

    private JButton navButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(primary ? PURPLE : new Color(27, 39, 30));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(11, 17, 11, 17));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void abrirMenuJuegos() {
        if (gameMenuFrame == null || gameMenuFrame.isClosed()) {
            gameMenuFrame = new GameMenuFrame(gameManager, this);
            desktop.add(gameMenuFrame);
        }
        homePanel.setVisible(false);
        mostrarVentana(gameMenuFrame);
    }

    private void abrirJuego(String nombre) {
        homePanel.setVisible(false);
        gameManager.abrirJuego(nombre, desktop);
    }

    private void abrirRecords() {
        if (recordsFrame == null || recordsFrame.isClosed()) {
            recordsFrame = new RecordsPlaceholderFrame();
            desktop.add(recordsFrame);
        }
        homePanel.setVisible(false);
        mostrarVentana(recordsFrame);
    }

    private void mostrarInicio() {
        for (JInternalFrame frame : desktop.getAllFrames()) frame.setVisible(false);
        homePanel.setVisible(true);
        homePanel.setBounds(0, 0, desktop.getWidth(), desktop.getHeight());
        homePanel.repaint();
    }

    private void mostrarVentana(JInternalFrame frame) {
        frame.setVisible(true);
        try { frame.setSelected(true); frame.toFront(); }
        catch (java.beans.PropertyVetoException ignored) { }
    }

    public JDesktopPane getDesktop() { return desktop; }

    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, new Color(7, 13, 9), getWidth(), getHeight(), new Color(16, 35, 21)));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(35, 220, 105, 24));
            g2.fillOval(getWidth() - 310, -160, 430, 430);
            g2.dispose();
        }
    }
}
