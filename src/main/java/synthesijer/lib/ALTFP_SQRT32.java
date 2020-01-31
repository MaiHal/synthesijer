//命令の追加
//package synthesijer.lib;
package synthesijer.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import synthesijer.ast.Type;
import synthesijer.ast.type.PrimitiveTypeKind;
import synthesijer.hdl.HDLModule;
import synthesijer.hdl.HDLPort.DIR;
import synthesijer.hdl.HDLPrimitiveType;

public class ALTFP_SQRT32 extends HDLModule{

	public float a;
	public float result;
	public boolean valid;
	public boolean nd;

	public static ArrayList<Op> op = new ArrayList<Op>(
		Arrays.asList(
			Op.FCOMPEQ32,
			Op.JT,
			Op.ASSIGN,
			Op.FGT32,
			Op.COND,
			Op.ASSIGN,
			Op.ASSIGN,
			Op.FGT32,
			Op.JT,
			Op.FDIV32,
			Op.FADD32,
			Op.FDIV32,
			Op.ASSIGN,
			Op.FSUB32,
			Op.ASSIGN,
			Op.ASSIGN,
			Op.RETURN
		)
	);

	public static ArrayList<OperandModel> srcs = new ArrayList<OperandModel>(){
    {
			add(new OperandModel("VAR", "FLOAT", 0)); //0 FCOMPEQ32
			add(new OperandModel("CONSTANT", "FLOAT", -1)); //1 FCOMPEQ32
			add(new OperandModel("VAR", "BOOLEAN", 1)); //2 JT
			add(new OperandModel("CONSTANT", "FLOAT", -1)); //3 ASSIGN
			add(new OperandModel("VAR", "FLOAT", 0)); //4 FGT32
			add(new OperandModel("CONSTANT", "FLOAT", -1)); //5 FGT32
			add(new OperandModel("VAR", "BOOLEAN", 2)); //6 COND
			add(new OperandModel("VAR", "FLOAT", 0)); //7 COND
			add(new OperandModel("CONSTANT", "FLOAT", -1)); //8 COND
			add(new OperandModel("VAR", "FLOAT", 0)); //9 ASSIGN
			add(new OperandModel("VAR", "FLOAT", 3)); //10 ASSIGN
			add(new OperandModel("VAR", "FLOAT", 4)); //11 FGT32
			add(new OperandModel("CONSTANT", "FLOAT", -1)); //12 FGT32
			add(new OperandModel("VAR", "BOOLEAN", 5)); //13 JT
			add(new OperandModel("VAR", "FLOAT", 0)); //14 FDIV32
			add(new OperandModel("VAR", "FLOAT", 6)); //15 FDIV32
			add(new OperandModel("VAR", "FLOAT", 6)); //16 FADD32
			add(new OperandModel("VAR", "FLOAT", 7)); //17 FADD32
			add(new OperandModel("VAR", "FLOAT", 8)); //18 FDIV32
			add(new OperandModel("CONSTANT", "FLOAT", -1)); //19 FDIV32
			add(new OperandModel("VAR", "FLOAT", 9)); //20 ASSIGN
			add(new OperandModel("VAR", "FLOAT", 6)); //21 FSUB32
			add(new OperandModel("VAR", "FLOAT", 10)); //22 FSUB32
			add(new OperandModel("VAR", "FLOAT", 11)); //23 ASSIGN
			add(new OperandModel("VAR", "FLOAT", 10)); //24 ASSIGN
			add(new OperandModel("VAR", "FLOAT", 6)); //25 RETURN
		}
	};

	public static ArrayList<VariableOperandModel> dests = new ArrayList<VariableOperandModel>(){
    {
			add(new VariableOperandModel("BOOLEAN", 1)); //0 FCOMPEQ32
			add(new VariableOperandModel(null, 0)); //1 JT
			add(new VariableOperandModel("FLOAT", 4)); //2 ASSIGN
			add(new VariableOperandModel("BOOLEAN", 1)); //3 FGT
			add(new VariableOperandModel("FLOAT", 1)); //4 COND
			add(new VariableOperandModel("FLOAT", 1)); //5 ASSIGN
			add(new VariableOperandModel("FLOAT", 4)); //6 ASSIGN
			add(new VariableOperandModel("BOOLEAN", 1)); //7 FGT32
			add(new VariableOperandModel(null, 0)); //8 JT
			add(new VariableOperandModel("FLOAT", 1)); //9 FDIV32
			add(new VariableOperandModel("FLOAT", 1)); //10 FADD32
			add(new VariableOperandModel("FLOAT", 1)); //11 FDIV32
			add(new VariableOperandModel("FLOAT", 2)); //12 ASSIGN
			add(new VariableOperandModel("FLOAT", 1)); //13 FSUB32
			add(new VariableOperandModel("FLOAT", 1)); //14 ASSIGN
			add(new VariableOperandModel("FLOAT", 4)); //15 ASSIGN
			add(new VariableOperandModel(null, 0)); //16 RETURN
		}
	};

	/*public static ArrayList<SchedulerItemModel> coverItem = new ArrayList<SchedulerItemModel>(){
    {
      add(new SchedulerItemModel(Op.FCOMPEQ32, srcs.get(0), srcs.get(1), null, dests.get(0)));
			add(new SchedulerItemModel(Op.JT, srcs.get(2), null, null, dests.get(1)));
			add(new SchedulerItemModel(Op.ASSIGN, srcs.get(3), null, null, dests.get(2)));
			add(new SchedulerItemModel(Op.FGT32, srcs.get(4), srcs.get(5), null, dests.get(3)));
			add(new SchedulerItemModel(Op.COND, srcs.get(6), srcs.get(7), srcs.get(8), dests.get(4)));
			add(new SchedulerItemModel(Op.ASSIGN, srcs.get(9), null, null, dests.get(5)));
			add(new SchedulerItemModel(Op.ASSIGN, srcs.get(10), null, null, dests.get(6)));
			add(new SchedulerItemModel(Op.FGT32, srcs.get(11), srcs.get(12), null, dests.get(7)));
			add(new SchedulerItemModel(Op.JT, srcs.get(13), null, null, dests.get(8)));
			add(new SchedulerItemModel(Op.FDIV32, srcs.get(14), srcs.get(15), null, dests.get(9)));
			add(new SchedulerItemModel(Op.FADD32, srcs.get(16), srcs.get(17), null, dests.get(10)));
			add(new SchedulerItemModel(Op.FDIV32, srcs.get(18), srcs.get(19), null, dests.get(11)));
			add(new SchedulerItemModel(Op.ASSIGN, srcs.get(20), null, null, dests.get(12)));
			add(new SchedulerItemModel(Op.FSUB32, srcs.get(21), srcs.get(22), null, dests.get(13)));
			add(new SchedulerItemModel(Op.ASSIGN, srcs.get(23), null, null, dests.get(14)));
			add(new SchedulerItemModel(Op.ASSIGN, srcs.get(24), null, null, dests.get(15)));
			add(new SchedulerItemModel(Op.RETURN, srcs.get(25), null, null, dests.get(16)));
    }
  };*/

	public static int[] pred_0 = {1};
	public static int[] succ_0 = {-1};
	public static int[] pred_1 = {};
	public static int[] succ_1 = {0};
	public static int[] pred_2 = {9, 10, 13, 16};
	public static int[] succ_2 = {};
	public static int[] pred_3 = {4};
	public static int[] succ_3 = {-1};
	public static int[] pred_4 = {6};
	public static int[] succ_4 = {-1, 3};
	public static int[] pred_5 = {7};
	public static int[] succ_5 = {-1};
	public static int[] pred_6 = {9, 10, 13, 16};
	public static int[] succ_6 = {4};
	public static int[] pred_7 = {8};
	public static int[] succ_7 = {5};
	public static int[] pred_8 = {};
	public static int[] succ_8 = {7};
	public static int[] pred_9 = {10};
	public static int[] succ_9 = {-1, 2, 6};
	public static int[] pred_10 = {11};
	public static int[] succ_10 = {2, 6, 9};
	public static int[] pred_11 = {12};
	public static int[] succ_11 = {10};
	public static int[] pred_12 = {13, 15};
	public static int[] succ_12 = {11};
	public static int[] pred_13 = {14};
	public static int[] succ_13 = {2, 6, 12};
	public static int[] pred_14 = {};
	public static int[] succ_14 = {13};
	public static int[] pred_15 = {16};
	public static int[] succ_15 = {12};
	public static int[] pred_16 = {};
	public static int[] succ_16 = {2, 6, 15};
	

	public static ArrayList<SchedulerItemModel> coverItem = new ArrayList<SchedulerItemModel>(){
    {
      add(new SchedulerItemModel(Op.FCOMPEQ32, pred_0, succ_0));
			add(new SchedulerItemModel(Op.JT, pred_1, succ_1));
			add(new SchedulerItemModel(Op.ASSIGN, pred_2, succ_2));
			add(new SchedulerItemModel(Op.FGT32, pred_3, succ_3));
			add(new SchedulerItemModel(Op.COND, pred_4, succ_4));
			add(new SchedulerItemModel(Op.ASSIGN, pred_5, succ_5));
			add(new SchedulerItemModel(Op.ASSIGN, pred_6, succ_6));
			add(new SchedulerItemModel(Op.FGT32, pred_7, succ_7));
			add(new SchedulerItemModel(Op.JT, pred_8, succ_8));
			add(new SchedulerItemModel(Op.FDIV32, pred_9, succ_9));
			add(new SchedulerItemModel(Op.FADD32, pred_10, succ_10));
			add(new SchedulerItemModel(Op.FDIV32, pred_11, succ_11));
			add(new SchedulerItemModel(Op.ASSIGN, pred_12, succ_12));
			add(new SchedulerItemModel(Op.FSUB32, pred_13, succ_13));
			add(new SchedulerItemModel(Op.ASSIGN, pred_14, succ_14));
			add(new SchedulerItemModel(Op.ASSIGN, pred_15, succ_15));
			add(new SchedulerItemModel(Op.RETURN, pred_16, succ_16));
    }
	};

	public ALTFP_SQRT32(){
		super("synthesijer_altfp_sqrt32", "clk", "reset");
		newPort("a",      DIR.IN,  HDLPrimitiveType.genVectorType(32));
		newPort("nd",     DIR.IN,  HDLPrimitiveType.genBitType());
		newPort("result", DIR.OUT, HDLPrimitiveType.genVectorType(32));
		newPort("valid",  DIR.OUT, HDLPrimitiveType.genBitType());
	}

	public static String getOpName(){
		return "ALTFP_SQRT32";
	}
}
