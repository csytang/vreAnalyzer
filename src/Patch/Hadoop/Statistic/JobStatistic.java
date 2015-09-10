package Patch.Hadoop.Statistic;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Map;

import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;
import Patch.Hadoop.ProjectParser;
import Patch.Hadoop.Job.JobHub;
import Patch.Hadoop.Job.JobVariable;

public class JobStatistic {
	JobVariable job;
	Color jobColor;
	int reuseCode = 0;
	int loc = 0;
	Map<SootClass,LinkedList<CFGNode>>jobUsesSequence;
	public JobStatistic(JobVariable jobvar){
		this.job = jobvar;
		this.jobColor = jobvar.getAnnotatedColor();
		JobHub jobhub = ProjectParser.instance.getjobHub(jobvar);
		jobUsesSequence = jobhub.getjobUse();
		this.loc = countStmts();
	}
	private int countStmts(){
		int result = 0;
		for(Map.Entry<SootClass, LinkedList<CFGNode>>entry:jobUsesSequence.entrySet()){
			result+=entry.getValue().size();
		}
		return result;
	}
	public void addreuseCode(){
		this.reuseCode++;
	}
	
}
