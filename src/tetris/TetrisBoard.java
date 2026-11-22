package tetris;

import java.util.*;

/** Motor de Tetris independiente de la interfaz; las coordenadas son fila y columna. */
public final class TetrisBoard {
    public static final int FILAS = 20;
    public static final int COLUMNAS = 10;
    public enum Estado { LISTO, JUGANDO, PAUSADO, TERMINADO }

    private final Tetromino[][] celdas = new Tetromino[FILAS][COLUMNAS];
    private final Deque<Tetromino> bolsa = new ArrayDeque<>();
    private final Random aleatorio;
    private Estado estado = Estado.LISTO;
    private Tetromino activa;
    private Tetromino siguiente;
    private int fila;
    private int columna;
    private int giro;
    private int puntaje;
    private int líneas;

    public TetrisBoard() { this(new Random()); }
    public TetrisBoard(Random aleatorio) { this.aleatorio = Objects.requireNonNull(aleatorio); }

    public void iniciar() {
        for (Tetromino[] renglon : celdas) Arrays.fill(renglon, null);
        bolsa.clear();
        puntaje = 0;
        líneas = 0;
        estado = Estado.JUGANDO;
        siguiente = sacarPieza();
        generarPieza();
    }

    public boolean mover(int desplazamiento) {
        if (desplazamiento != -1 && desplazamiento != 1)
            throw new IllegalArgumentException("El movimiento horizontal debe ser -1 o 1.");
        if (estado != Estado.JUGANDO || !cabe(activa, giro, fila, columna + desplazamiento)) return false;
        columna += desplazamiento;
        return true;
    }

    public boolean rotar() {
        if (estado != Estado.JUGANDO) return false;
        int nuevoGiro = (giro + 1) % 4;
        // Ajustes sencillos junto a paredes y suelo; no se pretende implementar SRS oficial.
        int[][] ajustes = {{0, 0}, {0, -1}, {0, 1}, {0, -2}, {0, 2}, {-1, 0}, {-2, 0}};
        for (int[] ajuste : ajustes) {
            if (cabe(activa, nuevoGiro, fila + ajuste[0], columna + ajuste[1])) {
                giro = nuevoGiro;
                fila += ajuste[0];
                columna += ajuste[1];
                return true;
            }
        }
        return false;
    }

    /** Un paso de gravedad, o descenso manual que suma un punto por celda recorrida. */
    public boolean bajar(boolean manual) {
        if (estado != Estado.JUGANDO) return false;
        if (cabe(activa, giro, fila + 1, columna)) {
            fila++;
            if (manual) sumarPuntos(1);
        } else fijarPieza();
        return true;
    }

    /** Baja hasta el primer obstáculo y fija la pieza; suma dos puntos por celda recorrida. */
    public int caer() {
        if (estado != Estado.JUGANDO) return 0;
        int distancia = 0;
        while (cabe(activa, giro, fila + 1, columna)) { fila++; distancia++; }
        sumarPuntos(2L * distancia);
        fijarPieza();
        return distancia;
    }

    public void pausar() { if (estado == Estado.JUGANDO) estado = Estado.PAUSADO; }
    public void reanudar() { if (estado == Estado.PAUSADO) estado = Estado.JUGANDO; }

    private Tetromino sacarPieza() {
        if (bolsa.isEmpty()) {
            List<Tetromino> piezas = new ArrayList<>(Arrays.asList(Tetromino.values()));
            Collections.shuffle(piezas, aleatorio);
            bolsa.addAll(piezas);
        }
        return bolsa.removeFirst();
    }

    private void generarPieza() {
        activa = siguiente;
        siguiente = sacarPieza();
        giro = 0;
        fila = 0;
        columna = (COLUMNAS - activa.getTamaño()) / 2;
        if (!cabe(activa, giro, fila, columna)) estado = Estado.TERMINADO;
    }

    private boolean cabe(Tetromino tipo, int rotación, int filaBase, int columnaBase) {
        for (int f = 0; f < tipo.getTamaño(); f++) {
            for (int c = 0; c < tipo.getTamaño(); c++) {
                if (!tipo.ocupa(rotación, f, c)) continue;
                int y = filaBase + f, x = columnaBase + c;
                if (y < 0 || y >= FILAS || x < 0 || x >= COLUMNAS || celdas[y][x] != null) return false;
            }
        }
        return true;
    }

    private void fijarPieza() {
        for (int f = 0; f < activa.getTamaño(); f++)
            for (int c = 0; c < activa.getTamaño(); c++)
                if (activa.ocupa(giro, f, c)) celdas[fila + f][columna + c] = activa;
        int eliminadas = limpiarLíneas();
        int[] puntos = {0, 100, 300, 500, 800};
        sumarPuntos((long) puntos[eliminadas] * getNivel());
        líneas += eliminadas;
        generarPieza();
    }

    private int limpiarLíneas() {
        int destino = FILAS - 1;
        int eliminadas = 0;
        for (int origen = FILAS - 1; origen >= 0; origen--) {
            boolean completa = true;
            for (int c = 0; c < COLUMNAS; c++) if (celdas[origen][c] == null) { completa = false; break; }
            if (completa) eliminadas++;
            else {
                System.arraycopy(celdas[origen], 0, celdas[destino], 0, COLUMNAS);
                destino--;
            }
        }
        while (destino >= 0) Arrays.fill(celdas[destino--], null);
        return eliminadas;
    }

    private void sumarPuntos(long cantidad) {
        puntaje = (int) Math.min(Integer.MAX_VALUE, puntaje + cantidad);
    }

    public Tetromino getCelda(int fila, int columna) { return celdas[fila][columna]; }
    public Estado getEstado() { return estado; }
    public Tetromino getActiva() { return activa; }
    public Tetromino getSiguiente() { return siguiente; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    public int getGiro() { return giro; }
    public int getPuntaje() { return puntaje; }
    public int getLíneas() { return líneas; }
    public int getNivel() { return 1 + líneas / 10; }
    public int getIntervalo() { return Math.max(100, 800 - Math.min(12, getNivel() - 1) * 60); }
}
