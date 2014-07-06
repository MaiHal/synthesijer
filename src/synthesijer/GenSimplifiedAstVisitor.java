package synthesijer;

import java.util.ArrayList;

import synthesijer.ast.Expr;
import synthesijer.ast.Method;
import synthesijer.ast.Module;
import synthesijer.ast.Scope;
import synthesijer.ast.Statement;
import synthesijer.ast.SynthesijerAstVisitor;
import synthesijer.ast.Type;
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
import synthesijer.ast.statement.BlockStatement;
import synthesijer.ast.statement.BreakStatement;
import synthesijer.ast.statement.ContinueStatement;
import synthesijer.ast.statement.ExprStatement;
import synthesijer.ast.statement.ForStatement;
import synthesijer.ast.statement.IfStatement;
import synthesijer.ast.statement.ReturnStatement;
import synthesijer.ast.statement.SkipStatement;
import synthesijer.ast.statement.SwitchStatement;
import synthesijer.ast.statement.SwitchStatement.Elem;
import synthesijer.ast.statement.SynchronizedBlock;
import synthesijer.ast.statement.TryStatement;
import synthesijer.ast.statement.VariableDecl;
import synthesijer.ast.statement.WhileStatement;

public class GenSimplifiedAstVisitor implements SynthesijerAstVisitor {
	
	private final IdentifierGenerator idGenerator;

	public GenSimplifiedAstVisitor(IdentifierGenerator idGenerator){
		this.idGenerator = idGenerator;
	}
	
	@Override
	public void visitMethod(Method o) {
		BlockStatement block = o.getBody();
		block.accept(new GenSimplifiedAstBlockVisitor(o, idGenerator));
	}

	@Override
	public void visitModule(Module o) {
		for(Method m: o.getMethods()){
			m.accept(this);
		}
	}
	
	public void defaultAcceptor(Statement s){
		throw new RuntimeException("Internal Error in GenSimplifiedAstVisitor");
	}
	
	@Override
	public void visitBlockStatement(BlockStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitBreakStatement(BreakStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitContinueStatement(ContinueStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitExprStatement(ExprStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitForStatement(ForStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitIfStatement(IfStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitReturnStatement(ReturnStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitSkipStatement(SkipStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitSwitchStatement(SwitchStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitSwitchCaseElement(Elem o) {
		throw new RuntimeException("Internal Error in GenSimplifiedAstVisitor");
	}

	@Override
	public void visitSynchronizedBlock(SynchronizedBlock o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitTryStatement(TryStatement o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitVariableDecl(VariableDecl o) {
		defaultAcceptor(o);
	}

	@Override
	public void visitWhileStatement(WhileStatement o) {
		defaultAcceptor(o);
	}

}

class GenSimplifiedAstBlockVisitor implements SynthesijerAstVisitor{
	
	final IdentifierGenerator idGenerator;
	final ArrayList<Statement> newList = new ArrayList<Statement>();
	final Scope scope;
	
	public GenSimplifiedAstBlockVisitor(Scope scope, IdentifierGenerator idGenerator){
		this.idGenerator = idGenerator;
		this.scope = scope;
	}
	
	@Override
	public void visitMethod(Method o) {
		throw new RuntimeException("Internal Error in GenSimplifiedAstBlockVisitor");
	}

	@Override
	public void visitModule(Module o) {
		throw new RuntimeException("Internal Error in GenSimplifiedAstBlockVisitor");
	}

	@Override
	public void visitBlockStatement(BlockStatement o) {
		GenSimplifiedAstBlockVisitor v = new GenSimplifiedAstBlockVisitor(o, idGenerator); 
		for(Statement stmt : o.getStatements()){
			stmt.accept(v);
		}
		o.replaceStatements(v.newList);
	}

	@Override
	public void visitBreakStatement(BreakStatement o) {
		newList.add(o);
	}

	@Override
	public void visitContinueStatement(ContinueStatement o) {
		newList.add(o);
	}

	@Override
	public void visitExprStatement(ExprStatement o) {
		o.getExpr().accept(new GenSimplifiedAstExprVisitor(this));
		newList.add(o);
	}

	@Override
	public void visitForStatement(ForStatement o) {
		GenSimplifiedAstBlockVisitor v = new GenSimplifiedAstBlockVisitor(o, idGenerator);
		for(Statement stmt: o.getInitializations()){
			stmt.accept(v);
		}
		o.replaceInitializations(v.newList);
		o.getBody().accept(this);
		// TODO treating condition expression
		newList.add(o);
	}

	@Override
	public void visitIfStatement(IfStatement o) {
		o.getThenPart().accept(this);
		if(o.getElsePart() != null){
			o.getElsePart().accept(this);
		}
		newList.add(o);
	}

	@Override
	public void visitReturnStatement(ReturnStatement o) {
		o.getExpr().accept(new GenSimplifiedAstExprVisitor(this));
		newList.add(o);
	}

	@Override
	public void visitSkipStatement(SkipStatement o) {
		newList.add(o);
	}

	@Override
	public void visitSwitchStatement(SwitchStatement o) {
		newList.add(o);
	}

	@Override
	public void visitSwitchCaseElement(Elem o) {
		for(Statement stmt: o.getStatements()){
			stmt.accept(this);
		}
	}

	@Override
	public void visitSynchronizedBlock(SynchronizedBlock o) {
		visitBlockStatement(o);
	}

	@Override
	public void visitTryStatement(TryStatement o) {
		o.getBody().accept(this);
		newList.add(o);
	}

	@Override
	public void visitVariableDecl(VariableDecl o) {
		o.getInitExpr().accept(new GenSimplifiedAstExprVisitor(this));
		newList.add(o);
	}	

	@Override
	public void visitWhileStatement(WhileStatement o) {
		o.getBody().accept(this);
		newList.add(o);
	}

}

class GenSimplifiedAstExprVisitor implements SynthesijerExprVisitor{
	
	private final GenSimplifiedAstBlockVisitor block;
	
	public GenSimplifiedAstExprVisitor(GenSimplifiedAstBlockVisitor visitor) {
		this.block = visitor;
	}
	
	@Override
	public void visitArrayAccess(ArrayAccess o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitAssignExpr(AssignExpr o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitAssignOp(AssignOp o) {
		// TODO Auto-generated method stub
		
	}
	
	private Ident genTempIdent(Type type){
		String name = String.format("tmp_%04d", block.idGenerator.id());
		Ident ident = new Ident(block.scope);
		ident.setIdent(name);
		block.scope.addVariableDecl(new VariableDecl(block.scope, name, type, null));
		return ident;
	}
	
	private Statement genTempAssignStatement(Ident ident, Expr expr){
		AssignExpr assign = new AssignExpr(block.scope);
		assign.setLhs(ident);
		System.out.println("fefe: " + expr);
		assign.setRhs(expr);
		ExprStatement stmt = new ExprStatement(block.scope, assign);
		return stmt;
	}

	@Override
	public void visitBinaryExpr(BinaryExpr o) {
		// replacement of lhs
		if(o.getLhs().isVariable() == false){
			Ident ident = genTempIdent(o.getType());
			block.newList.add(genTempAssignStatement(ident, o.getLhs()));
			o.setLhs(ident);
		}else{
			o.getLhs().accept(this);
		}
		
		// replacement of rhs
		if(o.getRhs().isVariable() == false){
			Ident ident = genTempIdent(o.getType());
			block.newList.add(genTempAssignStatement(ident, o.getRhs()));
			o.setRhs(ident);
		}else{
			o.getRhs().accept(this);
		}
	}

	@Override
	public void visitFieldAccess(FieldAccess o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitIdent(Ident o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitLitral(Literal o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitMethodInvocation(MethodInvocation o) {
		for(Expr expr: o.getParameters()){
			if(expr.isVariable()) continue;
			expr.accept(this);
		}
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitTypeCast(TypeCast o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visitUnaryExpr(UnaryExpr o) {
		// TODO Auto-generated method stub
		
	}

}
