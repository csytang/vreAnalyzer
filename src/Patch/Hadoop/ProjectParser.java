package Patch.Hadoop;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Patch.Hadoop.Job.JobHub;
import Patch.Hadoop.Job.JobUnderstand;
import Patch.Hadoop.Job.JobVariable;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JNewExpr;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.Context.Context;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.PointsTo.PointsToGraph;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.UI.SourceClassBinding;
import vreAnalyzer.Util.Util;
	
public class ProjectParser {
	
	public static ProjectParser instance = null;
	private SootMethod sootmethod = null;
	private CFGDefUse cfggraph;
	private boolean verbose = false;
	private Context allcontext = null;
	private static int numjobs = 0;
	
	private static int numShadow = 0;
	private SootClass libMapper;
	private SootClass libReducer;
	private SootClass cs;
	private CFGNode exitNode;
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
	public void annotateallJobs(){
		for(Map.Entry<JobVariable, JobHub>entry:jobtoHub.entrySet()){
			JobVariable job = entry.getKey();
			File sourceFile = SourceClassBinding.getSourceFileFromClassName(job.getSootClass().toString());
			//bug process
			if(sourceFile==null){
				System.err.println("Cannot fine the job class:\t"+job.getSootClass().toString());
				continue;
			}
			String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
			htmlfileName+=".html";
			File htmlFile = new File(htmlfileName);
			Patch.Hadoop.Job.JobAnnotate jobannot = new Patch.Hadoop.Job.JobAnnotate(job,htmlFile);
		}
	}
	public void Annotate(){
		// Following will only be allowed if it is started from GUI and source is binded
		// Annotated all jobs with selected color;
		if(vreAnalyzerCommandLine.isSourceBinding()){
			ProjectParser.inst().annotateallJobs();
		}
	}
	public void runProjectParser(){
		libMapper = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Mapper");
		libReducer = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Reducer");
		jobtoHub = new HashMap<JobVariable,JobHub>();
		indextoJob = new HashMap<Integer,JobVariable>();// start from 1 insteadof 0
		// run all the application methods
		for(SootMethod sm:ProgramFlowBuilder.inst().getAppConcreteMethods()){
			
			sootmethod = sm;
			cs = sm.getDeclaringClass();
			cfggraph = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(sm);
			exitNode = cfggraph.EXIT;
			currContexts = PointsToAnalysis.inst().getContexts(sm);
			allcontext = currContexts.get(0);	
			Parse();
		}
		System.out.println("# Jobs:\t"+numjobs);
		System.out.println("# Mappers:\t"+JobUnderstand.getNumberofMapper());
		System.out.println("# Combiner:\t"+JobUnderstand.getNumberofCombiner());
		System.out.println("# Reducer:\t"+JobUnderstand.getNumberofReducer());
		System.out.println("# Shadow:\t"+numShadow);
		// annotated job and binding information
		Annotate();
		
	}
	
	public void Parse(){
		// 1. Get the CFG
		int index = 0;
		List<CFGNode> allnodes = cfggraph.getNodes();
		// 2.0 Find mapper and reducer in lib class set
		
				
		// 2. Find all Jobs and associated with mapper and reducer classes
		for(int i = 0;i < allnodes.size();i++){
			NodeDefUses cfgNode = (NodeDefUses) allnodes.get(i);
			if(!cfgNode.isSpecial()){
				Stmt stmt = cfgNode.getStmt();
				
				// Not one assign value from this, parameter and caughtexception
				List<Variable>definedVariables = cfgNode.getDefinedVars();
			
				
				// If setting is from identitystmt, then 
				if(stmt instanceof IdentityStmt){
					continue;
				}
				
				// define of job
				// 1. update all successors of this node 
				if(!definedVariables.isEmpty()){
					for(Variable defvar:definedVariables){	
							// If it is a define of job
						if(defvar.getValue().getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
							   	//number of jobs counter
							   	//created by use standard API
							
							if(stmt instanceof AssignStmt){
								// created by use customized functions
								AssignStmt assign = (AssignStmt)stmt;
								Value left = assign.getLeftOp();//defined variable
								Value right = assign.getRightOp(); //used variable
								PointsToGraph p2g = (PointsToGraph)allcontext.getValueAfter(cfgNode);
								HashMap<Local,Set<AnyNewExpr>> roots = p2g.getRoots();
								if(right instanceof Local){
									Local loright = (Local)right;
									if(roots.get(loright)==null||
											roots.get(loright).isEmpty()){
										Type parameterType = defvar.getValue().getType();
										assert((parameterType instanceof RefLikeType));					
										RefType parameter = (RefType)parameterType;
										JNewExpr argsExpr = new JNewExpr(parameter);
										if(defvar.isLocal())
										{
											p2g.assignNew((Local) defvar.getValue(), argsExpr);
											Util.updateSucceedP2G((Local)defvar.getValue(), argsExpr, cfgNode, cfggraph, allcontext);
											p2g.assignNew(loright, argsExpr);
											Util.updateSucceedP2G(loright, argsExpr, cfgNode, cfggraph, allcontext);
											
										}
									}else{
										if(defvar.isLocal())
										{
											for(AnyNewExpr argsExpr:roots.get(loright)){
												p2g.assignNew((Local) defvar.getValue(), argsExpr);
												Util.updateSucceedP2G((Local) defvar.getValue(), argsExpr, cfgNode, cfggraph, allcontext);
											}
											
										}
									}
								}else{
									Type parameterType = defvar.getValue().getType();
									assert((parameterType instanceof RefLikeType));					
									RefType parameter = (RefType)parameterType;
									JNewExpr argsExpr = new JNewExpr(parameter);
									if(defvar.isLocal())
									{
										p2g.assignNew((Local) defvar.getValue(), argsExpr);
										Util.updateSucceedP2G((Local) defvar.getValue(), argsExpr, cfgNode, cfggraph, allcontext);
										
									}
								}
								if(!defvar.getValue().toString().startsWith("$") &&
										!defvar.getValue().toString().startsWith("temp$")){
									
									defineJob(defvar,cfgNode,stmt);
								}
								else{
									if(verbose)
										System.out.println("Create a shadow job:\t"+defvar.getValue().toString());
								}
							}else if(stmt instanceof DefinitionStmt){
								// created by use customized functions
								DefinitionStmt defstmt = (DefinitionStmt)stmt;
								Value left = defstmt.getLeftOp();//defined variable
								Value right = defstmt.getRightOp(); //used variable
								PointsToGraph p2g = (PointsToGraph)allcontext.getValueAfter(cfgNode);
								HashMap<Local,Set<AnyNewExpr>> roots = p2g.getRoots();
								if(right instanceof Local){
									Local loright = (Local)right;
									if(roots.get(loright)==null||
											roots.get(loright).isEmpty()){
										Type parameterType = defvar.getValue().getType();
										assert((parameterType instanceof RefLikeType));					
										RefType parameter = (RefType)parameterType;
										JNewExpr argsExpr = new JNewExpr(parameter);
										if(defvar.isLocal())
										{
											p2g.assignNew(loright, argsExpr);
											Util.updateSucceedP2G(loright, argsExpr, cfgNode, cfggraph, allcontext);
											p2g.assignNew((Local) defvar.getValue(), argsExpr);
											Util.updateSucceedP2G((Local) defvar.getValue(), argsExpr, cfgNode, cfggraph, allcontext);
											
										}
									}else{
										if(defvar.isLocal())
										{
											for(AnyNewExpr argsExpr:roots.get(loright)){
												p2g.assignNew((Local) defvar.getValue(), argsExpr);
												Util.updateSucceedP2G((Local) defvar.getValue(), argsExpr, cfgNode, cfggraph, allcontext);
											}
											
										}
									}
								}else{
									Type parameterType = defvar.getValue().getType();
									assert((parameterType instanceof RefLikeType));					
									RefType parameter = (RefType)parameterType;
									JNewExpr argsExpr = new JNewExpr(parameter);
									if(defvar.isLocal())
									{
										p2g.assignNew((Local) defvar.getValue(), argsExpr);
										Util.updateSucceedP2G((Local) defvar.getValue(), argsExpr, cfgNode, cfggraph, allcontext);
										
									}
								}
								if(!defvar.getValue().toString().startsWith("$") &&
										!defvar.getValue().toString().startsWith("temp$"))
									defineJob(defvar,cfgNode,stmt);
								else{
									if(verbose)
										System.out.println("Create a shadow job:\t"+defvar.getValue().toString());
								}
							}else{
								if(verbose)
									System.err.println("Should create a job:\t"+defvar.getValue().toString());
							}
							
							
						}
			
					}
				}
			}
		}
		for(int i = 0;i < allnodes.size();i++){
			NodeDefUses cfgNode = (NodeDefUses) allnodes.get(i);
			if(!cfgNode.isSpecial()){
				Stmt stmt = cfgNode.getStmt();
				
				// Not one assign value from this, parameter and caughtexception
				List<Variable>useVariables = cfgNode.getUsedVars();
			
				
				// If setting is from identitystmt, then 
				if(stmt instanceof IdentityStmt){
					continue;
				}		
				// use of the job
				if(!useVariables.isEmpty()){
					for(Variable usevar:useVariables){	
						// If it is a define of job
						if(usevar.getValue().getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
							if(stmt.containsInvokeExpr()){
								JobHub jobinstance = getjobHub(usevar);
								// get the invoke expression
								if(jobinstance==null)
									continue;
								InvokeExpr invokeExpr = stmt.getInvokeExpr();
								switch(invokeExpr.getMethod().getName()){
								case "setMapperClass":{
									// 1. setMapperClass									
									JobUnderstand.process_SetMapperClass(jobinstance, invokeExpr, cfgNode,cfggraph,libMapper);
									break;
								}
								case "setCombinerClass":{
									// 2. setCombinerClass
									JobUnderstand.process_SetCombinerClass(jobinstance, invokeExpr, cfgNode,cfggraph,libReducer);
									break;
								}
								case "setReducerClass":{
									// 3. setReducerClass
									JobUnderstand.process_SetReducerClass(jobinstance, invokeExpr, cfgNode,cfggraph,libReducer);
									break;
								}
								case "setOutputKeyClass":{
									// 4. setOutputKeyClass
									break;
								}
								case "setOutputValueClass":{
									// 5. setOutputValueClass
									break;
								}
								case "addArchiveToClassPath":{
									// 6. addArchiveToClassPath
									break;
								}
								default:{
									System.out.println("A stmt:\t"+stmt);
								}
								}
								
							}
						}
					}
				
				}
				
				
				
				
			}
		}
	}
	public void defineJob(Variable defvar,NodeDefUses cfgNode,Stmt stmt){
		JobVariable jvb = new JobVariable(defvar,cfgNode);
		if(!containjobvar(jvb)){
			Value defValue = defvar.getValue();
			System.out.println("Add a job\t"+jvb+"\t @stmt"+stmt+"\t @method: "+sootmethod);
			numjobs++;
			JobHub jhb = new JobHub(jvb);
			jobtoHub.put(jvb, jhb);
			indextoJob.put(numjobs, jvb);
		}
	}
	public JobHub getjobHub(Variable jobvar){
		// see the pointer part, if the associated $(partial use the temperate value)
		@SuppressWarnings("unchecked")
		PointsToGraph p2g = (PointsToGraph)allcontext.getValueBefore(exitNode);
		HashMap<Local,Set<AnyNewExpr>> roots = p2g.getRoots();
		Local joblocal = (Local)jobvar.getValue();
		Set<AnyNewExpr>jobsites  = roots.get(joblocal);
		for(JobVariable curr:jobtoHub.keySet()){
			if(curr.getVariable().equals(jobvar)){
				return jobtoHub.get(curr);
			}
			else if(jobvar.isLocal() && curr.getVariable().isLocal()){
				Local currlocal = (Local)curr.getVariable().getValue();
				Set<AnyNewExpr>currlocalsites = roots.get(currlocal);
				if(currlocalsites==null);
				else if(!currlocalsites.isEmpty()&&!jobsites.isEmpty()){
					if(jobsites.containsAll(currlocalsites)||
							currlocalsites.containsAll(jobsites)){
						System.out.println("Find a shadow job<(shadow) "+jobvar.getValue().toString()+" --> (real)"+curr.getVariable().getValue().toString()+" >");
						numShadow++;
						return jobtoHub.get(curr);
					}
				}
			}
		}
		return null;
	}
	public boolean containjobvar(JobVariable jvb){
		for(JobVariable curr:jobtoHub.keySet()){
			if(curr.equals(jvb))
				return true;
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
	public Map<JobVariable,JobHub> getjobtoHub(){
		return jobtoHub;
	}
	
	
}
