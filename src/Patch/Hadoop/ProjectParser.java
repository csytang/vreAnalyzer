package Patch.Hadoop;
import java.awt.Color;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import Patch.Hadoop.Job.ColorMap;
import Patch.Hadoop.Job.JobHub;
import Patch.Hadoop.Job.JobMethodBind;
import Patch.Hadoop.Job.JobUseAnnotate;
import Patch.Hadoop.Job.JobVariable;
import Patch.Hadoop.ReuseAssets.ReuseAssetWriteToTable;
import Patch.Hadoop.Statistic.JobDataCollector;
import Patch.Hadoop.Statistic.JobDataWriteToTable;
import Patch.Hadoop.Tag.BlockJobTag;
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
import vreAnalyzer.Blocks.BlockGenerator;
import vreAnalyzer.Blocks.BlockType;
import vreAnalyzer.Blocks.ClassBlock;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Blocks.MethodBlock;
import vreAnalyzer.Blocks.SimpleBlock;
import vreAnalyzer.Context.Context;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.PointsTo.PointsToGraph;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.UI.SourceClassBinding;
import vreAnalyzer.UI.TreeCellRender;
import vreAnalyzer.Util.Util;
	
public class ProjectParser {
	
	public static ProjectParser instance = null;
	private SootMethod sootmethod = null;
	private CFGDefUse cfggraph;
	private boolean verbose = false;
	@SuppressWarnings("rawtypes")
	private Context allcontext = null;
	private static int numjobs = 0;
	private static int jobId = 0;
	private static int numShadow = 0;
	private CFGNode exitNode;
	private List<Context<SootMethod,CFGNode,PointsToGraph>> currContexts;
	private Set<File>allannotatedFiles = new HashSet<File>();
	private MethodBlock mblock = null;
	public static boolean isfeatureAnnotatedReady = false;
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
	
	private DefaultMutableTreeNode findTreeNode(DefaultMutableTreeNode root, String s) {
	    @SuppressWarnings("unchecked")
	    Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
	    while (e.hasMoreElements()) {
	        DefaultMutableTreeNode node = e.nextElement();
	        if (node.toString().equalsIgnoreCase(s)) {
	            return node;
	        }
	    }
	    return null;
	}
	
	/**
	 * this method will link the jobColor legend to the file,
	 * if user double click the cell in legend, it will show the file in the 
	 * textarea;
	 */
	public void annotateallJobs(){
		for(Map.Entry<JobVariable, JobHub>entry:jobtoHub.entrySet()){
			JobVariable job = entry.getKey();
			
			File sourceFile = job.getSourceFile();
			if(sourceFile==null){
				System.err.println("Cannot fine the job class:\t"+job.getSootClass().toString());
				continue;
			}
			String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
			htmlfileName+=".html";
			File htmlFile = new File(htmlfileName);
			allannotatedFiles.add(htmlFile);
			CodeBlock jobblock = job.getBlock();
			BlockJobTag smkTag;
			// add job marked tag to this statement
			if( (smkTag = (BlockJobTag) jobblock.getTag(BlockJobTag.TAG_NAME))==null){
				smkTag = new BlockJobTag();
				smkTag.addJob(job);
				jobblock.addTag(smkTag);
			}else{
				smkTag.addJob(job);
			}
			
			Patch.Hadoop.Job.JobAnnotate jobannot = new Patch.Hadoop.Job.JobAnnotate(job,htmlFile);
		}
		
		JTree mainFrameTree = MainFrame.inst().getFeatureAnnotatedTree();
		
		TreeCellRender treeRender = (TreeCellRender) mainFrameTree.getCellRenderer();
		if(treeRender!=null){
			treeRender.addCells(allannotatedFiles);
		}else{
			// 加入tree节点渲染
			mainFrameTree.setCellRenderer(new TreeCellRender(allannotatedFiles));
		}
	}
	
	
	public void annotateallJobUses(){
		for(Map.Entry<JobVariable, JobHub>entry:jobtoHub.entrySet()){
			JobVariable job = entry.getKey();
			JobHub jobhub = entry.getValue();
			Map<SootClass,LinkedList<CodeBlock>>jobUsesBlocks = jobhub.getjobUse();
			for(Map.Entry<SootClass, LinkedList<CodeBlock>>useentry:jobUsesBlocks.entrySet()){
				SootClass cls = useentry.getKey();
				File sourceFile = SourceClassBinding.getSourceFileFromClassName(cls.toString());
				String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
				htmlfileName+=".html";
				File htmlFile = new File(htmlfileName);
				allannotatedFiles.add(htmlFile);
				@SuppressWarnings("unused")
				JobUseAnnotate jobuseannot = new JobUseAnnotate(job, useentry.getValue(), htmlFile);
			}
		}
	}
	
	public void annotate(){
		// 1. Following will only be allowed if it is started from GUI and source is binded
		// Annotated all jobs with selected color;
		if(vreAnalyzerCommandLine.isSourceBinding()){
			// 2. Following will annoated use of job
			ProjectParser.inst().annotateallJobs();
			ProjectParser.inst().annotateallJobUses();
			isfeatureAnnotatedReady = true;
		}
		
	}
	
	public static Color hex2Rgb(String colorStr) {
		if(true){
			System.out.println("Color is:"+colorStr);
		}
	    return new Color(
	            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
	            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
	            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
	}
	
	
	public void collectStatisiticData(){
		if(vreAnalyzerCommandLine.isSourceBinding()){
			JobDataCollector.inst().parse(jobtoHub);
			// show data
			JobDataWriteToTable.inst().showData(jobtoHub);
		}
	}
	public void collectCommonAssetData(){
		if(vreAnalyzerCommandLine.isSourceBinding()){
			ReuseAssetWriteToTable.inst().showData(JobDataCollector.inst().getCommonAssets());
		}
	}
	public void runProjectParser(){
		jobtoHub = new HashMap<JobVariable,JobHub>();
		indextoJob = new HashMap<Integer,JobVariable>();// start from 1 insteadof 0
		// run all the application methods
		for(SootMethod sm:ProgramFlowBuilder.inst().getAppConcreteMethods()){
			mblock = MethodBlock.getMethodBlock(sm);
			sootmethod = sm;
			cfggraph = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(sm);
			exitNode = cfggraph.EXIT;
			currContexts = PointsToAnalysis.inst().getContexts(sm);
			allcontext = currContexts.get(0);	
			Parse();
		}
		System.out.println("# Jobs:\t"+numjobs);
		System.out.println("# Shadow:\t"+numShadow);
		// annotated job and binding information
		annotate();
		collectStatisiticData();
		collectCommonAssetData();
		addToLegend();
		
	}
	public void addToLegend(){
		// 3. add the annotated color to legend
		ColorMap.inst().addToLegend();
		ColorMap.inst().addLegendListener();
	}
	public void Parse(){
		// 1. Get the CFG
		
		List<CFGNode> allnodes = cfggraph.getNodes();
		// 2.0 Find mapper and reducer in lib class set
		Map<JobVariable,List<SimpleBlock>>jobToUseBlocks = new HashMap<JobVariable,List<SimpleBlock>>();		
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
					////////////////////////////////////
					for(Variable usevar:useVariables){	
						// If it is a define of job
						if(usevar.getValue().getType().toString().equals("org.apache.hadoop.mapreduce.Job")){
							JobHub jobinstance = getjobHub(usevar);
							if(jobinstance==null)
								continue;
							//SimpleBlock cb = SimpleBlock.tryToCreate(cfgNode, sootmethod,mblock.getBlockId());
							//add to table
							//BlockGenerator.inst().addNewBlockToPool(cb);
							JobVariable job = jobinstance.getJob();
							SimpleBlock cb = SimpleBlock.createTemp(cfgNode, sootmethod, mblock.getBlockId());
							if(jobToUseBlocks.containsKey(job)){
								jobToUseBlocks.get(job).add(cb);
							}else{
								List<SimpleBlock>tempblocklist = new LinkedList<SimpleBlock>();
								tempblocklist.add(cb);
								jobToUseBlocks.put(job, tempblocklist);
							}
							//jobinstance.addUse(sootmethod.getDeclaringClass(),cb);
							
							// contains job -> invoke method
							if(stmt.containsInvokeExpr()){
								// map , reduce add the binding manually
								InvokeExpr invorkEpxr = stmt.getInvokeExpr();
								SootMethod sminvoked = invorkEpxr.getMethod();
								JobMethodBind jmb = new JobMethodBind(job,invorkEpxr,cfggraph,cfgNode);
								List<SootMethod>bindingsm = jmb.getBindingMethod();
								if(bindingsm.isEmpty())
									continue;
								BlockType bindType = jmb.getBindType();
								if(bindType==BlockType.Class){
									List<CFGNode>bindcfgnodess = new LinkedList<CFGNode>();
									SootClass bindsc  = bindingsm.get(0).getDeclaringClass();
									for(SootMethod sm:bindingsm){
										CFG bindcfg = ProgramFlowBuilder.inst().getCFG(sm);
										bindcfgnodess.addAll(bindcfg.getNodes());
									}
									ClassBlock cBlock = ClassBlock.tryToCreate(bindcfgnodess, bindsc);
									BlockGenerator.inst().addNewBlockToPool(cBlock,false);
									jobinstance.addUse(bindsc, cBlock);
								}else if(bindType==BlockType.Method){
									CFG bindcfg = ProgramFlowBuilder.inst().getCFG(bindingsm.get(0));
									List<CFGNode>bindcfgnodess = bindcfg.getNodes();
									SootClass cls = bindingsm.get(0).getDeclaringClass();
									ClassBlock cblock = ClassBlock.getMethodBlock(cls);
									MethodBlock mBlock = MethodBlock.tryToCreate(bindcfgnodess,bindingsm.get(0),cblock.getBlockId());
									BlockGenerator.inst().addNewBlockToPool(mBlock,false);
									jobinstance.addUse(bindingsm.get(0).getDeclaringClass(), mBlock);
								}
							}
						}
						
					}
					//////////////////////////////////////////////////////////////////
				}
			}
		}
		
		/////////////////////////////
		//1. combine temp blocks
		for(Map.Entry<JobVariable,List<SimpleBlock>>entry:jobToUseBlocks.entrySet()){
			JobVariable job = entry.getKey();
			List<SimpleBlock> blocklist = entry.getValue();
			List<CFGNode>nodes = new LinkedList<CFGNode>();
			List<CFGNode>tempnodes = new LinkedList<CFGNode>();
			JobHub jobhub = jobtoHub.get(job);
			for(SimpleBlock block:blocklist){
				nodes.addAll(block.getCFGNodes());
			}
			CFG cfg = ProgramFlowBuilder.inst().getCFG(job.getSootMethod());
			int blocklistsize = nodes.size();
			int[]coderange = new int[blocklistsize];
			for(int i = 0;i < nodes.size();i++){
				coderange[i]=cfg.getIndexId(nodes.get(i));
			}
			quickSort(coderange,0,coderange.length-1);
			int startIndex = 0;
			int endIndex = 0;
			for(int i = 0;i < coderange.length;i++){
				startIndex = i;
				endIndex = startIndex;
				if(i<coderange.length-1){
					while(coderange[i+1]-coderange[i]==1){
						endIndex++;
						i++;
						if(i==coderange.length-1)
							break;
					}
				}
				if(endIndex-startIndex>=1){
					tempnodes.clear();
					for(int j = startIndex;j <= endIndex;j++){
						tempnodes.add(cfg.getNodes().get(coderange[j]));
					}
					SimpleBlock cb = SimpleBlock.tryToCreate(tempnodes, sootmethod,mblock.getBlockId());
					//add to table
					BlockGenerator.inst().addNewBlockToPool(cb,false);
					jobhub.addUse(sootmethod.getDeclaringClass(),cb);			
				}else if(endIndex==startIndex){
					SimpleBlock cb = SimpleBlock.tryToCreate(cfg.getNodes().get(coderange[startIndex]), sootmethod,mblock.getBlockId());
					//add to table
					BlockGenerator.inst().addNewBlockToPool(cb,false);
					jobhub.addUse(sootmethod.getDeclaringClass(), cb);
				}
			}
		}
	}
	
	
	public void quickSort(int arr[],int left,int right){
		int index = partition(arr,left,right);
		if(left< index-1){
			quickSort(arr,left,index-1);
		}
		if(index<right){
			quickSort(arr,index,right);
		}
	}
	
	
	public int partition(int arr[],int left,int right){
		int i = left, j = right;
		int temp;
		int pivot = arr[(left+right)/2];
		while(i <= j){
			while(arr[i]< pivot)
				i++;
			while(arr[j]> pivot)
				j--;
			if(i <= j){
				temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				i++;
				j--;
			}
		}
		return i;
	}

	
	public void defineJob(Variable defvar,NodeDefUses cfgNode,Stmt stmt){
		SimpleBlock jobblock = SimpleBlock.tryToCreate(cfgNode,cfgNode.getMethod(),mblock.getBlockId());
		BlockGenerator.inst().addNewBlockToPool(jobblock,false);
		JobVariable jvb = new JobVariable(defvar,jobblock);
		if(!containjobvar(jvb)){
			Value defValue = defvar.getValue();
			jvb.setJobId(jobId);
			if(verbose)
				System.out.println("Add a job\t"+jvb+"\t @stmt"+stmt+"\t @method: "+sootmethod);
			numjobs++;
			JobHub jhb = new JobHub(jvb);
			jobtoHub.put(jvb, jhb);
			indextoJob.put(jobId, jvb);
			jobId++;
		}
	}
	
	public JobHub getjobHub(JobVariable job){
		return jobtoHub.get(job);
	}
	
	public JobHub getjobHub(Variable jobvar){
		// see the pointer part, if the associated $(partial use the temperate value)
		@SuppressWarnings("unchecked")
		PointsToGraph p2g = (PointsToGraph)allcontext.getValueBefore(exitNode);
		HashMap<Local,Set<AnyNewExpr>> roots = p2g.getRoots();
		// jobvar instaneceof FieldRef
		if(!(jobvar.getValue() instanceof Local))
			return null;
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
						if(verbose)
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
