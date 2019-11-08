package synthesijer.hdl.expr;

import java.util.ArrayList;

import synthesijer.SynthesijerUtils;
import synthesijer.Constant;
import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLOp;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLTreeVisitor;
import synthesijer.hdl.HDLType;
import synthesijer.hdl.sequencer.SequencerState;

public class HDLPhiExpr implements HDLExpr {

    private final int uid;
    private final HDLOp op;
    private final HDLExpr dest;
    private final HDLExpr[] args;
	private final SequencerState[] patterns;

    private final HDLSignal result;

	private final HDLSignal prevStateKey;

    public HDLPhiExpr(HDLModule m, int uid, HDLOp op, HDLExpr dest, HDLExpr[] args, SequencerState[] ss) {
        this.uid = uid;
        this.op = op;
		this.dest = dest;
        this.args = args;
		this.patterns = new SequencerState[args.length];
		
		HDLSignal key = null;
		for(int i = 0; i < patterns.length; i++){
			if(ss[i] != null){
				key = m.newSignal(String.format("phi_prev_%04d", uid), ss[i].getKey().getType());
				break;
			}
		}
		this.prevStateKey = key;
		
		for(int i = 0; i < patterns.length; i++){
			this.patterns[i] = ss[i];
			if(ss[i] != null){
				var seq = ss[i].getSequencer();
				var prev = seq.getPrevStateKey();
				prev.setAssign(ss[i], ss[i].getStateId());
				
				prevStateKey.setAssignForSequencer(seq, seq.getPrevStateKey(), ss[i].getStateId(), ss[i].getStateId());
			}
		}
        for (HDLExpr expr : args) {
            if (expr == null)
                throw new RuntimeException("An argument of HDLCombinationExpr is null.");
        }
        HDLType type = args[0].getType();
        result = m.newSignal(String.format("tmp_%04d", uid), type, HDLSignal.ResourceKind.WIRE, this, true);
    }

    @Override
    public void accept(HDLTreeVisitor v) {
        v.visitHDLExpr(this);
    }

    public String toString() {
        return "HDLPhiExpr::(" + op + " " + getArgsString(args) + ")";
    }

    private String getArgsString(HDLExpr[] args) {
        String s = "";
        for (HDLExpr a : args) {
            s += a.toString() + " ";
        }
        return s;
    }
	
	/**
	 *
	 * @return expression in VHDL style
	 */
    @Override
	public String getVHDL(){
		String s = Constant.BR;
		for(int i = 0; i < args.length; i++){
			if(patterns[i] == null) continue;
			s += "        ";
			s += args[i].getVHDL();
			s += " when ";
			//s += patterns[i].getSequencer().getPrevStateKey().getVHDL();
			s += prevStateKey.getVHDL();
			s += " = ";
			s += patterns[i].getStateId().getVHDL();
			s += " else" + Constant.BR;
		}
		s += "        ";
		s += dest.getVHDL();
		return s;
	}

	/**
	 *
	 * @return expression in Verilog-HDL style
	 */
    @Override
	public String getVerilogHDL(){
		String s = Constant.BR;
		for(int i = 0; i < args.length; i++){
			if(patterns[i] == null) continue;
			s += "        ";
			//s += patterns[i].getSequencer().getPrevStateKey().getVerilogHDL();
			s += prevStateKey.getVerilogHDL();
			s += " == ";
			s += patterns[i].getStateId().getVerilogHDL();
			s += " ? ";
			s += args[i].getVerilogHDL();
			s += " :" + Constant.BR;
		}
		s += "        ";
		s += dest.getVerilogHDL();
		return s;
	}

	/**
	 *
	 * @return result expression
	 */
    @Override
	public HDLExpr getResultExpr(){
		return result;
	}

	/**
	 *
	 * @return result type of this expression
	 */
	@Override
	public HDLType getType(){
		return result.getType();
	}

    private void getSrcSignals(ArrayList<HDLSignal> list, HDLExpr arg) {
        HDLSignal[] src = arg.getSrcSignals();
        if (src != null) {
            for (HDLSignal s : src) {
                list.add(s);
            }
        }
        if (arg.getResultExpr() instanceof HDLSignal) {
            list.add((HDLSignal) arg.getResultExpr());
        }
    }

	/**
	 *
	 * @return an array of source signals of this expression
	 */
	@Override
	public HDLSignal[] getSrcSignals(){
        ArrayList<HDLSignal> list = new ArrayList<>();
        for (HDLExpr arg : args) {
            getSrcSignals(list, arg);
        }
        return list.toArray(new HDLSignal[] {});
	}

}
