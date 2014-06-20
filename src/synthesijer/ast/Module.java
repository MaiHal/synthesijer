package synthesijer.ast;

import java.util.ArrayList;
import java.util.Hashtable;

import synthesijer.CompileState;
import synthesijer.ast.statement.VariableDecl;
import synthesijer.model.State;
import synthesijer.model.Statemachine;

public class Module implements Scope, SynthesijerAstTree{
	
	private final Scope parent;
	private final String name;
	private Hashtable<String, String> importTable;
	
	private Hashtable<String, Method> methodTable = new Hashtable<String, Method>();
	private Hashtable<String, Variable> variableTable = new Hashtable<String, Variable>();
	private ArrayList<Method> methods = new ArrayList<Method>();
	private ArrayList<VariableDecl> variables = new ArrayList<VariableDecl>();
	private ArrayList<Scope> scopes = new ArrayList<Scope>();
	
	private Statemachine statemachine;
	
	public Module(String name, Hashtable<String, String> importTable){
		this(null, name, importTable);
	}

	public Module(Scope parent, String name, Hashtable<String, String> importTable){
		this.parent = parent;
		this.name = name;
		this.importTable = importTable;
		scopes.add(this);
	}	
	
	public void addScope(Scope s){
		scopes.add(s);
	}
	
	public Scope[] getScope(){
		return scopes.toArray(new Scope[]{});
	}
	
	public String getName(){
		return name;
	}
	
	public Scope getParentScope(){
		return parent;
	}
	
	public Variable search(String name){
		Variable var = variableTable.get(name);
		if(var != null)
			return var;
		if(parent != null)
			return parent.search(name);
		return null;
	}
	
	public Module getModule(){
		return this;
	}
	
	public Method getMethod(){
		return null;
	}

	public void addMethod(Method m){
		methodTable.put(m.getName(), m);
		methods.add(m);
	}
	
	public void addVariableDecl(VariableDecl v){
		variableTable.put(v.getName(), v.getVariable());
		variables.add(v);
	}
	
	public VariableDecl[] getVariableDecls(){
		return variables.toArray(new VariableDecl[]{});
	}

	public Variable[] getVariables(){
		return variableTable.values().toArray(new Variable[]{});
	}

	public ArrayList<Method> getMethods(){
		return methods;
	}	
	
	public void genStateMachine(){
		genInitStateMachine();
		for(Method m: methods){
			m.genStateMachine();
		}
	}

	private void genInitStateMachine(){
		statemachine = new Statemachine("module_variale_declararions");
		State d = statemachine.newState("init_end", true);
		for(int i = variables.size(); i > 0; i--){
			d = variables.get(i-1).genStateMachine(statemachine, d, null, null, null);
		}
	}
	
	public void accept(SynthesijerAstVisitor v){
		v.visitModule(this);
	}

}
