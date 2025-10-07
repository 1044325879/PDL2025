package practicaPDL;

public class Token {
	
	private String codigo;
	private String atributo;
	private int atributoI;
	private int linea = -1;
	
	public static Token BOOLEAN = new Token("boolean");
	public static Token FOR = new Token("for");
	public static Token FUNCTION = new Token("function");
	public static Token IF = new Token("if");
	public static Token INPUT = new Token("input");
	public static Token INT = new Token("int");
	public static Token OUTPUT = new Token("output");
	public static Token RETURN = new Token("return");
	public static Token STRING = new Token("string");
	public static Token VAR =new Token("var");
	public static Token VOID = new Token("void");
	//constante entera public static Token CONSTANTE_ENTERA=new Token("constEntera");
	public static Token CADENA = new Token("cadena");
	//id
	public static Token OPERADOR1=new Token("operador",1);//+=
	public static Token OPERADOR2=new Token("asig");//=
	public static Token COMA=new Token("coma");
	public static Token PUNTOCOMA=new Token("puntoComa");
	public static Token PARENTESISA=new Token("par",1);
	public static Token PARENTESISC=new Token("par",2);
	public static Token LLAVEA=new Token("lla",1);
	public static Token LLAVEC=new Token("lla",2);
	public static Token SUMA=new Token("suma");
	public static Token O_LOGICO=new Token("or");
	public static Token DISTINTO=new Token("dist");
	
	
	
	
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
		if(this.atributoI!=-1) {
			return "<"+this.codigo+","+this.atributoI+">";
		}else {
			return "<"+this.codigo+","+this.atributo+">";
		}
		
	}
	
	public String getLexema() {
	    if (atributo != null) return atributo;
	    if (atributoI != -1) return String.valueOf(atributoI);
	    return ""; 
	}

}
