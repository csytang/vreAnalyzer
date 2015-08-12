package Patch.Hadoop.Pipelines;

import java.util.HashMap;
import java.util.Map;
import soot.SootClass;
import soot.Value;

public class ReducerPipelines {
		// Singleton
		private static ReducerPipelines instance;
		Map<Value,SingleReducerPipeline>reducePips;
		
		
		public ReducerPipelines(){
			reducePips = new HashMap<Value,SingleReducerPipeline>();
		}
		
		public static ReducerPipelines inst(){
			if(instance==null)
				instance = new ReducerPipelines();
			return instance;
		}
		
		public void createNewSingleReducer(Value key,SootClass appClass) {
			SingleReducerPipeline re = new SingleReducerPipeline(key,appClass);
			reducePips.put(key,re);
		}

		public SingleReducerPipeline getReducerPipeline(Value key) {
			// TODO Auto-generated method stub
			return this.reducePips.get(key);
		}
		
		
}
