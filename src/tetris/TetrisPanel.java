package tetris;

import javax.swing.JPanel;
import ui.RetroTypography;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Dibuja el tablero; no altera las reglas ni registra estadísticas. */
public final class TetrisPanel extends JPanel {
    private final TetrisBoard tablero;

    public TetrisPanel(TetrisBoard tablero) {
        this.tablero = tablero;
        setBackground(new Color(7, 12, 8));
        setOpaque(false);
        setFocusable(true);
        setPreferredSize(new Dimension(260, 500));
        getAccessibleContext().setAccessibleName("Tablero de Tetris");
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { repaint(); }
            @Override public void focusLost(FocusEvent e) { repaint(); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); }
        });
    }

    public static Color color(Tetromino pieza) {
        switch (pieza) {
            case I: return new Color(72, 206, 220);
            case O: return new Color(244, 205, 74);
            case T: return new Color(179, 124, 235);
            case S: return new Color(87, 210, 121);
            case Z: return new Color(238, 106, 117);
            case J: return new Color(100, 149, 244);
            case L: return new Color(244, 163, 81);
            default: throw new IllegalArgumentException("Pieza desconocida.");
        }
    }

    static void bloque(Graphics2D g, Tetromino pieza, int x, int y, int lado) {
        g.setColor(color(pieza));
        g.fillRect(x + 1, y + 1, lado - 2, lado - 2);
        g.setColor(color(pieza).brighter());
        g.drawLine(x + 2, y + 2, x + lado - 3, y + 2);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int lado = Math.max(1, Math.min((getWidth() - 12) / TetrisBoard.COLUMNAS,
                (getHeight() - 12) / TetrisBoard.FILAS));
        int ancho = lado * TetrisBoard.COLUMNAS, alto = lado * TetrisBoard.FILAS;
        int x = (getWidth() - ancho) / 2, y = (getHeight() - alto) / 2;
        g.setColor(new Color(7, 12, 8));
        g.fillRect(x, y, ancho, alto);
        g.setColor(new Color(33, 51, 38));
        for (int c = 0; c <= TetrisBoard.COLUMNAS; c++) g.drawLine(x + c * lado, y, x + c * lado, y + alto);
        for (int f = 0; f <= TetrisBoard.FILAS; f++) g.drawLine(x, y + f * lado, x + ancho, y + f * lado);
        for (int f = 0; f < TetrisBoard.FILAS; f++)
            for (int c = 0; c < TetrisBoard.COLUMNAS; c++)
                if (tablero.getCelda(f, c) != null) bloque(g, tablero.getCelda(f, c), x + c * lado, y + f * lado, lado);
        Tetromino activa = tablero.getActiva();
        if (activa != null && tablero.getEstado() != TetrisBoard.Estado.TERMINADO) {
            for (int f = 0; f < activa.getTamaño(); f++)
                for (int c = 0; c < activa.getTamaño(); c++)
                    if (activa.ocupa(tablero.getGiro(), f, c))
                        bloque(g, activa, x + (tablero.getColumna() + c) * lado,
                                y + (tablero.getFila() + f) * lado, lado);
        }
        String mensaje = null;
        switch (tablero.getEstado()) {
            case LISTO: mensaje = "Listo para jugar"; break;
            case PAUSADO: mensaje = "En pausa"; break;
            case TERMINADO: mensaje = "Fin de partida"; break;
            default: break;
        }
        if (mensaje != null) {
            g.setColor(new Color(7, 12, 8, 210));
            g.fillRect(x, y, ancho, alto);
            g.setColor(Color.WHITE);
            g.setFont(RetroTypography.fuente(Font.BOLD, Math.min(22, Math.max(14, ancho / 11))));
            int texto = g.getFontMetrics().stringWidth(mensaje);
            g.drawString(mensaje, x + (ancho - texto) / 2, y + alto / 2);
        }
        g.setColor(hasFocus() ? new Color(110, 245, 145) : new Color(73, 96, 78));
        g.drawRect(x - 1, y - 1, ancho + 2, alto + 2);
        g.dispose();
    }
}
