package tresenraya;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Patrón Iterator: recorre las nueve casillas del {@link Board} fila por fila
 * (0,0), (0,1), (0,2), (1,0) ... (2,2) y termina después de visitar la última.
 * Quien lo usa no necesita conocer cómo se guarda el tablero.
 */
public class BoardIterator implements Iterator<Cell> {
    private final Board board;
    private int next = 0;

    BoardIterator(Board board) {
        this.board = board;
    }

    @Override
    public boolean hasNext() {
        return next < Board.CELL_COUNT;
    }

    @Override
    public Cell next() {
        if (!hasNext()) {
            throw new NoSuchElementException("Ya se visitaron las nueve casillas.");
        }
        Cell cell = board.getCell(next / Board.SIZE, next % Board.SIZE);
        next++;
        return cell;
    }
}
