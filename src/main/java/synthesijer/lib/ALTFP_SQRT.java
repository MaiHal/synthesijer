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

public class ALTFP_SQRT extends HDLModule{

	public float data;
	public float result;
	public static ArrayList<Op> op = new ArrayList<Op>(
		Arrays.asList(
			Op.FCOMPEQ32,
			Op.JT,
			Op.RETURN,
			Op.FGT32,
			Op.COND,
			Op.ASSIGN,
			Op.ASSIGN,
			Op.CONV_F2D,
			Op.FGT64,
			Op.JT,
			Op.FDIV32,
			Op.FADD32,
			Op.FDIV32,
			Op.ASSIGN,
			Op.FSUB64,
			Op.ASSIGN,
			Op.ASSIGN
		)
	);

	public ALTFP_SQRT(){
		super("alt_sqrt", "clock");
		newPort("data",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
	}
}