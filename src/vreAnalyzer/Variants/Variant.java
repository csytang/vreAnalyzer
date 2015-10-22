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
	int id = 0;
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
			if(block.getSootMethod()==callerMethod && block.getType()==BlockType.Stmt){
				List<CFGNode> blocknodes = block.getCFGNodes();
				// 4. 将包含在 block nodes 中的node全部删除 看剩余
				Set<Stmt>remainsstmts = blockProcess(callerstmts,blocknodes);
				if(isFullSubSet(remainsstmts,callerstmts)){
					// 将这个codeblock的id加入到blockIds中
					blockIds.add(block.getBlockId());
				}else if(isOverlap(remainsstmts,callerstmts)){
					// 将这个codeblock的id加入到blockIds中
					blockIds.add(block.getBlockId());
				}
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
					if(block.getSootMethod()==callee && block.getType()==BlockType.Stmt){
						List<CFGNode> blocknodes = block.getCFGNodes();
						// 3. 将包含在 block nodes 中的node全部删除 看剩余
						Set<Stmt>remainsstmts = blockProcess(calleestmtsSet,blocknodes);
						if(isFullSubSet(remainsstmts,calleestmtsSet)){
							// 将这个codeblock的id加入到blockIds中
							blockIds.add(block.getBlockId());
						}else if(isOverlap(remainsstmts,calleestmtsSet)){
							// 将这个codeblock的id加入到blockIds中
							blockIds.add(block.getBlockId());
						}
					}
				}
			}
		}
		return blockIds;
	}
	
	
	
	private boolean isFullSubSet(Set<Stmt> remainsstmts, Set<Stmt> allstmts) {
		// TODO Auto-generated method stub
		return allstmts.containsAll(remainsstmts);
	}
	private boolean isOverlap(Set<Stmt> remainsstmts, Set<Stmt> allstmts){
		// 1. 两个set有重复
		Set<Stmt> allstmtsTmp = new HashSet<Stmt>(allstmts);
		return allstmtsTmp.retainAll(remainsstmts);
	}
	public Set<Stmt> blockProcess(Set<Stmt>allstmts, List<CFGNode>blocknodes){
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
	public List<SootMethod> getAllMethods() {
		// TODO Auto-generated method stub
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
	public List<SootClass> getAllClasses() {
		// TODO Auto-generated method stub
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
	
}
