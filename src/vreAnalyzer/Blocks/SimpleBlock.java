package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import soot.SootMethod;
import soot.jimple.IdentityRef;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import Patch.Hadoop.ReuseAssets.AssetType;

public class SimpleBlock extends CodeBlock{
	public static Map<CFGNode,SimpleBlock>valuepool = new HashMap<CFGNode,SimpleBlock>();
	private List<CFGNode>blocks;
	
	
	public SimpleBlock(Variable variable,CFGNode cfgNode,SootMethod method){
		super();
		super.addValue(variable);
		if(variable.isLocal()){
			super.setType(AssetType.Local);
		}else if(variable.isFieldRef()){
			super.setType(AssetType.Field);
		}else if(variable.getValue() instanceof IdentityRef){
			super.setType(AssetType.Argument);
		}
		blocks = new LinkedList<CFGNode>();
		blocks.add(cfgNode);
		super.setBlocks(blocks);
		super.setMethod(method);
		super.setSootClass(method.getDeclaringClass());
		valuepool.put(cfgNode, this);
	}
	public static SimpleBlock tryToCreate(Variable variable,CFGNode cfgNode,SootMethod method){
		if(valuepool.containsKey(cfgNode)){
			SimpleBlock exist = valuepool.get(cfgNode);
			if(!exist.getValues().contains(variable))
				exist.addValue(variable);
			return valuepool.get(variable);
		}else
			return new SimpleBlock(variable,cfgNode,method);
	}
}
