package practicaPDL;

public class Token {
	
	private String codigo;
	private String atributo;
	private int atributoI;
	private int linea = -1;
	
	public static Token BOOLEAN = new Token("boolean");
	public static Token BREAK = new Token("break");
	public static Token CASE = new Token("case");
	public static Token FLOAT = new Token("float");
	public static Token FUNCTION = new Token("function");
	public static Token IF = new Token("if");
	public static Token INT = new Token("int");
	public static Token LET = new Token("let");
	public static Token READ = new Token("read");
	public static Token RETURN = new Token("return");
	public static Token STRING = new Token("string");
	public static Token SWITCH = new Token("switch");
	public static Token VOID = new Token("void");
	public static Token WRITE = new Token("write");
	public static Token AUTOINCREMENTO = new Token("autoincremento");
	public static Token CONSTANTE_REAL = new Token("constr");
	public static Token CONSTANTE_ENTERA = new Token("conste");
	//constante entera public static Token CONSTANTE_ENTERA=new Token("constEntera");
	public static Token CADENA = new Token("cadena");
	//id
	public static Token IDENTIFICADOR = new Token("id");
	public static Token IGUAL=new Token("igual");//=
	public static Token COMA=new Token("coma");
	public static Token PUNTO_COMA=new Token("puntoComa");
	public static Token DOS_PUNTOS = new Token("dospuntos");
	public static Token PARENTESISA=new Token("par",1);
	public static Token PARENTESISC=new Token("par",2);
	public static Token LLAVEA=new Token("lla",1);
	public static Token LLAVEC=new Token("lla",2);
	public static Token SUMA=new Token("suma");//+
	public static Token O_LOGICO=new Token("or");//||
	public static Token DISTINTO=new Token("dist");//!=
	
	
	
	
	public Token(String codigo) {
		this.codigo=codigo;
		this.atributoI=-1;
	} 
	public int getLinea() {
		return linea;
	}
	public void setLinea(int linea) {
		this.linea = linea;
	}
	public Token(String codigo, int atributoI) {
		this.codigo=codigo;
		this.atributoI=atributoI;
	}
	
	public Token(String codigo, String atributo) {
		this.codigo=codigo;
		this.atributo=atributo;
		this.atributoI=-1;
	}
	
	public Token(String codigo, int atributoI, int linea) {
	    this.codigo = codigo;
	    this.atributoI = atributoI;
	    this.linea = linea;
	}
	public Token(String codigo, String atributo, int linea) {
	    this.codigo = codigo;
	    this.atributo = atributo;
	    this.atributoI = -1;
	    this.linea = linea;
	}
	
	//metodo para asignar su atributo
	public void setAtributoI(int atributoI) {
		this.atributoI=atributoI;
	}
	
	public String getCodigo() {
		return codigo;
	}
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getAtributo() {
		return atributo;
	}
	public void setAtributo(String atributo) {
		this.atributo = atributo;
	}
	public int getAtributoI() {
		return atributoI;
	}
	
	public String toString() {
	    if (this.atributoI != -1) {
	        // Tiene atributo entero (por ejemplo índice o valor numérico)
	        return "<" + this.codigo + "," + this.atributoI + ">";
	    } else{
	        // Tiene atributo tipo String (por ejemplo lexema)
	        return "<" + this.codigo + "," + this.atributo + ">";
	    }
	}
	
	public String getLexema() {
	    if (atributo != null) return atributo;
	    if (atributoI != -1) return String.valueOf(atributoI);
	    return ""; 
	}

}
