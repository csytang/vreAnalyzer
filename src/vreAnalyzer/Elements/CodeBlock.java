package vreAnalyzer.Elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import Patch.Hadoop.CommonAss.AssetType;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.IdentityRef;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Tag.BlockMarkedTag;

public class CodeBlock {
	/**
	 * a code block represents a code segment, it could be a single statement,
	 * variable, method, class and even a package.
	 */
	private AssetType blockType = null;
	private List<CFGNode>blocks;
	private Variable variable;// if applicable
	private SootMethod method;// if applicable
	private SootClass cls;
	private BlockMarkedTag bmtag;
	public static Map<SootClass,CodeBlock>clspool = new HashMap<SootClass,CodeBlock>();
	public static Map<SootMethod,CodeBlock>methodpool = new HashMap<SootMethod,CodeBlock>();
	public static Map<CFGNode,CodeBlock>cfgNodepool = new HashMap<CFGNode,CodeBlock>();
	public static Map<Variable,CodeBlock>valuepool = new HashMap<Variable,CodeBlock>();
	private CodeBlock(List<CFGNode>cfgNodes,SootClass cls){
		blockType = AssetType.Class;
		blocks = new LinkedList<CFGNode>(cfgNodes);
		this.cls = cls;
		clspool.put(cls, this);
	}
	public static CodeBlock tryToCreate(List<CFGNode>cfgNodes,SootClass cls){
		if(clspool.containsKey(cls)){
			return clspool.get(cls);
		}else
			return new CodeBlock(cfgNodes,cls);
	}
	private CodeBlock(List<CFGNode>cfgNodes,SootMethod method){
		blockType = AssetType.Method;
		blocks = new LinkedList<CFGNode>(cfgNodes);
		this.method = method;
		this.cls = method.getDeclaringClass();
		methodpool.put(method, this);
	}
	public static CodeBlock tryToCreate(List<CFGNode>cfgNodes,SootMethod method){
		if(methodpool.containsKey(method)){
			return methodpool.get(method);
		}else
			return new CodeBlock(cfgNodes,method);
	}
	private CodeBlock(CFGNode cfgNode,SootMethod method){
		blockType = AssetType.Stmt;
		blocks = new LinkedList<CFGNode>();
		blocks.add(cfgNode);
		this.method = method;
		this.cls = method.getDeclaringClass();
		cfgNodepool.put(cfgNode, this);
	}
	public static CodeBlock tryToCreate(CFGNode cfgNode,SootMethod method){
		if(cfgNodepool.containsKey(cfgNode)){
			return methodpool.get(cfgNode);
		}else
			return new CodeBlock(cfgNode,method);
	}
	private CodeBlock(Variable variable,CFGNode cfgNode,SootMethod method){
		this.variable = variable;
		if(variable.isLocal()){
			blockType = AssetType.Local;
		}else if(variable.isFieldRef()){
			blockType = AssetType.Field;
		}else if(variable.getValue() instanceof IdentityRef){
			blockType = AssetType.Argument;
		}
		blocks = new LinkedList<CFGNode>();
		blocks.add(cfgNode);
		this.method = method;
		this.cls = method.getDeclaringClass();
		valuepool.put(variable, this);
	}
	public static CodeBlock tryToCreate(Variable variable,CFGNode cfgNode,SootMethod method){
		if(valuepool.containsKey(variable)){
			return valuepool.get(variable);
		}else
			return new CodeBlock(variable,cfgNode,method);
	}
	public SootClass getSootClass(){
		return this.cls;
	}
	public SootMethod getSootMethod(){
		return this.method;
	}
	public List<CFGNode> getCFGNodes(){
		return this.blocks;
	}
	public Variable getValue(){
		if(this.blockType.equals(AssetType.Argument)||
				this.blockType.equals(AssetType.Field)||
				this.blockType.equals(AssetType.Local)){
			return this.variable;
		}
		return null;
	}
	public AssetType getType(){
		return this.blockType;
	}
	public BlockMarkedTag getTag(String tagName) {
		// TODO Auto-generated method stub
		if(tagName.equals(BlockMarkedTag.TAG_NAME))
			return bmtag;
		else
			return null;
	}
	public void addTag(BlockMarkedTag smkTag) {
		// TODO Auto-generated method stub
		bmtag = smkTag;
	}
}
