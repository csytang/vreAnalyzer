package Patch.Hadoop;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;

public class JobVariable{
	Variable jobvariable;
	SootClass sc;
	SootMethod sm;
	CFGNode jobCFGNode;
	public JobVariable(Variable val,CFGNode cfgNode) {
		jobvariable = val;
		jobCFGNode = cfgNode;
		sm = jobCFGNode.getMethod();
		sc = sm.getDeclaringClass();
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
	

	
}
