package Patch.Hadoop;

import java.util.LinkedList;
import java.util.List;

import soot.SootClass;


public class MapPipelines {
	
	// Singleton
	private static MapPipelines instance;
	List<SingleMapPipeline>mapPips;
	public MapPipelines(){
		mapPips = new LinkedList<SingleMapPipeline>();
	}
	
	public static MapPipelines inst(){
		if(instance==null)
			instance = new MapPipelines();
		return instance;
	}
	
	public void createNewSingleMap(SootClass appClass) {
		SingleMapPipeline mp = new SingleMapPipeline(appClass);
		mapPips.add(mp);
	}
	
	public void findCommonAssetsandReset(){
		
	}
	
}
