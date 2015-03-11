package synthesijer.scheduler.opt;

import java.util.ArrayList;

import synthesijer.scheduler.Op;
import synthesijer.scheduler.SchedulerBoard;
import synthesijer.scheduler.SchedulerInfo;
import synthesijer.scheduler.SchedulerItem;
import synthesijer.scheduler.SchedulerSlot;
import synthesijer.scheduler.VariableOperand;

public class PackArrayWriteAccess implements SchedulerInfoOptimizer{

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
		return "pack_array_write";
	}
	
	private SchedulerSlot copySlots(SchedulerSlot slot){
		SchedulerSlot newSlot = new SchedulerSlot(slot.getStepId()); 
		for(SchedulerItem item: slot.getItems()){
			newSlot.addItem(item);
			item.setSlot(newSlot);
		}
		return newSlot;
	}
	
	public SchedulerBoard conv(SchedulerBoard src){
		SchedulerBoard ret = src.genSameEnvBoard();
		SchedulerSlot[] slots = src.getSlots();
		int i = 0;
		while(i < slots.length){
			SchedulerSlot slot = slots[i];
			i++;
			SchedulerSlot newSlot = copySlots(slot);
			ret.addSlot(newSlot);
			SchedulerItem[] items = slot.getItems();
			if(items.length > 1){ continue; /* skip */ }
			if(items[0].getOp() != Op.ARRAY_INDEX){ continue; /* skip */ }
			SchedulerSlot candidate = slots[i];
			SchedulerItem[] candidate_items = candidate.getItems();
			if(candidate_items.length > 1){ continue; /* skip */ }
			if(candidate_items[0].getOp() != Op.ASSIGN){ continue; /* skip */ }
			if(items[0].getDestOperand() != candidate_items[0].getDestOperand()){ continue; /* skip */ }
			newSlot.addItem(candidate_items[0]);
			candidate_items[0].setSlot(newSlot);
			i++;
		}
		return ret;
	}

}
