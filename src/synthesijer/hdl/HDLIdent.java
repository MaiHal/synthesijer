package synthesijer.hdl;

public class HDLIdent implements HDLExpr{
	
	private final String sym;
	
	public HDLIdent(String sym){
		this.sym = sym;
	}

	@Override
	public String getVHDL() {
		return sym;
	}

	@Override
	public String getVerilogHDL() {
		return sym;
	}
	
	

}
