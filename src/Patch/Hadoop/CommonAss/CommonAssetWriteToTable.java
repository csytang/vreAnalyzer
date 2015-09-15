package Patch.Hadoop.CommonAss;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Patch.Hadoop.Job.JobVariable;
import vreAnalyzer.UI.MainFrame;


public class CommonAssetWriteToTable {
	public static CommonAssetWriteToTable instance;
	public static CommonAssetWriteToTable inst() {
		// TODO Auto-generated method stub
		if(instance==null)
			instance = new CommonAssetWriteToTable();
		return instance;
	}
	public void showData(List<CommonAsset> commonAssets) {
		// TODO Auto-generated method stub
		JTable commonassets = MainFrame.inst().getCommonAssetTable();
		DefaultTableModel commonassetmodel = (DefaultTableModel)commonassets.getModel();
		for(int i = 0;i < commonAssets.size();i++){
			//"CommonAsset ID","Color","LOC","Shared Feature IDs","Element Type"
			CommonAsset asset = commonAssets.get(i);
			List<JobVariable>jobs = asset.getJobs();
			List<Integer>ids = new LinkedList<Integer>();
			for(JobVariable jb:jobs){
				ids.add(jb.getJobId());
			}
			commonassetmodel.addRow(new Object[]{i,asset.getColor(),asset.getLOC(),ids,asset.getType()});
		}
	}
	
	
}
