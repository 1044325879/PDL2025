package practicaPDL;

import java.io.*;
import java.util.*;

public class AnalizadorSemantico {
	
	//para token-atributo
    public static class Simbolo {
        String nombre;
        Map<String, String> atributos = new LinkedHashMap<>();

        public Simbolo(String nombre) {
            this.nombre = nombre;
        }
    }

    private final Map<String, Map<String, Simbolo>> tablas = new LinkedHashMap<>();
    private String ambitoActual = "global";
    private int desplazar = 0;
    private int paramCounter = 1;
    private final Map<String, Integer> desplLocal = new HashMap<>();
    private List<String> tipos = null;

    public AnalizadorSemantico() {
        tablas.put("global", new LinkedHashMap<>());
    }

    public void iniciarFuncion(String nombre, String tipoRetorno, List<String> tiposParams) {
        ambitoActual = nombre;
        tablas.put(nombre, new LinkedHashMap<>());
        desplLocal.put(nombre, 0);

        //parametros de tabla
        Simbolo f = new Simbolo(nombre);
        f.atributos.put("Tipo", "function");
        f.atributos.put("Despl", "-");
        f.atributos.put("numParam", String.valueOf(tiposParams.size()));
        for (int i = 0; i < tiposParams.size(); i++) {
            f.atributos.put("TipoParam" + (i+1), tiposParams.get(i));
            f.atributos.put("ModoParam" + (i+1), "valor");
        }
        f.atributos.put("TipoRetorno", tipoRetorno);
        f.atributos.put("EtiqFuncion", nombre);
        tablas.get("global").put(nombre, f);

        tipos = tiposParams;
        paramCounter = 1;
    }

    public void terminarFuncion() {
        ambitoActual = "global";
        tipos = null;
    }
    
    //para variable
    public void declararVariable(String nombre, String tipo) {
        int tamano = getTamanoTipo(tipo);
        int despl = ambitoActual.equals("global") ? desplazar : desplLocal.get(ambitoActual);
        Simbolo s = new Simbolo(nombre);
        s.atributos.put("Tipo", tipo);
        s.atributos.put("Despl", String.valueOf(despl));
        tablas.get(ambitoActual).put(nombre, s);
        if (ambitoActual.equals("global")) desplazar += tamano;
        else desplLocal.put(ambitoActual, despl + tamano);
    }

    //parametro
    public void declararParametro(String nombre, String tipo) {
        int tamano = getTamanoTipo(tipo);
        int despl = desplLocal.get(ambitoActual);
        Simbolo s = new Simbolo(nombre);
        s.atributos.put("Tipo", tipo);
        s.atributos.put("Despl", String.valueOf(despl));
        s.atributos.put("Param", "Sí");
        s.atributos.put("ModoParam" + paramCounter, "valor");
        tablas.get(ambitoActual).put(nombre, s);
        desplLocal.put(ambitoActual, despl + tamano);
        paramCounter++;
    }

    //crear tabla simbolo
    public void volcar(String fichero) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fichero));
        int nTabla = 1;
        for (String ambito : tablas.keySet()) {
            bw.write("CONTENIDOS DE LA TABLA #" + nTabla + " (" + ambito + "):\n");
            for (Simbolo s : tablas.get(ambito).values()) {
                bw.write("*'" + s.nombre + "'\n");
                for (Map.Entry<String, String> entry : s.atributos.entrySet()) {
                    bw.write("+" + entry.getKey() + ":'" + entry.getValue() + "'\n");
                }
                bw.write("\n");
            }
            nTabla++;
        }
        bw.close();
    }

    
    //tipo de desp
    private int getTamanoTipo(String tipo) {
        switch (tipo) {
            case "int":
            case "boolean":
            case "void":
                return 1;
            case "string":
                return 64;
            default:
                return 1;
        }
    }
}
