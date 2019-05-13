package synthesijer.scheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class IRWriter {

	private final String name;

	public IRWriter(String name){
		this.name = name;
	}

	public void generate(SchedulerInfo info) throws IOException{
		try(
				PrintStream ir = new PrintStream(new FileOutputStream(new File(name + ".ir")));
		){
			ir.println("(MODULE " + info.getName());
			//genVariables(ir, info.getModuleVarList().toArray(new VariableOperand[]{}));
			genVariables(ir, info.getModuleVarList().toArray(new Operand[]{}));
			for(SchedulerBoard b: info.getBoardsList()){
				genSchedulerBoard(ir, b);
			}
			ir.println(")");
			ir.close();
		}catch(IOException e){
			throw new IOException(e);
		}
	}

	private void genSchedulerBoard(PrintStream ir, SchedulerBoard b){
		ir.println(" (BOARD " + b.getReturnType() + " " + b.getName());
		//genVariables(ir, b.getVarList().toArray(new VariableOperand[]{}));
		genVariables(ir, b.getVarList().toArray(new Operand[]{}));
		ir.println("    (SEQUENCER " + b.getName());
		for(SchedulerSlot s: b.getSlots()){
			genSchedulerSlot(ir, s);
		}
		ir.println("    )");
		ir.println(" )");
	}

	private void genSchedulerSlot(PrintStream ir, SchedulerSlot slot){
		ir.println("      (SLOT " + slot.getStepId());
		for(SchedulerItem i: slot.getItems()){
			genSchedulerItem(ir, i);
		}
		ir.println("      )");
	}

	private void genSchedulerItem(PrintStream ir, SchedulerItem item){
		ir.println("        " + item.toSexp());
	}

	//private void genVariables(PrintStream ir, VariableOperand[] vars){
	private void genVariables(PrintStream ir, Operand[] vars){
		ir.println("  (VARIABLES ");
		//for(VariableOperand v: vars){
		for(Operand v: vars){
			gen_variable(ir, v);
		}
		ir.println("  )");
	}

	//private void gen_variable(PrintStream ir, VariableOperand v){
	private void gen_variable(PrintStream ir, Operand v){
		String s = "    " + v.toSexp();
		ir.println(s);
	}
}
