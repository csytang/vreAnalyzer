package vreAnalyzer.Reuse.Checking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AnyNewExpr;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.Use;
import vreAnalyzer.PointsTo.PointsToGraph;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class PointsToDefUseChecking {
	/**
	 *  Rigid Mode is {@link PointsToGraph} equals 
	 * @param curr
	 * @param obj
	 * @return
	 */
	// Coarse Mode
	public static boolean P2DefUseGroughEquals(PointsToGraph currPointsTo,Object obj,SootMethod src,SootMethod other,NodeDefUses srcCFGNode, NodeDefUses otherCFGNode){
		if(obj == null && currPointsTo == null){
			return true;
		}
		if (!(obj instanceof PointsToGraph))
			return false;
		PointsToGraph otherPointsTo = (PointsToGraph) obj;
		/////////////////////Def and Use//////////////////////////
		List<Use>srcCurrAllUses = new LinkedList<Use>();
		List<Use>otherCurrAllUses = new LinkedList<Use>();
		//CFGDefUse 
		CFGDefUse srcCFGDefUse = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(src);
		CFGDefUse otherCFGDefUse = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(other);
		srcCurrAllUses = srcCFGDefUse.getUsesBefore(srcCFGNode);
		otherCurrAllUses = otherCFGDefUse.getUsesBefore(otherCFGNode);
		//////////////////////PointsTo//////////////////////////
		HashMap<Local,Set<AnyNewExpr>> currroots = currPointsTo.getRoots();
		HashMap<Local,Set<AnyNewExpr>> otherroots = otherPointsTo.getRoots();
		if (currroots == null) {
			if (otherroots != null){
				return false;
			}else
				return true;
		}
		
		Set<Local> rootKeys= new HashSet<Local>(currroots.keySet());
		Set<Local> otherrootKeys = new HashSet<Local>(otherroots.keySet());
		///////////////////////////////////////////////////////
		if(rootKeys.containsAll(otherrootKeys)){
			if(srcCurrAllUses.containsAll(otherCurrAllUses))
				return true;
			else
				return false;
		}else if(otherrootKeys.containsAll(rootKeys)){
			if(otherCurrAllUses.containsAll(srcCurrAllUses))
				return true;
			else
				return false;
		}else
			return false;
	}
	
}
