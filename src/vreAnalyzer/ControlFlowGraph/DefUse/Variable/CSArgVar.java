package vreAnalyzer.ControlFlowGraph.DefUse.Variable;

import soot.Value;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.StmtTag;
/** Represents method-call argument (actual parameter) as a special variable at call site and callee's identity statements */
public class CSArgVar extends StdVariable {
	private final CallSite cs; // location of call where this var is a param
	private final int argIdx; // position in argument list at call site
	public CSArgVar(Value val, CallSite cs, int argIdx) { super(val); this.cs = cs; this.argIdx = argIdx; }
	/** cs and argIdx are enough; don't use parent's val equality because of StringConstant's duality
	 *  (represents string ref and string itself created automatically for raw-string param) */
	@Override public boolean equals(Object obj) { return
		((obj instanceof CSArgVar) && ((CSArgVar)(obj)).cs.equals(this.cs) && ((CSArgVar)(obj)).argIdx == this.argIdx); }
	/** cs and argIdx are enough; don't use parent's hashCode because of StringConstant's duality
	 *  (represents string ref and string itself created automatically for raw-string param) */
	@Override public int hashCode() { return cs.hashCode() + argIdx; }
	@Override public String toString() { return "arg" + val + "I" + argIdx + "M" + ProgramFlowBuilder.inst().getMethodIdx(cs.getLoc().getMethod()) +
		"S" + ((StmtTag)cs.getLoc().getStmt().getTag(StmtTag.TAG_NAME)).getIdxInMethod(); }
	@Override public boolean isKillable() { return false; }
}
