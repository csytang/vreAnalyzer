package vreAnalyzer.Reuse.Normal.Pipeline;

import java.util.LinkedList;
import java.util.List;
import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;




public class NormalPipelines {

	// Singleton
	private static NormalPipelines normalpipeSingleton;
	
	// all mappers' sootclasses
	List<SingleNormalPipeline>pipes;
	
	private boolean verbose = true;
	
	
	
	
	
	public NormalPipelines(){
		pipes = new LinkedList<SingleNormalPipeline>();
	}
	public static NormalPipelines inst() {
		// TODO Auto-generated method stub
		if(normalpipeSingleton == null)
			createInstance();
		return normalpipeSingleton;
	}

	private static void createInstance() {
		// TODO Auto-generated method stub
		normalpipeSingleton = new NormalPipelines();
		
	}

	public void createNewSingleNormal(SootClass appClass) {
		// TODO Auto-generated method stub
		SingleNormalPipeline singpi = new SingleNormalPipeline(appClass);
		pipes.add(singpi);
	}
	
	

}
