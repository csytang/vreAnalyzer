package Patch.Hadoop.Pipelines;

import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleReducerPipeline {

	SootMethod reduce;
	SootClass reduceClass;
	Object job;
	
	public SingleReducerPipeline(Object key,SootClass appClass) {
		// TODO Auto-generated constructor stub
		job = key;
		reduceClass = appClass;
		assert(reduceClass.getSuperclass().getName().equals("org.apache.hadoop.mapreduce.Reducer"));
		for(SootMethod sm:appClass.getMethods()){
			
			// Temporary setting
			if(sm.isConcrete() && 
					sm.getName().equals("reduce") &&
					sm.getModifiers()==Modifier.PUBLIC 
					){
				reduce = sm;
				break;
			}
		}
	}

	

	public SootClass getSootClass() {
		// TODO Auto-generated method stub
		return reduceClass;
	}

	public SootMethod getSootMethod() {
		// TODO Auto-generated method stub
		return reduce;
	}
	public Object getJob(){
		return job;
	}
	

}
