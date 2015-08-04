package vreAnalyzer.ControlFlowGraph.DefUse.Use;

import soot.jimple.Constant;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.CSArgVar;
import vreAnalyzer.Elements.CallSite;

public class CSConstParamCUse extends CUse {
	public CSConstParamCUse(Constant constVal, CallSite cs, int argIdx, NodeDefUses n) {
		super(new CSArgVar(constVal, cs, argIdx), n); }
}