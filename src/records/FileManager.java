package records;

import core.Stat;
import core.PlayerNames;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Almacena hasta tres récords por juego, sin serialización de objetos Java. */
public final class FileManager {
    private static final int MAGIC = 0x41524341;
    private static final int VERSION = 1;
    private final Path directorio;

    public FileManager(Path directorio) {
        this.directorio = Objects.requireNonNull(directorio).toAbsolutePath().normalize();
    }

    public Path getDirectorio() { return directorio; }

    public Path rutaArchivo(String juegoId) {
        if (juegoId == null || !juegoId.matches("[a-z][a-z0-9_]{0,39}")) {
            throw new IllegalArgumentException("Identificador de juego inválido.");
        }
        return directorio.resolve(juegoId + ".dat");
    }

    public List<Stat> cargar(String juegoId) throws IOException {
        Path archivo = rutaArchivo(juegoId);
        try (DataInputStream entrada = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(archivo)))) {
            if (entrada.readInt() != MAGIC || entrada.readInt() != VERSION) {
                throw new IOException("Formato de récords no reconocido.");
            }
            int cantidad = entrada.readInt();
            if (cantidad < 0 || cantidad > 3) throw new IOException("Cantidad de récords inválida.");
            List<Stat> resultados = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                Stat stat = new Stat(entrada.readUTF(), entrada.readUTF(), entrada.readInt());
                try { validarStat(stat); }
                catch (IllegalArgumentException e) { throw new IOException("Registro inválido.", e); }
                resultados.add(stat);
            }
            if (entrada.read() != -1) throw new IOException("El archivo contiene datos inesperados.");
            return resultados;
        } catch (NoSuchFileException e) {
            // Solo la ausencia es un primer uso; permisos y corrupción se propagan.
            return new ArrayList<>();
        } catch (EOFException e) {
            throw new IOException("El archivo de récords está incompleto.", e);
        }
    }

    public void guardar(String juegoId, List<Stat> datos) throws IOException {
        Path archivo = rutaArchivo(juegoId);
        Objects.requireNonNull(datos);
        if (datos.size() > 3) throw new IllegalArgumentException("Solo se guardan tres récords por juego.");
        for (Stat stat : datos) validarStat(stat);
        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(directorio, juegoId + "-", ".tmp");
        try {
            try (DataOutputStream salida = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temporal)))) {
                salida.writeInt(MAGIC);
                salida.writeInt(VERSION);
                salida.writeInt(datos.size());
                for (Stat stat : datos) {
                    salida.writeUTF(stat.getClave());
                    salida.writeUTF(stat.getNombre());
                    salida.writeInt(stat.getValor());
                }
            }
            try {
                reemplazar(temporal, archivo);
            } catch (AtomicMoveNotSupportedException e) {
                // No reemplazar de manera insegura si el sistema de archivos no lo admite.
                throw new IOException("La ubicación no permite reemplazar los récords de forma segura.", e);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    private static void reemplazar(Path temporal, Path archivo) throws IOException {
        for (int intento = 0; ; intento++) {
            try {
                Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (AccessDeniedException e) {
                if (intento == 3) throw e;
                // Un bloqueo breve en Windows no debe hacer perder un guardado; los fallos persistentes se informan.
                try { Thread.sleep(25L * (intento + 1)); }
                catch (InterruptedException interrumpida) {
                    Thread.currentThread().interrupt();
                    throw new IOException("El guardado fue interrumpido.", interrumpida);
                }
            }
        }
    }

    static void validarStat(Stat stat) {
        if (stat == null || stat.getClave() == null || stat.getClave().isBlank()
                || stat.getNombre() == null || stat.getNombre().isBlank()
                || stat.getClave().length() > 80 || stat.getNombre().length() > 120) {
            throw new IllegalArgumentException("El resultado necesita una estadística y un jugador válidos (máximo 80 y 120 caracteres).");
        }
        PlayerNames.validar(stat.getNombre());
    }
}
