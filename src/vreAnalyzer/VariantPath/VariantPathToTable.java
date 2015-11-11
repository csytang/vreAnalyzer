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
	private Map<Integer,Integer> rowToPathId = new HashMap<Integer,Integer>();
	// 从路径id 对应到 图片文件上
	private Map<Integer,File> pathIdToFile = new HashMap<Integer,File>();
	
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
		rowToPathId.clear();
		String idList = "";
		idList += "[";
		int index = 0;
		for(Variant variant:variants){
			idList += variant.getVariantId();
			idList += ",";
			rowToPathId.put(index, variant.getVariantId());
			index++;
		}
		if(variants.size()>=1){
			idList = idList.substring(0, idList.length()-1);
		}
		idList += "]";
		varitablePathModel.addRow(new Object[]{pathId,idList});
	}
	public void addPathListener(){
		pathIdToFile = VariantPathAnalysis.inst().getpathIdToFile();
		variantPathTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if(variantPathTable.getSelectedRow() > -1){
					int pathId = rowToPathId.get(variantPathTable.getSelectedRow());
					File img = pathIdToFile.get(pathId);
					JPanel imagePanel = new JPanel();
					try {
						ImageDisplay imdisplay = new ImageDisplay(img,imagePanel);
						MainFrame.inst().getImageDisplayPanel().setViewportView(imagePanel);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}
	
}
