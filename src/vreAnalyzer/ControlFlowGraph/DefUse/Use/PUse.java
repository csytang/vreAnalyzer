package vreAnalyzer.ControlFlowGraph.DefUse.Use;

import soot.Value;
import vreAnalyzer.ControlFlowGraph.Branch;
import vreAnalyzer.Elements.CFGNode;

public class  PUse extends Use {
	private Branch br; // location
	
	public PUse(Value v, Branch br) { super(v); this.br = br; }
	
	@Override
	public CFGNode getSrcNode() { return br.getSrc(); }
	@Override
	public CFGNode getN() { return null; }
	@Override
	public Branch getBranch() { return br; }
	
	@Override
	public String toString() {
		return var + "@" + br;
	}
}