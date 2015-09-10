package vreAnalyzer.Elements;

import java.util.ArrayList;
import java.util.List;
import soot.SootMethod;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.Branch;
import vreAnalyzer.Tag.StmtTag;

public class CFGNode {
	
	///////////////////////Field///////////////////////////////////
	protected StmtTag sTag;
	
	protected ArrayList<CFGNode> preds = new ArrayList<CFGNode>();
	protected ArrayList<CFGNode> succs = new ArrayList<CFGNode>();
	protected CFGNode fallThroughTgt = null;
	private boolean isCallSite;
	private CallSite callsite = null;
	private SootMethod sm;
	private ArrayList<Branch> outBranches = null; // created on demand
	private boolean verbose = true;
	///////////////////////////////////////////////////////////////
	
	////////////////////Constructor///////////////////////////////
	public CFGNode(StmtTag sTag,SootMethod sm) 
	{ 
		this.sTag = sTag;
		this.isCallSite = false;
		this.sm = sm;
		
		// FINISH
	}
	//////////////////////////////////////////////////////////////
	
	////////////////////Member Functions/////////////////////////
	/** String containing short id of node in CFG. This implementation uses 0-based index in node list */
	public String getIdStringInMethod() {
		return Integer.toString(getIdInMethod());
	}
	
	
	/** String containing short id of node in CFG. This implementation uses 0-based index in node list */
	public int getIdInMethod() {
		return sTag.getIdxInMethod();
	}
	
	// Whether it is in catch block, agent it to sNode
	public boolean isInCatchBlock() {return sTag!=null && sTag.getStatement() != null && sTag.isInCatchBlock();}
	
	public boolean isSpecial() { assert sTag.getStatement() != null; return false; }
	
	public ArrayList<CFGNode> getPreds() { return preds; }
	public ArrayList<CFGNode> getSuccs() { return succs; }
	public CFGNode getFallThroughTgt() { return fallThroughTgt; }
	public void setFallThroughTgt(CFGNode tgt){ fallThroughTgt = tgt;}
	public void addPred(CFGNode n) { if (!preds.contains(n)) preds.add(n); }  // avoid multiple connections to a predecessor
	public void addSucc(CFGNode n) { if (!succs.contains(n)) succs.add(n); }  // avoid multiple connections to a successor
	public List<Branch> getOutBranches() { return outBranches; }
	public void addOutBranch(Branch outBr) {
		if (outBranches == null)
			outBranches = new ArrayList<Branch>();
		outBranches.add(outBr);
	}
	public Stmt getStmt(){
		if(this.sTag==null)
			return null;
		else
		return this.sTag.getStatement();
	}
	public SootMethod getMethod(){return sm;}
	public StmtTag getStmtTag(){return this.sTag;}
	public String toString(){
		return sTag.getStatement().toString();
	}
	public void setCallSite(CallSite cs){
		this.isCallSite = true;
		this.callsite = cs;
	}
	public CallSite getCallSite(){
		if(isCallSite){
			return callsite;
		}else{
			return null;
		}
	}
	//////////////////////////////////////////////////////////////

	
	

	
}
