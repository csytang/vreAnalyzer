package vreAnalyzer.Reuse.Checking;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.SootMethod;
import soot.jimple.AnyNewExpr;
import soot.jimple.Ref;
import soot.jimple.ThisRef;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.Use;
import vreAnalyzer.PointsTo.PointsToGraph;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class PointsToDefUseChecking {
	private static boolean verbose = false;
	/**
	 *  Rigid Mode is {@link PointsToGraph} equals 
	 * @param curr
	 * @param obj
	 * @return
	 */
	// Coarse Mode
	public static boolean P2DefUseGroughEquals(PointsToGraph currPointsTo,Object obj,SootMethod src,SootMethod other,NodeDefUses srcCFGNode, NodeDefUses otherCFGNode){
		if (obj == null){
			return false;
		}
		if (!(obj instanceof PointsToGraph))
			return false;
		PointsToGraph otherPointsTo = (PointsToGraph) obj;
		if(verbose){
			System.out.println();
			System.out.println("Compare points to graph");
			System.out.println(currPointsTo.toString());
			System.out.println("-------AND------------");
			System.out.println(obj.toString());
			System.out.println();
		}
		
		/////////////////////Def and Use//////////////////////////
		List<Use>srcCurrAllUses = new LinkedList<Use>();
		List<Use>otherCurrAllUses = new LinkedList<Use>();
		//CFGDefUse 
		CFGDefUse srcCFGDefUse = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(src);
		CFGDefUse otherCFGDefUse = (CFGDefUse) ProgramFlowBuilder.inst().getCFG(other);
		
		srcCurrAllUses = srcCFGDefUse.getUsesBefore(srcCFGNode);
		otherCurrAllUses = otherCFGDefUse.getUsesBefore(otherCFGNode);
		
			
		/////////////////////////////////////////////////////////
		
		
		//////////////////////PointsTo//////////////////////////
		HashMap<Local,Set<AnyNewExpr>> currroots = currPointsTo.getRoots();
		HashMap<Local,Set<AnyNewExpr>> otherroots = otherPointsTo.getRoots();
		

		if (currroots == null) {
			if (otherroots != null){
				if(verbose)
					System.out.println("return false@52");
				return false;
			}
		}
		
		Set<Local> rootKeys= currroots.keySet();
		Set<Local> otherrootKeys = otherroots.keySet();
		
		///////////////////////////////////////////////////////
		if(rootKeys.size()>=otherrootKeys.size()){
			for(Local loroot:otherrootKeys){
				Set<AnyNewExpr>varExprs = otherroots.get(loroot);
				if(currroots.values().contains(varExprs));
				else
					return false;
				if(otherCurrAllUses.contains(loroot)){
					for (Local rootlocal : rootKeys) {
						if(currroots.get(loroot).equals(varExprs)){
							if(srcCurrAllUses.contains(rootlocal)){
								
							}
							else
								return false;
							break;
						}
					}
				}
			}
			return true;
		}else{
			for (Local loroot : rootKeys) {
				Set<AnyNewExpr>varExprs = currroots.get(loroot);
				if(otherroots.values().contains(varExprs));
				else{
					return false;
				}
				if(srcCurrAllUses.contains(loroot)){
					for (Local rootlocal : otherrootKeys) {
						if(otherroots.get(loroot).equals(varExprs)){
							if(otherCurrAllUses.contains(rootlocal));
							else
								return false;
							break;
						}
					}
				}
			}
			return true;
		}
	}
	
}
