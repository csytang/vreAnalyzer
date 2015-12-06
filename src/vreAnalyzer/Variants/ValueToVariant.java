package vreAnalyzer.Variants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import soot.Value;
import vreAnalyzer.Elements.CallSite;

public class ValueToVariant {
	// 一个Value对应多个Variant
	private Map<Value,Set<Variant>> valueToVariant = new HashMap<Value,Set<Variant>>();
	
	// 这个函数中的所有Variant
	private Set<Variant> variantSet = new HashSet<Variant>();
	
	// 如果函数是一个调用函数 那么这里为空
	@SuppressWarnings("unused")
	private CallSite callsite = null;
	
	public ValueToVariant(CallSite callsite){
		this.callsite = callsite;
	}
	
	// 获得这个函数中对应的所有Value集合
	public Set<Value> getValueSet(){
		return this.valueToVariant.keySet();
	}
	
	// 将Value 对应Variant集合 加入到Map
	public void addValueToVariant(Value value, Set<Variant> variantset){
		if(valueToVariant.containsKey(value)){
			valueToVariant.get(value).addAll(variantset);
		}else{
			valueToVariant.put(value, variantset);
		}
		variantSet.addAll(variantset);
	}
	
	// 将Value 對應Variant 加入Map
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
