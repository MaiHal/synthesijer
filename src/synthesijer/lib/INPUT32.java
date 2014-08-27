package synthesijer.lib;

import java.util.EnumSet;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class INPUT32 extends HDLModule{
	
	public int value;
	
	public INPUT32(){
		super("inputport", "clk", "reset");
		newParameter("WIDTH", HDLPrimitiveType.genIntegerType(), String.valueOf(32));
		newPort("value",  DIR.OUT, HDLPrimitiveType.genSignedType(32));
		newPort("din",  DIR.IN, HDLPrimitiveType.genVectorType(32), EnumSet.of(HDLPort.OPTION.EXPORT));
	}

}
