package Patch.Hadoop.Job;
import java.util.LinkedList;

import vreAnalyzer.Elements.CFGNode;


public class JobHub {
	
	
	private JobVariable job;
	private LinkedList<CFGNode>jobUsesSequence;
	
	public JobHub(JobVariable job){
		this.job = job;
		jobUsesSequence = new LinkedList<CFGNode>();
	}
	public void addUse(CFGNode cfgNode){
		jobUsesSequence.add(cfgNode);
	}
	public LinkedList<CFGNode> getjobUse(){
		return jobUsesSequence;
	}
	public String getJobName(){
		return job.getVariable().getValue().toString();
	}
	
}
