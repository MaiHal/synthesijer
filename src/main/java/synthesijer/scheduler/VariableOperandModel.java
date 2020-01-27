package synthesijer.scheduler;

import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;

public class VariableOperandModel {

  private String type;
  private int succNum;
	
	public VariableOperandModel(String type, int succNum) {
    this.type = type;
		this.succNum = succNum;
	}

  public String getType(){
    return this.type;
  }

  public int getSuccNum(){
    return this.succNum;
  }
}
