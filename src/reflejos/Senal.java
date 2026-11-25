package reflejos;

import java.awt.Color;
import javax.swing.ImageIcon;

// Representa un tipo de señal del juego: su color de fondo, su dibujo (puede ser
// null si la señal es solo texto), el texto que muestra y si es "buena" (hay que
// reaccionar) o trampa (no tocar). Tenerlo como clase aparte nos deja armar las
// señales como una lista de objetos, en vez de resolverlas con un switch grande.
public class Senal {

    private Color fondo;
    private ImageIcon icono;   // null cuando la señal no lleva dibujo
    private String texto;
    private boolean buena;     // true = hay que reaccionar, false = es trampa

    public Senal(Color fondo, ImageIcon icono, String texto, boolean buena) {
        this.fondo = fondo;
        this.icono = icono;
        this.texto = texto;
        this.buena = buena;
    }

    public Color getFondo() {
        return fondo;
    }

    public ImageIcon getIcono() {
        return icono;
    }

    public String getTexto() {
        return texto;
    }

    public boolean isBuena() {
        return buena;
    }
}
