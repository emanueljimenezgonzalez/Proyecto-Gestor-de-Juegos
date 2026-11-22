package ui;

import core.Stat;
import records.RecordsController;
import records.RecordsManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

/** Vista del top 3; la clasificación y el almacenamiento pertenecen al modelo. */
public final class RecordsFrame extends JInternalFrame {
    private static final Color FONDO = new Color(9, 14, 10);
    private static final Color PANEL = new Color(22, 34, 25);
    private static final Color TEXTO = new Color(248, 250, 248);
    private static final Color SECUNDARIO = new Color(183, 194, 185);
    private final RecordsController controller;
    private final RecordsManager manager;
    private final JComboBox<Juego> selector = new JComboBox<>();
    private final JButton actualizar = new JButton("Actualizar");
    private final JTextArea estado = new JTextArea(2, 20);
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Posición", "Jugador", "Valor"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
        @Override public Class<?> getColumnClass(int columna) { return columna == 1 ? String.class : Integer.class; }
    };
    private final JTable tabla = new JTable(modelo);
    private final Runnable observador = () -> SwingUtilities.invokeLater(this::refrescar);
    private boolean cargando;

    public RecordsFrame(RecordsController controller) {
        super("Récords generales", true, true, true, true);
        this.controller = controller;
        this.manager = controller.getManager();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(680, 400);
        setMinimumSize(new Dimension(460, 300));
        setLocation(75, 55);
        JPanel contenido = new GameBackground("inicio", new BorderLayout(0, 20));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(24, 26, 24, 26));
        setContentPane(contenido);

        JPanel cabecera = new JPanel(new BorderLayout(12, 16));
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("Récords generales");
        titulo.setFont(RetroTypography.fuente(Font.BOLD, 25));
        titulo.setForeground(TEXTO);
        cabecera.add(titulo, BorderLayout.NORTH);
        JPanel controles = new JPanel(new BorderLayout(12, 0));
        controles.setOpaque(false);
        JLabel etiqueta = new JLabel("Juego:");
        etiqueta.setForeground(TEXTO);
        etiqueta.setLabelFor(selector);
        etiqueta.setDisplayedMnemonic('J');
        for (Map.Entry<String, String> juego : manager.getJuegos().entrySet())
            selector.addItem(new Juego(juego.getKey(), juego.getValue()));
        selector.getAccessibleContext().setAccessibleName("Juego cuyos récords se consultan");
        controles.add(etiqueta, BorderLayout.WEST);
        controles.add(selector, BorderLayout.CENTER);
        actualizar.setMnemonic('A');
        actualizar.setForeground(Color.WHITE);
        actualizar.setBackground(new Color(25, 110, 55));
        actualizar.setOpaque(true);
        actualizar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        actualizar.setContentAreaFilled(true);
        actualizar.setBorderPainted(false);
        actualizar.setBorder(new EmptyBorder(9, 16, 9, 16));
        UiSupport.configurarBoton(actualizar);
        actualizar.setMnemonic(0);
        controles.add(actualizar, BorderLayout.EAST);
        cabecera.add(controles, BorderLayout.CENTER);
        contenido.add(cabecera, BorderLayout.NORTH);

        tabla.setRowHeight(42);
        tabla.setFont(RetroTypography.fuente(Font.PLAIN, 15));
        tabla.setForeground(TEXTO);
        tabla.setBackground(PANEL);
        tabla.setSelectionBackground(new Color(35, 94, 55));
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setGridColor(new Color(73, 96, 78));
        tabla.setShowGrid(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getAccessibleContext().setAccessibleName("Tres mejores resultados del juego seleccionado");
        tabla.getAccessibleContext().setAccessibleDescription("Tabla de solo lectura. Usa las flechas para recorrer las filas.");
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(RetroTypography.fuente(Font.BOLD, 13));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(85);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabla.setDefaultRenderer(Integer.class, new Celda(SwingConstants.RIGHT));
        tabla.setDefaultRenderer(String.class, new Celda(SwingConstants.LEFT));
        JScrollPane desplazamiento = new JScrollPane(tabla);
        desplazamiento.setColumnHeaderView(tabla.getTableHeader());
        desplazamiento.getViewport().setBackground(PANEL);
        desplazamiento.setBorder(BorderFactory.createLineBorder(new Color(73, 96, 78)));
        contenido.add(desplazamiento, BorderLayout.CENTER);
        estado.setForeground(SECUNDARIO);
        estado.setFont(RetroTypography.fuente(Font.PLAIN, 13));
        estado.setBackground(FONDO);
        estado.setEditable(false);
        estado.setFocusable(false);
        estado.setLineWrap(true);
        estado.setWrapStyleWord(true);
        estado.getAccessibleContext().setAccessibleName("Estado de los récords");
        contenido.add(estado, BorderLayout.SOUTH);

        selector.addActionListener(e -> refrescar());
        actualizar.addActionListener(e -> cargar());
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "F5", "actualizar", this::cargar);
        UiSupport.atajo(getRootPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, "alt A", "actualizarAlt", this::cargar);
        manager.agregarObservador(observador);
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override public void internalFrameClosed(InternalFrameEvent e) { manager.quitarObservador(observador); }
        });
        refrescar();
    }

    public void refrescar() {
        if (isClosed() || cargando) return;
        Juego juego = (Juego) selector.getSelectedItem();
        modelo.setRowCount(0);
        if (juego == null) {
            estado.setText("Todavía no hay juegos registrados.");
            actualizar.setEnabled(false);
            return;
        }
        java.util.List<Stat> datos = manager.obtenerTop3(juego.id);
        String clave = datos.isEmpty() ? "Valor" : datos.get(0).getClave();
        tabla.getColumnModel().getColumn(2).setHeaderValue(clave);
        tabla.getTableHeader().repaint();
        int posicion = 1;
        for (Stat stat : datos) modelo.addRow(new Object[]{posicion++, stat.getNombre(), stat.getValor()});
        String error = manager.getError(juego.id);
        estado.setForeground(error == null ? SECUNDARIO : new Color(255, 191, 157));
        if (error != null) {
            estado.setText(manager.tieneGuardadoPendiente(juego.id)
                    ? "Sin guardar. Pulsa Reintentar guardado."
                    : "No se pudieron cargar los récords. Corrige el archivo y pulsa Actualizar.");
            estado.setToolTipText("El archivo original se conserva. Revisa la carpeta de récords o los permisos de acceso y vuelve a intentar.");
        } else {
            estado.setText(manager.tieneGuardadoPendiente(juego.id) ? "Guardando el resultado…"
                    : datos.isEmpty() ? "Todavía no hay resultados registrados."
                    : datos.size() + (datos.size() == 1 ? " resultado guardado." : " mejores resultados guardados."));
            estado.setToolTipText(null);
        }
        actualizar.setText(manager.tieneGuardadoPendiente(juego.id) ? "Reintentar guardado" : "Actualizar");
        actualizar.getAccessibleContext().setAccessibleName(actualizar.getText() + " de " + juego.nombre);
        actualizar.setToolTipText("F5: actualizar o reintentar el guardado de " + juego.nombre);
    }

    private void cargar() {
        Juego juego = (Juego) selector.getSelectedItem();
        if (juego == null || cargando) return;
        cargando = true;
        selector.setEnabled(false);
        actualizar.setEnabled(false);
        estado.setForeground(SECUNDARIO);
        estado.setText("Procesando récords…");
        controller.actualizar(juego.id, error -> {
            if (isClosed()) return;
            cargando = false;
            selector.setEnabled(true);
            actualizar.setEnabled(true);
            refrescar();
            if (error != null && manager.getError(juego.id) == null) {
                estado.setText("No se pudo completar la operación. Vuelve a intentar cuando la aplicación esté disponible.");
                estado.setForeground(new Color(255, 191, 157));
            }
        });
    }

    private static final class Juego {
        final String id;
        final String nombre;
        Juego(String id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }

    private static final class Celda extends DefaultTableCellRenderer {
        Celda(int alineacion) {
            setHorizontalAlignment(alineacion);
            putClientProperty("html.disable", Boolean.TRUE);
        }
        @Override public Component getTableCellRendererComponent(JTable tabla, Object valor,
                boolean seleccionada, boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
            setBorder(BorderFactory.createCompoundBorder(getBorder(), new EmptyBorder(0, 10, 0, 10)));
            setToolTipText(valor == null ? null : valor.toString());
            return this;
        }
    }
}
