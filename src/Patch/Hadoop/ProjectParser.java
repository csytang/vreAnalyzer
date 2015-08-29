package Patch.Hadoop;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.grimp.NewInvokeExpr;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JNewExpr;
import soot.tagkit.AbstractHost;
import soot.tagkit.SourceLnPosTag;
import vreAnalyzer.Context.Context;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.PointsTo.PointsToGraph;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Util.Util;
	
public class ProjectParser {
	
	public static ProjectParser instance = null;
	private Body sootmethodBody = null;
	private SootMethod sootmethod = null;
	private SootClass sootcls = null;
	private CFGDefUse cfggraph;
	private boolean verbose = true;
	private Context allcontext = null;
	private static int numjobs = 0;
	private List<Context<SootMethod,CFGNode,PointsToGraph>> currContexts;
	/* 
	 * Store all jobs that defined in the main method,
	 * including locals and fields 
	 */
	
	private Map<JobVariable,JobHub>jobtoHub = null;
	private Map<Integer,JobVariable>indextoJob = null;
	

	public static ProjectParser inst(){
		if(instance==null)
			instance = new ProjectParser();
		return instance;
	}
	public JobHub getjobHub(Value job){
		return jobtoHub.get(job);
	}
	public void ClassParser(){
		jobtoHub = new HashMap<JobVariable,JobHub>();
		indextoJob = new HashMap<Integer,JobVariable>();// start from 1 insteadof 0
		// run all the application methods
		for(SootMethod sm:ProgramFlowBuilder.inst().getAppConcreteMethods()){
			cfggraph = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(sm);
			CFGNode exitNode = cfggraph.EXIT;
			currContexts = PointsToAnalysis.inst().getContexts(sm);
			allcontext = currContexts.get(0);	
			sootcls = sm.getDeclaringClass();
			sootmethodBody = sm.retrieveActiveBody();
			sootmethod = sm;
			Parse();
		}
		System.out.println("# Jobs:\t"+numjobs);
	}
	public void Parse(){
		// 1. Get the CFG
		int index = 0;
		List<CFGNode> allnodes = cfggraph.getNodes();
		// 2.0 Find mapper and reducer in lib class set
		SootClass libMapper = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Mapper");
		SootClass libReducer = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Reducer");
				
		// 2. Find all Jobs and associated with mapper and reducer classes
		for(int i = 0;i < allnodes.size();i++){
			NodeDefUses cfgNode = (NodeDefUses) allnodes.get(i);
			if(!cfgNode.isSpecial()){
				Stmt stmt = cfgNode.getStmt();
				// Not one assign value from this, parameter and caughtexception
				List<Variable>useVariables = cfgNode.getUsedVars();
				List<Variable>definedVariables = cfgNode.getDefinedVars();
				// Get the job from defined values
				boolean containsInvokeExpr = stmt.containsInvokeExpr();
				if(!definedVariables.isEmpty()){
					for(Variable defvar:definedVariables){	
							// If it is a define of job
						if(defvar.getValue().getType().toString().equals("org.apache.hadoop.mapreduce.Job")&&
								!defvar.getValue().toString().startsWith("$")){
								// number of jobs counter
							   //created by use standard API
								if(containsInvokeExpr){
									if(stmt.getInvokeExpr().getMethod().getName().toString().equals("getInstance")){
										// job is created by get instance
										Type parameterType = defvar.getValue().getType();
										assert((parameterType instanceof RefLikeType));					
										RefType parameter = (RefType)parameterType;
										JNewExpr argsExpr = new JNewExpr(parameter);
										PointsToGraph p2g = (PointsToGraph)allcontext.getValueAfter(cfgNode);
										p2g.assignNew((Local) defvar.getValue(), argsExpr);	
										System.out.println("The points to updated!");
										System.out.println("A job is created using standard Hadoop job API");
									}
									else if(stmt instanceof AssignStmt){
											// created by use customized functions
										
									}
									JobVariable jvb = new JobVariable(defvar,cfgNode);
									
									if(!jobtoHub.containsKey(jvb)){
										Value defValue = defvar.getValue();
										System.out.println("Add a job\t"+jvb+"\t @stmt"+stmt+"\t @method: "+sootmethod);
										numjobs++;
										JobHub jhb = new JobHub(jvb);
										jobtoHub.put(jvb, jhb);
										indextoJob.put(numjobs, jvb);
									}
								}
						}
			
					}
				}
			}
		}
	}
	private boolean containJobUse(List<Variable> useVariables) {
		// TODO Auto-generated method stub
		for(Variable useVar:useVariables){
			if(useVar.getValue().getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
				return true;
			}
		}
		return false;
	}
	public JobVariable getJob(Variable vi,CFGNode cfgNode){
		SootMethod sm = cfgNode.getMethod();
		SootClass sc = sm.getDeclaringClass();
		for(JobVariable jobvar: jobtoHub.keySet()){
			if(jobvar.getSootClass().equals(sc)&&
					jobvar.getSootMethod().equals(sm)&&
					jobvar.getVariable().equals(vi))
				return jobvar;
		}
		return null;
		
		
	}
	
	
	
}
