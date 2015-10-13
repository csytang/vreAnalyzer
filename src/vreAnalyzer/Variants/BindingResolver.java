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
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;
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
    // 对于每一个函数来讲 有以下两个
    // 1. unbindValueList 未绑定值列表
    private Map<SootMethod,List<Value>> methodToUnbindValues = new HashMap<SootMethod,List<Value>>();
    // 2. partialUnbindValueList 部分未绑定值列表
    private Map<SootMethod,List<Value>> methodToParitalUnbindValues = new HashMap<SootMethod,List<Value>>();
    // Map 从SootMethod到 variant
    private Map<SootMethod,List<Variant>> methodToVariants = new HashMap<SootMethod,List<Variant>>();
    // 临时的variant 列表包括所有的method中的variant 
    private Map<SootMethod,List<Variant>> methodToVariantsTmp = new HashMap<SootMethod,List<Variant>>();
    
    // Map 从SootMethod 到 valueToVariant
    private Map<SootMethod,ValueToVariant>methodToValueToVariant = new HashMap<SootMethod,ValueToVariant>();
   
    
    public BindingResolver(){
    	methodToArgsList = new HashMap<SootMethod,List<Args>>();
    	allAppMethod = new LinkedList<SootMethod>();
    }
	public static BindingResolver inst(){
		if(instance==null)
			instance = new BindingResolver();
		return instance;
	}
	public void run(){
		parse();
		singlecolorannotation(Color.RED);
	}
	
	private void parse(){
		allAppMethod.addAll(ProgramFlowBuilder.inst().getAppConcreteMethods());
		// 定义的变量
		List<Variable> defVars = null;
		// 使用的变量
		List<Variable> useVars = null;
		
		for(SootMethod method:allAppMethod){
			MethodTag mTag = (MethodTag) method.getTag(MethodTag.TAG_NAME);
			List<CallSite> callsites = mTag.getAllCallSites();
			
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
			if(verbose)
				System.out.println("Currently process method:"+method.getName());
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode>nodes = cfg.getNodes();

			// 2.1 The method is invoked by other method use callers' parameters
			for(CFGNode node:nodes){
				if(node.isSpecial())
					continue;
				//////////////////////
				NodeDefUses defusenode = (NodeDefUses) node;
				Stmt stmt = defusenode.getStmt();
				if(verbose)
					System.out.println("Currently process statement:"+stmt.toString());
				useVars = defusenode.getUsedVars();// 在当前语句中的使用
				defVars = defusenode.getDefinedVars();// 当前语句中的定义
				///////////////////////
				if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
					isParaAssignStmt = true;
				}else{
					isParaAssignStmt = false;
				}
				/////////////如果这个语句是域赋值给local语句////////////////////////////
				if(isParaAssignStmt){
					DefinitionStmt defstmt = (DefinitionStmt) stmt;
					Value argu = defstmt.getLeftOp();
					RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
					if(rbTag!=null){
						rbTag.addBindingValue(argu);		
					}else{
						rbTag = new RBTag(argu);
						stmt.addTag(rbTag);
					}
					// 将这个值加入到methodToUnbindValues
					if(!methodToUnbindValues.get(method).contains(argu)){
						methodToUnbindValues.get(method).add(argu);
					}
					if(verbose){
						System.out.println("Add RBTag to stmt:"+stmt);
						System.out.println("----Value:"+argu.toString());
					}
					continue;
				}
				////////////////////////////////////////////////////////
				// P1 检查这个点下的所有unbindingValueList
				////////////////////////////////////////////////////////
				///////////////////如果此语句中有未绑定内容调用///////////////////////
				if(usedOverlap(useVars,methodToUnbindValues.get(method))){
					// 存在RB内容
					boolean containPRBValue = false;// 是否包含部分帮定值
					// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
					for(Variable use:useVars){
						if(methodToUnbindValues.get(method).contains(use.getValue())){
							// 如果包含则为未绑定语句
							RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
							if(rbTag!=null){
								rbTag.addBindingValue(use.getValue());
							}else{
								rbTag = new RBTag(use.getValue());
								stmt.addTag(rbTag);
							}
							if(verbose){
								System.out.println("Add RBTag to stmt:"+stmt);
								System.out.println("----Value:"+use.getValue().toString());
							}
						}else{
							PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
							if(prbTag!=null){
								prbTag.addBindingValue(use.getValue());					
							}else{                                                                 
								prbTag = new PRBTag(use.getValue());
								stmt.addTag(prbTag);
							}
							// 将这个值加入到部分未绑定序列
							methodToParitalUnbindValues.get(method).add(use.getValue());
							if(!containPRBValue)
								containPRBValue = true;
						}
					}
					if(containPRBValue){
						if(defVars.isEmpty())// 将这个部分未绑定的cfgnode加入到列表中
							PRBAnalysisStack.push(node);
						else{
							// 并且LOP是一个真正的local
							boolean containsLocal = false;
							for(Variable def:defVars){
								if(def.isLocal()){
									containsLocal = true;
									break;
								}
							}
							if(!containsLocal){
								PRBAnalysisStack.push(node);
							}
						}
					}
					RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
					
					// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
					// 检查一下 多少个variant绑定在上面
					for(Variable def:defVars){// def is local
						rbTag.addBindingValue(def.getValue());
						//将为左侧的定义 加入到绑定中
						if(!methodToUnbindValues.get(method).contains(def.getValue())){
							methodToUnbindValues.get(method).add(def.getValue());
							if(verbose){
								System.out.println("Add RBTag to stmt:"+stmt);
								System.out.println("----Value:"+def.getValue().toString());
							}
						}
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
						}else{
							prbTag = new PRBTag(use.getValue());
							stmt.addTag(prbTag);
						}
					}
					PRBAnalysisStack.push(node);
				}
				else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
					// 使用了prb值 但是是有赋值内容
					// 所有的prb值 和在PRBAnalysisStack中的prb值需要指向这个值
					// 首先将这个stmt加入
					
					for(Variable use:useVars){
						RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
						if(rbTag!=null){
							rbTag.addBindingValue(use.getValue());
						}else{
							rbTag = new RBTag(use.getValue());
							stmt.addTag(rbTag);
						}
						if(verbose){
							System.out.println("Add RBTag to stmt:"+stmt);
							System.out.println("----Value:"+use.getValue().toString());
						}
						if(!methodToUnbindValues.get(method).contains(use.getValue())){
							methodToUnbindValues.get(method).add(use.getValue());
						}
					}
					// 加入所有的定义defs
					for(Variable def:defVars){// def is local
						RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
						rbTag.addBindingValue(def.getValue());
						if(!methodToUnbindValues.get(method).contains(def.getValue())){
							methodToUnbindValues.get(method).add(def.getValue());
						}
					}
					
					int stacklength = PRBAnalysisStack.size();
					CFGNode lastnode = PRBAnalysisStack.get(stacklength-1);
					Stmt laststmt = lastnode.getStmt();
					RBTag lastrbTag = (RBTag)laststmt.getTag(RBTag.TAG_NAME);
					Set<Value>lastbindvalues = lastrbTag.getBindingValues();
					
					while(!PRBAnalysisStack.isEmpty()){
						CFGNode prbnode = PRBAnalysisStack.pop();
						Stmt prbstmt = prbnode.getStmt();
						/*
						 * 删除stmt上绑定的PRBTag 加入新的RBTag
						 * 在新加入的RBTag上绑定 lastnode上的values 
						 */
						PRBTag prbTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
						Set<Value> bindingvalues = prbTag.getBindingValues();
						// 从partial unbind value中移除
						for(Value value:bindingvalues){
							if(methodToParitalUnbindValues.get(method).contains(value))
								methodToParitalUnbindValues.get(method).remove(value);
						}
						RBTag rbTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
						rbTag.addBindingValue(bindingvalues);
						rbTag.addBindingValue(lastbindvalues);
						prbstmt.removeTag(PRBTag.TAG_NAME);
						// 将这个Tag取代 PRBTag加入到stmt上
						prbstmt.addTag(rbTag);
						// 加入到unbindvalue 中
						methodToUnbindValues.get(method).addAll(bindingvalues);
						if(verbose){
							System.out.println("Add RBTag to stmt:"+prbstmt);
							
							String valuesString = "";
							for(Value value:bindingvalues){
								valuesString+=value;
								valuesString+=":";
							}
							if(!bindingvalues.isEmpty()){
								valuesString.subSequence(0, valuesString.length()-1);
							}
							System.out.println("----Value:"+valuesString);
							
						}
					}
					PRBAnalysisStack.clear();
				}
				//////////////////////////////////////////////////
				//如果存在函数 调用那么则将函数调用放入到methodToArgsList中
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
		//实参 形参对应列表
		Map<Value,Value>localParameterToRemoteArgu = new HashMap<Value,Value>();
		for(SootMethod method:calleeMethod){
			// clean the analysis stack
			PRBAnalysisStack.clear();
			PRBAnalysisStackBindingValue.clear();
			if(verbose)
				System.out.println("Currently process method:"+method.getName());
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
					// SootMethod callerMethod = argument.getCallerMethod();
					localParameterToRemoteArgu.clear();
					
					for(CFGNode node:nodes){
						NodeDefUses defusenode = (NodeDefUses)node;
						if(node.isSpecial())
							continue;
						Stmt stmt = defusenode.getStmt();//$r7 := @caughtexception
						if(verbose)
							System.out.println("currently process statement:"+stmt);
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
									// 将对应加入到list中
									localParameterToRemoteArgu.put(argu, remote);
									if(rbTag!=null){
										// 将这个argument的赋值语句 加入相应的argument值作为绑定值
										rbTag.addBindingValue(argu);
										// 将remote的value 加入绑定值中
										rbTag.addBindingValue(remote);
									}else{
										rbTag = new RBTag(argu);
										// 将remote的value 加入绑定值中
										rbTag.addBindingValue(remote);
										stmt.addTag(rbTag);
									}
									if(verbose){
										System.out.println("Add RBTag to stmt:"+stmt);
										System.out.println("----Value:"+argu.toString());
									}
									// 将这个值加入到methodToUnbindValues
									if(!methodToUnbindValues.get(method).contains(argu)){
										methodToUnbindValues.get(method).add(argu);
									}
									continue;
								}else{
									//当前的参数是 fixed 没有必要处理
									continue;
								}
							}
						}
						
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
										
									}else{
										rbTag = new RBTag(use.getValue());
										stmt.addTag(rbTag);										
									}
									// 如果在这个语句上使用argument 那么
									if(localParameterToRemoteArgu.containsKey(use.getValue())){
										rbTag.addBindingValue(localParameterToRemoteArgu.get(use.getValue()));
									}
									if(verbose){
										System.out.println("Add RBTag to stmt:"+stmt);
										System.out.println("----Value:"+use.getValue().toString());
									}
								}else{// 只有在未定义中可以加入为 PRB值
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
							if(containPRBValue){
								if(defVars.isEmpty())// 将这个部分未绑定的cfgnode加入到列表中
									PRBAnalysisStack.push(node);
								else{
									// 并且LOP是一个真正的local
									boolean containsLocal = false;
									for(Variable def:defVars){
										if(def.isLocal()){
											containsLocal = true;
											break;
										}
									}
									if(!containsLocal){
										PRBAnalysisStack.push(node);
									}
								}
							}
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
							// 检查一下 多少个variant绑定在上面
							for(Variable def:defVars){
								rbTag.addBindingValue(def.getValue());
								//将为左侧的定义 加入到绑定中
								if(!methodToUnbindValues.get(method).contains(def.getValue())){
									methodToUnbindValues.get(method).add(def.getValue());
									if(verbose){
										System.out.println("Add RBTag to stmt:"+stmt);
										System.out.println("----Value:"+def.getValue().toString());
									}
								}							
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
							// 所有的prb值 和在PRBAnalysisStack中的prb值需要指向这个值
							// 首先将这个stmt加入
							
							for(Variable use:useVars){
								RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
								if(rbTag!=null){
									rbTag.addBindingValue(use.getValue());
								}else{
									rbTag = new RBTag(use.getValue());
									stmt.addTag(rbTag);
								}
								if(verbose){
									System.out.println("Add RBTag to stmt:"+stmt);
									System.out.println("----Value:"+use.getValue().toString());
								}
								if(!methodToUnbindValues.get(method).contains(use.getValue())){
									methodToUnbindValues.get(method).add(use.getValue());
								}
							}
							// 加入所有的定义defs
							for(Variable def:defVars){// def is local
								RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
								rbTag.addBindingValue(def.getValue());
								if(!methodToUnbindValues.get(method).contains(def.getValue())){
									methodToUnbindValues.get(method).add(def.getValue());
								}
							}
							
							int stacklength = PRBAnalysisStack.size();
							CFGNode lastnode = PRBAnalysisStack.get(stacklength-1);
							Stmt laststmt = lastnode.getStmt();
							RBTag lastrbTag = (RBTag)laststmt.getTag(RBTag.TAG_NAME);
							Set<Value>lastbindvalues = lastrbTag.getBindingValues();
							
							while(!PRBAnalysisStack.isEmpty()){
								CFGNode prbnode = PRBAnalysisStack.pop();
								Stmt prbstmt = prbnode.getStmt();
								/*
								 * 删除stmt上绑定的PRBTag 加入新的RBTag
								 * 在新加入的RBTag上绑定 lastnode上的values 
								 */
								PRBTag prbTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
								Set<Value> bindingvalues = prbTag.getBindingValues();
								// 从partial unbind value中移除
								for(Value value:bindingvalues){
									if(methodToParitalUnbindValues.get(method).contains(value))
										methodToParitalUnbindValues.get(method).remove(value);
								}
								RBTag rbTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
								rbTag.addBindingValue(bindingvalues);
								rbTag.addBindingValue(lastbindvalues);
								prbstmt.removeTag(PRBTag.TAG_NAME);
								// 将这个Tag取代 PRBTag加入到stmt上
								prbstmt.addTag(rbTag);
								// 加入到unbindvalue 中
								methodToUnbindValues.get(method).addAll(bindingvalues);
								if(verbose){
									System.out.println("Add RBTag to stmt:"+prbstmt);
									
									String valuesString = "";
									for(Value value:bindingvalues){
										valuesString+=value;
										valuesString+=":";
									}
									if(!bindingvalues.isEmpty()){
										valuesString.subSequence(0, valuesString.length()-1);
									}
									System.out.println("----Value:"+valuesString);
									
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
					NodeDefUses defusenode = (NodeDefUses) node;
					Stmt stmt = defusenode.getStmt();
					useVars = defusenode.getUsedVars();// 在当前语句中的使用
					defVars = defusenode.getDefinedVars();// 当前语句中的定义
					///////////////////////
					if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
						isParaAssignStmt = true;
					}else{
						isParaAssignStmt = false;
					}
					/////////////如果这个语句是域赋值给local语句////////////////////////////
					if(isParaAssignStmt){
						DefinitionStmt defstmt = (DefinitionStmt) stmt;
						Value argu = defstmt.getLeftOp();
						RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
						if(rbTag!=null){
							rbTag.addBindingValue(argu);		
						}else{
							rbTag = new RBTag(argu);
							stmt.addTag(rbTag);
						}
						// 将这个值加入到methodToUnbindValues
						if(!methodToUnbindValues.get(method).contains(argu)){
							methodToUnbindValues.get(method).add(argu);
						}
						if(verbose){
							System.out.println("Add RBTag to stmt:"+stmt);
							System.out.println("----Value:"+argu.toString());
						}
						continue;
					}
					///////////////////如果此语句中有未绑定内容调用///////////////////////
					if(usedOverlap(useVars,methodToUnbindValues.get(method))){
						// 存在RB内容
						boolean containPRBValue = false;// 是否包含部分帮定值
						// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
						for(Variable use:useVars){
							if(methodToUnbindValues.get(method).contains(use.getValue())){
								// 如果包含则为未绑定语句
								RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
								if(rbTag!=null){
									rbTag.addBindingValue(use.getValue());
								}else{
									rbTag = new RBTag(use.getValue());
									stmt.addTag(rbTag);
								}
								if(verbose){
									System.out.println("Add a RBTag for stmt:"+stmt);
									System.out.println("----Value:"+use.getValue().toString());
								}
							}else{// 只有在未定义中可以加入为 PRB值
								PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
								if(prbTag!=null){
									prbTag.addBindingValue(use.getValue());					
								}else{                                                                 
									prbTag = new PRBTag(use.getValue());
									stmt.addTag(prbTag);
								}
								// 将这个值加入到部分未绑定序列
								methodToParitalUnbindValues.get(method).add(use.getValue());
								if(!containPRBValue)
									containPRBValue = true;
							}
						}
						if(containPRBValue){
							if(defVars.isEmpty())// 将这个部分未绑定的cfgnode加入到列表中
								PRBAnalysisStack.push(node);
							else{
								// 并且LOP是一个真正的local
								boolean containsLocal = false;
								for(Variable def:defVars){
									if(def.isLocal()){
										containsLocal = true;
										break;
									}
								}
								if(!containsLocal){
									PRBAnalysisStack.push(node);
								}
							}
						}
						RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
						
						// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
						// 检查一下 多少个variant绑定在上面
						for(Variable def:defVars){
							rbTag.addBindingValue(def.getValue());
							//将为左侧的定义 加入到绑定中
							if(!methodToUnbindValues.get(method).contains(def.getValue())){
								methodToUnbindValues.get(method).add(def.getValue());
							}
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
							}else{
								prbTag = new PRBTag(use.getValue());
								stmt.addTag(prbTag);
							}
						}
						PRBAnalysisStack.push(node);
					}
					else if(usedOverlap(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
						// 使用了prb值 但是是有赋值内容
						// 所有的prb值 和在PRBAnalysisStack中的prb值需要指向这个值
						// 首先将这个stmt加入
						
						for(Variable use:useVars){
							RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
							if(rbTag!=null){
								rbTag.addBindingValue(use.getValue());
							}else{
								rbTag = new RBTag(use.getValue());
								stmt.addTag(rbTag);
							}
							if(verbose){
								System.out.println("Add RBTag to stmt:"+stmt);
								System.out.println("----Value:"+use.getValue().toString());
							}
							if(!methodToUnbindValues.get(method).contains(use.getValue())){
								methodToUnbindValues.get(method).add(use.getValue());
							}
						}
						// 加入所有的定义defs
						for(Variable def:defVars){// def is local
							RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
							rbTag.addBindingValue(def.getValue());
							if(!methodToUnbindValues.get(method).contains(def.getValue())){
								methodToUnbindValues.get(method).add(def.getValue());
							}
						}
						
						int stacklength = PRBAnalysisStack.size();
						CFGNode lastnode = PRBAnalysisStack.get(stacklength-1);
						Stmt laststmt = lastnode.getStmt();
						RBTag lastrbTag = (RBTag)laststmt.getTag(RBTag.TAG_NAME);
						Set<Value>lastbindvalues = lastrbTag.getBindingValues();
						
						while(!PRBAnalysisStack.isEmpty()){
							CFGNode prbnode = PRBAnalysisStack.pop();
							Stmt prbstmt = prbnode.getStmt();
							/*
							 * 删除stmt上绑定的PRBTag 加入新的RBTag
							 * 在新加入的RBTag上绑定 lastnode上的values 
							 */
							PRBTag prbTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
							Set<Value> bindingvalues = prbTag.getBindingValues();
							// 从partial unbind value中移除
							for(Value value:bindingvalues){
								if(methodToParitalUnbindValues.get(method).contains(value))
									methodToParitalUnbindValues.get(method).remove(value);
							}
							RBTag rbTag = (RBTag)prbstmt.getTag(RBTag.TAG_NAME);
							rbTag.addBindingValue(bindingvalues);
							rbTag.addBindingValue(lastbindvalues);
							prbstmt.removeTag(PRBTag.TAG_NAME);
							// 将这个Tag取代 PRBTag加入到stmt上
							prbstmt.addTag(rbTag);
							// 加入到unbindvalue 中
							methodToUnbindValues.get(method).addAll(bindingvalues);
							if(verbose){
								System.out.println("Add RBTag to stmt:"+prbstmt);
								
								String valuesString = "";
								for(Value value:bindingvalues){
									valuesString+=value;
									valuesString+=":";
								}
								if(!bindingvalues.isEmpty()){
									valuesString.subSequence(0, valuesString.length()-1);
								}
								System.out.println("----Value:"+valuesString);
								
							}
						}
						PRBAnalysisStack.clear();
					}
			
				}
				
			}
			
		}
		
	}
	
	public void RBTagCollector(){
		/*
		 * 遍历所有的caller函数
		 */
		methodToUnbindValues.clear();
		for(SootMethod method:allAppMethod){
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode>nodes = cfg.getNodes();
			// 初始化methodToVariant 让每一个函数对应一系列variants
			if(!methodToVariants.containsKey(method)){
				methodToVariants.put(method, new LinkedList<Variant>());
			}
			if(!methodToVariantsTmp.containsKey(method)){
				methodToVariantsTmp.put(method, new LinkedList<Variant>());
			}
			if(!methodToValueToVariant.containsKey(method)){
				methodToValueToVariant.put(method, new ValueToVariant());
			}
			ValueToVariant mvalueToVariant = methodToValueToVariant.get(method);
			for(CFGNode node:nodes){
				if(node.isSpecial())
					continue;
				NodeDefUses defusenode = (NodeDefUses) node;
				Stmt stmt = defusenode.getStmt();
				// 获得RBTag
				RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
				if(rbTag!=null){
					// 获得绑定的值
					Set<Value>bindingvalues = rbTag.getBindingValues();
					if(!bindingvalues.isEmpty()){
						//判读是否存在这个value
						for(Value bindv:bindingvalues){
							// 如果存在 在那个一个variant
							if(methodToUnbindValues.get(method).contains(bindv)){
								// 将这个语句加入到 那个variant之中
								if(mvalueToVariant.containsValue(bindv)){
									// 
									List<Variant>variantlist = mvalueToVariant.getVariantsByValue(bindv);
									for(Variant variant:variantlist){
										variant.addBindingStmts(stmt);
										variant.addPaddingValue(bindv);
									}
								}else{
									// 创建一个Variant 根据这个值
									Variant variant = new Variant(bindv,stmt);
									mvalueToVariant.addValueToVariant(bindv, variant);
									methodToVariantsTmp.get(method).add(variant);
								}
								
							}
						}
					}
				}
			}
			//DEBUG
			if(verbose)
				System.out.println("Current number of variants is:"+methodToVariantsTmp.get(method).size());
			//FINISH
			System.out.println("Now refining variants");
			/*
			 * 开始的stmt的是同一个 就全部绑定一起
			 * 1. 任意两个variant 
			 */
			List<Variant>variantsListTmp = new ArrayList<Variant>(methodToVariantsTmp.get(method));
			boolean isRepeat = false;
			for(int i = 0;i < variantsListTmp.size();i++){
				for(int j = i+1;j < variantsListTmp.size();j++){
					Variant varianti = variantsListTmp.get(i);
					Variant variantj = variantsListTmp.get(j);
					if(varianti.getFirstBindStmt().equals(variantj.getFirstBindStmt())){
						isRepeat = true;
					}else
						isRepeat = false;
				}
				/*
				 * 添加到永久只在i层
				 */
				// 如果是重复 这将i层variant 加入
				if(isRepeat){
					
				}else{
					
				}
			}
			
			
			
		}
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
	private boolean usedOverlap(Set<Value>vaset_A,Set<Value>vaset_B){
		Set<Value>intersection = new HashSet<Value>(vaset_A);
		intersection.retainAll(vaset_B);
		if(intersection.isEmpty())
			return false;
		else
			return true;
	}
	private void singlecolorannotation(Color color){
		/*
		 * 遍历所有的varaint
		 */
		boolean startFromSource;
		int[][]positions = null;
	    int[]lines = null;
		for(SootMethod method:allAppMethod){
			CFGDefUse cfg = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode>nodes = cfg.getNodes();
			SootClass cls = method.getDeclaringClass();
			List<Stmt>annotateStmt = new LinkedList<Stmt>();
			File sourceFile = SourceClassBinding.getSourceFileFromClassName(cls.toString());
			if(sourceFile==null){
				System.err.println("Cannot fine the class:\t"+cls.toString());
				continue;
			}
			String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
			htmlfileName+=".html";
			File htmlFile = new File(htmlfileName);
			for(CFGNode node:nodes){
				if(node.isSpecial()){
					continue;
				}
				Stmt stmt = node.getStmt();
				RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
				if(rbTag!=null){
					annotateStmt.add(stmt);
				}
			}
			// 将这个函数上色
			startFromSource = vreAnalyzerCommandLine.isStartFromSource();
			if(startFromSource) {
	            positions = new int[annotateStmt.size()][4];
	        }else{
	            lines = new int[annotateStmt.size()];
	        }
	        for(int i = 0;i < annotateStmt.size();i++) {
	        	Stmt stmt = annotateStmt.get(i);
	            SourceLocationTag slcTag = (SourceLocationTag) stmt.getTag(SourceLocationTag.TAG_NAME);
	            if (startFromSource) {
	                int startline = slcTag.getStartLineNumber();
	                int startcolumn = slcTag.getStartPos();
	                int endline = slcTag.getEndLineNumber();
	                int endcolumn = slcTag.getEndPos();
	                positions[i][0] = startline;
	                positions[i][1] = startcolumn;
	                positions[i][2] = endline;
	                positions[i][3] = endcolumn;
	            }else {
	                int startline = slcTag.getStartLineNumber();
	                lines[i] = startline;
	            }
	            if(startFromSource) {
	                HTMLAnnotation.annotatemultipleLinesingleColor(htmlFile, lines, color, MainFrame.inst().getHTMLToJava());
	            }else{
	            	 HTMLAnnotation.annotatemultipleLinesingleColor(htmlFile, lines, color, MainFrame.inst().getHTMLToJava());
	            }
	        }
	        
	        
		}
	}
	
}
