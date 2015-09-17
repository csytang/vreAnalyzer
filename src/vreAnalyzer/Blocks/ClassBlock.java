package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;

public class ClassBlock extends CodeBlock{
	
	public static Map<SootClass,ClassBlock>clspool = new HashMap<SootClass,ClassBlock>();
	private ClassBlock(List<CFGNode>cfgNodes,SootClass cls,int blockId){
		super();
		super.setBlockId(blockId);
		super.setType(BlockType.Class);
		super.setBlocks(new LinkedList<CFGNode>(cfgNodes));
		super.setSootClass(cls);
		
		clspool.put(cls, this);
	}
	public static ClassBlock tryToCreate(List<CFGNode>cfgNodes,SootClass cls){
		if(clspool.containsKey(cls)){
			return clspool.get(cls);
		}else{
			int id = BlockGenerator.getBlockId();
			BlockGenerator.increaseId();
			return new ClassBlock(cfgNodes,cls,id);
		}
	}
}
