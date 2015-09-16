package vreAnalyzer.Blocks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Patch.Hadoop.ReuseAssets.AssetType;
import Patch.Hadoop.Tag.BlockJobTag;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;

public class CodeBlock {
	
	/**
	 * a code block represents a code segment, it could be a single statement,
	 * variable, method, class and even a package.
	 */
	private AssetType blockType = null;
	private List<CFGNode>blocks;
	private List<Variable> variable;// if applicable
	private SootMethod method;// if applicable
	private SootClass cls;
	private BlockJobTag bmtag;
	
	public static Map<CFGNode,CodeBlock>valuepool = new HashMap<CFGNode,CodeBlock>();
	
	
	public SootClass getSootClass(){
		return this.cls;
	}
	public void setSootClass(SootClass sootcls){
		this.cls = sootcls;
	}
	public SootMethod getSootMethod(){
		return this.method;
	}
	public List<CFGNode> getCFGNodes(){
		return this.blocks;
	}
	public void setBlocks(List<CFGNode>blocks){
		this.blocks = blocks;
	}
	public List<Variable> getValues(){
		if(this.blockType.equals(AssetType.Argument)||
				this.blockType.equals(AssetType.Field)||
				this.blockType.equals(AssetType.Local)){
			return this.variable;
		}
		return null;
	}
	public void addValue(Variable vi){
		if(this.variable==null)
			this.variable = new LinkedList<Variable>();
		this.variable.add(vi);
	}
	public AssetType getType(){
		return this.blockType;
	}
	public void setType(AssetType type){
		this.blockType = type;
	}
	public void setMethod(SootMethod sm){
		this.method = sm;
	}
	public BlockJobTag getTag(String tagName) {
		// TODO Auto-generated method stub
		if(tagName.equals(BlockJobTag.TAG_NAME))
			return bmtag;
		else
			return null;
	}
	public void addTag(BlockJobTag smkTag) {
		// TODO Auto-generated method stub
		bmtag = smkTag;
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj==null)
			return false;
		if(!(obj instanceof CodeBlock)){
			return false;
		}
		CodeBlock objBlock =(CodeBlock)obj;
		if(objBlock.blockType!=this.blockType)
			return false;
		if(this.blockType==AssetType.Class){
			return this.cls==objBlock.getSootClass();
		}else if(this.blockType==AssetType.Method){
			return this.method==objBlock.getSootMethod();
		}else{
			return this.variable==objBlock.getValues();
		}
	}
}
