package synthesijer.model;

import java.util.ArrayList;

public class BasicBlock {

	private final ArrayList<State> states = new ArrayList<>();
	private final ArrayList<BasicBlock> successors = new ArrayList<>();
	
	public void addState(State s){
		states.add(s);
	}
	
	public void addNextBlock(BasicBlock b){
		successors.add(b);
	}
	
	public void printAll(){
		for(State s: states){
			System.out.println(s + ":" + s.getBody());
		}
	}

}
