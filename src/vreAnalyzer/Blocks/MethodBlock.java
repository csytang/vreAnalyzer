package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;

public class MethodBlock extends CodeBlock{
	public static Map<SootMethod,MethodBlock>methodpool = new HashMap<SootMethod,MethodBlock>();
	public MethodBlock(List<CFGNode>cfgNodes,SootMethod method,int blockId,int parentId){
		super();
		super.setType(BlockType.Method);
		super.setBlocks(new LinkedList<CFGNode>(cfgNodes));
		super.setMethod(method);
		super.setSootClass(method.getDeclaringClass());
		super.setBlockId(blockId);
		super.setParentId(parentId);
		methodpool.put(method, this);
		
	}
	public static MethodBlock tryToCreate(List<CFGNode>cfgNodes,SootMethod method,int parentId){
		if(methodpool.containsKey(method)){
			MethodBlock methodb = methodpool.get(method);
			
			return methodb;
		}else{
			int id = BlockGenerator.getBlockId();
			BlockGenerator.increaseId();
			return new MethodBlock(cfgNodes,method,id,parentId);
		}
	}
	public static MethodBlock getMethodBlock(SootMethod method){
		if(methodpool.containsKey(method))
			return methodpool.get(method);
		else
			return null;
	}
}
