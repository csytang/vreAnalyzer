package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;

import Patch.Hadoop.Tag.BlockJobTag;
import soot.jimple.Stmt;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;

public class JobUseAnnotate {
	JobVariable hostJob;
	int[][]positions;
	int[]lines;
	Color annotatedColor;
	boolean startFromSource = false;
	int counter = 0;
	
	public JobUseAnnotate(JobVariable job,LinkedList<CodeBlock>blocks,File htmlFile){
		this.hostJob = job;
		String hovertext = "Job:"+job.toString()+"(Id:"+job.getJobId()+")";
		annotatedColor = ColorMap.inst().getJobColor(job);
		startFromSource = vreAnalyzerCommandLine.isStartFromSource();
		
		for(CodeBlock block:blocks){
			BlockJobTag bmkTag;
			// add job marked tag to this statement
			if((bmkTag = (BlockJobTag) block.getTag(BlockJobTag.TAG_NAME))==null){
				bmkTag = new BlockJobTag();
				bmkTag.addJob(job);
				block.addTag(bmkTag);
			}else{
				bmkTag.addJob(job);
			}
			
			
			if(startFromSource){
				positions = new int[block.getCFGNodes().size()][4];
			}else{
				lines = new int[block.getCFGNodes().size()];
			}
			for(int i = 0;i < block.getCFGNodes().size();i++){
				CFGNode node = block.getCFGNodes().get(i);
				if(node.isSpecial())
					continue;
				Stmt useStmt = node.getStmt();
				SourceLocationTag slcTag = (SourceLocationTag) useStmt.getTag(SourceLocationTag.TAG_NAME);
				
				if(startFromSource){
					int startline = slcTag.getStartLineNumber();
					int startcolumn = slcTag.getStartPos();
					int endline = slcTag.getEndLineNumber();
					int endcolumn = slcTag.getEndPos();
					positions[i][0] = startline;
					positions[i][1] = startcolumn;
					positions[i][2] = endline;
					positions[i][3] = endcolumn;
				}else{
					int startline = slcTag.getStartLineNumber();
					lines[i] = startline;
				}
				
			}
			if(startFromSource){
				HTMLAnnotation.annotatemultipleLineHTML(hovertext,htmlFile, positions, annotatedColor, MainFrame.inst().getHTMLToJava());
			}else{
				HTMLAnnotation.annotatemultipleLineHTML(hovertext,htmlFile, lines, annotatedColor, MainFrame.inst().getHTMLToJava());
			}
		}
	}
}
