package synthesijer.model;

import java.util.ArrayList;
import java.util.Hashtable;

public class GenBasicStatemachineBlockVisitor implements StatemachineVisitor{
	
	private final ArrayList<BasicBlock> list;
	private final BasicBlock bb;
	private final Hashtable<State, Boolean> sentinel;
	
	private GenBasicStatemachineBlockVisitor(ArrayList<BasicBlock> list, Hashtable<State, Boolean> sentinel) {
		this.list = list;
		this.sentinel = sentinel;
		this.bb = new BasicBlock();
		list.add(bb);
	}

	public GenBasicStatemachineBlockVisitor() {
		this(new ArrayList<BasicBlock>(), new Hashtable<State, Boolean>());
	}
	
	public BasicBlock getBasicBlock(){
		return bb;
	}
	
	public BasicBlock[] getBasicBlockList(){
		return list.toArray(new BasicBlock[0]);
	}
	
	private BasicBlock stepIn(State s){
		GenBasicStatemachineBlockVisitor v = new GenBasicStatemachineBlockVisitor(list, sentinel); 
		s.accept(v);
		return v.getBasicBlock();
	}
	
	private BasicBlock newBB(){
		GenBasicStatemachineBlockVisitor v = new GenBasicStatemachineBlockVisitor(list, sentinel); 
		return v.getBasicBlock();
	}

	@Override
	public void visitStatemachine(Statemachine o) {
		State s = o.getEntryState();
		s.accept(this);
	}
	
	@Override
	public void visitState(State o) {
		if(sentinel.containsKey(o)) return;
		sentinel.put(o, true);
		if(o.getTransitions().length == 1 && o.getPredecesors().length <= 1){
			bb.addState(o);
			State s = o.getTransitions()[0].getDestination();
			if(s != null){
				s.accept(this);
			}
		}else{
			BasicBlock b = null;
			if(o.getPredecesors().length <= 1){ // just fork
				b = bb;
			}else{ // fork & join
				b = newBB();
			}
			b.addState(o);
			for(Transition t: o.getTransitions()){
				State s = t.getDestination();
				if(s != null){
					b.addNextBlock(stepIn(s));
				}
			}
		}
	}
	

}
