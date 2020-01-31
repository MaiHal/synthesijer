package synthesijer.scheduler;

import java.util.ArrayList;

import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;

public class SchedulerItemModel {

	public Op op;
	public OperandModel[] src;
  public VariableOperandModel dest;
  public int[] pIds;
  public int[] sIds;
	//private int id;

	/* SchedulerItemModel(Op op, OperandModel SrcA, OperandModel SrcB, OperandModel SrcC, VariableOperandModel dest) {
    OperandModel[] om;
    if(SrcC!=null){
      om = new OperandModel[3];
      om[0] = SrcA;
      om[1] = SrcB;
      om[2] = SrcC;
    }else if(SrcB!=null){
      om = new OperandModel[2];
      om[0] = SrcA;
      om[1] = SrcB;
    }else{
      om = new OperandModel[1];
      om[0] = SrcA;
    }
		this.op = op;
		this.src = om;
    this.dest = dest;
	}*/

  SchedulerItemModel(Op op, int[] pIds, int[] sIds){
    this.op = op;
    this.pIds = pIds;
    this.sIds = sIds;
  }

  public Op getOp(){
		return op;
	}

  /*public void addPred(SchedulerItemModel pred){
		this.predecessor.add(pred);
	}

	public void addSucc(SchedulerItemModel succ){
		this.successor.add(succ);
	}*/

  public int[] getPred(){
		return this.pIds;
	}

  public int[] getSucc(){
		return this.sIds;
	}

  /*public String getOperandModelCategory(int i){
    return src[i].getCategory();
  }

  public String getOperandModelType(int i){
    return src[i].getType();
  }

  public int getOperandModelId(int i){
    return src[i].getId();
  }

  public String getVariableOperandModelType(){
    return dest.getType();
  }

  public int getVariableOperandModelSucc(){
    return dest.getSuccNum();
  }*/
}
