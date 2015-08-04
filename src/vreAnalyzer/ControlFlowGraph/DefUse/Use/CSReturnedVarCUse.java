package vreAnalyzer.ControlFlowGraph.DefUse.Use;

import soot.jimple.InvokeExpr;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.CSReturnedVar;
import vreAnalyzer.Elements.CallSite;

public class CSReturnedVarCUse extends CUse {
	public CSReturnedVarCUse(InvokeExpr vInvExpr, CallSite cs, NodeDefUses n) {
		super(new CSReturnedVar(vInvExpr, cs), n); }
}