package main;

import core.GameManager;
import snake.SnakeGame;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                System.err.println("No se pudo aplicar el estilo del sistema: " + exception.getMessage());
            }

            GameManager gameManager = new GameManager();
            gameManager.registrarJuego("Snake", SnakeGame::getInstance);

            MainFrame mainFrame = new MainFrame(gameManager);
            mainFrame.setVisible(true);
        });
    }
}
