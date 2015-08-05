package Patch.Hadoop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Patch.Hadoop.Pipelines.MapperPipelines;
import Patch.Hadoop.Pipelines.ReducerPipelines;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
	
public class HubClassParser {
	
	private SootClass entry = null;
	private SootMethod mainMethod = null;
	public static HubClassParser instance = null;
	
	
	/* 
	 * Store all jobs that defined in the main method,
	 * including locals and fields 
	 */
	
	private Map<Object,MapReduceHub>jobtoHub = null;
	
	public static HubClassParser inst(){
		return instance;
	}
	public static HubClassParser inst(SootClass entry){
		if(instance==null)
			instance = new HubClassParser(entry);
		return instance;
	}
	
	public HubClassParser(SootClass entry){
		this.entry = entry;
		String mainSubsig = "void main(java.lang.String[])";
		mainMethod = this.entry.getMethod(mainSubsig);
		jobtoHub = new HashMap<Object,MapReduceHub>();
	}
	public void Parse(){
		// 1. Get the CFG
		CFGDefUse cfggraph = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(mainMethod);
		List<CFGNode> allnodes = cfggraph.getNodes();
		
		// 2.0 Find mapper and reducer in lib class set
		SootClass libMapper = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Mapper");
		SootClass libReducer = ProgramFlowBuilder.inst().findLibClassByName("org.apache.hadoop.mapreduce.Reducer");
				
				
		// 2. Find all Jobs and associated with mapper and reducer classes
		for(int i = 0;i < allnodes.size();i++){
			CFGNode cfgNode = allnodes.get(i);
			if(!cfgNode.isSpecial()){
				Stmt stmt = (Stmt) cfgNode.getStmt();
				if (!(stmt instanceof IdentityStmt)) {
					for(ValueBox vbox:stmt.getUseAndDefBoxes()){
						Value key = vbox.getValue();
						
						// Here, we get rid of jimple temporary local
						if(key.getType().toString().equals("org.apache.hadoop.mapreduce.Job") &&
								!key.toString().startsWith("$")){
							if(!jobtoHub.containsKey(key))
								jobtoHub.put(key, new MapReduceHub());
							
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
									MapperPipelines.inst().createNewSingleMap(appMapper);
								}
								
								// Set a Reducer;
								if(invokeExpr.getMethod().getName().equals("setReducerClass")){
									
									String reducer = invokeExpr.getArg(0).toString();									
									reducer = reducer.substring(reducer.indexOf("\"")+1, reducer.lastIndexOf("\""));
									reducer = reducer.replaceAll("/", ".");
									// Get lib Class for this setting
									SootClass appReducer = ProgramFlowBuilder.inst().findAppClassByNameAndSuper(reducer, libReducer);
																	
									jobtoHub.get(key).setReducerClass(appReducer);
									ReducerPipelines.inst().createNewSingleReducer(appReducer);
								}
								
							}
						}
					}
				}
			}
		}
		
		// 3. Start common assets analysis on Mappers and Reducers
		MapperPipelines.inst().findCommonAssetsandReset();
		ReducerPipelines.inst().findCommonAssetsandReset();
		
		
	}
	
	
	
}
