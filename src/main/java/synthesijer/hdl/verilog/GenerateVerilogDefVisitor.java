package synthesijer.hdl.verilog;

import java.io.PrintWriter;
import java.util.HashMap;

import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLInstance;
import synthesijer.hdl.HDLInstanceRef;
import synthesijer.hdl.HDLLiteral;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLParameter;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSequencer;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLTreeVisitor;
import synthesijer.hdl.HDLUserDefinedType;
import synthesijer.hdl.HDLUtils;

public class GenerateVerilogDefVisitor implements HDLTreeVisitor{

    private final PrintWriter dest;
    private int offset;
	private HashMap<HDLUserDefinedType, Boolean> definedType = new HashMap<>();

    public GenerateVerilogDefVisitor(PrintWriter dest, int offset){
        this.dest = dest;
        this.offset = offset;
    }

    @Override
    public void visitHDLExpr(HDLExpr o) {
    }

    @Override
    public void visitHDLInstance(HDLInstance o) {
    }

    @Override
    public void visitHDLLitral(HDLLiteral o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitHDLModule(HDLModule o) {
        for(HDLPort p: o.getPorts()){
            if(p.isSet(HDLPort.OPTION.NO_SIG)) continue; // nothing to do
            if(p.getSignal() == null) continue; // nothing to do
            p.getSignal().accept(this);
        }
        HDLUtils.nl(dest);
        for(HDLSignal s: o.getSignals()){ s.accept(this); }
        HDLUtils.nl(dest);
        for(HDLSequencer m: o.getSequencers()){ m.accept(this); }
        HDLUtils.nl(dest);
    }

    @Override
    public void visitHDLPort(HDLPort o) {
        HDLUtils.print(dest, offset, String.format("%s %s %s", o.getDir().getVerilogHDL(), o.getType().getVerilogHDL(), o.getName()));
    }

    @Override
    public void visitHDLParameter(HDLParameter o) {
        HDLUtils.print(dest, offset, String.format("parameter %s = %s", o.getName(), o.getDefaultValue().getVerilogHDL()));
    }

    @Override
    public void visitHDLSequencer(HDLSequencer o) {
    }

    @Override
    public void visitHDLSignal(HDLSignal o) {
        if(o.getType() instanceof HDLUserDefinedType){
            ((HDLUserDefinedType) o.getType()).accept(this);
        }
        String s = "";

        if(o.isDebugFlag()){
            s += "(* mark_debug=\"TRUE\", keep=\"TRUE\", S=\"TRUE\" *) ";
        }

        if(o.getResetValue() != null && o.isRegister()){
            s += String.format("%s %s %s = %s;", o.getKind().toString(), o.getType().getVerilogHDL(), o.getName(), o.getResetValue().getVerilogHDL());
        }else{
            s += String.format("%s %s %s;", o.getKind().toString(), o.getType().getVerilogHDL(), o.getName());
        }
        HDLUtils.println(dest, offset, s);
    }

    @Override
    public void visitHDLType(HDLPrimitiveType o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitHDLUserDefinedType(HDLUserDefinedType o) {
		if(definedType.containsKey(o)) return;
		definedType.put(o, true);
        for(int i = 0; i < o.getItems().length; i++){
            //			HDLUtils.println(dest, offset, String.format("parameter %s = 32'd%d;", o.getItems()[i].getVerilogHDL(), i));
            HDLUtils.println(dest, offset, String.format("localparam %s = 32'd%d;", o.getItems()[i].getVerilogHDL(), i));
        }
    }

    @Override
    public void visitHDLInstanceRef(HDLInstanceRef o){
    }

}
