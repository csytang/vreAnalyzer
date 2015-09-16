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
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.UI.RandomColor;

public class ReuseAsset {
	
	private int blockId = 0;
	private AssetType commonType;
	private CodeBlock block;
	private SootMethod commonMethod =  null;
	private SootClass commonClass = null;
	private Color color;
	private List<JobVariable>jobs;
	private List<CFGNode>cfgNodes;
	private static Map<CodeBlock,ReuseAsset>blockToAsset = new HashMap<CodeBlock,ReuseAsset>();
	private static Map<Set<JobVariable>,Color>joblistToColor = new HashMap<Set<JobVariable>,Color>();
	private int LOC = 0;
	
	private ReuseAsset(CodeBlock block,int lineofCode,Collection<JobVariable> joblist){
		this.blockId = block.getBlockId();
		this.block = block;
		this.LOC = lineofCode;
		this.commonType = block.getType();
		Set<JobVariable>alljobs = new HashSet<JobVariable>(joblist);
		if(joblistToColor.containsKey(alljobs)){
			this.color = joblistToColor.get(alljobs);
		}else{
			RandomColor rcolor = new RandomColor();
			this.color = rcolor.getColor();
			joblistToColor.put(alljobs, this.color);
		}
		if(this.commonType.equals(AssetType.Method)){
			this.commonMethod = block.getSootMethod();
		}else if(this.commonType.equals(AssetType.Class)){
			this.commonClass = block.getSootClass();
		}else if(this.commonType.equals(AssetType.Stmt)){
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
	public static Color getCommonColor(Set<JobVariable>jobs){
		if(joblistToColor.containsKey(jobs)){
			return joblistToColor.get(jobs);
		}else{
			RandomColor rcolor = new RandomColor();
			joblistToColor.put(jobs, rcolor.getColor());
			return rcolor.getColor();
		}
	}
	public static ReuseAsset customizecontainsKey(Map<CodeBlock,ReuseAsset>blockToAsset,CodeBlock block){
		for(Map.Entry<CodeBlock, ReuseAsset>entry:blockToAsset.entrySet()){
			if(entry.getKey().equals(block))
				return entry.getValue();
		}
		return null;
	}
	public AssetType getType(){
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
