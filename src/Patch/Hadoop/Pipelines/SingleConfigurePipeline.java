package Patch.Hadoop.Pipelines;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleConfigurePipeline {
	
	SootMethod setup;
	SootClass mapperClass;
	Value job;
	
	
	
	public SingleConfigurePipeline(Value job, SootClass appClass, SootMethod appMethod)  {
		// TODO Auto-generated constructor stub
		this.job = job;
		this.mapperClass = appClass;
		this.setup = appMethod;
		
	}
	
	
	
	public CFGNode[] getCommonAsset(SingleConfigurePipeline otherConfigure) {
		// TODO Auto-generated method stub
		SootMethod otherMethod = otherConfigure.getSootMethod();
		return Util.getCommonAsset(setup, otherMethod); 
	}
	public SootClass getSootClass() {
		// TODO Auto-generated method stub
		return mapperClass;
	}
	public SootMethod getSootMethod() {
		// TODO Auto-generated method stub
		return setup;
	}
}
