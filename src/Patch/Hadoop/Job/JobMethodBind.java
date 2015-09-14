package Patch.Hadoop.Job;
import java.util.LinkedList;
import java.util.List;
import Patch.Hadoop.Job.Understand.setCombinerClassUnd;
import Patch.Hadoop.Job.Understand.setMapperClassUnd;
import Patch.Hadoop.Job.Understand.setReducerClassUnd;
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
			new setCombinerClassUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libReducer);
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
			new setMapperClassUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libMapper);
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
			new setReducerClassUnd(argCount, job, expr, inputducfg, duNode, bindsm, bindstmt, libReducer);
			break;
		}
		
		}
	}
	
	public List<SootMethod> getBindingMethod(){
		return bindsm;
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
