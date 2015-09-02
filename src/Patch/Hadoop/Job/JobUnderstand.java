package Patch.Hadoop.Job;


import soot.SootClass;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class JobUnderstand {
	private static int numMapper = 0;
	private static int numReducer =0;
	private static int numCombiner = 0;
	private static boolean verbose = true;
	public JobUnderstand(){
		
	}
	///////////////////////////////////////Member function///////////////////////////
	public static int getNumberofReducer() {
		// TODO Auto-generated method stub
		return numReducer;
	}
	public static int getNumberofMapper(){
		return numMapper;
	}
	public static int getNumberofCombiner(){
		return numCombiner;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	///////////////////////////////Analyze function/////////////////////////////////
	public static void process_SetMapperClass(JobHub jobhub,InvokeExpr invokeExpr,NodeDefUses cfgNode,CFGDefUse cfg,SootClass libMapper){
		int argCount = invokeExpr.getArgCount();
		Value arg0 = invokeExpr.getArg(0);
		// 1. class constant 
		if(argCount==1){
			if(arg0 instanceof ClassConstant){
				ClassConstant vclassConstant = (ClassConstant)arg0;	
				String className = vclassConstant.getValue().toString();
				className = className.replaceAll("/", ".");
				SootClass mapperClass = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(className, libMapper);
				assert(mapperClass!=null);
				if(verbose)
					System.out.println("set a mapclass:\t"+mapperClass.getName()+" for job:"+jobhub.getJobName());
				jobhub.setMapperClass(mapperClass);
				numMapper++;
			}else {
				jobhub.setMapperClassVar(arg0, cfgNode);
				numMapper++;
			}
		}else if(argCount==2){
			if(arg0.getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
				Value arg1 = invokeExpr.getArg(1);
				if(arg1 instanceof ClassConstant){
					ClassConstant vclassConstant = (ClassConstant)arg1;					
					String className = vclassConstant.getValue().toString();
					className = className.replaceAll("/", ".");
					SootClass mapperClass = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(className, libMapper);
					assert(mapperClass!=null);
					if(verbose)
						System.out.println("set a mapclass:\t"+mapperClass.getName()+" for job:"+jobhub.getJobName());
					numMapper++;
					jobhub.setMapperClass(mapperClass);
				}else {
					jobhub.setMapperClassVar(arg1, cfgNode);
					numMapper++;
				}
			}
		}
		else {	
			new Exception("Multiple input arguments for mapper class in statement"+cfgNode.getStmt());
		}
	}
	
	public static void process_SetCombinerClass(JobHub jobhub,
			InvokeExpr invokeExpr, NodeDefUses cfgNode, CFGDefUse cfggraph,
			SootClass libReducer) {
		int argCount = invokeExpr.getArgCount();
		Value arg0 = invokeExpr.getArg(0);
		// 1. class constant 
		if(argCount==1){
			if(arg0 instanceof ClassConstant){
				ClassConstant vclassConstant = (ClassConstant)arg0;	
				String className = vclassConstant.getValue().toString();
				className = className.replaceAll("/", ".");
				SootClass combinerClass = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(className, libReducer);
				assert(combinerClass!=null);
				if(verbose)
					System.out.println("set a combiner:\t"+combinerClass.getName()+" for job:"+jobhub.getJobName());
				jobhub.setCombinerClass(combinerClass);
				numCombiner++;
			}else {
				jobhub.setCombinerClassVar(arg0, cfgNode);
				numCombiner++;
			}
		}else if(argCount==2){
			if(arg0.getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
				Value arg1 = invokeExpr.getArg(1);
				if(arg1 instanceof ClassConstant){
					ClassConstant vclassConstant = (ClassConstant)arg1;					
					String className = vclassConstant.getValue().toString();
					className = className.replaceAll("/", ".");
					SootClass combinerClass = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(className, libReducer);
					assert(combinerClass!=null);
					if(verbose)
						System.out.println("set a combiner:\t"+combinerClass.getName()+" for job:"+jobhub.getJobName());
					numCombiner++;
					jobhub.setCombinerClass(combinerClass);
				}else {
					jobhub.setCombinerClassVar(arg1, cfgNode);
					numCombiner++;
				}
			}
		}
		else {	
			new Exception("Multiple input arguments for combiner class in statement"+cfgNode.getStmt());
		}
		
	}


	public static void process_SetReducerClass(JobHub jobhub,
			InvokeExpr invokeExpr, NodeDefUses cfgNode, CFGDefUse cfggraph,
			SootClass libReducer) {
		int argCount = invokeExpr.getArgCount();
		Value arg0 = invokeExpr.getArg(0);
		// 1. class constant 
		if(argCount==1){
			if(arg0 instanceof ClassConstant){
				ClassConstant vclassConstant = (ClassConstant)arg0;	
				String className = vclassConstant.getValue().toString();
				className = className.replaceAll("/", ".");
				SootClass combinerClass = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(className, libReducer);
				assert(combinerClass!=null);
				if(verbose)
					System.out.println("set a reduceclass:\t"+combinerClass.getName()+" for job:"+jobhub.getJobName());
				jobhub.setReducerClass(combinerClass);
				numReducer++;
			}else {
				jobhub.setReducerClassVar(arg0, cfgNode);
				numReducer++;
			}
		}else if(argCount==2){
			if(arg0.getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
				Value arg1 = invokeExpr.getArg(1);
				if(arg1 instanceof ClassConstant){
					ClassConstant vclassConstant = (ClassConstant)arg1;					
					String className = vclassConstant.getValue().toString();
					className = className.replaceAll("/", ".");
					SootClass combinerClass = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(className, libReducer);
					assert(combinerClass!=null);
					if(verbose)
						System.out.println("set a combiner:\t"+combinerClass.getName()+" for job:"+jobhub.getJobName());
					numReducer++;
					jobhub.setReducerClass(combinerClass);
				}else {
					jobhub.setReducerClassVar(arg1, cfgNode);
					numReducer++;
				}
			}
		}
		else {	
			new Exception("Multiple input arguments for combiner class in statement"+cfgNode.getStmt());
		}
		
	}
	/////////////////////////////////////////////////////////////////////////////
}
