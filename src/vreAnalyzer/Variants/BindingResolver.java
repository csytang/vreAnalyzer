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
    // 包含函数调用的函数
    private List<SootMethod> callerMethod = null;
    // 不包含任何函数调用的函数
    private List<SootMethod> calleeMethod = null;
  
    // 将部分未绑定(PRBTag)的Stack
    private Stack<CFGNode> PRBAnalysisStack = new Stack<CFGNode>();
    // 对于当前的 PRB值 对应的底层 PRValue
    private Map<Value,List<Value>> PRBAnalysisStackBindingValue = new HashMap<Value,List<Value>>();
    // Method 到 ValueToVariant的对应表
    private Map<SootMethod,List<ValueToVariant>>methodToVToVariant = new HashMap<SootMethod,List<ValueToVariant>>();//globally, for a value, there is a mapped variant
    // 对于每一个函数来讲 有以下两个
    // 1. unbindValueList 未绑定值列表
    private Map<SootMethod,List<Value>>methodToUnbindValues = new HashMap<SootMethod,List<Value>>();
    // 2. partialUnbindValueList 部分未绑定值列表
    private Map<SootMethod,List<Value>>methodToParitalUnbindValues = new HashMap<SootMethod,List<Value>>();
    
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
	
	private Variant createNewVToVariant(Value value,SootMethod method,List<Stmt>bindingstmts){
		if(methodToVToVariant.containsKey(method)){
			Variant variant = new Variant(value,bindingstmts);
			ValueToVariant vtovariant = new ValueToVariant(value,variant);
			// 将这个ValueToVariant加入到MethodToVToVariant之中
			methodToVToVariant.get(method).add(vtovariant);
			// 向全局列表中加入这个variant
			variants.add(variant);
			// 向methodToUnbindValues加入未绑定值
			methodToUnbindValues.get(method).add(value);
			return variant;
		}else{
			// 进入这里应该是一个错误
			return null;
		}
	}
	private Variant createNewVToVariant(Value value,SootMethod method,Stmt bindingstmt){
		if(methodToVToVariant.containsKey(method)){
			Variant variant = new Variant(value,bindingstmt);
			ValueToVariant vtovariant = new ValueToVariant(value,variant);
			// 将这个ValueToVariant加入到MethodToVToVariant之中
			methodToVToVariant.get(method).add(vtovariant);
			// 向全局列表中加入这个variant
			variants.add(variant);
			// 向methodToUnbindValues加入未绑定值
			methodToUnbindValues.get(method).add(value);
			return variant;
		}else{
			// 进入这里应该是一个错误
			return null;
		}

	}
	private Variant getVariantFromValue(Value value,SootMethod method){
		if(methodToVToVariant.containsKey(method)){
			for(ValueToVariant vtvariant:methodToVToVariant.get(method)){
				if(vtvariant.getValue().equals(value))
					return vtvariant.getvariant();
			}
			return null;
		}else{
			// 进入这里应该是一个错误
			return null;
		}
	}
	private List<Variant> getVariantFromValue(Set<Value>values,SootMethod method){
		List<Variant>varaints = new LinkedList<Variant>();
		if(methodToVToVariant.containsKey(method)){
			for(Value value:values){
				for(ValueToVariant vtvariant:methodToVToVariant.get(method)){
					if(vtvariant.getValue().equals(value)){
						varaints.add(vtvariant.getvariant());
						break;
					}
				}
			}
		}else{
			// 进入这里应该是一个错误
			return null;
		}
		return variants;
	}
	public void parse(){
		
		allAppMethod.addAll(ProgramFlowBuilder.inst().getAppConcreteMethods());
		// 定义的变量
		List<Variable> defVars = null;
		// 使用的变量
		List<Variable> useVars = null;
		
		for(SootMethod method:allAppMethod){
			MethodTag mTag = (MethodTag) method.getTag(MethodTag.TAG_NAME);
			List<CallSite> callsites = mTag.getAllCallSites();
			// 初始化methodToVToVariant
			methodToVToVariant.put(method, new LinkedList<ValueToVariant>());
			// 初始化methodToUnbindValue
			methodToUnbindValues.put(method, new LinkedList<Value>());
			// 初始化methodToParitalUnbindValues
			methodToParitalUnbindValues.put(method, new LinkedList<Value>());
			for(CallSite site:callsites){
				
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
		
		boolean isParaAssignStmt = false;
		for(SootMethod method:callerMethod){
			// clean the analysis stack
			PRBAnalysisStack.clear();
			PRBAnalysisStackBindingValue.clear();
			
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode>nodes = cfg.getNodes();

			// 2.1 The method is invoked by other method use callers' parameters
			for(CFGNode node:nodes){
				if(node.isSpecial())
					continue;
				//////////////////////
				NodeDefUses defusenode = (NodeDefUses)node;
				Stmt stmt = defusenode.getStmt();
				useVars = defusenode.getUsedVars();//在当前语句中的使用
				defVars = defusenode.getDefinedVars();//当前语句中的定义
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
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					// 创建对应的variant
					Variant variantcreated = createNewVToVariant(argu,method,stmt);
					if(rbTag!=null){
						rbTag.addBindingValue(argu);
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantcreated));
						}
					}else{
						rbTag = new RBTag(argu);
						stmt.addTag(rbTag);
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantcreated));
						}
					}
					continue;
				}
				////////////////////////////////////////////////////////
				// P1 检查这个点下的所有unbindingValueList
				////////////////////////////////////////////////////////
				// 当前allvariantlist包含所有使用的variant
				List<Variant>allvariantlist = new LinkedList<Variant>();
				///////////////////如果此语句中有未绑定内容调用///////////////////////
				if(usedOverlap(useVars,methodToUnbindValues.get(method))){
					// 存在RB内容
					boolean containPRBValue = false;// 是否包含部分帮定值
					// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
					for(Variable use:useVars){
						if(methodToUnbindValues.get(method).contains(use.getValue())){
							// 如果包含则为未绑定语句
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							if(rbTag!=null){
								rbTag.addBindingValue(use.getValue());
								if(verbose){
									System.out.println("RB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(getVariantFromValue(use.getValue(),method)));
								}
							}else{
								rbTag = new RBTag(use.getValue());
								stmt.addTag(rbTag);
								if(verbose){
									System.out.println("RB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(getVariantFromValue(use.getValue(),method)));
								}
							}
							Variant variant = getVariantFromValue(use.getValue(),method);
							allvariantlist.add(variant);
						}else{
							PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
							if(prbTag!=null){
								prbTag.addBindingValue(use.getValue());
								if(verbose){
									System.out.println("PRB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
								}
							}else{                                                                 
								prbTag = new PRBTag(use.getValue());
								stmt.addTag(prbTag);
								if(verbose){
									System.out.println("PRB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
								}
							}
							// 将这个值加入到部分未绑定序列
							methodToParitalUnbindValues.get(method).add(use.getValue());
							if(!containPRBValue)
								containPRBValue = true;
						}
					}
					if(containPRBValue && defVars.isEmpty()){
						// 将这个部分未绑定的cfgnode加入到列表中
						PRBAnalysisStack.push(node);
					}
					RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
					// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
					// 检查一下 多少个variant绑定在上面
					for(Variable def:defVars){
						rbTag.addBindingValue(def.getValue());
						//将为左侧的定义 加入到绑定中
						if(!methodToUnbindValues.get(method).contains(def.getValue())){
							methodToUnbindValues.get(method).add(def.getValue());
						}
						if(verbose){
							System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+def.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(allvariantlist));
						}
						/*
						 * 由于使用的已经加入到variant之中 只需要
						 * 将定义的变量加入到variant之中
						 */
						for(Variant variant:allvariantlist){
							variant.addPaddingValue(def.getValue());
						}
					}
					// 将当前语句加入到variant的绑定中
					for(Variant variant:allvariantlist){
						variant.addBindingStmts(stmt);
					}
				}
				else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && defVars.isEmpty()){
					// 使用了prb值 但是没有赋值内容
					for(Variable use:useVars){
						// 将这个部分未绑定的值 加入到PRBValue列表中
						PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
						if(methodToParitalUnbindValues.get(method).contains(use.getValue())){
							continue;
						}else{
							methodToParitalUnbindValues.get(method).add(use.getValue());
						}
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
				else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
					// 使用了prb值 但是是有赋值内容
					List<Stmt>partialBindingStmt = new LinkedList<Stmt>();
					List<Value>partialBindingValues = new LinkedList<Value>();
					while(PRBAnalysisStack!=null){
						CFGNode prbnode = PRBAnalysisStack.pop();
						Stmt prbstmt = prbnode.getStmt();
						PRBTag prpTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
						partialBindingStmt.add(prbstmt);
						partialBindingValues.addAll(prpTag.getBindingValues());
						if(PRBAnalysisStack.size()==0){
							//当前为最后一个节点
							// 1. 找到所需要放置的variant
							// 2. 将所有stmt 放入到variant中
							// 3. 将所有value 放入到variant中
							RBTag rpTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
							Set<Value>bindingvalues = rpTag.getBindingValues();
							List<Variant>bindingvaraints = getVariantFromValue(bindingvalues,method);
							for(Variant variant:bindingvaraints){
								variant.addPaddingValue(partialBindingValues);
								variant.addBindingStmts(partialBindingStmt);
							}
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
							if(methodToUnbindValues.get(method).contains(vi)){
								curr.addUnBindArg(vi);
							}
						}
					}
				}
			}
		}
		System.out.println("--------------------Finish caller method----------------------------");
		
	
		/////////////////////////////////////////////////////////
		
		Map<Value,List<Value>>localParameterToRemoteArgu = new HashMap<Value,List<Value>>();
		for(SootMethod method:calleeMethod){
			// clean the analysis stack
			PRBAnalysisStack.clear();
			PRBAnalysisStackBindingValue.clear();
			
			localParameterToRemoteArgu.clear();
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			if(cfg==null)
				continue;
			Body body = method.retrieveActiveBody();
			List<Local>argLists = body.getParameterLocals();//locals assigned with parameters
			List<CFGNode>nodes = cfg.getNodes();
			// 获得对应的实际参数列表
			List<Args> args = methodToArgsList.get(method);
			// 对于每一次调用进行一次运算
			if(args!=null){
				for(Args argument:args){
					// 调用函数
					SootMethod callerMethod = argument.getCallerMethod();
					
					for(CFGNode node:nodes){
						NodeDefUses defusenode = (NodeDefUses)node;
						if(node.isSpecial())
							continue;
						Stmt stmt = defusenode.getStmt();//$r7 := @caughtexception
						useVars = defusenode.getUsedVars();
						defVars = defusenode.getDefinedVars();
						if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
							isParaAssignStmt = true;
						}else{
							isParaAssignStmt = false;
						}
						Value argu = null;
						if(isParaAssignStmt){
							IdentityStmt idstmt =  (IdentityStmt)stmt;
							argu = idstmt.getLeftOp();
							Local localarg = (Local)argu;
							int argIndex = argLists.indexOf(localarg);
							if(argIndex==-1)// catch exception
								continue;
							// 获取在调用层的参数
							Value remote = argument.getArgs().get(argIndex);
							List<Value> unbinds	= argument.getUnBindArgs();
							if(unbinds==null){
								// 如果所有的实参 都是fixed 那么没有必要进行处理 也不必加入到unbind序列中
								continue;
							}else if(unbinds!=null){
								// 如果有实参未绑定
								if(unbinds.contains(remote)){
									RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
									// 获取实际的variant
									Variant remotevaraint = getVariantFromValue(remote,callerMethod);
									// 将这个variant加入到methodToVToVariant中
									ValueToVariant vtoVariant = new ValueToVariant(argu,remotevaraint);
									methodToVToVariant.get(method).add(vtoVariant);
									if(rbTag!=null){
										rbTag.addBindingValue(argu);
										if(verbose){
											System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(remotevaraint));
										}
									}else{
										rbTag = new RBTag(argu);
										stmt.addTag(rbTag);
										if(verbose){
											System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(remotevaraint));
										}
									}
									continue;
								}else{
									//当前的参数是 fixed 没有必要处理
									continue;
								}
							}
						}
						////////////////////////////////////////////////////////
						// P1 检查这个点下的所有unbindingValueList
						////////////////////////////////////////////////////////
						// 当前allvariantlist包含所有使用的variant
						List<Variant>allvariantlist = new LinkedList<Variant>();
						///////////////////如果此语句中有未绑定内容调用///////////////////////
						if(usedOverlap(useVars,methodToUnbindValues.get(method))){
							// 存在RB内容
							boolean containPRBValue = false;// 是否包含部分帮定值
							// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
							for(Variable use:useVars){
								if(methodToUnbindValues.get(method).contains(use.getValue())){
									// 如果包含则为未绑定语句
									RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
									if(rbTag!=null){
										rbTag.addBindingValue(use.getValue());
										if(verbose){
											System.out.println("RB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(getVariantFromValue(use.getValue(),method)));
										}
									}else{
										rbTag = new RBTag(use.getValue());
										stmt.addTag(rbTag);
										if(verbose){
											System.out.println("RB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(getVariantFromValue(use.getValue(),method)));
										}
									}
									Variant variant = getVariantFromValue(use.getValue(),method);
									allvariantlist.add(variant);
								}else{
									PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
									if(prbTag!=null){
										prbTag.addBindingValue(use.getValue());
										if(verbose){
											System.out.println("PRB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
										}
									}else{                                                                 
										prbTag = new PRBTag(use.getValue());
										stmt.addTag(prbTag);
										if(verbose){
											System.out.println("PRB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
										}
									}
									// 将这个值加入到部分未绑定序列
									methodToParitalUnbindValues.get(method).add(use.getValue());
									if(!containPRBValue)
										containPRBValue = true;
								}
							}
							if(containPRBValue && defVars.isEmpty()){
								// 将这个部分未绑定的cfgnode加入到列表中
								PRBAnalysisStack.push(node);
							}
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
							// 检查一下 多少个variant绑定在上面
							for(Variable def:defVars){
								rbTag.addBindingValue(def.getValue());
								//将为左侧的定义 加入到绑定中
								if(!methodToUnbindValues.get(method).contains(def.getValue())){
									methodToUnbindValues.get(method).add(def.getValue());
								}
								if(verbose){
									System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+def.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(allvariantlist));
								}
								/*
								 * 由于使用的已经加入到variant之中 只需要
								 * 将定义的变量加入到variant之中
								 */
								for(Variant variant:allvariantlist){
									variant.addPaddingValue(def.getValue());
								}
							}
							// 将当前语句加入到variant的绑定中
							for(Variant variant:allvariantlist){
								variant.addBindingStmts(stmt);
							}
						}
						
						else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && defVars.isEmpty()){
							// 使用了prb值 但是没有赋值内容
							for(Variable use:useVars){
								// 将这个部分未绑定的值 加入到PRBValue列表中
								PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
								if(methodToParitalUnbindValues.get(method).contains(use.getValue())){
									continue;
								}else{
									methodToParitalUnbindValues.get(method).add(use.getValue());
								}
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
						else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
							// 使用了prb值 但是是有赋值内容
							List<Stmt>partialBindingStmt = new LinkedList<Stmt>();
							List<Value>partialBindingValues = new LinkedList<Value>();
							while(PRBAnalysisStack!=null){
								CFGNode prbnode = PRBAnalysisStack.pop();
								Stmt prbstmt = prbnode.getStmt();
								PRBTag prpTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
								partialBindingStmt.add(prbstmt);
								partialBindingValues.addAll(prpTag.getBindingValues());
								if(PRBAnalysisStack.size()==0){
									//当前为最后一个节点
									// 1. 找到所需要放置的variant
									// 2. 将所有stmt 放入到variant中
									// 3. 将所有value 放入到variant中
									RBTag rpTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
									Set<Value>bindingvalues = rpTag.getBindingValues();
									List<Variant>bindingvaraints = getVariantFromValue(bindingvalues,method);
									for(Variant variant:bindingvaraints){
										variant.addPaddingValue(partialBindingValues);
										variant.addBindingStmts(partialBindingStmt);
									}
								}
							}
							PRBAnalysisStack.clear();
						}
						
						
					}
					
				}
			}else{
				// 没有参数进入和 callermethod的处理方法一样
				// 2.1 The method is invoked by other method use callers' parameters
				for(CFGNode node:nodes){
					if(node.isSpecial())
						continue;
					//////////////////////
					NodeDefUses defusenode = (NodeDefUses)node;
					Stmt stmt = defusenode.getStmt();
					useVars = defusenode.getUsedVars();//在当前语句中的使用
					defVars = defusenode.getDefinedVars();//当前语句中的定义
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
						RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
						// 创建对应的variant
						Variant variantcreated = createNewVToVariant(argu,method,stmt);
						if(rbTag!=null){
							rbTag.addBindingValue(argu);
							if(verbose){
								System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantcreated));
							}
						}else{
							rbTag = new RBTag(argu);
							stmt.addTag(rbTag);
							if(verbose){
								System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+argu.toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(variantcreated));
							}
						}
						continue;
					}
					////////////////////////////////////////////////////////
					// P1 检查这个点下的所有unbindingValueList
					////////////////////////////////////////////////////////
					// 当前allvariantlist包含所有使用的variant
					List<Variant>allvariantlist = new LinkedList<Variant>();
					///////////////////如果此语句中有未绑定内容调用///////////////////////
					if(usedOverlap(useVars,methodToUnbindValues.get(method))){
						// 存在RB内容
						boolean containPRBValue = false;// 是否包含部分帮定值
						// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
						for(Variable use:useVars){
							if(methodToUnbindValues.get(method).contains(use.getValue())){
								// 如果包含则为未绑定语句
								RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
								if(rbTag!=null){
									rbTag.addBindingValue(use.getValue());
									if(verbose){
										System.out.println("RB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(getVariantFromValue(use.getValue(),method)));
									}
								}else{
									rbTag = new RBTag(use.getValue());
									stmt.addTag(rbTag);
									if(verbose){
										System.out.println("RB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(getVariantFromValue(use.getValue(),method)));
									}
								}
								Variant variant = getVariantFromValue(use.getValue(),method);
								allvariantlist.add(variant);
							}else{
								PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
								if(prbTag!=null){
									prbTag.addBindingValue(use.getValue());
									if(verbose){
										System.out.println("PRB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"]");
									}
								}else{                                                                 
									prbTag = new PRBTag(use.getValue());
									stmt.addTag(prbTag);
									if(verbose){
										System.out.println("PRB值:[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+use.getValue().toString()+"] for stmt:["+stmt+"] ");
									}
								}
								// 将这个值加入到部分未绑定序列
								methodToParitalUnbindValues.get(method).add(use.getValue());
								if(!containPRBValue)
									containPRBValue = true;
							}
						}
						if(containPRBValue && defVars.isEmpty()){
							// 将这个部分未绑定的cfgnode加入到列表中
							PRBAnalysisStack.push(node);
						}
						RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
						// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
						// 检查一下 多少个variant绑定在上面
						for(Variable def:defVars){
							rbTag.addBindingValue(def.getValue());
							//将为左侧的定义 加入到绑定中
							if(!methodToUnbindValues.get(method).contains(def.getValue())){
								methodToUnbindValues.get(method).add(def.getValue());
							}
							if(verbose){
								System.out.println("[Class: "+method.getDeclaringClass().getName()+"\tMethod:"+method.getDeclaringClass().getName()+"\t]Value:["+def.getValue().toString()+"] for stmt:["+stmt+"] with variant:"+getVariantListIds(allvariantlist));
							}
							/*
							 * 由于使用的已经加入到variant之中 只需要
							 * 将定义的变量加入到variant之中
							 */
							for(Variant variant:allvariantlist){
								variant.addPaddingValue(def.getValue());
							}
						}
						// 将当前语句加入到variant的绑定中
						for(Variant variant:allvariantlist){
							variant.addBindingStmts(stmt);
						}
					}
					else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && defVars.isEmpty()){
						// 使用了prb值 但是没有赋值内容
						for(Variable use:useVars){
							// 将这个部分未绑定的值 加入到PRBValue列表中
							PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
							if(methodToParitalUnbindValues.get(method).contains(use.getValue())){
								continue;
							}else{
								methodToParitalUnbindValues.get(method).add(use.getValue());
							}
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
					else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
						// 使用了prb值 但是是有赋值内容
						List<Stmt>partialBindingStmt = new LinkedList<Stmt>();
						List<Value>partialBindingValues = new LinkedList<Value>();
						while(PRBAnalysisStack!=null){
							CFGNode prbnode = PRBAnalysisStack.pop();
							Stmt prbstmt = prbnode.getStmt();
							PRBTag prpTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
							partialBindingStmt.add(prbstmt);
							partialBindingValues.addAll(prpTag.getBindingValues());
							if(PRBAnalysisStack.size()==0){
								//当前为最后一个节点
								// 1. 找到所需要放置的variant
								// 2. 将所有stmt 放入到variant中
								// 3. 将所有value 放入到variant中
								RBTag rpTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
								Set<Value>bindingvalues = rpTag.getBindingValues();
								List<Variant>bindingvaraints = getVariantFromValue(bindingvalues,method);
								for(Variant variant:bindingvaraints){
									variant.addPaddingValue(partialBindingValues);
									variant.addBindingStmts(partialBindingStmt);
								}
							}
						}
						PRBAnalysisStack.clear();
					}
					
				}
				
			}
			
		}
		// DEBUG
		System.out.println("Num. of variants without combine:"+variants.size());
		// FINISH
	}
	
	
	
	// 判断两个集合是否有交集
	private boolean usedOverlap(List<Variable> useVars, List<Value> list) {
		// TODO Auto-generated method stub
		for(Variable use:useVars){
			if(list.contains(use.getValue()))
				return true;
		}
		return false;
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
	public String getVariantListIds(Variant variant){
		String varaintIds = "variant:["+variants.indexOf(variant)+"]";
		return varaintIds;
	}
}
