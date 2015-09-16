package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;
import Patch.Hadoop.ReuseAssets.AssetType;

public class ClassBlock extends CodeBlock{
	
	public static Map<SootClass,ClassBlock>clspool = new HashMap<SootClass,ClassBlock>();
	private ClassBlock(List<CFGNode>cfgNodes,SootClass cls){
		super();
		super.setType(AssetType.Class);
		super.setBlocks(new LinkedList<CFGNode>(cfgNodes));
		super.setSootClass(cls);
		clspool.put(cls, this);
	}
	public static ClassBlock tryToCreate(List<CFGNode>cfgNodes,SootClass cls){
		if(clspool.containsKey(cls)){
			return clspool.get(cls);
		}else
			return new ClassBlock(cfgNodes,cls);
	}
}
