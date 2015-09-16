package vreAnalyzer;
import java.util.LinkedList;
import java.util.List;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Blocks.MethodBlock;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.MethodTag;

public class BlockGenerator {
	public static BlockGenerator instance;
	public static BlockGenerator inst(List<SootClass> appClasses){
		if(instance==null)
			instance = new BlockGenerator(appClasses);
		return instance;
	}
	public BlockGenerator(List<SootClass> appClasses){
		for(SootClass cls:appClasses){
			List<SootMethod>clsmethods = cls.getMethods();
			List<CFGNode>classallnode = new LinkedList<CFGNode>();
			for(SootMethod method:clsmethods){
				if(method.getTag(MethodTag.TAG_NAME)!=null){
					// 1. Create method block
					List<CFGNode>nodes = ProgramFlowBuilder.inst().getCFG(method).getNodes();
					MethodBlock.tryToCreate(nodes, method);
					classallnode.addAll(nodes);
				}
			}
			// 2. Create class block
			
		}
	}
}
