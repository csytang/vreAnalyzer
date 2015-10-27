package vreAnalyzer.Variants;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import vreAnalyzer.Blocks.BlockGenerator;
import vreAnalyzer.Blocks.BlockType;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.ControlFlowGraph.CFG;
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
	private String codeRange = "";
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
		Set<Integer> blockIdStmt = new HashSet<Integer>();
		int blockIdMethod = -1;
		for(CodeBlock block:blockpool){
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
		}
		if(blockIdStmt.isEmpty() && blockIdMethod!=-1){
			// 如果不被包含在任何子语句中
			blockIds.add(blockIdMethod);
			blockIdMethod = -1;
		}else if(!blockIdStmt.isEmpty()){
			blockIds.addAll(blockIdStmt);
			blockIdStmt.clear();
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
					
				}
			}
			if(blockIdStmt.isEmpty() && blockIdMethod!= -1){
				// 如果不被包含在任何子语句中
				blockIds.add(blockIdMethod);
				blockIdMethod = -1;
			}else if(!blockIdStmt.isEmpty()){
				blockIds.addAll(blockIdStmt);
				blockIdStmt.clear();
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
		
		Set<Stmt>remainstmts = new HashSet<Stmt>();
		remainstmts.addAll(allstmts);
		for(CFGNode node:blocknodes){
			Stmt stmt = node.getStmt();
			if(remainstmts.contains(stmt))
				remainstmts.remove(stmt);
		}
		
		return remainstmts;
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

	public String getCodeRangeforVariant() {
		
		// 1. CodeRange 字符串
		if(!codeRange.trim().equals(""))
			return codeRange;
		
		// 2. 对于 caller method
		CFG cfg = ProgramFlowBuilder.inst().getCFG(callerMethod);
		int callerblocksize = this.bindingStmts.size();
		int []callercoderange = new int[callerblocksize];
		int rangeindex = 0;
		
		for(int i = 0;i < bindingStmts.size();i++){
			CFGNode cfgNode = cfg.convertstmtToCFGNode(bindingStmts.get(i));
			callercoderange[rangeindex] = cfgNode.getIdInMethod();
			rangeindex++;
		}
		// 2.1 删除相同的元素
		Set<Integer> updatecallercoderangeSet = removeRepeateValues(callercoderange);
		callercoderange = new int[updatecallercoderangeSet.size()];
		int index = 0;
		for(int id:updatecallercoderangeSet){
			callercoderange[index] = id;
			index++;
		}
		
		String callerCodeRange = "";
		
		// 3. caller array排序
		if(callerblocksize>1){
			callerCodeRange += "@"+callerMethod.getName();
			quickSort(callercoderange,0,callercoderange.length-1);
			String rangestring = "[";
				int startIndex = 0;
				int endIndex = 0;
				for(int i = 0;i < callercoderange.length;i++){
					startIndex = i;
					endIndex = startIndex;
					if(i<callercoderange.length-1){
						while(callercoderange[i+1]-callercoderange[i]==1){
							endIndex++;
							i++;
							if(i==callercoderange.length-1)
								break;
						}
					}
					if(endIndex-startIndex>=2)
						rangestring+=callercoderange[startIndex]+":"+callercoderange[endIndex]+",";
					else if(endIndex-startIndex==1)
						rangestring+=callercoderange[startIndex]+","+callercoderange[endIndex]+",";
					else
						rangestring+=callercoderange[startIndex]+",";
						
				}
				if(callercoderange.length>0)
					rangestring = rangestring.substring(0, rangestring.length()-1);
				rangestring+="]";
				callerCodeRange += rangestring;
		}
		codeRange = callerCodeRange;
		// 4. 在Callee中处理
		String calleeCodeRange = "";
		if(callsiteList==null){
			callsiteList.addAll(callSiteToBindingStmt.keySet());
		}
		
		// 5. 遍历整个list
		for(CallSite callsite:callsiteList){
			List<Stmt> calleebindingstmts = callSiteToBindingStmt.get(callsite);
			List<SootMethod> calleeMethods = callsite.getAppCallees();
			for(SootMethod callee:calleeMethods){
				CFG calleecfg = ProgramFlowBuilder.inst().getCFG(callee);
				Set<Stmt>calleestmtSet = calleecfg.getstmtSet();
				
				// 如果是这个 callee method
				if(calleestmtSet.containsAll(calleebindingstmts)){
					int calleeblocksize = calleebindingstmts.size();
					int []calleecoderange = new int[calleeblocksize];
					rangeindex = 0;
					for(int i = 0;i < calleebindingstmts.size();i++){
						CFGNode cfgNode = calleecfg.convertstmtToCFGNode(calleebindingstmts.get(i));
						calleecoderange[rangeindex] = cfgNode.getIdInMethod();
						rangeindex++;
					}
					// 2.1 删除相同的元素
					Set<Integer> updatecalleecoderangeSet = removeRepeateValues(calleecoderange);
					calleecoderange = new int[updatecalleecoderangeSet.size()];
					index = 0;
					for(int id:updatecalleecoderangeSet){
						calleecoderange[index] = id;
						index++;
					}
					
					
					calleeCodeRange = "";
					calleeCodeRange += "@"+callee.getName();
					if(calleeblocksize>1){
						quickSort(calleecoderange,0,calleecoderange.length-1);
						String rangestring = "[";
							int startIndex = 0;
							int endIndex = 0;
							for(int i = 0;i < callercoderange.length;i++){
								startIndex = i;
								endIndex = startIndex;
								if(i<calleecoderange.length-1){
									while(calleecoderange[i+1]-calleecoderange[i]==1){
										endIndex++;
										i++;
										if(i==calleecoderange.length-1)
											break;
									}
								}
								if(endIndex-startIndex>=2)
									rangestring+=calleecoderange[startIndex]+":"+calleecoderange[endIndex]+",";
								else if(endIndex-startIndex==1)
									rangestring+=calleecoderange[startIndex]+","+calleecoderange[endIndex]+",";
								else
									rangestring+=calleecoderange[startIndex]+",";
									
							}
							if(calleecoderange.length>0)
								rangestring = rangestring.substring(0, rangestring.length()-1);
							rangestring+="]";
							calleeCodeRange += rangestring;
					}
					codeRange += calleeCodeRange;
				}
			}
		}
		
		return codeRange;
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
	public Set<Integer> removeRepeateValues(int []codeIds){
		Set<Integer>updateIds = new HashSet<Integer>();
		for(int i:codeIds){
			updateIds.add(i);
		}
		return updateIds;
	}

}
