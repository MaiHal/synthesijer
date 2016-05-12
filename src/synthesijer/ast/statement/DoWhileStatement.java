package synthesijer.ast.statement;

import synthesijer.ast.Expr;
import synthesijer.ast.Scope;
import synthesijer.ast.Statement;
import synthesijer.ast.SynthesijerAstVisitor;

public class DoWhileStatement extends Statement{
	
	private Expr condition;
	private BlockStatement body;
	
	public DoWhileStatement(Scope scope){
		super(scope);
	}
	
	public void setCondition(Expr expr){
		this.condition = expr;
	}
	
	public Expr getCondition(){
		return condition;
	}
	
	public void setBody(BlockStatement body){
		this.body = body;
	}
	
	public BlockStatement getBody(){
		return body;
	}

	public void accept(SynthesijerAstVisitor v){
		v.visitDoWhileStatement(this);
	}

}
