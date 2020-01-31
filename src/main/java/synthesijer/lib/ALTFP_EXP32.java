//package synthesijer.lib;
package synthesijer.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_EXP32 extends HDLModule{

	public float a;
	public float result;
	public boolean valid;
	public boolean nd;

	public static ArrayList<Op> op = new ArrayList<Op>(
		Arrays.asList(
			Op.FGT32,
      Op.JT,
      Op.FGT32,
      Op.JT,
      Op.FMUL32,
      Op.ASSIGN,
      Op.FSUB32,
      Op.ASSIGN,
      Op.FLT32,
      Op.JT,
      Op.FLT32,
      Op.JT,
      Op.FDIV32,
      Op.ASSIGN,
      Op.FADD32,
      Op.ASSIGN,
			Op.RETURN
		)
	);

	public ALTFP_EXP32(){
		super("synthesijer_altfp_exp32", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

  public static String getOpName(){
		return "ALTFP_EXP32";
	}
}