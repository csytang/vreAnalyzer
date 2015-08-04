package vreAnalyzer.ControlFlowGraph.DefUse.Use;

import soot.Value;
import vreAnalyzer.ControlFlowGraph.Branch;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class CUse extends Use {
	private NodeDefUses n; // location
	
	/** Default: creates StdVar, not ObjVar. */
	public CUse(Value v, NodeDefUses n) { super(v); this.n = n; }
	/** Takes var as param, so var can be ObjVar instead of StdVar. */
	public CUse(Variable var, NodeDefUses n) { super(var); this.n = n; }
	
	@Override
	public CFGNode getSrcNode() { return n; }
	@Override
	public CFGNode getN() { return n; }
	@Override
	public Branch getBranch() { return null; }
	
	@Override
	public String toString() {
		return var + "@" + ProgramFlowBuilder.inst().getContainingMethodIdx(n) +
			"[" + n.getStmtTag().getIdxInMethod() + "]";
	}
}
