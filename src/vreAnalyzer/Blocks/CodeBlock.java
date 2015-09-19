package vreAnalyzer.Blocks;

import java.util.List;

import Patch.Hadoop.Tag.BlockJobTag;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class CodeBlock {
	
	/**
	 * a code block represents a code segment, it could be a single statement,
	 * variable, method, class and even a package.
	 */
	private BlockType blockType = null;
	private List<CFGNode>blocks;
	private SootMethod method;// if applicable
	private String codeRange = "";
	private SootClass cls;
	private BlockJobTag bmtag;
	private int blockId = 0;
	private int parentId = 0;
	private int featureId = -1;
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
	
	public BlockType getType(){
		return this.blockType;
	}
	public void setType(BlockType type){
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
		if(this.blockType==BlockType.Class){
			return this.cls==objBlock.getSootClass();
		}else if(this.blockType==BlockType.Method){
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
	public String getCodeRange() {
		// TODO Auto-generated method stub
		if(!codeRange.trim().equals(""))
			return codeRange;
		CFG cfg = ProgramFlowBuilder.inst().getCFG(method);
		int blockrealSize = this.blocks.size();
		if(this.blocks.contains(cfg.ENTRY))
			blockrealSize--;
		if(this.blocks.contains(cfg.EXIT))
			blockrealSize--;
		int[]coderange = new int[blockrealSize];
		int rangeindex = 0;
		for(int i = 0;i < blocks.size();i++){
			if(this.blocks.get(i).isSpecial())
				continue;
			coderange[rangeindex]=this.blocks.get(i).getIdInMethod();
			rangeindex++;
		}
		// sort this array
		if(blockrealSize<=0)
			return "[]";
		quickSort(coderange,0,coderange.length-1);
		String rangestring = "[";
		String temp = "";
		int startIndex = 0;
		int endIndex = 0;
		for(int i = 0;i < coderange.length;i++){
			startIndex = i;
			endIndex = startIndex;
			if(i<coderange.length-1){
				while(coderange[i+1]-coderange[i]==1){
					endIndex++;
					i++;
					if(i==coderange.length-1)
						break;
				}
			}
			if(endIndex-startIndex>=2)
				rangestring+=coderange[startIndex]+":"+coderange[endIndex]+",";
			else if(endIndex-startIndex==1)
				rangestring+=coderange[startIndex]+","+coderange[endIndex]+",";
			else
				rangestring+=coderange[startIndex]+",";
				
		}
		if(coderange.length>0)
			rangestring = rangestring.substring(0, rangestring.length()-1);
		rangestring+="]";
		codeRange = rangestring;
		return rangestring;
	}
	public void quickSort(int arr[],int left,int right){
		int index = partition(arr,left,right);
		if(left< index-1){
			quickSort(arr,left,index-1);
		}
		if(index<right){
			quickSort(arr,index,right);
		}
	}
	public int partition(int arr[],int left,int right){
		int i = left, j = right;
		int temp;
		int pivot = arr[(left+right)/2];
		while(i <= j){
			while(arr[i]< pivot)
				i++;
			while(arr[j]> pivot)
				j--;
			if(i <= j){
				temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				i++;
				j--;
			}
		}
		return i;
	}
	public void setParentId(int parentId) {
		// TODO Auto-generated method stub
		this.parentId = parentId;
	}
	public int getParentId(){
		return this.parentId;
	}
	public void setFeatureId(int id){
		this.featureId = id;
	}
	public int getFeatureId() {
		// TODO Auto-generated method stub

		return featureId;
	}
}
