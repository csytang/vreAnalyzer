package vreAnalyzer.ControlFlowGraph;

import java.util.Comparator;

import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

/**
 * At the control flow it is the branch, if any
 * @author tangchris
 *
 */
public class Branch {
	protected CFGNode src;
	protected CFGNode tgt; //@ invariant tgt != null
	
	public CFGNode getSrc() { return src; }
	public CFGNode getTgt() { return tgt; }
	
	public Branch(CFGNode src, CFGNode tgt) {
		this.src = src;
		this.tgt = tgt;
	}
	
	@Override
	public int hashCode() { return ((src == null)? 0 : src.hashCode()) + tgt.hashCode(); }
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Branch))
			return false;
		
		Branch brOther = (Branch) obj;
		return this.src == brOther.src && this.tgt == brOther.tgt;
	}
	@Override
	public String toString() {
		String str;
		if (src == null)
			str = "MTD_EN";
		else {
			final String srcSId = src.getIdStringInMethod();
			final int srcMIdx = ProgramFlowBuilder.inst().getContainingMethodIdx(src);
			str = srcMIdx + "[" + srcSId + "]";
		}
		final String tgtSId = tgt.getIdStringInMethod();
		final int tgtMIdx = ProgramFlowBuilder.inst().getContainingMethodIdx(tgt);
		return str + "->" + tgtMIdx + "[" + tgtSId + "]";
	}
	
	public static class BranchComparator implements Comparator<Branch> {
		public int compare(Branch br1, Branch br2) {
			// compare containing methods first
			CFG cfg1 = ProgramFlowBuilder.inst().getContainingCFG(br1.getTgt());
			CFG cfg2 = ProgramFlowBuilder.inst().getContainingCFG(br2.getTgt());
			if (cfg1 != cfg2)
				return (ProgramFlowBuilder.inst().getMethodIdx(cfg1.getMethod()) < ProgramFlowBuilder.inst().getMethodIdx(cfg2.getMethod()))? -1 : 1;
			
			return br1.toString().compareTo(br2.toString());
		}
	}
}
