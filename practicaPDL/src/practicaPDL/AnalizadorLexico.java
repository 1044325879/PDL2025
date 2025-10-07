package practicaPDL;

import java.io.*;
import java.util.*;

public class AnalizadorLexico {

    private static List<String> errorList = new ArrayList<>();
    public static boolean correcto = true;

    public AnalizadorLexico(String ficheroEntrada) {

        String ficheroToken = "token.txt";
        String ficheroTabla = "tablaSimbolo.txt";
        String ficheroError = "error.txt";
        String ficheroTabla2 = "tablaSimbolo2.txt";
        String ficheroTokenLinea = "tokenLinea.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(ficheroEntrada))) {
            List<Token> tokens = new ArrayList<>();
            List<Integer> tokenLineas = new ArrayList<>();
            Map<String, Integer> tablaSimbolo = new HashMap<>();
            Map<Integer, String> tablaSimbolo2 = new LinkedHashMap<>();
            int id = 1;
            int numTabla = 1;
            int numLinea = 0;
            String line;
            while ((line = br.readLine()) != null) {
                numLinea++;

                // para comentarios
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                    if (line.trim().isEmpty()) continue;
                }

                // para separar los signos y las palabras
                String regex ="(?=[(){};,+=!|?])|(?<=[(){};,+=!|?])";
                String[] palabras = line.split("\\s+|" + regex);
                for (int i = 0; i < palabras.length; i++) {
                    String palabra = palabras[i].trim();
                    if (palabra.isEmpty()) continue;

                    // output/input
                    if (palabra.equals("output") || palabra.equals("input")) {
                        if (palabra.equals("output")) {
                        	tokens.add(Token.OUTPUT);
                        	tokenLineas.add(numLinea);
                        }
                        else {
                        	tokens.add(Token.INPUT);
                        	tokenLineas.add(numLinea);
                        }

                        if (i + 1 < palabras.length && palabras[i + 1].startsWith("\"")) {
                            String texto = palabras[++i];
                            boolean cerrado = false;
                            if (texto.endsWith("\"")) {
                                String contenido = "";
                                if (texto.length() > 1) {
                                    contenido = texto.substring(1, texto.length() - 1);
                                }
                                tokens.add(new Token("cadena", "\"" + contenido + "\""));
                                tokenLineas.add(numLinea);
                                cerrado = true;
                            } else {
                                StringBuilder sb = new StringBuilder(texto.substring(1));
                                while (i + 1 < palabras.length && !palabras[i + 1].endsWith("\"")) {
                                    sb.append(" ").append(palabras[++i]);
                                }
                                if (i + 1 < palabras.length) {
                                    sb.append(" ").append(palabras[++i], 0, palabras[i].length() - 1);
                                    tokens.add(new Token("cadena", "\""+sb.toString()+"\""));
                                    tokenLineas.add(numLinea);
                                    cerrado = true;
                                }
                            }
                            if (!cerrado) {
                                reportError(numLinea, "Error: cadena sin comillas de cierre");
                            }
                        }
                        continue;
                    }

                    // operadores especiales
                    if (i + 1 < palabras.length && palabra.equals("+") && palabras[i + 1].equals("=")) {
                        tokens.add(Token.OPERADOR1);  // +=
                        tokenLineas.add(numLinea);
                        i++;
                        continue;
                    } else if (i + 1 < palabras.length && palabra.equals("!") && palabras[i + 1].equals("=")) {
                        tokens.add(Token.DISTINTO);   // !=
                        tokenLineas.add(numLinea);
                        i++;
                        continue;
                    } else if (i + 1 < palabras.length && palabra.equals("|") && palabras[i + 1].equals("|")) {
                        tokens.add(Token.O_LOGICO);   // ||
                        tokenLineas.add(numLinea);
                        i++;
                        continue;
                    }
                    // otros operadores
                    else if (palabra.equals("+")) {
                        tokens.add(Token.SUMA); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals("=")) {
                        tokens.add(Token.OPERADOR2); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals(",")) {
                        tokens.add(Token.COMA); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals(";")) {
                        tokens.add(Token.PUNTOCOMA); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals("(")) {
                        tokens.add(Token.PARENTESISA); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals(")")) {
                        tokens.add(Token.PARENTESISC); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals("{")) {
                        tokens.add(Token.LLAVEA); 
                        tokenLineas.add(numLinea);continue;
                    } else if (palabra.equals("}")) {
                        tokens.add(Token.LLAVEC); 
                        tokenLineas.add(numLinea);continue;
                    }

                    // constante entera
                    if (palabra.matches("\\d+")) {
                        int value = Integer.parseInt(palabra);
                        if (value > 32767 || value < -32768) {
                            reportError(numLinea, "Error: constante entera fuera del rango " + palabra);
                        }
                        tokens.add(new Token("constante_entera", palabra));
                        tokenLineas.add(numLinea);
                        continue;
                    }

                    // cadena
                    if (palabra.startsWith("\"")) {
                        StringBuilder sb = new StringBuilder(palabra.substring(1));
                        boolean cerrado = false;
                        if (palabra.endsWith("\"") && palabra.length() > 1) {
                            tokens.add(new Token("cadena", "\""+sb.substring(0, sb.length() - 1)+"\""));
                            tokenLineas.add(numLinea);
                            cerrado = true;
                        } else {
                            while (i + 1 < palabras.length && !palabras[i + 1].endsWith("\"")) {
                                sb.append(" ").append(palabras[++i]);
                            }
                            if (i + 1 < palabras.length) {
                                sb.append(" ").append(palabras[++i], 0, palabras[i].length() - 1);
                                cerrado = true;
                            }
                        }
                        if (cerrado) {
                            tokens.add(new Token("cadena", "\""+sb.toString()+"\""));
                            tokenLineas.add(numLinea);
                        } else {
                            reportError(numLinea, "Error: cadena sin comillas de cierre");
                        }
                        continue;
                    }

                    // otros elementos
                    switch (palabra) {
                        case "boolean": 
                        	tokens.add(Token.BOOLEAN);
                        	tokenLineas.add(numLinea);continue;
                        case "for": 
                        	tokens.add(Token.FOR); 
                        	tokenLineas.add(numLinea);continue;
                        case "function": 
                        	tokens.add(Token.FUNCTION); 
                        	tokenLineas.add(numLinea);continue;
                        case "if": 
                        	tokens.add(Token.IF); 
                        	tokenLineas.add(numLinea);continue;
                        case "input": 
                        	tokens.add(Token.INPUT); 
                        	tokenLineas.add(numLinea);continue;
                        case "int": 
                        	tokens.add(Token.INT); 
                        	tokenLineas.add(numLinea);continue;
                        case "output": 
                        	tokens.add(Token.OUTPUT); 
                        	tokenLineas.add(numLinea);continue;
                        case "return": 
                        	tokens.add(Token.RETURN); 
                        	tokenLineas.add(numLinea);continue;
                        case "string": 
                        	tokens.add(Token.STRING); 
                        	tokenLineas.add(numLinea);continue;
                        case "var":
                        	tokens.add(Token.VAR); 
                        	tokenLineas.add(numLinea);continue;
                        case "void": 
                        	tokens.add(Token.VOID); 
                        	tokenLineas.add(numLinea);continue;
                    }

                    // id
                    if (palabra.matches("[a-zA-Z][a-zA-Z0-9_]*")) {
                        int identificador;
                        if (tablaSimbolo.containsKey(palabra)) {
                            identificador = tablaSimbolo.get(palabra);
                        } else {
                            identificador = id++;
                            tablaSimbolo.put(palabra, identificador);
                            tablaSimbolo2.put(identificador, palabra);
                        }
                        tokens.add(new Token("id", identificador));
                        tokenLineas.add(numLinea);
                        continue;
                    }

                    // resto -> error
                    reportError(numLinea, "Símbolo no permitido o identificador inválido: " + palabra);
                }
            }
            br.close();

            // escribir en token.txt
            BufferedWriter bw = new BufferedWriter(new FileWriter(ficheroToken));
            for (Token token : tokens) {
                bw.write(token.toString() + "\n");
            }
            bw.close();

            // escribir en tablaSimbolo.txt
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(ficheroTabla));
            bw2.write("CONTENIDOS DE LA TABLA #" + numTabla + ":\n");
            int despl = 0;
            for (Map.Entry<Integer, String> entry : tablaSimbolo2.entrySet()) {
                String lexema = entry.getValue();
                String tipo = "entero"; 
                bw2.write("*'" + lexema + "'\n");
                bw2.write("+tipo:'" + tipo + "'\n");
                bw2.write("+despl:" + despl + "\n\n");
                despl += 4;
            }
            bw2.close();
            
            // escribir en tablaSimbolo2.txt
            BufferedWriter bw3 = new BufferedWriter(new FileWriter(ficheroTabla2));
            for (Map.Entry<Integer, String> entry : tablaSimbolo2.entrySet()) {
                bw3.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
            bw3.close();

            // escribir en error.txt
            if (!errorList.isEmpty()) {
                BufferedWriter bw4 = new BufferedWriter(new FileWriter(ficheroError));
                for (String error : errorList) {
                    bw4.write(error + "\n");
                }
                bw4.close();
            }
            
            //escribir en tokenlineas
            BufferedWriter bw5 = new BufferedWriter(new FileWriter(ficheroTokenLinea));
            for (int linea : tokenLineas) {
                bw5.write(linea + "\n");
            }
            bw5.close();

        } catch (FileNotFoundException e1) {
            System.err.println("NO EXISTE EL FICHERO");
        } catch (IOException e2) {
            System.err.println("ERROR AL LEER DEL FICHERO");
        }
    }

    private static void reportError(int lineNumber, String message) {
        String errorMessage = "Linea " + lineNumber + " " + message;
        errorList.add(errorMessage);
        System.err.println(errorMessage);
        correcto = false;
    }
}

