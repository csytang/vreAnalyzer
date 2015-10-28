package Patch.Hadoop.Statistic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.jimple.Stmt;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Tag.SourceLocationTag.LocationType;
import Patch.Hadoop.Job.JobHub;
import Patch.Hadoop.Job.JobVariable;
import Patch.Hadoop.ReuseAssets.ReuseAsset;
import Patch.Hadoop.Tag.BlockJobTag;


public class JobDataCollector {
	
	public static JobDataCollector instance;
	protected Map<JobVariable,JobStatistic>jobToStatistic;
	private boolean startFromSource = false;
	
	public JobDataCollector(){
		jobToStatistic = new HashMap<JobVariable,JobStatistic>();
	}
	public static JobDataCollector inst(){
		if(instance==null)
			instance = new JobDataCollector();
		return instance;
	}
	public Map<JobVariable,JobStatistic>getJobToStatistic(){
		return jobToStatistic;
	}
	public void parse(Map<JobVariable,JobHub>jobtoHub){
		for(Map.Entry<JobVariable, JobHub>entry:jobtoHub.entrySet()){
			JobVariable job = entry.getKey();
			JobHub hub = entry.getValue();
			
			Map<SootClass,LinkedList<CodeBlock>>jobUsesSequence = hub.getjobUse();
			JobStatistic jobstat = null;
			if(!jobToStatistic.containsKey(job)){
				jobstat = new JobStatistic(job);
				jobToStatistic.put(job, jobstat);
			}
			else
				jobstat = jobToStatistic.get(job);
			
			
            ///////////////////////////////////////////////////////////////////////////
			for(Map.Entry<SootClass, LinkedList<CodeBlock>>blockentry:jobUsesSequence.entrySet()){
				LinkedList<CodeBlock>blocks = blockentry.getValue();
				for(CodeBlock block:blocks){
					BlockJobTag bmTag = block.getTag(BlockJobTag.TAG_NAME);
					Set<JobVariable> bindingjobs = bmTag.getJobs();
					Set<Integer>lines = new HashSet<Integer>();
					int line = 0;
					
					for(CFGNode cfgNode:block.getCFGNodes()){
						if(cfgNode.isSpecial())
							continue;
						Stmt stmt = cfgNode.getStmt();
						SourceLocationTag slcTag = (SourceLocationTag) stmt.getTag(SourceLocationTag.TAG_NAME);
						if(slcTag.getTagType()==LocationType.SOURCE_TAG){// start from java source code
							startFromSource = true;
							for(int i = slcTag.getStartLineNumber();i <= slcTag.getEndLineNumber();i++){
								lines.add(i);
							}
							jobstat.addUseStmts(blockentry.getKey(), lines);
						}
						
						else{// start from bytecode
							startFromSource = false;
							line = slcTag.getStartLineNumber();	
							lines.add(line);
							jobstat.addUseStmts(blockentry.getKey(), line);
						}
					}
					/////
					
						if(bindingjobs.size()>1){
							ReuseAsset comasst = null;
							boolean firstTime = true;
							for(JobVariable jb:bindingjobs){
								jobtoHub.get(jb).addSharedUse(block);
								if(firstTime){
									comasst =  ReuseAsset.tryToCreate(block,lines.size(), bindingjobs);
									
									firstTime = false;
								}
								if(jobToStatistic.containsKey(jb)){
									jobToStatistic.get(jb).addReuseAsset(blockentry.getKey(),comasst,lines);
								}else{
									JobStatistic dependstatis = new JobStatistic(jb);
									dependstatis.addReuseAsset(blockentry.getKey(),comasst,lines);
									jobToStatistic.put(jb, dependstatis);
								}
							}
						}
					
					/////
				}
			}
			///////////////////////////////////////////////////////////////////////////
			
		}
		
	}
	public Map<CodeBlock,ReuseAsset> getCommonAssets(){
		return ReuseAsset.getBlockToAsset();
	}
}
