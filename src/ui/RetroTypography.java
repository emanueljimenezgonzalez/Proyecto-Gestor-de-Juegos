package ui;

import java.awt.Font;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.util.ArrayList;
import java.util.List;

/** Estilo de terminal clásica con respaldo Unicode de Java para tildes y símbolos. */
public final class RetroTypography {
    private RetroTypography() { }
    public static Font fuente(int estilo, int tamaño) {
        return new Font(Font.MONOSPACED, estilo, tamaño);
    }
    public static void instalar() {
        List<Object> claves = new ArrayList<>(UIManager.getDefaults().keySet());
        for (Object clave : claves) {
            Object valor = UIManager.get(clave);
            if (valor instanceof Font) {
                Font anterior = (Font) valor;
                UIManager.put(clave, new FontUIResource(fuente(anterior.getStyle(), Math.max(13, anterior.getSize()))));
            }
        }
    }
}
