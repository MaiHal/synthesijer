package synthesijer.ast.statement;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import synthesijer.ast.Method;
import synthesijer.ast.Module;
import synthesijer.ast.Scope;
import synthesijer.ast.Statement;
import synthesijer.ast.Variable;
import synthesijer.hdl.HDLModule;
import synthesijer.model.State;
import synthesijer.model.StateMachine;

public class BlockStatement extends Statement implements Scope{
	
	private ArrayList<Statement> statements = new ArrayList<Statement>();
	
	private final Scope parent;
	
	private Hashtable<String, Variable> varTable = new Hashtable<String, Variable>();
	
	public BlockStatement(Scope scope){
		super(scope);
		this.parent = scope;
	}
	
	public Scope getParentScope(){
		return parent;
	}

	public Module getModule(){
		return parent.getModule();
	}

	public Method getMethod(){
		return parent.getMethod();
	}

	public void addStatement(Statement stmt){
		if(stmt != null){
			statements.add(stmt);
		}
	}
	
	public State genStateMachine(StateMachine m, State dest, State terminal, State loopout, State loopCont){
		State d = dest;
		for(int i = statements.size(); i > 0; i--){
			Statement stmt = statements.get(i-1);
			d = stmt.genStateMachine(m, d, terminal, loopout, loopCont);
		}
		return d;
	}
	
	public void dumpAsXML(PrintWriter dest){
		dest.printf("<statement type=\"block\">\n");
		for(Statement s: statements){
			s.dumpAsXML(dest);
		}
		dest.printf("</statement>\n");
	}
	
	public void makeCallGraph(){
		for(Statement s: statements){
			s.makeCallGraph();
		}
	}
	
	public void registrate(Variable v){
		varTable.put(v.getName(), v);
	}
	
	public Variable search(String s){
		Variable v = varTable.get(s);
		if(v != null) return v;
		return parent.search(s);
	}

	@Override
	public void generateHDL(HDLModule m) {
		for(Statement s: statements){
			s.generateHDL(m);
		}
	}
	
}
