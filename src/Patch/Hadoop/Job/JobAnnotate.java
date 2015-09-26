package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;
import soot.jimple.Stmt;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Tag.SourceLocationTag.LocationType;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;

public class JobAnnotate {
	JobVariable hostJob;
	Stmt jobstmt;
	Color annotatedColor;
	SourceLocationTag slcTag;
	int startline = 0;
	int startcolumn = 0;
	int endline = 0;
	int endcolumn = 0;
	
	public JobAnnotate(JobVariable job,File htmlFile){
		this.hostJob = job;
		String hovertext = "Job:"+job.toString()+"(Id:"+job.getJobId()+")";
		annotatedColor = ColorMap.inst().getJobColor(job);
		jobstmt = job.getBlock().getCFGNodes().get(0).getStmt();
		// set the color job mapping to the MainFrame
		
		slcTag = (SourceLocationTag) jobstmt.getTag(SourceLocationTag.TAG_NAME);
		if(slcTag.getTagType()==LocationType.SOURCE_TAG){
			startline = slcTag.getStartLineNumber();
			startcolumn = slcTag.getStartPos();
			endline = slcTag.getEndLineNumber();
			endcolumn = slcTag.getEndPos();
			// annotated source code
			HTMLAnnotation.annotateHTML_Feature(job,hovertext,htmlFile, startline, startcolumn, endline, endcolumn,MainFrame.inst().getHTMLToJava());
		}else{
			startline = slcTag.getStartLineNumber();
			HTMLAnnotation.annotateHTML_Feature(job,hovertext,htmlFile, startline, MainFrame.inst().getHTMLToJava());
		}
		
		
	}
}
