package Patch.Hadoop.Job;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import Patch.Hadoop.ProjectParser;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.Util.RandomColor;

public class ColorMap {
	/**
	 * Move the color map issue to this file and directly write to legend
	 */
	public static ColorMap instance = null;
	private static Map<JobVariable,Color>jbToColor = null;
	private static ArrayList<Set<JobVariable>>indexToJobs = null;
	private static Map<String,Set<JobVariable>>hexColorToJob = new HashMap<String,Set<JobVariable>>();
	private static Map<Set<JobVariable>,Color>combinedToColor = null;
	private DefaultTableModel treemodel = null;
	public static ColorMap inst(){
		if(instance==null)
			instance = new ColorMap();
		return instance;
	}
	public ColorMap(){
		indexToJobs = new ArrayList<Set<JobVariable>>();
		jbToColor = new HashMap<JobVariable,Color>();
		combinedToColor = new HashMap<Set<JobVariable>,Color>();
	}
	public void registerLegendJobsList(Set<JobVariable>jobs){
		indexToJobs.add(jobs);
	}
	public ArrayList<Set<JobVariable>> getLegendJobsList(){
		return indexToJobs;
	}
	public void registerJobColor(JobVariable job,Color color){
		this.jbToColor.put(job, color);
	}
	public Color getJobColor(JobVariable job){
		return this.jbToColor.get(job);
	}
	public Map<String,Set<JobVariable>> getJobColorMap(){
		return hexColorToJob;
	}
	public Set<JobVariable> getJobVariableFromhexColor(String hexColor){
		return hexColorToJob.get(hexColor);
	}
	public void addHexColorToJob(String hex,Set<JobVariable>jobs){
		hexColorToJob.put(hex, jobs);
	}
	public static Color getCombinedColor(Set<JobVariable>jobs){
		if(combinedToColor.containsKey(jobs)){
			return combinedToColor.get(jobs);
		}else{
			RandomColor rcolor = new RandomColor();
			combinedToColor.put(jobs, rcolor.getColor());
			return rcolor.getColor();
		}
	}
	public DefaultTableModel getTableModel(){
		return treemodel;
	}
	public void addToLegend(){
		
		String legendheaders[] = {"Feature","Color"};
		DefaultTableModel model = new DefaultTableModel(null,legendheaders){
			@Override
			public boolean isCellEditable(int row, int column) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		
		for(Map.Entry<String, Set<JobVariable>>entry:hexColorToJob.entrySet()){
				Color color = ProjectParser.hex2Rgb(entry.getKey());
				String jobsString = "[";
				for(JobVariable jb:entry.getValue()){
					jobsString+=jb.getJobId();
					jobsString+=":";
				}
				jobsString = jobsString.substring(0, jobsString.length()-1);
				jobsString+="]";
				ColorMap.inst().registerLegendJobsList(entry.getValue());
				model.addRow(new Object[]{jobsString,color});
		}
		
		treemodel = model;
	}
	public void addLegendListener(){
		final JTable table = MainFrame.inst().getJobColorMapTable();
		final ArrayList<Set<JobVariable>>indexToJobs = ColorMap.inst().getLegendJobsList();
		table.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent me){
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
								// TODO Auto-generated catch block
								e1.printStackTrace();
						}
					}
				}
			}
		});
	}
}
