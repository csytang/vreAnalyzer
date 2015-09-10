package vreAnalyzer.Tag;

import Patch.Hadoop.Job.JobVariable;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class StmtMarkedTag implements Tag{
	
	public static String TAG_NAME = "smkt";
	private JobVariable jobassociated;
	public StmtMarkedTag(JobVariable job){
		this.jobassociated = job;
	}
	public JobVariable getJob(){
		return this.jobassociated;
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
