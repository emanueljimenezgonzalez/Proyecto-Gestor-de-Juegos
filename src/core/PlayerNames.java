package core;

/** Una misma validación para los juegos y el almacenamiento, sin restringir el alfabeto. */
public final class PlayerNames {
    public static final int MAX_LENGTH = 120;
    private PlayerNames() { }

    public static String validar(String texto) {
        if (texto == null) throw new IllegalArgumentException("Escribe el nombre del jugador.");
        int inicio = 0, fin = texto.length();
        while (inicio < fin && espacio(texto.codePointAt(inicio))) inicio += Character.charCount(texto.codePointAt(inicio));
        while (fin > inicio && espacio(texto.codePointBefore(fin))) fin -= Character.charCount(texto.codePointBefore(fin));
        String nombre = texto.substring(inicio, fin);
        if (nombre.isEmpty()) throw new IllegalArgumentException("Escribe el nombre del jugador.");
        if (nombre.length() > MAX_LENGTH)
            throw new IllegalArgumentException("El nombre admite un máximo de 120 caracteres.");
        if (nombre.codePoints().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Escribe el nombre en una sola línea, sin tabulaciones.");
        return nombre;
    }

    private static boolean espacio(int carácter) {
        return Character.isWhitespace(carácter) || Character.isSpaceChar(carácter);
    }
}
