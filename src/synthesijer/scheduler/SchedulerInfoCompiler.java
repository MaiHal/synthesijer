package synthesijer.scheduler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

import synthesijer.CompileState;
import synthesijer.Manager;
import synthesijer.Manager.HDLModuleInfo;
import synthesijer.SynthesijerUtils;
import synthesijer.ast.Expr;
import synthesijer.ast.Method;
import synthesijer.ast.Type;
import synthesijer.ast.expr.Literal;
import synthesijer.ast.expr.NewArray;
import synthesijer.ast.expr.NewClassExpr;
import synthesijer.ast.type.ArrayRef;
import synthesijer.ast.type.ArrayType;
import synthesijer.ast.type.BitVector;
import synthesijer.ast.type.ComponentRef;
import synthesijer.ast.type.ComponentType;
import synthesijer.ast.type.MySelfType;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLInstance;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLOp;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSequencer;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLType;
import synthesijer.hdl.HDLUtils;
import synthesijer.hdl.HDLVariable;
import synthesijer.hdl.expr.HDLPreDefinedConstant;
import synthesijer.hdl.expr.HDLValue;
import synthesijer.hdl.sequencer.SequencerState;

public class SchedulerInfoCompiler {
	
	private SchedulerInfo info;
	private HDLModule hm;
	
	private HDLInstance fadd32 = null;
	private HDLInstance fsub32 = null;
	private HDLInstance fmul32 = null;
	private HDLInstance fdiv32 = null;
	
	private HDLInstance fadd64 = null;
	private HDLInstance fsub64 = null;
	private HDLInstance fmul64 = null;
	private HDLInstance fdiv64 = null;
	
	private HDLInstance f2i = null;
	private HDLInstance i2f = null;
	private HDLInstance d2l = null;
	private HDLInstance l2d = null;
	private HDLInstance f2d = null;
	private HDLInstance d2f = null;
	
	public SchedulerInfoCompiler(SchedulerInfo info, HDLModule hm){
		this.info = info;
		this.hm = hm;
	}
	
	public void compile(){
		System.out.println("Compile: " + info.getName());
		genDeclarations();
		genStatemachines();
	}
	
	private Hashtable<String, HDLVariable> varTable = new Hashtable<>();

	private void genDeclarations(){
		for(Hashtable<String, VariableOperand> t: info.getVarTableList()){
			for(VariableOperand v: t.values()){
				HDLVariable var = genHDLVariable(v);
				if(var != null) varTable.put(v.getName(), var);
			}
		}
	}
	
	private HDLInstance genHDLVariable(String name, ArrayType t){
		Manager.HDLModuleInfo info = null;
		Type t0 = t.getElemType();
		if(t0 instanceof PrimitiveTypeKind == false){
			throw new RuntimeException("unsupported type: " + t);
		}
		switch((PrimitiveTypeKind)t0){
		case BOOLEAN: info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM1");  break;
		case BYTE:    info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM8");  break;
		case SHORT:   info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM16"); break;
		case INT:     info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM32"); break;
		case LONG:    info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM64"); break;
		case FLOAT:   info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM32"); break;
		case DOUBLE:  info = Manager.INSTANCE.searchHDLModuleInfo("BlockRAM64"); break;
		default: throw new RuntimeException("unsupported type: " + t);
		}
		HDLInstance inst = hm.newModuleInstance(info.hm, name);
		inst.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
		inst.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
		return inst;
	}
	
	private class Pair{
		public final HDLSignal reg; // the actual signal for the method.
		public final HDLPort port;  // port from outside
		public final HDLSignal local; // signal for local invocation
		public Pair(HDLSignal reg, HDLPort port, HDLSignal local){
			this.reg = reg;
			this.port = port;
			this.local = local;
		}
	}
	
	private Hashtable<String, ArrayList<Pair>> paramListMap = new Hashtable<>();
	
	private ArrayList<Pair> getMethodParamPairList(String methodName){
		ArrayList<Pair> list;
		if(paramListMap.containsKey(methodName) == false){
			list = new ArrayList<>();
			paramListMap.put(methodName, list);
		}else{
			list = paramListMap.get(methodName);
		}
		return list;
	}
	
	private HDLVariable genHDLVariable(VariableOperand v){
		String name = v.getName();
		Type type = v.getType();
		if(type instanceof PrimitiveTypeKind){
			if(type == PrimitiveTypeKind.DECLARED){
				//SynthesijerUtils.warn("Declaration is skipped: " + name + "::" + type);
				return null;
			}
			if(type == PrimitiveTypeKind.VOID) return null; // Void variable is not synthesized.
			HDLSignal sig = hm.newSignal(name, getHDLType(type));
			if(v.getVariable() != null && v.getVariable().isMethodParam()){
				if(v.getVariable().getMethod().isPrivate()){
					String prefix = v.getVariable().getMethod().getName();
					String n = prefix + "_" + v.getVariable().getName();
					HDLSignal local = hm.newSignal(n + "_local", getHDLType(type));
					getMethodParamPairList(prefix).add(new Pair(sig, null, local));
				}else{
					String prefix = v.getVariable().getMethod().getName();
					String n = prefix + "_" + v.getVariable().getName();
					HDLPort port = hm.newPort(n, HDLPort.DIR.IN, getHDLType(type));
					HDLSignal local = hm.newSignal(n + "_local", getHDLType(type));
					getMethodParamPairList(prefix).add(new Pair(sig, port, local));
				}
			}else if(v.getVariable() != null && v.getVariable().isPublic() && (!v.getVariable().isGlobalConstant())){
				String n = v.getVariable().getName();
				HDLPort din = hm.newPort(n + "_in", HDLPort.DIR.IN, getHDLType(type));
				HDLPort we = hm.newPort(n + "_we", HDLPort.DIR.IN, HDLPrimitiveType.genBitType());
				HDLPort dout = hm.newPort(n + "_out", HDLPort.DIR.OUT, getHDLType(type));
				dout.getSignal().setAssign(null, sig);
				// always(clk'posedge) sig <= din when we = '1' else sig
				HDLSignal mux = hm.newSignal(name + "_mux", getHDLType(type), HDLSignal.ResourceKind.WIRE); 
				mux.setAssign(null, hm.newExpr(HDLOp.IF, we.getSignal(), din.getSignal(), sig));
				sig.setDefaultValue(mux);
			}
			return sig;
		}else if(type instanceof ArrayType){
			HDLInstance array = genHDLVariable(name, (ArrayType)type);
			NewArray expr = (NewArray)(v.getVariable().getInitExpr());
			if(expr.getDimExpr().get(0) instanceof Literal){
				Literal value = (Literal)(expr.getDimExpr().get(0));
				array.getParameterPair("WORDS").setValue(value.getValueAsStr());
				int dims = Integer.valueOf(value.getValueAsStr());
				int depth = (int)Math.ceil(Math.log(dims) / Math.log(2.0));
				array.getParameterPair("DEPTH").setValue(String.valueOf(depth));
			}else{
				SynthesijerUtils.warn("unsupported to init array with un-immediate number:" + expr.getDimExpr());
				SynthesijerUtils.warn("the size of memory is set as default parameter(DEPTH=1024)");
			}
			if(v.getVariable() != null && v.getVariable().isPublic() && (!v.getVariable().isGlobalConstant())){
				String n = v.getVariable().getName();
				HDLPort addr = hm.newPort(n + "_address", HDLPort.DIR.IN, array.getSignalForPort("address").getType());
				HDLPort we = hm.newPort(n + "_we", HDLPort.DIR.IN, array.getSignalForPort("we").getType());
				HDLPort oe = hm.newPort(n + "_oe", HDLPort.DIR.IN, array.getSignalForPort("oe").getType());
				HDLPort din = hm.newPort(n + "_din", HDLPort.DIR.IN, array.getSignalForPort("din").getType());
				HDLPort dout = hm.newPort(n + "_dout", HDLPort.DIR.OUT, array.getSignalForPort("dout").getType());
				HDLPort length = hm.newPort(n + "_length", HDLPort.DIR.OUT, array.getSignalForPort("length").getType());
				array.getSignalForPort("address").setAssign(null, addr.getSignal());
				array.getSignalForPort("we").setAssign(null, we.getSignal());
				array.getSignalForPort("oe").setAssign(null, oe.getSignal());
				array.getSignalForPort("din").setAssign(null, din.getSignal());
				dout.getSignal().setAssign(null, array.getSignalForPort("dout"));				
				length.getSignal().setAssign(null, array.getSignalForPort("length"));				
			}
			return array;
		}else if(type instanceof ComponentType){
			ComponentType c = (ComponentType)type;
			Manager.HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo(c.getName());
			if(info == null){
				SynthesijerUtils.error(c.getName() + " is not found.");
				Manager.INSTANCE.HDLModuleInfoList();
				System.exit(0);
			}
			if(info.getCompileState().isBefore(CompileState.GENERATE_HDL)){
				SynthesijerUtils.info("enters into >>>");
				Manager.INSTANCE.compileSchedulerInfo(c.getName(), info);
				SynthesijerUtils.info("<<< return to compiling " + this.info.getName());
			}
			HDLInstance inst = hm.newModuleInstance(info.hm, name);
			NewClassExpr expr = (NewClassExpr)v.getVariable().getInitExpr();
			if(expr.getParameters().size() > 0){
				NewArray param = (NewArray)(expr.getParameters().get(0));
				ArrayList<Expr> elem = param.getElems();
				for(int i = 0; i < elem.size()/2; i ++){
					String key = ((Literal)elem.get(2*i)).getValueAsStr();
					String value = ((Literal)elem.get(2*i+1)).getValueAsStr();
					if(inst.getParameterPair(key) == null){
						SynthesijerUtils.error(key + " is not defined in " + inst.getSubModule().getName());
						System.exit(0);
					}
					inst.getParameterPair(key).setValue(value);
				}
			}

			for(HDLPort p: inst.getSubModule().getPorts()){
				if(p.isSet(HDLPort.OPTION.EXPORT)){
					String n = inst.getSignalForPort(p.getName()).getName();
					HDLPort export = hm.newPort(n + "_exp", p.getDir(), p.getType(), EnumSet.of(HDLPort.OPTION.EXPORT));
					if(p.getDir() == HDLPort.DIR.OUT){
						export.getSignal().setAssign(null, inst.getSignalForPort(p.getName()));
					}else{
						inst.getSignalForPort(p.getName()).setAssign(null, export.getSignal());
					}
				}
			}
			
			inst.getSignalForPort(inst.getSubModule().getSysClkName()).setAssign(null, hm.getSysClk().getSignal());
			inst.getSignalForPort(inst.getSubModule().getSysResetName()).setAssign(null, hm.getSysReset().getSignal());
			return inst;
		}else if(type instanceof ArrayRef){
			Type t = ((ArrayRef) type).getRefType().getElemType();
			HDLSignal sig = hm.newSignal(name, getHDLType(t));
			return sig;
		}else if(type instanceof ComponentRef){
			ComponentRef cr = (ComponentRef)type;
			HDLSignal sig = null;
			if(cr.getRefType() instanceof PrimitiveTypeKind){
				sig = hm.newSignal(name, getHDLType(cr.getRefType()));
			}else if(cr.getRefType() instanceof ArrayType){
				Type t = ((ArrayType) cr.getRefType()).getElemType();
				sig = hm.newSignal(name, getHDLType(t));
			}else{
				System.out.println("unknown ref type: " + name + ":" + cr.getRefType());
			}
			return sig;
		}else if(type instanceof BitVector){
			HDLSignal sig = hm.newSignal(name, HDLPrimitiveType.genVectorType(((BitVector) type).getWidth()));
			return sig;
		}else{
			throw new RuntimeException("unsupported type: " + type + " of " + name);
		}
	}

	private HDLType getHDLType(Type type){
		if(type instanceof PrimitiveTypeKind){
			return getHDLType((PrimitiveTypeKind)type);
		}else if(type instanceof ArrayType){
			return getHDLType((ArrayType)type);
		}else if(type instanceof ComponentType){
			return getHDLType((ComponentType)type);
		}else if(type instanceof MySelfType){
			return getHDLType((MySelfType)type);
		}else{
			return null;
		}
	}

	private HDLPrimitiveType getHDLType(PrimitiveTypeKind t){
		switch(t){
		case BOOLEAN: return HDLPrimitiveType.genBitType(); 
		case BYTE: return HDLPrimitiveType.genSignedType(8); 
		case CHAR: return HDLPrimitiveType.genVectorType(16);
		case SHORT: return HDLPrimitiveType.genSignedType(16);
		case INT: return HDLPrimitiveType.genSignedType(32);
		case LONG: return HDLPrimitiveType.genSignedType(64);
		case FLOAT: return HDLPrimitiveType.genVectorType(32);
		case DOUBLE: return HDLPrimitiveType.genVectorType(64);
		default: return null; // return HDLPrimitiveType.genUnknowType();
		}
	}


	private HDLPrimitiveType getHDLType(MySelfType t){
		System.err.println("unsupported type: " + t);
		return null;
	}
	
	private HDLPrimitiveType getHDLType(ComponentType t){
		System.err.println("unsupported type: " + t.getName() + "::ComponentType");
		return null;
	}
	
	private HDLPrimitiveType getHDLType(ArrayType t){
		System.err.println("unsupported type: " + t);
		return null;
	}
	
	private void genStatemachines(){
		for(SchedulerBoard board: info.getBoardsList()){		
			genMethodCtrlSignals(board);
		}
		for(SchedulerBoard board: info.getBoardsList()){
			SequencerState[] states = genStatemachine(board);
			genExprs(board, states);
		}
	}
		
	private HDLExpr convOperandToHDLExpr(Operand o){
		HDLExpr ret;
		if(o instanceof VariableOperand){
			ret = varTable.get(((VariableOperand)o).getName());
		}else{ // instanceof ConstantOperand
			ConstantOperand c = (ConstantOperand)o;
			ret = new HDLValue(c.getValue(), (HDLPrimitiveType)getHDLType(c.getType()));
		}
		return ret;
	}
	
	private void genExprs(SchedulerBoard board, SequencerState[] states){
		Method m = board.getMethod();
		HDLSignal return_sig = null;
		if(m.getType() != PrimitiveTypeKind.VOID){
			if(board.getMethod().isPrivate() == false){
				HDLPort return_port = hm.newPort(board.getName() + "_return", HDLPort.DIR.OUT, getHDLType(m.getType()));
				return_sig = return_port.getSignal();
			}else{
				return_sig = hm.newSignal(board.getName() + "_return", getHDLType(m.getType()));
			}
		}
		
		Hashtable<String, FieldAccessItem> fieldAccessChainMap = new Hashtable<>();
		for(SchedulerSlot slot: board.getSlots()){
			for(SchedulerItem item: slot.getItems()){
				genExpr(board, item, states[item.getStepId()], return_sig, paramListMap.get(board.getName()), fieldAccessChainMap);
			}
		}
		
	}
	
	private HDLOp convOp2HDLOp(Op op){
		HDLOp ret = HDLOp.UNDEFINED;
		switch(op){
		case METHOD_ENTRY : break;
		case METHOD_EXIT : break;
		case ASSIGN : break;
		case NOP : break;
		case ADD : ret = HDLOp.ADD;break;
		case SUB : ret = HDLOp.SUB;break;
		case MUL : ret = HDLOp.MUL;break;
		case DIV : break;
		case MOD : break;
		case LT : ret = HDLOp.LT;break;
		case LEQ : ret = HDLOp.LEQ;break;
		case GT : ret = HDLOp.GT;break;
		case GEQ : ret = HDLOp.GEQ;break;
		case COMPEQ : ret = HDLOp.EQ;break;
		case NEQ : ret = HDLOp.NEQ;break;
		case LSHIFT : ret = HDLOp.LSHIFT;break;
		case LOGIC_RSHIFT : ret = HDLOp.LOGIC_RSHIFT;break;
		case ARITH_RSHIFT : ret = HDLOp.ARITH_RSHIFT;break;
		case JP : break;
		case JT : break;
		case RETURN : break;
		case SELECT : break;
		case AND : ret = HDLOp.AND;break;
		case NOT : ret = HDLOp.NOT;break;
		case LAND : ret = HDLOp.AND;break;
		case LOR : ret = HDLOp.OR;break;
		case OR : ret = HDLOp.OR;break;
		case XOR : ret = HDLOp.XOR;break;
		case LNOT : ret = HDLOp.NOT;break;
		case ARRAY_ACCESS : break;
		case CALL : break;
		case EXT_CALL : break;
		case FIELD_ACCESS : break;
		case BREAK : break;
		case CONTINUE : break;
		case CAST : break;
		case UNDEFINED : break;
		default:
		}
		return ret;
	}
	
	private int getBitWidth(Type t){
		if(t instanceof PrimitiveTypeKind){
			return ((PrimitiveTypeKind) t).getWidth();
		}else if(t instanceof BitVector){
			return ((BitVector) t).getWidth();
		}else if(t instanceof ArrayRef){
			return getBitWidth(((ArrayRef) t).getRefType());
		}else if(t instanceof ArrayType){
			return getBitWidth(((ArrayType) t).getElemType());
		}else{
			System.out.println(t);
			return -1;
		}
	}
	
	private boolean isFloating(Type t){
		if(t instanceof PrimitiveTypeKind == false) return false;
		return ((PrimitiveTypeKind)t).isFloating();
	}

	private void genExpr(SchedulerBoard board, SchedulerItem item, SequencerState state, HDLSignal return_sig, ArrayList<Pair> paramList, Hashtable<String, FieldAccessItem> fieldAccessChainMap){
		switch(item.getOp()){
		case METHOD_ENTRY:{
			if(paramList != null){
				for(Pair pair: paramList){
					// MUX to select valid siganl from inside/outside arguments
					HDLExpr arg;
					if(board.getMethod().isPrivate() == false){
						arg = hm.newExpr(HDLOp.IF, varTable.get(board.getName()+"_req"), pair.port.getSignal(), pair.local);
					}else{
						arg = pair.local;
					}
					pair.reg.setAssign(state, arg);
				}
			}
			break;
		}
		case METHOD_EXIT:{
			break;
		}
		case ASSIGN : {
			Operand[] src = item.getSrcOperand();
			VariableOperand dest = item.getDestOperand();
			if(dest.getType() instanceof PrimitiveTypeKind || dest.getType() instanceof BitVector){
				
				HDLVariable d;
				FieldAccessItem fa = fieldAccessChainMap.get(dest.getName());
				if(fa != null){
					// should write into field variable pointed by FIELD_ACCESS
					HDLInstance obj = (HDLInstance)(varTable.get(fa.obj.getName()));
					d = obj.getSignalForPort(fa.name);
					if(d != null){
						// for unsynthesized HDLModule
					}else{
						d = obj.getSignalForPort(fa.name + "_in");
						HDLSignal we = obj.getSignalForPort(fa.name + "_we");
						we.setAssign(state, HDLPreDefinedConstant.HIGH); // in this state
						we.setDefaultValue(HDLPreDefinedConstant.LOW); // otherwise
					}
				}else{
					d = (HDLVariable)(convOperandToHDLExpr(dest));
				}
				d.setAssign(state, convOperandToHDLExpr(src[0]));
				
			}else if(dest.getType() instanceof ArrayRef){
				VariableRefOperand d = (VariableRefOperand)dest;
				VariableOperand ref = d.getRef();
				
				// The address to access should be settled by ARRAY_ACCESS
				//HDLSignal addr = ...
				
				HDLSignal we = null, din = null;
				HDLVariable var = varTable.get(ref.getName());
				if(var instanceof HDLInstance){
					// local memory
					HDLInstance array = (HDLInstance)var;
					we = array.getSignalForPort("we_b");
					din = array.getSignalForPort("din_b");
				}else{
					// external memory (through Field Access)
					FieldAccessItem fa = fieldAccessChainMap.get(ref.getName());
					HDLInstance obj = (HDLInstance)(varTable.get(fa.obj.getName()));
					we = obj.getSignalForPort(fa.name + "_we");
					din = obj.getSignalForPort(fa.name + "_din");
				}

				we.setAssign(state, HDLPreDefinedConstant.HIGH);
				we.setDefaultValue(HDLPreDefinedConstant.LOW);
				din.setAssign(state, convOperandToHDLExpr(src[0]));
			}else{
				SynthesijerUtils.warn("Unsupported ASSIGN: " + item.info());
			}
		}
		case NOP :{
			break;
		}
		case DIV :{
			System.out.println("DIV is not implemented yet.");
			break;
		}
		case MOD :{
			System.out.println("MOD is not implemented yet.");
			break;
		}
		case JP :{
			break;
		}
		case JT :{
			break;
		}
		case RETURN : {
			if(return_sig == null) break;
			Operand[] src = item.getSrcOperand();
			return_sig.setAssign(state, convOperandToHDLExpr(src[0]));
		}
		case SELECT :{
			break;
		}
		case ARRAY_ACCESS :{

			state.setMaxConstantDelay(2);
			HDLSignal dest = (HDLSignal)convOperandToHDLExpr(item.getDestOperand());
			Operand src[] = item.getSrcOperand();
			
			HDLSignal addr = null, oe = null, dout = null;
			if(varTable.get(((VariableOperand)src[0]).getName()) instanceof HDLInstance){
				// local memory
				HDLInstance array;
				array = (HDLInstance)(varTable.get(((VariableOperand)src[0]).getName()));
				addr = array.getSignalForPort("address_b");
				oe = array.getSignalForPort("oe_b");
				dout = array.getSignalForPort("dout_b");
			}else{
				// external memory (through Field Access)
				FieldAccessItem fa = fieldAccessChainMap.get(((VariableOperand)src[0]).getName());
				HDLInstance obj = (HDLInstance)(varTable.get(fa.obj.getName()));
				addr = obj.getSignalForPort(fa.name + "_address");
				oe = obj.getSignalForPort(fa.name + "_oe");
				dout = obj.getSignalForPort(fa.name + "_dout");
			}

			HDLExpr index = convOperandToHDLExpr(src[1]);
			
			addr.setAssign(state, 0, index);
			oe.setAssign(state, 0, HDLPreDefinedConstant.HIGH);
			oe.setDefaultValue(HDLPreDefinedConstant.LOW);
			
			dest.setAssign(state, 2, dout);
			
			break;
		}
		case CALL :{
			MethodInvokeItem item0 = (MethodInvokeItem)item;
			Operand[] params = item0.getSrcOperand();
			ArrayList<Pair> list = getMethodParamPairList(item0.name);
			for(int i = 0; i < params.length; i++){
				HDLSignal t = list.get(i).local;
				HDLExpr s = convOperandToHDLExpr(params[i]);
				t.setAssign(state, 0, s);
			}
			if(item0.getDestOperand().getType() != PrimitiveTypeKind.VOID){
				HDLSignal dest = (HDLSignal)convOperandToHDLExpr(item0.getDestOperand());
				HDLSignal ret = hm.getSignal(item0.name + "_return");
				dest.setAssign(state.getTransitions().get(0).getDestState(), ret); // should be read in ***_body
				//dest.setAssign(state, ret);
			}
			break;
		}
		case EXT_CALL :{
			MethodInvokeItem item0 = (MethodInvokeItem)item;
			HDLInstance obj = (HDLInstance)(varTable.get(item0.obj.getName()));
			Operand[] params = item0.getSrcOperand();
			for(int i = 0; i < item0.args.length; i++){
				HDLSignal t = obj.getSignalForPort(item0.name + "_" + item0.args[i]);
				t.setAssign(state, 0, convOperandToHDLExpr(params[i]));
			}
			if(item0.getDestOperand().getType() != PrimitiveTypeKind.VOID){ // non-void function
				HDLSignal dest = (HDLSignal)convOperandToHDLExpr(item0.getDestOperand());
				HDLSignal ret = obj.getSignalForPort(item0.name + "_return");
				dest.setAssign(state.getTransitions().get(0).getDestState(), ret); // should be read in ***_body
				//dest.setAssign(state, ret);
			}
			break;
		}
		case FIELD_ACCESS :{
			FieldAccessItem item0 = (FieldAccessItem)item;
			HDLInstance obj = (HDLInstance)(varTable.get(item0.obj.getName()));
			HDLSignal src = obj.getSignalForPort(item0.name); // only for array.length
			if(src == null) src = obj.getSignalForPort(item0.name + "_out"); // normal 
			HDLExpr dest = convOperandToHDLExpr(item0.getDestOperand());
			if(dest instanceof HDLSignal && src != null){
				HDLSignal d = (HDLSignal)dest;
				d.setAssign(state, src);
			}else{
				// just ref
			}
			// stored this item into map to use in following items
			fieldAccessChainMap.put(item0.getDestOperand().getName(), item0);
			break;
		}
		case BREAK :{
			break;
		}
		case CONTINUE :{
			break;
		}
		case CAST:{
			TypeCastItem item0 = (TypeCastItem)item;
			HDLSignal dest = (HDLSignal)(convOperandToHDLExpr(item.getDestOperand()));
			HDLExpr src = convOperandToHDLExpr(item.getSrcOperand()[0]);
			int w0 = getBitWidth(item0.orig);
			int w1 = getBitWidth(item0.target);
			if(w0 < 0 || w1 < 0){
				SynthesijerUtils.error("Unsupported CAST: " + item.info());
			}
			if(w0 > w1){
				dest.setAssign(state, hm.newExpr(HDLOp.DROPHEAD, src, HDLUtils.newValue(w0-w1, 32)));
			}else{
				dest.setAssign(state, hm.newExpr(HDLOp.PADDINGHEAD, src, HDLUtils.newValue(w0-w1, 32)));
			}
			break;
		}
		case UNDEFINED :{
			System.out.println("UNDEFINED : " + item.info());
			break;
		}
		case FADD32 :
		case FSUB32 :
		case FMUL32 :
		case FDIV32 :
		case FADD64 :
		case FSUB64 :
		case FMUL64 :
		case FDIV64 :
		{
			Operand[] arg = item.getSrcOperand();
			HDLInstance inst = getFloatingInstance(item.getOp());
			inst.getSignalForPort("a").setAssign(state, 0, convOperandToHDLExpr(arg[0]));
			inst.getSignalForPort("b").setAssign(state, 0, convOperandToHDLExpr(arg[1]));
			inst.getSignalForPort("nd").setAssign(state, 0, HDLPreDefinedConstant.HIGH);
			inst.getSignalForPort("nd").setDefaultValue(HDLPreDefinedConstant.LOW);
			HDLSignal dest = (HDLSignal)convOperandToHDLExpr(item.getDestOperand());
			dest.setAssign(state, inst.getSignalForPort("result"));
			break;
		}
		case CONV_F2I:
		case CONV_I2F:
		case CONV_D2L:
		case CONV_L2D:
		case CONV_F2D:
		case CONV_D2F:
		{
			Operand[] arg = item.getSrcOperand();
			HDLInstance inst = getFloatingInstance(item.getOp());
			inst.getSignalForPort("a").setAssign(state, 0, convOperandToHDLExpr(arg[0]));
			inst.getSignalForPort("nd").setAssign(state, 0, HDLPreDefinedConstant.HIGH);
			inst.getSignalForPort("nd").setDefaultValue(HDLPreDefinedConstant.LOW);
			HDLSignal dest = (HDLSignal)convOperandToHDLExpr(item.getDestOperand());
			dest.setAssign(state, inst.getSignalForPort("result"));
			break;
		}
		default: {
			HDLOp op = convOp2HDLOp(item.getOp());
//			if(op == HDLOp.UNDEFINED) return;
			HDLVariable dest = (HDLVariable)(convOperandToHDLExpr(item.getDestOperand()));
			Operand[] src = item.getSrcOperand();
			int nums = op.getArgNums();
			if(nums == 1){
				dest.setAssign(state, hm.newExpr(op, convOperandToHDLExpr(src[0])));  
			}else{
				dest.setAssign(state, hm.newExpr(op, convOperandToHDLExpr(src[0]), convOperandToHDLExpr(src[1])));  
			}
		}
		}
	}

	private void genMethodCtrlSignals(SchedulerBoard board){
		
		HDLSignal req_flag = hm.newSignal(board.getName() + "_req_flag", HDLPrimitiveType.genBitType());
		HDLSignal req_local = hm.newSignal(board.getName() + "_req_local", HDLPrimitiveType.genBitType());
		if(board.getMethod().isPrivate() == false){
			HDLPort req_port = hm.newPort(board.getName() + "_req", HDLPort.DIR.IN, HDLPrimitiveType.genBitType());
			HDLPort busy_port = hm.newPort(board.getName() + "_busy", HDLPort.DIR.OUT, HDLPrimitiveType.genBitType());
			busy_port.getSignal().setResetValue(HDLPreDefinedConstant.HIGH);
			varTable.put(req_port.getName(), req_port.getSignal());
			varTable.put(busy_port.getName(), busy_port.getSignal());
			req_flag.setAssign(null, hm.newExpr(HDLOp.OR, req_local, req_port.getSignal()));
		}else{
			req_flag.setAssign(null, req_local);
			HDLSignal busy_sig = hm.newSignal(board.getName() + "_busy", HDLPrimitiveType.genBitType());
			varTable.put(busy_sig.getName(), busy_sig);
		}
		
		varTable.put(req_flag.getName(), req_flag);
		varTable.put(req_local.getName(), req_local);

	}
	
	private SequencerState[] genStatemachine(SchedulerBoard board){
		HDLSequencer seq = hm.newSequencer(board.getName() + "_method");
		IdGen id = new IdGen("S");
		SequencerState[] states = new SequencerState[board.getSlots().length];
		
		HDLVariable req_flag = varTable.get(board.getName() + "_req_flag");
		HDLVariable busy_port_sig = varTable.get(board.getName() + "_busy");
		
		for(SchedulerSlot slot: board.getSlots()){
			for(SchedulerItem item: slot.getItems()){
				states[item.getStepId()] = seq.addSequencerState(id.get(item.getStepId()));
			}
		}
		for(SchedulerSlot slot: board.getSlots()){
			for(SchedulerItem item: slot.getItems()){
				SequencerState s = states[item.getStepId()];
				switch(item.getOp()){
				case METHOD_EXIT: {
					Method m = board.getMethod();
					if(m.getWaitWithMethod() == null){ // independent method (normal)
						s.addStateTransit(states[item.getStepId()+1]);
					}else{ // must wait for other method.
						HDLVariable flag = varTable.get(m.getWaitWithMethod().getName() + "_busy");
						HDLExpr unlock = hm.newExpr(HDLOp.EQ, flag, HDLPreDefinedConstant.LOW); // the waiting method has been done.
						s.addStateTransit(unlock, states[item.getStepId()+1]);
						busy_port_sig.setAssign(s, hm.newExpr(HDLOp.IF, unlock, HDLPreDefinedConstant.LOW, HDLPreDefinedConstant.HIGH));
					}
					break;
				}
				case METHOD_ENTRY:{
					s.addStateTransit(req_flag, states[item.getBranchId()[0]]);
					busy_port_sig.setAssign(s, req_flag);
					break;
				}
				case SELECT:{
					SelectItem item0 = (SelectItem)item;
					for(int i = 0; i < item0.pat.length; i++){
						HDLExpr cond = convOperandToHDLExpr(item0.target);
						HDLExpr pat = convOperandToHDLExpr(item0.pat[i]);
						s.addStateTransit(hm.newExpr(HDLOp.EQ, cond, pat), states[item.getBranchId()[i]]);
					}
					s.addStateTransit(states[item0.getBranchId()[item0.pat.length]]);
					break;
				}
				case JT:{
					HDLExpr flag = convOperandToHDLExpr(item.getSrcOperand()[0]);
					s.addStateTransit(hm.newExpr(HDLOp.EQ, flag, HDLPreDefinedConstant.HIGH), states[item.getBranchId()[0]]);
					s.addStateTransit(hm.newExpr(HDLOp.EQ, flag, HDLPreDefinedConstant.LOW), states[item.getBranchId()[1]]);
					break;
				}
				case JP:
					s.addStateTransit(states[item.getBranchId()[0]]);
					break;
				case CALL:
				case EXT_CALL:
				{
					SequencerState call_body = seq.addSequencerState(s.getStateId().getValue()+"_body");
					MethodInvokeItem item0 = (MethodInvokeItem)item;
					HDLVariable call_req, call_busy;
					String flag_name;
					if(item0.getOp() == Op.EXT_CALL){
						HDLInstance obj = (HDLInstance)(varTable.get(item0.obj.getName()));
						call_req = obj.getSignalForPort(item0.name + "_req");
						call_busy = obj.getSignalForPort(item0.name + "_busy");
						flag_name = String.format("%s_ext_call_flag_%04d", obj.getName(), item.getStepId());
					}else{
						call_req = varTable.get(item0.name + "_req_local");
						call_busy = varTable.get(item0.name + "_busy");
						flag_name = String.format("%s_call_flag_%04d", item0.name, item.getStepId());
					}
					HDLSignal flag = (HDLSignal)varTable.get(flag_name);
					if(flag == null){
						flag = hm.newSignal(flag_name, HDLPrimitiveType.genBitType(), HDLSignal.ResourceKind.WIRE);
						flag.setAssign(null, hm.newExpr(HDLOp.EQ,
					             hm.newExpr(HDLOp.AND,
					             hm.newExpr(HDLOp.EQ, call_busy, HDLPreDefinedConstant.LOW),
					             hm.newExpr(HDLOp.EQ, call_req, HDLPreDefinedConstant.LOW)),
					             HDLPreDefinedConstant.HIGH));
						varTable.put(flag_name, flag);
					}
					
					// when busy = '0', s -> call_body
					s.addStateTransit(hm.newExpr(HDLOp.EQ, call_busy, HDLPreDefinedConstant.LOW), call_body);
					
					// call_body
					call_req.setAssign(call_body, 0, HDLPreDefinedConstant.HIGH);
					call_req.setDefaultValue(HDLPreDefinedConstant.LOW); // otherwise '0'
					call_body.setMaxConstantDelay(1);
					if(item0.isNoWait() == true){
						//System.out.println("no wait:" + call_req);
					}else{
						call_body.setStateExitFlag(flag);
					}
					call_body.addStateTransit(states[item.getStepId()+1]);
					break;
				}
				case FADD32 :
				case FSUB32 :
				case FMUL32 :
				case FDIV32 :
				case FADD64 :
				case FSUB64 :
				case FMUL64 :
				case FDIV64 :
				case CONV_F2I :
				case CONV_I2F :
				case CONV_D2L :
				case CONV_L2D :
				case CONV_F2D :
				case CONV_D2F :
				{
					s.setMaxConstantDelay(item.getOp().latency);
					s.addStateTransit(states[item.getStepId()+1]);
					HDLInstance inst = getFloatingInstance(item.getOp());
					s.setStateExitFlag(inst.getSignalForPort("valid"));
					break;
				}
				default:
					if(item.isBranchOp()){
						s.addStateTransit(states[item.getBranchId()[0]]);
					}else{
						s.addStateTransit(states[item.getStepId()+1]);
					}
				}
			}
		}
		seq.getIdleState().addStateTransit(states[0]);
		return states;
	}

	private HDLInstance getFloatingInstance(Op op){
		switch(op){
		case FADD32:{
			if(fadd32 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FADD32");
				fadd32 = hm.newModuleInstance(info.hm, "synthesijer_fadd32");
				fadd32.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fadd32.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fadd32;
		}
		case FSUB32:{
			if(fsub32 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FSUB32");
				fsub32 = hm.newModuleInstance(info.hm, "synthesijer_fsub32");
				fsub32.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fsub32.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fsub32;
		}
		case FMUL32:{
			if(fmul32 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FMUL32");
				fmul32 = hm.newModuleInstance(info.hm, "synthesijer_fmul32");
				fmul32.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fmul32.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fmul32;
		}
		case FDIV32:{
			if(fdiv32 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FDIV32");
				fdiv32 = hm.newModuleInstance(info.hm, "synthesijer_fdiv32");
				fdiv32.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fdiv32.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fdiv32;
		}
		case FADD64:{
			if(fadd64 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FADD64");
				fadd64 = hm.newModuleInstance(info.hm, "synthesijer_fadd64");
				fadd64.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fadd64.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fadd64;
		}
		case FSUB64:{
			if(fsub64 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FSUB64");
				fsub64 = hm.newModuleInstance(info.hm, "synthesijer_fsub64");
				fsub64.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fsub64.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fsub64;
		}
		case FMUL64:{
			if(fmul64 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FMUL64");
				fmul64 = hm.newModuleInstance(info.hm, "synthesijer_fmul64");
				fmul64.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fmul64.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fmul64;
		}
		case FDIV64:{
			if(fdiv64 == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FDIV64");
				fdiv64 = hm.newModuleInstance(info.hm, "synthesijer_fdiv64");
				fdiv64.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				fdiv64.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return fdiv64;
		}
		case CONV_F2I:{
			if(f2i == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FCONV_F2I");
				f2i = hm.newModuleInstance(info.hm, "synthesijer_fconv_f2i");
				f2i.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				f2i.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return f2i;
		}
		case CONV_I2F:{
			if(i2f == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FCONV_I2F");
				i2f = hm.newModuleInstance(info.hm, "synthesijer_fconv_i2f");
				i2f.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				i2f.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return i2f;
		}
		case CONV_L2D:{
			if(l2d == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FCONV_L2D");
				l2d = hm.newModuleInstance(info.hm, "synthesijer_fconv_l2d");
				l2d.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				l2d.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return l2d;
		}
		case CONV_D2L:{
			if(d2l == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FCONV_D2L");
				d2l = hm.newModuleInstance(info.hm, "synthesijer_fconv_d2l");
				d2l.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				d2l.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return d2l;
		}
		case CONV_F2D:{
			if(f2d == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FCONV_F2D");
				f2d = hm.newModuleInstance(info.hm, "synthesijer_fconv_f2d");
				f2d.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				f2d.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return f2d;
		}
		case CONV_D2F:{
			if(d2f == null){
				HDLModuleInfo info = Manager.INSTANCE.searchHDLModuleInfo("FCONV_D2F");
				d2f = hm.newModuleInstance(info.hm, "synthesijer_fconv_d2f");
				d2f.getSignalForPort("clk").setAssign(null, hm.getSysClk().getSignal());
				d2f.getSignalForPort("reset").setAssign(null, hm.getSysReset().getSignal());
			}
			return d2f;
		}
		default: return null;
		}
	}


	private class IdGen{
		String prefix; 
		public IdGen(String prefix){
			this.prefix = prefix;
		}
		public String get(int id){
			String v = String.format("%s_%04d", prefix, id);
			return v;
		}
	}

}
