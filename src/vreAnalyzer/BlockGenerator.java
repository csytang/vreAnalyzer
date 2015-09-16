package vreAnalyzer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Blocks.ClassBlock;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Blocks.MethodBlock;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.MethodTag;

public class BlockGenerator {
	public static BlockGenerator instance;
	private static ArrayList<CodeBlock> blockpool = new ArrayList<CodeBlock>();
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
					CFG cfg = ProgramFlowBuilder.inst().getCFG(method);
					List<CFGNode>nodes = cfg.getNodes();
					MethodBlock.tryToCreate(nodes, method);
					classallnode.addAll(nodes);
					// 2. Create inside method blocks
					/**
					 * Strategy:
					 * 1. go through the control flow, find the branch, create separate block for each branch
					 * 
					 * 2. 
					 */
					CFGNode entry = cfg.ENTRY;
					List<CFGNode>temp = new LinkedList<CFGNode>();
					Stack<CFGNode>analysisstack = new Stack<CFGNode>();
					analysisstack.push(entry);
					while(!analysisstack.isEmpty()){
						CFGNode curr = analysisstack.pop();
						temp.add(curr);
						for(CFGNode next:curr.getSuccs()){
							analysisstack.push(next);
						}
						if(curr.getSuccs().size()>1){
							
						}else if(curr.getSuccs().size()==1){
							temp.add(curr.getSuccs().get(0));
						}
					}
				}
			}
			// 2. Create class block
			ClassBlock.tryToCreate(classallnode, cls);
		}
	}
}
