package Patch.Hadoop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Patch.Hadoop.Pipelines.ConfigurePipelines;
import Patch.Hadoop.Pipelines.JobExternalConfigurePipelines;
import Patch.Hadoop.Pipelines.MapperPipelines;
import Patch.Hadoop.Pipelines.ReducerPipelines;
import Patch.Hadoop.Pipelines.SingleConfigurePipeline;
import Patch.Hadoop.Pipelines.SingleJobExternalConfigurePipeline;
import Patch.Hadoop.Pipelines.SingleMapperPipeline;
import Patch.Hadoop.Pipelines.SingleReducerPipeline;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
	
public class HubClassParser {
	
	private SootClass entry = null;
	private SootMethod mainMethod = null;
	public static HubClassParser instance = null;
	private Body methodBody = null;
	private CFGDefUse cfggraph;
	private boolean verbose = true;
	/* 
	 * Store all jobs that defined in the main method,
	 * including locals and fields 
	 */
	
	private Map<Value,JobHub>jobtoHub = null;
	private Map<Integer,Value>indextoJob = null;
	
	public static HubClassParser inst(){
		return instance;
	}
	public static HubClassParser inst(SootClass entry){
		if(instance==null)
			instance = new HubClassParser(entry);
		return instance;
	}
	public JobHub getjobHub(Value job){
		return jobtoHub.get(job);
	}
	public HubClassParser(SootClass entry){
		this.entry = entry;
		String mainSubsig = "void main(java.lang.String[])";
		mainMethod = this.entry.getMethod(mainSubsig);
		jobtoHub = new HashMap<Value,JobHub>();
		indextoJob = new HashMap<Integer,Value>();
		// Get the pointsto graph of this method
		cfggraph = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(mainMethod);
		methodBody = mainMethod.retrieveActiveBody();
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
				Stmt stmt = (Stmt) cfgNode.getStmt();
				if (!(stmt instanceof IdentityStmt)) {
					for(ValueBox vbox:stmt.getUseAndDefBoxes()){
						Value key = vbox.getValue();
						
						// Here, we get rid of jimple temporary local
						if(key.getType().toString().equals("org.apache.hadoop.mapreduce.Job") &&
								!key.toString().startsWith("$") &&
								(methodBody.getLocals().contains(key)||
										entry.getFields().contains(key))){
							if(!jobtoHub.containsKey(key)){
								
								jobtoHub.put(key, new JobHub());
								indextoJob.put(index,key);
								index++;
							}
							
							// Find embedded mapper setting and reducer setting
							if(stmt.containsInvokeExpr()){
								
								InvokeExpr invokeExpr = stmt.getInvokeExpr();
								
								// Set a Mapper;
								if(invokeExpr.getMethod().getName().equals("setMapperClass")){ 
									
									String mapper = invokeExpr.getArg(0).toString();
									mapper = mapper.substring(mapper.indexOf("\"")+1, mapper.lastIndexOf("\""));
									mapper = mapper.replaceAll("/", ".");
									// Get lib Class for this setting
									SootClass appMapper = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(mapper, libMapper);			
									
									jobtoHub.get(key).setMapperClass(appMapper);
									MapperPipelines.inst().createNewSingleMap(key,appMapper);
								}
								
								// Set a Reducer;
								else if(invokeExpr.getMethod().getName().equals("setReducerClass")){
									
									String reducer = invokeExpr.getArg(0).toString();									
									reducer = reducer.substring(reducer.indexOf("\"")+1, reducer.lastIndexOf("\""));
									reducer = reducer.replaceAll("/", ".");
									// Get lib Class for this setting
									SootClass appReducer = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(reducer, libReducer);
																	
									jobtoHub.get(key).setReducerClass(appReducer);
									ReducerPipelines.inst().createNewSingleReducer(key,appReducer);
								}
								
								// set a combiner
								else if(invokeExpr.getMethod().getName().equals("setCombinerClass")){
									String combiner = invokeExpr.getArg(0).toString();									
									combiner = combiner.substring(combiner.indexOf("\"")+1, combiner.lastIndexOf("\""));
									combiner = combiner.replaceAll("/", ".");
									// Get lib Class for this setting
									// Get lib Class for this setting
									SootClass appCombiner = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(combiner, libReducer);
																	
									jobtoHub.get(key).setCombinerClass(appCombiner);
									
									
								}
								
								// skip these settings for now
								else if(invokeExpr.getMethod().getName().equals("setJarByClass")||
										invokeExpr.getMethod().getName().equals("waitForCompletion")||
										invokeExpr.getMethod().getName().equals("setOutputKeyClass")||
										invokeExpr.getMethod().getName().equals("setOutputValueClass")){
									continue;
								}
								
								// Set the input path 
								else if(invokeExpr.getMethod().getName().equals("addInputPath")){
									// there are two argus: 
									// (1) the job
									// (2) the path
									Value input = (Value) invokeExpr.getArg(1);
									jobtoHub.get(key).setInputPath(input);
								}
								
								// Set the output path
								else if(invokeExpr.getMethod().getName().equals("setOutputPath")){
									// there are two argus: 
									// (1) the job
									// (2) the path
									Value output = (Value) invokeExpr.getArg(1);
									jobtoHub.get(key).setOutPath(output);
								}// Other settings of this job
								else{
									if(JobExternalConfigurePipelines.inst().getEnConfigurePipeline(key)==null)
										JobExternalConfigurePipelines.inst().createNewJobExternalConfigurePipeline(key, mainMethod, cfgNode);
									else
										JobExternalConfigurePipelines.inst().getEnConfigurePipeline(key).addSettingNode(cfgNode);
								}
							}
							
							
						}
					}
				}
			}
		}
		
		
		
		
		
			
		
		
		
	}
	public void showCommons(){
		// 4. Start common assets analysis on Mappers and Reducers
				for(int i = 0;i < jobtoHub.keySet().size();i++){
					
					SingleMapperPipeline srcMapper = MapperPipelines.inst().getMapperPipeline(indextoJob.get(i));
					SingleReducerPipeline srcReducer = ReducerPipelines.inst().getReducerPipeline(indextoJob.get(i));
					SingleConfigurePipeline srcConfigure = ConfigurePipelines.inst().getConfigurePipeline(indextoJob.get(i));
					SingleJobExternalConfigurePipeline srcJobexConfigure = JobExternalConfigurePipelines.inst().getEnConfigurePipeline(indextoJob.get(i));
					
					for(int j = i+1; j < jobtoHub.keySet().size();j++){
						
						SingleMapperPipeline otherMapper = MapperPipelines.inst().getMapperPipeline(indextoJob.get(j));
						SingleReducerPipeline otherReducer = ReducerPipelines.inst().getReducerPipeline(indextoJob.get(j));
						SingleConfigurePipeline otherConfigure = ConfigurePipelines.inst().getConfigurePipeline(indextoJob.get(j));
						SingleJobExternalConfigurePipeline otherJobexConfigure = JobExternalConfigurePipelines.inst().getEnConfigurePipeline(indextoJob.get(j));
						
						
						// Get the common asset of other configuration
						Map<NodeDefUses,NodeDefUses> commonEx = srcJobexConfigure.getCommonAsset(otherJobexConfigure);
						if(verbose){
							if(commonEx.size()==0)
								System.out.println("No common parts in extra-configue");
							else{
								System.out.println("The common parts in extra configuration are:");
								for(Map.Entry<NodeDefUses, NodeDefUses>entry : commonEx.entrySet()){
									System.out.println("Node in src:\t|"+entry.getKey().toString()+"\n Node in other:\t|"+entry.getValue().toString());
								}
							}
						}
						
						// Get the common asset of configuration
						CFGNode[] configcommons = srcConfigure.getCommonAsset(otherConfigure);
						if(verbose){
							SootClass configure = jobtoHub.get(indextoJob.get(i)).getConfigureClass();
							SootClass otherconfigure = jobtoHub.get(indextoJob.get(j)).getConfigureClass();
							System.out.println("StartIndex and EndIndex Configuration in \t"+configure.getName()+"\t is");
							System.out.println("Start:\t"+configcommons[0].toString()+"\t|\tEnd:\t"+configcommons[1].toString());
							System.out.println("StartIndex and EndIndex Configuration in \t"+otherconfigure.getName()+"\t is");
							System.out.println("Start:\t"+configcommons[2].toString()+"\t|\tEnd:\t"+configcommons[3].toString());
						}
						
						
						// Get the common asset of mapper
						CFGNode[] mappercommons = srcMapper.getCommonAsset(otherMapper);
						if(verbose){
							SootClass mapper = jobtoHub.get(indextoJob.get(i)).getMapperClass();
							SootClass othermapper = jobtoHub.get(indextoJob.get(j)).getMapperClass();
							System.out.println("StartIndex and EndIndex Map in \t"+mapper.getName()+"\t is");
							System.out.println("Start:\t"+mappercommons[0].toString()+"\t|\tEnd:\t"+mappercommons[1].toString());
							System.out.println("StartIndex and EndIndex Map in \t"+othermapper.getName()+"\t is");
							System.out.println("Start:\t"+mappercommons[2].toString()+"\t|\tEnd:\t"+mappercommons[3].toString());
						}
						
						
						
						// Get the common asset of reduce
						CFGNode[] reducercommons = srcReducer.getCommonAsset(otherReducer);
						if(verbose){
							SootClass reducer = jobtoHub.get(indextoJob.get(i)).getReducerClass();
							SootClass otherreducer = jobtoHub.get(indextoJob.get(j)).getReducerClass();
							System.out.println("StartIndex and EndIndex Map in \t"+reducer.getName()+"\t is");
							System.out.println("Start:\t"+reducercommons[0].toString()+"\t|\tEnd:\t"+reducercommons[1].toString());
							System.out.println("StartIndex and EndIndex Map in \t"+otherreducer.getName()+"\t is");
							System.out.println("Start:\t"+reducercommons[2].toString()+"\t|\tEnd:\t"+reducercommons[3].toString());
						}
					
						
						
					}
				}
	}
	
	
}
