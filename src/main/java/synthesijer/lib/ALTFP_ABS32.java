//package synthesijer.lib;
package synthesijer.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_ABS32 extends HDLModule{

	public float a;
	public float result;
	public boolean valid;
	public boolean nd;

	public static ArrayList<Op> op = new ArrayList<Op>(
		Arrays.asList(
			Op.FGT32,
      Op.JT,
      Op.ASSIGN,
      Op.FMUL32,
      Op.ASSIGN,
			Op.RETURN
		)
	);

	public static int[] pred_0 = {1};
	public static int[] succ_0 = {-1};
	public static int[] pred_1 = {};
	public static int[] succ_1 = {0};
	public static int[] pred_2 = {5};
	public static int[] succ_2 = {-1};
	public static int[] pred_3 = {4};
	public static int[] succ_3 = {-1};
	public static int[] pred_4 = {5};
	public static int[] succ_4 = {3};
	public static int[] pred_5 = {};
	public static int[] succ_5 = {2, 4};

	public static ArrayList<SchedulerItemModel> coverItem = new ArrayList<SchedulerItemModel>(){
    {
      add(new SchedulerItemModel(Op.FGT32, pred_0, succ_0));
			add(new SchedulerItemModel(Op.JT, pred_1, succ_1));
			add(new SchedulerItemModel(Op.ASSIGN, pred_2, succ_2));
			add(new SchedulerItemModel(Op.FMUL32, pred_3, succ_3));
			add(new SchedulerItemModel(Op.ASSIGN, pred_4, succ_4));
			add(new SchedulerItemModel(Op.RETURN, pred_5, succ_5));
    }
	};

	public ALTFP_ABS32(){
		super("synthesijer_altfp_abs32", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

  public static String getOpName(){
		return "ALTFP_ABS32";
	}
}