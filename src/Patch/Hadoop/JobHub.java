package Patch.Hadoop;


import java.util.LinkedList;
import java.util.List;

import soot.SootClass;
import soot.Value;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;


public class JobHub {
	
	//private SootClass configClass;
	private SootClass mapperClass;
	private SootClass reducerClass;
	private SootClass combinerClass;
	private Value inputPath;
	private Value outputPath;
	private JobVariable job;
	private List<Variable> jobpointers;
	
	public JobHub(JobVariable job){
		jobpointers = new LinkedList<Variable>();
		this.job = job;
	}
	public void setMapperClass(SootClass mp){
		this.mapperClass = mp;
	}
	public void setReducerClass(SootClass re){
		this.reducerClass = re;
	}
	public void setInputPath(Value in){
		inputPath = in;
	}
	public Value getInputPath(){
		return inputPath;
	}
	public void setOutPath(Value out){
		outputPath = out;
	}
	public Value getOutputPath(){
		return outputPath;
	}
	
	public SootClass getMapperClass(){
		return this.mapperClass;
	}
	public SootClass getReducerClass(){
		return this.reducerClass;
	}

	public String getJobName(){
		return job.toString();
	}
	public void setCombinerClass(SootClass combiner) {
		// TODO Auto-generated method stub
		combinerClass = combiner;
	}
	public SootClass getCombinerClass(){
		return combinerClass;
	}
	public SootClass getConfigureClass() {
		// TODO Auto-generated method stub
		return mapperClass;
	}
}
