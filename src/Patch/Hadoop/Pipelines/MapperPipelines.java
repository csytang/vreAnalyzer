package Patch.Hadoop.Pipelines;

import java.util.LinkedList;
import java.util.List;
import Patch.Hadoop.Scheduler.MapperScheduler;
import soot.SootClass;
import vreAnalyzer.Elements.CFGNode;


public class MapperPipelines {
	
	// Singleton
	private static MapperPipelines instance;
	List<SingleMapperPipeline>mapPips;
	private boolean verbose = true;
	
	
	
	public MapperPipelines(){
		mapPips = new LinkedList<SingleMapperPipeline>();
	}
	
	public static MapperPipelines inst(){
		if(instance==null)
			instance = new MapperPipelines();
		return instance;
	}
	
	public void createNewSingleMap(SootClass appClass) {
		SingleMapperPipeline mp = new SingleMapperPipeline(appClass);
		mapPips.add(mp);
	}
	
	public void findCommonAssetsandReset(){
		if(mapPips.size()<=1){
			System.err.println("WARNING!! Less than 2 mapper classes detected, not adapted to detect reuse");
			return;
		}
		SingleMapperPipeline src = null,other = null;
		CFGNode[] commons = null;
		for(int i = 0;i < mapPips.size();i++){
			for(int j = i+1; j < mapPips.size();j++){
				src = mapPips.get(i);
				other = mapPips.get(j);
				commons= src.getCommonAsset(other);
				if(commons==null){
					System.out.println("No common assets detected in\t"+src.getSootClass().getName()+"\t and "+other.getSootClass().getName());
				}
				else if(verbose){
					System.out.println("StartIndex and EndIndex in \t"+src.getSootClass().getName()+"\t is");
					System.out.println("Start:\t"+commons[0].toString()+"\t|\tEnd:\t"+commons[1].toString());
					System.out.println("StartIndex and EndIndex in \t"+other.getSootClass().getName()+"\t is");
					System.out.println("Start:\t"+commons[2].toString()+"\t|\tEnd:\t"+commons[3].toString());
				}
			
			MapperScheduler mapperScheduler = new MapperScheduler(src.getSootMethod(),other.getSootMethod(),commons);
		
			mapperScheduler.SootMethodIntegrate();
			mapperScheduler.MethodCleanUp();
			mapperScheduler.SootClassInUpdate();
			}
		}
	}
	
}
