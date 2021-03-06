package vreAnalyzer.Variants;
import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThisRef;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.UI.TreeCellRender;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

import java.io.File;
import java.util.*;

import javax.swing.JTree;

public class BindingResolver {

	public static BindingResolver instance = null;
	private Map<SootMethod,List<Args>> methodToArgsList = null;
    
    private boolean verbose = true;
    private List<SootMethod> allAppMethod = null;
    // 包含函数调用的函数
    protected List<SootMethod> callerMethod = null;
    // 不包含任何函数调用的函数
    protected List<SootMethod> calleeMethod = null;
  
    // 将部分未绑定(PRBTag)的Stack
    private Stack<CFGNode> PRBAnalysisStack = new Stack<CFGNode>();
    
    // 对于每一个函数来讲 有以下两个
    // 1. unbindValueList 未绑定值列表
    private Map<SootMethod,Set<Value>> methodToUnbindValues = new HashMap<SootMethod,Set<Value>>();
    // 2. partialUnbindValueList 部分未绑定值列表
    private Map<SootMethod,Set<Value>> methodToParitalUnbindValues = new HashMap<SootMethod,Set<Value>>();
    
    // Map 从SootMethod 到 valueToVariant
    private Map<SootMethod,List<ValueToVariant>> methodToValueToVariant = new HashMap<SootMethod,List<ValueToVariant>>();
    
    // SootMethod 到Variants 的 对应
    private Map<SootMethod,List<Variant>> methodToVariants = new HashMap<SootMethod,List<Variant>>();

    private boolean containPRBValue = false;//是否包含部分 未绑定值
    // 包含所有的Varaint
    private List<Variant> fullVariantList = new LinkedList<Variant>();
    
    // 包含所有的callsite
    private Map<SootMethod,List<CallSite>> callerMethodToCallSite = new HashMap<SootMethod,List<CallSite>>();
    private Map<SootMethod,Set<CallSite>> calleeMethodToCallSite = new HashMap<SootMethod,Set<CallSite>>();
    
    
    // 设置一个Map从variantId 到block Ids的对应
    private Map<Integer,Set<Integer>> variantIdToblockIds = new HashMap<Integer,Set<Integer>>();
    
    // 设置SootMethod 到 ConditionCheck的对应
    private Map<SootMethod,ConditionCheck> methodToConditionCheck = new HashMap<SootMethod,ConditionCheck>();
    
    
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
		// 分析程序 并创建variant
		parse();
		// 出去不可见的Variant
		variantColorAssign();
		variantcolorannotation();
		// 删除不需要的Variant
		removeHiddenVariant(VariantAnnotate.getShouldBeHideVariants());
		VariantColorMap.inst().addToLegend();
		VariantAnnotate.setvariantready();
		// 加入表格和文件中中
		VariantToFile.inst().startWrite();
		VariantToTable.inst().addVariantToTable(fullVariantList);
		VariantToFile.inst().endWrite();
	}
	
	public void parse(){
		int variantId = 1;
		allAppMethod.addAll(ProgramFlowBuilder.inst().getAppConcreteMethods());
		// 定义的变量
		List<Variable> defVars = null;
		
		// 使用的变量
		List<Variable> useVars = null;
		
		callerMethod = new LinkedList<SootMethod>();
		boolean containappmethodcallee = false;
		for(SootMethod method:allAppMethod){
			MethodTag mTag = (MethodTag) method.getTag(MethodTag.TAG_NAME);
			List<CallSite> callsites = mTag.getAllCallSites();
			
			
			// 初始化methodToUnbindValue
			methodToUnbindValues.put(method, new HashSet<Value>());
			// 初始化methodToParitalUnbindValues
			methodToParitalUnbindValues.put(method, new HashSet<Value>());
			// 初始化methodToValueToVariant
			methodToValueToVariant.put(method, new LinkedList<ValueToVariant>());
			// 初始化methodToVariant
			methodToVariants.put(method, new LinkedList<Variant>());
			containappmethodcallee = false;
			// 如果存在callsite列表 加入methodToCallSites 中
			if(!callsites.isEmpty()){
				callerMethodToCallSite.put(method, callsites);
			}
			
			
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
					if(allAppMethod.contains(callee)){
						if(calleeMethodToCallSite.containsKey(callee)){
							calleeMethodToCallSite.get(callee).add(site);
						}else{
							Set<CallSite> callsiteSet = new HashSet<CallSite>();
							callsiteSet.add(site);
							calleeMethodToCallSite.put(callee, callsiteSet);
						}
					}
					
					
					
					if(verbose){
						System.out.println("Add a method and args bind: callee method["+callee.getName()+"] with input parameters "
								+ "["+argsString+"] in caller method["+method.getName()+"]");
					}
					// FINISH
					Args ar = new Args(method,callee,site,args);
					// add this args to callee into record
					if(methodToArgsList.containsKey(callee)){
						methodToArgsList.get(callee).add(ar);
					}else{						
						List<Args>argsList = new LinkedList<Args>();
						argsList.add(ar);
						methodToArgsList.put(callee, argsList);
					}
					if(allAppMethod.contains(callee)){
						if(containappmethodcallee==false){
							containappmethodcallee = true;
						}
					}
				}
				
				
			}
			if(!callsites.isEmpty() && containappmethodcallee){
				callerMethod.add(method);
			}
			
		}
		
		/////////////////////////////
		calleeMethod = new LinkedList<SootMethod>();
		calleeMethod.addAll(methodToArgsList.keySet());
		
		// ValueToVariant 对应检查
		boolean isParaAssignStmt = false;
		
		for(SootMethod method:callerMethod) {
			PRBAnalysisStack.clear();
			if(verbose){
				System.out.println("当前处理函数: "+method.getName());
			}
			CFGDefUse cfg = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(method);
			List<CFGNode> nodes = cfg.getNodes();
			
			// 获得vtVariant
			ValueToVariant vtVariant = new ValueToVariant(null);
			methodToValueToVariant.get(method).add(vtVariant);
			
			// 判断是否第一个条件值被设定
			boolean isInitConditionvalueSet = true;
			ConditionCheck conditionCheck = new ConditionCheck(method,null);
			
			for(CFGNode node:nodes){
				if(node.isSpecial())
					continue;
				//////////////////////
				NodeDefUses defusenode = (NodeDefUses) node;
				
				Stmt stmt = defusenode.getStmt();
				/*
				 * 在caller 和 callee中判断是否
				 * 一个node是分支节点 如果是 看判断条件中的useVars 是否为unbind 值
				 * if z0 != 0 goto r8 = virtualinvoke r0.<org.apache.mahout.cf.taste.hadoop.als.DatasetSplitter: org.apache.hadoop.mapreduce.Job prepareJob(org.apache.hadoop.fs.Path,org.apache.hadoop.fs.Path,java.lang.Class,java.lang.Class,java.lang.Class,java.lang.Class,java.lang.Class)>(r4, r5, class "org/apache/hadoop/mapreduce/lib/input/SequenceFileInputFormat", class "org/apache/mahout/cf/taste/hadoop/als/DatasetSplitter$WritePrefsMapper", class "org/apache/hadoop/io/NullWritable", class "org/apache/hadoop/io/Text", class "org/apache/hadoop/mapreduce/lib/output/TextOutputFormat")
				 */
				if(defusenode.getSuccs().size()>1){					
					if(stmt instanceof IfStmt){
						IfStmt ifstmt = (IfStmt) stmt;
						Value conditionValue = ifstmt.getCondition();
						ConditionExpr coniExpr = (ConditionExpr) conditionValue;
						Value lOperator = coniExpr.getOp1();// 左侧操作符
						Value rOperator = coniExpr.getOp2();// 右側操作符
						String symbol = coniExpr.getSymbol();
						if(symbol.equals("=")){
							// 如果操作符是 ＝ 赋值
							// 左侧是定义 右侧是使用						
							if(vtVariant.containsValue(rOperator)){
								if(isInitConditionvalueSet){
									if(!methodToConditionCheck.containsKey(method)){
										conditionCheck.setInitialConditionCFGNode(node, null);
										conditionCheck.addInitialConditionValue(rOperator, null);
										methodToConditionCheck.put(method, conditionCheck);
									}
									isInitConditionvalueSet = false;
								}
								conditionCheck.addConditionValue(rOperator, null);
							}
							
						}else{
							// 其他--- 两侧都是使用
							if(vtVariant.containsValue(lOperator)){
								if(isInitConditionvalueSet){
									if(!methodToConditionCheck.containsKey(method)){
										conditionCheck.setInitialConditionCFGNode(node, null);
										conditionCheck.addInitialConditionValue(lOperator, null);
										methodToConditionCheck.put(method, conditionCheck);
									}
									isInitConditionvalueSet = false;
								}
								conditionCheck.addConditionValue(lOperator, null);
							}
							if(vtVariant.containsValue(rOperator)){
								if(isInitConditionvalueSet){
									if(!methodToConditionCheck.containsKey(method)){
										conditionCheck.setInitialConditionCFGNode(node, null);
										conditionCheck.addInitialConditionValue(rOperator, null);
										methodToConditionCheck.put(method, conditionCheck);
									}
									isInitConditionvalueSet = false;
								}
								conditionCheck.addConditionValue(rOperator, null);
							}
							
						}
					}else if(stmt instanceof SwitchStmt){
						SwitchStmt switchstmt = (SwitchStmt) stmt;
						Value keyValue = switchstmt.getKey();
						if(verbose){
							System.out.println("Key is:"+keyValue);
						}
						if(vtVariant.containsValue(keyValue)){
							if(isInitConditionvalueSet){
								if(!methodToConditionCheck.containsKey(method)){
									conditionCheck.setInitialConditionCFGNode(node, null);
									conditionCheck.addInitialConditionValue(keyValue, null);
									methodToConditionCheck.put(method, conditionCheck);
								}
								isInitConditionvalueSet = false;
							}
							conditionCheck.addConditionValue(keyValue, null);
						}
					}
				}
				
				if(verbose){
					System.out.println("Currently process statement:"+stmt.toString());
				}
				
				useVars = defusenode.getUsedVars();// 在当前语句中的使用
				defVars = defusenode.getDefinedVars();// 当前语句中的定义
				///////////////////////
				if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
					isParaAssignStmt = true;
				}else{
					isParaAssignStmt = false;
				}
				/////////////如果这个语句是域赋值给local语句////////////////////////////
				if(isParaAssignStmt) {
					DefinitionStmt defstmt = (DefinitionStmt) stmt;
					Value argu = defstmt.getLeftOp();
					RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
					if(rbTag!=null){
						// 加入本地bindingvalue 在所有的Caller函数中
						rbTag.addBindingValue(argu, null);		
					}else{
						rbTag = new RBTag(argu, false, null, null);
						stmt.addTag(rbTag);
					}
					// 将这个值加入到methodToUnbindValues
					if(!methodToUnbindValues.get(method).contains(argu)){
						methodToUnbindValues.get(method).add(argu);
					}
					if(verbose){
						System.out.println("line@189: Add RBTag to stmt:"+stmt);
						System.out.println("line@190: ----Value:"+argu.toString());
					}
					
					// 创建一个Variant
					Variant variant = new Variant(argu, stmt, null,method,variantId);
					
					// 加入到localToVariant表中
					vtVariant.addValueToVariant(argu, variant);
					
					
					variantId++;
					// 加入到fullVariant list中
					fullVariantList.add(variant);
					
					// 加入到SooMethod 到 Variant的对应
					methodToVariants.get(method).add(variant);
					
					continue;
				}
				////////////////////////////////////////////////////////
				// P1 检查这个点下的所有unbindingValueList
				////////////////////////////////////////////////////////
				///////////////////如果此语句中有未绑定内容调用///////////////////////
				if(usedOverlap_Variable(useVars,methodToUnbindValues.get(method))) {
					// 存在RB内容
					Set<Variant> usedVariantSet = new HashSet<Variant>();
					containPRBValue = false;// 是否包含部分帮定值
					// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
					for(Variable use:useVars) {
						if(methodToUnbindValues.get(method).contains(use.getValue())) {
							// 如果包含则为未绑定语句
							// 我们只将field 和 local加入
							if(!use.isLocal() && !use.isFieldRef()) {
								continue;
							}
							RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
							if(rbTag!=null){
								rbTag.addBindingValue(use.getValue(),null);
							}else{
								rbTag = new RBTag(use.getValue(),false,null,null);
								stmt.addTag(rbTag);
							}
							if(verbose){
								System.out.println("line@227: Add RBTag to stmt:"+stmt);
								System.out.println("line@227: ----Value:"+use.getValue().toString());
							}
							// 获得在这个语句中使用的 variant
							Set<Variant> useVariant = vtVariant.getVariantsByValue(use.getValue());
							// 将使用的 variant 加入本语句的Set中
							if(useVariant!=null)
								usedVariantSet.addAll(useVariant);
							
						}else{
							// 我们只将field 和 local加入
							if(!use.isLocal() && !use.isFieldRef()){
								continue;
							}
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
						//TODO 检查这个位置是否有经过
						PRBAnalysisStack.clear();					
						if(defVars.isEmpty()){// 将这个部分未绑定的cfgnode加入到列表中
							//---- 1. PRBAnalysisNode--------
							PRBAnalysisStack.push(node);
							if(verbose){
								System.out.println("向栈中加入底层节点: "+node.getStmt().toString());	
							}
						}
						else{
							// 并且LOP是一个真正的local
							boolean containsLocalField = false;
							for(Variable def:defVars){
								if(def.isLocal()||def.isFieldRef()){
									containsLocalField = true;
									break;
								}
							}
							if(!containsLocalField){
								PRBAnalysisStack.push(node);
								if(verbose){
									System.out.println("向栈中加入底层节点: "+node.getStmt().toString());
								}
							}else{
								
								PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
								Set<Value>punbindValue = prbTag.getBindingValues();
								for(Value value:punbindValue){
									methodToParitalUnbindValues.get(method).remove(value);
								}
								stmt.removeTag(PRBTag.TAG_NAME);
							}
						}
						containPRBValue = false;
					}
					RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
					
					// 所有的使用Variant而已
					/*
					 * 1. 加入binding 語句
					 * 2. 加入def的value
					 */
					for(Variant usedvariant:usedVariantSet){
						usedvariant.addBindingStmts(stmt, null);
					}
					
					// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
					// 检查一下 多少个variant绑定在上面
					for(Variable def:defVars){// def is local
						// 我们只将field 和 local加入
						if(!def.isLocal() && !def.isFieldRef()){
							continue;
						}
						rbTag.addBindingValue(def.getValue(),null);
						//将为左侧的定义 加入到绑定中
						if(!methodToUnbindValues.get(method).contains(def.getValue())){
							methodToUnbindValues.get(method).add(def.getValue());
							if(verbose){
								System.out.println("line@316: Add RBTag to stmt:"+stmt);
								System.out.println("line@317: ----Value:"+def.getValue().toString());
							}
						}
						// def的值加入到binding value中
						for(Variant usedvariant:usedVariantSet){
							usedvariant.addPaddingValue(def.getValue(), null);
						}
						// 加入vtovariant
						vtVariant.addValueToVariant(def.getValue(), usedVariantSet);
					}
					
				}
				else if(usedOverlap_Variable(useVars,methodToParitalUnbindValues.get(method)) && defVars.isEmpty()){
					// 使用了prb值 但是没有赋值内容
					for(Variable use:useVars){
						// 将这个部分未绑定的值 加入到PRBValue列表中
						// 我们只将field 和 local加入
						if(!use.isLocal() && !use.isFieldRef()){
							continue;
						}
						// 加入到 部分未绑定值
						PRBTag prbTag = (PRBTag) stmt.getTag(PRBTag.TAG_NAME);
						if(prbTag!=null){
							prbTag.addBindingValue(use.getValue());
						}else{
							prbTag = new PRBTag(use.getValue());
							stmt.addTag(prbTag);
						}
						if(methodToParitalUnbindValues.get(method).contains(use.getValue())){
							continue;
						}else{
							methodToParitalUnbindValues.get(method).add(use.getValue());
						}
					}
					if(!PRBAnalysisStack.isEmpty()){
						PRBAnalysisStack.push(node);
						if(verbose){
							System.out.println("向栈中节点: "+node.getStmt().toString());
						}
					}
				}
				else if(usedOverlap_Variable(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
					// 使用了prb值 但是是有赋值内容
					// 所有的prb值 和在PRBAnalysisStack中的prb值需要指向这个值
					// 首先将这个stmt加入
					
					for(Variable use:useVars){
						// 我们只将field 和 local加入
						if(!use.isLocal() && !use.isFieldRef()){
							continue;
						}
						RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
						if(rbTag!=null){
							rbTag.addBindingValue(use.getValue(),null);
						}else{
							rbTag = new RBTag(use.getValue(),false,null,null);
							stmt.addTag(rbTag);
						}
						if(verbose){
							System.out.println("line@376: Add RBTag to stmt:"+stmt);
							System.out.println("line@377: ----Value:"+use.getValue().toString());
						}
						if(!methodToUnbindValues.get(method).contains(use.getValue())){
							methodToUnbindValues.get(method).add(use.getValue());
						}
					}
					// 加入所有的定义defs
					for(Variable def:defVars){// def is local
						// 我们只将field 和 local加入
						if(!def.isLocal() && !def.isFieldRef()){
							continue;
						}
						RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
						rbTag.addBindingValue(def.getValue(),null);
						if(!methodToUnbindValues.get(method).contains(def.getValue())){
							methodToUnbindValues.get(method).add(def.getValue());
						}
					}
					
					int stacklength = PRBAnalysisStack.size();
					if(stacklength<1)
						continue;
					
					CFGNode lastnode = getStackElement(PRBAnalysisStack,stacklength-1);
					Stmt laststmt = lastnode.getStmt();
					if(verbose){
						System.out.println("The last statement is:"+laststmt);
						System.out.println("All CFGNodes in stack:");
						for(CFGNode prbnode:PRBAnalysisStack){
							System.out.println("Node:"+prbnode.getStmt());
						}
					}
					RBTag lastrbTag = (RBTag)laststmt.getTag(RBTag.TAG_NAME);
					if(lastrbTag==null){
						System.out.println("Error lastrbTag should not be null");
					}
					Set<Value>lastbindvalues = lastrbTag.getBindingValues(null);
					
					// 堆栈对应的Variant集合
					Set<Variant> variantset = new HashSet<Variant>();
					for(Value vi:lastbindvalues){
						Set<Variant> vivariantset = vtVariant.getVariantsByValue(vi);
						if(vivariantset!=null)
							variantset.addAll(vivariantset);
					}
					
					// variant 中加入当前语句 
					for(Variant variant:variantset){
						variant.addBindingStmts(stmt, null);
						for(Variable def:defVars) {
							variant.addPaddingValue(def.getValue(), null);
						}
						for(Variable use:useVars) {
							variant.addPaddingValue(use.getValue(), null);
						}
					}
					for(Variable def:defVars) {
						vtVariant.addValueToVariant(def.getValue(), variantset);
					}
					for(Variable use:useVars) {
						vtVariant.addValueToVariant(use.getValue(), variantset);
					}
					
					// variant 中加入之前的stack中的语句和值	
					while(!PRBAnalysisStack.isEmpty()){
						CFGNode prbnode = PRBAnalysisStack.pop();
						Stmt prbstmt = prbnode.getStmt();
						/*
						 * 删除stmt上绑定的PRBTag 加入新的RBTag
						 * 在新加入的RBTag上绑定 lastnode上的values 
						 */
						PRBTag prbTag = (PRBTag) prbstmt.getTag(PRBTag.TAG_NAME);
						if(prbTag==null){
							System.out.println("PRBTag 为空对于语句:"+prbstmt.toString());
						}
						Set<Value> bindingvalues = prbTag.getBindingValues();
						// 从partial unbind value中移除
						for(Value value:bindingvalues){
							if(methodToParitalUnbindValues.get(method).contains(value))
								methodToParitalUnbindValues.get(method).remove(value);
						}
						
						RBTag rbTag = new RBTag(bindingvalues,false,null,null);
						rbTag.addBindingValue(lastbindvalues,null);
						prbstmt.removeTag(PRBTag.TAG_NAME);
						// 将这个Tag取代 PRBTag加入到stmt上
						prbstmt.addTag(rbTag);
						// 加入到unbindvalue 中
						methodToUnbindValues.get(method).addAll(bindingvalues);
						if(verbose){
							System.out.println("line@467: Add RBTag to stmt:"+prbstmt);
							
							String valuesString = "";
							for(Value value:bindingvalues){
								valuesString+=value;
								valuesString+=":";
							}
							if(!bindingvalues.isEmpty()){
								valuesString.subSequence(0, valuesString.length()-1);
							}
							System.out.println("line@477: ----Value:"+valuesString);
							
						}
						// variant 中加入当前语句 
						for(Variant variant:variantset){
							variant.addBindingStmts(stmt, null);
							for(Value value:bindingvalues){
								variant.addPaddingValue(value, null);
							}
						}
						for(Value value:bindingvalues) {
							vtVariant.addValueToVariant(value, variantset);
						}
					}
					// 清空PRB栈 并将containPRB设置为false
					PRBAnalysisStack.clear();
					
				}
				//////////////////////////////////////////////////
				// 如果存在函数 调用那么则将函数调用放入到methodToArgsList中
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
		
		
		if(verbose)
			System.out.println("--------------------Finish caller method----------------------------");
		
		/////////////////////////////////////////////////////////
		//实参 形参对应列表
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		Map<Value,Value>localParameterToRemoteArgu = new HashMap<Value,Value>();
		for(SootMethod method:calleeMethod){
			// clean the analysis stack
			PRBAnalysisStack.clear();
			if(verbose)
				System.out.println("当前处理的函数: "+method.getName());
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			if(cfg==null)
				continue;
			Body body = method.retrieveActiveBody();
			List<Local> argLists = body.getParameterLocals(); // locals assigned with parameters
			List<CFGNode> nodes = cfg.getNodes();
			// 获得对应的实际参数列表
			List<Args> args = methodToArgsList.get(method);
			
			// 对于每一次调用进行一次运算
			if(args!=null){
				for(Args argument:args){
					// 调用函数
					// SootMethod callerMethod = argument.getCallerMethod();
					
					// 判断是否第一个条件值被设定
					boolean isInitConditionvalueSet = true;
					
					localParameterToRemoteArgu.clear();
					PRBAnalysisStack.clear();
					
					ValueToVariant callervtVariant = methodToValueToVariant.get(argument.getCallerMethod()).get(0);// ---- 调用函数的Value To Variant
					
					ValueToVariant localvtVariant = new ValueToVariant(argument.getCallSite());
					
					methodToValueToVariant.get(method).add(localvtVariant); // ---- 当前函数的Value To Variant
					
					
					for(CFGNode node:nodes){
						NodeDefUses defusenode = (NodeDefUses) node;
						if(node.isSpecial())
							continue;
						Stmt stmt = defusenode.getStmt(); // $r7 := @caughtexception
						if(verbose)
							System.out.println("处理当前的语句: "+stmt);
						useVars = defusenode.getUsedVars();
						defVars = defusenode.getDefinedVars();
						if(stmt instanceof IdentityStmt && !(((IdentityStmt) stmt).getRightOp() instanceof ThisRef)){
							isParaAssignStmt = true;
						}else{
							isParaAssignStmt = false;
						}
						Value argu = null;
						/*
						 * 在caller 和 callee中判断是否
						 * 一个node是分支节点 如果是 看判断条件中的useVars 是否为unbind 值
						 * if z0 != 0 goto r8 = virtualinvoke r0.<org.apache.mahout.cf.taste.hadoop.als.DatasetSplitter: org.apache.hadoop.mapreduce.Job prepareJob(org.apache.hadoop.fs.Path,org.apache.hadoop.fs.Path,java.lang.Class,java.lang.Class,java.lang.Class,java.lang.Class,java.lang.Class)>(r4, r5, class "org/apache/hadoop/mapreduce/lib/input/SequenceFileInputFormat", class "org/apache/mahout/cf/taste/hadoop/als/DatasetSplitter$WritePrefsMapper", class "org/apache/hadoop/io/NullWritable", class "org/apache/hadoop/io/Text", class "org/apache/hadoop/mapreduce/lib/output/TextOutputFormat")
						 */
						if(defusenode.getSuccs().size()>1){					
							if(stmt instanceof IfStmt){
								IfStmt ifstmt = (IfStmt) stmt;
								Value conditionValue = ifstmt.getCondition();
								ConditionExpr coniExpr = (ConditionExpr) conditionValue;
								Value lOperator = coniExpr.getOp1();// 左侧操作符
								Value rOperator = coniExpr.getOp2();// 右側操作符
								String symbol = coniExpr.getSymbol();
								if(symbol.equals("=")){
									// 如果操作符是 ＝ 赋值
									// 左侧是定义 右侧是使用						
									if(localvtVariant.containsValue(rOperator)){
										ConditionCheck conditionCheck = null;
										if(isInitConditionvalueSet){
											if(!methodToConditionCheck.containsKey(method)){
												conditionCheck = new ConditionCheck(method,argument.getCallSite());
												conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
												conditionCheck.addInitialConditionValue(rOperator, argument.getCallSite());
												methodToConditionCheck.put(method, conditionCheck);
												isInitConditionvalueSet = false;
											}else{
												conditionCheck = methodToConditionCheck.get(method);
												conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
												conditionCheck.addInitialConditionValue(rOperator, argument.getCallSite());
												isInitConditionvalueSet = false;
											}
										}
										// 加入到所有的ConditionalCheck中
										conditionCheck.addConditionValue(rOperator, argument.getCallSite());
									}
									
								}else{
									// 其他--- 两侧都是使用
									if(localvtVariant.containsValue(lOperator)){
										ConditionCheck conditionCheck = null;
										if(isInitConditionvalueSet){
											if(!methodToConditionCheck.containsKey(method)){
												conditionCheck = new ConditionCheck(method,argument.getCallSite());
												conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
												conditionCheck.addInitialConditionValue(lOperator, argument.getCallSite());
												methodToConditionCheck.put(method, conditionCheck);
												isInitConditionvalueSet = false;
											}else{
												conditionCheck = methodToConditionCheck.get(method);
												conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
												conditionCheck.addInitialConditionValue(lOperator, argument.getCallSite());
												isInitConditionvalueSet = false;
											}
											// 加入到所有的ConditionalCheck中
											conditionCheck.addConditionValue(lOperator, argument.getCallSite());
										}else{
											conditionCheck = methodToConditionCheck.get(method);
											// 加入到所有的ConditionalCheck中
											conditionCheck.addConditionValue(lOperator, argument.getCallSite());
										}
										
									}
									if(localvtVariant.containsValue(rOperator)){
										ConditionCheck conditionCheck = null;
										if(isInitConditionvalueSet){
											if(!methodToConditionCheck.containsKey(method)){
												conditionCheck = new ConditionCheck(method,argument.getCallSite());
												conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
												conditionCheck.addInitialConditionValue(rOperator, argument.getCallSite());
												methodToConditionCheck.put(method, conditionCheck);
												isInitConditionvalueSet = false;
											}else{
												conditionCheck = methodToConditionCheck.get(method);
												conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
												conditionCheck.addInitialConditionValue(rOperator, argument.getCallSite());
												isInitConditionvalueSet = false;
											}
											// 加入到所有的ConditionalCheck中
											conditionCheck.addConditionValue(lOperator, argument.getCallSite());
										}else{
											conditionCheck = methodToConditionCheck.get(method);
											// 加入到所有的ConditionalCheck中
											conditionCheck.addConditionValue(lOperator, argument.getCallSite());
										}
									}
									
								}
							}else if(stmt instanceof SwitchStmt){
								SwitchStmt switchstmt = (SwitchStmt) stmt;
								Value keyValue = switchstmt.getKey();
								if(verbose)
									System.out.println("Key is:"+keyValue);
								if(localvtVariant.containsValue(keyValue)){
									ConditionCheck conditionCheck = null;
									if(isInitConditionvalueSet){
										if(!methodToConditionCheck.containsKey(method)){
											conditionCheck = new ConditionCheck(method,argument.getCallSite());
											conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
											conditionCheck.addInitialConditionValue(keyValue, argument.getCallSite());
											methodToConditionCheck.put(method, conditionCheck);
											isInitConditionvalueSet = false;
										}else{
											conditionCheck = methodToConditionCheck.get(method);
											conditionCheck.setInitialConditionCFGNode(node, argument.getCallSite());
											conditionCheck.addInitialConditionValue(keyValue, argument.getCallSite());
											isInitConditionvalueSet = false;
										}
										// 加入到所有的ConditionalCheck中
										conditionCheck.addConditionValue(keyValue, argument.getCallSite());
									}else{
										conditionCheck = methodToConditionCheck.get(method);
										// 加入到所有的ConditionalCheck中
										conditionCheck.addConditionValue(keyValue, argument.getCallSite());
									}
								}
							}
						}
						
						
						
						if(isParaAssignStmt){
							IdentityStmt idstmt =  (IdentityStmt) stmt;
							argu = idstmt.getLeftOp();
							Local localarg = (Local)argu;
							int argIndex = argLists.indexOf(localarg);
							if(argIndex==-1) // catch exception
								continue;
							// 获取在调用层的参数
							Value remote = argument.getArgs().get(argIndex);
							List<Value> unbinds	= argument.getUnBindArgs();
							if(unbinds.isEmpty()){
								// 如果所有的实参 都是fixed 那么没有必要进行处理 也不必加入到unbind序列中
								continue;
							}else{
								// 如果有实参未绑定
								if(unbinds.contains(remote)){
									RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
									// 将对应加入到list中
									localParameterToRemoteArgu.put(argu, remote);
									if(rbTag!=null){
										// 将这个argument的赋值语句 加入相应的argument值作为绑定值
										rbTag.addBindingValue(argu,argument.getCallSite());
									}else{
										rbTag = new RBTag(argu,true,argument.getCallerMethod(),argument.getCallSite());								
										stmt.addTag(rbTag);
									}
									if(verbose){
										System.out.println("line@612: Add RBTag to stmt:"+stmt);
										System.out.println("line@613: ----Value:"+argu.toString());
									}
									// 将这个值加入到methodToUnbindValues
									if(!methodToUnbindValues.get(method).contains(argu)){
										methodToUnbindValues.get(method).add(argu);
									}
									// 获得远程的Variant
									Set<Variant> callerVariants = callervtVariant.getVariantsByValue(remote);
									// 有两个任务 
									// 1. 在callerVariant中加入绑定值 和绑定 语句
									// 2. 并且加入到本地的 valueToVariant中
									for(Variant variant:callerVariants){
										variant.addBindingStmts(stmt, argument.getCallSite());
										variant.addPaddingValue(argu, argument.getCallSite());
										// methodToVariant加入 
										methodToVariants.get(method).add(variant);
									}
									localvtVariant.addValueToVariant(argu, callerVariants);
									continue;
								}else{
									//当前的参数是 fixed 没有必要处理
									continue;
								}
							}
						}
						
						///////////////////如果此语句中有未绑定内容调用///////////////////////
						if(usedOverlap_Variable(useVars,methodToUnbindValues.get(method))){
							// 存在RB内容-------------
							Set<Variant> usedVariantSet = new HashSet<Variant>();
							containPRBValue = false;// 是否包含部分帮定值
							// 如果这里使用了 RB内容 我们设置其他的使用变量为 PRB变量
							for(Variable use:useVars){
								if(methodToUnbindValues.get(method).contains(use.getValue())){
									// 如果包含则为未绑定语句
									// 我们只将field 和 local加入
									if(!use.isLocal() && !use.isFieldRef()){
										continue;
									}
									RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
									if(rbTag!=null){
										rbTag.addBindingValue(use.getValue(),argument.getCallSite());
										
									}else{
										rbTag = new RBTag(use.getValue(),true,argument.getCallerMethod(),argument.getCallSite());
										stmt.addTag(rbTag);										
									}
									
									if(verbose){
										System.out.println("line@661: Add RBTag to stmt:"+stmt);
										System.out.println("line@662: ----Value:"+use.getValue().toString());
									}
									// 获得在这个语句中使用的 variant
									Set<Variant> useVariant = localvtVariant.getVariantsByValue(use.getValue());
									// 将使用的 variant 加入本语句的Set中
									if(useVariant!=null)
										usedVariantSet.addAll(useVariant);
								}else{// 只有在未定义中可以加入为 PRB值
									// 我们只将field 和 local加入
									if(!use.isLocal() && !use.isFieldRef()){
										continue;
									}
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
								//TODO 检查这个位置是否有经过
								
								PRBAnalysisStack.clear();
								
								if(defVars.isEmpty()){// 将这个部分未绑定的cfgnode加入到列表中
									//---- 1. PRBAnalysisNode--------
									PRBAnalysisStack.push(node);
									if(verbose){
										System.out.println("line700:向栈中加入底层节点: "+node.getStmt().toString());	
									}
								}
								else{
									// 并且LOP是一个真正的local
									boolean containsLocalField = false;
									for(Variable def:defVars){
										if(def.isLocal()||def.isFieldRef()){
											containsLocalField = true;
											break;
										}
									}
									if(!containsLocalField){
										PRBAnalysisStack.push(node);
										if(verbose){
											System.out.println("line715:向栈中加入底层节点: "+node.getStmt().toString());
										}
									}else{
										
										PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
										Set<Value>punbindValue = prbTag.getBindingValues();
										for(Value value:punbindValue){
											methodToParitalUnbindValues.get(method).remove(value);
										}
										stmt.removeTag(PRBTag.TAG_NAME);

									}
								}
								containPRBValue = false;
								
							}
							RBTag rbTag = (RBTag)stmt.getTag(RBTag.TAG_NAME);
							// 对于这里的def 由于 存在未绑定的使用 那么def 也是未绑定
							// 检查一下 多少个variant绑定在上面
							for(Variable def:defVars){
								// 我们只将field 和 local加入
								if(!def.isLocal() && !def.isFieldRef()){
									continue;
								}
								rbTag.addBindingValue(def.getValue(),argument.getCallSite());
								//将为左侧的定义 加入到绑定中
								if(!methodToUnbindValues.get(method).contains(def.getValue())){
									methodToUnbindValues.get(method).add(def.getValue());									
								}
								// def的值加入到binding value中
								for(Variant usedvariant:usedVariantSet){
									usedvariant.addPaddingValue(def.getValue(), argument.getCallSite());
								}
								// 加入vtovariant
								localvtVariant.addValueToVariant(def.getValue(), usedVariantSet);
								
							}							
						}
						
						else if(usedOverlap_Variable(useVars,methodToParitalUnbindValues.get(method)) && defVars.isEmpty()){
							// 使用了prb值 但是没有赋值内容
							for(Variable use:useVars){
								// 将这个部分未绑定的值 加入到PRBValue列表中
								// 我们只将field 和 local加入
								if(!use.isLocal() && !use.isFieldRef()){
									continue;
								}
								PRBTag prbTag = (PRBTag)stmt.getTag(PRBTag.TAG_NAME);
								if(prbTag!=null){
									prbTag.addBindingValue(use.getValue());
								}else{
									prbTag = new PRBTag(use.getValue());
									stmt.addTag(prbTag);								
								}
								if(methodToParitalUnbindValues.get(method).contains(use.getValue())){
									continue;
								}else{
									methodToParitalUnbindValues.get(method).add(use.getValue());
								}
							}
							if(!PRBAnalysisStack.isEmpty()){
								PRBAnalysisStack.push(node);
								if(verbose){
									System.out.println("line788:向PRB分析栈中加入:\t"+node.getStmt());
								}
							}
						}
						else if(usedOverlap_Variable(useVars,methodToParitalUnbindValues.get(method)) && !defVars.isEmpty()){
							// 使用了prb值 但是是有赋值内容
							// 所有的prb值 和在PRBAnalysisStack中的prb值需要指向这个值
							// 首先将这个stmt加入
							for(Variable use:useVars){
								// 我们只将field 和 local加入
								if(!use.isLocal() && !use.isFieldRef()){
									continue;
								}
								RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
								if(rbTag!=null){
									rbTag.addBindingValue(use.getValue(),argument.getCallSite());
								}else{
									rbTag = new RBTag(use.getValue(),true,argument.getCallerMethod(),argument.getCallSite());
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
								// 我们只将field 和 local加入
								if(!def.isLocal() && !def.isFieldRef()){
									continue;
								}
								RBTag rbTag = (RBTag) stmt.getTag(RBTag.TAG_NAME);
								rbTag.addBindingValue(def.getValue(),argument.getCallSite());
								if(!methodToUnbindValues.get(method).contains(def.getValue())){
									methodToUnbindValues.get(method).add(def.getValue());
								}
							}
							
							int stacklength = PRBAnalysisStack.size();
							if(stacklength<1)
								continue;
							
							CFGNode lastnode = getStackElement(PRBAnalysisStack, stacklength-1);
							Stmt laststmt = lastnode.getStmt();
							if(verbose){
								System.out.println("line836: 在堆栈中的最后语句 :"+laststmt);
								System.out.println("line837: 在堆栈中的所有语句 :");
								for(CFGNode prbnode:PRBAnalysisStack){
									System.out.println("Node:"+prbnode.getStmt());
								}
							}
							
							
							RBTag lastrbTag = (RBTag)laststmt.getTag(RBTag.TAG_NAME);
							if(lastrbTag==null){
								System.out.println("Error lastrbTag should not be null");
							}
							
							Set<Value> lastbindvalues = lastrbTag.getBindingValues(argument.getCallSite());
							// 堆栈对应的Variant集合
							Set<Variant> variantset = new HashSet<Variant>();
							for(Value vi:lastbindvalues){
								// variantset.addAll(localvtVariant.getVariantsByValue(vi));
								Set<Variant> vivariantset = localvtVariant.getVariantsByValue(vi);
								if(vivariantset!=null)
									variantset.addAll(vivariantset);
							}
							// variant 中加入当前语句 
							for(Variant variant:variantset){
								variant.addBindingStmts(stmt, argument.getCallSite());
								for(Variable def:defVars) {
									variant.addPaddingValue(def.getValue(), argument.getCallSite());
								}
								for(Variable use:useVars) {
									variant.addPaddingValue(use.getValue(), argument.getCallSite());
								}
							}
							for(Variable def:defVars) {
								localvtVariant.addValueToVariant(def.getValue(), variantset);
							}
							for(Variable use:useVars) {
								localvtVariant.addValueToVariant(use.getValue(), variantset);
							}
							
							while(!PRBAnalysisStack.isEmpty()){
								CFGNode prbnode = PRBAnalysisStack.pop();
								Stmt prbstmt = prbnode.getStmt();
								
								// 删除stmt上绑定的PRBTag 加入新的RBTag
								// 在新加入的RBTag上绑定 lastnode上的values 
								 
								PRBTag prbTag = (PRBTag)prbstmt.getTag(PRBTag.TAG_NAME);
								if(prbTag==null){
									System.out.println("PRBTag 为空对于语句:"+prbstmt.toString());
								}
								Set<Value> bindingvalues = prbTag.getBindingValues();
								// 从partial unbind value中移除
								for(Value value:bindingvalues){
									if(methodToParitalUnbindValues.get(method).contains(value))
										methodToParitalUnbindValues.get(method).remove(value);
								}
								RBTag rbTag = new RBTag(bindingvalues,true,argument.getCallerMethod(),argument.getCallSite());
								rbTag.addBindingValue(lastbindvalues,argument.getCallSite());
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
								
								// variant 中加入当前语句 
								for(Variant variant:variantset){
									variant.addBindingStmts(stmt, argument.getCallSite());
									for(Value value:bindingvalues){
										variant.addPaddingValue(value, argument.getCallSite());
									}
								}
								for(Value value:bindingvalues) {
									localvtVariant.addValueToVariant(value, variantset);
								}
								
							}
							PRBAnalysisStack.clear();
							containPRBValue = false;
						}
					}
				}
			}
		}
		
	}
	
	public void removeHiddenVariant(List<Variant> variants){
		/*
		 * 删除不可见Variant
		 */
		if(verbose)
			System.out.println("#variant in fullvariantList is:"+fullVariantList.size());
		fullVariantList.removeAll(variants);
		if(verbose)
			System.out.println("After remove #variant in fullvariantList is:"+fullVariantList.size());
	}
	
	private boolean usedOverlap_Variable(List<Variable> useVars, Set<Value> list) {
		for(Variable use:useVars){
			if(list.contains(use.getValue()))
				return true;
		}
		return false;
	}
	
	private void variantColorAssign(){
		
		for(Variant variant:fullVariantList){
			VariantColorMap.inst().registerColorForVariant(variant);
		}
		
	}
	
	public static <T> T getStackElement(Stack<T> stack, int index) {
		  if (index == 0) {
		    return stack.peek();
		  }

		  T x = stack.pop();
		  try {
		    return getStackElement(stack, index - 1);
		  } finally {
		    stack.push(x);
		  }
	}
		
	private void variantcolorannotation(){
		Set<File>shouldbeRendered = new HashSet<File>();
		String variantId = "";
		for(Variant variant:fullVariantList){
			variantId = "";
			variantId += variant.getVariantId();
			// annotate
			VariantAnnotate varAnnotate = new VariantAnnotate(variant,variantId,VariantColorMap.inst().getColorforVariant(variant));
			// 获得这个varAnnotate的中需要被rendered的文件
			List<File> shouldAnnotate = varAnnotate.getShouldAnnotate();
			if(!shouldAnnotate.isEmpty()){
				shouldbeRendered.addAll(shouldAnnotate);
			}
		}
		JTree mainannotatedTree = MainFrame.inst().getFeatureAnnotatedTree();
		// 加入tree节点渲染
		mainannotatedTree.setCellRenderer(new TreeCellRender(shouldbeRendered));
	}
	
	public List<Variant> getfullVariantList(){
		return fullVariantList;
	}
	public Map<SootMethod,List<Variant>> getmethodToVariants(){
		return methodToVariants;
	}
	public List<SootMethod> getCallerMethods(){
		return callerMethod;
	}
	public List<SootMethod> getCalleeMethods(){
		return calleeMethod;
	}
	public Map<SootMethod,List<CallSite>> getCallerMethodToCallSites(){
		return callerMethodToCallSite;
	}
	public Map<Integer,Set<Integer>> getvariantIdToBlockIds(){
		if(variantIdToblockIds.isEmpty()){
			for(Variant variant:fullVariantList){
				int variantId = variant.getVariantId();
				Set<Integer> blockIds = variant.getBlockIdsInSet();
				variantIdToblockIds.put(variantId, blockIds);
			}
		}
		return variantIdToblockIds;
	}
	/**
	 * 返回从SootMethod 到 检查条件的对应
	 * @return
	 */
	public Map<SootMethod,ConditionCheck> getmethodToConditionCheck(){
		return methodToConditionCheck;
	}
	public boolean isCallerMethod(SootMethod method){
		return callerMethod.contains(method);
	
	}
	/**
	 * 返回这个caller函数下的所有callee函数
	 */
	public Set<SootMethod> getCalleesForCaller(SootMethod caller){
		Set<SootMethod> allcallees  = new HashSet<SootMethod>();
		List<CallSite> callsites = callerMethodToCallSite.get(caller);
		if(callsites!=null){
			for(CallSite site:callsites){
				allcallees.addAll(site.getAppCallees());
			}
		}
		return allcallees;
	}

	public Set<SootMethod> getCallerForCallee(SootMethod method) {
		Set<SootMethod> allcallers = new HashSet<SootMethod>();
		Set<CallSite> callsites = calleeMethodToCallSite.get(method);
		for(CallSite site:callsites){
			allcallers.add(site.getCallerMethod());
		}
		return allcallers;
	}
	public Map<SootMethod,Set<CallSite>> getCalleeMethodToCallSites(){
		return calleeMethodToCallSite;
	}
	
	
}