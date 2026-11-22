package records;

import core.Stat;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/** Clasificación estable por juego. Las operaciones de archivos se invocan fuera del hilo Swing. */
public final class RecordsManager {
    private final FileManager archivos;
    private final Object operaciones = new Object();
    private final Map<String, String> juegos = new LinkedHashMap<>();
    private final Map<String, List<Stat>> resultados = new HashMap<>();
    private final Map<String, String> errores = new HashMap<>();
    private final Set<String> cargados = new HashSet<>();
    private final Set<String> pendientes = new HashSet<>();
    private final List<Runnable> observadores = new CopyOnWriteArrayList<>();

    public RecordsManager(FileManager archivos) { this.archivos = Objects.requireNonNull(archivos); }

    public synchronized void registrarJuego(String id, String nombre) {
        archivos.rutaArchivo(id);
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Falta el nombre del juego.");
        juegos.put(id, nombre);
        resultados.computeIfAbsent(id, k -> new ArrayList<>());
    }

    public synchronized Map<String, String> getJuegos() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(juegos));
    }

    public FileManager getArchivos() { return archivos; }

    public void cargarTodos() {
        for (String id : getJuegos().keySet()) {
            try { actualizar(id); } catch (IOException e) { /* El error queda disponible por juego. */ }
        }
    }

    /** Reintenta primero un guardado pendiente; nunca lo sustituye por datos antiguos del disco. */
    public void actualizar(String id) throws IOException {
        synchronized (operaciones) {
            boolean guardar;
            List<Stat> datos;
            synchronized (this) {
                validarJuego(id);
                guardar = pendientes.contains(id);
                datos = obtenerTop3(id);
            }
            try {
                if (guardar) archivos.guardar(id, datos);
                else {
                    datos = archivos.cargar(id);
                    validarMismaClave(datos);
                    datos.sort(Comparator.comparingInt(Stat::getValor).reversed());
                }
                synchronized (this) {
                    if (guardar) pendientes.remove(id);
                    else { resultados.put(id, datos); cargados.add(id); }
                    errores.remove(id);
                }
            } catch (IOException e) {
                synchronized (this) {
                    if (!pendientes.contains(id)) cargados.remove(id);
                    errores.put(id, e.getMessage());
                }
                throw e;
            } finally { notificar(); }
        }
    }

    public void registrarResultado(String id, Stat resultado) throws IOException {
        FileManager.validarStat(resultado);
        Stat copia = copiar(resultado);
        synchronized (operaciones) {
            boolean necesitaCarga;
            synchronized (this) { validarJuego(id); necesitaCarga = !cargados.contains(id); }
            // Una carga fallida no autoriza sobrescribir el archivo con una lista vacía.
            if (necesitaCarga) actualizar(id);
            List<Stat> anteriores = obtenerTop3(id);
            if (!anteriores.isEmpty() && !anteriores.get(0).getClave().equals(copia.getClave())) {
                throw new IllegalArgumentException("Las partidas del mismo juego deben utilizar la misma estadística.");
            }
            List<Stat> nuevos = new ArrayList<>(anteriores);
            nuevos.add(copia);
            nuevos.sort(Comparator.comparingInt(Stat::getValor).reversed());
            if (nuevos.size() > 3) nuevos.remove(3);
            if (iguales(anteriores, nuevos)) return;
            synchronized (this) { resultados.put(id, nuevos); pendientes.add(id); }
            try {
                archivos.guardar(id, nuevos);
                synchronized (this) { pendientes.remove(id); errores.remove(id); }
            } catch (IOException e) {
                synchronized (this) { errores.put(id, e.getMessage()); }
                throw e;
            } finally { notificar(); }
        }
    }

    public synchronized List<Stat> obtenerTop3(String id) {
        validarJuego(id);
        List<Stat> copias = new ArrayList<>();
        for (Stat stat : resultados.get(id)) copias.add(copiar(stat));
        return Collections.unmodifiableList(copias);
    }

    public synchronized Stat obtenerMejorResultado(String id) {
        List<Stat> top = obtenerTop3(id);
        return top.isEmpty() ? null : top.get(0);
    }

    public synchronized String getError(String id) { validarJuego(id); return errores.get(id); }
    public synchronized boolean tieneGuardadoPendiente(String id) { validarJuego(id); return pendientes.contains(id); }
    public synchronized boolean hayGuardadosPendientes() { return !pendientes.isEmpty(); }
    public void agregarObservador(Runnable observador) { observadores.add(Objects.requireNonNull(observador)); }
    public void quitarObservador(Runnable observador) { observadores.remove(observador); }

    private void notificar() { for (Runnable observador : observadores) observador.run(); }
    private void validarJuego(String id) {
        if (!juegos.containsKey(id)) throw new IllegalArgumentException("Juego no registrado: " + id);
    }
    private static Stat copiar(Stat s) { return new Stat(s.getClave(), s.getNombre(), s.getValor()); }
    private static boolean iguales(List<Stat> a, List<Stat> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            Stat x = a.get(i), y = b.get(i);
            if (x.getValor() != y.getValor() || !x.getNombre().equals(y.getNombre())
                    || !x.getClave().equals(y.getClave())) return false;
        }
        return true;
    }
    private static void validarMismaClave(List<Stat> datos) throws IOException {
        for (Stat stat : datos) {
            if (!stat.getClave().equals(datos.get(0).getClave()))
                throw new IOException("El archivo mezcla estadísticas diferentes.");
        }
    }
}
