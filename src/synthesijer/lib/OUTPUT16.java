package synthesijer.lib;

import java.util.EnumSet;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class OUTPUT16 extends HDLModule{
	
	short value;
	
	public OUTPUT16(){
		super("outputport", "clk", "reset");
		newParameter("WIDTH", HDLPrimitiveType.genIntegerType(), String.valueOf(16));
		newPort("value",  DIR.IN, HDLPrimitiveType.genSignedType(16));
		newPort("dout",  DIR.OUT, HDLPrimitiveType.genVectorType(32), EnumSet.of(HDLPort.OPTION.EXPORT));
	}

}
