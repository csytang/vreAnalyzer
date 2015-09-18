package Patch.Hadoop;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import Patch.Hadoop.Job.ColorMap;
import Patch.Hadoop.Job.JobAnnotate;
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
	private Context allcontext = null;
	private static int numjobs = 0;
	private static int jobId = 0;
	private static int numShadow = 0;
	private SootClass cs;
	private CFGNode exitNode;
	private List<Context<SootMethod,CFGNode,PointsToGraph>> currContexts;
	private List<File>allannotatedFiles = new LinkedList<File>();
	private MethodBlock mblock = null;
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
	public void addLegendListener(){
		final JTable table = MainFrame.inst().getJobColorMapTable();
		table.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent me){
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if(me.getClickCount()==2){
					int jobId = JobAnnotate.rowToJobId.get(row);
					JobVariable job = indextoJob.get(jobId);
					File sourceFile = job.getSourceFile();
					String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
					htmlfileName+=".html";
					File htmlFile = new File(htmlfileName);
					JTextPane txtrSource = MainFrame.getSrcTextPane();
					txtrSource.setContentType("text/html");
					List<String> content;
					try {
						content = Files.readAllLines(htmlFile.toPath(),Charset.defaultCharset());
						String allString = "";
						for(String subcontent:content){
							allString+=subcontent;
							allString+="\n";
						}
						
						txtrSource.setText("");
						txtrSource.setText(allString);
						txtrSource.setCaretPosition(0);
					} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
					}
				}
			}
		});
	}
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
		
		JTree mainFrameTree = MainFrame.inst().getTree();
		mainFrameTree.setCellRenderer(new TreeCellRender(allannotatedFiles));
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
			
		}
		
	}
	public static Color hex2Rgb(String colorStr) {
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
			cs = sm.getDeclaringClass();
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
		// 3. add the annotated color to legend
		JTable jobColorMapTable = MainFrame.inst().getJobColorMapTable();
		DefaultTableModel model = (DefaultTableModel)jobColorMapTable.getModel();
		Map<String,Set<JobVariable>>hexColorToJob = ColorMap.inst().getJobColorMap();
		for(Map.Entry<String, Set<JobVariable>>entry:hexColorToJob.entrySet()){
			Color color = hex2Rgb(entry.getKey());
			String jobsString = "[";
			for(JobVariable jb:entry.getValue()){
				jobsString+=jb.getJobId();
				jobsString+=":";
			}
			jobsString = jobsString.substring(0, jobsString.length()-1);
			jobsString+="]";
			model.addRow(new Object[]{jobsString,color});
		}
					
		addLegendListener();
		
		
	}
	
	public void Parse(){
		// 1. Get the CFG
		int index = 0;
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
			for(SimpleBlock block:blocklist){
				nodes.addAll(block.getCFGNodes());
			}
			SimpleBlock cb = SimpleBlock.tryToCreate(nodes, sootmethod,mblock.getBlockId());
			//add to table
			BlockGenerator.inst().addNewBlockToPool(cb,false);
			jobtoHub.get(job).addUse(sootmethod.getDeclaringClass(), cb);
		}
		
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
