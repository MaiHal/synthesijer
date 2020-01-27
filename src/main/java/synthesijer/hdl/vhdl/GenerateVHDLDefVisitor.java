package synthesijer.hdl.vhdl;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.ArrayList;

import synthesijer.Constant;
import synthesijer.hdl.HDLExpr;
import synthesijer.hdl.HDLInstance;
import synthesijer.hdl.HDLInstanceRef;
import synthesijer.hdl.HDLLiteral;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLParameter;
import synthesijer.hdl.HDLPort;
import synthesijer.hdl.HDLPort.OPTION;
import synthesijer.hdl.HDLPrimitiveType;
import synthesijer.hdl.HDLSequencer;
import synthesijer.hdl.HDLSignal;
import synthesijer.hdl.HDLTreeVisitor;
import synthesijer.hdl.HDLUserDefinedType;
import synthesijer.hdl.HDLUtils;
import synthesijer.hdl.expr.HDLValue;

public class GenerateVHDLDefVisitor implements HDLTreeVisitor{

	private PrintWriter dest;
	private int offset;
	public static ArrayList<String> compNames = new ArrayList<>();
	public static ArrayList<HDLSignal> varSignals = new ArrayList<>();
	public static ArrayList<HDLSignal> usingSignals = new ArrayList<>();
	
	private HashMap<HDLUserDefinedType, Boolean> definedType = new HashMap<>();

	public GenerateVHDLDefVisitor(PrintWriter dest, int offset){
		this.dest = dest;
		this.offset = offset;
	}

	@Override
	public void visitHDLExpr(HDLExpr o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitHDLInstance(HDLInstance o) {
		if(o.getSubModule().isComponentDeclRequired() == false) return;
		// component部
		HDLUtils.println(dest, offset, String.format("component %s", o.getSubModule().getName()));
		if(o.getSubModule().getParameters().length > 0){
			genGenericList(dest, offset+2, o.getSubModule().getParameters());
		}
		genPortList(dest, offset+2, o.getSubModule().getPorts(), (o.getSubModule().getParameters().length > 0));
		HDLUtils.println(dest, offset, String.format("end component %s;", o.getSubModule().getName()));
		compNames.add(o.getSubModule().getName());
	}

	@Override
	public void visitHDLLitral(HDLLiteral o) {
		// TODO Auto-generated method stub

	}

	private void genGenericList(PrintWriter dest, int offset, HDLParameter[] params){
		HDLUtils.println(dest, offset, "generic (");
		String sep = "";
		for(HDLParameter p: params){
			dest.print(sep);
			offset += 2;
			p.accept(this);
			offset -= 2;
			sep = ";" + Constant.BR;
		}
		HDLUtils.println(dest, 0, "");
		HDLUtils.println(dest, offset, ");");
	}

	private void genPortList(PrintWriter dest, int offset, HDLPort[] ports, boolean paramFlag){
		// ここでentity部のportを宣言
		HDLUtils.println(dest, offset, "port (");
		String sep = "";
		for(HDLPort p: ports){
			//p.accept(new GenerateVHDLDefVisitor(dest, offset+2)); 元からコメントアウト
			dest.print(sep);
			HDLUtils.print(dest, offset+2, String.format("%s : %s %s", p.getName(), p.getDir().getVHDL(), ((HDLPrimitiveType)p.getType()).getVHDL(paramFlag)));
			sep = ";" + Constant.BR;
		}
		HDLUtils.println(dest, 0, "");
		HDLUtils.println(dest, offset, ");");
	}

	private void genParamList(PrintWriter dest, int offset, HDLParameter[] params, boolean paramFlag){
		HDLUtils.println(dest, offset, "generic (");
		String sep = "";
		for(HDLParameter p: params){
			dest.print(sep);
			//p.accept(new GenerateVHDLDefVisitor(dest, offset+2)); 元からコメントアウト
			HDLUtils.print(dest, offset+2, String.format("%s : %s := %s",
					p.getName(),
					((HDLPrimitiveType)p.getType()).getVHDL(),
					p.getDefaultValue().getVHDL()));
			sep = ";" + Constant.BR;
		}
		HDLUtils.println(dest, 0, "");
		HDLUtils.println(dest, offset, ");");
	}

	@Override
	public void visitHDLModule(HDLModule o) {
			// library import
			HDLUtils.println(dest, offset, String.format("library IEEE;"));
			HDLUtils.println(dest, offset, String.format("use IEEE.std_logic_1164.all;"));
			//HDLUtils.println(dest, offset, String.format("use IEEE.numeric_std.all;"));
			HDLUtils.nl(dest);

			HDLModule.LibrariesInfo[] libraries = o.getLibraries();
			for(HDLModule.LibrariesInfo lib: libraries){
				HDLUtils.println(dest, offset, String.format("library " + lib.libName + ";"));
				for(String s: lib.useName){
					HDLUtils.println(dest, offset, String.format("use " + s + ";"));
				}
				HDLUtils.nl(dest);
			}

			HDLUtils.println(dest, offset, String.format("entity %s is", o.getName()));
			
			if(o.getParameters().length > 0){
				genParamList(dest, offset+2, o.getParameters(), false);
			}
			if(o.getPorts().length > 0){
				// entity部のport宣言出力の呼び出し元
				genPortList(dest, offset+2, o.getPorts(), false);
			}
			HDLUtils.println(dest, offset, String.format("end %s;", o.getName()));
			HDLUtils.nl(dest);
			HDLUtils.nl(dest);
			// architecture
			HDLUtils.println(dest, offset, String.format("architecture RTL of %s is", o.getName()));

			Hashtable<String, Boolean> componentFlags = new Hashtable<>();
			// component部の呼び出し元
			for(HDLInstance i: o.getModuleInstances()){
				if(componentFlags.containsKey(i.getSubModule().getName())) continue; // already
				offset += 2;
				i.accept(this);
				offset -= 2;
				componentFlags.put(i.getSubModule().getName(), true);
			}
			HDLUtils.nl(dest);
			// signalの一部を出力(entityのportに対するsignalな感じがする)
			for(HDLPort p: o.getPorts()){
				if(p.isSet(OPTION.NO_SIG)) continue;
				if(p.getSignal() == null) continue;
				offset += 2;
				p.getSignal().accept(this);
				offset -= 2;
			}
			offset += 2;
			HDLUtils.nl(dest);	
			// signal部の呼び出し元
			for(HDLSignal s: o.getSignals()){s.accept(this);}
			offset -= 2;
	}

	@Override
	public void visitHDLPort(HDLPort o) {
		System.out.println(o);
		HDLUtils.print(dest, offset, String.format("%s : %s %s", o.getName(), o.getDir().getVHDL(), o.getType().getVHDL()));
	}

	@Override
	public void visitHDLParameter(HDLParameter o) {
		HDLUtils.print(dest, offset, String.format("%s : %s := %s", o.getName(), o.getType().getVHDL(), o.getDefaultValue().getVHDL()));
	}

	@Override
	public void visitHDLSequencer(HDLSequencer o) {
	}

	@Override
	public void visitHDLSignal(HDLSignal o) {
		if(o.getType() instanceof HDLUserDefinedType){
			((HDLUserDefinedType)o.getType()).accept(this);
		}
		String s = "";
		// signal部の文字列を作成
		if(o.getResetValue() != null){
			if(o.getName().contains("sig")){
				if(o.getName().contains("return")){
					s = String.format("signal %s : %s := %s;", o.getName(), o.getType().getVHDL(), o.getResetValue().getVHDL());
					usingSignals.add(o);
				}
			}else if(o.isRegister()){
				int before = varSignals.size();
				addVarSignal(o);
				if(before != varSignals.size()){
					for (String cn : compNames){
						if(o.getName().contains(cn)){
							s = String.format("signal %s : %s := %s;", o.getName(), o.getType().getVHDL(), o.getResetValue().getVHDL());
							usingSignals.add(o);
						}else if(!o.getResetValue().getVHDL().equals("(others => '0')")){
							s = String.format("signal %s : %s := %s;", o.getName(), o.getType().getVHDL(), o.getResetValue().getVHDL());
							usingSignals.add(o);
						}
					}
				}
			}else if(o.isWire()){
				for (String cn : compNames){
					if(o.getName().contains(cn) && !o.getName().contains("clk") && !o.getName().contains("reset")){
						s = String.format("signal %s : %s;", o.getName(), o.getType().getVHDL());
						usingSignals.add(o);
					}
				}
			}
			// signal部の出力
			if(!s.equals("")){
				HDLUtils.println(dest, offset, s);
			}

			if(o.isDebugFlag()){
				HDLUtils.println(dest, offset, String.format("attribute mark_debug of %s : signal is \"true\";", o.getName()));
				HDLUtils.println(dest, offset, String.format("attribute keep of %s : signal is \"true\";", o.getName()));
				HDLUtils.println(dest, offset, String.format("attribute S of %s : signal is \"true\";", o.getName()));
			}
		}
	}

	public void addVarSignal(HDLSignal o){
		String[] s = o.getName().split("_");
		if(1 < s.length){
			if(!("returnbusyexprreqmethod".contains(s[1]))){
				varSignals.add(o);
			}
		}
	}

	@Override
	public void visitHDLType(HDLPrimitiveType o) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visitHDLUserDefinedType(HDLUserDefinedType o) {
		if(definedType.containsKey(o) == true) return;
		definedType.put(o, true);
		/*HDLUtils.println(dest, offset, String.format("type %s is (", o.getName()));
		String sep = "";
		for(HDLValue s: o.getItems()){
			HDLUtils.print(dest, 0, sep);
			HDLUtils.print(dest, offset+2, String.format("%s", s.getVHDL()));
			sep = "," + Constant.BR;
		}
		HDLUtils.println(dest, offset, String.format("%s  );", Constant.BR));*/
	}

	@Override
	public void visitHDLInstanceRef(HDLInstanceRef o){
	}

}
