package synthesijer.ast.expr;

import java.util.ArrayList;

import synthesijer.ast.Expr;
import synthesijer.ast.Scope;

public class NewArray extends Expr{
	
	private ArrayList<Expr> dimExpr = new ArrayList<Expr>();
	
	public NewArray(Scope scope){
		super(scope);
	}

	public void addDimExpr(Expr expr){
		dimExpr.add(expr);
	}

	public ArrayList<Expr> getDimExpr(){
		return dimExpr;
	}
	
	public void accept(SynthesijerExprVisitor v){
		v.visitNewArray(this);
	}

	@Override
	public boolean isConstant() {
		return false;
	}

}
