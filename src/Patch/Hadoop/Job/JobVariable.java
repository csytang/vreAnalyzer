package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CodeBlock;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.UI.RandomColor;
import vreAnalyzer.UI.SourceClassBinding;

public class JobVariable{
	
	Variable jobvariable;
	SootClass sc;
	SootMethod sm;
	CFGNode cfgNode;
	CodeBlock jobNodeBlock;
	File sourceFile;
	int jobId;
	private Color jobColor;
	
	public JobVariable(Variable val,CodeBlock block) {
		
		jobvariable = val;
		jobNodeBlock = block;
		cfgNode = block.getCFGNodes().get(0);
		sm = block.getCFGNodes().get(0).getMethod();
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
			vreAnalyzerCommandLine.featurewriter.println(jobvariable.toString()+"," +
			"\"" +cfgNode.toString().replaceAll("\"", "'")+"\"" +","+
			"\"" +sm.getName().replaceAll("\"", "'")+"\"" +","+
			"\"" +sc.getName().replaceAll("\"", "'")+"\"" +","+
			"\"" +sourceFile.getAbsolutePath()+"\"");
		}
		else{
			vreAnalyzerCommandLine.featurewriter.println(jobvariable.toString()+"," +
					"\"" +cfgNode.toString()+"\"" +","+
					"\"" +sm.getName().replaceAll("\"", "'")+"\"" +","+
					"\"" +sc.getName().replaceAll("\"", "'")+"\"");
		}
		
	}
	public SootClass getSootClass(){
		return sc;
	}
	public File getSourceFile(){
		return sourceFile;
	}
	public SootMethod getSootMethod(){
		return sm;
	}
	public void setJobId(int id){
		this.jobId= id;
	}
	public Variable getVariable(){
		return jobvariable;
	}
	public CodeBlock getBlock(){
		return jobNodeBlock;
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
		return this.cfgNode.getStmtTag();
	}
	public Color getAnnotatedColor() {
		// TODO Auto-generated method stub
		return jobColor;
	}
	public int getJobId() {
		// TODO Auto-generated method stub
		return jobId;
	}
	

	
}
