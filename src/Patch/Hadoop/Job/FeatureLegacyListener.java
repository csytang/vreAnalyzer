package Patch.Hadoop.Job;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JTable;

import vreAnalyzer.UI.MainFrame;

public class FeatureLegacyListener implements MouseListener{
	JTable table;
	ArrayList<Set<JobVariable>>indexToJobs;
	public FeatureLegacyListener(ArrayList<Set<JobVariable>>indexToJobs, JTable table){
		this.table = table;
		this.indexToJobs = indexToJobs;
	}
	@Override
	public void mouseClicked(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent me) {
		Point p = me.getPoint();
		int row = table.rowAtPoint(p);
		if(me.getClickCount()==2){			
			Set<JobVariable> jobs = indexToJobs.get(row);
			if(jobs.size()==1){
				JobVariable job = null;
				for(JobVariable jb:jobs){
					job = jb;
				}
				File sourceFile = job.getSourceFile();
				String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
				htmlfileName+=".html";
				File htmlFile = new File(htmlfileName);
				JEditorPane txtrSource = MainFrame.getSrcTextPane();
				txtrSource.setContentType("text/html");
				List<String> content;
				try {
					content = Files.readAllLines(htmlFile.toPath(),Charset.defaultCharset());
					String allString = "";
					for(String subcontent:content){
						allString+=subcontent;
						allString+="\n";
					}		
					txtrSource.setText("");
					txtrSource.setText(allString);
					txtrSource.setCaretPosition(0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}		
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}

}
