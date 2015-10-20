package vreAnalyzer.Variants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import soot.Value;
import vreAnalyzer.Elements.CallSite;

public class ValueToVariant {
	// 
	private Map<Value,Set<Variant>> valueToVariant = new HashMap<Value,Set<Variant>>();
	private Set<Variant> variantSet = new HashSet<Variant>();
	private CallSite callsite = null;// 如果函数是一个调用函数 那么这里为空
	public ValueToVariant(CallSite callsite){
		this.callsite = callsite;
	}
	public Set<Value> getValueSet(){
		return this.valueToVariant.keySet();
	}
	public void addValueToVariant(Value value,Set<Variant> variantset){
		if(valueToVariant.containsKey(value)){
			valueToVariant.get(value).addAll(variantset);
		}else{
			valueToVariant.put(value, variantset);
		}
		variantSet.addAll(variantset);
	}
	public void addValueToVariant(Value value, Variant variant){
		if(valueToVariant.containsKey(value)){
			valueToVariant.get(value).add(variant);
		}else{
			Set<Variant>variantset = new HashSet<Variant>();
			variantset.add(variant);
			valueToVariant.put(value, variantset);
		}
		variantSet.add(variant);
	}
	
	public Set<Variant> getVariantsByValue(Value value){
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
	public Set<Variant> getVariants(){
		return this.variantSet;
	}
}
