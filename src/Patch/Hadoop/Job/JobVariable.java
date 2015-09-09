package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.UI.RandomColor;
import vreAnalyzer.UI.SourceClassBinding;

public class JobVariable{
	Variable jobvariable;
	SootClass sc;
	SootMethod sm;
	CFGNode jobCFGNode;
	File sourceFile;
	private Color jobColor;
	public JobVariable(Variable val,CFGNode cfgNode) {
		jobvariable = val;
		jobCFGNode = cfgNode;
		sm = jobCFGNode.getMethod();
		sc = sm.getDeclaringClass();
		if(vreAnalyzerCommandLine.isSourceBinding()){
			sourceFile = SourceClassBinding.getSourceFileFromClassName(sc.toString());
		}
		if(vreAnalyzerCommandLine.isSourceBinding()&&vreAnalyzerCommandLine.isStartFromGUI()){
			RandomColor rcolor = new RandomColor();
			jobColor = rcolor.getColor();
		}
		// write this job to file
		if(sourceFile!=null){
			vreAnalyzerCommandLine.jobwriter.println(jobvariable.toString()+"," +
			"\"" +cfgNode.toString()+"\"" +","+
			"\"" +sm.getName()+"\"" +","+
			"\"" +sc.getName()+"\"" +","+
			"\"" +sourceFile.getAbsolutePath()+"\"");
		}
		else{
			vreAnalyzerCommandLine.jobwriter.println(jobvariable.toString()+"," +
					"\"" +cfgNode.toString()+"\"" +","+
					"\"" +sm.getName()+"\"" +","+
					"\"" +sc.getName()+"\"");
		}
		
	}
	public SootClass getSootClass(){
		return sc;
	}
	public File getSootFile(){
		return sourceFile;
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
