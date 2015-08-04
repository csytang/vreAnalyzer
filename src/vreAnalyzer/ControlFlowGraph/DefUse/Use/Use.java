package vreAnalyzer.ControlFlowGraph.DefUse.Use;

import java.util.Comparator;

import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;
import vreAnalyzer.ControlFlowGraph.Branch;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.StdVariable;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.StmtTag;

public abstract class Use {
	protected Variable var; // variable that wraps value
	public Variable getVar() { return var; }
	public Value getValue() { return var.getValue(); }
	
	/** Default: creates StdVar, not ObjVar. */
	public Use(Value v) { this.var = new StdVariable(v); }
	protected Use(Variable var) { this.var = var; }
	/** Returns use's node (or starting node) */
	public abstract CFGNode getSrcNode();
	/** Returns use's node if use corresponds to a node; null if use's src is actually a branch. */
	public abstract CFGNode getN();
	/** Returns use's branch if use corresponds to a branch; null otherwise */
	public abstract Branch getBranch();
	public boolean isRetUse() { return var.getValue() instanceof InvokeExpr; }
	public boolean isInCatchBlock() { return getSrcNode().isInCatchBlock(); }
	
	/** The purpose is to produce always the uses (and DUAs) in the same order */
	public static class UseComparator implements Comparator<Use> {
		public int compare(Use o1, Use o2) {
			// compare containing methods first
			SootMethod m1 = (SootMethod)((StmtTag) o1.getSrcNode().getStmt().getTag(StmtTag.TAG_NAME)).getSootMethod();
			SootMethod m2 = (SootMethod)((StmtTag) o2.getSrcNode().getStmt().getTag(StmtTag.TAG_NAME)).getSootMethod();
			if (m1 != m2)
				return (ProgramFlowBuilder.inst().getMethodIdx(m1) < ProgramFlowBuilder.inst().getMethodIdx(m2))? -1 : 1;
			
			return o1.toString().compareTo(o2.toString());
		}
	}
}
