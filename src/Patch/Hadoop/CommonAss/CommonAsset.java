package Patch.Hadoop.CommonAss;

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
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CodeBlock;
import vreAnalyzer.UI.RandomColor;

public class CommonAsset {
	public static int globalassetId = 0;
	private int assetId = 0;
	private AssetType commonType;
	private CodeBlock block;
	private SootMethod commonMethod =  null;
	private SootClass commonClass = null;
	private Color color;
	private List<JobVariable>jobs;
	private static Map<CodeBlock,CommonAsset>blockToAsset = new HashMap<CodeBlock,CommonAsset>();
	private static Map<Set<JobVariable>,Color>joblistToColor = new HashMap<Set<JobVariable>,Color>();
	private int LOC = 0;
	
	private CommonAsset(CodeBlock block,int lineofCode,Collection<JobVariable> joblist){
		this.assetId = globalassetId;
		globalassetId++;
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
		}
		this.jobs = new LinkedList<JobVariable>();
		this.jobs.addAll(joblist);
		blockToAsset.put(block, this);
	}
	public static Map<CodeBlock, CommonAsset> getBlockToAsset(){
		return blockToAsset;
	}
	public static CommonAsset tryToCreate(CodeBlock block,int lineofCode,Collection<JobVariable> joblist){
		CommonAsset result = customizecontainsKey(blockToAsset,block);
		if(result!=null)
			return result;
		else{
			return new CommonAsset(block,lineofCode,joblist);
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
	public static CommonAsset customizecontainsKey(Map<CodeBlock,CommonAsset>blockToAsset,CodeBlock block){
		for(Map.Entry<CodeBlock, CommonAsset>entry:blockToAsset.entrySet()){
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
	public List<Variable> getVariable(){
		return block.getValue();
	}
	public int getAssetId(){
		return this.assetId;
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
