package Patch.Hadoop.CommonAss;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import Patch.Hadoop.Job.JobVariable;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Elements.CodeBlock;

public class CommonAsset {
	public static int globalassetId = 0;
	private int assetId = 0;
	private AssetType commonType;
	private CodeBlock block;
	private SootMethod commonMethod =  null;
	private SootClass commonClass = null;
	private Color color;
	private List<JobVariable>jobs;
	public CommonAsset(CodeBlock block,Color color,JobVariable job){
		this.assetId = globalassetId;
		globalassetId++;
		this.block = block;
		this.commonType = block.getType();
		this.color = color;
		if(this.commonType.equals(AssetType.Method)){
			this.commonMethod = block.getSootMethod();
		}else if(this.commonType.equals(AssetType.Class)){
			this.commonClass = block.getSootClass();
		}
		this.jobs = new LinkedList<JobVariable>();
		this.jobs.add(job);
	}
	public AssetType getType(){
		return commonType;
	}
	public void addBindingJob(JobVariable job){
		this.jobs.add(job);
	}
}
