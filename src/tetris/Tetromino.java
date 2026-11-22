package tetris;

/** Las siete piezas y sus rotaciones. No contiene dependencias de Swing. */
public enum Tetromino {
    I(new String[]{"0000", "1111", "0000", "0000"}),
    O(new String[]{"11", "11"}),
    T(new String[]{"010", "111", "000"}),
    S(new String[]{"011", "110", "000"}),
    Z(new String[]{"110", "011", "000"}),
    J(new String[]{"100", "111", "000"}),
    L(new String[]{"001", "111", "000"});

    private final boolean[][][] rotaciones;

    Tetromino(String[] patron) {
        int n = patron.length;
        rotaciones = new boolean[4][n][n];
        for (int fila = 0; fila < n; fila++)
            for (int columna = 0; columna < n; columna++)
                rotaciones[0][fila][columna] = patron[fila].charAt(columna) == '1';
        for (int giro = 1; giro < 4; giro++)
            for (int fila = 0; fila < n; fila++)
                for (int columna = 0; columna < n; columna++)
                    rotaciones[giro][fila][columna] = rotaciones[giro - 1][n - 1 - columna][fila];
    }

    public int getTamaño() { return rotaciones[0].length; }
    public boolean ocupa(int giro, int fila, int columna) {
        return rotaciones[Math.floorMod(giro, 4)][fila][columna];
    }
}
