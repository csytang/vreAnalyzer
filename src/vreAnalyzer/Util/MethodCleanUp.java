package vreAnalyzer.Util;

import java.util.List;
import java.util.Map;
import soot.SootMethod;
import vreAnalyzer.Context.Context;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.PointsTo.PointsToGraph;

/**
 * MethodCleanup will:
 * 1. clean multiple returns generated when combining
 * 2. use points to graph to clear multiple agents into one
 * 3.  
 * 
 * @author tangchris
 *
 */
public class MethodCleanUp {
	public MethodCleanUp(){
		
	}
	private static MethodCleanUp instance;
	public static MethodCleanUp inst(){
		if(instance == null)
			instance = new MethodCleanUp();
		return instance;
	}
	public void clean(SootMethod integrated,SootMethod sourceA, SootMethod sourceB,Map<Object, Object> reflection){
		// 1. obtain the PointsToGraph of sourceA and sourceB respectively
		List<Context<SootMethod,CFGNode,PointsToGraph>> contextAList = PointsToAnalysis.inst().getContexts().get(sourceA);
		List<Context<SootMethod,CFGNode,PointsToGraph>> contextBList = PointsToAnalysis.inst().getContexts().get(sourceB);
		
		
		// 2. 
		
		
	}
	
}
