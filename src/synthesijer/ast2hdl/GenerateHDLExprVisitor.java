package synthesijer.ast2hdl;

import synthesijer.ast.Expr;
import synthesijer.ast.Method;
import synthesijer.ast.Op;
import synthesijer.ast.Type;
import synthesijer.ast.Variable;
import synthesijer.ast.expr.ArrayAccess;
import synthesijer.ast.expr.AssignExpr;
import synthesijer.ast.expr.AssignOp;
import synthesijer.ast.expr.BinaryExpr;
import synthesijer.ast.expr.FieldAccess;
import synthesijer.ast.expr.Ident;
import synthesijer.ast.expr.Literal;
import synthesijer.ast.expr.MethodInvocation;
import synthesijer.ast.expr.NewArray;
import synthesijer.ast.expr.NewClassExpr;
import synthesijer.ast.expr.ParenExpr;
import synthesijer.ast.expr.SynthesijerExprVisitor;
import synthesijer.ast.expr.TypeCast;
import synthesijer.ast.expr.UnaryExpr;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.ast.type.StringType;
import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLInstance;
import synthesijer.hdl.HDLOp;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSequencer;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLVariable;
import synthesijer.hdl.expr.HDLPreDefinedConstant;
import synthesijer.hdl.expr.HDLValue;

public class GenerateHDLExprVisitor implements SynthesijerExprVisitor{
		
	private final GenerateHDLModuleVisitor parent;
	private final HDLSequencer.SequencerState state;
	
	private HDLExpr result;
	
	public GenerateHDLExprVisitor(GenerateHDLModuleVisitor parent, HDLSequencer.SequencerState state){
		this.parent = parent;
		this.state = state;
	}
	
	public HDLExpr getResult(){
		return result;
	}
	
	private HDLExpr stepIn(Expr expr){
		GenerateHDLExprVisitor visitor = new GenerateHDLExprVisitor(parent, state);
		expr.accept(visitor);
		return visitor.getResult();
	}

	@Override
	public void visitArrayAccess(ArrayAccess o) {
		if(o.getIndexed() instanceof Ident){
			ArrayAccess aa = (ArrayAccess)o;
			Ident id = (Ident)aa.getIndexed();
			HDLVariable var = parent.getHDLVariable(o.getScope().search(id.getSymbol()));
			HDLInstance inst = (HDLInstance)var;
			// address
			HDLSignal addr = inst.getSignalForPort("raddress"); // see synthsijer.lib.BlockRAM
			addr.setAssign(state, stepIn(aa.getIndex()));
			// write-enable
			HDLSignal we = inst.getSignalForPort("we"); // see synthsijer.lib.BlockRAM
			we.setAssign(state, HDLPreDefinedConstant.LOW);
			// data
			result = inst.getSignalForPort("dout"); // see synthsijer.lib.BlockRAM
			state.setMaxConstantDelay(2);
		}else{
			throw new RuntimeException(String.format("%s(%s) cannot convert to HDL.", o.getIndexed(), o.getIndexed().getClass()));
		}
	}
		
	private HDLOp convOp(Op op){
		switch(op){
		case PLUS : return HDLOp.ADD;
		case MINUS : return HDLOp.SUB;
		case AND : return HDLOp.AND;
		case LAND : return HDLOp.AND;		
		case LOR : return HDLOp.OR;
		case OR : return HDLOp.OR;
		case XOR : return HDLOp.XOR;
		case COMPEQ : return HDLOp.EQ;
		case NEQ : return HDLOp.NEQ;
		case GT : return HDLOp.GT;
		case GEQ : return HDLOp.GEQ;
		case LT : return HDLOp.LT;
		case LEQ : return HDLOp.LEQ;
		case INC : return HDLOp.ADD;
		case DEC : return HDLOp.SUB;
		case LNOT : return HDLOp.NOT;
		default:
			return HDLOp.UNDEFINED;
		}
	}
	
	@Override
	public void visitAssignExpr(AssignExpr o) {
		HDLExpr expr = stepIn(o.getRhs());
		Expr lhs = o.getLhs();
		if(lhs instanceof Ident){
			Ident id = (Ident)lhs;
			HDLVariable sig = parent.getHDLVariable(o.getScope().search(id.getSymbol()));
			sig.setAssign(state, expr);
		}else if(lhs instanceof ArrayAccess){
			ArrayAccess aa = (ArrayAccess)lhs;
			Ident id = (Ident)aa.getIndexed();
			HDLVariable var = parent.getHDLVariable(o.getScope().search(id.getSymbol()));
			HDLInstance inst = (HDLInstance)var;
			// address
			HDLSignal addr = inst.getSignalForPort("waddress"); // see synthsijer.lib.BlockRAM
			addr.setAssign(state, stepIn(aa.getIndex()));
			// write-enable
			HDLSignal we = inst.getSignalForPort("we"); // see synthsijer.lib.BlockRAM
			we.setAssign(state, HDLPreDefinedConstant.HIGH);
			we.setDefaultValue(HDLPreDefinedConstant.LOW);
			// data
			HDLSignal din = inst.getSignalForPort("din"); // see synthsijer.lib.BlockRAM
			din.setAssign(state, expr);
		}else if(lhs instanceof FieldAccess){
			HDLExpr l = stepIn(lhs);
			if(l instanceof HDLSignal){
				((HDLSignal)l).setAssign(state, expr);
			}else{
				throw new RuntimeException("unsupported yet: " + o);
			}
		}else{
			throw new RuntimeException("unsupported yet: " + o);
		}
		result = expr;
	}
	
	@Override
	public void visitAssignOp(AssignOp o) {
		HDLExpr expr = parent.module.newExpr(convOp(o.getOp()), stepIn(o.getLhs()), stepIn(o.getRhs()));
		Ident id = (Ident)o.getLhs();
		HDLVariable sig = parent.getHDLVariable(o.getScope().search(id.getSymbol()));
		sig.setAssign(state, expr);
		result = expr;
	}
		
	@Override
	public void visitBinaryExpr(BinaryExpr o) {
		HDLExpr lhs = stepIn(o.getLhs());
		HDLExpr rhs = stepIn(o.getRhs());
		result = parent.module.newExpr(convOp(o.getOp()), lhs, rhs);
		//HDLSignal sig = parent.module.newSignal("binaryexpr_result_" + this.hashCode(), HDLPrimitiveType.genVectorType(32));
		//sig.setAssign(null, parent.module.newExpr(convOp(o.getOp()), lhs, rhs));
		//result = sig;
	}
	
	@Override
	public void visitFieldAccess(FieldAccess o) {
		//System.out.println(o);
		Ident id = (Ident)o.getSelected();
		HDLVariable var = parent.getHDLVariable(o.getScope().search(id.getSymbol()));
		HDLInstance inst = (HDLInstance)var;
		HDLSignal sig = inst.getSignalForPort(o.getIdent().getSymbol());
		if(sig == null){
			sig = inst.getSignalForPort("field_" + o.getIdent().getSymbol() + "_output");
		}
		result = sig.getResultExpr();
	}
	
	@Override
	public void visitIdent(Ident o) {
		Variable v = o.getScope().search(o.getSymbol());
		result = parent.getHDLVariable(v);
	}
	
	private HDLPrimitiveType convToHDLType(Type type){
		if(type instanceof PrimitiveTypeKind){
			switch((PrimitiveTypeKind)type){
			case BOOLEAN: return HDLPrimitiveType.genBitType();
			case BYTE:    return HDLPrimitiveType.genSignedType(8);
			case CHAR:    return HDLPrimitiveType.genVectorType(16);
			case SHORT:   return HDLPrimitiveType.genSignedType(16);
			case INT:     return HDLPrimitiveType.genSignedType(32);
			case LONG:    return HDLPrimitiveType.genSignedType(64);
			case DOUBLE:  return HDLPrimitiveType.genVectorType(64);
			case FLOAT:   return HDLPrimitiveType.genVectorType(32);
			default: return HDLPrimitiveType.genUnknowType();
			}
		}else if(type instanceof StringType){
			return HDLPrimitiveType.genStringType();
		}else{
			return HDLPrimitiveType.genUnknowType();
		}
	}
	
	@Override
	public void visitLitral(Literal o) {
		//result = new HDLValue(o.getValueAsStr(), convToHDLType(o.getType()));
		result = parent.module.newExpr(HDLOp.ID, new HDLValue(o.getValueAsStr(), convToHDLType(o.getType())));
		//System.out.println(o + " -> " + result);
	}
	
	@Override
	public void visitMethodInvocation(MethodInvocation o) {

		HDLInstance inst = null;
		Method method = null;
		if(o.getMethod() instanceof Ident){ // local method
			throw new RuntimeException("Unsupported local method invocation:" + o.getMethod());
		}else if(o.getMethod() instanceof FieldAccess){
			FieldAccess fa = (FieldAccess)o.getMethod();
			if(fa.getSelected() instanceof Ident == false){
				throw new RuntimeException("Unsupported mulit-link method invocation: " + o.getMethod());
			}
			Ident id = (Ident)(fa.getSelected());
			HDLVariable var = parent.getHDLVariable(o.getScope().search(id.getSymbol()));
			inst = (HDLInstance)var;
			method = o.getTargetMethod();
		}else{
			throw new RuntimeException("Unsupported method invocation: " + o.getMethod());
		}
		
		for(int i = 0; i < o.getParameters().size(); i++){
			String arg = o.getMethodName() + "_" + method.getArgs()[i].getVariable().getName();
			HDLSignal s = inst.getSignalForPort(arg);
			s.setAssign(state, stepIn(o.getParameters().get(i)));
		}
		
		HDLSignal req = inst.getSignalForPort(o.getMethodName() + "_req");
		req.setAssign(state, 0, HDLPreDefinedConstant.HIGH);
		req.setDefaultValue(HDLPreDefinedConstant.LOW);

		
		if(o.getMethod().getType() != PrimitiveTypeKind.VOID){
			result = inst.getSignalForPort(o.getMethodName() + "_return");
		}else{
			result = null;
		}
		HDLSignal busy = inst.getSignalForPort(o.getMethodName() + "_busy");
		HDLSignal flag = parent.module.newSignal(String.format("%s_%04d", busy.getName(), parent.module.getExprUniqueId()), HDLPrimitiveType.genBitType(), HDLSignal.ResourceKind.WIRE);
		flag.setAssign(null,
				parent.module.newExpr(HDLOp.EQ,
						parent.module.newExpr(HDLOp.AND,
								parent.module.newExpr(HDLOp.EQ, busy, HDLPreDefinedConstant.LOW),
								parent.module.newExpr(HDLOp.EQ, req, HDLPreDefinedConstant.LOW)),
								HDLPreDefinedConstant.HIGH));
		state.setMaxConstantDelay(1);
		state.setStateExitFlag(flag);

	}
	
	@Override
	public void visitNewArray(NewArray o) {
		// TODO Auto-generated method stub		
	}
	
	@Override
	public void visitNewClassExpr(NewClassExpr o) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void visitParenExpr(ParenExpr o) {
		result = stepIn(o.getExpr());
	}
	
	@Override
	public void visitTypeCast(TypeCast o) {
		HDLExpr expr = stepIn(o.getExpr());
		
		HDLPrimitiveType target = convToHDLType(o.getType());
		HDLPrimitiveType given = (HDLPrimitiveType)(expr.getType());
		
		if(target.getWidth() == given.getWidth()){
			result = expr; // as is
		}else{
			//HDLSignal tmp = parent.module.newSignal("cast_tmp_" + this.hashCode(), given, HDLSignal.ResourceKind.WIRE);
			//tmp.setAssign(null, expr);
			//HDLSignal cast = parent.module.newSignal("cast_result_" + this.hashCode(), target, HDLSignal.ResourceKind.WIRE);

			if(target.getWidth() > given.getWidth()){
				result = parent.module.newExpr(HDLOp.PADINGHEAD, expr, new HDLValue(String.valueOf(target.getWidth()-given.getWidth()), HDLPrimitiveType.genIntegerType()));
			}else{
				result = parent.module.newExpr(HDLOp.DROPHEAD, expr, new HDLValue(String.valueOf(given.getWidth()-target.getWidth()), HDLPrimitiveType.genIntegerType()));
			}
			//result = cast;
		}
		//System.out.println(result);
	}
	
	@Override
	public void visitUnaryExpr(UnaryExpr o) {
		HDLExpr arg = stepIn(o.getArg());
		HDLVariable sig = (HDLVariable)arg;
		HDLExpr expr;
		if(o.getOp() == Op.INC || o.getOp() == Op.DEC){
			expr = parent.module.newExpr(convOp(o.getOp()), arg, HDLPreDefinedConstant.INTEGER_ONE);
			sig.setAssign(state, expr);
		}else{
			expr = parent.module.newExpr(convOp(o.getOp()), arg);
		}
		result = expr;
	}
	
}
