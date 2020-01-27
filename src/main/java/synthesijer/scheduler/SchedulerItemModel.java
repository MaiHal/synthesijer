package synthesijer.scheduler;

import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;

public class SchedulerItemModel {

	public Op op;
	public OperandModel[] src;
  public VariableOperandModel dest;

	public SchedulerItemModel(Op op, OperandModel SrcA, OperandModel SrcB, OperandModel SrcC, VariableOperandModel dest) {
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
	}

  public Op getOp(){
		return op;
	}

  public String getOperandModelCategory(int i){
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
  }
}
