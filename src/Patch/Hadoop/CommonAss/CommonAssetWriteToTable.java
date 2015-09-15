package Patch.Hadoop.CommonAss;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Patch.Hadoop.Job.JobVariable;
import vreAnalyzer.Elements.CodeBlock;
import vreAnalyzer.UI.MainFrame;


public class CommonAssetWriteToTable {
	public static CommonAssetWriteToTable instance;
	public static CommonAssetWriteToTable inst() {
		// TODO Auto-generated method stub
		if(instance==null)
			instance = new CommonAssetWriteToTable();
		return instance;
	}
	
	public void showData(Map<CodeBlock,CommonAsset> commonAssets) {
		// TODO Auto-generated method stub
		JTable commonassets = MainFrame.inst().getCommonAssetTable();
		DefaultTableModel commonassetmodel = (DefaultTableModel)commonassets.getModel();
		for(Map.Entry<CodeBlock, CommonAsset>entry:commonAssets.entrySet()){
			CommonAsset asset = entry.getValue();
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
