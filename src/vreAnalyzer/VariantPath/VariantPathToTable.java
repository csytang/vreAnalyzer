package vreAnalyzer.VariantPath;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.UI.NonEditableModel;
import vreAnalyzer.Util.Graphviz.ImageDisplay;
import vreAnalyzer.Variants.Variant;

public class VariantPathToTable {
	// 单例模式
	public static VariantPathToTable instance;
	// 从表格行 对应到 路径id
	private static Map<Integer,Integer> rowToPathId = new HashMap<Integer,Integer>();
	// 从路径id 对应到 图片文件上
	private Map<Integer,File> pathIdToFile = new HashMap<Integer,File>();
	private boolean verbose = true;
	private static int tableRowIndex = 0;
	public static VariantPathToTable inst(){
		if(instance==null)
			instance = new VariantPathToTable();
		return instance;
	}
	
	private JTable variantPathTable;
	private NonEditableModel varitablePathModel;
	
	public VariantPathToTable(){
		/*
		 * "Path ID","Variants(in Id)"
		 */
		variantPathTable = MainFrame.inst().getVariantPathTable();
		varitablePathModel = (NonEditableModel) variantPathTable.getModel();
	}
	public void addARowToTable(int pathId,Set<Variant>variants){
		String idList = "";
		idList += "[";
		
		for(Variant variant:variants){
			idList += variant.getVariantId();
			idList += ",";
		}
		rowToPathId.put(VariantPathToTable.tableRowIndex, pathId);
		if(verbose){
			System.out.println("Add Table to row:"+VariantPathToTable.tableRowIndex+"\twith pathId:"+pathId);
		}
		if(variants.size()>=1){
			idList = idList.substring(0, idList.length()-1);
		}
		idList += "]";
		varitablePathModel.addRow(new Object[]{pathId,idList});
		VariantPathToTable.tableRowIndex++;
	}
	public void addPathListener(){
		pathIdToFile = VariantPathAnalysis.inst().getpathIdToFile();
		variantPathTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(variantPathTable.getSelectedRow() > -1){
					int selectedRow = variantPathTable.getSelectedRow();
					int pathId = rowToPathId.get(selectedRow);
					File img = pathIdToFile.get(pathId);
					JPanel imagePanel = new JPanel();
					try {
						ImageDisplay imdisplay = new ImageDisplay(img,imagePanel);
						MainFrame.inst().getImageDisplayPanel().setViewportView(imdisplay.getImagePanel());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			
			
			
			
		});
	}
	
}
