package tresenraya;

import core.PlayerSession;
import core.Stat;

import javax.swing.SwingUtilities;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba de consola del juego completo, sin abrir ventanas
 * (ejecutar con -Djava.awt.headless=true).
 */
public class TicTacToeGameTest {
    private static int fallos = 0;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(TicTacToeGameTest::probar);
        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASARON" : "\nFALLOS: " + fallos);
        if (fallos > 0) System.exit(1);
    }

    private static void probar() {
        PlayerSession.iniciar("Ana");
        TicTacToeGame juego = TicTacToeGame.getInstance();
        check("Singleton: getInstance devuelve siempre el mismo objeto", juego == TicTacToeGame.getInstance());
        check("Sin partida terminada, getStats es null", juego.getStats() == null);

        AtomicInteger eventos = new AtomicInteger();
        juego.addPropertyChangeListener("resultadoPartida", e -> eventos.incrementAndGet());

        // Partida 1: empieza X (Ana). X gana la fila superior.
        juego.iniciar();
        check("Partida nueva sin jugadas no cuenta como en curso", !juego.tienePartidaEnCurso());
        juego.jugar(0, 0);
        check("Hay partida en curso tras la primera jugada", juego.tienePartidaEnCurso());
        juego.jugar(0, 0);
        juego.jugar(1, 0);
        juego.jugar(0, 1);
        juego.jugar(1, 1);
        juego.jugar(0, 2);
        Stat s1 = juego.getStats();
        check("Casilla ocupada se rechaza sin cambiar el turno (X completa la fila)", s1 != null);
        check("Gana Ana con valor 1 y clave Victorias",
                s1 != null && s1.getNombre().equals("Ana") && s1.getValor() == 1
                        && s1.getClave().equals("Victorias"));
        check("Se publicó exactamente un resultado", eventos.get() == 1);
        check("Tras terminar ya no hay partida en curso", !juego.tienePartidaEnCurso());
        juego.jugar(2, 2);
        check("No se puede jugar con la partida terminada", eventos.get() == 1);

        // Partida 2: empieza O (Jugador 2). Termina en empate.
        juego.iniciar();
        check("Al iniciar se limpia el resultado anterior", juego.getStats() == null);
        int[][] jugadas = {{0, 0}, {0, 1}, {0, 2}, {1, 1}, {1, 0}, {1, 2}, {2, 1}, {2, 0}, {2, 2}};
        for (int[] j : jugadas) juego.jugar(j[0], j[1]);
        check("Empate: no hay resultado ni evento", juego.getStats() == null && eventos.get() == 1);

        // Partida 3: empieza X de nuevo; Ana gana otra vez, acumula 2.
        juego.iniciar();
        juego.jugar(0, 0);
        juego.jugar(1, 0);
        juego.jugar(0, 1);
        juego.jugar(1, 1);
        juego.jugar(0, 2);
        Stat s3 = juego.getStats();
        check("Ana acumula 2 victorias en la sesión", s3 != null && s3.getNombre().equals("Ana") && s3.getValor() == 2);
        check("getStats devuelve una copia", juego.getStats() != s3);
        check("Se publicaron dos resultados en total", eventos.get() == 2);
    }

    private static void check(String prueba, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALLO] ") + prueba);
        if (!ok) fallos++;
    }
}
