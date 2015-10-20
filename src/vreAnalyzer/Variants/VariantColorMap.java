package vreAnalyzer.Variants;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import soot.Value;
import vreAnalyzer.Util.RandomColor;

public class VariantColorMap {
	
	
	public static VariantColorMap instance = null;
	private static Map<String,Set<Variant>>hexColorToVariant = new HashMap<String,Set<Variant>>();
	private static Map<Set<Variant>,Color>combinedToColor = new HashMap<Set<Variant>,Color>();
	private DefaultTableModel treemodel = null;
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

	public DefaultTableModel getTableModel() {
		// TODO Auto-generated method stub
		return treemodel;
	}
	
	public void addToLegend(){
		// 1. 首先加入所有的单一颜色
		String legendheaders[] = {"Variant","Color"};
		DefaultTableModel model = new DefaultTableModel(null,legendheaders){
			@Override
			public boolean isCellEditable(int row, int column) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		for(Map.Entry<Variant, Color>entry : variantToColor.entrySet()){
			Variant variant = entry.getKey();
			Color color = entry.getValue();
			int variantId = variant.getVariantId();
			String variantIdStr = "["+variantId+"]";
			model.addRow(new Object[]{variantIdStr,color});
		}
		treemodel = model;
		
	}
	
}
