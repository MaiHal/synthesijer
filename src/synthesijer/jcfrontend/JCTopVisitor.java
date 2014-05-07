package synthesijer.jcfrontend;

import java.util.List;

import openjdk.com.sun.tools.javac.tree.JCTree;
import openjdk.com.sun.tools.javac.tree.JCTree.JCClassDecl;
import openjdk.com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import openjdk.com.sun.tools.javac.tree.JCTree.JCStatement;
import openjdk.com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import openjdk.com.sun.tools.javac.tree.JCTree.Visitor;
import synthesijer.SynthesijerUtils;
import synthesijer.ast.Method;
import synthesijer.ast.Module;
import synthesijer.ast.Scope;
import synthesijer.ast.Type;
import synthesijer.ast.statement.VariableDecl;
import synthesijer.ast.type.MySelfType;

public class JCTopVisitor extends Visitor{
	
	private final Module module;
	
	public JCTopVisitor(Module m){
		this.module = m;
	}
	
	public Scope getScope(){
		return module;
	}
	
	public  void visitClassDef(JCClassDecl that){
		for (JCTree def : that.defs) {
			if(def == null){
				;
			}else if(def instanceof JCMethodDecl){
				def.accept(this);
			}else if(def instanceof JCVariableDecl){
				def.accept(new JCStmtVisitor(module));
			}else{
				System.err.printf("Unknown class: %s (%s)", def, def.getClass());
			}
		}
	}
	
	public void visitMethodDef(JCMethodDecl decl){
		String name = decl.getName().toString();
		Type type;
		if(JCFrontendUtils.isConstructor(decl)){
			type = new MySelfType();
		}else{
			type = TypeBuilder.genType(decl.getReturnType());
		}
		Method m = new Method(module, name, type);
		
		m.setArgs(parseArgs(decl.getParameters(), m));
		
		m.setUnsynthesizableFlag(JCFrontendUtils.isAnnotatedBy(decl.mods.annotations, "unsynthesizable"));
		m.setAutoFlag(JCFrontendUtils.isAnnotatedBy(decl.mods.annotations, "auto"));
		m.setSynchronizedFlag(JCFrontendUtils.isSynchronized(decl.mods));
		m.setPrivateFlag(JCFrontendUtils.isPrivate(decl.mods));
		m.setRawFlag(JCFrontendUtils.isAnnotatedBy(decl.mods.annotations, "raw"));
		m.setCombinationFlag(JCFrontendUtils.isAnnotatedBy(decl.mods.annotations, "combination"));
		m.setParallelFlag(JCFrontendUtils.isAnnotatedBy(decl.mods.annotations, "parallel"));
		m.setNoWaitFlag(JCFrontendUtils.isAnnotatedBy(decl.mods.annotations, "no_wait"));
		m.setConstructorFlag(JCFrontendUtils.isConstructor(decl));
		
		for(JCStatement stmt: decl.body.getStatements()){
			JCStmtVisitor visitor = new JCStmtVisitor(m);
			stmt.accept(visitor);
			m.getBody().addStatement(visitor.getStatement());
		}
		
		module.addMethod(m);
	}
	
	private VariableDecl[] parseArgs(List<JCVariableDecl> args, Scope scope){
		if(args == null || args.size() == 0) return new VariableDecl[0];
		VariableDecl[] v = new VariableDecl[args.size()];
		for(int i = 0; i < v.length; i++){
			JCStmtVisitor visitor = new JCStmtVisitor(scope);
			args.get(i).accept(visitor); // Since args.get(i) is an instance of JCVariableDecl,
			                             // this visitor should visit visitVarDef
			v[i] = (VariableDecl)(visitor.getStatement()); // this type cast should occur no errors.
		}
		return v;
	}

	public void visitTree(JCTree t){
		SynthesijerUtils.error("[JCTopVisitor] The following is unexpected in this context.");
		SynthesijerUtils.dump(t);
	}
	
}