package tresenraya;

/** Una casilla del tablero: guarda su posición fija y la marca que contiene. */
public class Cell {

    /** Contenido posible de una casilla. */
    public enum Mark {
        EMPTY(' '), X('X'), O('O');

        private final char symbol;

        Mark(char symbol) { this.symbol = symbol; }

        public char getSymbol() { return symbol; }
    }

    private final int row;
    private final int col;
    private Mark mark = Mark.EMPTY;

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }

    public int getCol() { return col; }

    public Mark getMark() { return mark; }

    public boolean isEmpty() { return mark == Mark.EMPTY; }

    /** Solo el tablero modifica las casillas. */
    void setMark(Mark mark) { this.mark = mark; }

    @Override
    public String toString() {
        return "Cell[" + row + "," + col + "=" + mark + "]";
    }
}
