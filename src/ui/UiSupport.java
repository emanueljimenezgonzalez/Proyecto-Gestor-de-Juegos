package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import core.PlayerNames;
import java.awt.*;
import java.awt.event.*;

/** Reglas compartidas de foco, contraste, atajos locales y confirmaciones seguras. */
public final class UiSupport {
    private UiSupport() { }

    public static JButton boton(String texto) {
        JButton botón = new JButton(texto);
        botón.setBackground(new Color(25, 110, 55));
        configurarBoton(botón);
        return botón;
    }

    public static void configurarBoton(JButton botón) {
        botón.setUI(new BasicButtonUI() {
            @Override protected void paintText(Graphics g, AbstractButton b, Rectangle zona, String texto) {
                if (b.isEnabled()) super.paintText(g, b, zona, texto);
                else {
                    g.setColor(new Color(183, 200, 188));
                    g.drawString(texto, zona.x, zona.y + g.getFontMetrics().getAscent());
                }
            }
        });
        botón.setFont(RetroTypography.fuente(Font.BOLD, 14));
        botón.setForeground(Color.WHITE);
        botón.setOpaque(true);
        botón.setContentAreaFilled(true);
        botón.setBorderPainted(true);
        botón.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        atajo(botón, JComponent.WHEN_FOCUSED, "ENTER", "activar", () -> { if (botón.isEnabled()) botón.doClick(0); });
        borde(botón, false);
        botón.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { borde(botón, true); }
            @Override public void focusLost(FocusEvent e) { borde(botón, false); }
        });
    }

    private static void borde(JButton botón, boolean foco) {
        botón.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foco ? new Color(181, 255, 201) : botón.getBackground(), 2),
                new EmptyBorder(7, 8, 7, 8)));
    }

    public static void atajo(JComponent componente, int ámbito, String tecla, String acción, Runnable tarea) {
        componente.getInputMap(ámbito).put(KeyStroke.getKeyStroke(tecla), acción);
        componente.getActionMap().put(acción, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent evento) { tarea.run(); }
        });
    }

    /** La primera opción y Escape conservan la partida; Enter no descarta por accidente. */
    public static boolean confirmarDescarte(Component padre, String mensaje, String acción) {
        Object[] opciones = {"Cancelar", acción};
        return JOptionPane.showOptionDialog(padre, mensaje, acción, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]) == 1;
    }

    public static void errorNombre(JTextField campo, JLabel error, String mensaje) {
        mostrarError(campo, error, mensaje);
        if (mensaje != null) { campo.requestFocusInWindow(); campo.selectAll(); }
    }

    private static void mostrarError(JTextField campo, JLabel error, String mensaje) {
        error.setText(mensaje == null ? " " : "<html>" + mensaje + "</html>");
        error.setForeground(new Color(255, 191, 157));
        campo.getAccessibleContext().setAccessibleDescription(mensaje == null
                ? "Nombre del jugador, de 1 a 120 caracteres." : mensaje);
    }

    public static void validarNombreAlEditar(JTextField campo, JLabel error) {
        campo.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        campo.getDocument().addDocumentListener(new DocumentListener() {
            private void revisar() {
                try { PlayerNames.validar(campo.getText()); mostrarError(campo, error, null); }
                catch (IllegalArgumentException e) { mostrarError(campo, error, e.getMessage()); }
            }
            @Override public void insertUpdate(DocumentEvent e) { revisar(); }
            @Override public void removeUpdate(DocumentEvent e) { revisar(); }
            @Override public void changedUpdate(DocumentEvent e) { revisar(); }
        });
    }

    public static void mostrarAyuda(Component padre, String título, String texto) {
        JTextArea contenido = new JTextArea(texto, 0, 42);
        contenido.setFont(RetroTypography.fuente(Font.PLAIN, 14));
        contenido.setEditable(false);
        contenido.setLineWrap(true);
        contenido.setWrapStyleWord(true);
        contenido.setOpaque(false);
        contenido.getAccessibleContext().setAccessibleName(título);
        JOptionPane.showMessageDialog(padre, contenido, título, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void exigirSwing() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("La ventana debe controlarse desde el hilo Swing.");
    }
}
