package vreAnalyzer.Tag;

import java.util.HashSet;
import java.util.Set;

import Patch.Hadoop.Job.JobVariable;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class StmtMarkedTag implements Tag{
	
	public static String TAG_NAME = "smkt";
	private Set<JobVariable> jobassociated;
	public StmtMarkedTag(){
		jobassociated = new HashSet<JobVariable>();
	}
	public Set<JobVariable> getJobs(){
		return this.jobassociated;
	}
	public void addJob(JobVariable job){
		this.jobassociated.add(job);
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return StmtMarkedTag.TAG_NAME;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

}
