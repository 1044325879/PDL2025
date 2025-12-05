package practicaPDL;

import java.io.*;
import java.util.*;

public class AnalizadorLexico {

    private static List<String> errorList = new ArrayList<>();
    public static boolean correcto = true;
    
    private static final int INT_MAX=32767;
    private static final double REAL_MAX=117549436.0;

    public AnalizadorLexico(String ficheroEntrada) {

        String ficheroToken = "token.txt";
        String ficheroTabla = "tablaSimbolo.txt";
        String ficheroError = "error.txt";
        String ficheroTabla2 = "tablaSimbolo2.txt";
        String ficheroTokenLinea = "tokenLinea.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(ficheroEntrada))) {
            //lista para tokens
        	List<Token> tokens = new ArrayList<>();
            //lista para saber linea que esta cada token
        	List<Integer> tokenLineas = new ArrayList<>();
        	//map que contiene datos de tabla de simbolo
            Map<String, Integer> tablaSimbolo = new HashMap<>();
            Map<Integer, String> tablaSimbolo2 = new LinkedHashMap<>();
            //para identificador
            int id = 1;
            //num tabla de simbolo
            int numTabla = 1;
            //para situar la linea
            int numLinea = 0;
            
            //conjunto de palabras reservadas del MyJS
            Set<String> reservadas = new HashSet<>(Arrays.asList(
                "boolean","break","case","float","function","if","int","let",
                "read","return","string","switch","void","write"
            ));
            //contenido de palabras que hay en esta linea
            String line;
            //hasta que no llegamos al final
            while ((line = br.readLine()) != null) {
            	//situamos en linea actual
                numLinea++;
                /*
                // para comentarios
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                    if (line.trim().isEmpty()) continue;
                }*/
                int pos = line.indexOf("//");
                if (pos != -1) {
                    String before = line.substring(0, pos);
                    long countQuotes = before.chars().filter(ch -> ch == '"').count();
                    if (countQuotes % 2 == 0) {
                        line = before;
                        if (line.trim().isEmpty()) continue;
                    }
                }

                // para separar los signos y las palabras
                //String regex = "(?=[(){};,:+=!|\"])|(?<=[(){};,:+=!|\"])";
                String regex = "(?=[(){};,:+=!|])|(?<=[(){};,:+=!|])";
                String[] palabras = line.split("\\s+|" + regex);
                
                for (int i = 0; i < palabras.length; i++) {
                    String palabra = palabras[i].trim();
                    if (palabra.isEmpty()) continue;
                    //operadores especiales
                    // ++
                    if(i+1 < palabras.length && palabra.equals("+")&&palabras[i+1].equals("+")) {
                    	tokens.add(Token.AUTOINCREMENTO);//++
                    	tokenLineas.add(numLinea);
                    	i++;
                    	continue;
                    }
                    
                    //!=
                    if(i+1 < palabras.length && palabra.equals("!")&&palabras[i+1].equals("=")) {
                    	tokens.add(Token.DISTINTO);//!=
                    	tokenLineas.add(numLinea);
                    	i++;
                    	continue;
                    }
                    
                    //||
                    if(i+1 < palabras.length && palabra.equals("|")&&palabras[i+1].equals("|")) {
                    	tokens.add(Token.O_LOGICO);//||
                    	tokenLineas.add(numLinea);
                    	i++;
                    	continue;
                    }
                    
                    // otros operadores
                    if (palabra.equals("+")) {
                        tokens.add(Token.SUMA); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals("=")) {
                        tokens.add(Token.IGUAL); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals(",")) {
                        tokens.add(Token.COMA); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals(";")) {
                        tokens.add(Token.PUNTO_COMA); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals(":")) {
                        tokens.add(Token.DOS_PUNTOS); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals("(")) {
                        tokens.add(Token.PARENTESISA); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals(")")) {
                        tokens.add(Token.PARENTESISC); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals("{")) {
                        tokens.add(Token.LLAVEA); 
                        tokenLineas.add(numLinea);
                        continue;
                    }
                    if (palabra.equals("}")) {
                        tokens.add(Token.LLAVEC); 
                        tokenLineas.add(numLinea);
                        continue;
                    }

                    //constante real
                    //d+ . d+
                    if(palabra.matches("\\d+\\.\\d+")) {
                    	try {
                    		//transformar a double
                    		double valor=Double.parseDouble(palabra);
                    		if(valor<=REAL_MAX) {
                    			tokens.add(new Token("constr", palabra));
                    		}else {
                    			reportError(numLinea, "Error: constante real fuera del rango " + palabra);
                    		}
                    	}catch (NumberFormatException ex) {
                            reportError(numLinea, "Error: real inválido " + palabra);
                        }
                    	continue;
                    }
                    
                    // constante entera
                    //d+
                    if (palabra.matches("\\d+")) {
                    	try {
                    		int value = Integer.parseInt(palabra);
                            if (value >INT_MAX || value< -INT_MAX) {
                                reportError(numLinea, "Error: constante entera fuera del rango " + palabra);
                            }
                            tokens.add(new Token("conste", palabra));
                            tokenLineas.add(numLinea);
                    	}catch (NumberFormatException ex) {
                            reportError(numLinea, "Error: entera inválida " + palabra);
                        }
                    	continue;
                    }
  
                    // cadena
                    if (palabra.startsWith("\"")) {
                        StringBuilder sb = new StringBuilder();
                        int contador = 0;
                        //quitar la primera "
                        String frag = palabra.substring(1);
                        if (palabra.endsWith("\"") && palabra.length() > 1) {
                            // caso "..."
                            String contenido = frag.substring(0, frag.length() - 1).trim();
                            contador = contenido.length();
                            if (contador <= 64) {
                                tokens.add(new Token("cadena", "\"" + contenido + "\""));
                                tokenLineas.add(numLinea);
                            } else {
                                reportError(numLinea, "Error: Longitud de cadena excedida (>64)");
                            }
                        } else {
                            sb.append(frag);
                            contador += frag.length();
                            boolean cerrado = false;
                            while (i + 1 < palabras.length) {
                                String siguiente = palabras[++i];
                                if (siguiente.endsWith("\"")) {
                                    // último fragmento
                                    String cuerpo = siguiente.substring(0, siguiente.length() - 1);
                                    contador += 1 /*espacio*/ + cuerpo.length();
                                    if (!cuerpo.isEmpty() && ".,;:!?".indexOf(cuerpo.charAt(0)) >= 0) {
                                        sb.append(cuerpo);
                                    } else {
                                        sb.append(" ").append(cuerpo);
                                    }
                                    cerrado = true;
                                    break;
                                } else {
                                    contador += 1 /*espacio*/ + siguiente.length();
                                    if (!siguiente.isEmpty() && ".,;:!?".indexOf(siguiente.charAt(0)) >= 0) {
                                        sb.append(siguiente);          // 不加空格
                                    } else {
                                        sb.append(" ").append(siguiente);
                                    }
                                }
                            }
                            if (!cerrado) {
                                reportError(numLinea, "Error: cadena sin comillas de cierre");
                            } else {
                            	String contenidoFinal = sb.toString().trim();
                                if (contador <= 64) {
                                    tokens.add(new Token("cadena", "\"" + contenidoFinal + "\""));
                                    tokenLineas.add(numLinea);
                                } else {
                                    reportError(numLinea, "Error: Longitud de cadena excedida (>64)");
                                }
                            }
                        }
                        continue;
                    }

                    // otros elementos
                    switch (palabra) {
                        case "boolean": 
                        	tokens.add(Token.BOOLEAN);
                        	tokenLineas.add(numLinea);continue;
                        case "break": 
                        	tokens.add(Token.BREAK); 
                        	tokenLineas.add(numLinea);continue;
                        case "case": 
                        	tokens.add(Token.CASE); 
                        	tokenLineas.add(numLinea);continue;
                        case "float": 
                        	tokens.add(Token.FLOAT); 
                        	tokenLineas.add(numLinea);continue;
                        case "function": 
                        	tokens.add(Token.FUNCTION); 
                        	tokenLineas.add(numLinea);continue;
                        case "if": 
                        	tokens.add(Token.IF); 
                        	tokenLineas.add(numLinea);continue;
                        case "int": 
                        	tokens.add(Token.INT); 
                        	tokenLineas.add(numLinea);continue;
                        case "let": 
                        	tokens.add(Token.LET); 
                        	tokenLineas.add(numLinea);continue;
                        case "read": 
                        	tokens.add(Token.READ); 
                        	tokenLineas.add(numLinea);continue;
                        case "return": 
                        	tokens.add(Token.RETURN); 
                        	tokenLineas.add(numLinea);continue;
                        case "string": 
                        	tokens.add(Token.STRING); 
                        	tokenLineas.add(numLinea);continue;
                        case "switch": 
                        	tokens.add(Token.SWITCH); 
                        	tokenLineas.add(numLinea);continue;
                        case "write":
                        	tokens.add(Token.WRITE); 
                        	tokenLineas.add(numLinea);continue;
                        case "void": 
                        	tokens.add(Token.VOID); 
                        	tokenLineas.add(numLinea);continue;
                    }

                    // id
                    if (palabra.matches("[A-Za-z_][A-Za-z0-9_]*")) {
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
                bw.write(token.toString());
                bw.newLine();
            }
            bw.close();

            // escribir en tablaSimbolo.txt
            BufferedWriter bw2 = new BufferedWriter(new FileWriter(ficheroTabla));
            bw2.write("CONTENIDOS DE LA TABLA #" + numTabla + ":\n");
            int despl = 0;
            for (Map.Entry<Integer, String> entry : tablaSimbolo2.entrySet()) {
                String lexema = entry.getValue();
                String tipo = "entero"; 
                bw2.write("* LEXEMA : '" + lexema + "'\n");
                bw2.write("  Atributos :\n");
                bw2.write("  +tipo: (esto es de tipo int) '" + tipo + "'\n");
                bw2.write("  +despl: " + despl + "\n");
                bw2.write("  ------------------\n");
                bw2.newLine();
                despl += 4;
            }
            bw2.close();
            
            // escribir en tablaSimbolo2.txt
            BufferedWriter bw3 = new BufferedWriter(new FileWriter(ficheroTabla2));
            for (Map.Entry<Integer, String> entry : tablaSimbolo2.entrySet()) {
                bw3.write(entry.getKey() + "=" + entry.getValue());
                bw3.newLine();
            }
            bw3.close();

            // escribir en error.txt
            if (!errorList.isEmpty()) {
                BufferedWriter bw4 = new BufferedWriter(new FileWriter(ficheroError));
                for (String error : errorList) {
                    bw4.write(error);
                    bw4.newLine();
                }
                bw4.close();
            }
            
            //escribir en tokenlineas
            BufferedWriter bw5 = new BufferedWriter(new FileWriter(ficheroTokenLinea));
            for (int linea : tokenLineas) {
                bw5.write(Integer.toString(linea));
                bw5.newLine();
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

