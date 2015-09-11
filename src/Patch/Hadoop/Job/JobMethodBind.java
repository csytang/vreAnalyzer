package Patch.Hadoop.Job;

import java.util.LinkedList;
import java.util.List;

import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class JobMethodBind {
	List<SootMethod>bindsm= null;
	SootClass libMapper = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Mapper");
	SootClass libReducer = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Reducer");
	public JobMethodBind(JobVariable job,InvokeExpr expr){
		bindsm = new LinkedList<SootMethod>();
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
					
					SootMethod mapsm = findMethodByName(mappercls,"map");
					SootMethod setsm = findMethodByName(mappercls,"setup");
					SootMethod runsm = findMethodByName(mappercls,"run");
					SootMethod cleanupsm = findMethodByName(mappercls,"cleanup");
					if(cleanupsm!=null){
						bindsm.add(cleanupsm);
					}
					if(mapsm!=null){
						bindsm.add(mapsm);
					}
					if(setsm!=null){
						bindsm.add(setsm);
					}
					if(runsm!=null){
						bindsm.add(runsm);
					}
				}else{
					
					
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
					SootMethod mapsm = findMethodByName(mappercls,"map");
					SootMethod setsm = findMethodByName(mappercls,"setup");
					SootMethod runsm = findMethodByName(mappercls,"run");
					SootMethod cleanupsm = findMethodByName(mappercls,"cleanup");
					if(cleanupsm!=null){
						bindsm.add(cleanupsm);
					}
					if(mapsm!=null){
						bindsm.add(mapsm);
					}
					if(setsm!=null){
						bindsm.add(setsm);
					}
					if(runsm!=null){
						bindsm.add(runsm);
					}
				}else{
					
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
	public SootMethod findMethodByName(SootClass sc,String methodName){
		List<SootMethod>allmethods = sc.getMethods();
		SootMethod foundMethod = null;
		for(SootMethod sm:allmethods){
			if(sm.getName().equals(methodName) && !Modifier.isVolatile(sm.getModifiers())){
				if(foundMethod==null)
					foundMethod = sm;
				else
					throw new RuntimeException("ambiguous method: " + methodName + " in class " + sc);
			}
		}
		return foundMethod;
	}
	
	
	
}
