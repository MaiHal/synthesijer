package synthesijer.hdl;

import synthesijer.hdl.expr.HDLPreDefinedConstant;

/**
 * In order to make an instance of this class, use builder method.
 *   
 * @author miyo
  */
public class HDLPrimitiveType implements HDLTree, HDLType{

	private final KIND kind;
	private final int width;

	private HDLPrimitiveType(KIND kind, int width) {
		this.kind = kind;
		this.width = width;
	}
	
	public KIND getKind(){
		return kind;
	}

	public boolean isBit(){
		return kind == KIND.BIT;
	}
	
	public int getWidth(){
		return width;
	}
	
	public static HDLPrimitiveType genBitType(){
		return new HDLPrimitiveType(KIND.BIT, 1);
	}

	public static HDLPrimitiveType genSignedType(int width){
		return new HDLPrimitiveType(KIND.SIGNED, width);
	}

	public static HDLPrimitiveType genIntegerType(){
		return new HDLPrimitiveType(KIND.INTEGER, 0);
	}

	public static HDLPrimitiveType genStringType(){
		return new HDLPrimitiveType(KIND.STRING, 0);
	}

	public static HDLPrimitiveType genVectorType(int width){
		return new HDLPrimitiveType(KIND.VECTOR, width);
	}
		
	public static HDLPrimitiveType genUnknowType(){
		return new HDLPrimitiveType(KIND.UNKNOWN, 0);
	}

	public HDLLiteral getDefaultValue(){
		if(kind == KIND.VECTOR || kind == KIND.SIGNED){
			return HDLPreDefinedConstant.INTEGER_ZERO;
		}else if(kind == KIND.BIT){
			return HDLPreDefinedConstant.LOW;
		}else{
			return null;
		}
	}
	
	public String getVHDL(){
		switch(kind){
		case VECTOR: return String.format("std_logic_vector(%d-1 downto 0)", width);
		case BIT:    return String.format("std_logic");
		case SIGNED: return String.format("signed(%d-1 downto 0)", width);
		case INTEGER: return "integer";
		default: return "UNKNOWN";
		}
	}

	public String getVerilogHDL(){
		switch(kind){
		case VECTOR: return String.format("[%d-1 : 0]", width);
		case BIT:    return String.format("");
		case SIGNED: return String.format("signed [%d-1 : 0]", width);
		case INTEGER: return "";
		default: return "UNKNOWN";
		}
	}

	@Override
	public void accept(HDLTreeVisitor v) {
		v.visitHDLType(this);
	}
	
	public String toString(){
		return "HDLPrimitiveType:" + kind;
	}

}
