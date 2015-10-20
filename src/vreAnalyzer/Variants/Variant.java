package vreAnalyzer.Variants;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import vreAnalyzer.Elements.CallSite;

import java.util.HashMap;
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
	
}
