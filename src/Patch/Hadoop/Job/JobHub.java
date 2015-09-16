package Patch.Hadoop.Job;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import soot.SootClass;
import vreAnalyzer.Blocks.CodeBlock;


public class JobHub {
	
	
	private JobVariable job;
	private Map<SootClass,LinkedList<CodeBlock>>jobUsesSequence;
	private LinkedList<CodeBlock>sharedBlocks;
	public JobHub(JobVariable job){
		this.job = job;
		jobUsesSequence = new HashMap<SootClass,LinkedList<CodeBlock>>();
		sharedBlocks = new LinkedList<CodeBlock>();
	}
	public void addUse(SootClass sc,CodeBlock blocknode){
		if(!jobUsesSequence.containsKey(sc)){
			jobUsesSequence.put(sc, new LinkedList<CodeBlock>());
		}
		jobUsesSequence.get(sc).add(blocknode);
	}
	
	/**
	 * add a shared cfgNode to this job
	 * @param cfgNode
	 */
	public void addSharedUse(CodeBlock blockNode){
		this.sharedBlocks.add(blockNode);
	}
	
	public Map<SootClass,LinkedList<CodeBlock>> getjobUse(){
		return jobUsesSequence;
	}
	public String getJobName(){
		return job.getVariable().getValue().toString();
	}
	public JobVariable getJob(){
		return job;
	}
	
}
