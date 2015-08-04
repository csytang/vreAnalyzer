package vreAnalyzer.ControlFlowGraph.DefUse.Def;

import java.util.Comparator;

import soot.Value;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.StdVariable;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.StmtTag;

public class Def {
	private Variable var; // value
	private CFGNode n; // location
	
	public Value getValue() { return var.getValue(); }
	public Variable getVar() { return var; }
	public CFGNode getN() { return n; }
	
	/** Tells whether uses or constants at def node are used to compute def value.
	 *  Note: in 'a = 1', 'a' is computed, because it's assigned; in 'm(1)', '1' is NOT computed. */
	public boolean isComputed() { return true; }
	
	/** Default: creates StdVar, not ObjVar. */
	public Def(Value v, CFGNode n) { this.var = new StdVariable(v); this.n = n; }
	/** Takes var as param, so var can be ObjVar instead of StdVar. */
	public Def(Variable var, CFGNode n) { this.var = var; this.n = n; }
	
	// Pattern[Def Variable @ MethodID[StatementID] ]
	@Override
	public String toString() {
		return var + "@" + ProgramFlowBuilder.inst().getContainingMethodIdx(n) +
			"[" + ((n.getStmt() == null)? "EN" : ((StmtTag)n.getStmtTag()).getIdxInMethod()) + "]";
	}
	public boolean isInCatchBlock() { return n.isInCatchBlock(); }
	
	/** The purpose is to produce always the defs (and DUAs) in the same order */
	public static class DefComparator implements Comparator<Def> {
		public int compare(Def o1, Def o2) {
			// compare containing methods first
			CFG cfg1 = ProgramFlowBuilder.inst().getContainingCFG(o1.getN());
			CFG cfg2 = ProgramFlowBuilder.inst().getContainingCFG(o2.getN());
			if (cfg1 != cfg2)
				return (ProgramFlowBuilder.inst().getMethodIdx(cfg1.getMethod()) < ProgramFlowBuilder.inst().getMethodIdx(cfg2.getMethod()))? -1 : 1;
			
			return o1.toString().compareTo(o2.toString());
		}
	}
}
