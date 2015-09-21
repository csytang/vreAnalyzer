package vreAnalyzer.ControlFlowGraph.DefUse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse.VariableComparator;
import vreAnalyzer.ControlFlowGraph.DefUse.Def.Def;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.Use;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.StmtTag;

public  class NodeDefUses extends CFGNode {
	///////////////////////////////Field////////////////////////////////////////////////
	// Array of defs and uses in id
	protected int[] localDefsIds;// = Def.NODEF; // NODEF if no def
	protected int[] localUsesIds;
	////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////Member Function/////////////////////////////////////////
	public void setLocalDefsIds(int[] localDefsIds) { this.localDefsIds = localDefsIds; }
	public int[] getLocalDefsIds() { return localDefsIds; }
	public void setLocalUsesIds(int[] localUsesIds) { this.localUsesIds = localUsesIds; }
	public int[] getLocalUsesIds() { return localUsesIds; }
	
	/** Includes ALL uses of NON-constants: locals, fields, array elems, objects (incl. str-const lib params) */
	public List<Variable> getUsedVars() {
		Set<Variable> varsSet = new HashSet<Variable>();
		// get list of local variables used
		CFGDefUse cfgDU = (CFGDefUse) ProgramFlowBuilder.inst().getContainingCFG(this);
		for (int uId : getLocalUsesIds()) {
			Variable v = cfgDU.getUses().get(uId).getVar();
			varsSet.add(v);
		}
		// add used field and array-element variables
		for (Use u : cfgDU.getFieldUses())
			if (u.getN() == this)
				varsSet.add(u.getVar());
		for (Use u : cfgDU.getArrayElemUses())
			if (u.getN() == this)
				varsSet.add(u.getVar());
		List<Variable> sortedVarsList = new ArrayList<Variable>(varsSet);
		Collections.sort(sortedVarsList, new VariableComparator());
		return sortedVarsList;
	}
	
	
	/** Includes ALL defs of NON-constants: locals, fields, array elems, objects (incl. str-const lib params) */
	public List<Variable> getDefinedVars() {
		Set<Variable> varsSet = new HashSet<Variable>();
		// get list of local variables defined
		CFGDefUse cfgDU = (CFGDefUse) ProgramFlowBuilder.inst().getContainingCFG(this);
		for (int dId : getLocalDefsIds()) {
			Variable v = cfgDU.getDefs().get(dId).getVar();
			varsSet.add(v);
		}
		// add defined field and array-element variables
		for (Def d : cfgDU.getFieldDefs())
			if (d.getN() == this)
				varsSet.add(d.getVar());
		for (Def d : cfgDU.getArrayElemDefs())
			if (d.getN() == this)
				varsSet.add(d.getVar());
		
		
		List<Variable> sortedVarsList = new ArrayList<Variable>(varsSet);
		Collections.sort(sortedVarsList, new VariableComparator());
		return sortedVarsList;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////Constructor/////////////////////////////////////////////
	public NodeDefUses(StmtTag sTag,SootMethod sm) {
		super(sTag,sm);
		// TODO Auto-generated constructor stub
	}
	/////////////////////////////////////////////////////////////////////////////////////
	
}