package Patch.Hadoop.Job;
import java.util.LinkedList;
import java.util.List;

import Patch.Hadoop.CommonAss.AssetType;
import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.MethodTag;

public class JobMethodBind {
	List<SootMethod>bindsm= null;
	List<Stmt>bindstmt = null;
	AssetType bindType = AssetType.NON;
	SootClass libMapper = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Mapper");
	SootClass libReducer = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Reducer");
	SootClass libInputFormat = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.InputFormat");
	SootClass libOutputFormat = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.OutputFormat");
	public JobMethodBind(JobVariable job,InvokeExpr expr,CFGDefUse inputducfg,NodeDefUses duNode){
		bindsm = new LinkedList<SootMethod>();
		bindstmt = new LinkedList<Stmt>();
		SootMethod invokemethod = expr.getMethod();
		int argCount = expr.getArgCount();
		String methodName =invokemethod.getName();
		switch(methodName){
		case "setCombinerClass":{
			new JobUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libReducer);
			bindType = AssetType.Class;
			break;
		}
		case "setInputFormatClass":{
			new JobUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libInputFormat);
			bindType = AssetType.Method;
			break;
		}
		case "setMapOutputKeyClass":{
			break;
		}
		case "setMapOutputValueClass":{
			break;
		}
		case "setMapperClass":{
			new JobUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libMapper);
			bindType = AssetType.Class;
			break;
		}
		case "setOutputFormatClass":{
			new JobUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libOutputFormat);
			bindType = AssetType.Method;
			break;
		}
		case "setOutputValueClass":{
			break;
		}
		case "setOutputKeyClass":{
			break;
		}
		case "setReducerClass":{
			new JobUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libReducer);
			bindType = AssetType.Class;
			break;
		}
		
		}
	}
	
	public List<SootMethod> getBindingMethod(){
		return bindsm;
	}
	public AssetType getBindType(){
		return bindType;
	}
	public static List<SootMethod> findallMethodByName(SootClass sc,String methodName){
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
