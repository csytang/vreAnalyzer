package Patch.Hadoop.Job;


import java.util.LinkedList;
import java.util.List;

import soot.SootClass;
import soot.Value;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;



public class JobHub {
	
	// Mapper setting
	private SootClass mapperClass;
	private Value mapperClassVar;
	private CFGNode mapperClassVarCFGNode;
	// Reducer setting
	private SootClass reducerClass;
	private Value reducerClassVar;
	private CFGNode reducerClassCFGNode;
	
	// Combiner setting
	private SootClass combinerClass;
	private Value combinerClassVar;
	private CFGNode combinerClassVarCFGNode;
	
	private Value inputPath;
	private Value outputPath;
	private JobVariable job;
	
	
	public JobHub(JobVariable job){
		
		this.job = job;
	}
	public void setMapperClass(SootClass mp){
		this.mapperClass = mp;
	}
	public void setMapperClassVar(Value variable,CFGNode binding){
		this.mapperClassVar = variable;
		this.mapperClassVarCFGNode = binding;
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
		return job.getVariable().getValue().toString();
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
	public void setCombinerClassVar(Value combiner, NodeDefUses combinercfgNode) {
		// TODO Auto-generated method stub
		this.combinerClassVar = combiner;
		this.combinerClassVarCFGNode = combinercfgNode;
	}
	public void setReducerClassVar(Value reducer, NodeDefUses reducercfgNode) {
		// TODO Auto-generated method stub
		this.reducerClassVar = reducer;
		this.reducerClassCFGNode = reducercfgNode;
	}
}
