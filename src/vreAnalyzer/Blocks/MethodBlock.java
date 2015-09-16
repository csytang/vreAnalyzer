package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import Patch.Hadoop.ReuseAssets.AssetType;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;

public class MethodBlock extends CodeBlock{
	public static Map<SootMethod,MethodBlock>methodpool = new HashMap<SootMethod,MethodBlock>();
	public MethodBlock(List<CFGNode>cfgNodes,SootMethod method){
		super();
		super.setType(AssetType.Method);
		super.setBlocks(new LinkedList<CFGNode>(cfgNodes));
		super.setMethod(method);
		super.setSootClass(method.getDeclaringClass());
		methodpool.put(method, this);
	}
	public static MethodBlock tryToCreate(List<CFGNode>cfgNodes,SootMethod method){
		if(methodpool.containsKey(method)){
			return methodpool.get(method);
		}else
			return new MethodBlock(cfgNodes,method);
	}
}
