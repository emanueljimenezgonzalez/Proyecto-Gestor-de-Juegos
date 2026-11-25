package main;

import core.GameManager;
import snake.SnakeGame;
import tetris.TetrisGame;
import tresenraya.TicTacToeGame;
import ui.MainFrame;
import ui.UiSupport;
import core.Stat;
import records.FileManager;
import records.RecordsManager;
import records.RecordsController;
import java.nio.file.Path;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // La ruta es configurable; por defecto pertenece al directorio de ejecución.
        Path ruta = Path.of(System.getProperty("arcadia.records.dir", "records"));
        RecordsManager records = new RecordsManager(new FileManager(ruta));
        records.registrarJuego("snake", "Snake");
        records.registrarJuego("tetris", "Tetris");
        records.registrarJuego("tres_en_raya", "Tres en raya");
        records.registrarJuego("externo", "Juego externo");
        records.cargarTodos();
        RecordsController controller = new RecordsController(records);
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                System.err.println("No se pudo aplicar el estilo del sistema: " + exception.getMessage());
            }

            ui.RetroTypography.instalar();
            GameManager gameManager = new GameManager();
            gameManager.registrarJuego("Snake", "Come, crece y evita chocar contigo mismo.", SnakeGame::getInstance);
            gameManager.registrarJuego("Tetris", "Gira las piezas y completa líneas para sumar puntos.", TetrisGame::getInstance);
            gameManager.registrarJuego("Tres en raya", "Dos jugadores se turnan para alinear tres marcas.", TicTacToeGame::getInstance);

            MainFrame mainFrame = new MainFrame(gameManager, controller);
            gameManager.registrarJuego("Snake", "Come, crece y evita chocar contigo mismo.", () -> {
                SnakeGame juego = SnakeGame.getInstance();
                conectarResultado(juego, "snake", controller, mainFrame); return juego;
            });
            gameManager.registrarJuego("Tetris", "Gira las piezas y completa líneas para sumar puntos.", () -> {
                TetrisGame juego = TetrisGame.getInstance();
                conectarResultado(juego, "tetris", controller, mainFrame); return juego;
            });
            gameManager.registrarJuego("Tres en raya", "Dos jugadores se turnan para alinear tres marcas.", () -> {
            TicTacToeGame juego = TicTacToeGame.getInstance();
            conectarResultado(juego, "tres_en_raya", controller, mainFrame); return juego;
            });
            // Listener único: una partida termina, se registra una vez y se intenta guardar.


            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override public void windowDeactivated(WindowEvent evento) { mainFrame.pausarPartidas(); }
                @Override public void windowIconified(WindowEvent evento) { mainFrame.pausarPartidas(); }
                @Override public void windowClosing(WindowEvent evento) {
                    mainFrame.pausarPartidas();
                    if (controller.estaOcupado()) {
                        JOptionPane.showMessageDialog(mainFrame, "Se están procesando los récords. Espera y vuelve a cerrar.");
                        return;
                    }
                    if (records.hayGuardadosPendientes()) {
                        if (!UiSupport.confirmarDescarte(mainFrame,
                                "Hay récords sin guardar que se perderán al salir. También se descartarán las partidas sin terminar.",
                                "Salir sin guardar")) return;
                    } else if (mainFrame.hayPartidasEnCurso() && !UiSupport.confirmarDescarte(mainFrame,
                            "Hay partidas sin terminar. Si sales, se descartarán sin registrar un resultado.", "Salir")) return;
                    controller.close();
                    mainFrame.dispose();
                    System.exit(0);
                }
            });
            mainFrame.setVisible(true);
            if (records.getJuegos().keySet().stream().anyMatch(id -> records.getError(id) != null)) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Algunos récords no pudieron cargarse. Los archivos originales se conservaron.\nAbre Récords para revisar el juego afectado.",
                        "Récords no disponibles", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private static void conectarResultado(JInternalFrame juego, String juegoId,
            RecordsController controller, MainFrame ventana) {
        juego.addPropertyChangeListener("resultadoPartida", evento ->
                controller.registrarResultado(juegoId, (Stat) evento.getNewValue(), error -> {
                    if (error == null) return;
                    String mensaje;
                    if (error instanceof IllegalArgumentException) mensaje = error.getMessage();
                    else if (controller.getManager().tieneGuardadoPendiente(juegoId))
                        mensaje = "El resultado está en memoria, pero no se pudo guardar.\nAbre Récords y pulsa Reintentar guardado.";
                    else mensaje = "No se pudo registrar el resultado. El archivo anterior se conserva.\nRevisa los récords de " + juego.getTitle() + " antes de volver a jugar.";
                    JOptionPane.showMessageDialog(ventana, mensaje, "No se guardó el resultado", JOptionPane.WARNING_MESSAGE);
                }));
    }
}
