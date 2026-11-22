package ui;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.HashMap;

/** Fondos compartidos: carga una vez y recorta sin deformar las ilustraciones. */
public final class GameBackground extends JPanel {
    private static final Map<String, BufferedImage> imágenes = new HashMap<>();
    private final String tema;
    public GameBackground(String tema, LayoutManager diseño) {
        super(diseño);
        this.tema = tema;
        setBackground(new Color(9, 14, 10));
    }
    private static synchronized BufferedImage cargar(String tema) {
        if (imágenes.containsKey(tema)) return imágenes.get(tema);
        BufferedImage imagen = null;
        String recurso = "assets/backgrounds/" + tema + ".png";
        try (InputStream entrada = GameBackground.class.getClassLoader().getResourceAsStream(recurso)) {
            if (entrada != null) imagen = ImageIO.read(entrada);
            else {
                // Admite también javac/IDE ejecutados desde la raíz del proyecto.
                Path archivo = Path.of("src").resolve(recurso);
                if (Files.isRegularFile(archivo)) imagen = ImageIO.read(archivo.toFile());
            }
        } catch (java.io.IOException error) {
            System.err.println("No se pudo cargar el fondo: " + tema);
        }
        imágenes.put(tema, imagen);
        return imagen;
    }
    public static void pintar(Graphics graphics, int ancho, int alto, String tema) {
        BufferedImage imagen = cargar(tema);
        if (imagen == null || ancho <= 0 || alto <= 0) return;
        Graphics2D g = (Graphics2D) graphics.create();
        double escala = Math.max((double) ancho / imagen.getWidth(), (double) alto / imagen.getHeight());
        int w = (int) Math.ceil(imagen.getWidth() * escala), h = (int) Math.ceil(imagen.getHeight() * escala);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(imagen, (ancho - w) / 2, (alto - h) / 2, w, h, null);
        g.setColor(new Color(3, 8, 5, 90));
        g.fillRect(0, 0, ancho, alto);
        g.dispose();
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        pintar(g, getWidth(), getHeight(), tema);
    }
}
