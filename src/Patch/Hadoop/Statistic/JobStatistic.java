package Patch.Hadoop.Statistic;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
	Map<SootClass,Set<Integer>>methodCodeLines;
	Map<SootClass,Set<Integer>>reuseMethodCodeLines;
	public JobStatistic(JobVariable jobvar){
		this.job = jobvar;
		this.jobColor = jobvar.getAnnotatedColor();
		JobHub jobhub = ProjectParser.instance.getjobHub(jobvar);
		jobUsesSequence = jobhub.getjobUse();
	}
	public void addUseStmts(SootClass sootcls,int sourceline){
		if(!methodCodeLines.containsKey(sootcls)){
			methodCodeLines.put(sootcls, new HashSet<Integer>());
			methodCodeLines.get(sootcls).add(sourceline);
		}
		else
			methodCodeLines.get(sootcls).add(sourceline);
	}
	public void addUseStmts(SootClass sootcls,Set<Integer>sourcelines){
		if(!methodCodeLines.containsKey(sootcls))
			methodCodeLines.put(sootcls, sourcelines);
		else
			methodCodeLines.get(sootcls).addAll(sourcelines);
	}
	
	public void addReuseCode(SootClass sootcls,int reuseline){
		if(!reuseMethodCodeLines.containsKey(sootcls)){
			reuseMethodCodeLines.put(sootcls, new HashSet<Integer>());
			reuseMethodCodeLines.get(sootcls).add(reuseline);
		}else
			methodCodeLines.get(sootcls).add(reuseline);
	}
	public void addReuseCode(SootClass sootcls,Set<Integer> reuselines){
		if(!reuseMethodCodeLines.containsKey(sootcls)){
			reuseMethodCodeLines.put(sootcls, new HashSet<Integer>());
			reuseMethodCodeLines.get(sootcls).addAll(reuselines);
		}else
			methodCodeLines.get(sootcls).addAll(reuselines);
	}
	public int getLOC(){
		for(Map.Entry<SootClass, Set<Integer>>entry:methodCodeLines.entrySet()){
			this.loc+=entry.getValue().size();
		}
		return this.loc;
	}
	public int getReuseCode(){
		for(Map.Entry<SootClass, Set<Integer>>entry:reuseMethodCodeLines.entrySet()){
			this.reuseCode+=entry.getValue().size();
		}
		return this.reuseCode;
	}
}
