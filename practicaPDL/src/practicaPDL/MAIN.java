package practicaPDL;

import java.io.File;
import java.io.IOException;

public class MAIN {
	
	public static void main(String[] args) throws Exception {
		
		String nombreFichero="PIdG107 (15).txt";
		//String nombreFichero="PIdG104 (5).txt";
		AnalizadorLexico aLex= new AnalizadorLexico(nombreFichero);
		if(true) {
			AnalizadorSintactico sintactico = new AnalizadorSintactico("token.txt");
			sintactico.analizar();
		}
	}

}
