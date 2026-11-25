package tresenraya;

import java.util.Iterator;

/**
 * Tablero de 3 x 3 de Tres en raya. Mantiene el estado de las nueve casillas
 * y ofrece un {@link Iterator} para recorrerlas sin exponer la matriz interna.
 */
public class Board implements Iterable<Cell> {
    public static final int SIZE = 3;
    public static final int CELL_COUNT = SIZE * SIZE;
    /** 3 filas + 3 columnas + 2 diagonales. */
    public static final int LINE_COUNT = 2 * SIZE + 2;

    private final Cell[][] cells = new Cell[SIZE][SIZE];

    public Board() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }
    }

    /** Devuelve la casilla de la posición indicada (0 a 2 en fila y columna). */
    public Cell getCell(int row, int col) {
        validarPosicion(row, col);
        return cells[row][col];
    }

    /**
     * Coloca una marca en una casilla libre.
     *
     * @throws IllegalArgumentException si la posición está fuera del tablero
     *                                  o la marca es vacía.
     * @throws IllegalStateException    si la casilla ya está ocupada.
     */
    public void place(int row, int col, Cell.Mark mark) {
        if (mark == null || mark == Cell.Mark.EMPTY) {
            throw new IllegalArgumentException("Debe colocarse una X o una O.");
        }
        Cell cell = getCell(row, col);
        if (!cell.isEmpty()) {
            throw new IllegalStateException("La casilla (" + row + ", " + col + ") ya está ocupada.");
        }
        cell.setMark(mark);
    }

    /** Verdadero cuando no queda ninguna casilla libre. */
    public boolean isFull() {
        for (Cell cell : this) {
            if (cell.isEmpty()) return false;
        }
        return true;
    }

    /**
     * Busca una línea completa (fila, columna o diagonal) de una misma marca.
     * Recorre las nueve casillas una sola vez con el iterador y cuenta las
     * marcas de cada jugador por línea.
     *
     * @return las tres casillas de la línea ganadora, o {@code null} si todavía no hay ganador.
     */
    public Cell[] findWinningLine() {
        int[][] conteo = new int[2][LINE_COUNT]; // [0 = X, 1 = O][línea]
        for (Cell cell : this) {
            if (cell.isEmpty()) continue;
            int jugador = cell.getMark() == Cell.Mark.X ? 0 : 1;
            int r = cell.getRow();
            int c = cell.getCol();
            conteo[jugador][r]++;                       // filas: 0 a 2
            conteo[jugador][SIZE + c]++;                // columnas: 3 a 5
            if (r == c) conteo[jugador][2 * SIZE]++;    // diagonal principal: 6
            if (r + c == SIZE - 1) conteo[jugador][2 * SIZE + 1]++; // diagonal inversa: 7
        }
        for (int jugador = 0; jugador < 2; jugador++) {
            for (int linea = 0; linea < LINE_COUNT; linea++) {
                if (conteo[jugador][linea] == SIZE) return casillasDeLinea(linea);
            }
        }
        return null;
    }

    /** Marca ganadora, o {@link Cell.Mark#EMPTY} si todavía no hay ganador. */
    public Cell.Mark getWinner() {
        Cell[] linea = findWinningLine();
        return linea == null ? Cell.Mark.EMPTY : linea[0].getMark();
    }

    private Cell[] casillasDeLinea(int linea) {
        Cell[] casillas = new Cell[SIZE];
        for (int i = 0; i < SIZE; i++) {
            if (linea < SIZE) casillas[i] = cells[linea][i];
            else if (linea < 2 * SIZE) casillas[i] = cells[i][linea - SIZE];
            else if (linea == 2 * SIZE) casillas[i] = cells[i][i];
            else casillas[i] = cells[i][SIZE - 1 - i];
        }
        return casillas;
    }

    /** Deja las nueve casillas vacías para una nueva partida. */
    public void reset() {
        for (Cell cell : this) {
            cell.setMark(Cell.Mark.EMPTY);
        }
    }

    /** Iterador que recorre fila por fila, de izquierda a derecha. */
    @Override
    public Iterator<Cell> iterator() {
        return new BoardIterator(this);
    }

    private static void validarPosicion(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IllegalArgumentException("Posición fuera del tablero: (" + row + ", " + col + ").");
        }
    }
}
