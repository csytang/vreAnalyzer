package Patch.Hadoop.Statistic;

import java.util.HashMap;
import java.util.Map;

import Patch.Hadoop.Job.JobVariable;

public class JobDataCollector {
	public static JobDataCollector instance;
	private Map<JobVariable,JobStatistic>jobToStatistic;
	public JobDataCollector(){
		jobToStatistic = new HashMap<JobVariable,JobStatistic>();
	}
	public static JobDataCollector inst(){
		if(instance==null)
			instance = new JobDataCollector();
		return instance;
	}
	public void parse(){
		
	}
}
