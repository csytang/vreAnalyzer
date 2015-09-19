package Patch.Hadoop.Job;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import vreAnalyzer.UI.RandomColor;

public class ColorMap {
	public static ColorMap instance;
	private static Map<JobVariable,Color>jbToColor;
	private static ArrayList<Set<JobVariable>>indexToJobs;
	private static Map<String,Set<JobVariable>>hexColorToJob = new HashMap<String,Set<JobVariable>>();
	private static Map<Set<JobVariable>,Color>combinedToColor;
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
}
