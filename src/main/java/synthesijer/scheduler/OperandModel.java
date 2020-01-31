package synthesijer.scheduler;

import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;

public class OperandModel {

	private String category;
  private String type;
  private int id;
	
	public OperandModel(String category, String type, int id) {
		this.category = category;
    this.type = type;
		this.id = id;
	}

  public String getCategory(){
    return this.category;
  }

  public String getType(){
    return this.type;
  }

  public int getId(){
    return this.id;
  }
}
