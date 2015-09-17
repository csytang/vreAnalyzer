package Patch.Hadoop.ReuseAssets;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Patch.Hadoop.Job.JobVariable;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Blocks.BlockType;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Elements.CFGNode;

public class ReuseAsset {
	
	private int blockId = 0;
	private BlockType commonType;
	private CodeBlock block;
	private SootMethod commonMethod =  null;
	private SootClass commonClass = null;
	private Color color;
	private List<JobVariable>jobs;
	private List<CFGNode>cfgNodes;
	private static Map<CodeBlock,ReuseAsset>blockToAsset = new HashMap<CodeBlock,ReuseAsset>();
	private int LOC = 0;
	
	private ReuseAsset(CodeBlock block,int lineofCode,Collection<JobVariable> joblist){
		this.blockId = block.getBlockId();
		this.block = block;
		this.LOC = lineofCode;
		this.commonType = block.getType();
		Set<JobVariable>alljobs = new HashSet<JobVariable>(joblist);
		if(this.commonType.equals(BlockType.Method)){
			this.commonMethod = block.getSootMethod();
		}else if(this.commonType.equals(BlockType.Class)){
			this.commonClass = block.getSootClass();
		}else if(this.commonType.equals(BlockType.Stmt)){
			this.cfgNodes = block.getCFGNodes();
		}
		this.jobs = new LinkedList<JobVariable>();
		this.jobs.addAll(joblist);
		blockToAsset.put(block, this);
	}
	public static Map<CodeBlock, ReuseAsset> getBlockToAsset(){
		return blockToAsset;
	}
	public static ReuseAsset tryToCreate(CodeBlock block,int lineofCode,Collection<JobVariable> joblist){
		ReuseAsset result = customizecontainsKey(blockToAsset,block);
		if(result!=null)
			return result;
		else{
			return new ReuseAsset(block,lineofCode,joblist);
		}
	}
	public static ReuseAsset customizecontainsKey(Map<CodeBlock,ReuseAsset>blockToAsset,CodeBlock block){
		for(Map.Entry<CodeBlock, ReuseAsset>entry:blockToAsset.entrySet()){
			if(entry.getKey().equals(block))
				return entry.getValue();
		}
		return null;
	}
	public BlockType getType(){
		return commonType;
	}
	public SootClass getSootClass(){
		return commonClass;
	}
	public SootMethod getSootMethod(){
		return commonMethod;
	}
	public List<CFGNode> getCFGNodes(){
		return cfgNodes;
	}
	public int getBlockId(){
		return this.blockId;
	}
	public List<JobVariable> getJobs(){
		return this.jobs;
	}
	public Color getColor(){
		return this.color;
	}
	public int getLOC() {
		// TODO Auto-generated method stub
		return this.LOC;
	}
}
