package synthesijer.ast.statement;

import synthesijer.ast.Expr;
import synthesijer.ast.Scope;
import synthesijer.ast.SynthesijerAstVisitor;
import synthesijer.hdl.HDLModule;
import synthesijer.model.State;
import synthesijer.model.Statemachine;

public class ExprStatement extends ExprContainStatement{
	
	private final Expr expr;
	
	public ExprStatement(Scope scope, Expr expr){
		super(scope);
		this.expr = expr;
	}
	
	public Expr getExpr(){
		return expr;
	}
	
	public State genStateMachine(Statemachine m, State dest, State terminal, State loopout, State loopCont){
		State s = m.newState("expr");
		s.setBody(this);
		s.addTransition(dest);
		return s;
	}

	public void generateHDL(HDLModule m) {
		
	}

	public void accept(SynthesijerAstVisitor v){
		v.visitExprStatement(this);
	}

}
