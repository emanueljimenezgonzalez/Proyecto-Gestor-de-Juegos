package tresenraya;

/** Pruebas de consola de las reglas del tablero (victoria y empate). */
public class BoardRulesTest {
    private static int fallos = 0;

    public static void main(String[] args) {
        // Las 8 líneas posibles: filas 0-2, columnas 0-2, diagonal y diagonal inversa.
        int[][][] lineas = {
            {{0, 0}, {0, 1}, {0, 2}}, {{1, 0}, {1, 1}, {1, 2}}, {{2, 0}, {2, 1}, {2, 2}},
            {{0, 0}, {1, 0}, {2, 0}}, {{0, 1}, {1, 1}, {2, 1}}, {{0, 2}, {1, 2}, {2, 2}},
            {{0, 0}, {1, 1}, {2, 2}}, {{0, 2}, {1, 1}, {2, 0}}
        };
        String[] nombres = {"fila 1", "fila 2", "fila 3", "columna 1", "columna 2", "columna 3",
                "diagonal", "diagonal inversa"};
        for (int i = 0; i < lineas.length; i++) {
            for (Cell.Mark marca : new Cell.Mark[] {Cell.Mark.X, Cell.Mark.O}) {
                Board board = new Board();
                for (int[] p : lineas[i]) board.place(p[0], p[1], marca);
                Cell[] ganadora = board.findWinningLine();
                check("Gana " + marca + " con " + nombres[i],
                        ganadora != null && ganadora.length == 3 && board.getWinner() == marca);
            }
        }

        Board vacio = new Board();
        check("Tablero vacío no tiene ganador", vacio.findWinningLine() == null
                && vacio.getWinner() == Cell.Mark.EMPTY);

        Board dos = new Board();
        dos.place(0, 0, Cell.Mark.X);
        dos.place(0, 1, Cell.Mark.X);
        check("Dos en línea no es victoria", dos.findWinningLine() == null);

        Board mezclada = new Board();
        mezclada.place(0, 0, Cell.Mark.X);
        mezclada.place(0, 1, Cell.Mark.O);
        mezclada.place(0, 2, Cell.Mark.X);
        check("Fila mezclada no es victoria", mezclada.findWinningLine() == null);

        // X O X / X O O / O X X : lleno y sin ganador.
        Board empate = new Board();
        Cell.Mark X = Cell.Mark.X, O = Cell.Mark.O;
        Cell.Mark[][] datos = {{X, O, X}, {X, O, O}, {O, X, X}};
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) empate.place(r, c, datos[r][c]);
        check("Empate: tablero lleno sin ganador", empate.isFull() && empate.findWinningLine() == null);

        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASARON" : "\nFALLOS: " + fallos);
        if (fallos > 0) System.exit(1);
    }

    private static void check(String prueba, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALLO] ") + prueba);
        if (!ok) fallos++;
    }
}
