package synthesijer.scheduler.opt;

import java.util.ArrayList;
import java.util.Hashtable;

import synthesijer.scheduler.Op;
import synthesijer.scheduler.Operand;
import synthesijer.scheduler.SchedulerBoard;
import synthesijer.scheduler.SchedulerInfo;
import synthesijer.scheduler.SchedulerItem;
import synthesijer.scheduler.SchedulerSlot;
import synthesijer.scheduler.VariableOperand;

public class BasicParallelizer implements SchedulerInfoOptimizer{

	public SchedulerInfo opt(SchedulerInfo info){
		SchedulerInfo result = new SchedulerInfo(info.getName());
		ArrayList<VariableOperand>[] vars = info.getVarTableList();
		for(ArrayList<VariableOperand> v: vars){
			result.addVarTable(v);
		}
		for(SchedulerBoard b: info.getBoardsList()){
			result.addBoard(conv(b));
		}
		return result;
	}
	
	public String getKey(){
		return "basic_parallelize";
	}
	
	private SchedulerSlot copySlots(SchedulerSlot slot){
		SchedulerSlot newSlot = new SchedulerSlot(slot.getStepId()); 
		for(SchedulerItem item: slot.getItems()){
			newSlot.addItem(item);
			item.setSlot(newSlot);
		}
		return newSlot;
	}
	
	private Hashtable<SchedulerSlot, Integer> getEntryDegrees(SchedulerSlot[] slots){
		Hashtable<SchedulerSlot, Integer> degrees = new Hashtable<>();
		Hashtable<Integer, SchedulerSlot> map = new Hashtable<>();
		for(SchedulerSlot s: slots){
			map.put(s.getStepId(), s);
		}
		for(SchedulerSlot s: slots){
			int[] ids = s.getNextStep();
			for(int id: ids){
				SchedulerSlot target = map.get(id);
				Integer v = degrees.get(target);
				if(v == null){
					degrees.put(target, 1);
				}else{
					degrees.put(target, v+1);
				}
			}
		}
		return degrees;
	}
	
	private Hashtable<SchedulerSlot, ArrayList<SchedulerSlot>> analyze(ArrayList<SchedulerSlot> bb){
		Hashtable<Operand, ArrayList<SchedulerSlot>> writing = new Hashtable<>();
		Hashtable<Operand, ArrayList<SchedulerSlot>> reading = new Hashtable<>();
		Hashtable<SchedulerSlot, ArrayList<SchedulerSlot>> dependents = new Hashtable<>();
		for(SchedulerSlot s: bb){
			ArrayList<SchedulerSlot> dependent = new ArrayList<>();
			dependents.put(s, dependent);
			for(Operand src: s.getSrcOperands()){ // should read after previous write
				if(writing.containsKey(src)){
					ArrayList<SchedulerSlot> l = writing.get(src);
					for(SchedulerSlot ll: l){
						dependent.add(ll);
					}
				}
			}
			for(Operand dest: s.getDestOperands()){ // should write after previous write
				if(dest != null && writing.containsKey(dest)){
					ArrayList<SchedulerSlot> l = writing.get(dest);
					for(SchedulerSlot ll: l){
						dependent.add(ll);
					}
				}
			}
			for(Operand dest: s.getDestOperands()){ // should write after previous read
				if(dest != null && reading.containsKey(dest)){
					ArrayList<SchedulerSlot> l = reading.get(dest);
					for(SchedulerSlot ll: l){
						dependent.add(ll);
					}
				}
			}
			
			for(Operand dest: s.getDestOperands()){
				if(dest == null) continue;
				ArrayList<SchedulerSlot> l = writing.get(dest);
				if(l == null) l = new ArrayList<>();
				if(dest instanceof VariableOperand){
					l.add(s);
					writing.put(dest, l);
				}
			}
			for(Operand src: s.getSrcOperands()){
				ArrayList<SchedulerSlot> l = reading.get(src);
				if(l == null) l = new ArrayList<>();
				if(src instanceof VariableOperand){
					l.add(s);
					reading.put(src, l);
				}
			}
		}
		return dependents;
	}
	
	private boolean isReady(SchedulerSlot slot, ArrayList<SchedulerSlot> dependent, ArrayList<SchedulerSlot> restList){
		if(dependent == null) return true;
		for(SchedulerSlot s: dependent){
			if(restList.contains(s) == true){
				return false;
			}
		}
		return true;
	}
	
	private void parallelize(SchedulerBoard board, ArrayList<SchedulerSlot> bb, Hashtable<Integer, Integer> id_map){
		SchedulerSlot target = null;
		Hashtable<SchedulerSlot, ArrayList<SchedulerSlot>> dependents = analyze(bb);
		ArrayList<SchedulerSlot> restList = bb;
		ArrayList<SchedulerSlot> genList = new ArrayList<>();
		while(restList.size() > 0){
			ArrayList<SchedulerSlot> tmpList = new ArrayList<>();
			for(SchedulerSlot s: restList){
				if(target == null){
					target = copySlots(s);
					board.addSlot(target);
					genList.add(target);
					if(s.getStepId() != target.getStepId()){
						id_map.put(s.getStepId(), target.getStepId());
					}
				}else{
					if(isReady(s, dependents.get(s), restList)){
						if(s.getStepId() != target.getStepId()){
							id_map.put(s.getStepId(), target.getStepId());
						}
						for(SchedulerItem item: s.getItems()){
							target.addItem(item);
							item.setSlot(target);
						}
					}else{
						tmpList.add(s);
					}
				}
			}
			restList = tmpList;
			target = null; // next
		}
		for(SchedulerSlot s: genList){
			for(SchedulerItem i: s.getItems()){
				i.remapBranchIds(id_map);
			}
		}
	}
	
	private boolean isExcept(SchedulerItem item){
		Op op = item.getOp();
		switch(op){
		case METHOD_ENTRY:
		case METHOD_EXIT:
		case MUL32:
		case MUL64:
		case DIV32:
		case DIV64:
		case MOD32:
		case MOD64:
		case LSHIFT32:
		case LOGIC_RSHIFT32:
		case ARITH_RSHIFT32:
		case LSHIFT64:
		case LOGIC_RSHIFT64:
		case ARITH_RSHIFT64:
		case JP:
		case JT:
		case RETURN:
		case SELECT:
		case ARRAY_ACCESS:
		case ARRAY_INDEX:
		case CALL:
		case EXT_CALL:
		case FIELD_ACCESS:
		case BREAK:
		case CONTINUE:
		case FADD32:
		case FSUB32:
		case FMUL32:
		case FDIV32:
		case FADD64:
		case FSUB64:
		case FMUL64:
		case FDIV64:
		case CONV_F2I:
		case CONV_I2F:
		case CONV_D2L:
		case CONV_L2D:
		case CONV_F2D:
		case CONV_D2F:
		case FLT32:
		case FLEQ32:
		case FGT32:
		case FGEQ32:
		case FCOMPEQ32:
		case FNEQ32:
		case FLT64:
		case FLEQ64:
		case FGT64:
		case FGEQ64:
		case FCOMPEQ64:
		case FNEQ64:
		case UNDEFINED:
			return true;
		default:
			return false;
		}
	}
	
	public SchedulerBoard conv(SchedulerBoard src){
		SchedulerBoard ret = new SchedulerBoard(src.getName(), src.getMethod());
		SchedulerSlot[] slots = src.getSlots();
		Hashtable<SchedulerSlot, Integer> degrees = getEntryDegrees(slots);
		ArrayList<SchedulerSlot> bb = null;
		Hashtable<Integer, Integer> id_map = new Hashtable<>();
		for(int i = 0; i < slots.length; i++){
			SchedulerSlot slot = slots[i];
			Integer d = degrees.get(i);
			if(d == null) d = 0;
			if(slot.hasBranchOp() || slot.getNextStep().length > 1 || slot.getLatency() > 0 || d > 1 || slot.getItems().length > 1 || isExcept(slot.getItems()[0])){
				if(bb != null && bb.size() > 0){
					parallelize(ret, bb, id_map);
				}
				// the slot should be registered as a new slot
				SchedulerSlot newSlot = copySlots(slot);
				ret.addSlot(newSlot);
				for(SchedulerItem item: newSlot.getItems()){
					item.remapBranchIds(id_map);
				}
				bb = null; // reset
				continue;
			}
			if(bb == null){
				bb = new ArrayList<>();
			}
			bb.add(slot);
		}
		if(bb != null && bb.size() > 0){
			parallelize(ret, bb, id_map);
		}
		return ret;
	}

}
