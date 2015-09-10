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
	}
	public void setcountStmts(int totallines){
		this.loc = totallines;
	}
	public void addreuseCode(){
		this.reuseCode++;
	}
	public int getLOC(){
		return this.loc;
	}
	public int getReuseCode(){
		return this.reuseCode;
	}
}
