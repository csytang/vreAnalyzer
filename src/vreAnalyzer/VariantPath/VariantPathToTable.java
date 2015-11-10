package vreAnalyzer.VariantPath;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import vreAnalyzer.UI.MainFrame;

public class VariantPathToTable {
	// 单例模式
	public static VariantPathToTable instance;
	public static VariantPathToTable inst(){
		if(instance==null)
			instance = new VariantPathToTable();
		return instance;
	}
	private JTable variantPathTable;
	private DefaultTableModel varitablePathModel;
	
	public VariantPathToTable(){
		/*
		 * "Path ID","Variants(in Id)"
		 */
		variantPathTable = MainFrame.inst().getVariantPathTable();
		varitablePathModel = (DefaultTableModel) variantPathTable.getModel();
	}
	public void addRowToTable(int pathId,String variantsString){
		varitablePathModel.addRow(new Object[]{pathId,variantsString});
	}
	
}
