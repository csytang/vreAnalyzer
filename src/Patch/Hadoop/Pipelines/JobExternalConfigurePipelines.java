package Patch.Hadoop.Pipelines;

import java.util.HashMap;
import java.util.Map;
import soot.SootMethod;
import soot.Value;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;

public class JobExternalConfigurePipelines {

	private static JobExternalConfigurePipelines instance;
	Map<Object,SingleJobExternalConfigurePipeline>pipes;
	public JobExternalConfigurePipelines(){
		pipes = new HashMap<Object,SingleJobExternalConfigurePipeline>();
	}
	public static JobExternalConfigurePipelines inst(){
		if(instance==null)
			instance = new JobExternalConfigurePipelines();
		return instance;
	}
	public void createNewJobExternalConfigurePipeline(Object job,SootMethod appMethod,NodeDefUses nodedefuse) {
		SingleJobExternalConfigurePipeline ecp = new SingleJobExternalConfigurePipeline(job,appMethod,nodedefuse);
		pipes.put(job,ecp);
	}

	public SingleJobExternalConfigurePipeline getEnConfigurePipeline(Object value) {
		// TODO Auto-generated method stub
		return pipes.get(value);
	}
}
