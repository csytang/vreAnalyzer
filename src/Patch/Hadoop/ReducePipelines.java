package Patch.Hadoop;

import java.util.LinkedList;
import java.util.List;

import soot.SootClass;

public class ReducePipelines {
		// Singleton
		private static ReducePipelines instance;
		List<SingleReducePipeline>reducePips;
		public ReducePipelines(){
			reducePips = new LinkedList<SingleReducePipeline>();
		}
		
		public static ReducePipelines inst(){
			if(instance==null)
				instance = new ReducePipelines();
			return instance;
		}
		
		public void createNewSingleMap(SootClass appClass) {
			SingleReducePipeline re = new SingleReducePipeline(appClass);
			reducePips.add(re);
		}
		
		public void findCommonAssetsandReset(){
			
		}
}
