package ui;

import core.GameManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Ventana interna con tarjetas para los juegos registrados. */
public class GameMenuFrame extends JInternalFrame {
    private static final Color BACKGROUND = new Color(9, 14, 10);
    private static final Color PURPLE = new Color(28, 190, 94);

    public GameMenuFrame(GameManager gameManager, MainFrame mainFrame) {
        super("Biblioteca de juegos", true, true, true, true);
        setSize(650, 430);
        setLocation(70, 55);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        JPanel content = new JPanel();
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(25, 28, 25, 28));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Biblioteca de juegos");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        JLabel subtitle = new JLabel("Selecciona un título para comenzar a jugar.");
        subtitle.setForeground(new Color(184, 195, 185));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));

        JPanel cards = new JPanel(new GridLayout(0, 2, 14, 14));
        cards.setOpaque(false);
        for (String gameName : gameManager.getJuegosDisponibles()) {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBackground(new Color(22, 34, 25));
            card.setBorder(new EmptyBorder(18, 18, 18, 18));
            JLabel icon = new JLabel(gameName.equalsIgnoreCase("Snake") ? "🐍" : "🎮");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 35));
            JLabel name = new JLabel(gameName);
            name.setForeground(Color.WHITE);
            name.setFont(new Font("SansSerif", Font.BOLD, 19));
            JLabel state = new JLabel("Disponible");
            state.setForeground(new Color(91, 235, 139));
            state.setFont(new Font("SansSerif", Font.PLAIN, 11));
            JButton play = new JButton("Jugar  →");
            play.setForeground(Color.WHITE);
            play.setBackground(PURPLE);
            play.setFocusPainted(false);
            play.setBorder(new EmptyBorder(9, 12, 9, 12));
            play.addActionListener(event -> {
                gameManager.abrirJuego(gameName, mainFrame.getDesktop());
                setVisible(false);
            });
            JPanel labels = new JPanel();
            labels.setOpaque(false);
            labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
            labels.add(name); labels.add(Box.createVerticalStrut(3)); labels.add(state);
            card.add(icon, BorderLayout.WEST);
            card.add(labels, BorderLayout.CENTER);
            card.add(play, BorderLayout.SOUTH);
            cards.add(card);
        }
        content.add(cards);
        add(content, BorderLayout.CENTER);
    }
}
