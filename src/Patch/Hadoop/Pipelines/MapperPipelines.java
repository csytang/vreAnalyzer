package Patch.Hadoop.Pipelines;



import java.util.HashMap;
import java.util.Map;
import soot.SootClass;
import soot.Value;


public class MapperPipelines {
	
	// Singleton
	private static MapperPipelines instance;
	Map<Object,SingleMapperPipeline>mapPips;
	
	
	
	public MapperPipelines(){
		mapPips = new HashMap<Object,SingleMapperPipeline>();
	}
	
	public static MapperPipelines inst(){
		if(instance==null)
			instance = new MapperPipelines();
		return instance;
	}
	
	public void createNewSingleMap(Object job,SootClass appClass) {
		SingleMapperPipeline mp = new SingleMapperPipeline(job,appClass);
		mapPips.put(job,mp);
	}
	public SingleMapperPipeline getMapperPipeline(Object job){
		return mapPips.get(job);
	}
	
	
}
