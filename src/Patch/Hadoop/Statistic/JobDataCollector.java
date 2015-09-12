package Patch.Hadoop.Statistic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import soot.SootClass;
import soot.jimple.Stmt;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.StmtMarkedTag;
import Patch.Hadoop.Job.JobHub;
import Patch.Hadoop.Job.JobVariable;


public class JobDataCollector {
	public static JobDataCollector instance;
	protected Map<JobVariable,JobStatistic>jobToStatistic;
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
			int totalsize = 0;
			Map<SootClass,LinkedList<CFGNode>>jobUsesSequence = hub.getjobUse();
			JobStatistic jobstat = null;
			if(!jobToStatistic.containsKey(job)){
				jobstat = new JobStatistic(job);
				jobToStatistic.put(job, jobstat);
			}
			else
				jobstat = jobToStatistic.get(job);
			for(Map.Entry<SootClass, LinkedList<CFGNode>>cfgentry:jobUsesSequence.entrySet()){
				LinkedList<CFGNode>cfgnodes = cfgentry.getValue();
				totalsize+=cfgnodes.size();
				for(CFGNode cfgNode:cfgnodes){
					if(cfgNode.isSpecial())
						continue;
					Stmt stmt = cfgNode.getStmt();
					StmtMarkedTag smkTag = (StmtMarkedTag) stmt.getTag(StmtMarkedTag.TAG_NAME);
					Set<JobVariable> jobassociated= smkTag.getJobs();
					if(jobassociated.size()>1){
						for(JobVariable jb:jobassociated){
							jobtoHub.get(jb).addSharedUse(cfgNode);
							if(jobToStatistic.containsKey(jb)){
								jobToStatistic.get(jb).addreuseCode();
							}else{
								JobStatistic dependstatis = new JobStatistic(jb);
								dependstatis.addreuseCode();
								jobToStatistic.put(jb, dependstatis);
							}
							
						}
					}
				}
			}
			jobstat.setcountStmts(totalsize);
			
		}
		
	}
}
