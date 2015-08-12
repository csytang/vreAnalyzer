package Patch.Hadoop.Pipelines;


import java.util.HashMap;
import java.util.Map;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;


public class ConfigurePipelines {
	
	// Singleton
	private static ConfigurePipelines instance;
	Map<Value,SingleConfigurePipeline>configPips;
	private boolean verbose = true;
	
	
	
	public ConfigurePipelines(){
		configPips = new HashMap<Value,SingleConfigurePipeline>();
	}
	
	public static ConfigurePipelines inst(){
		if(instance==null)
			instance = new ConfigurePipelines();
		return instance;
	}
	
	public void createNewConfigurePipeline(Value job,SootClass appClass,SootMethod appMethod) {
		SingleConfigurePipeline mp = new SingleConfigurePipeline(job,appClass,appMethod);
		configPips.put(job,mp);
	}

	public SingleConfigurePipeline getConfigurePipeline(Value value) {
		// TODO Auto-generated method stub
		return configPips.get(value);
	}
	
	
	
}
