package Patch.Hadoop.Statistic;

import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import vreAnalyzer.UI.MainFrame;
import Patch.Hadoop.Job.ColorMap;
import Patch.Hadoop.Job.JobHub;
import Patch.Hadoop.Job.JobVariable;

public class JobDataWriteToTable {
	public static JobDataWriteToTable instance;
	
	public JobDataWriteToTable(){
		
	}
	public static JobDataWriteToTable inst(){
		if(instance==null){
			instance = new JobDataWriteToTable();
		}
		return instance;
	}
	public void showData(Map<JobVariable,JobHub>jobtoHub){
		// get Table
		JTable jobstatisticTable = MainFrame.inst().getStatisticTable();
		DefaultTableModel statisticmodel = (DefaultTableModel)jobstatisticTable.getModel();
		// write to table
		for(Map.Entry<JobVariable, JobHub>entry:jobtoHub.entrySet()){
			JobVariable job = entry.getKey();
			JobStatistic statisitc = JobDataCollector.inst().jobToStatistic.get(job);
			/*
			 *String statheaders[] = {"Feature","Color","LOC","Reuse LOC","Dependenency(LOC)"}; 
			 */
			statisticmodel.addRow(new Object[]{job.toString(),ColorMap.inst().getJobColor(job),statisitc.getLOC(),statisitc.getReuseCode(),""});
		}
	}
	
}
