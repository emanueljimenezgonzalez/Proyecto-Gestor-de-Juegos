package ui;

import core.GameManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/** Inicio de Arcadia: conserva la identidad visual y muestra el catálogo disponible. */
public final class HomePanel extends JPanel {
    public HomePanel(GameManager manager, Consumer<String> abrirJuego) {
        this(manager, abrirJuego, false);
    }

    public HomePanel(GameManager manager, Consumer<String> abrirJuego, boolean solicitarNombre) {
        setLayout(new BorderLayout(0, 28));
        setBorder(new EmptyBorder(36, 45, 36, 45));
        JPanel títulos = new JPanel();
        títulos.setOpaque(false);
        títulos.setLayout(new BoxLayout(títulos, BoxLayout.Y_AXIS));
        JLabel título = new JLabel("¿A qué vamos a jugar?");
        título.setForeground(new Color(248, 250, 248));
        título.setFont(RetroTypography.fuente(Font.BOLD, 38));
        título.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel descripción = new JLabel("Elige un juego, supera tu mejor marca y disfruta la partida.");
        descripción.setForeground(new Color(183, 194, 185));
        descripción.setFont(RetroTypography.fuente(Font.PLAIN, 15));
        descripción.setAlignmentX(Component.LEFT_ALIGNMENT);
        títulos.add(título);
        títulos.add(Box.createVerticalStrut(10));
        títulos.add(descripción);
        add(títulos, BorderLayout.NORTH);

        JScrollPane opciones = new JScrollPane(new GameLibraryPanel(manager, abrirJuego));
        opciones.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        opciones.setBorder(null);
        opciones.setOpaque(false);
        opciones.getViewport().setOpaque(false);
        opciones.getVerticalScrollBar().setUnitIncrement(24);
        add(opciones, BorderLayout.CENTER);
        if (solicitarNombre && !core.PlayerSession.activa()) {
            remove(opciones);
            título.setText("Bienvenido a Arcadia");
            descripción.setVisible(false);
            JPanel formulario = new JPanel(new GridBagLayout());
            formulario.setOpaque(false);
            JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
            tarjeta.setBackground(new Color(9, 20, 13));
            tarjeta.setBorder(new EmptyBorder(24, 24, 24, 24));
            tarjeta.setPreferredSize(new Dimension(430, 210));
            JLabel etiqueta = new JLabel("Tu nombre");
            etiqueta.setForeground(Color.WHITE);
            etiqueta.setFont(RetroTypography.fuente(Font.BOLD, 18));
            JTextField nombre = new JTextField(18);
            nombre.setName("nombreSesion");
            etiqueta.setLabelFor(nombre);
            nombre.getAccessibleContext().setAccessibleName("Tu nombre");
            JLabel error = new JLabel(" ");
            UiSupport.validarNombreAlEditar(nombre, error);
            JButton entrar = UiSupport.boton("Entrar");
            entrar.setName("entrarSesion");
            Runnable ingresar = () -> {
                try { core.PlayerSession.iniciar(nombre.getText()); }
                catch (IllegalArgumentException e) { UiSupport.errorNombre(nombre, error, e.getMessage()); return; }
                remove(formulario);
                título.setText("¿A qué vamos a jugar?");
                descripción.setText("Jugador: " + core.PlayerSession.getNombre());
                descripción.setVisible(true);
                add(opciones, BorderLayout.CENTER);
                revalidate(); repaint();
            };
            entrar.addActionListener(e -> ingresar.run());
            nombre.addActionListener(e -> ingresar.run());
            JPanel campos = new JPanel(new BorderLayout(0, 5));
            campos.setOpaque(false);
            campos.add(nombre, BorderLayout.NORTH);
            campos.add(error, BorderLayout.CENTER);
            tarjeta.add(etiqueta, BorderLayout.NORTH);
            tarjeta.add(campos, BorderLayout.CENTER);
            tarjeta.add(entrar, BorderLayout.SOUTH);
            formulario.add(tarjeta);
            add(formulario, BorderLayout.CENTER);
            SwingUtilities.invokeLater(nombre::requestFocusInWindow);
        }
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setPaint(new GradientPaint(0, 0, new Color(7, 13, 9), getWidth(), getHeight(), new Color(16, 35, 21)));
        g.fillRect(0, 0, getWidth(), getHeight());
        GameBackground.pintar(g, getWidth(), getHeight(), "inicio");
        g.dispose();
    }
}
