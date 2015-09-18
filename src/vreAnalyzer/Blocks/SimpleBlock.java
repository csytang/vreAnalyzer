package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;

public class SimpleBlock extends CodeBlock{
	public static Map<List<CFGNode>,SimpleBlock>valuepool = new HashMap<List<CFGNode>,SimpleBlock>();
	private static Map<SootMethod,List<SimpleBlock>>methodToBlocks = new HashMap<SootMethod,List<SimpleBlock>>();
	private List<CFGNode>blocks;
	
	
	public SimpleBlock(List<CFGNode> cfgnodes,SootMethod method,int blockId,int parentId){
		super();
		blocks = new LinkedList<CFGNode>();
		blocks.addAll(cfgnodes);
		super.setBlockId(blockId);
		super.setBlocks(blocks);
		super.setMethod(method);
		super.setSootClass(method.getDeclaringClass());
		super.setType(BlockType.Stmt);
		super.setParentId(parentId);
		valuepool.put(blocks, this);
		if(methodToBlocks.containsKey(method)){
			methodToBlocks.get(method).add(this);
		}else{
			List<SimpleBlock>blocks = new LinkedList<SimpleBlock>();
			blocks.add(this);
			methodToBlocks.put(method, blocks);
		}
	}
	
	public static SimpleBlock tryToCreate(List<CFGNode> cfgnodes,SootMethod method,int parentId){
		int id = 0;
		List<SimpleBlock> methodBlocks = null;
		if(valuepool.containsKey(cfgnodes)){
			SimpleBlock exist = valuepool.get(cfgnodes);
			return exist;
		}else if((methodBlocks = methodToBlocks.get(method))!=null){
			for(SimpleBlock block:methodBlocks){
				if(block.getCFGNodes().containsAll(cfgnodes)){
					parentId = block.getBlockId();
					break;
				}
			}
			id = BlockGenerator.getBlockId();
			BlockGenerator.increaseId();
			return new SimpleBlock(cfgnodes,method,id,parentId);
		}else{
			id = BlockGenerator.getBlockId();
			BlockGenerator.increaseId();
			return new SimpleBlock(cfgnodes,method,id,parentId);
		}
	}
	public static SimpleBlock createTemp(CFGNode cfgnode,SootMethod method,int parentId){
		List<CFGNode>list = new LinkedList<CFGNode>();
		list.add(cfgnode);
		int temperateId = -1;
		return new SimpleBlock(list,method,temperateId,parentId);
	}
	
	public static SimpleBlock tryToCreate(CFGNode cfgnode,SootMethod method,int parentId){
		int id = 0;
		if(methodToBlocks.containsKey(method)){
			List<SimpleBlock>blocks = methodToBlocks.get(method);
			for(SimpleBlock block:blocks){
				if(block.getCFGNodes().contains(cfgnode))
				{
					if(block.getCFGNodes().size()==1)
						return block;
					else
						parentId = block.getBlockId();
				}
			}
			id = BlockGenerator.getBlockId();
			BlockGenerator.increaseId();
			List<CFGNode>list = new LinkedList<CFGNode>();
			list.add(cfgnode);
			return new SimpleBlock(list,method,id,parentId);
		}else{
			id = BlockGenerator.getBlockId();
			BlockGenerator.increaseId();
			List<CFGNode>list = new LinkedList<CFGNode>();
			list.add(cfgnode);
			return new SimpleBlock(list,method,id,parentId);
		}
	}
	
}
