package vreAnalyzer.Variants;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Value;

public class ValueToVariant {
	
	private Map<Value,List<Variant>>valueToVariant = new HashMap<Value,List<Variant>>();
	
	public ValueToVariant(){
		
	}
	public void addValueToVariant(Value value,List<Variant>variantlist){
		if(valueToVariant.containsKey(value)){
			valueToVariant.get(value).addAll(variantlist);
		}else{
			valueToVariant.put(value, variantlist);
		}
	}
	public void addValueToVariant(Value value,Variant variant){
		if(valueToVariant.containsKey(value)){
			valueToVariant.get(value).add(variant);
		}else{
			List<Variant>variantlist = new LinkedList<Variant>();
			variantlist.add(variant);
			valueToVariant.put(value, variantlist);
		}
	}
	public List<Variant> getVariantsByValue(Value value){
		if(valueToVariant.containsKey(value)){
			return valueToVariant.get(value);
		}else
			return null;
	}
	public boolean containsValue(Value value){
		if(valueToVariant.containsKey(value)){
			return true;
		}else
			return false;
	}
}
