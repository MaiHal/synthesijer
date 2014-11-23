package synthesijer.hdl.expr;

import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLLiteral;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLTreeVisitor;

public class HDLValue implements HDLLiteral{
	
	private final HDLPrimitiveType type;
	private final String value;
	
	public HDLValue(String value, HDLPrimitiveType type){
		this.value = value;
		this.type = type;
	}
	
	public String getValue(){
		return value;
	}

	@Override
	public String getVHDL() {
		switch(type.getKind()){
		case VECTOR:
		case SIGNED: {
			if(type.getWidth()%4 == 0){
				String v = String.format("%016x", Long.parseLong(value));
				//System.out.printf("%s => %s => %s\n", value, v, v.substring(v.length()-type.getWidth()/4, v.length()));
				if(v.length()-type.getWidth()/4 < 0){
					System.out.println(v);
					System.out.println(this.getValue() + ":" + type.getWidth() + "," + v.length() + "-" + (type.getWidth()/4));
				}
				String s = String.format("X\"%s\"", v.substring(v.length()-type.getWidth()/4, v.length()));
				return s;
			}else{
				String v = "";
				for(int i = 0; i < 64; i++){ v += "0"; }
				v += Long.toBinaryString(Long.parseLong(value));
				return String.format("\"%s\"", v.substring(v.length()-type.getWidth(), v.length()));
			}
		}
		case BIT :
			if(value.equals("true")){
				return "'1'";
			}else{
				return "'0'";
			}
		case INTEGER:
			return String.valueOf(value);
		case STRING:
			return value;
		default:
			return "UNKNWON(" + value + ")";
		}
	}

	@Override
	public String getVerilogHDL() {
		switch(type.getKind()){
		case VECTOR:
		case SIGNED:{
			if(type.getWidth()%4 == 0){
				String v = String.format("%064x", Long.parseLong(value));
				return String.format("%d'h%s", type.getWidth(), v.substring(v.length()-type.getWidth()/4, v.length()));
			}else{
				String v = "";
				for(int i = 0; i < 64; i++){ v += "0"; }
				v += Long.toBinaryString(Long.parseLong(value));
				return String.format("%d'b%s", type.getWidth(), v.substring(v.length()-type.getWidth(), v.length()));
			}
		}
		case BIT:
			if(value.equals("true")){
				return "1'b1";
			}else{
				return "1'b0";
			}
		case INTEGER:
			return String.valueOf(value);
		case STRING:
			return value;
		default:
			return "UNKNWON(" + value + ")";
		}
	}

	@Override
	public void accept(HDLTreeVisitor v) {
		v.visitHDLLitral(this);
	}
	
	@Override
	public HDLExpr getResultExpr() {
		return this;
	}

	@Override
	public HDLPrimitiveType getType() {
		return type;
	}
	
	@Override
	public HDLSignal[] getSrcSignals() {
		return null;
	}

}
