package Patch.Hadoop.Job;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;


public class JobHub {
	
	
	private JobVariable job;
	private Map<SootClass,LinkedList<CFGNode>>jobUsesSequence;
	private List<CFGNode>reuseCFGNodes;
	
	public JobHub(JobVariable job){
		this.job = job;
		jobUsesSequence = new HashMap<SootClass,LinkedList<CFGNode>>();
		reuseCFGNodes = new LinkedList<CFGNode>();
	}
	public void addUse(SootClass sc,CFGNode cfgNode){
		if(!jobUsesSequence.containsKey(sc)){
			jobUsesSequence.put(sc, new LinkedList<CFGNode>());
			
		}
		jobUsesSequence.get(sc).add(cfgNode);
	}
	public Map<SootClass,LinkedList<CFGNode>> getjobUse(){
		return jobUsesSequence;
	}
	public String getJobName(){
		return job.getVariable().getValue().toString();
	}
	
}
