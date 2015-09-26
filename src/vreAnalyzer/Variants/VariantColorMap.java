package vreAnalyzer.Variants;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import soot.Value;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.Util.RandomColor;

public class VariantColorMap {
	
	
	public static VariantColorMap instance = null;
	private static Map<String,Set<Variant>>hexColorToVariant = new HashMap<String,Set<Variant>>();
	private static Map<Set<Variant>,Color>combinedToColor = new HashMap<Set<Variant>,Color>();
	
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
		String hex = ColorToHex(color);
		Set<Variant>variant = new HashSet<Variant>();
		variant.add(vi);
		hexColorToVariant.put(hex, variant);
	}
	public String ColorToHex(Color col){
		String hex = "";
		hex = Integer.toHexString(col.getRGB() & 0xffffff);
		if (hex.length() < 6) {
		    hex = "0" + hex;
		}
		hex = "#" + hex;
		return hex;
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

	public Set<Variant> getVariantFromhexColor(String hex) {
		// TODO Auto-generated method stub
		return hexColorToVariant.get(hex);
	}

	public Color getCombinedColor(Set<Variant> currvariants) {
		// TODO Auto-generated method stub
		if(combinedToColor.containsKey(currvariants)){
			return combinedToColor.get(currvariants);
		}else{
			RandomColor rcolor = new RandomColor();
			combinedToColor.put(currvariants, rcolor.getColor());
			return rcolor.getColor();
		}
	}

	public void addHexColorToVariant(String hex, Set<Variant> currvariants) {
		// TODO Auto-generated method stub
		hexColorToVariant.put(hex, currvariants);
	}
}
