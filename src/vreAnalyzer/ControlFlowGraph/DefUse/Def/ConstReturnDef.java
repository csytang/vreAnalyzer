package vreAnalyzer.ControlFlowGraph.DefUse.Def;

import soot.jimple.Constant;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.ReturnVar;

public class ConstReturnDef  extends Def {
	@Override
	public boolean isComputed() { return false; }
	public ConstReturnDef(Constant constVal, NodeDefUses n) {
		super(new ReturnVar(constVal, n), n); }
}
