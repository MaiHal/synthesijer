package synthesijer;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskListener;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TreeScanner;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;

import java.util.ArrayList;
import java.util.Hashtable;

import synthesijer.jcfrontend.JCFrontendUtils;
import synthesijer.jcfrontend.JCTopVisitor;
import synthesijer.jcfrontend.SourceInfo;
import synthesijer.jcfrontend.PreScanner;
import synthesijer.ast.Module;

/**
 * The user interface for Synthesijer.
 * 
 * @author miyo
 *
 */
public class SynthesijerPlugin implements Plugin, TaskListener{

	@Override
	public String getName(){
		return "Synthesijer";
	}

	boolean vhdlFlag = true;
	boolean verilogFlag = true;

	@Override
	public void init(JavacTask task, String... args){
		task.addTaskListener(this);
	}

	@Override
	public void started(TaskEvent e){
		if (e.getKind() == TaskEvent.Kind.GENERATE){
			newModule(e.getCompilationUnit());
        }
	}

	@Override
	public void finished(TaskEvent e){
	}
	

	private boolean isHDLModule(String extending, Hashtable<String, String> importTable){
		if(extending == null) return false;
		if(extending.equals("HDLModule")) return true; // ad-hoc
		if(extending.equals("synthesijer.hdl.HDLModule")) return true;
		return false;
	}

	public void newModule(CompilationUnitTree t){
		/*
			Hashtable<String, String> importTable = new Hashtable<>();
			ArrayList<String> implementing = new ArrayList<>();
			String extending = "";
			boolean synthesizeFlag = true;

			CompilationUnitTree t = ;
			SourceInfo info = new SourceInfo();
			t.accept(new PreScanner(), info);
			Module module = new Module(info.className, importTable, null, implementing);
			JCTopVisitor visitor = new JCTopVisitor(module);
			t.accept(visitor, null);
			Manager.INSTANCE.addModule(module, synthesizeFlag);
		*/
		
		SourceInfo info = new SourceInfo();
		t.accept(new PreScanner(), info);

		if(info.isAnnotation){
			SynthesijerUtils.warn(info.className + " is skipped.");
			return;
		}
		if(info.isInterface){
			SynthesijerUtils.warn(info.className + " is skipped.");
			return;
		}
		for(ImportTree s: t.getImports()){
			info.importTable.put(s.getQualifiedIdentifier().toString(), s.toString());
		}
			   		
		boolean synthesizeFlag = true;
		if(isHDLModule(info.extending, info.importTable) == true){
			synthesizeFlag = false;
		}
		
		Module module = new Module(info.className, info.importTable, info.extending, info.implementing);
		module.setSynthesijerHDL(info.isSynthesijerHDL);
		
		JCTopVisitor visitor = new JCTopVisitor(module);
		t.accept(visitor, null);
		
		Manager.INSTANCE.addModule(module, synthesizeFlag);
	}
	

}

