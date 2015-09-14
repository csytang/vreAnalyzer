package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;

import soot.jimple.Stmt;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CodeBlock;
import vreAnalyzer.Tag.BlockMarkedTag;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Tag.SourceLocationTag.LocationType;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;

public class JobUseAnnotate {
	JobVariable hostJob;
	int[][]positions;
	int[]lines;
	Color annotatedColor;
	boolean startFromSource = false;
	boolean firstime = true;
	int counter = 0;
	
	public JobUseAnnotate(JobVariable job,LinkedList<CodeBlock>blocks,File htmlFile){
		this.hostJob = job;
		String hovertext = "Job:"+job.toString()+"(Id:"+job.getJobId()+")";
		annotatedColor = job.getAnnotatedColor();
		firstime = true;
		
		for(CodeBlock block:blocks){
			BlockMarkedTag bmkTag;
			// add job marked tag to this statement
			if( (bmkTag = (BlockMarkedTag) block.getTag(BlockMarkedTag.TAG_NAME))==null){
				bmkTag = new BlockMarkedTag();
				bmkTag.addJob(job);
				block.addTag(bmkTag);
			}else{
				bmkTag.addJob(job);
			}
			for(CFGNode node:block.getCFGNodes()){
				if(node.isSpecial())
					continue;
				Stmt useStmt = node.getStmt();
				
				
				SourceLocationTag slcTag = (SourceLocationTag) useStmt.getTag(SourceLocationTag.TAG_NAME);
				if(firstime){
					if(slcTag.getTagType()==LocationType.SOURCE_TAG){
						startFromSource = true;
						positions = new int[block.getCFGNodes().size()][4];
					}else{
						startFromSource = false;
						lines = new int[block.getCFGNodes().size()];
					}
					firstime = false;
				}
				
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
}
