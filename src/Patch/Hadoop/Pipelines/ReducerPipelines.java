package Patch.Hadoop.Pipelines;

import java.util.LinkedList;
import java.util.List;

import Patch.Hadoop.Scheduler.ReducerScheduler;
import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;

public class ReducerPipelines {
		// Singleton
		private static ReducerPipelines instance;
		List<SingleReducerPipeline>reducePips;
		private boolean verbose = true;
		
		public ReducerPipelines(){
			reducePips = new LinkedList<SingleReducerPipeline>();
		}
		
		public static ReducerPipelines inst(){
			if(instance==null)
				instance = new ReducerPipelines();
			return instance;
		}
		
		public void createNewSingleReducer(SootClass appClass) {
			SingleReducerPipeline re = new SingleReducerPipeline(appClass);
			reducePips.add(re);
		}
		
		public void findCommonAssetsandReset(){
			if(reducePips.size()<=1){
				System.err.println("WARNING!! Less than 2 reducer classes detected, not adapted to detect reuse");
				return;
			}
			SingleReducerPipeline src = null,other = null;
			CFGNode[] commons = null;
			for(int i = 0;i < reducePips.size();i++){
				for(int j = i+1; j < reducePips.size();j++){
					src = reducePips.get(i);
					other = reducePips.get(j);
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
				
				ReducerScheduler reduerScheduler = new ReducerScheduler(src.getSootMethod(),other.getSootMethod(),commons);
			
				reduerScheduler.SootMethodIntegrate();
				reduerScheduler.MethodCleanUp();
				reduerScheduler.SootClassInUpdate();
				}
			}
		}
}
