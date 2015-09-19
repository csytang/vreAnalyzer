package vreAnalyzer.Variants;

import java.util.HashMap;
import java.util.Map;

import vreAnalyzer.Blocks.CodeBlock;

public class DynamicVariants  {
	/**
	 * this type of variant is create and could be resolved dynamic, since it is 
	 * binding dynamically at runtime
	 * like 
	 * @overload 多載
	 * @override 複寫
	 */
	private CodeBlock variant;
	private BindingType type;
	private static Map<DynamicVariants,BindingType> dvariantpool = new HashMap<DynamicVariants,BindingType>();
	public DynamicVariants(CodeBlock block,BindingType type){
		//{"Block ID","LOC","Features(IF)","Seperators"};
		this.variant = block;
		this.type = type;
	}
	public static boolean poolContain(DynamicVariants vi){
		return dvariantpool.containsKey(vi);
	}
	public static void addToPool(DynamicVariants vi){
		dvariantpool.put(vi, vi.getType());
	}
	public BindingType getType(){
		return this.type;
	}
	public CodeBlock getBlock() {
		// TODO Auto-generated method stub
		return variant;
	}
}
