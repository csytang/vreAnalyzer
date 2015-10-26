package vreAnalyzer.Variants;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import vreAnalyzer.Blocks.BlockGenerator;
import vreAnalyzer.Blocks.BlockType;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Variant {

	private List<Stmt> bindingStmts = null;
	private List<Value> paddingValues = null;
	private SootMethod callerMethod  = null;// 如果有
	private Map<CallSite,List<Stmt>> callSiteToBindingStmt = new HashMap<CallSite,List<Stmt>>();
	private Map<CallSite,List<Value>> callSiteToBindingValue = new HashMap<CallSite,List<Value>>();
	private List<CallSite> callsiteList = null;
	
	// 第一个判别条件和语句
	private Map<CallSite,Set<Value>> calleeinitConditionalValues = new HashMap<CallSite,Set<Value>>();
	private Map<CallSite,Stmt> calleeinitConditionalStmt = new HashMap<CallSite,Stmt>();
	private Set<Value> callerinitConditionalValues = new HashSet<Value>();
	private Stmt callerinitConditionalStmt = null;
	int id = 0;
	
	/*
	 * Variant 构造器
	 */
	public Variant(Value vi,List<Stmt>stmts,CallSite callsite,SootMethod method,int id){
		if(callsite==null){
			paddingValues = new LinkedList<Value>();
			bindingStmts = new LinkedList<Stmt>();
			paddingValues.add(vi);
			bindingStmts.addAll(stmts);
			callerMethod = method;
		}else{
			List<Stmt>remotebindings = new LinkedList<Stmt>();
			List<Value>remotevalues = new LinkedList<Value>();
			remotebindings.addAll(stmts);
			remotevalues.add(vi);
			callSiteToBindingStmt.put(callsite, remotebindings);
			callSiteToBindingValue.put(callsite, remotevalues);
		}
		this.id = id;
	}
	
	public Variant(Value vi,Stmt stmt,CallSite callsite,SootMethod method,int id){
		if(callsite==null){
			paddingValues = new LinkedList<Value>();
			bindingStmts = new LinkedList<Stmt>();
			paddingValues.add(vi);
			bindingStmts.add(stmt);
			callerMethod = method;
		}else{
			List<Stmt>remotebindings = new LinkedList<Stmt>();
			List<Value>remotevalues = new LinkedList<Value>();
			remotebindings.add(stmt);
			remotevalues.add(vi);
			callSiteToBindingStmt.put(callsite, remotebindings);
			callSiteToBindingValue.put(callsite, remotevalues);
		}
		this.id = id;
	}
	//////////////////////////////////////////////////////////////
	
	
	// 加入绑定值到这个Variant////////////////////////////////////
	public void addPaddingValue(List<Value>vis,CallSite callsite){
		if(callsite==null){
			this.paddingValues.addAll(vis);
		}else{
			if(callSiteToBindingValue.containsKey(callsite)){
				callSiteToBindingValue.get(callsite).addAll(vis);
			}else{
				callSiteToBindingValue.put(callsite, vis);
			}
		}
	}
	
	public void addPaddingValue(Set<Value>vis,CallSite callsite){
		if(callsite==null){
			this.paddingValues.addAll(vis);
		}else{
			if(callSiteToBindingValue.containsKey(callsite)){
				callSiteToBindingValue.get(callsite).addAll(vis);
			}else{
				List<Value>vislist = new LinkedList<Value>(vis);
				callSiteToBindingValue.put(callsite, vislist);
			}
		}
	}
	
	public void addPaddingValue(Value vi,CallSite callsite){
		if(callsite==null){
			this.paddingValues.add(vi);
		}else{
			if(callSiteToBindingValue.containsKey(callsite)){
				callSiteToBindingValue.get(callsite).add(vi);
			}else{
				List<Value>vislist = new LinkedList<Value>();
				vislist.add(vi);
				callSiteToBindingValue.put(callsite, vislist);
			}
		}
	}
	///////////////////////////////////////////////////////////
	
	// 加入绑定语句//////////////////////////////////////////////
    public void addBindingStmts(Stmt stmt,CallSite callsite) {
    	if(callsite==null){
    		this.bindingStmts.add(stmt);
    	}else{
    		if(callSiteToBindingStmt.containsKey(callsite)){
    			callSiteToBindingStmt.get(callsite).add(stmt);
    		}else{
    			List<Stmt>stmtlist = new LinkedList<Stmt>();
    			stmtlist.add(stmt);
    			callSiteToBindingStmt.put(callsite, stmtlist);
    		}
    	}
    }
    
    public void addBindingStmts(List<Stmt>stmts,CallSite callsite) {
    	if(callsite==null){
    		this.bindingStmts.addAll(stmts);
    	}else{
    		if(callSiteToBindingStmt.containsKey(callsite)){
    			callSiteToBindingStmt.get(callsite).addAll(stmts);
    		}else{
    			callSiteToBindingStmt.put(callsite, stmts);
    		}
    	}
    }
    /////////////////////////////////////////////////////////
    
	public List<Stmt> getBindingStmts(CallSite callsite) {
		if(callsite==null){
			return this.bindingStmts; 
		}else{
			if(callSiteToBindingStmt.containsKey(callsite)){
				return callSiteToBindingStmt.get(callsite);
			}else{
				return null;
			}
		}
	}
	
	// 获得在一个callsite下的绑定值
	public List<Value> getPaddingValues(CallSite callsite) {
		if(callsite == null){
			return this.paddingValues;
		}else{
			if(callSiteToBindingValue.containsKey(callsite)){
				return callSiteToBindingValue.get(callsite);
			}else
				return null;
		}
	}
	
	// 获得所有的callsite
	public List<CallSite> getCallSiteList(){
		if(callsiteList==null)
			callsiteList = new LinkedList<CallSite>(callSiteToBindingStmt.keySet());
		return callsiteList;
	}
	public SootMethod getCallerMethod() {
		// TODO Auto-generated method stub
		return callerMethod;
	}
	public int getVariantId() {
		// TODO Auto-generated method stub
		return id;
	}
	
	// 获得一个Variant在code block中对应
	public List<Integer> getBlockIds() {
		// TODO Auto-generated method stub
		List<Integer> blockIds = new LinkedList<Integer>();
		///////////////////////////CallerStmt////////////////////////////
		// 1. 获得
		Set<Stmt>callerstmts = new HashSet<Stmt>();
		callerstmts.addAll(bindingStmts);
		// 2. 获得所有block的列表
		List<CodeBlock> blockpool = BlockGenerator.instance.getblockPool();
		// 3. 在caller函数中
		for(CodeBlock block:blockpool){
			Set<Integer> blockIdStmt = new HashSet<Integer>();
			int blockIdMethod = -1;
			if(block.getSootMethod()==callerMethod && block.getType()==BlockType.Stmt){
				List<CFGNode> blocknodes = block.getCFGNodes();
				// 4. 将包含在 block nodes 中的node全部删除 看剩余
				Set<Stmt>remainsstmts = removeStmtinBlock(callerstmts,blocknodes);
				if(isFullSubSet(remainsstmts,callerstmts)){
					// 将这个codeblock的id加入到blockIds中
					blockIdStmt.add(block.getBlockId());
				}else if(isOverlap(remainsstmts,callerstmts)){
					// 将这个codeblock的id加入到blockIds中
					blockIdStmt.add(block.getBlockId());
				}
			}else if(block.getSootMethod()==callerMethod && block.getType()==BlockType.Method){
				blockIdMethod = block.getBlockId();
			}
			if(blockIdStmt.isEmpty()){
				// 如果不被包含在任何子语句中
				blockIds.add(blockIdMethod);
				blockIdMethod = -1;
			}else{
				blockIds.addAll(blockIdStmt);
				blockIdStmt.clear();
			}
		}
		///////////////////////CallSite/////////////////////////////////
		for(CallSite callsite:callsiteList){
			// 1. 获取在这个callsite下的stmts
			List<Stmt> callsiteStmts = callSiteToBindingStmt.get(callsite);
			Set<Stmt> calleestmtsSet = new HashSet<Stmt>(callsiteStmts);
			List<SootMethod>callees = callsite.getAllCallees();
			// 2. 在callee函数中
			for(SootMethod callee:callees){
				for(CodeBlock block:blockpool){// 此處應該為callee
					Set<Integer> blockIdStmt = new HashSet<Integer>();
					int blockIdMethod = -1;
					if(block.getSootMethod()==callee && block.getType()==BlockType.Stmt){
						List<CFGNode> blocknodes = block.getCFGNodes();
						// 3. 将包含在 block nodes 中的node全部删除 看剩余
						Set<Stmt>remainsstmts = removeStmtinBlock(calleestmtsSet,blocknodes);
						if(isFullSubSet(remainsstmts,calleestmtsSet)){
							// 将这个codeblock的id加入到blockIds中
							blockIdStmt.add(block.getBlockId());
						}else if(isOverlap(remainsstmts,calleestmtsSet)){
							// 将这个codeblock的id加入到blockIds中
							blockIdStmt.add(block.getBlockId());
						}
					}else if(block.getSootMethod()==callerMethod && block.getType()==BlockType.Method){
						blockIdMethod = block.getBlockId();
					}
					if(blockIdStmt.isEmpty()){
						// 如果不被包含在任何子语句中
						blockIds.add(blockIdMethod);
						blockIdMethod = -1;
					}else{
						blockIds.addAll(blockIdStmt);
						blockIdStmt.clear();
					}
					
				}
			}
		}
		return blockIds;
	}
	
	// 判断两个集合关系 是否为 子集
	private boolean isFullSubSet(Set<Stmt> remainsstmts, Set<Stmt> allstmts) {
		// TODO Auto-generated method stub
		return allstmts.containsAll(remainsstmts);
	}
	
	// 判断两个集合关系 是否为 有交集
	private boolean isOverlap(Set<Stmt> remainsstmts, Set<Stmt> allstmts){
		// 1. 两个set有重复
		Set<Stmt> allstmtsTmp = new HashSet<Stmt>(allstmts);
		return allstmtsTmp.retainAll(remainsstmts);
	}
	
	// 出去在block中的重复语句
	public Set<Stmt> removeStmtinBlock(Set<Stmt>allstmts, List<CFGNode>blocknodes){
		/*
		 * 对于这个block中 包含的 进行处理
		 */
		List<Stmt>blockstmts = new LinkedList<Stmt>();
		for(CFGNode node:blocknodes){
			Stmt stmt = node.getStmt();
			blockstmts.add(stmt);
		}
		// 加入看是否有重叠
		List<Stmt>remainstmts = new LinkedList<Stmt>();
		remainstmts.addAll(allstmts);
		remainstmts.retainAll(blockstmts);
		Set<Stmt>remainstmtset = new HashSet<Stmt>(remainstmts);
		return remainstmtset;
	}
	
	// 获得这个Variant所涉及的所有函数
	public List<SootMethod> getAllMethods() {
		
		Set<SootMethod>methodset = new HashSet<SootMethod>();
		methodset.add(callerMethod);
		if(callsiteList==null){
			callsiteList = new LinkedList<CallSite>(callSiteToBindingStmt.keySet());
		}
		for(CallSite callsite:callsiteList){
			methodset.addAll(callsite.getAppCallees());
		}
		List<SootMethod> allmethods = new LinkedList<SootMethod>(methodset);
		
		return allmethods;
	}
	
	// 获得这个Variant所涉及的所有类
	public List<SootClass> getAllClasses() {

		Set<SootClass>classset = new HashSet<SootClass>();
		classset.add(callerMethod.getDeclaringClass());
		if(callsiteList==null){
			callsiteList = new LinkedList<CallSite>(callSiteToBindingStmt.keySet());
		}
		
		for(CallSite callsite:callsiteList){
			for(SootMethod callee:callsite.getAppCallees()){
				if(ProgramFlowBuilder.inst().getAppClasses().contains(callee.getDeclaringClass())){
					classset.add(callee.getDeclaringClass());
				}
			}
		}
		List<SootClass> allclasses = new LinkedList<SootClass> (classset);
		return allclasses;
	}
		
	// 初始化 条件 stmt 和 callsite
	public void setInitialConditionStmt(Stmt stmt,CallSite callsite){
		if(callsite==null){
			callerinitConditionalStmt = stmt;
		}else{
			calleeinitConditionalStmt.put(callsite,stmt);
		}
	}
	
	// 初始化 条件 stmt 和 callsite
	public void addInitialConditionValue(Value value,CallSite callsite){
		if(callsite==null){
			// 对于callsite 无 Caller
			if(callerinitConditionalValues.isEmpty()){
				Set<Value>values = new HashSet<Value>();
				values.add(value);
				callerinitConditionalValues.addAll(values);
			}else{
				callerinitConditionalValues.add(value);
			}
		}else{
			if(!calleeinitConditionalValues.containsKey(callsite)){
				Set<Value>values = new HashSet<Value>();
				values.add(value);
				calleeinitConditionalValues.put(callsite, values);
			}else{
				calleeinitConditionalValues.get(callsite).add(value);
			}
		}
	}
	
	// 初始化条件值
	public void addInitialConditionValue(Set<Value> values, CallSite callsite){
		if(callsite==null){
			callerinitConditionalValues.addAll(values);
		}else{
			if(!calleeinitConditionalValues.containsKey(callsite)){
				calleeinitConditionalValues.put(callsite, values);
			}else{
				calleeinitConditionalValues.get(callsite).addAll(values);
			}
		}
	}
	
	// 获得Variant的分隔符号
	public String getSeperatorValues() {
		
		// 返回一个字符串 这个字符串中
		String seperatorValueString = "";
		seperatorValueString += "[";
		// 1. 加入在caller中的value
		if(!callerinitConditionalValues.isEmpty()){
			seperatorValueString += "(@";
			seperatorValueString += callerMethod.getName();
			seperatorValueString += " ";
		}
		for(Value value:callerinitConditionalValues){
			seperatorValueString += value;
			seperatorValueString += ":";
		}
		if(!callerinitConditionalValues.isEmpty()){
			seperatorValueString = seperatorValueString.substring(0,seperatorValueString.length()-1);
			seperatorValueString += ")";
		}
		
		
		// 遍历所有的callsite
		if(callsiteList==null)
			callsiteList = new LinkedList<CallSite>(callSiteToBindingStmt.keySet());
		if(callsiteList.size()!=0){
			seperatorValueString += ":";
		}
		
		for(CallSite site:callsiteList){
			Set<Value> calleeValues = calleeinitConditionalValues.get(site);
			seperatorValueString += "(@";
			// 加入method
			
			for(Value calleeValue:calleeValues){
				seperatorValueString += calleeValue;
				seperatorValueString += ":";
			}
			if(!calleeValues.isEmpty()){
				seperatorValueString = seperatorValueString.substring(0,seperatorValueString.length()-1);
			}
			seperatorValueString += ")";
		}
		if(callsiteList.size()!=0){
			seperatorValueString = seperatorValueString.substring(0,seperatorValueString.length()-1);
		}
		
		seperatorValueString += "]";
		return seperatorValueString;
		
	}
	
}
