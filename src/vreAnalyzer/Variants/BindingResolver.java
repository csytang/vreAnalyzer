package vreAnalyzer.Variants;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
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
    private List<SootMethod> callerMethod = null;
    private List<SootMethod> calleeMethod = null;
    private Map<SootMethod,List<Value>>unBindingValueMap = null;
    //private Stack<SootMethod>analysisStack = new Stack<SootMethod>();
    private Map<Args,SootMethod>argsToCalleeMethod = new HashMap<Args,SootMethod>();
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
		Map<Value,List<Value>>localParameterToRemoteArgu = new HashMap<Value,List<Value>>();
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
					// add this args to callee into record
					argsToCalleeMethod.put(ar, callee);
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
		calleeMethod = new LinkedList<SootMethod>();
		calleeMethod.addAll(methodToArgsList.keySet());
		Set<SootMethod>tmp = new HashSet<SootMethod>(allAppMethod);
		tmp.retainAll(calleeMethod);
		callerMethod = new LinkedList<SootMethod>(tmp);
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
		 * unbind method First
		 * 4.1 Arguments/Local points to argument in non-callee method 
		 * 4.2 all assignment/use method invoke, assignment along with unbind value should be set unbind
		 * 
		 */
		// use to decide whether a stmt is parameter assign stmt
		unBindingValueMap = new HashMap<SootMethod,List<Value>>();
		boolean isParaAssignStmt = false;
		for(SootMethod method:callerMethod){
			unBindingValueMap.put(method,new LinkedList<Value>());
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode>nodes = cfg.getNodes();

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
				
				Value argu = null;
				if(isParaAssignStmt){
					if(useVars.contains(argu)){
						unBindingValueMap.get(method).add(argu);
						RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
						if(rbTag!=null){
							rbTag.addBindingValue(argu);
							if(verbose){
								System.out.println("Value:["+argu.toString()+"] for stmt:["+stmt+"]");
							}
						}else{
							rbTag = new RBTag(argu);
							stmt.addTag(rbTag);
							if(verbose){
								System.out.println("Value:["+argu.toString()+"] for stmt:["+stmt+"]");
							}
						}
					}
				}
				if(stmt instanceof AssignStmt){
					AssignStmt assignstmt = (AssignStmt)stmt;
					Value RHSValue = assignstmt.getRightOp();
					if(RHSValue!=null){
						if(unBindingValueMap.get(method).contains(RHSValue)){
							Value LHSValue = assignstmt.getLeftOp();
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							unBindingValueMap.get(method).add(LHSValue);
							if(rbTag!=null){
								rbTag.addBindingValue(LHSValue);
								if(verbose){
									System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
								}
							}else{
								rbTag = new RBTag(LHSValue);
								stmt.addTag(rbTag);
								if(verbose){
									System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
								}
							}
						}
					}
				}else if(stmt instanceof DefinitionStmt){
					DefinitionStmt defstmt = (DefinitionStmt)stmt;
					Value RHSValue = defstmt.getRightOp();
					if(RHSValue!=null){
						if(unBindingValueMap.get(method).contains(RHSValue)){
							Value LHSValue = defstmt.getLeftOp();
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							unBindingValueMap.get(method).add(LHSValue);
							if(rbTag!=null){
								rbTag.addBindingValue(LHSValue);
								if(verbose){
									System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
								}
							}else{
								rbTag = new RBTag(LHSValue);
								stmt.addTag(rbTag);
								if(verbose){
									System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
								}
							}
						}
					}
				}else if(stmt instanceof InvokeStmt){
					InvokeStmt invokestmt = (InvokeStmt)stmt;
					InvokeExpr invokexpr = invokestmt.getInvokeExpr();
					SootMethod callee = invokexpr.getMethod();
					// add this method to callee method analysis stack
					//analysisStack.push(callee);
					List<Args>argulist = methodToArgsList.get(callee);
					Args curr = null;
					for(Args args:argulist){
						if(args.getCallerMethod().equals(method)){
							curr = args;
							break;
						}
					}
					if(curr!=null){
						for(Value vi:curr.getArgs()){
							// this argument is used
							if(unBindingValueMap.get(method).contains(vi)){
								curr.addUnBindArg(vi);
							}
						}
					}
				}
			}
		}
		/////////////////////////////////////////////////////////
		/**
		 * WARNING:
		 * not all arguments in callee method should be added to RBTag, since it fully depends on the caller.
		 * 
		 */
		for(Map.Entry<Args,SootMethod>entry:argsToCalleeMethod.entrySet()){
			SootMethod method = entry.getValue();
			unBindingValueMap.put(method,new LinkedList<Value>());
			localParameterToRemoteArgu.clear();
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			Body body = method.retrieveActiveBody();
			List<Local>argLists = body.getParameterLocals();//locals assigned with parameters
			List<CFGNode>nodes = cfg.getNodes();

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
					
				for(Variable viu:useVars){
					Value value = viu.getValue();
					if(isParaAssignStmt){
						// if this value is the argument
						if(value.equivTo(argu)){
							 // Run through all the argument lists and only
							 // annotate one with unbind
							Args args = entry.getKey();
							Value remote = args.getArgs().get(argIndex);
							if(localParameterToRemoteArgu.containsKey(value)){
								localParameterToRemoteArgu.get(value).add(remote);
							}else{
								List<Value>mappedval = new LinkedList<Value>();
								mappedval.add(remote);
								localParameterToRemoteArgu.put(value, mappedval);
							}
							List<Value> unbinds	= args.getUnBindArgs();
							
							if(unbinds!=null){
								if(!unbinds.isEmpty()){
									if(unbinds.contains(remote)){
										unBindingValueMap.get(method).add(value);
										// add this tag to the stmt
										RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
										if(rbTag!=null){
											rbTag.addBindingValue(value);
											if(verbose){
												System.out.println("Value:["+value.toString()+"] for stmt:["+stmt+"]");
											}
										}else{
											rbTag = new RBTag(value);
											stmt.addTag(rbTag);
											if(verbose){
												System.out.println("Value:["+value.toString()+"] for stmt:["+stmt+"]");
											}
										}
									}
								}
							}
							
							
						}
					}if(stmt instanceof AssignStmt){
						AssignStmt assignstmt = (AssignStmt)stmt;
						Value RHSValue = assignstmt.getRightOp();
						if(RHSValue!=null){
							if(unBindingValueMap.get(method).contains(RHSValue)){
								Value LHSValue = assignstmt.getLeftOp();
								RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
								unBindingValueMap.get(method).add(LHSValue);
								if(rbTag!=null){
									rbTag.addBindingValue(LHSValue);
									if(verbose){
										System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
									}
								}else{
									rbTag = new RBTag(LHSValue);
									stmt.addTag(rbTag);
									if(verbose){
										System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
									}
								}
							}
						}
					}else if(stmt instanceof DefinitionStmt){
						DefinitionStmt defstmt = (DefinitionStmt)stmt;
						Value RHSValue = defstmt.getRightOp();
						if(RHSValue!=null){
							if(unBindingValueMap.get(method).contains(RHSValue)){
								Value LHSValue = defstmt.getLeftOp();
								RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
								unBindingValueMap.get(method).add(LHSValue);
								if(rbTag!=null){
									rbTag.addBindingValue(LHSValue);
									if(verbose){
										System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
									}
								}else{
									rbTag = new RBTag(LHSValue);
									stmt.addTag(rbTag);
									if(verbose){
										System.out.println("Value:["+LHSValue.toString()+"] for stmt:["+stmt+"]");
									}
								}
							}
						}
					}
				}
			}
			
		}
		/*
		for(SootMethod method:calleeMethod){
			unBindingValueMap.put(method,new LinkedList<Value>());
			localParameterToRemoteArgu.clear();
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			Body body = method.retrieveActiveBody();
			List<Local>argLists = body.getParameterLocals();//locals assigned with parameters
			List<CFGNode>nodes = cfg.getNodes();

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
					
				for(Variable viu:useVars){
					Value value = viu.getValue();
					if(isParaAssignStmt){
						// if this value is the argument
						if(value.equivTo(argu)){
							 // Run through all the argument lists and only
							 // annotate one with unbind
							for(Args args:methodToArgsList.get(method)){
								Value remote = args.getArgs().get(argIndex);
								if(localParameterToRemoteArgu.containsKey(value)){
									localParameterToRemoteArgu.get(value).add(remote);
								}else{
									List<Value>mappedval = new LinkedList<Value>();
									mappedval.add(remote);
									localParameterToRemoteArgu.put(value, mappedval);
								}
								List<Value> unbinds	= args.getUnBindArgs();
								
								if(unbinds!=null){
									if(!unbinds.isEmpty()){
										if(unbinds.contains(remote)){
											unBindingValueMap.get(method).add(value);
											// add this tag to the stmt
											RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
											if(rbTag!=null){
												rbTag.addBindingValue(value);
											}else{
												rbTag = new RBTag(value);
												stmt.addTag(rbTag);
											}
										}
									}
								}
							}
							
							
						}
					}if(stmt instanceof AssignStmt){
						AssignStmt assignstmt = (AssignStmt)stmt;
						Value RHSValue = assignstmt.getRightOp();
						if(RHSValue!=null){
							if(unBindingValueMap.get(method).contains(RHSValue)){
								Value LHSValue = assignstmt.getLeftOp();
								RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
								unBindingValueMap.get(method).add(LHSValue);
								if(rbTag!=null){
									rbTag.addBindingValue(LHSValue);
								}else{
									rbTag = new RBTag(LHSValue);
									stmt.addTag(rbTag);
								}
							}
						}
					}else if(stmt instanceof DefinitionStmt){
						DefinitionStmt defstmt = (DefinitionStmt)stmt;
						Value RHSValue = defstmt.getRightOp();
						if(RHSValue!=null){
							if(unBindingValueMap.get(method).contains(RHSValue)){
								Value LHSValue = defstmt.getLeftOp();
								RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
								unBindingValueMap.get(method).add(LHSValue);
								if(rbTag!=null){
									rbTag.addBindingValue(LHSValue);
								}else{
									rbTag = new RBTag(LHSValue);
									stmt.addTag(rbTag);
								}
							}
						}
					}else if(stmt instanceof InvokeStmt){
						InvokeStmt invokestmt = (InvokeStmt)stmt;
						InvokeExpr invokexpr = invokestmt.getInvokeExpr();
						SootMethod callee = invokexpr.getMethod();
						List<Args>argulist = methodToArgsList.get(callee);
						Args curr = null;
						for(Args args:argulist){
							if(args.getCallerMethod().equals(method)){
								curr = args;
								break;
							}
						}
						if(curr!=null){
							for(Value vi:curr.getArgs()){
								// this argument is used
								if(unBindingValueMap.get(method).contains(vi)){
									curr.addUnBindArg(vi);
								}
							}
						}
					}
				}
					
					
			}
			
		}
		*/
		
		
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
