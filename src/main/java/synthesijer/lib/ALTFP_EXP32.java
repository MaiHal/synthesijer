//package synthesijer.lib;
package synthesijer.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_EXP32 extends HDLModule{

	public float a;
	public float result;
	public boolean valid;
	public boolean nd;

	public static ArrayList<Op> op = new ArrayList<Op>(
		Arrays.asList(
			Op.FGT32,
      Op.JT,
      Op.FGT32,
      Op.JT,
      Op.FMUL32,
      Op.ASSIGN,
      Op.FSUB32,
      Op.ASSIGN,
      Op.FLT32,
      Op.JT,
      Op.FLT32,
      Op.JT,
      Op.FDIV32,
      Op.ASSIGN,
      Op.FADD32,
      Op.ASSIGN,
			Op.RETURN
		)
	);

	public static int[] pred_0 = {1};
	public static int[] succ_0 = {-1};
	public static int[] pred_1 = {};
	public static int[] succ_1 = {0};
	public static int[] pred_2 = {3};
	public static int[] succ_2 = {-1};
	public static int[] pred_3 = {};
	public static int[] succ_3 = {2};
	public static int[] pred_4 = {5};
	public static int[] succ_4 = {-3, -2};
	public static int[] pred_5 = {12, 16};
	public static int[] succ_5 = {4};
	public static int[] pred_6 = {7};
	public static int[] succ_6 = {-1};
	public static int[] pred_7 = {8, 10, 14};
	public static int[] succ_7 = {6};
	public static int[] pred_8 = {9};
	public static int[] succ_8 = {-1, 7};
	public static int[] pred_9 = {};
	public static int[] succ_9 = {8};
	public static int[] pred_10 = {11};
	public static int[] succ_10 = {-1, 7};
	public static int[] pred_11 = {};
	public static int[] succ_11 = {10};
	public static int[] pred_12 = {13};
	public static int[] succ_12 = {-3, -2, 5};
	public static int[] pred_13 = {16};
	public static int[] succ_13 = {12};
	public static int[] pred_14 = {15};
	public static int[] succ_14 = {-1, 7};
	public static int[] pred_15 = {};
	public static int[] succ_15 = {14};
	public static int[] pred_16 = {};
	public static int[] succ_16 = {-2, 5, 13};

	public static ArrayList<SchedulerItemModel> coverItem = new ArrayList<SchedulerItemModel>(){
    {
      add(new SchedulerItemModel(Op.FGT32, pred_0, succ_0));
			add(new SchedulerItemModel(Op.JT, pred_1, succ_1));
			add(new SchedulerItemModel(Op.FGT32, pred_2, succ_2));
			add(new SchedulerItemModel(Op.JT, pred_3, succ_3));
			add(new SchedulerItemModel(Op.FMUL32, pred_4, succ_4));
			add(new SchedulerItemModel(Op.ASSIGN, pred_5, succ_5));
			add(new SchedulerItemModel(Op.FSUB32, pred_6, succ_6));
			add(new SchedulerItemModel(Op.ASSIGN, pred_7, succ_7));
			add(new SchedulerItemModel(Op.FLT32, pred_8, succ_8));
			add(new SchedulerItemModel(Op.JT, pred_9, succ_9));
			add(new SchedulerItemModel(Op.FLT32, pred_10, succ_10));
			add(new SchedulerItemModel(Op.JT, pred_11, succ_11));
			add(new SchedulerItemModel(Op.FDIV32, pred_12, succ_12));
			add(new SchedulerItemModel(Op.ASSIGN, pred_13, succ_13));
			add(new SchedulerItemModel(Op.FADD32, pred_14, succ_14));
			add(new SchedulerItemModel(Op.ASSIGN, pred_15, succ_15));
			add(new SchedulerItemModel(Op.RETURN, pred_16, succ_16));
    }
	};

	public ALTFP_EXP32(){
		super("synthesijer_altfp_exp32", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

  public static String getOpName(){
		return "ALTFP_EXP32";
	}
}