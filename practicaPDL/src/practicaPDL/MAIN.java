package practicaPDL;

import java.io.File;
import java.io.IOException;

public class MAIN {
	
	public static void main(String[] args) throws Exception {
		
		String nombreFichero="Ejemplo 5.txt";
		//String nombreFichero="PIdG104 (13).txt";
		AnalizadorLexico aLex= new AnalizadorLexico(nombreFichero);
		if(true) {
			AnalizadorSintactico sintactico = new AnalizadorSintactico("token.txt");
			sintactico.analizar();
		}
	}

}
