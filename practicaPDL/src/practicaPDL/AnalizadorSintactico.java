package practicaPDL;

import java.io.*;
import java.util.*;

public class AnalizadorSintactico {

    private List<Token> tokens;
    private List<Integer> tokenLineas = new ArrayList<>();
    private int indice;
    private Token tokenActual;
    private BufferedWriter parseOut;
    private Map<Integer, String> idnombre = new HashMap<>();
    private AnalizadorSemantico sem = new AnalizadorSemantico();
    private String tipoActual = "";
    private String idActual = "";
    private int lineaActual = 1;

    public AnalizadorSintactico(String rutaToken) throws IOException {
        this.tokens = cargarTokens(rutaToken);
        this.indice = 0;
        this.tokenActual = tokens.get(indice);//obtener todos los tokens que hay en fichero
        this.parseOut = new BufferedWriter(new FileWriter("parse.txt"));
        this.parseOut.write("Descendente ");
        cargarIdNombre("tablaSimbolo2.txt");
        cargarTokenLineas("tokenLinea.txt");
    }
    
    private void cargarIdNombre(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String linea;
        while ((linea = br.readLine()) != null) {
            if (linea.contains("=")) {
                String[] partes = linea.split("=");
                int num = Integer.parseInt(partes[0].trim());
                String nombre = partes[1].trim();
                idnombre.put(num, nombre);
            }
        }
        br.close();
    }
    
    //metodo para cargar los tokens del fichero
    private List<Token> cargarTokens(String nombreFichero) throws IOException {
        List<Token> lista = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(nombreFichero));
        String linea;
        while ((linea = br.readLine()) != null) {
            linea = linea.replaceAll("[<>]", "");
            String[] partes = linea.split(",");
            String codigo = partes[0].trim();
            String atributo = partes.length > 1 ? partes[1].trim() : "-";
            try {
                int atributoI = Integer.parseInt(atributo);
                lista.add(new Token(codigo, atributoI));
            } catch (NumberFormatException e) {
                lista.add(new Token(codigo, atributo));
            }
        }
        //final de fichero
        lista.add(new Token("EOF", "-"));
        return lista;
    }
    
    private void cargarTokenLineas(String nombreFichero) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(nombreFichero));
        String linea;
        while ((linea = br.readLine()) != null) {
            tokenLineas.add(Integer.parseInt(linea.trim()));
        }
        br.close();
    }

    
    public void analizar() throws Exception {
        try {
        	//comienza por s
            S();
            if (!tokenActual.getCodigo().equals("EOF")) {
                //error("Tokens extra al final del programa. Último token: " + tokenActual);
            } else {
                //System.out.println("Análisis sintáctico correcto.");
            }
        } finally {
            parseOut.close();
            GestorError.volcar("error.txt");
            sem.volcar("tablaSimbolo.txt");
        }
    }

    private void avanzar() {
        if (indice < tokens.size() - 1) {
            indice++;
            tokenActual = tokens.get(indice);
            if (indice < tokenLineas.size()) {
                tokenActual.setLinea(tokenLineas.get(indice));
            }
            //System.out.println("Avanza a: " + tokenActual);
        }
    }
    
    private void emparejar(String esperado) {
        if (tokenActual.getCodigo().equals(esperado)) {
            avanzar();
        } else {
            error("Se esperaba: " + esperado + " pero se encontró: " + tokenActual.getCodigo());
        }
    }

    //para id, parentesis, clave y id
    private void emparejar(String esperado, int atributoI) {
        if (tokenActual.getCodigo().equals(esperado) && tokenActual.getAtributoI() == atributoI) {
            avanzar();
        } else {
            error("Se esperaba: " + esperado + " con atributo " + atributoI + " pero se encontró: " + tokenActual);
        }
    }

    //gestiona error
    private void error(String mensaje) {
        //System.err.println("Error sintáctico: " + mensaje);
        //GestorError.agregar("Sintáctico", mensaje);
        
        int errorLinea = tokenActual.getLinea();
        if (errorLinea > 0) {
            mensaje = "Error en la línea " + errorLinea + ": " + mensaje;
        }else {
        	mensaje="Error sintáctico: " + mensaje;
        }
        System.err.println(mensaje);
        GestorError.agregar("Sintáctico", mensaje);
    }

    private String getIdNombre() {
        return idnombre.get(tokenActual.getAtributoI());
    }
    
    private String extraerId(Token token) {
        if (token.getCodigo().equals("id")) {
            //return token.getAtributo();
            return idnombre.get(token.getAtributoI());
        } else {
            return "<desconocido>";
        }
    }

    private void S() throws IOException {
        parseOut.write("1 ");//S->A
        A();
    }

    private void A() throws IOException {
        if (esInicioDeB(tokenActual.getCodigo())) {
            parseOut.write("2 ");//A->BA
            B();
            A();
        } else {
            parseOut.write("3 ");//A->lambda
        }
    }

    private boolean esInicioDeB(String codigo) {
        return codigo.equals("var") || codigo.equals("if") || codigo.equals("for") ||
               codigo.equals("function") || codigo.equals("return") || codigo.equals("input") ||
               codigo.equals("output") || codigo.equals("id");
    }

    private void B() throws IOException {
        switch (tokenActual.getCodigo()) {
	        case "var": {
	            parseOut.write("4 ");//B->var C id B1
	            emparejar("var");
	            String tipo = C();
	            String nombreVar = getIdNombre();
	            emparejar("id");
	            sem.declararVariable(nombreVar, tipo);
	            B1();
	            break;
	        }

            case "if":
                parseOut.write("7 ");//B->if (E) F
                emparejar("if");
                emparejar("par", 1);
                E();
                emparejar("par", 2);
                F();
                break;
            case "for":
                parseOut.write("8 ");//B->for(G){H}
                emparejar("for");
                emparejar("par", 1);
                G();
                emparejar("par", 2);
                emparejar("lla", 1);
                H();
                emparejar("lla", 2);
                break;
            case "function":
                parseOut.write("9 ");//B->function C id (I){H}
                emparejar("function");
                tipoActual = tokenActual.getCodigo();
                String tipoFuncion = tokenActual.getCodigo();
                C();
                idActual = extraerId(tokenActual);
                String nombreFunc = extraerId(tokenActual);
                emparejar("id");
                emparejar("par", 1);
                List<String> parametrosTipo = new ArrayList<>();
                List<String> parametrosNombre = new ArrayList<>();
                I();
                sem.iniciarFuncion(nombreFunc, tipoFuncion, parametrosTipo);
                for (int i = 0; i < parametrosTipo.size(); i++)
                    sem.declararParametro(parametrosNombre.get(i), parametrosTipo.get(i));
                emparejar("par", 2);
                emparejar("lla", 1);
                H();
                sem.terminarFuncion();
                emparejar("lla", 2);
                break;
            default:
                parseOut.write("10 ");//B->F
                F();
                break;
        }
    }

    private void B1() throws IOException {
        if (tokenActual.getCodigo().equals("puntoComa")) {
            parseOut.write("5 ");//B1->;
            emparejar("puntoComa");
        } else if (tokenActual.getCodigo().equals("asig")) {
            parseOut.write("6 ");//B1->=D;
            emparejar("asig");
            D();
            emparejar("puntoComa");
        } else {
            error("Se esperaba ';' o '=' en declaración");
        }
    }

    private String C() throws IOException {
        String tipo = tokenActual.getCodigo();
        if (tipo.equals("int")) {
            parseOut.write("11 ");
            emparejar("int");
            return "int";
        } else if (tipo.equals("boolean")) {
            parseOut.write("12 ");
            emparejar("boolean");
            return "boolean";
        } else if (tipo.equals("string")) {
            parseOut.write("13 ");
            emparejar("string");
            return "string";
        } else if (tipo.equals("void")) {
            parseOut.write("14 ");
            emparejar("void");
            return "void";
        } else {
            error("Tipo no válido: " + tipo);
            return "indefinido";
        }
    }

    private void D() throws IOException {
        parseOut.write("15 "); // D->D2 D1
        D2();
        D1();
    }

    private void D1() throws IOException {
        if (tokenActual.getCodigo().equals("suma")) { // +
            parseOut.write("16 "); // D1-> + D2 D1
            avanzar();
            D2();
            D1();
        }else if (
                tokenActual.getCodigo().equals("constante_entera") ||
                tokenActual.getCodigo().equals("id") ||
                tokenActual.getCodigo().equals("cadena")
            ) {
                String segundoLexema = tokenActual.getLexema();
                error("Hay una expresión con dos operandos, pero sin operador. El segundo operando es " + segundoLexema+ ")");
                avanzar();
                D1();
            } else {
            parseOut.write("17 "); // D' -> lambda
        }
    }

    private void D2() throws IOException {
        switch (tokenActual.getCodigo()) {
            case "constante_entera":
                parseOut.write("18 ");//D2->constante
                avanzar();
                break;
            case "id":
                parseOut.write("19 ");//D2->id
                avanzar();
                break;
            case "cadena":
                parseOut.write("20 ");//D2->cadena
                avanzar();
                break;
            default:
                error("Expresión no válida: " + tokenActual.getCodigo());
                return;
        }
    }


    private void E() throws IOException {
        parseOut.write("21 ");//E->E1 E2
        E1();
        E2();
    }
    
    private void E1() throws IOException{
    	parseOut.write("22 ");//E1->D
    	D();
    }

    private void E2() throws IOException {
        String t = tokenActual.getCodigo();
        if (t.equals("dist")) {
        	parseOut.write("23 ");//E2->!=E1 E2
        	emparejar("dist");
            E1();
            E2();
        }else if(t.equals("or")) {
        	parseOut.write("24 ");//E2->||E1 E2
        	emparejar("or");
            E1();
            E2();
        }else {
        	parseOut.write("25 ");//E2->lambda
        }
    }

    private void F() throws IOException {
        switch (tokenActual.getCodigo()) {
            case "return":
                parseOut.write("26 ");//F->return(F2)
                emparejar("return");
                if (tokenActual.getCodigo().equals("par") && tokenActual.getAtributoI() == 1) {
                    emparejar("par", 1);
                    F2();
                    emparejar("par", 2);
                } else {
                    F2();
                }
                emparejar("puntoComa");
                break;
            case "input":
                parseOut.write("27 ");//F->input(F2)
                emparejar("input");
                if (tokenActual.getCodigo().equals("par") && tokenActual.getAtributoI() == 1) {
                    emparejar("par", 1);
                    F2();
                    emparejar("par", 2);
                } else {
                    F2();
                }
                emparejar("puntoComa");
                break;
            case "output":
                parseOut.write("28 ");//F->output(F2)
                emparejar("output");
                if (tokenActual.getCodigo().equals("par") && tokenActual.getAtributoI() == 1) {
                    emparejar("par", 1);
                    F2();
                    emparejar("par", 2);
                } else {
                    F2();
                }
                emparejar("puntoComa");
                break;
            default:
                parseOut.write("29 ");//F->F1
                F1();
                break;
        }
    }
    
    private void F1() throws IOException {
        parseOut.write("30 ");//F1->id F1_1
        emparejar("id");
        F11();
    }

    private void F11() throws IOException {
        if (tokenActual.getCodigo().equals("asig")) {
            parseOut.write("31 ");//F1_1->=E;
            emparejar("asig");
            E();
            emparejar("puntoComa");
        } else if (tokenActual.getCodigo().equals("operador") && tokenActual.getAtributoI() == 1) {
            parseOut.write("32 ");//F1_1->+=E;
            emparejar("operador",1);
            E();
            emparejar("puntoComa");
        } else if (tokenActual.getCodigo().equals("par") && tokenActual.getAtributoI() == 1) {
            parseOut.write("33 ");//F1_1->(F2);
            emparejar("par", 1);
            F2();
            emparejar("par", 2);
            emparejar("puntoComa");
        } else {
            error("Se esperaba '=' o '+=' o llamada a función en F1_1");
        }
    }
    
    private void F2() throws IOException {
        if (tokenActual.getCodigo().equals("id")) {
            parseOut.write("34 ");//F->id F3
            avanzar();
            F3();
        } else if (tokenActual.getCodigo().equals("constante_entera")) {
            parseOut.write("35 ");//F2->constante
            avanzar();
        } else if (tokenActual.getCodigo().equals("cadena")) {
            parseOut.write("36 ");//F2->cadena
            avanzar();
        } else {
            parseOut.write("37 ");//F2->lambda
        }
    }

    private void F3() throws IOException {
        if (tokenActual.getCodigo().equals("par") && tokenActual.getAtributoI() == 1) {
            parseOut.write("38 "); //F3->( F2 )
            emparejar("par", 1);
            F2();
            emparejar("par", 2);
        } else {
            parseOut.write("39 "); //F3->lambda
        }
    }


    private void G() throws IOException {
        parseOut.write("40 ");//G->G1;E;G1
        G1();
        emparejar("puntoComa");
        E();
        emparejar("puntoComa");
        G1();
    }

    private void G1() throws IOException {
        if (tokenActual.getCodigo().equals("id")) {
            parseOut.write("41 ");//G1-> id G1_1
            emparejar("id");
            G2();
        } else if (tokenActual.getCodigo().equals("var")) {
            parseOut.write("44 ");//G1->var C id =D
            emparejar("var");
            C();
            emparejar("id");
            emparejar("asig");
            D();
        } else {
            parseOut.write("45 ");//G1->lambda
        }
    }

    private void G2() throws IOException {
        if (tokenActual.getCodigo().equals("asig")) {
            parseOut.write("42 ");//G1_1->=D
            emparejar("asig");
            D();
        } else if (tokenActual.getCodigo().equals("operador") && tokenActual.getAtributoI() == 1) {
            parseOut.write("43 ");//G1_1->+=D
            emparejar("operador", 1);
            D();
        } else {
            error("Se esperaba '=' o '+=' en G1_1");
        }
    }

    private void H() throws IOException {
        if (esInicioDeB(tokenActual.getCodigo())) {
            parseOut.write("46 ");//H-> BH
            B();
            H();
        } else {
            parseOut.write("47 ");//H->lambda
        }
    }

    private void I() throws IOException {
        if (tokenActual.getCodigo().equals("void")) {
            parseOut.write("48 ");//I->void I2
            emparejar("void");
            I2();
        } else if (tokenActual.getCodigo().equals("int")) {
            parseOut.write("51 ");//i->int id I1
            emparejar("int");
            emparejar("id");
            I1();
        } else if (tokenActual.getCodigo().equals("boolean")) {
            parseOut.write("52 ");//I->boolean id I1
            emparejar("boolean");
            emparejar("id");
            I1();
        } else if (tokenActual.getCodigo().equals("string")) {
            parseOut.write("53 ");//I->string id I1
            emparejar("string");
            emparejar("id");
            I1();
        } else {
            error("Tipo en parámetros de función incorrecto: " + tokenActual.getCodigo());
        }
    }

    private void I2() throws IOException {
        if (tokenActual.getCodigo().equals("id")) {
            parseOut.write("49 ");//I2->id I1
            emparejar("id");
            I1();
        } else {
            parseOut.write("50 ");//I2->lambda
        }
    }

    private void I1() throws IOException {
        if (tokenActual.getCodigo().equals("coma")) {
            parseOut.write("54 ");//I1->,C id I1
            emparejar("coma");
            C();
            emparejar("id");
            I1();
        } else {
            parseOut.write("55 ");//I1-> lambda
        }
    }
}

