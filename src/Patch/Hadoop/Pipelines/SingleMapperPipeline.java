package Patch.Hadoop.Pipelines;

import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleMapperPipeline {
	SootMethod mapper;
	SootClass mapperClass;
	
	public SingleMapperPipeline(SootClass appClass) {
		// TODO Auto-generated constructor stub
		mapperClass = appClass;
		assert(mapperClass.getSuperclass().getName().equals("org.apache.hadoop.mapreduce.Mapper"));
		for(SootMethod sm:appClass.getMethods()){
			
			//  Temporary setting
			if(sm.isConcrete() && 
					sm.getName().equals("map") &&
					sm.getModifiers()==Modifier.PUBLIC){
				mapper = sm;
				break;
			}
		}
	}

	public SootMethod getSootMethod() {
		// TODO Auto-generated method stub
		return mapper;
	}

	public CFGNode[] getCommonAsset(SingleMapperPipeline other) {
		// TODO Auto-generated method stub
		SootMethod otherMethod = other.getSootMethod();
		return Util.getCommonAsset(mapper, otherMethod); 
	}

	public SootClass getSootClass() {
		// TODO Auto-generated method stub
		return mapperClass;
	}

}
