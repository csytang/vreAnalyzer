package vreAnalyzer.Reuse.Normal.Pipeline;

import java.util.LinkedList;
import java.util.List;
import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Reuse.Scheduler.NormalScheduler;




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
	public void findCommonAssetsandReset(){
		if(pipes.size()<=1){
			System.err.println("WARNING!! Less than 2 classes detected, not adapted to detect reuse");
			return;
		}
		SingleNormalPipeline src = null,other= null;
		CFGNode[] commons = null;
		for(int i = 0;i < pipes.size();i++){
			for(int j = i+1; j < pipes.size();j++){
				src = pipes.get(i);
				other = pipes.get(j);
				commons= src.getCommonAsset(other);
				if(commons==null){
					System.out.println("No common assets detected in\t"+src.getSootClass().getName()+"\t and "+other.getSootClass().getName());
				}
				else if(verbose){
					System.out.println("StartIndex and EndIndex in \t"+src.getSootClass().getName()+"\t is");
					System.out.println("Start:\t"+commons[0].toString()+"\t|\tEnd:\t"+commons[1].toString());
					System.out.println("StartIndex and EndIndex in \t"+other.getSootClass().getName()+"\t is");
					System.out.println("Start:\t"+commons[2].toString()+"\t|\tEnd:\t"+commons[3].toString());
				}
			
			NormalScheduler normalScheduler = new NormalScheduler(src.getSootMethod(),other.getSootMethod(),commons);
		
			normalScheduler.SootMethodIntegrate();
			normalScheduler.MethodCleanUp();
			normalScheduler.SootClassInUpdate();
			}
		}
	}
	

}
