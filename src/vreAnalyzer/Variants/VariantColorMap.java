package vreAnalyzer.Variants;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.table.DefaultTableModel;
import vreAnalyzer.Util.RandomColor;

public class VariantColorMap {
	
	// 单例模式
	public static VariantColorMap instance = null;
	
	// 十六进制颜色对应Variant
	private static Map<String,Set<Variant>> hexColorToVariant = new HashMap<String,Set<Variant>>();
	
	/////////////////////////
	/*
	 * 当一个语句有属于多个Variant, 那么
	 */
	private static Map<Set<Variant>,Color> combinedToColor = new HashMap<Set<Variant>,Color>();
	
	// VariantToColor
	private Map<Variant,Color> variantToColor = null;
	////////////////////////
	
	// 在MainFrame中的treemodel
	private DefaultTableModel treemodel = null;
	
	
	
	public static VariantColorMap inst(){
		if(instance==null)
			instance = new VariantColorMap();
		return instance;
	}
	
	public VariantColorMap(){
		variantToColor = new HashMap<Variant,Color>();
	}
	
	public void registerColorForVariant(Variant vi){
		/*
		 * 注册一个新的颜色对于Variant
		 */
		RandomColor rcolor = new RandomColor();
		Color color = rcolor.getColor();
		String hex = ColorToHex(color);
		
		// 处理颜色重复问题
		while(hexColorToVariant.containsKey(hex)){
			rcolor = new RandomColor();
			color = rcolor.getColor();
			hex = ColorToHex(color);
		}
		if(!variantToColor.containsKey(vi)){
			variantToColor.put(vi, color);
		}
		
		Set<Variant>variant = new HashSet<Variant>();
		variant.add(vi);
		hexColorToVariant.put(hex, variant);
	}
	
	// 将Color转化为十六进制字符串
	public String ColorToHex(Color col){
		String hex = "";
		hex = Integer.toHexString(col.getRGB() & 0xffffff);
		if (hex.length() < 6) {
		    hex = "0" + hex;
		}
		hex = "#" + hex;
		return hex;
	}
	public void removeColorMap (Variant vi) {
		/*
		 * 将这个颜色从variantToColor列表中删除
		 */
		if(variantToColor.containsKey(vi)){
			Color color = variantToColor.get(vi);
			variantToColor.remove(vi);
			String hexColor = ColorToHex(color);
			if(hexColorToVariant.containsKey(hexColor)){
				hexColorToVariant.remove(hexColor);
			}
		}
	}
	
	
	public Color getColorforVariant(Variant variant) {
		// TODO 獲取variant的顏色
		return variantToColor.get(variant);
	}

	public Set<Variant> getVariantFromhexColor(String hex) {
		// TODO 获得hex颜色从variant中
		return hexColorToVariant.get(hex);
	}

	public Color getCombinedColor(Set<Variant> currvariants) {
		// 获得一个variant结合颜色
		if(combinedToColor.containsKey(currvariants)){
			return combinedToColor.get(currvariants);
		}else{
			RandomColor rcolor = new RandomColor();
			combinedToColor.put(currvariants, rcolor.getColor());
			String hexcolor = ColorToHex(rcolor.getColor());
			
			// 将集合颜色让如到 hexColorToVaraint中
			hexColorToVariant.put(hexcolor, currvariants);
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
		for(Variant variant:BindingResolver.inst().getfullVariantList()){
			if(variantToColor.containsKey(variant) && BindingResolver.inst().getfullVariantList().contains(variant)){
				Color color = variantToColor.get(variant);
				int variantId = variant.getVariantId();
				String variantIdStr = "["+variantId+"]";
				model.addRow(new Object[]{variantIdStr,color});
			}
		}
		treemodel = model;
		
	}
	
}
