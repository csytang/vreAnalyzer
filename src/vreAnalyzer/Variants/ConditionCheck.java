package vreAnalyzer.Variants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;
import soot.Value;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;

public class ConditionCheck {
	
	// 第一个判别条件和语句
	private Map<CallSite,Set<Value>> calleeinitConditionalValues = new HashMap<CallSite,Set<Value>>();
	private Map<CallSite,CFGNode> calleeinitConditionalCFGNode = new HashMap<CallSite,CFGNode>();
	private Set<Value> callerinitConditionalValues = new HashSet<Value>();
	private CFGNode callerinitConditionalCFGNode = null;
	// 所有的判别和语句
	private Set<Value> allcallerConditionalValues = new HashSet<Value>();
	private Map<CallSite,Set<Value>> calleeConditionalvaluesMap = new HashMap<CallSite,Set<Value>>();
	
	
	private SootMethod method = null;
	private Set<CallSite> callsiteSet = new HashSet<CallSite>();
	private boolean isCaller = false;
	
	public ConditionCheck(SootMethod smethod,CallSite scallsite){
		method = smethod;
		if(scallsite==null){
			isCaller = true;
		}else{
			isCaller = false;
			callsiteSet.add(scallsite);
		}
	}
	
	// 获得条件值
	public Set<Value> getInitialConditionValue(CallSite callsite){
		if(callsite==null){
			// 在caller位置
			return callerinitConditionalValues;
		}else{
			// 在callee位置
			return calleeinitConditionalValues.get(callsite);		
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
				callsiteSet.add(callsite);
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
				callsiteSet.add(callsite);
			}else{
				calleeinitConditionalValues.get(callsite).addAll(values);
			}
		}
	}
	
	public void addConditionValue(Value value,CallSite callsite){
		if(callsite==null){
			// 对于callsite 无 Caller
			if(allcallerConditionalValues.isEmpty()){
				Set<Value>values = new HashSet<Value>();
				values.add(value);
				allcallerConditionalValues.addAll(values);
			}else{
				allcallerConditionalValues.add(value);
			}
		}else{
			if(!calleeConditionalvaluesMap.containsKey(callsite)){
				Set<Value>values = new HashSet<Value>();
				values.add(value);
				calleeConditionalvaluesMap.put(callsite, values);
			}else{
				calleeConditionalvaluesMap.get(callsite).add(value);
			}
		}
	}
	
	public void addConditionValue(Set<Value> values, CallSite callsite){
		if(callsite==null){
			allcallerConditionalValues.addAll(values);
		}else{
			if(!calleeConditionalvaluesMap.containsKey(callsite)){
				calleeConditionalvaluesMap.put(callsite, values);
			}else{
				calleeConditionalvaluesMap.get(callsite).addAll(values);
			}
		}
	}
	
	
		
	// 初始化 条件 stmt 和 callsite
	public void setInitialConditionCFGNode(CFGNode cfgNode,CallSite callsite){
		if(callsite==null){
			callerinitConditionalCFGNode = cfgNode;
		}else{
			calleeinitConditionalCFGNode.put(callsite,cfgNode);
		}
	}

	public boolean isCallerMethod() {
		// TODO Auto-generated method stub
		return isCaller;
	}
	
	public Set<Value> getinitConditionalValues(CallSite callsite){
		if(callsite==null){
			return callerinitConditionalValues;
		}else{
			return calleeinitConditionalValues.get(callsite);
		}
	}
	public Set<Value> getallConditionalValues(CallSite callsite){
		if(callsite==null){
			return allcallerConditionalValues;
		}else{
			return calleeConditionalvaluesMap.get(callsite);
		}
	}
	
	public CFGNode getinitConditionalCFGNode(CallSite callsite){
		if(callsite==null){
			return callerinitConditionalCFGNode;
		}else{
			return calleeinitConditionalCFGNode.get(callsite);
		}
	}
	
	public Map<CallSite,Set<Value>> getcalleeinitConditionalValues(){
		return calleeinitConditionalValues;
	}

	public Map<CallSite, CFGNode> getcalleeinitCFGNodes() {
		return calleeinitConditionalCFGNode;
	}

	public String getCallerConditionalValueString() {
		String callerConditionalValueString = "[";
		for(Value value:callerinitConditionalValues){
			callerConditionalValueString += value.toString();
			callerConditionalValueString += ",";
		}
		if(callerinitConditionalValues.size()>=1){
			callerConditionalValueString = callerConditionalValueString.substring(0,callerConditionalValueString.length()-1);
		}
		
		return callerConditionalValueString;
	}
	
}
