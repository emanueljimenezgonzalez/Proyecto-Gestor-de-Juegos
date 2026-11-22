package ui;

import core.GameManager;
import core.Stat;
import core.PausableGame;
import records.RecordsController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Ventana principal con un panel de inicio y un área para los Internal Frames. */
public class MainFrame extends JFrame {
    private final GameManager gameManager;
    private final JDesktopPane desktop = new JDesktopPane();
    private final HomePanel homePanel;
    private GameMenuFrame gameMenuFrame;
    private RecordsFrame recordsFrame;
    private JInternalFrame juegoActual;
    private final JButton salirJuego = UiSupport.boton("Salir del juego");
    private final RecordsController recordsController;
    private final JPanel mejoresPanel = new JPanel(new GridLayout(0, 2, 18, 6));

    private static final Color NAVY = new Color(8, 12, 9);
    private static final Color PURPLE = new Color(28, 190, 94);
    private static final Color TEXT = new Color(248, 250, 248);
    private static final Color MUTED = new Color(183, 194, 185);

    public MainFrame(GameManager gameManager, RecordsController recordsController) {
        super("Arcadia | Gestor de Juegos");
        this.gameManager = gameManager;
        this.homePanel = new HomePanel(gameManager, this::abrirJuego, true);
        this.recordsController = recordsController;
        configurarVentana();
        crearInterfaz();
        mejoresPanel.setBackground(new Color(4, 7, 5));
        mejoresPanel.setBorder(new EmptyBorder(12, 28, 12, 28));
        add(mejoresPanel, BorderLayout.SOUTH);
        recordsController.getManager().agregarObservador(() -> SwingUtilities.invokeLater(this::actualizarMejores));
        actualizarMejores();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 640));
        setSize(1180, 780);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        brand.setFont(RetroTypography.fuente(Font.BOLD, 22));
        JLabel subtitle = new JLabel("GESTOR DE JUEGOS");
        subtitle.setForeground(new Color(155, 170, 158));
        subtitle.setFont(RetroTypography.fuente(Font.PLAIN, 10));
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
        home.getAccessibleContext().setAccessibleName("Inicio");
        games.getAccessibleContext().setAccessibleName("Jugar");
        records.getAccessibleContext().setAccessibleName("Récords");
        home.setToolTipText("Ctrl+1: ir al inicio");
        games.setToolTipText("Ctrl+2: abrir los juegos disponibles");
        records.setToolTipText("Ctrl+3: consultar los récords");
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ctrl 1", "inicio", this::mostrarInicio);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ctrl 2", "juegos", this::abrirMenuJuegos);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "ctrl 3", "récords", this::abrirRecords);
        home.addActionListener(e -> mostrarInicio());
        games.addActionListener(e -> abrirMenuJuegos());
        records.addActionListener(e -> abrirRecords());
        salirJuego.setVisible(false);
        salirJuego.setToolTipText("Volver al Inicio; la partida queda en pausa");
        salirJuego.addActionListener(e -> salirDelJuego());
        nav.add(home); nav.add(games); nav.add(records); nav.add(salirJuego);
        header.add(brandPanel, BorderLayout.WEST);
        header.add(nav, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        desktop.setBackground(NAVY);
        desktop.setBorder(null);



        desktop.add(homePanel);
        homePanel.setBounds(0, 0, 1180, 700);
        add(desktop, BorderLayout.CENTER);
        desktop.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if (homePanel.isVisible()) homePanel.setBounds(0, 0, desktop.getWidth(), desktop.getHeight());
            }
        });
    }

    private JButton navButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setBackground(primary ? new Color(25, 110, 55) : new Color(27, 39, 30));
        UiSupport.configurarBoton(button);
        return button;
    }
    private void abrirMenuJuegos() {
        if (!core.PlayerSession.activa()) { mostrarInicio(); return; }
        pausarPartidas();
        if (gameMenuFrame == null || gameMenuFrame.isClosed()) {
            gameMenuFrame = new GameMenuFrame(gameManager, this);
            desktop.add(gameMenuFrame);
        }
        homePanel.setVisible(false);
        mostrarVentana(gameMenuFrame);
    }

    public boolean abrirJuego(String nombre) {
        if (!core.PlayerSession.activa()) { mostrarInicio(); return false; }
        pausarPartidas();
        try {
            JInternalFrame juego = gameManager.abrirJuego(nombre, desktop);
            if (!Boolean.TRUE.equals(juego.getClientProperty("opcionesFinalConectadas"))) {
                juego.putClientProperty("opcionesFinalConectadas", Boolean.TRUE);
                juego.addPropertyChangeListener("volverInicio", evento -> mostrarInicio());
                juego.addPropertyChangeListener("resultadoPartida", evento -> {
                    Stat resultado = (Stat) evento.getNewValue();
                    SwingUtilities.invokeLater(() -> mostrarFinPartida(juego, resultado));
                });
            }
            juegoActual = juego;
            salirJuego.setVisible(true);
            try { juego.setMaximum(true); }
            catch (java.beans.PropertyVetoException ignored) { }
            homePanel.setVisible(false);
            return true;
        } catch (RuntimeException e) {
            System.err.println("No se pudo abrir " + nombre + ": " + e.getClass().getSimpleName());
            JOptionPane.showMessageDialog(this,
                    "No se pudo abrir el juego. Puedes intentar de nuevo o elegir otro.",
                    "Juego no disponible", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void abrirRecords() {
        pausarPartidas();
        if (recordsFrame == null || recordsFrame.isClosed()) {
            recordsFrame = new RecordsFrame(recordsController);
            desktop.add(recordsFrame);
        }
        homePanel.setVisible(false);
        recordsFrame.refrescar();
        mostrarVentana(recordsFrame);
    }

    private void actualizarMejores() {
        mejoresPanel.removeAll();
        recordsController.getManager().getJuegos().forEach((id, nombre) -> {
            Stat mejor = recordsController.getManager().obtenerMejorResultado(id);
            String texto = nombre + ": " + (mejor == null ? "sin récords"
                    : mejor.getValor() + " · " + mejor.getNombre());
            if (recordsController.getManager().getError(id) != null) {
                texto += recordsController.getManager().tieneGuardadoPendiente(id)
                        ? " (sin guardar)" : " (error de carga)";
            }
            JLabel indicador = new JLabel(texto);
            indicador.setForeground(TEXT);
            indicador.setToolTipText("Mejor resultado de " + nombre + (mejor == null ? "" : ": " + mejor.getClave()));
            mejoresPanel.add(indicador);
        });
        mejoresPanel.revalidate();
        mejoresPanel.repaint();
    }

    private void mostrarInicio() {
        salirJuego.setVisible(false);
        pausarPartidas();
        for (JInternalFrame frame : desktop.getAllFrames()) frame.setVisible(false);
        homePanel.setVisible(true);
        homePanel.setBounds(0, 0, desktop.getWidth(), desktop.getHeight());
        homePanel.repaint();
    }

    private void mostrarVentana(JInternalFrame frame) {
        salirJuego.setVisible(false);
        frame.setVisible(true);
        try { frame.setSelected(true); frame.toFront(); }
        catch (java.beans.PropertyVetoException ignored) { }
    }

    public JDesktopPane getDesktop() { return desktop; }

    private void mostrarFinPartida(JInternalFrame juego, Stat resultado) {
        if (resultado == null || juego.isClosed()) return;
        GameOverPanel opciones = new GameOverPanel(resultado,
                () -> { juego.getGlassPane().setVisible(false); mostrarInicio(); },
                () -> {
                    juego.getGlassPane().setVisible(false);
                    if (juego instanceof core.GameFunction) ((core.GameFunction) juego).iniciar();
                },
                () -> dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        juego.setGlassPane(opciones);
        opciones.setVisible(true);
        opciones.setFocusCycleRoot(true);
        opciones.setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        SwingUtilities.invokeLater(() -> opciones.transferFocusDownCycle());
    }

    private void salirDelJuego() {
        if (juegoActual == null) return;
        if (juegoActual instanceof PausableGame) {
            PausableGame partida = (PausableGame) juegoActual;
            partida.pausarPartida();
            if (partida.tienePartidaEnCurso() && !UiSupport.confirmarDescarte(this,
                    "Volverás al Inicio. La partida quedará en pausa y podrás continuar al abrir este juego.",
                    "Salir del juego")) return;
        }
        mostrarInicio();
        juegoActual = null;
    }

    public void pausarPartidas() {
        for (String nombre : gameManager.getJuegosDisponibles()) {
            JInternalFrame juego = gameManager.getInstancia(nombre);
            if (juego instanceof PausableGame) ((PausableGame) juego).pausarPartida();
        }
    }

    public boolean hayPartidasEnCurso() {
        for (String nombre : gameManager.getJuegosDisponibles()) {
            JInternalFrame juego = gameManager.getInstancia(nombre);
            if (juego instanceof PausableGame && ((PausableGame) juego).tienePartidaEnCurso()) return true;
        }
        return false;
    }

}
