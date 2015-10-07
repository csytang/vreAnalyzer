package vreAnalyzer.Variants;

import soot.Body;
import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.UI.SourceClassBinding;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import java.awt.Color;
import java.io.File;
import java.util.*;

public class BindingResolver {

	public static BindingResolver instance = null;
	private Map<SootMethod,List<Args>>methodToArgsList = null;
    private ArrayList<Variant>variants = new ArrayList<Variant>();
    private boolean verbose = true;
    private List<SootMethod> allAppMethod = null;
    private List<SootMethod> callerMethod = null;
    private List<SootMethod> calleeMethod = null;
  
    // 将部分未绑定(PRBTag)的Stack
    private Stack<CFGNode> PRBAnalysisStack = new Stack<CFGNode>();
    private List<Value>PRBValuelist = new LinkedList<Value>();
    // 对于当前的 PRB值 对应的底层 PRValue
    private Map<Value,List<Value>> PRBAnalysisStackBindingValue = new HashMap<Value,List<Value>>();
    
    
    // PRB stack
    
    private Map<SootMethod,List<Value>>unBindingValueMap = null;
    private Map<Value,List<Variant>>valueToVariant = new HashMap<Value,List<Variant>>();//globally, for a value, there is a mapped variant
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
		/*
		 * 1. 检查所有的variant的设定是否存在问题 是否将variant加入（统一位置 从新思考）
		 * 
		 * 统一位置  所有的variant 单独列一个函数处理
		 * 
		 * 2. 
		 */
		// 1. for all methods first set the call site binding
		allAppMethod.addAll(ProgramFlowBuilder.inst().getAppConcreteMethods());
		// 定义的变量
		List<Variable> defVars = null;
		// 使用的变量
		List<Variable> useVars = null;
		
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
				
				for(SootMethod callee:appcallees){
					if(verbose){
						System.out.println("Add a method and args bind: callee method["+callee.getName()+"] with input parameters "
								+ "["+argsString+"] in caller method["+method.getName()+"]");
					}
					// FINISH
					Args ar = new Args(method,callee,args);
					// add this args to callee into record
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
		/////////////////////////////
		
		calleeMethod = new LinkedList<SootMethod>();
		calleeMethod.addAll(methodToArgsList.keySet());
		Set<SootMethod>tmp = new HashSet<SootMethod>(allAppMethod);
		tmp.retainAll(calleeMethod);
		callerMethod = new LinkedList<SootMethod>(allAppMethod);
		callerMethod.removeAll(tmp);
		
		// 2. if there is a call binding, does not use its own arguments value use parameter instead
		// use to decide whether a stmt is parameter assign stmt
		unBindingValueMap = new HashMap<SootMethod,List<Value>>();
		
		boolean isParaAssignStmt = false;
		for(SootMethod method:callerMethod){
			// clean the analysis stack
			PRBAnalysisStack.clear();
			PRBValuelist.clear();
			PRBAnalysisStackBindingValue.clear();
			
			unBindingValueMap.put(method,new LinkedList<Value>());
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode>nodes = cfg.getNodes();

			// 2.1 The method is invoked by other method use callers' parameters
			for(CFGNode node:nodes){
				if(node.isSpecial())
					continue;
				//////////////////////
				NodeDefUses defusenode = (NodeDefUses)node;
				Stmt stmt = defusenode.getStmt();
				useVars = defusenode.getUsedVars();
				defVars = defusenode.getDefinedVars();
				///////////////////////
				if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
					isParaAssignStmt = true;
				}else{
					isParaAssignStmt = false;
				}
				
				/////////////如果这个语句是域赋值给local语句////////////////////////////
				if(isParaAssignStmt){
					DefinitionStmt defstmt = (DefinitionStmt)stmt;
					Value argu = defstmt.getLeftOp();
					unBindingValueMap.get(method).add(argu);
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					for(Variable use:useVars){
						unBindingValueMap.get(method).add(use.getValue());
					}
					// 创建对应的variant
					List<Variant>variantscreated = createNewVariant(argu,stmt);
					if(rbTag!=null){
						rbTag.addBindingValue(argu);
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantscreated));
						}
					}else{
						rbTag = new RBTag(argu);
						stmt.addTag(rbTag);
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantscreated));
						}
					}
					continue;
				}
				////////////////////////////////////////////////////////
				
				boolean isused = false;
				for(Variable use:useVars){
					if(unBindingValueMap.get(method).contains(use.getValue())){
						isused = true;
						break;
					}
				}
				
				List<Variant> allvariantlist = new LinkedList<Variant>();
				
				
				///////////////////如果此语句中有未绑定内容调用///////////////////////
				if(isused){
					// 存在RB内容
					// 获得所有使用的变量的variant列表
					for(Variable use:useVars){
						if(valueToVariant.containsKey(use.getValue())){
							List<Variant> variantlist = valueToVariant.get(use.getValue());
							allvariantlist.addAll(variantlist);
						}
					}
					boolean containPRBValue = false;// 是否包含部分帮定值
					// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
					for(Variable use:useVars){
						if(!unBindingValueMap.get(method).contains(use.getValue())){
							// 将这个部分未绑定的值 加入到PRBValue列表中
							PRBValuelist.add(use.getValue());
							PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
							if(prbTag!=null){
								prbTag.addBindingValue(use.getValue());
								if(verbose){
									System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
								}
							}else{
								prbTag = new PRBTag(use.getValue());
								stmt.addTag(prbTag);
								if(verbose){
									System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
								}
							}
							if(!containPRBValue)
								containPRBValue = true;
						}else{
							// 如果包含则为未绑定语句
							List<Variant> variantlist = valueToVariant.get(use.getValue());
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							if(rbTag!=null){
								rbTag.addBindingValue(use.getValue());
								if(verbose){
									System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantlist));
								}
							}else{
								rbTag = new RBTag(use.getValue());
								stmt.addTag(rbTag);
								if(verbose){
									System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantlist));
								}
							}
						}
					}
					if(containPRBValue){
						// 将这个部分未绑定的cfgnode加入到列表中
						PRBAnalysisStack.push(node);
					}
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
					for(Variable def:defVars){
						rbTag.addBindingValue(def.getValue());
						//将为左侧的定义 加入到绑定中
						if(!unBindingValueMap.get(method).contains(def.getValue())){
							unBindingValueMap.get(method).add(def.getValue());
						}
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+def.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(allvariantlist));
						}
						
						markdefandPreUse(def.getValue(),node,nodes);
						/*
						 * 由于使用的已经加入到variant之中 只需要
						 * 将定义的变量加入到variant之中
						 */
						for(Variant variant:allvariantlist){
							variant.addPaddingValue(def.getValue());
							variant.addFirstBind(def.getValue(), stmt);
						}
						valueToVariant.put(def.getValue(), allvariantlist);
					}
					// 将当前语句加入到variant的绑定中
					for(Variant variant:allvariantlist){
						variant.addBindingStmts(stmt);
					}
				}
				else if(isuseVarsPRBindingOverlap(useVars,PRBValuelist) && defVars.isEmpty()){
					// 使用了prb值 但是没有赋值内容
					for(Variable use:useVars){
						// 将这个部分未绑定的值 加入到PRBValue列表中
						PRBValuelist.add(use.getValue());
						PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
						if(prbTag!=null){
							prbTag.addBindingValue(use.getValue());
							if(verbose){
								System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
							}
						}else{
							prbTag = new PRBTag(use.getValue());
							stmt.addTag(prbTag);
							if(verbose){
								System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
							}
						}
					}
					PRBAnalysisStack.push(node);
				}
				else if(isuseVarsPRBindingOverlap(useVars,PRBValuelist) && !defVars.isEmpty()){
					// 使用了prb值 但是是有赋值内容
					List<Stmt>partialBindingStmt = new LinkedList<Stmt>();
					while(PRBAnalysisStack!=null){
						CFGNode prbnode = PRBAnalysisStack.pop();
						Stmt prbstmt = prbnode.getStmt();
						PRBTag prbTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
						RBTag rpTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
						if(prbTag!=null && rpTag==null){
							partialBindingStmt.add(prbstmt);
						}else if(rpTag!=null){
							// 这里只执行一次 然后跳出
							Set<Value>rpbindingValue = rpTag.getBindingValues();
							// 对于这里的variants 加入对应的stmt
							for(Value value:rpbindingValue){
								for(Variant variant:valueToVariant.get(value)){
									variant.addBindingStmts(partialBindingStmt);
									variant.addPaddingValue(PRBValuelist);
								}
							}
							// 将所有的部分未绑定(PRB) -> 转换为 未绑定(RB)
							for(Value rpbvalue:PRBValuelist){
								if(!unBindingValueMap.get(method).contains(rpbvalue)){
									unBindingValueMap.get(method).add(rpbvalue);
								}
							}
							
							PRBValuelist.clear();
							break;
						}
						
					}
					PRBAnalysisStack.clear();
					
				}
				//////////////////////////////////////////////////
			
				
				if(stmt.containsInvokeExpr()){
					InvokeExpr invokexpr = stmt.getInvokeExpr();
					SootMethod callee = invokexpr.getMethod();
					// add this method to callee method analysis stack
					// analysisStack.push(callee);
					List<Args>argulist = methodToArgsList.get(callee);
					if(argulist==null){
						continue;
					}
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
		System.out.println("--------------------Finish caller method----------------------------");
		
	
		/////////////////////////////////////////////////////////
		/**
		 * WARNING:
		 * not all arguments in callee method should be added to RBTag, since it fully depends on the caller.
		 */
		Map<Value,List<Value>>localParameterToRemoteArgu = new HashMap<Value,List<Value>>();
		for(SootMethod method:calleeMethod){
			PRBAnalysisStack.clear();
			PRBValuelist.clear();
			unBindingValueMap.put(method,new LinkedList<Value>());
			localParameterToRemoteArgu.clear();
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			if(cfg==null)
				continue;
			Body body = method.retrieveActiveBody();
			List<Local>argLists = body.getParameterLocals();//locals assigned with parameters
			List<CFGNode>nodes = cfg.getNodes();
			// 2.1 The method is invoked by other method use callers' parameters
			for(CFGNode node:nodes){
				NodeDefUses defusenode = (NodeDefUses)node;
				if(node.isSpecial())
					continue;
				Stmt stmt = defusenode.getStmt();//$r7 := @caughtexception
				useVars = defusenode.getUsedVars();
				defVars = defusenode.getDefinedVars();
				
				if(stmt instanceof IdentityStmt &&
						!(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
					isParaAssignStmt = true;
				}else{
					isParaAssignStmt = false;
				}
				
				int argIndex = -1;
				Value argu = null;
				if(isParaAssignStmt){
					IdentityStmt idstmt =  (IdentityStmt)stmt;
					// 2.1.1 arguments
					argu = idstmt.getLeftOp();
					Local localarg = (Local)argu;
					argIndex = argLists.indexOf(localarg);
					if(argIndex==-1)
						continue;
					
					// Run through all the argument lists and only
					// annotate one with unbind
					// args list
					List<Args> args = methodToArgsList.get(method);
					if(args!=null){
						
						for(Args arg:args){
							// 1. a remote value 
							Value remote = arg.getArgs().get(argIndex);
							
							
							List<Value> unbinds	= arg.getUnBindArgs();
							if(localParameterToRemoteArgu.containsKey(argu)){
								localParameterToRemoteArgu.get(argu).add(remote);
							}else{
								List<Value>mappedval = new LinkedList<Value>();
								mappedval.add(remote);
								localParameterToRemoteArgu.put(argu, mappedval);
							}
							if(unbinds!=null){
								if(!unbinds.isEmpty()){
									if(unbinds.contains(remote)){
										unBindingValueMap.get(method).add((Value)localarg);
										// add this tag to the stmt
										RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
										///// add variants to variant set///
										List<Variant> variantlist = valueToVariant.get(remote);
										for(Variant variant:variantlist){
											variant.addPaddingValue((Value)localarg);
											variant.addBindingStmts(stmt);
											variant.addFirstBind((Value)localarg, stmt);
										}
				
										if(valueToVariant.containsKey((Value)localarg)){
											//valueToVariant.put((Value)localarg,variantlist);
											valueToVariant.get((Value)localarg).addAll(variantlist);
										}else{
											
											valueToVariant.put((Value)localarg,variantlist);
										}
										/////
										if(rbTag!=null){
											rbTag.addBindingValue((Value)localarg);
											if(verbose){
												System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+localarg.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantlist));
											}
										}else{
											rbTag = new RBTag((Value)localarg);
											stmt.addTag(rbTag);
											if(verbose){
												System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+localarg.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantlist));
											}
										}
												
									}
								}
							}
						}
					}
					continue;
				}
				
				boolean isused = false;
				for(Variable use:useVars){
					if(unBindingValueMap.get(method).contains(use.getValue())){
						isused = true;
						break;
					}
				}
				
				List<Variant>allvariantlist = new LinkedList<Variant>();

				///////////////////如果此语句中有未绑定内容调用///////////////////////
				if(isused){
					// 存在RB内容
					// 获得所有使用的变量的variant列表
					for(Variable use:useVars){
						if(valueToVariant.containsKey(use.getValue())){
							List<Variant> variantlist = valueToVariant.get(use.getValue());
							allvariantlist.addAll(variantlist);
						}
					}
					boolean containPRBValue = false;// 是否包含部分帮定值
					// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
					for(Variable use:useVars){
						if(!unBindingValueMap.get(method).contains(use.getValue())){
							// 将这个部分未绑定的值 加入到PRBValue列表中
							PRBValuelist.add(use.getValue());
							PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
							if(prbTag!=null){
								prbTag.addBindingValue(use.getValue());
								if(verbose){
									System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
								}
							}else{
								prbTag = new PRBTag(use.getValue());
								stmt.addTag(prbTag);
								if(verbose){
									System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
								}
							}
							if(!containPRBValue)
								containPRBValue = true;
						}else{
							// 如果包含则为未绑定语句
							List<Variant> variantlist = valueToVariant.get(use.getValue());
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							if(rbTag!=null){
								rbTag.addBindingValue(use.getValue());
								if(verbose){
									System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantlist));
								}
							}else{
								rbTag = new RBTag(use.getValue());
								stmt.addTag(rbTag);
								if(verbose){
									System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantlist));
								}
							}
						}
					}
					if(containPRBValue){
						// 将这个部分未绑定的cfgnode加入到列表中
						PRBAnalysisStack.push(node);
					}
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
					for(Variable def:defVars){
						rbTag.addBindingValue(def.getValue());
						//将为左侧的定义 加入到绑定中
						if(!unBindingValueMap.get(method).contains(def.getValue())){
							unBindingValueMap.get(method).add(def.getValue());
						}
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+def.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(allvariantlist));
						}
						
						markdefandPreUse(def.getValue(),node,nodes);
						/*
						 * 由于使用的已经加入到variant之中 只需要
						 * 将定义的变量加入到variant之中
						 */
						for(Variant variant:allvariantlist){
							variant.addPaddingValue(def.getValue());
							variant.addFirstBind(def.getValue(), stmt);
						}
						valueToVariant.put(def.getValue(), allvariantlist);
					}
					// 将当前语句加入到variant的绑定中
					for(Variant variant:allvariantlist){
						variant.addBindingStmts(stmt);
					}
				}
				else if(isuseVarsPRBindingOverlap(useVars,PRBValuelist) && defVars.isEmpty()){
					// 使用了prb值 但是没有赋值内容
					for(Variable use:useVars){
						// 将这个部分未绑定的值 加入到PRBValue列表中
						PRBValuelist.add(use.getValue());
						PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
						if(prbTag!=null){
							prbTag.addBindingValue(use.getValue());
							if(verbose){
								System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
							}
						}else{
							prbTag = new PRBTag(use.getValue());
							stmt.addTag(prbTag);
							if(verbose){
								System.out.println("[Parital binding for class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
							}
						}
					}
					PRBAnalysisStack.push(node);
				}
				else if(isuseVarsPRBindingOverlap(useVars,PRBValuelist) && !defVars.isEmpty()){
					// 使用了prb值 但是是有赋值内容
					List<Stmt>partialBindingStmt = new LinkedList<Stmt>();
					while(PRBAnalysisStack!=null){
						CFGNode prbnode = PRBAnalysisStack.pop();
						Stmt prbstmt = prbnode.getStmt();
						PRBTag prbTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
						RBTag rpTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
						if(prbTag!=null && rpTag==null){
							partialBindingStmt.add(prbstmt);
						}else if(rpTag!=null){
							// 这里只执行一次
							Set<Value>rpbindingValue = rpTag.getBindingValues();
							// 对于这里的variants 加入对应的stmt
							for(Value value:rpbindingValue){
								for(Variant variant:valueToVariant.get(value)){
									variant.addBindingStmts(partialBindingStmt);
									variant.addPaddingValue(PRBValuelist);
								}
							}
							// 将所有的部分未绑定(PRB) -> 转换为 未绑定(RB)
							for(Value rpbvalue:PRBValuelist){
								if(!unBindingValueMap.get(method).contains(rpbvalue)){
									unBindingValueMap.get(method).add(rpbvalue);
								}
							}
							PRBValuelist.clear();
							break;
						}
						
					}
					PRBAnalysisStack.clear();
					
				}
			}
			
			
		}
		// DEBUG
		System.out.println("Num. of variants without combine:"+variants.size());
		// FINISH
	}
	/*
	 * 生成一个新的variant
	 */
	private List<Variant> createNewVariant(Value vi,List<Stmt>stmts){
		///// add variants to variant set///
		Variant variant = new Variant(vi,stmts);
		variants.add(variant);
		List<Variant>variantlist = new LinkedList<Variant>();
		variantlist.add(variant);
		valueToVariant.put(vi, variantlist);
		return variantlist;
	}
	private List<Variant> createNewVariant(Value vi,Stmt stmt){
		///// add variants to variant set///
		Variant variant = new Variant(vi,stmt);
		variants.add(variant);
		List<Variant>variantlist = new LinkedList<Variant>();
		variantlist.add(variant);
		valueToVariant.put(vi, variantlist);
		return variantlist;
	}
	
	private boolean isuseVarsPRBindingOverlap(List<Variable> useVars, List<Value> pRBBindingValues) {
		// TODO Auto-generated method stub
		for(Variable vari:useVars){
			if(pRBBindingValues.contains(vari.getValue()))
				return true;
		}
		return false;
	}
	
	
	/**
	 * 1. current solution, first we all annotated all the files(testing)
	 * 2. seperate stmts list into combination of code blocks
	 * 
	 * Rewrite with annotated color
	 */
	
	public void markdefandPreUse(Value value,CFGNode cfgNode,List<CFGNode>nodes){
		for(CFGNode node:nodes){
			if(node==cfgNode)
				break;
			NodeDefUses defusenode = (NodeDefUses)node;
			if(defusenode.isSpecial())
				continue;
			Stmt stmt = defusenode.getStmt();
			List<Variable>defs = defusenode.getDefinedVars();
			for(Variable variable:defs){
				if(variable.getValue().equals(value)){
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					if(rbTag!=null){
						rbTag.addBindingValue(value);
						if(verbose){
							System.out.println("Value:["+value.toString()+"] for stmt:["+stmt+"] with variant:");
						}
					}else{
						rbTag = new RBTag(value);
						stmt.addTag(rbTag);
						if(verbose){
							System.out.println("Value:["+value.toString()+"] for stmt:["+stmt+"] with variant:");
						}
					}
				
					///// add variants to variant set///
					List<Variant> variantlist = valueToVariant.get(value);
					for(Variant variant:variantlist){
						variant.addBindingStmts(stmt);
						variant.addFirstBind(value, stmt);
					}
					/////
				}
			}
			List<Variable>uses = defusenode.getUsedVars();
			for(Variable use:uses){
				if(use.getValue().equals(value)){
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					if(rbTag!=null){
						rbTag.addBindingValue(value);
						if(verbose){
							System.out.println("Value:["+value.toString()+"] for stmt:["+stmt+"] with variant:");
						}
					}else{
						rbTag = new RBTag(value);
						stmt.addTag(rbTag);
						if(verbose){
							System.out.println("Value:["+value.toString()+"] for stmt:["+stmt+"] with variant:");
						}
					}
					///// add variants to variant set///
					List<Variant> variantlist = valueToVariant.get(value);
					for(Variant variant:variantlist){
						variant.addBindingStmts(stmt);
						variant.addFirstBind(value, stmt);
					}
					/////
				}
			}
		}
	}
	
	public void annotate() {
		if (vreAnalyzerCommandLine.isSourceBinding() &&
				vreAnalyzerCommandLine.isStartFromGUI()) {
			Map<SootClass, List<Stmt>> classVariantStmtMap = new HashMap<SootClass, List<Stmt>>();
			for (Variant variant : variants) {
				List<Stmt> bindingStmts = variant.getBindingStmts();
				Color variantColor = VariantColorMap.inst().getColorforVariant(variant);
				classVariantStmtMap.clear();
				String variantId = "variant:"+variants.indexOf(variant);
				if(verbose)
					System.out.println("Currently process variant with id:"+variantId);
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
					if(verbose)
						System.out.println("Stmt:"+stmt);
				}
				
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
			VariantColorMap.inst().addToLegend();
			
			
			
		}
	}
	public String getVariantListIds(List<Variant>variantlist){
		String varaintIds = "variants:[";
		for(Variant vri:variantlist){
			varaintIds+=variants.indexOf(vri)+",";
		}
		if(!variantlist.isEmpty())
			varaintIds = varaintIds.substring(0, varaintIds.length()-1);
		else
			return "variants:[]";
		varaintIds+="]";
		return varaintIds;
	}
	
}
