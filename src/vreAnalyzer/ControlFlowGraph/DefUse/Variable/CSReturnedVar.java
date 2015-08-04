package vreAnalyzer.ControlFlowGraph.DefUse.Variable;

import soot.jimple.InvokeExpr;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.StmtTag;

/** Represents special kind of "variable" returned by method call at caller site, to facilitate data-dependence analysis */
public final class CSReturnedVar extends StdVariable {
	private CallSite cs; // location of call at which var is returned
	public CSReturnedVar(InvokeExpr vInvExpr, CallSite cs) { super(vInvExpr); this.cs = cs; }
	@Override
	public boolean equals(Object obj) { return ((obj instanceof CSReturnedVar) && ((CSReturnedVar)(obj)).cs.equals(this.cs)); }
	@Override
	public int hashCode() { return cs.hashCode(); }
	@Override
	public String toString() { return "retvarM" + ProgramFlowBuilder.inst().getMethodIdx(cs.getLoc().getMethod()) + 
		"S" + ((StmtTag)cs.getLoc().getStmt().getTag(StmtTag.TAG_NAME)).getIdxInMethod(); }
	
	@Override public boolean isReturnedVar() { return true; }
	@Override public boolean isDefinite() { return true; }
	@Override public boolean isKillable() { return false; }
}