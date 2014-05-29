package synthesijer.hdl;

import java.util.ArrayList;

import synthesijer.hdl.HDLSequencer.SequencerState;

public class HDLInstance implements HDLTree, HDLExpr, HDLVariable{
	
	private final HDLModule module;
	private final String name;
	private final HDLModule submodule;
	
	private ArrayList<Pair> pairs = new ArrayList<Pair>();
	
	HDLInstance(HDLModule module, String name, HDLModule submodule){
		this.module = module;
		this.name = name;
		this.submodule = submodule;
		genPortSignals();
	}
	
	private String getAbsoluteName(String n){
		return this.name + "_" + n;
	}
	
	private void genPortSignals(){
		for(Pair pair: pairs){ module.rmSignal(pair.signal); }
		pairs.clear();
		for(HDLPort p: submodule.getPorts()){
			HDLSignal s = module.newSignal(getAbsoluteName(p.getName()), p.getType(), HDLSignal.ResourceKind.WIRE);
			pairs.add(new Pair(s, p));
		}
	}
	
	public void update(){
		genPortSignals();
	}
	
	public String getName(){
		return name;
	}
	
	public HDLModule getSubModule(){
		return submodule;
	}
	
	public ArrayList<Pair> getPairs(){
		return pairs;
	}
	
	public HDLSignal getSignalForPort(String name){
		for(Pair pair: pairs){
			if(pair.port.getName().equals(name)) return pair.signal;
		}
		return null;
	}
	

	@Override
	public void accept(HDLTreeVisitor v) {
		v.visitHDLInstance(this);
	}
	
	public class Pair{
		public final HDLSignal signal;
		public final HDLPort port;
		Pair(HDLSignal signal, HDLPort port){this.signal = signal; this.port = port;}
	}

	@Override
	public void setAssign(SequencerState s, HDLExpr expr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setResetValue(HDLExpr s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getVHDL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVerilogHDL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDLExpr getResultExpr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDLType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HDLSignal[] getSrcSignals() {
		// TODO Auto-generated method stub
		return null;
	}

}
