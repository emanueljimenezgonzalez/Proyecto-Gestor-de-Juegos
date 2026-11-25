package reflejos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

// Dibuja los iconitos del juego con Graphics2D. Los hacemos nosotros, en vez de
// usar emojis, para que se vean igual en cualquier maquina (los emojis de color
// en swing a veces no renderizan). Son metodos estaticos porque no necesitan
// guardar estado, solo devuelven el dibujo.
public class DibujoIconos {

    // Dibuja un rayo amarillo (la señal buena, "reacciona ya").
    public static ImageIcon crearRayo(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // los puntos del zigzag, calculados en proporcion al tamaño
        int[] px = { (int) (s * 0.55), (int) (s * 0.30), (int) (s * 0.48),
                     (int) (s * 0.40), (int) (s * 0.72), (int) (s * 0.50) };
        int[] py = { (int) (s * 0.08), (int) (s * 0.55), (int) (s * 0.55),
                     (int) (s * 0.92), (int) (s * 0.40), (int) (s * 0.40) };

        g.setColor(new Color(255, 225, 55));   // amarillo para que resalte en el verde
        g.fillPolygon(px, py, 6);
        g.setColor(new Color(120, 90, 0));      // un bordecito para que se defina
        g.setStroke(new BasicStroke(1.5f));
        g.drawPolygon(px, py, 6);

        g.dispose();
        return new ImageIcon(img);
    }

    // Dibuja una araña negra (la señal trampa, "no toques").
    public static ImageIcon crearArana(int s) {
        BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(20, 20, 20));
        g.setStroke(new BasicStroke(2.2f));

        int cx = s / 2;
        int cy = s / 2 + 4;
        int abdomen = s / 3;        // la parte de atras, mas gorda
        int cabeza = s / 6;

        // las 8 patas, 4 a cada lado, con una curvita para que no sean rectas
        for (int i = 0; i < 4; i++) {
            int yy = cy - 6 + i * 6;
            int codoY = yy - 8 + i * 4;
            // izquierda
            g.drawLine(cx - abdomen / 3, yy, cx - abdomen, codoY);
            g.drawLine(cx - abdomen, codoY, cx - abdomen - 8, codoY + 8);
            // derecha
            g.drawLine(cx + abdomen / 3, yy, cx + abdomen, codoY);
            g.drawLine(cx + abdomen, codoY, cx + abdomen + 8, codoY + 8);
        }

        // el cuerpo: abdomen abajo y cabeza arriba
        g.fillOval(cx - abdomen / 2, cy - abdomen / 3, abdomen, abdomen);
        g.fillOval(cx - cabeza / 2, cy - abdomen / 3 - cabeza + 2, cabeza, cabeza);

        // dos ojitos blancos para que se note que es la cabeza
        g.setColor(Color.WHITE);
        int ojo = Math.max(2, s / 24);
        g.fillOval(cx - cabeza / 4 - ojo / 2, cy - abdomen / 3 - cabeza / 2, ojo, ojo);
        g.fillOval(cx + cabeza / 4 - ojo / 2, cy - abdomen / 3 - cabeza / 2, ojo, ojo);

        g.dispose();
        return new ImageIcon(img);
    }
}
