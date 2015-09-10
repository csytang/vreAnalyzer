package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;

import soot.jimple.Stmt;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Tag.StmtMarkedTag;
import vreAnalyzer.Tag.SourceLocationTag.LocationType;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;

public class JobUseAnnotate {
	JobVariable hostJob;
	int[][]positions;
	int[]lines;
	Color annotatedColor;
	boolean startFromSource = false;
	int counter = 0;
	
	public JobUseAnnotate(JobVariable job,LinkedList<CFGNode>cfgNodes,File htmlFile){
		this.hostJob = job;
		String hovertext = "Job:"+job.toString()+"(Id:"+job.getJobId()+")";
		annotatedColor = job.getAnnotatedColor();
		SourceLocationTag taginst = (SourceLocationTag) cfgNodes.get(0).getStmt().getTag(SourceLocationTag.TAG_NAME);
		if(taginst.getTagType()==LocationType.SOURCE_TAG){
			positions = new int[cfgNodes.size()][4];
			startFromSource = true;
		}else{
			lines = new int[cfgNodes.size()];
			startFromSource = false;
		}
		for(CFGNode node:cfgNodes){
			
			Stmt useStmt = node.getStmt();
			
			// add job marked tag to this statement
			StmtMarkedTag smkTag = new StmtMarkedTag(job);
			useStmt.addTag(smkTag);
			
			SourceLocationTag slcTag = (SourceLocationTag) useStmt.getTag(SourceLocationTag.TAG_NAME);
			if(startFromSource){
				int startline = slcTag.getStartLineNumber();
				int startcolumn = slcTag.getStartPos();
				int endline = slcTag.getEndLineNumber();
				int endcolumn = slcTag.getEndPos();
				positions[counter][0] = startline;
				positions[counter][1] = startcolumn;
				positions[counter][2] = endline;
				positions[counter][3] = endcolumn;
			}else{
				int startline = slcTag.getStartLineNumber();
				lines[counter] = startline;
			}
			counter++;
		}
		if(startFromSource){
			HTMLAnnotation.annotatemultipleLineHTML(hovertext,htmlFile, positions, annotatedColor, MainFrame.inst().getHTMLToJava());
		}else{
			HTMLAnnotation.annotatemultipleLineHTML(hovertext,htmlFile, lines, annotatedColor, MainFrame.inst().getHTMLToJava());
		}
	}
}
