//命令の追加
package synthesijer.lib;

import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_SQRT extends HDLModule{

	public float data;
	public float result;

	public ALTFP_SQRT(){
		super("alt_sqrt", "clock");
		newPort("data",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
	}

}