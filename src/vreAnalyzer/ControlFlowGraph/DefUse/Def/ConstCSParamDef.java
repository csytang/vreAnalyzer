package vreAnalyzer.ControlFlowGraph.DefUse.Def;

import soot.jimple.Constant;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.CSArgVar;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;

public class ConstCSParamDef  extends Def {
	@Override
	public boolean isComputed() { return false; }
	public ConstCSParamDef(Constant constVal, CallSite cs, int argIdx, CFGNode n) {
		super(new CSArgVar(constVal, cs, argIdx), n); }
}
