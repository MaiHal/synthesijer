//命令の追加
package synthesijer.lib;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_SQRT extends HDLModule{

	public float a;
	public float result;

	public ALTFP_SQRT(){
		super("synthesijer_alt_sqrt", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("b",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

}