package Patch.Hadoop.Job;

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

public class JobUnd {
	public JobUnd(int argCount,JobVariable job,InvokeExpr expr,CFGDefUse inputducfg,NodeDefUses duNode,List<SootMethod>bindsm,List<Stmt>bindstmt,SootClass lib){
		if(argCount==1){
			Value argu0 = expr.getArg(0);
			if(argu0 instanceof ClassConstant){
				// 1. Firstly find the class
				ClassConstant vclssConstant = (ClassConstant)argu0;
				String clsName = vclssConstant.getValue().toString();
				clsName = clsName.replaceAll("/", ".");
				SootClass cls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(clsName, lib);
				if(cls==null)
					return;
				if(lib.getName().equals("org.apache.hadoop.mapreduce.InputFormat")||
						lib.getName().equals("org.apache.hadoop.mapreduce.OutputFormat")){
					for(SootMethod sm:cls.getMethods()){
						if(sm.isConstructor()){
							bindsm.add(sm);
						}
					}
				}else if(lib.getName().equals("org.apache.hadoop.mapreduce.Reducer")||
						lib.getName().equals("org.apache.hadoop.mapreduce.Combiner")){
					List<SootMethod> reducesm = JobMethodBind.findallMethodByName(cls,"reduce");
					List<SootMethod> setsm = JobMethodBind.findallMethodByName(cls,"setup");
					List<SootMethod> runsm = JobMethodBind.findallMethodByName(cls,"run");
					List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(cls,"cleanup");
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
				}else if(lib.getName().equals("org.apache.hadoop.mapreduce.Mapper")){
					// 2. 
					List<SootMethod> mapsm = JobMethodBind.findallMethodByName(cls,"map");
					List<SootMethod> setsm = JobMethodBind.findallMethodByName(cls,"setup");
					List<SootMethod> runsm = JobMethodBind.findallMethodByName(cls,"run");
					List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(cls,"cleanup");
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
				}
			}else{
				if(argu0 instanceof Local||
						argu0 instanceof FieldRef){
					Variable videf = duNode.getDeffromValue(argu0);
					int id = inputducfg.getDefVariableId(videf);
					NodeDefUses defNode = (NodeDefUses) inputducfg.getNodes().get(id);
					bindstmt.add(defNode.getStmt());
				}else{
					
				}
			}
		}else if(argCount==2){
			Value argu1 = expr.getArg(1);
			if(argu1 instanceof ClassConstant){
				// 1. Firstly find the class
				ClassConstant vclssConstant = (ClassConstant)argu1;
				String inputFormatclsName = vclssConstant.getValue().toString();
				inputFormatclsName = inputFormatclsName.replaceAll("/", ".");
				SootClass cls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(inputFormatclsName, lib);
				if(cls==null)
					return;
				if(lib.getName().equals("org.apache.hadoop.mapreduce.InputFormat")||
						lib.getName().equals("org.apache.hadoop.mapreduce.OutputFormat")){
					for(SootMethod sm:cls.getMethods()){
						if(sm.isConstructor()){
							bindsm.add(sm);
						}
					}
				}else if(lib.getName().equals("org.apache.hadoop.mapreduce.Reducer")||
						lib.getName().equals("org.apache.hadoop.mapreduce.Combiner")){
					List<SootMethod> reducesm = JobMethodBind.findallMethodByName(cls,"reduce");
					List<SootMethod> setsm = JobMethodBind.findallMethodByName(cls,"setup");
					List<SootMethod> runsm = JobMethodBind.findallMethodByName(cls,"run");
					List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(cls,"cleanup");
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
				}else if(lib.getName().equals("org.apache.hadoop.mapreduce.Mapper")){
					// 2. 
					List<SootMethod> mapsm = JobMethodBind.findallMethodByName(cls,"map");
					List<SootMethod> setsm = JobMethodBind.findallMethodByName(cls,"setup");
					List<SootMethod> runsm = JobMethodBind.findallMethodByName(cls,"run");
					List<SootMethod> cleanupsm = JobMethodBind.findallMethodByName(cls,"cleanup");
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
				}
			}else{
				if(argu1 instanceof Local||
						argu1 instanceof FieldRef){
					Variable videf = duNode.getDeffromValue(argu1);
					int id = inputducfg.getDefVariableId(videf);
					NodeDefUses defNode = (NodeDefUses) inputducfg.getNodes().get(id);
					bindstmt.add(defNode.getStmt());
				}else{
					
				}
			}
		}
	}
	
}
