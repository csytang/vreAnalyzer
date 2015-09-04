package Patch.Hadoop.Job;

import java.awt.Color;
import java.io.File;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import soot.jimple.Stmt;
import vreAnalyzer.Tag.SourceLocationTag;
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
		annotatedColor = job.getAnnotatedColor();
		jobstmt = job.getCFGNode().getStmt();
		slcTag = (SourceLocationTag) jobstmt.getTag(SourceLocationTag.TAG_NAME);
		startline = slcTag.getStartLineNumber();
		startcolumn = slcTag.getStartPos();
		endline = slcTag.getEndLineNumber();
		endcolumn = slcTag.getEndPos();
		
		// set the color job mapping to the MainFrame
		JTable jobColorMapTable = MainFrame.inst().getJobColorMapTable();
		DefaultTableModel model = (DefaultTableModel)jobColorMapTable.getModel();
		model.addRow(new Object[]{job.toString(),annotatedColor});
		
		// annotated source code
		HTMLAnnotation.annotateHTML(htmlFile, startline, startcolumn, endline, endcolumn, annotatedColor,MainFrame.inst().getHTMLToJava());
	}
}
