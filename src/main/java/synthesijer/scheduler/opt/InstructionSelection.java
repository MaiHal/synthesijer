package synthesijer.scheduler.opt;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Enumeration;

import synthesijer.SynthesijerUtils;
import synthesijer.scheduler.Op;
import synthesijer.scheduler.Operand;
import synthesijer.scheduler.SchedulerBoard;
import synthesijer.scheduler.SchedulerInfo;
import synthesijer.scheduler.SchedulerItem;
import synthesijer.scheduler.SchedulerSlot;
import synthesijer.scheduler.VariableOperand;

public class InstructionSelection implements SchedulerInfoOptimizer{

  public InstructionSelection(ArrayList<SSAGraphNode> nodes){
    ArrayList<Integer> rules = new ArrayList<Integer>();
    ArrayList<Integer> comfirmed_rules = new ArrayList<Integer>();
    for(SSAGraphNode n: nodes){
      rules.add(getRules(n));
      PBQP p = new PBQP(rules);
      comfirmed_rules.add(p.confirmed_rule);
    }
  }

  public SchedulerInfo opt(SchedulerInfo info){
    SchedulerInfo result = info.getSameInfo();
    for(SchedulerBoard b: info.getBoardsList()){
        result.addBoard(b);
    }
    return result;
  }

  public String getKey(){
    return "inst_sel";
  }

  public int getRules(SSAGraphNode n){
    return 1;
  }
}