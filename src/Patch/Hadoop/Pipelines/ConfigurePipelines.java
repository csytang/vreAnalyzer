package Patch.Hadoop.Pipelines;
import java.util.HashMap;
import java.util.Map;
import soot.SootClass;
import soot.SootMethod;


public class ConfigurePipelines {
	
	// Singleton
	private static ConfigurePipelines instance;
	Map<Object,SingleConfigurePipeline>configPips;	
	
	public ConfigurePipelines(){
		configPips = new HashMap<Object,SingleConfigurePipeline>();
	}
	
	public static ConfigurePipelines inst(){
		if(instance==null)
			instance = new ConfigurePipelines();
		return instance;
	}
	
	public void createNewConfigurePipeline(Object job,SootClass appClass,SootMethod appMethod) {
		SingleConfigurePipeline mp = new SingleConfigurePipeline(job,appClass,appMethod);
		configPips.put(job,mp);
	}

	public SingleConfigurePipeline getConfigurePipeline(Object value) {
		// TODO Auto-generated method stub
		return configPips.get(value);
	}
	
	
	
}
