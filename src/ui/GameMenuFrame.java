package ui;

import core.GameManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Biblioteca con el mismo catálogo y selector que la pantalla de inicio. */
public class GameMenuFrame extends JInternalFrame {
    public GameMenuFrame(GameManager gameManager, MainFrame mainFrame) {
        super("Biblioteca de juegos", true, true, true, true);
        setSize(790, 450);
        setLocation(45, 35);
        JPanel contenido = new GameBackground("inicio", new BorderLayout(0, 24));
        contenido.setBackground(new Color(9, 14, 10));
        contenido.setBorder(new EmptyBorder(25, 28, 25, 28));
        setContentPane(contenido);
        JLabel título = new JLabel("Biblioteca de juegos");
        título.setForeground(Color.WHITE);
        título.setFont(RetroTypography.fuente(Font.BOLD, 25));
        contenido.add(título, BorderLayout.NORTH);
        GameLibraryPanel biblioteca = new GameLibraryPanel(gameManager, nombre -> {
            if (mainFrame.abrirJuego(nombre)) setVisible(false);
        });
        JScrollPane opciones = new JScrollPane(biblioteca);
        opciones.setBorder(null);
        opciones.setOpaque(false);
        opciones.getViewport().setOpaque(false);
        opciones.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        opciones.getVerticalScrollBar().setUnitIncrement(24);
        contenido.add(opciones, BorderLayout.CENTER);
    }
}
