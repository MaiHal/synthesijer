package synthesijer.hdl;

public interface HDLTreeVisitor {

	public void visitHDLExpr(HDLExpr o);

	public void visitHDLIdent(HDLIdent o);

	public void visitHDLInstance(HDLInstance o);

	public void visitHDLLitral(HDLLiteral o);

	public void visitHDLModule(HDLModule o);

	public void visitHDLPort(HDLPort o);

	public void visitHDLSequencer(HDLSequencer o);

	public void visitHDLSignal(HDLSignal o);

	public void visitHDLType(HDLType o);

	public void visitHDLUserDefinedType(HDLUserDefinedType o);

}
