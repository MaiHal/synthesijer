//命令の追加
//package synthesijer.lib;
package synthesijer.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_EXP extends HDLModule{

	public float data;
	public float result;
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

	public ALTFP_EXP(){
		super("altfp_exp", "clock");
		newPort("data",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
	}

  public static String getOpName(){
		return "ALTFP_EXP";
	}
}