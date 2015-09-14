package Patch.Hadoop.Job.Understand;

import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import Patch.Hadoop.Job.JobVariable;

public class setInputFormatClassUnd {
	public setInputFormatClassUnd(int argCount,JobVariable job,InvokeExpr expr,CFGDefUse inputducfg,NodeDefUses duNode,List<SootMethod>bindsm,List<Stmt>bindstmt){
		if(argCount==1){
			Value inputFormatValue = expr.getArg(0);
			if(inputFormatValue instanceof ClassConstant){
				
			}else{
				
			}
		}else if(argCount==2){
			Value inputFormatValue = expr.getArg(1);
			if(inputFormatValue instanceof ClassConstant){
				
			}else{
				
			}
		}
	}
}
