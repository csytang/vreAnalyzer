package Patch.Hadoop.Pipelines;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleConfigurePipeline {
	
	SootMethod setup;
	SootClass mapperClass;
	Object job;
	
	public SingleConfigurePipeline(Object job, SootClass appClass, SootMethod appMethod)  {
		// TODO Auto-generated constructor stub
		this.job = job;
		this.mapperClass = appClass;
		this.setup = appMethod;
		
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
