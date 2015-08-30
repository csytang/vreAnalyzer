package Patch.Hadoop;

import java.awt.Color;
import java.util.Map;

import Patch.Hadoop.Annotation.JobAnnotation;
import vreAnalyzer.UI.MainFrame;

public class ProjectAnnotation {
	
	public static ProjectAnnotation instance;
	private Map<JobVariable,JobHub>jobtoHub = null;
	public static ProjectAnnotation inst(){
		if(instance==null)
			instance = new ProjectAnnotation();
		return instance;
	}
	public ProjectAnnotation(){
		jobtoHub = ProjectParser.inst().getjobtoHub();
	}
	public void annotateJob(){
		for(Map.Entry<JobVariable, JobHub>entry:jobtoHub.entrySet()){
			JobVariable job = entry.getKey();
			// Annotate this job 
			Color jobcolor = job.getAnnotatedColor();
			JobAnnotation annotation = new JobAnnotation(job,jobcolor,MainFrame.inst().getSrcTextArea());
		}
	}
	
}
