package synthesijer.hdl.sample;

import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLOp;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSequencer;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLUtils;
import synthesijer.hdl.expr.HDLConstant;

public class ClkDiv extends HDLModule{
	
	public ClkDiv(){
		super("clkdiv", "clk", "reset");
		HDLPort clk_out = newPort("clk_out", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
		HDLPort div = newPort("div", HDLPort.DIR.IN, HDLPrimitiveType.genVectorType(16));
		
		HDLSignal c = newSignal("counter", HDLPrimitiveType.genVectorType(16));
		
		HDLSequencer seq = newSequencer("main");
		HDLSequencer.SequencerState s0 = seq.addSequencerState("S0");
		seq.getIdleState().addStateTransit(s0);
		c.setAssign(seq.getIdleState(), HDLConstant.INTEGER_ZERO);
		
		HDLExpr cond = newExpr(HDLOp.EQ, div.getSignal(), c);
		
		c.setAssign(s0, newExpr(HDLOp.IF, cond, newExpr(HDLOp.ADD, c, 1), HDLConstant.INTEGER_ZERO));
		clk_out.getSignal().setAssign(s0, newExpr(HDLOp.IF, cond, HDLConstant.BOOLEAN_TRUE, HDLConstant.BOOLEAN_FALSE));
	}
	
	public static void main(String[] args){
		HDLModule m = new ClkDiv();
		HDLUtils.generate(m, HDLUtils.VHDL);
		HDLUtils.generate(m, HDLUtils.Verilog);
	}

}
