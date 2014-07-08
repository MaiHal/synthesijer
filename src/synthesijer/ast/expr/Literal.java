package synthesijer.ast.expr;

import synthesijer.ast.Expr;
import synthesijer.ast.Scope;
import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;

public class Literal extends Expr{
	
	public enum LITERAL_KIND {
		BOOLEAN,
		BYTE,
		CHAR,
		SHORT,
		INT,
		LONG,
		DOUBLE,
		FLOAT,
		STRING,
		NULL,
		UNKNOWN
	};
	
	private boolean valueBoolean;
	private byte valueByte;
	private char valueChar;
	private short valueShort;
	private int valueInt;
	private long valueLong;
	private double valueDouble;
	private float valueFloat;
	private String valueStr;
		
	private LITERAL_KIND kind;
	private int width;
	
	public Literal(Scope scope){
		super(scope);
	}
	
	public LITERAL_KIND getKind(){
		return kind;
	}
		
	public void setValue(boolean value){
		this.kind = LITERAL_KIND.BOOLEAN;
		this.valueBoolean = value;
		this.width = 1;
	}
	
	public void setValue(byte value){
		this.kind = LITERAL_KIND.BYTE;
		this.valueByte = value;
		this.width = 8;
	}
	
	public void setValue(char value){
		this.kind = LITERAL_KIND.CHAR;
		this.valueChar = value;
		this.width = 16;
	}
	
	public void setValue(short value){
		this.kind = LITERAL_KIND.SHORT;
		this.valueShort = value;
		this.width = 16;
	}
	
	public void setValue(int value){
		this.kind = LITERAL_KIND.INT;
		this.valueInt = value;
		this.width = 32;
	}
	
	public void setValue(long value){
		this.kind = LITERAL_KIND.LONG;
		this.valueLong = value;
		this.width = 64;
	}
	
	public void setValue(double value){
		this.kind = LITERAL_KIND.DOUBLE;
		this.valueDouble = value;
		this.width = 64;
	}
	
	public void setValue(float value){
		this.kind = LITERAL_KIND.FLOAT;
		this.valueFloat = value;
		this.width = 32;
	}
	
	public void setValue(String value){
		this.kind = LITERAL_KIND.STRING;
		this.valueStr = value;
		this.width = 0;
	}

	public void setValue(LITERAL_KIND kind){
		this.kind = kind;
		this.width = 0;
	}

	public String getValueAsStr(){
		switch(kind){
		case BOOLEAN: return String.valueOf(valueBoolean);
		case BYTE:    return String.valueOf(valueByte);
		case CHAR:    return String.valueOf(valueChar);
		case SHORT:   return String.valueOf(valueShort);
		case INT:     return String.valueOf(valueInt);
		case LONG:    return String.valueOf(valueLong);
		case DOUBLE:  return String.valueOf(valueDouble);
		case FLOAT:   return String.valueOf(valueFloat);
		case STRING:  return valueStr;
		case NULL:    return "NULL";
		default: return "UNKNOWN";
		}
	}
	
	public void accept(SynthesijerExprVisitor v){
		v.visitLitral(this);
	}
	
	@Override
	public boolean isConstant() {
		return true;
	}
	
	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public Type getType(){
		switch(kind){
		case BOOLEAN: return PrimitiveTypeKind.BOOLEAN;
		case BYTE:    return PrimitiveTypeKind.BYTE;
		case CHAR:    return PrimitiveTypeKind.CHAR;
		case SHORT:   return PrimitiveTypeKind.SHORT;
		case INT:     return PrimitiveTypeKind.INT;
		case LONG:    return PrimitiveTypeKind.LONG;
		case DOUBLE:  return PrimitiveTypeKind.DOUBLE;
		case FLOAT:   return PrimitiveTypeKind.FLOAT;
		case NULL:    return PrimitiveTypeKind.VOID;
		default: return PrimitiveTypeKind.UNDEFIEND;
		}
	}
	
}