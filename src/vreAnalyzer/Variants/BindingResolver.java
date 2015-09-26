package vreAnalyzer.Variants;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import java.util.*;

public class BindingResolver {

	public static BindingResolver instance = null;
	private Map<SootMethod,List<Args>>methodToArgsList = null;
    private ArrayList<Variant>variants = new ArrayList<Variant>();
    private boolean verbose = true;
    private List<SootMethod> allAppMethod = null;
    public BindingResolver(){
    	methodToArgsList = new HashMap<SootMethod,List<Args>>();
    	allAppMethod = new LinkedList<SootMethod>();
    }
	public static BindingResolver inst(){
		if(instance==null)
			instance = new BindingResolver();
		return instance;
	}
	public ArrayList<Variant>getVariants(){
		return variants;
	}
	
	public void parse(){
		// 1. for all methods first set the call site binding
		allAppMethod.addAll(ProgramFlowBuilder.inst().getAppConcreteMethods());
		for(SootMethod method:allAppMethod){
			MethodTag mTag = (MethodTag) method.getTag(MethodTag.TAG_NAME);
			List<CallSite> callsites = mTag.getAllCallSites();
			for(CallSite site:callsites){
				// 1.1 get call cfg node
				CFGNode srcCallCFGNode = site.getCallCFGNode();
				Stmt srcStmt = srcCallCFGNode.getStmt();
				InvokeExpr invokeExpr = srcStmt.getInvokeExpr();
				List<Value>args = invokeExpr.getArgs();
				if(args.isEmpty())
					continue;
				List<SootMethod>appcallees = site.getAppCallees();
				// DEBUG
				String argsString = "";
				for(Value ar:args){
					argsString+=ar.toString();
					argsString+=",";
				}
				if(!args.isEmpty()){
					argsString = argsString.substring(0,argsString.length()-1);
				}
				// FINISH
				for(SootMethod callee:appcallees){
					if(verbose){
						System.out.println("Add a method and args bind: callee method["+callee.getName()+"] with input parameters "
								+ "["+argsString+"] in caller method["+method.getName()+"]");
					}
					Args ar = new Args(method,callee,args);
					if(methodToArgsList.containsKey(callee)){
						methodToArgsList.get(callee).add(ar);
					}else{						
						List<Args>argsList = new LinkedList<Args>();
						argsList.add(ar);
						methodToArgsList.put(callee, argsList);
					}
				}
			}
		}
		// 2. if there is a call binding, does not use its own arguments value use parameter instead
		/*
		 * We categorize the variable into following groups:
		 * 1. Fields -- from class
		 * 2. Arguments -- from input set
		 * 3. Locals
		 * STEP
		 * STEP 1: get the cfg 
		 * STEP 2: for a CFGNode, get the use of this cfgNode
		 * STEP 3: classify this use
		 * is this use a field?
		 * is this use a local? --> binding to argument/ binding to RefLike/ others
		 * STEP 4: 
		 * 
		 */
		boolean isParaAssignStmt = false;
		for(SootMethod method:allAppMethod){
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			Body body = method.retrieveActiveBody();
			List<Local>argLists = body.getParameterLocals();//locals assigned with parameters
			List<CFGNode>nodes = cfg.getNodes();
			if(methodToArgsList.containsKey(method)){
				// 2.1 The method is invoked by other method use callers' parameters
				for(CFGNode node:nodes){
					NodeDefUses defusenode = (NodeDefUses)node;
					Stmt stmt = defusenode.getStmt();
					if(stmt instanceof IdentityStmt &&
							!(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
						isParaAssignStmt = true;
					}else{
						isParaAssignStmt = false;
					}
					List<Variable> useVars = defusenode.getUsedVars();
					int argIndex = -1;
					Value argu = null;
					if(isParaAssignStmt){
						IdentityStmt idstmt =  (IdentityStmt)stmt;
						// 2.1.1 arguments
						argu = idstmt.getLeftOp();
						Local localarg = (Local)argu;
						argIndex = argLists.indexOf(localarg);
					}
					for(Variable vi:useVars){
						Value value = vi.getValue();
						if(vi.isFieldRef()){
							
						}else if(vi.isArrayRef()){
							
						}else if(vi.isConstant()){
							
						}else if(vi.isLocal()){
							
						}
						
					}
					
				}
				
			}else{
				// 2.2 Use this method's own arguments directly
				for(CFGNode node:nodes){
					NodeDefUses defusenode = (NodeDefUses)node;
					List<Variable> useVars = defusenode.getUsedVars();
					for(Variable vi:useVars){
						Value value = vi.getValue();
						if(vi.isFieldRef()){
							
						}else if(vi.isArrayRef()){
							
						}else if(vi.isConstant()){
							
						}else if(vi.isLocal()){
							
						}
					}
					
					
				}
			}
		
		}
		
		
		
	}
	
	/**
	 * 1. current solution, first we all annotated all the files(testing)
	 * 2. seperate stmts list into combination of code blocks
	 * 
	 * Rewrite with annotated color
	 */
	/*
	public void annotate() {
		if (vreAnalyzerCommandLine.isSourceBinding() &&
				vreAnalyzerCommandLine.isStartFromGUI())
		{
			Map<SootClass, List<Stmt>> classVariantStmtMap = new HashMap<SootClass, List<Stmt>>();
			for (Variant variant : variants) {
				List<Stmt> bindingStmts = variant.getBindingStmts();
				Color variantColor = VariantColorMap.inst().getColorforVariant(variant);
				classVariantStmtMap.clear();
				for (Stmt stmt : bindingStmts) {
					StmtTag stmtTag = (StmtTag) stmt.getTag(StmtTag.TAG_NAME);
					SootClass cls = stmtTag.getSootMethod().getDeclaringClass();
					if (classVariantStmtMap.containsKey(cls)) {
						classVariantStmtMap.get(cls).add(stmt);
					} else {
						List<Stmt> stmts = new LinkedList<Stmt>();
						stmts.add(stmt);
						classVariantStmtMap.put(cls, stmts);
					}
				}
				String variantId = "variant:"+variants.indexOf(variant);
				for (Map.Entry<SootClass, List<Stmt>> entry : classVariantStmtMap.entrySet()) {
					SootClass cls = entry.getKey();
					List<Stmt> stmts = entry.getValue();
					File sourceFile = SourceClassBinding.getSourceFileFromClassName(cls.toString());
					String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length() - ".java".length());
					int startIndex = htmlfileName.lastIndexOf("/");
					startIndex += 1;
					String realName = htmlfileName.substring(startIndex);
					realName = "variant_" + realName;
					htmlfileName = htmlfileName.substring(0, startIndex) + realName;
					htmlfileName += ".html";
					File htmlFile = new File(htmlfileName);
					new VariantAnnotate(variant,variantId,stmts, htmlFile,variantColor);
				}
			}

		}
	}
	*/
}
