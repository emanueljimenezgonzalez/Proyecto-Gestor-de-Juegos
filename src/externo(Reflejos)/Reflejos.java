package reflejos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import plataforma.GameFunction;
import plataforma.Stat;

// Juego de reflejos tipo "go / no go": aparecen señales y hay que reaccionar
// rapido cuando son buenas (verde), pero NO tocar cuando son trampa (rojo).
// Se va poniendo mas rapido, y los aciertos seguidos arman combo para que suba
// la tension mientras avanza la partida.
public class Reflejos extends JInternalFrame implements GameFunction {

    // Singleton: siempre devolvemos la misma instancia, no queremos dos reflejos
    // abiertos al mismo tiempo.
    private static Reflejos instancia;

    public static Reflejos getInstancia() {
        if (instancia == null) {
            instancia = new Reflejos();
        }
        return instancia;
    }

    // Colores que usamos, los dejo como constantes para no repetir numeros sueltos.
    private static final Color NEUTRO = new Color(230, 230, 230);
    private static final Color VERDE = new Color(40, 180, 70);
    private static final Color ROJO = new Color(200, 50, 50);
    private static final Color NARANJA = new Color(235, 140, 20);   // fondo de las señales misterio
    private static final Color VERDE_TEXTO = new Color(30, 140, 55);

    // Cuantas señales trae una partida.
    private static final int TOTAL_SENALES = 25;
    // La ventana de reacción arranca grande y va bajando (la dificultad sube).
    // Con 28 por señal recien llega al minimo cerca del final de las 25, asi la
    // aceleración se siente repartida y no se estanca a la mitad.
    private static final int VENTANA_INICIAL = 1200;
    private static final int VENTANA_MINIMA = 550;
    private static final int BAJA_POR_SENAL = 28;   // cuanto se achica cada vez

    // estado de la partida
    private String nombreJugador;
    private int puntaje;
    private int senalActual;
    private int aciertos;
    private int fallos;
    private int racha;                 // aciertos seguidos (el combo)
    private int ventanaActual;         // ms que dura la señal de turno
    private boolean esperandoReaccion;
    private boolean senalBuena;        // true = hay que tocar, false = es trampa
    private long momentoAparicion;
    private boolean partidaEnCurso;
    private Stat resultado;            // queda en null hasta que la partida termina bien

    private Random random;
    private Timer timerAparicion;      // la espera antes de mostrar la proxima señal
    private Timer timerTick;           // refresca la barra de tiempo y marca el fin
    private Timer timerCuenta;         // la cuenta regresiva del arranque
    private int cuenta;

    // componentes de la UI
    private JPanel panelSenal;
    private JLabel lblSenal;
    private JLabel lblPuntaje;
    private JLabel lblProgreso;
    private JLabel lblCombo;
    private JLabel lblFeedback;
    private JProgressBar barraTiempo;
    private JButton btnIniciar;
    private JButton btnReaccionar;

    // La lista con los tipos de señal posibles. En cada aparicion elegimos uno
    // al azar de aca; tenerlos como objetos Senal nos evita el switch grande.
    private List<Senal> senales;

    // El constructor es privado porque el que reparte instancias es el singleton.
    private Reflejos() {
        super("Reflejos", true, true, true, true);
        setSize(480, 470);
        setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);

        random = new Random();
        resultado = null;

        armarUI();
        prepararTimers();
        prepararAtajoTeclado();

        // Si cierran la ventana a mitad de partida, frenamos los timers para que
        // no sigan corriendo por detrás (si no, salta el cartel de fin en una
        // ventana que ya nadie ve).
        addInternalFrameListener(new InternalFrameAdapter() {
            public void internalFrameClosing(InternalFrameEvent e) {
                detenerTimers();
                partidaEnCurso = false;
                esperandoReaccion = false;
            }
        });
    }

    private void armarUI() {
        setLayout(new BorderLayout());

        // Arriba: la instruccion siempre visible, y debajo el puntaje, el combo
        // y el progreso.
        JPanel arriba = new JPanel(new GridLayout(2, 1));
        arriba.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel lblInstruccion = new JLabel(
                "Verde: REACCIONA     Rojo: NO toques", SwingConstants.CENTER);
        lblInstruccion.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel fila = new JPanel(new GridLayout(1, 3));
        lblPuntaje = new JLabel("Puntaje: 0");
        lblCombo = new JLabel(" ", SwingConstants.CENTER);
        lblProgreso = new JLabel("Senal: 0 / " + TOTAL_SENALES, SwingConstants.RIGHT);
        lblPuntaje.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblCombo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCombo.setForeground(new Color(220, 120, 0));   // naranja para el combo
        lblProgreso.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fila.add(lblPuntaje);
        fila.add(lblCombo);
        fila.add(lblProgreso);

        arriba.add(lblInstruccion);
        arriba.add(fila);
        add(arriba, BorderLayout.NORTH);

        // El panel grande del medio es el que cambia de color segun la señal.
        panelSenal = new JPanel(new BorderLayout());
        panelSenal.setBackground(NEUTRO);
        panelSenal.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        lblSenal = new JLabel("Dale a Iniciar partida", SwingConstants.CENTER);
        lblSenal.setFont(new Font("SansSerif", Font.BOLD, 28));
        // El dibujo va arriba y el texto abajo.
        lblSenal.setHorizontalTextPosition(SwingConstants.CENTER);
        lblSenal.setVerticalTextPosition(SwingConstants.BOTTOM);
        lblSenal.setIconTextGap(8);
        panelSenal.add(lblSenal, BorderLayout.CENTER);
        add(panelSenal, BorderLayout.CENTER);

        // Armamos la lista con los 4 tipos de señal. Los dibujos del rayo y la
        // araña los hace la clase DibujoIconos; las dos de fondo naranja no dan
        // pista por el color, hay que reconocer el dibujo de memoria.
        senales = new ArrayList<Senal>();
        senales.add(new Senal(VERDE,   null,                        "YA!",       true));
        senales.add(new Senal(ROJO,    null,                        "NO toques", false));
        senales.add(new Senal(NARANJA, DibujoIconos.crearRayo(70),  "",          true));
        senales.add(new Senal(NARANJA, DibujoIconos.crearArana(70), "",          false));

        // Abajo, apilado: la barra de tiempo, el feedback y los botones.
        JPanel abajo = new JPanel();
        abajo.setLayout(new BoxLayout(abajo, BoxLayout.Y_AXIS));
        abajo.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));

        barraTiempo = new JProgressBar(0, 100);
        barraTiempo.setValue(0);
        barraTiempo.setForeground(VERDE);
        barraTiempo.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblFeedback = new JLabel(" ", SwingConstants.CENTER);
        lblFeedback.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblFeedback.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel botones = new JPanel();
        botones.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIniciar = new JButton("Iniciar partida");
        btnReaccionar = new JButton("Reaccionar");
        btnReaccionar.setEnabled(false);
        btnReaccionar.setToolTipText("Tambien podes usar la barra espaciadora");
        botones.add(btnIniciar);
        botones.add(btnReaccionar);

        abajo.add(barraTiempo);
        abajo.add(lblFeedback);
        abajo.add(botones);
        add(abajo, BorderLayout.SOUTH);

        btnIniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iniciarPartida();
            }
        });

        btnReaccionar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reaccionar();
            }
        });
    }

    private void prepararTimers() {
        // Este espera un ratito y recien ahí muestra la señal, asi el jugador no
        // puede anticipar siempre con el mismo timing.
        timerAparicion = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timerAparicion.stop();
                mostrarSenal();
            }
        });
        timerAparicion.setRepeats(false);

        // Este corre rapidito (cada 25ms) mientras hay una señal activa: refresca
        // la barra que se vacia y, cuando se acaba el tiempo, dispara el fallo.
        timerTick = new Timer(25, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!esperandoReaccion) {
                    timerTick.stop();
                    return;
                }
                long transcurrido = System.currentTimeMillis() - momentoAparicion;
                long restante = ventanaActual - transcurrido;
                if (restante <= 0) {
                    timerTick.stop();
                    barraTiempo.setValue(0);
                    seAcaboElTiempo();
                } else {
                    barraTiempo.setValue((int) (restante * 100 / ventanaActual));
                }
            }
        });

        // La cuenta regresiva del arranque: 3, 2, 1 y arranca.
        timerCuenta = new Timer(700, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cuenta--;
                if (cuenta >= 1) {
                    lblSenal.setText("" + cuenta);
                } else {
                    timerCuenta.stop();
                    programarSiguiente();
                }
            }
        });
    }

    // Dejamos que la barra espaciadora tambien sirva para reaccionar. En un juego
    // de reflejos es más comodo que estar apuntando al boton con el mouse.
    private void prepararAtajoTeclado() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "reaccionar");
        getRootPane().getActionMap().put("reaccionar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // solo si esta ventana es la seleccionada, para no robarle la
                // barra a otra ventana que este abierta.
                if (isSelected()) {
                    reaccionar();
                }
            }
        });
    }

    // Este es el metodo de la interfaz. La pantalla principal lo llama al abrir.
    // Solo deja el juego listo, no arranca solo (el jugador le da al boton).
    public void iniciar() {
        if (!partidaEnCurso) {
            lblSenal.setIcon(null);
            lblSenal.setText("Dale a Iniciar partida");
            panelSenal.setBackground(NEUTRO);
            lblFeedback.setText(" ");
            lblCombo.setText(" ");
            barraTiempo.setValue(0);
            btnIniciar.setEnabled(true);
            btnReaccionar.setEnabled(false);
        }
    }

    // Arranca una partida nueva: pide el nombre y larga la cuenta regresiva.
    private void iniciarPartida() {
        String nombre = JOptionPane.showInputDialog(this, "Ingrese su nombre:");
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Anonimo";   // si no pone nada igual lo dejamos jugar
        }
        nombreJugador = nombre.trim();

        // Le recordamos las reglas de las señales misterio una sola vez. Despues
        // en la partida ya va de memoria, que es la gracia.
        JOptionPane.showMessageDialog(this,
                "Ojo con las senales en fondo naranja:\n\n"
                + "   RAYO  = reacciona (es buena)\n"
                + "   ARANA = NO toques (es trampa)\n\n"
                + "Acordate cual es cual, el color no te va a ayudar!");

        // Reseteamos todo para la partida nueva.
        puntaje = 0;
        senalActual = 0;
        aciertos = 0;
        fallos = 0;
        racha = 0;
        resultado = null;
        partidaEnCurso = true;
        esperandoReaccion = false;

        btnIniciar.setEnabled(false);
        btnReaccionar.setEnabled(true);
        lblFeedback.setText(" ");
        lblCombo.setText(" ");
        barraTiempo.setValue(0);
        actualizarLabels();

        // Arrancamos con la cuenta regresiva para dar expectativa.
        cuenta = 3;
        panelSenal.setBackground(NEUTRO);
        lblSenal.setIcon(null);
        lblSenal.setText("3");
        timerCuenta.start();
    }

    // Deja el panel neutro y programa la proxima señal con una espera al azar.
    private void programarSiguiente() {
        if (senalActual >= TOTAL_SENALES) {
            terminarPartida();
            return;
        }
        esperandoReaccion = false;
        panelSenal.setBackground(NEUTRO);
        lblSenal.setIcon(null);
        lblSenal.setText("espera...");
        barraTiempo.setValue(0);

        // espera random entre 500 y 1400 ms antes de la proxima señal
        int espera = 500 + random.nextInt(900);
        timerAparicion.setInitialDelay(espera);
        timerAparicion.start();
    }

    // Muestra la señal: decide si es buena o trampa, achica la ventana de tiempo
    // segun cuanto avanzamos, y larga el tick que vacia la barra.
    private void mostrarSenal() {
        lblFeedback.setText(" ");   // limpiamos el feedback viejo

        // la ventana se achica a medida que avanza la partida (mas dificil)
        ventanaActual = VENTANA_INICIAL - (senalActual * BAJA_POR_SENAL);
        if (ventanaActual < VENTANA_MINIMA) {
            ventanaActual = VENTANA_MINIMA;
        }

        // Elegimos una señal al azar de la lista y la aplicamos al panel.
        Senal senal = senales.get(random.nextInt(senales.size()));
        senalBuena = senal.isBuena();
        panelSenal.setBackground(senal.getFondo());
        lblSenal.setIcon(senal.getIcono());
        lblSenal.setText(senal.getTexto());

        momentoAparicion = System.currentTimeMillis();
        esperandoReaccion = true;
        barraTiempo.setValue(100);
        timerTick.start();
    }

    // El jugador apreto reaccionar (o la barra espaciadora).
    private void reaccionar() {
        if (!esperandoReaccion) {
            return;   // si no hay señal activa ignoramos el click
        }
        timerTick.stop();
        esperandoReaccion = false;

        if (senalBuena) {
            // Bien, reaccionó a una buena. Base por velocidad más el bonus de combo.
            long tiempo = System.currentTimeMillis() - momentoAparicion;
            int base = (int) (100 - (tiempo / 10));
            if (base < 20) {
                base = 20;   // si acerto nunca menos de 20
            }
            racha++;
            int bonus = (racha - 1) * 15;   // mientras mas racha, mas bonus
            int puntos = base + bonus;
            puntaje += puntos;
            aciertos++;

            String msg = "+" + puntos + "  bien! (" + tiempo + " ms)";
            if (racha >= 2) {
                msg = msg + "   combo x" + racha;
            }
            mostrarFeedback(msg, true);
        } else {
            // toco una trampa: resta y se corta el combo
            puntaje -= 40;
            fallos++;
            racha = 0;
            mostrarFeedback("-40  tocaste la trampa", false);
        }

        senalActual++;
        actualizarLabels();
        programarSiguiente();
    }

    // Se acabo el tiempo de la señal: el jugador no reacciono.
    private void seAcaboElTiempo() {
        esperandoReaccion = false;

        if (senalBuena) {
            // dejo pasar una buena: pierde puntos y se corta el combo
            puntaje -= 20;
            fallos++;
            racha = 0;
            mostrarFeedback("-20  se te paso", false);
        } else {
            // no toco la trampa, eso estaba bien: suma y mantiene el combo
            racha++;
            int puntos = 50 + (racha - 1) * 15;
            puntaje += puntos;
            aciertos++;

            String msg = "+" + puntos + "  bien, aguantaste";
            if (racha >= 2) {
                msg = msg + "   combo x" + racha;
            }
            mostrarFeedback(msg, true);
        }

        senalActual++;
        actualizarLabels();
        programarSiguiente();
    }

    // Pinta el mensajito de feedback: verde si sumo, rojo si perdio puntos.
    private void mostrarFeedback(String texto, boolean positivo) {
        lblFeedback.setText(texto);
        if (positivo) {
            lblFeedback.setForeground(VERDE_TEXTO);
        } else {
            lblFeedback.setForeground(ROJO);
        }
    }

    // Cierra la partida y arma el Stat con el puntaje final.
    private void terminarPartida() {
        partidaEnCurso = false;
        esperandoReaccion = false;
        btnReaccionar.setEnabled(false);
        btnIniciar.setEnabled(true);
        barraTiempo.setValue(0);

        if (puntaje < 0) {
            puntaje = 0;   // no guardamos records negativos
        }

        panelSenal.setBackground(NEUTRO);
        lblSenal.setIcon(null);
        lblSenal.setText("Terminaste. Puntaje: " + puntaje);
        lblFeedback.setText(" ");
        lblCombo.setText(" ");

        // la clave es "puntaje" porque en este juego gana el que saca mas
        resultado = new Stat("puntaje", nombreJugador, puntaje);

        JOptionPane.showMessageDialog(this,
                "Juego terminado.\n\n"
                + "Puntaje final: " + puntaje + "\n"
                + "Aciertos: " + aciertos + "\n"
                + "Fallos: " + fallos);
    }

    private void actualizarLabels() {
        lblPuntaje.setText("Puntaje: " + puntaje);
        lblProgreso.setText("Senal: " + senalActual + " / " + TOTAL_SENALES);
        if (racha >= 2) {
            lblCombo.setText("Combo x" + racha);
        } else {
            lblCombo.setText(" ");
        }
    }

    private void detenerTimers() {
        if (timerAparicion != null) {
            timerAparicion.stop();
        }
        if (timerTick != null) {
            timerTick.stop();
        }
        if (timerCuenta != null) {
            timerCuenta.stop();
        }
    }

    // La pantalla principal pide esto cuando cierran el juego. Si la partida no
    // termino devolvemos null, asi no se guarda un record a medias.
    public Stat getStats() {
        return resultado;
    }
}
