package Patch.Hadoop.Pipelines;



import java.util.HashMap;
import java.util.Map;
import soot.SootClass;
import soot.Value;


public class MapperPipelines {
	
	// Singleton
	private static MapperPipelines instance;
	Map<Value,SingleMapperPipeline>mapPips;
	
	
	
	public MapperPipelines(){
		mapPips = new HashMap<Value,SingleMapperPipeline>();
	}
	
	public static MapperPipelines inst(){
		if(instance==null)
			instance = new MapperPipelines();
		return instance;
	}
	
	public void createNewSingleMap(Value job,SootClass appClass) {
		SingleMapperPipeline mp = new SingleMapperPipeline(job,appClass);
		mapPips.put(job,mp);
	}
	public SingleMapperPipeline getMapperPipeline(Value job){
		return mapPips.get(job);
	}
	
	
}
