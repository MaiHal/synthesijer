package synthesijer.ast.expr;

import java.util.ArrayList;

import synthesijer.ast.Expr;
import synthesijer.ast.Op;
import synthesijer.ast.Scope;
import synthesijer.ast.Type;
import synthesijer.ast.Variable;
import synthesijer.hdl.HDLOp;

public class BinaryExpr extends Expr{
	
	private Expr lhs, rhs;
	private Op op;
	
	public BinaryExpr(Scope scope){
		super(scope);
	}
	
	public void setLhs(Expr expr){
		this.lhs = expr;
	}
	
	public void setRhs(Expr expr){
		this.rhs = expr;
	}
	
	public void setOp(Op op){
		this.op = op;
	}

	public Expr getLhs(){
		return this.lhs;
	}
	
	public Expr getRhs(){
		return this.rhs;
	}
	
	public Op getOp(){
		return this.op;
	}
	
	public void accept(SynthesijerExprVisitor v){
		v.visitBinaryExpr(this);
	}
	
	public String toString(){
		return String.format("BinaryExpr::(%s %s %s)", op, lhs, rhs); 
	}
	
	@Override
	public boolean isConstant() {
		return rhs.isConstant() && lhs.isConstant();
	}
	
	@Override
	public boolean isVariable() {
		return lhs.isVariable() && rhs.isVariable();
	}
	
	@Override
	public Type getType(){
		Type r = rhs.getType();
		Type l = lhs.getType();
//		return r.isPrimary(l) ? r : l; // select primary type
		return r;
	}

	@Override
	public Variable[] getSrcVariables(){
		ArrayList<Variable> list = new ArrayList<>();
		if(op != Op.ASSIGN){
			for(Variable var: lhs.getSrcVariables()) list.add(var);
		}
		for(Variable var: rhs.getSrcVariables()) list.add(var);
		return list.toArray(new Variable[]{});
	}
	
	@Override
	public Variable[] getDestVariables(){
		ArrayList<Variable> list = new ArrayList<>();
		if(op == Op.ASSIGN){
			for(Variable var: lhs.getSrcVariables()) list.add(var);
		}
		return list.toArray(new Variable[]{});
	}
}
