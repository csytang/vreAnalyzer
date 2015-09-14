package Patch.Hadoop.Job.Understand;

import java.util.List;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import Patch.Hadoop.Job.JobMethodBind;
import Patch.Hadoop.Job.JobVariable;

public class setCombinerClassUnd {
	public setCombinerClassUnd(int argCount,JobVariable job,InvokeExpr expr,CFGDefUse inputducfg,NodeDefUses duNode,List<SootMethod>bindsm,List<Stmt>bindstmt,SootClass libReducer ){
		if(argCount==1){
			Value combinerclsValue = expr.getArg(0);
			if(combinerclsValue instanceof ClassConstant){
				// 1. Firstly find the class
				ClassConstant vclssConstant = (ClassConstant)combinerclsValue;
				String reducerclsName = vclssConstant.getValue().toString();
				reducerclsName = reducerclsName.replaceAll("/", ".");
				SootClass reducercls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(reducerclsName, libReducer);
				// 2. 
				if(reducercls==null)
					return;
				List<SootMethod> reducesm = JobMethodBind.findallMethodByName(reducercls,"reduce");
				List<SootMethod> setsm = JobMethodBind.findallMethodByName(reducercls,"setup");
				List<SootMethod> runsm = JobMethodBind.findallMethodByName(reducercls,"run");
				List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(reducercls,"cleanup");
				if(cleanupsm!=null){
					bindsm.addAll(cleanupsm);
				}
				if(reducesm!=null){
					bindsm.addAll(reducesm);
				}
				if(setsm!=null){
					bindsm.addAll(setsm);
				}
				if(runsm!=null){
					bindsm.addAll(runsm);
				}
			}else{
				// this job is represented by a variable 
				if(combinerclsValue instanceof Local||
						combinerclsValue instanceof FieldRef){
					Variable videf = duNode.getDeffromValue(combinerclsValue);
					int id = inputducfg.getDefVariableId(videf);
					NodeDefUses defNode = (NodeDefUses) inputducfg.getNodes().get(id);
					bindstmt.add(defNode.getStmt());
				}else{
					
				}
				
			}
		}else if(argCount==2){
			// Job
			Value jobValue = expr.getArg(0);
			// MapperClass
			Value combinerclsValue = expr.getArg(1);
			if(combinerclsValue instanceof ClassConstant){
				// 1. Firstly find the class
				ClassConstant vclssConstant = (ClassConstant)combinerclsValue;
				String reducerclsName = vclssConstant.getValue().toString();
				reducerclsName = reducerclsName.replaceAll("/", ".");
				SootClass reducercls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(reducerclsName, libReducer);
				// 2. 
				if(reducercls==null)
					return;
				List<SootMethod> reducesm = JobMethodBind.findallMethodByName(reducercls,"reduce");
				List<SootMethod> setsm = JobMethodBind.findallMethodByName(reducercls,"setup");
				List<SootMethod> runsm = JobMethodBind.findallMethodByName(reducercls,"run");
				List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(reducercls,"cleanup");
				if(cleanupsm!=null){
					bindsm.addAll(cleanupsm);
				}
				if(reducesm!=null){
					bindsm.addAll(reducesm);
				}
				if(setsm!=null){
					bindsm.addAll(setsm);
				}
				if(runsm!=null){
					bindsm.addAll(runsm);
				}
			}else{
				// this job is represented by a variable 
				if(combinerclsValue instanceof Local||
						combinerclsValue instanceof FieldRef){
					Variable videf = duNode.getDeffromValue(combinerclsValue);
					int id = inputducfg.getDefVariableId(videf);
					NodeDefUses defNode = (NodeDefUses) inputducfg.getNodes().get(id);
					bindstmt.add(defNode.getStmt());					
				}else{
					
				}
			}
		}
	}
}
