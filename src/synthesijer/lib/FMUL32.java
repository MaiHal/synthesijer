package synthesijer.lib;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class FMUL32 extends HDLModule{
	
	public float a;
	public float b;
	public float result;
	public boolean valid;
	public boolean nd;
	
	public FMUL32(){
		super("synthesijer_fmul32", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("b",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

}
