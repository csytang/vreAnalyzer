package Patch.Hadoop.Job;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;


public class JobHub {
	
	
	private JobVariable job;
	private Map<SootClass,LinkedList<CFGNode>>jobUsesSequence;
	private LinkedList<CFGNode>sharedCFGNode;
	public JobHub(JobVariable job){
		this.job = job;
		jobUsesSequence = new HashMap<SootClass,LinkedList<CFGNode>>();
		sharedCFGNode = new LinkedList<CFGNode>();
	}
	public void addUse(SootClass sc,CFGNode cfgNode){
		if(!jobUsesSequence.containsKey(sc)){
			jobUsesSequence.put(sc, new LinkedList<CFGNode>());
		}
		jobUsesSequence.get(sc).add(cfgNode);
	}
	public void addUse(SootClass sc,Collection<CFGNode> cfgNodes){
		if(!jobUsesSequence.containsKey(sc)){
			jobUsesSequence.put(sc, new LinkedList<CFGNode>());
		}
		jobUsesSequence.get(sc).addAll(cfgNodes);
	}
	
	/**
	 * add a shared cfgNode to this job
	 * @param cfgNode
	 */
	public void addSharedUse(CFGNode cfgNode){
		this.sharedCFGNode.add(cfgNode);
	}
	
	public Map<SootClass,LinkedList<CFGNode>> getjobUse(){
		return jobUsesSequence;
	}
	public String getJobName(){
		return job.getVariable().getValue().toString();
	}
	public JobVariable getJob(){
		return job;
	}
	
}
