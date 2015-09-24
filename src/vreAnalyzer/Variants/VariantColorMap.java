package vreAnalyzer.Variants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import soot.Value;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.Util.RandomColor;

public class VariantColorMap {
	
	
	public static VariantColorMap instance = null;
	/**
	 * Color inheritance:
	 * If there is an invoke expression goes like
	 * 
	 */
	private Map<Variant,Color> variantToColor = null;
	
	public static VariantColorMap inst(){
		if(instance==null)
			instance = new VariantColorMap();
		return instance;
	}
	
	public VariantColorMap(){
		variantToColor = new HashMap<Variant,Color>();
	}
	
	public void registerNewColor(Variant vi){
		RandomColor rcolor = new RandomColor();
		Color color = rcolor.getColor();
		if(!variantToColor.containsKey(vi)){
			variantToColor.put(vi, color);
		}
	}
	
	public void removeColorMap(Variant vi){
		if(variantToColor.containsKey(vi))
			variantToColor.remove(vi);
	}
	public void addToLegend(){
		JTable variantColorMapTable = MainFrame.inst().getJobColorMapTable();
		String legendheaders[] = {"Variant","Values","Color"};
		@SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel(null,legendheaders){
		@Override
		public boolean isCellEditable(int row, int column) {
			// TODO Auto-generated method stub
			return false;
			}
		};
		ArrayList<Variant>variants = BindingResolver.inst().getVariants();
		for(int i = 0;i < variants.size();i++){
			Variant vari = variants.get(i);
			List<Value>valueList = vari.getPaddingValues();
			Color color = variantToColor.get(vari);
			String values = covertValueToString(valueList);
			model.addRow(new Object[]{i,values,color});
		}
		variantColorMapTable.setModel(model);
	}
	public void addLegendListener(){
	
	}
	public String covertValueToString(List<Value>valueList){
		String result = "[";
		for(Value vit:valueList){
			result+=vit.toString();
			result+=",";
		}
		if(!valueList.isEmpty())
			result = result.substring(0,result.length()-1);
		result += "]";
		return result;
	}

	public Color getColorforVariant(Variant variant) {
		// TODO Auto-generated method stub
		return variantToColor.get(variant);
	}
}
