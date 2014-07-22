
import java.io.IOException;

import synthesijer.hdl.*;
import synthesijer.hdl.expr.*;

public class led_top{
	
	private static void addThreadPort(HDLModule m){
		m.newPort("start_req", HDLPort.DIR.IN, HDLPrimitiveType.genBitType());
		m.newPort("start_busy", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
		m.newPort("join_req", HDLPort.DIR.IN, HDLPrimitiveType.genBitType());
		m.newPort("join_busy", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
		m.newPort("yield_req", HDLPort.DIR.IN, HDLPrimitiveType.genBitType());
		m.newPort("yield_busy", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
	}
	
	public static void main(String... args) throws IOException{
		HDLModule led_top = new HDLModule("led_top", "clk", "reset");
		HDLPort q = led_top.newPort("q", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());

		HDLModule led = new HDLModule("led", "clk", "reset");
		HDLPort run_req = led.newPort("run_req", HDLPort.DIR.IN, HDLPrimitiveType.genBitType());
		HDLPort run_busy = led.newPort("run_busy", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
		HDLPort out = led.newPort("field_flag_output", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
		addThreadPort(led);
       HDLInstance inst = led_top.newModuleInstance(led, "U");

       inst.getSignalForPort("clk").setAssign(null, led_top.getSysClk().getSignal());
       inst.getSignalForPort("reset").setAssign(null, led_top.newExpr(HDLOp.NOT, led_top.getSysReset().getSignal()));
       inst.getSignalForPort(run_req.getName()).setAssign(null, HDLPreDefinedConstant.HIGH); // always high to start immediately
       q.getSignal().setAssign(null, inst.getSignalForPort(out.getName()));
		
		HDLUtils.generate(led_top, HDLUtils.VHDL);
		HDLUtils.generate(led_top, HDLUtils.Verilog);
	}
}

