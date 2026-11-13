package ui;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Pantalla temporal. El integrante 2 la sustituirá por RecordsFrame. */
public class RecordsPlaceholderFrame extends JInternalFrame {
    public RecordsPlaceholderFrame() {
        super("Récords", true, true, true, true);
        setSize(430, 220);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(9, 14, 10));
        JLabel message = new JLabel("La pantalla de récords será integrada por el integrante 2.", SwingConstants.CENTER);
        message.setForeground(Color.WHITE);
        message.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(message, BorderLayout.CENTER);
    }
}
