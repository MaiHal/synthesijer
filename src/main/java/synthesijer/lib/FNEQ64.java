package synthesijer.lib;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class FNEQ64 extends HDLModule{

	public float a;
	public float b;
	public float result;
	public boolean valid;
	public boolean nd;

	public FNEQ64(){
		super("synthesijer_fneq64", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("b",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genBitType());
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

}
