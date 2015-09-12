package Patch.Hadoop.Job;
import java.util.LinkedList;
import java.util.List;

import soot.Local;
import soot.Modifier;
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
import vreAnalyzer.Tag.MethodTag;

public class JobMethodBind {
	List<SootMethod>bindsm= null;
	List<Stmt>bindstmt = null;
	SootClass libMapper = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Mapper");
	SootClass libReducer = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Reducer");
	public JobMethodBind(JobVariable job,InvokeExpr expr,CFGDefUse inputducfg,NodeDefUses duNode){
		bindsm = new LinkedList<SootMethod>();
		bindstmt = new LinkedList<Stmt>();
		SootMethod invokemethod = expr.getMethod();
		int argCount = expr.getArgCount();
		String methodName =invokemethod.getName();
		switch(methodName){
		case "setCombinerClass":{
			break;
		}
		case "setInputFormatClass":{
			break;
		}
		case "setMapOutputKeyClass":{
			break;
		}
		case "setMapOutputValueClass":{
			break;
		}
		case "setMapperClass":{
			if(argCount==1){
				Value mapperclsValue = expr.getArg(0);
				if(mapperclsValue instanceof ClassConstant){
					// 1. Firstly find the class
					ClassConstant vclssConstant = (ClassConstant)mapperclsValue;
					String mapperclsName = vclssConstant.getValue().toString();
					mapperclsName = mapperclsName.replaceAll("/", ".");
					SootClass mappercls = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(mapperclsName, libMapper);
					// 2. 
					List<SootMethod> mapsm = findallMethodByName(mappercls,"map");
					List<SootMethod> setsm = findallMethodByName(mappercls,"setup");
					List<SootMethod> runsm = findallMethodByName(mappercls,"run");
					List<SootMethod> cleanupsm = findallMethodByName(mappercls,"cleanup");
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
					List<SootMethod> mapsm = findallMethodByName(mappercls,"map");
					List<SootMethod> setsm = findallMethodByName(mappercls,"setup");
					List<SootMethod> runsm = findallMethodByName(mappercls,"run");
					List<SootMethod> cleanupsm = findallMethodByName(mappercls,"cleanup");
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
			break;
		}
		case "setOutputFormatClass":{
			break;
		}
		case "setOutputValueClass":{
			break;
		}
		case "setOutputKeyClass":{
			break;
		}
		case "setReducerClass":{
			break;
		}
		
		
		
		
		
		
		}
	}
	
	public List<SootMethod> getBindingMethod(){
		return bindsm;
	}
	public List<SootMethod> findallMethodByName(SootClass sc,String methodName){
		List<SootMethod>allmethods = sc.getMethods();
		List<SootMethod> foundMethod = new LinkedList<SootMethod>();
		for(SootMethod sm:allmethods){
			if((sm.getName().equals(methodName) && !Modifier.isVolatile(sm.getModifiers()))&&
					sm.hasTag(MethodTag.TAG_NAME)){
				foundMethod.add(sm);
			}
		}
		return foundMethod;
	}
	
	
	
}
