package vreAnalyzer.Blocks;

import java.util.List;
import Patch.Hadoop.ReuseAssets.AssetType;
import Patch.Hadoop.Tag.BlockJobTag;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;

public class CodeBlock {
	
	/**
	 * a code block represents a code segment, it could be a single statement,
	 * variable, method, class and even a package.
	 */
	private AssetType blockType = null;
	private List<CFGNode>blocks;
	private SootMethod method;// if applicable
	private SootClass cls;
	private BlockJobTag bmtag;
	private int blockId = 0;
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
			return this.blocks.equals(objBlock.getCFGNodes());
		}
	}
	public int getBlockId() {
		// TODO Auto-generated method stub
		return blockId;
	}
	public void setBlockId(int id){
		blockId = id;
	}
}
