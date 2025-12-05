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
        //if (tokens.isEmpty()) throw new IOException("token.txt vacío");
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
        br.close();
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
        	//comienza por P
            P();
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
    //没测
    private String getIdNombre() {
        return idnombre.get(tokenActual.getAtributoI());
    }
    
    //也没测
    private String extraerId(Token token) {
        if (token.getCodigo().equals("id")) {
            //return token.getAtributo();
            return idnombre.get(token.getAtributoI());
        } else {
            return "<desconocido>";
        }
    }
    
    private void P() throws IOException {
        String c = tokenActual.getCodigo();
        if (c.equals("if") || c.equals("let") || c.equals("switch") ||
            c.equals("id") || c.equals("write") || c.equals("read") ||
            c.equals("return")) {
            parseOut.write("1 ");//P->BP
            B();
            P();
        } else if (c.equals("function")) {
            parseOut.write("2 ");//P->FP
            F();
            P();
        } else {
            parseOut.write("3 ");//P->lambda
            // lambda
        }
    }
    
    private void B() throws IOException {
        switch (tokenActual.getCodigo()) {
	        case "if":
	            parseOut.write("4 ");//B->if (E) S
	            emparejar("if");
	            emparejar("par", 1);
	            E();
	            emparejar("par", 2);
	            S();
	            break;
	            
	        case "let": {
	            parseOut.write("5 ");//B->let T id;
	            emparejar("let");
	            String tipo = T();
	            String nombreVar = getIdNombre();//para semantica
	            emparejar("id");
	            sem.declararVariable(nombreVar, tipo);
	            emparejar("puntoComa");
	            break;
	        }
            
            case "switch":
                parseOut.write("6 ");//B->switch(E){W}
                emparejar("switch");
                emparejar("par", 1);
                E();
                emparejar("par", 2);
                emparejar("lla", 1);
                W();
                emparejar("lla", 2);
                break;
            
            default:
                parseOut.write("7 ");//B->S
                S();
                break;
        }
    }
    

    private void S() throws IOException {
        String c = tokenActual.getCodigo();
        switch (c) {
            case "id":
                parseOut.write("8 ");//S->id S1
                // aquí podría usarse el nombre para semántica
                emparejar("id");
                S1();
                break;
            case "write":
                parseOut.write("9 ");//S->write E;
                emparejar("write");
                E();
                emparejar("puntoComa");
                break;
            case "read":
                parseOut.write("10 ");//S->read id;
                emparejar("read");
                emparejar("id");
                emparejar("puntoComa");
                break;
            case "return":
                parseOut.write("11 ");//S->return X;
                emparejar("return");
                X();
                emparejar("puntoComa");
                break;
            case "break":
                parseOut.write("12 ");   //S->break;
                emparejar("break");
                emparejar("puntoComa");
                break;
            default:
                error("Sentencia no válida. Se esperaba id, write, read o return.");
        }
    }
    
    private void S1() throws IOException {
        String c = tokenActual.getCodigo();
        if (c.equals("igual")) {
            parseOut.write("13 ");//S1->=E;
            emparejar("igual");
            E();
            emparejar("puntoComa");
        } else if (c.equals("par") && tokenActual.getAtributoI() == 1) {
            parseOut.write("14 ");//S1->(L);
            emparejar("par", 1);
            L();
            emparejar("par", 2);
            emparejar("puntoComa");
        } else if (c.equals("autoincremento")) {
            parseOut.write("15 ");//S1->autoincremento;
            emparejar("autoincremento");
            emparejar("puntoComa");
        } else {
            error("Se esperaba '=', '(' o 'autoincremento' después de id.");
        }
    }
    
    private void L() throws IOException {
        if (esInicioDeE()) {
            parseOut.write("16 ");//L->EQ
            E();
            Q();
        } else {
            parseOut.write("17 ");//L->lambda
            // lambda
        }
    }
    
    private void Q() throws IOException {
        if (tokenActual.getCodigo().equals("coma")) {//Q->,EQ
            parseOut.write("18 ");
            emparejar("coma");
            E();
            Q();
        } else {
            parseOut.write("19 ");//Q->lambda
            // lambda
        }
    }
    
    private void X() throws IOException {
        if (esInicioDeE()) {
            parseOut.write("20 ");//X->E
            E();
        } else {
            parseOut.write("21 ");//X->lambda
            // lambda
        }
    }
    
    private String T() throws IOException {
        String c = tokenActual.getCodigo();
        switch (c) {
            case "int":
                parseOut.write("22 ");//T->int
                emparejar("int");
                return "int";
            case "float":
                parseOut.write("23 ");//T->float
                emparejar("float");
                return "float";
            case "boolean":
                parseOut.write("24 ");//T->boolean
                emparejar("boolean");
                return "boolean";
            case "string":
                parseOut.write("25 ");//T->string
                emparejar("string");
                return "string";
            default:
                error("Tipo no válido: " + c);
                return "indefinido";
        }
    }
    
    private void F() throws IOException {
        parseOut.write("26 ");//F->function H id(A){C}
        emparejar("function");

        String tipoFuncion = H();

        // nombre de la función
        String nombreFunc = getIdNombre();
        emparejar("id");

        emparejar("par", 1);

        List<String> parametrosTipo = new ArrayList<>();
        List<String> parametrosNombre = new ArrayList<>();
        A(parametrosTipo, parametrosNombre);

        // acción semántica: inicio de función
        sem.iniciarFuncion(nombreFunc, tipoFuncion, parametrosTipo);
        for (int i = 0; i < parametrosTipo.size(); i++) {
            sem.declararParametro(parametrosNombre.get(i), parametrosTipo.get(i));
        }

        emparejar("par", 2);
        emparejar("lla", 1);
        C();
        sem.terminarFuncion();
        emparejar("lla", 2);
    }
    
    private String H() throws IOException {
        if (tokenActual.getCodigo().equals("void")) {
            parseOut.write("28 ");//H->T
            emparejar("void");
            return "void";
        } else {
            parseOut.write("27 ");//H->void
            return T();
        }
    }

    private void A(List<String> paramTipos, List<String> paramNombres) throws IOException {
        String c = tokenActual.getCodigo();
        if (c.equals("void")) {
            parseOut.write("30 ");//A->void
            emparejar("void");
            // sin parámetros
        } else if (c.equals("int") || c.equals("float") ||
                   c.equals("boolean") || c.equals("string")) {
            parseOut.write("29 ");//A->T id K
            String tipo = T();
            String nombre = getIdNombre();
            emparejar("id");
            paramTipos.add(tipo);
            paramNombres.add(nombre);
            K(paramTipos, paramNombres);
        } else {
            error("Error en parámetros de función. Se esperaba tipo o void.");
        }
    }
    
    private void K(List<String> paramTipos, List<String> paramNombres) throws IOException {
        if (tokenActual.getCodigo().equals("coma")) {
            parseOut.write("31 ");//K->,T id K
            emparejar("coma");
            String tipo = T();
            String nombre = getIdNombre();
            emparejar("id");
            paramTipos.add(tipo);
            paramNombres.add(nombre);
            K(paramTipos, paramNombres);
        } else {
            parseOut.write("32 ");//K->lambda
            // lambda
        }
    }
    
    private void C() throws IOException {
        String c = tokenActual.getCodigo();
        if (c.equals("if") || c.equals("let") || c.equals("switch") ||
            c.equals("id") || c.equals("write") || c.equals("read") ||
            c.equals("return")|| c.equals("break")) {
            parseOut.write("33 ");//C->BC
            B();
            C();
        } else {
            parseOut.write("34 ");//C->lambda
            // lambda
        }
    }
    
    private void W() throws IOException {
        String c = tokenActual.getCodigo();
        if (c.equals("case")) {
            parseOut.write("35 ");//W->case conste : CW
            emparejar("case");
            emparejar("conste");
            emparejar("dospuntos");
            C();
            W();
        } else if (c.equals("default")) {
            parseOut.write("36 ");//W->default : c
            emparejar("default");
            emparejar("dospuntos");
            C();
        } else {
            parseOut.write("37 ");//W->lambda
            // lambda
        }
    }
    
    private void E() throws IOException {
        parseOut.write("38 ");//E->RE1
        R();
        E1();
    }

    private void E1() throws IOException {
        if (tokenActual.getCodigo().equals("or")) {
            parseOut.write("39 ");//E1->or RE1
            emparejar("or");
            R();
            E1();
        } else {
            parseOut.write("40 ");//E1->lambda
            // lambda
        }
    }
    
    private void R() throws IOException {
        parseOut.write("41 ");//R->UR1
        U();
        R1();
    }

    private void R1() throws IOException {
        if (tokenActual.getCodigo().equals("dist")) {
            parseOut.write("42 ");//R1->dist U R1
            emparejar("dist");
            U();
            R1();
        } else {
            parseOut.write("43 ");//R1->lambda
            // lambda
        }
    }
    
    private void U() throws IOException {
        parseOut.write("44 ");//U->VU1
        V();
        U1();
    }

    private void U1() throws IOException {
        if (tokenActual.getCodigo().equals("suma")) {
            parseOut.write("45 ");//U1->suma VU1
            emparejar("suma");
            V();
            U1();
        } else {
            parseOut.write("46 ");//U1->lambda
            // lambda
        }
    }
    
    private void V() throws IOException {
        String c = tokenActual.getCodigo();
        switch (c) {
            case "id":
                parseOut.write("47 ");//V->id V1
                emparejar("id");
                V1();
                break;
            case "par":
                if (tokenActual.getAtributoI() == 1) {
                    parseOut.write("48 ");//V->(E)
                    emparejar("par", 1);
                    E();
                    emparejar("par", 2);
                } else {
                    error("Se esperaba '(' en factor.");
                }
                break;
            case "conste":
                parseOut.write("49 ");//V->conste
                emparejar("conste");
                break;
            case "constr":
                parseOut.write("50 ");//V->constr
                emparejar("constr");
                break;
            case "cadena":
                parseOut.write("51 ");//V->cadena
                emparejar("cadena");
                break;
            default:
                error("Factor no válido: " + c);
        }
    }
    
    private void V1() throws IOException {
        String c = tokenActual.getCodigo();
        if (c.equals("autoincremento")) {
            parseOut.write("52 ");//V1->autoincremento
            emparejar("autoincremento");
        } else if (c.equals("par") && tokenActual.getAtributoI() == 1) {
            parseOut.write("53 ");//V2->(L)
            emparejar("par", 1);
            L();
            emparejar("par", 2);
        } else {
            parseOut.write("54 ");//V1->lambda
            // lambda
        }
    }

    private boolean esInicioDeB(String codigo) {
        return codigo.equals("let") || codigo.equals("if") || codigo.equals("switch") ||
               codigo.equals("function") || codigo.equals("write") || codigo.equals("read") ||
               codigo.equals("return") || codigo.equals("id");
    }
    
    private boolean esInicioDeE() {
        String c = tokenActual.getCodigo();
        return c.equals("id") || c.equals("conste") || c.equals("constr") ||
               c.equals("cadena") ||
               (c.equals("par") && tokenActual.getAtributoI() == 1);
    }

}
