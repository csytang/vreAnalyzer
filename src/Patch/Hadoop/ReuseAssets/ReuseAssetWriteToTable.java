package Patch.Hadoop.ReuseAssets;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Patch.Hadoop.Job.JobVariable;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.UI.MainFrame;


public class ReuseAssetWriteToTable {
	public static ReuseAssetWriteToTable instance;
	public static ReuseAssetWriteToTable inst() {
		// TODO Auto-generated method stub
		if(instance==null)
			instance = new ReuseAssetWriteToTable();
		return instance;
	}
	
	public void showData(Map<CodeBlock,ReuseAsset> commonAssets) {
		// TODO Auto-generated method stub
		JTable commonassets = MainFrame.inst().getCommonAssetTable();
		DefaultTableModel commonassetmodel = (DefaultTableModel)commonassets.getModel();
		for(Map.Entry<CodeBlock, ReuseAsset>entry:commonAssets.entrySet()){
			ReuseAsset asset = entry.getValue();
			int id = asset.getAssetId();
			List<JobVariable>jobs = asset.getJobs();
			String idString = "[";
			for(JobVariable jb:jobs){
				idString+=jb.getJobId()+",";
			}
			idString+="]";
			if(asset.getType()==AssetType.Class){
				commonassetmodel.addRow(new Object[]{id,asset.getColor(),asset.getLOC(),idString,asset.getType()+":"+asset.getSootClass().getName()});
			}else if(asset.getType()==AssetType.Method){
				commonassetmodel.addRow(new Object[]{id,asset.getColor(),asset.getLOC(),idString,asset.getType()+":"+asset.getSootMethod().getName()});
			}else if(asset.getType()==AssetType.Argument||
					asset.getType()==AssetType.Field||
					asset.getType()==AssetType.Local){
				commonassetmodel.addRow(new Object[]{id,asset.getColor(),asset.getLOC(),idString,asset.getType()+":"+asset.getVariable().toString()});
			}
		}
		
	}
	
	
}
