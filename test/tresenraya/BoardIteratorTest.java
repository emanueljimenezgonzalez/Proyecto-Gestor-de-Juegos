package tresenraya;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/** Pruebas de consola de la Fase 1 (sin librerías externas). */
public class BoardIteratorTest {
    private static int fallos = 0;

    public static void main(String[] args) {
        recorreNueveUnaVezYTermina();
        ordenFilaPorFila();
        lanzaExcepcionAlTerminar();
        iteradoresIndependientes();
        colocarYCasillaOcupada();
        posicionFueraDelTablero();
        tableroLlenoYReinicio();
        System.out.println(fallos == 0 ? "\nTODAS LAS PRUEBAS PASARON" : "\nFALLOS: " + fallos);
        if (fallos > 0) System.exit(1);
    }

    private static void check(String prueba, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALLO] ") + prueba);
        if (!ok) fallos++;
    }

    private static void recorreNueveUnaVezYTermina() {
        Board board = new Board();
        Set<Cell> vistas = new HashSet<>();
        int visitas = 0;
        Iterator<Cell> it = board.iterator();
        while (it.hasNext()) { vistas.add(it.next()); visitas++; }
        check("Iterator visita 9 casillas", visitas == 9);
        check("Iterator no repite casillas", vistas.size() == 9);
        check("Iterator termina (hasNext = false)", !it.hasNext());
    }

    private static void ordenFilaPorFila() {
        int esperado = 0;
        boolean ok = true;
        for (Cell cell : new Board()) {
            if (cell.getRow() != esperado / 3 || cell.getCol() != esperado % 3) ok = false;
            esperado++;
        }
        check("Orden fila por fila: (0,0) ... (2,2)", ok);
    }

    private static void lanzaExcepcionAlTerminar() {
        Iterator<Cell> it = new Board().iterator();
        for (int i = 0; i < 9; i++) it.next();
        boolean lanzo = false;
        try { it.next(); } catch (NoSuchElementException e) { lanzo = true; }
        check("next() tras la casilla 9 lanza NoSuchElementException", lanzo);
    }

    private static void iteradoresIndependientes() {
        Board board = new Board();
        Iterator<Cell> a = board.iterator();
        a.next(); a.next();
        Iterator<Cell> b = board.iterator();
        Cell primera = b.next();
        check("Cada iterador empieza desde el inicio",
                primera.getRow() == 0 && primera.getCol() == 0);
    }

    private static void colocarYCasillaOcupada() {
        Board board = new Board();
        board.place(1, 1, Cell.Mark.X);
        check("place coloca la marca", board.getCell(1, 1).getMark() == Cell.Mark.X);
        boolean lanzo = false;
        try { board.place(1, 1, Cell.Mark.O); } catch (IllegalStateException e) { lanzo = true; }
        check("No se puede jugar en casilla ocupada", lanzo);
        check("La marca original se conserva", board.getCell(1, 1).getMark() == Cell.Mark.X);
    }

    private static void posicionFueraDelTablero() {
        Board board = new Board();
        int lanzadas = 0;
        int[][] malas = {{-1, 0}, {0, -1}, {3, 0}, {0, 3}};
        for (int[] p : malas) {
            try { board.place(p[0], p[1], Cell.Mark.X); } catch (IllegalArgumentException e) { lanzadas++; }
        }
        check("Posiciones fuera del tablero se rechazan", lanzadas == malas.length);
    }

    private static void tableroLlenoYReinicio() {
        Board board = new Board();
        check("Tablero nuevo no está lleno", !board.isFull());
        Cell.Mark marca = Cell.Mark.X;
        for (Cell cell : board) {
            board.place(cell.getRow(), cell.getCol(), marca);
            marca = marca == Cell.Mark.X ? Cell.Mark.O : Cell.Mark.X;
        }
        check("Tablero con 9 marcas está lleno", board.isFull());
        board.reset();
        boolean vacio = true;
        for (Cell cell : board) if (!cell.isEmpty()) vacio = false;
        check("reset() deja las 9 casillas vacías", vacio && !board.isFull());
    }
}
