package vreAnalyzer.Variants;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.UI.MainFrame;

public class VariantToTable {
	public static VariantToTable instance;
	public static VariantToTable inst(){
		if(instance==null)
			instance = new VariantToTable();
		return instance;
	}
	public VariantToTable(){
		
	}
	public void addVariantToTable(List<Variant> fullvaraintlist){
		/*
		 * "Variant ID","Parent Block Id","separators","SootMethod","SootClass"
		 */
		JTable jvariantTable = MainFrame.inst().getVariantTable();
		DefaultTableModel varitableModel = (DefaultTableModel) jvariantTable.getModel();
		for(Variant variant:fullvaraintlist){
			int id = variant.getVariantId();
			List<Integer> blocksIds = new LinkedList<Integer>();
			blocksIds = variant.getBlockIds();
			
			List<SootMethod> methods = variant.getAllMethods();
			List<SootClass> classes = variant.getAllClasses();
			
			// 将这个内容写入到表格中
			String variantId = ""+id;
			String blockidString = IdsToString(blocksIds);
			String methodsString = MethodsToString(methods);
			String classString = ClassesToString(classes);
			varitableModel.addRow(new Object[]{variantId,blockidString,"-",methodsString,classString});
			
		}		
	}
	
	public String IdsToString(List<Integer>blockIds){
		String ids = "[";
		for(int id:blockIds){
			ids += id;
			ids += ",";
		}
		if(blockIds.size()>=1){
			ids = ids.substring(0, ids.length()-1);
		}
		ids += "]";
		return ids;
	}
	public String MethodsToString(List<SootMethod> methods){
		String methodName = "";
		for(SootMethod method:methods){
			methodName += method.getName();
		}
		return methodName;
	}
	
	public String ClassesToString(List<SootClass>classes){
		String className = "";
		for(SootClass sootcls: classes){
			className += sootcls.getName();
		}
		return className;
	}
	
}
