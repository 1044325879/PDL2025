package practicaPDL;

import java.io.*;
import java.util.*;

public class GestorError {
    private static final List<String> errores = new ArrayList<>();

    //mensaje error
    public static void agregar(int linea, String tipo, String mensaje) {
        errores.add("Línea " + linea + " [" + tipo + "]: " + mensaje);
    }

    public static void agregar(String tipo, String mensaje) {
        errores.add("[" + tipo + "]: " + mensaje);
    } 

    public static boolean hayErrores() {
        return !errores.isEmpty();
    }

    //añadir los mensajes de errores en fichero
    public static void volcar(String fichero) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichero))) {
            for (String err : errores) {
                bw.write(err);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error al escribir error.txt: " + e.getMessage());
        }
    }

    //vaciar el contenido
    public static void limpiar() {
        errores.clear();
    }
}
