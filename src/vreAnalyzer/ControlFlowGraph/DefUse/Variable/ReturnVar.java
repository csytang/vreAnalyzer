package vreAnalyzer.ControlFlowGraph.DefUse.Variable;

import soot.Value;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.StmtTag;

/** Represents constant or local at return statement as a special kind of var (for data-dependence analysis purposes) */
public final class ReturnVar extends StdVariable {
	private final CFGNode retNode; // id within containing CFG
	public ReturnVar(Value val, CFGNode retNode) {
		super(val);
		this.retNode = retNode;
		assert isLocalOrConst();
	}
	@Override
	public boolean equals(Object obj) {
		return obj instanceof ReturnVar && super.equalsImpl((ReturnVar)obj) && ((ReturnVar)(obj)).retNode == this.retNode; }
	@Override
	public int hashCode() { return super.hashCode() + retNode.hashCode(); }
	@Override
	public String toString() { return "retvarV" + val +
		"S" + ((StmtTag)retNode.getStmt().getTag(StmtTag.TAG_NAME)).getIdxInMethod(); }
	@Override public boolean isKillable() { return false; }
}