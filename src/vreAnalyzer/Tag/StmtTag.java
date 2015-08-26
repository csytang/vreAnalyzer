package vreAnalyzer.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.RefLikeType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.Elements.Location;

import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Util.StringBasedComparator;
import vreAnalyzer.Util.Util;



public class StmtTag implements Tag{
	
	
	//////////////////Fields////////////////////////////
	// Predecessors statements
	private ArrayList<Stmt> predecessorStmts;
	
	// Successor statements
	private ArrayList<Stmt> successorStmts;
	
	private Location loc;
	public static String TAG_NAME = "std";
	// Index in method's list of stmts
	private int sIdx; 
	
	// Statement attached
	private SootMethod mNode;
	
	private Stmt stmt;
	// Keeps reference to local call site, if any
	private CallSite callSite = null;
	
	// Whether the statement is in the catch block
	private boolean inCatchBlock = true; // initial lattice value
	////////////////////////////////////////////////////////////
	
	//////////////////////Constructor////////////////////////////
	public StmtTag(SootMethod m, Stmt s, int sIdx) {
		predecessorStmts = new ArrayList<Stmt>(); // will be filled later
		successorStmts = new ArrayList<Stmt>(); // will be filled later
		stmt = s;
		mNode = m;
		loc = new Location(m, s);
		this.sIdx = sIdx;
	}
	
	///////////////////////////////////////////////////////////
	
	///////////////Member Function///////////////////////////////
	public boolean hasPredecessorStmts() { return !predecessorStmts.isEmpty(); }
	public ArrayList<Stmt> getPredecessorStmts() { return predecessorStmts; }
	public void addPredecessorStmt(Stmt pred) { predecessorStmts.add(pred); }
	public boolean hasSuccessorStmts() { return !successorStmts.isEmpty(); }
	public ArrayList<Stmt> getSuccessorStmts() { return successorStmts; }
	public void addSuccessorStmt(Stmt succ) { successorStmts.add(succ); }
	public Location getLocation() { return loc; }
	public int getIdxInMethod() { return sIdx; }
	public Stmt getStatement() {return stmt; }
	public SootMethod getSootMethod() {return mNode;}
	public String toString() {return stmt.toString();}
	/** TEMPORAL */
	public void setInCatchBlock(boolean inCatchBlock) { this.inCatchBlock = inCatchBlock; }
	/** TEMPORAL */
	public boolean isInCatchBlock() { return inCatchBlock; }
	public CallSite getCallSite() {return callSite;}
	//////////////////////////////////////////////////////////////

	
	//////////////Analysis////////////////////////////////////////
	/**
	 * @return Call site describing call performed in this stmt; null if this stmt is not a call
	 * @throws Exception 
	 */
	public CallSite initCallSite() throws Throwable  {
		// TODO Auto-generated method stub
		Stmt sThis = loc.getStmt();
		if (sThis.containsInvokeExpr()) {
			// Invoke expression
			InvokeExpr invokeExpr = sThis.getInvokeExpr();
			
			// Whether it is a invoke expression
			
			if (invokeExpr instanceof VirtualInvokeExpr || invokeExpr instanceof InterfaceInvokeExpr) {
				// Dynamic dispatch; note that SpecialInvokeExpr is handled separately as direct call in else block below
				InstanceInvokeExpr instInvExpr = ((InstanceInvokeExpr) invokeExpr);
				
				// Find all application targets
				// Application target
				Set<SootMethod> mAppTgts = new HashSet<SootMethod>();
				
				// Library target
				Set<SootMethod> mLibTgts = new HashSet<SootMethod>();
				
				// Find and set the concrete call target
				
				
				Util.getConcreteCallTargets(instInvExpr, mAppTgts, mLibTgts);
				
				
				if (mAppTgts.isEmpty() && mLibTgts.isEmpty()) {
					// TODO: See why this happens
					// is fine since the is -cp jar is not processed;
				}
				
				// Create call site, storing it in field
				List<SootMethod> sortedAppTgts = new ArrayList<SootMethod>(mAppTgts);
				Collections.sort(sortedAppTgts, new StringBasedComparator<SootMethod>());//MethodComparator());
				List<SootMethod> sortedLibTgts = new ArrayList<SootMethod>(mLibTgts);
				Collections.sort(sortedLibTgts, new StringBasedComparator<SootMethod>());//MethodComparator());
				callSite = new CallSite(loc, sortedAppTgts, sortedLibTgts,mNode,null);
				
				// Add a call site tag to this statement
				sThis.addTag(new CallSiteTag());
				
				return callSite;
				
			}else{
				// Static or special-instance (<init>) call
				SootMethod mCallee = invokeExpr.getMethodRef().resolve(); // should return method at declaring class or superclass
				assert mCallee != null;
				
				
				// Create call site, storing it in field
				List<SootMethod> singleMethodList = new ArrayList<SootMethod>();
				singleMethodList.add(mCallee);
				if (mCallee != null)
					callSite = new CallSite(loc, singleMethodList, new ArrayList<SootMethod>(),mNode,null);
				else
					callSite = new CallSite(loc, new ArrayList<SootMethod>(), singleMethodList,mNode,null);
				
				// Add a call site tag to this statement
				sThis.addTag(new CallSiteTag());
				
				return callSite;
				
			}
		}
		// No call in this statement
		return null;
	}
	////////////////////////////////////////////////////////////

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return StmtTag.TAG_NAME;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
}
