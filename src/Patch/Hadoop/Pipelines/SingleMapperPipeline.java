package Patch.Hadoop.Pipelines;

import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class SingleMapperPipeline {
	SootMethod mapper;
	SootMethod setup;
	SootClass mapperClass;
	Object job;
	
	public SingleMapperPipeline(Object key,SootClass appClass) {
		// TODO Auto-generated constructor stub
		job = key;
		mapperClass = appClass;
		assert(mapperClass.getSuperclass().getName().equals("org.apache.hadoop.mapreduce.Mapper"));
		for(SootMethod sm:appClass.getMethods()){
			
			//  Temporary setting
			if(sm.isConcrete() && 
					sm.getName().equals("map") &&
					sm.getModifiers()==Modifier.PUBLIC){
				mapper = sm;				
			}
			
			else if(sm.isConcrete() && 
					sm.getName().equals("setup") &&
					sm.getModifiers()==Modifier.PUBLIC){
				ConfigurePipelines.inst().createNewConfigurePipeline(job, appClass,sm);
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
	
	public Object getJob(){
		return job;
	}
}
