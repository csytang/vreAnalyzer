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

public class setMapperClassUnd {
	public setMapperClassUnd(int argCount,JobVariable job,InvokeExpr expr,CFGDefUse inputducfg,NodeDefUses duNode,List<SootMethod>bindsm,List<Stmt>bindstmt,SootClass libMapper ){
		if(argCount==1){
			Value mapperclsValue = expr.getArg(0);
			if(mapperclsValue instanceof ClassConstant){
				// 1. Firstly find the class
				ClassConstant vclssConstant = (ClassConstant)mapperclsValue;
				String mapperclsName = vclssConstant.getValue().toString();
				mapperclsName = mapperclsName.replaceAll("/", ".");
				SootClass mappercls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(mapperclsName, libMapper);
				if(mappercls==null)
					return;
				// 2. 
				List<SootMethod> mapsm = JobMethodBind.findallMethodByName(mappercls,"map");
				List<SootMethod> setsm = JobMethodBind.findallMethodByName(mappercls,"setup");
				List<SootMethod> runsm = JobMethodBind.findallMethodByName(mappercls,"run");
				List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(mappercls,"cleanup");
				if(cleanupsm!=null){
					bindsm.addAll(cleanupsm);
				}
				if(mapsm!=null){
					bindsm.addAll(mapsm);
				}
				if(setsm!=null){
					bindsm.addAll(setsm);
				}
				if(runsm!=null){
					bindsm.addAll(runsm);
				}
			}else{
				// this job is represented by a variable 
				if(mapperclsValue instanceof Local||
						mapperclsValue instanceof FieldRef){
					Variable videf = duNode.getDeffromValue(mapperclsValue);
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
			Value mapperclsValue = expr.getArg(1);
			if(mapperclsValue instanceof ClassConstant){
				// 1. Firstly find the class
				ClassConstant vclssConstant = (ClassConstant)mapperclsValue;
				String mapperclsName = vclssConstant.getValue().toString();
				mapperclsName = mapperclsName.replaceAll("/", ".");
				SootClass mappercls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(mapperclsName, libMapper);
				// 2. 
				if(mappercls==null)
					return;
				List<SootMethod> mapsm = JobMethodBind.findallMethodByName(mappercls,"map");
				List<SootMethod> setsm = JobMethodBind.findallMethodByName(mappercls,"setup");
				List<SootMethod> runsm = JobMethodBind.findallMethodByName(mappercls,"run");
				List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(mappercls,"cleanup");
				if(cleanupsm!=null){
					bindsm.addAll(cleanupsm);
				}
				if(mapsm!=null){
					bindsm.addAll(mapsm);
				}
				if(setsm!=null){
					bindsm.addAll(setsm);
				}
				if(runsm!=null){
					bindsm.addAll(runsm);
				}
			}else{
				// this job is represented by a variable 
				if(mapperclsValue instanceof Local||
						mapperclsValue instanceof FieldRef){
					Variable videf = duNode.getDeffromValue(mapperclsValue);
					int id = inputducfg.getDefVariableId(videf);
					NodeDefUses defNode = (NodeDefUses) inputducfg.getNodes().get(id);
					bindstmt.add(defNode.getStmt());					
				}else{
					
				}
			}
		}
	}
}
