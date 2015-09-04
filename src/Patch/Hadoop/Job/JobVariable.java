package Patch.Hadoop.Job;

import java.awt.Color;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.UI.RandomColor;

public class JobVariable{
	Variable jobvariable;
	SootClass sc;
	SootMethod sm;
	CFGNode jobCFGNode;
	private Color jobColor;
	public JobVariable(Variable val,CFGNode cfgNode) {
		jobvariable = val;
		jobCFGNode = cfgNode;
		System.out.println("The job statement:\t"+cfgNode.getStmt().toString());
		sm = jobCFGNode.getMethod();
		sc = sm.getDeclaringClass();
		if(vreAnalyzerCommandLine.isSourceBinding()&&vreAnalyzerCommandLine.isStartFromGUI()){
			RandomColor rcolor = new RandomColor();
			jobColor = rcolor.getColor();
		}
	}
	public SootClass getSootClass(){
		return sc;
	}
	public SootMethod getSootMethod(){
		return sm;
	}
	public Variable getVariable(){
		return jobvariable;
	}
	public CFGNode getCFGNode(){
		return jobCFGNode;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return jobvariable.toString();
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		
		if(!(obj instanceof JobVariable))
			return false;
		JobVariable objjobvariable = (JobVariable)obj;
		if(sc.equals(objjobvariable.getSootClass())&&sm.equals(objjobvariable.getSootMethod())){
			if(jobvariable.getValue().equals(objjobvariable.getVariable().getValue()))
				return true;
			else
				return false;
		}else
			return false;
	}
	public StmtTag getSootStmtTag() {
		// TODO Auto-generated method stub
		return this.jobCFGNode.getStmtTag();
	}
	public Color getAnnotatedColor() {
		// TODO Auto-generated method stub
		return jobColor;
	}
	

	
}
