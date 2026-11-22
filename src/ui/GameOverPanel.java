package ui;

import core.Stat;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Opciones compartidas al finalizar, dentro de la ventana del juego. */
public final class GameOverPanel extends JPanel {
    public GameOverPanel(Stat resultado, Runnable inicio, Runnable repetir, Runnable salir) {
        super(new GridBagLayout());
        setOpaque(false);
        JPanel tarjeta = new JPanel(new BorderLayout(0, 18));
        tarjeta.setBackground(new Color(9, 20, 13));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(77, 182, 108)), new EmptyBorder(24, 28, 24, 28)));
        JLabel título = new JLabel("Partida terminada", SwingConstants.CENTER);
        título.setFont(RetroTypography.fuente(Font.BOLD, 22));
        título.setForeground(Color.WHITE);
        JLabel puntaje = new JLabel("Puntaje: " + resultado.getValor(), SwingConstants.CENTER);
        puntaje.setFont(RetroTypography.fuente(Font.PLAIN, 16));
        puntaje.setForeground(new Color(183, 220, 193));
        JPanel texto = new JPanel(new GridLayout(0, 1, 0, 12));
        texto.setOpaque(false); texto.add(título); texto.add(puntaje);
        JPanel opciones = new JPanel(new GridLayout(0, 1, 0, 10));
        opciones.setOpaque(false);
        añadir(opciones, "Volver a jugar", repetir);
        añadir(opciones, "Volver al inicio", inicio);
        añadir(opciones, "Salir del gestor", salir);
        tarjeta.add(texto, BorderLayout.NORTH);
        tarjeta.add(opciones, BorderLayout.CENTER);
        add(tarjeta);
    }
    private static void añadir(JPanel panel, String texto, Runnable acción) {
        JButton botón = UiSupport.boton(texto);
        botón.addActionListener(e -> acción.run());
        panel.add(botón);
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(new Color(0, 0, 0, 185));
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }
}
