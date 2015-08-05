package vreAnalyzer.Reuse.Normal.Pipeline;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleNormalPipeline {
	
	private SootClass app;
	private SootMethod constructor;

	
	public SingleNormalPipeline(SootClass appClass) {
		// TODO Auto-generated constructor stub
		app = appClass;
		this.setMethod();
	}
	private void setMethod() {
		constructor = app.getMethodByName("<init>");
	}
	public CFGNode[] getCommonAsset(SingleNormalPipeline other) {
		SootMethod otherMethod = other.getSootMethod();
		return Util.getCommonAsset(constructor, otherMethod); 

	}
	public SootMethod getSootMethod() {
		// TODO Auto-generated method stub
		return constructor;
	}
	public SootClass getSootClass(){
		return app;
	}
	

}
