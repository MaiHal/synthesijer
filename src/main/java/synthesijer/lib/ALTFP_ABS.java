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

public class ALTFP_ABS extends HDLModule{

	public float data;
	public float result;
	public static ArrayList<Op> op = new ArrayList<Op>(
		Arrays.asList(
			Op.FGEQ32,
      Op.JT,
      Op.RETURN,
      Op.FMUL32,
			Op.RETURN
		)
	);

	public ALTFP_ABS(){
		super("altfp_abs", "clock");
		newPort("data",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
	}

  public static String getOpName(){
		return "ALTFP_ABS";
	}
}