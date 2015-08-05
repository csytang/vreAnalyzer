package Patch.Hadoop.Pipelines;

import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleReducerPipeline {

	SootMethod reducer;
	SootClass reducerClass;
	public SingleReducerPipeline(SootClass appClass) {
		// TODO Auto-generated constructor stub
		reducerClass = appClass;
		assert(reducerClass.getSuperclass().getName().equals("org.apache.hadoop.mapreduce.Reducer"));
		for(SootMethod sm:appClass.getMethods()){
			
			// Temporary setting
			if(sm.isConcrete() && 
					sm.getName().equals("reducer") &&
					sm.getModifiers()==Modifier.PUBLIC){
				reducer = sm;
				break;
			}
		}
	}

	public CFGNode[] getCommonAsset(SingleReducerPipeline other) {
		// TODO Auto-generated method stub
		SootMethod otherMethod = other.getSootMethod();
		return Util.getCommonAsset(reducer, otherMethod); 
	}

	public SootClass getSootClass() {
		// TODO Auto-generated method stub
		return reducerClass;
	}

	public SootMethod getSootMethod() {
		// TODO Auto-generated method stub
		return reducer;
	}

	

}
