package vreAnalyzer.Elements;

import soot.SootMethod;
import vreAnalyzer.Context.Context;
import vreAnalyzer.PointsTo.PointsToGraph;

import java.util.ArrayList;
import java.util.List;

public class CallSite implements Comparable<CallSite>{
	/*
	 * 将这个Constructor的调用加入到Callsite中
	 */
	/////////////////////Fields/////////////////////////
	private Location loc;
	private List<SootMethod> mAppCallees;
	private Context callingContext;
	private CFGNode srccfgNode;
	private SootMethod caller;
	// Mapping to MethodNode 
	
	
	private List<SootMethod> mLibCallees;
	
	
	
	/////////////////////Constructor//////////////////////
	
	public CallSite(Location loc, List<SootMethod> sortedAppTgts,
			List<SootMethod> sortedLibTgts,SootMethod mNode,CFGNode cfgNode) {
		// TODO All are initially set,expect the CFGNode, which is set at the control flow part
		
		this.loc = loc;
		this.mAppCallees = sortedAppTgts;
		this.mLibCallees = sortedLibTgts;
		this.srccfgNode = cfgNode;
		this.caller = mNode;
		
	}
	
	/////////////////////////////////////////////////////
	
	
	
	
	////////////////////Function Methods/////////////////
	public Location getLoc() { return loc; }
	public List<SootMethod> getAppCallees() { return mAppCallees; }
	public List<SootMethod> getLibCallees() { return mLibCallees; }
	public List<SootMethod> getAllCallees() {
		@SuppressWarnings("unchecked")
		ArrayList<SootMethod> clone =  (ArrayList<SootMethod>) ((ArrayList<SootMethod>) mAppCallees).clone();
		ArrayList<SootMethod> allCallees = clone;
		allCallees.addAll(mLibCallees);
		return allCallees;
	}
	public void setCallingContext(Context<SootMethod,CFGNode,PointsToGraph> callingContext){
		this.callingContext = callingContext;
	}

	// This will be initialized at CFG part
	public void setCFGNode(CFGNode cfgNode){
		this.srccfgNode = cfgNode;
		this.srccfgNode.setCallSite(this);
	}
	public Context<SootMethod,CFGNode,PointsToGraph>getCallingContext(){
		if(this.callingContext==null){
			System.err.println("Warning! get a null context");
		}
		return this.callingContext;
	}
	public boolean hasAppCallees() { return !mAppCallees.isEmpty(); }
	public boolean hasLibCallees() { return !mLibCallees.isEmpty(); }
	public String toString() {
		return "CS( " + loc + " , APP" + mAppCallees + " LIB" + mLibCallees + " )";
	}
	public CFGNode getCallCFGNode(){return srccfgNode;}
	public SootMethod getCallerMethod(){return caller;}
	/////////////////////////////////////////////////////




	@Override
	public int compareTo(CallSite o) {
		// TODO Auto-generated method stub
		return this.callingContext.getId() - o.callingContext.getId();
	}
	
	
}
