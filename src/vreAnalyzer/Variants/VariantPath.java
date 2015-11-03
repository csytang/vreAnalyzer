package vreAnalyzer.Variants;

import java.util.LinkedList;
import java.util.List;

import soot.SootMethod;
import vreAnalyzer.Elements.CallSite;

public class VariantPath {
	enum VariantStat{single, branch};
	private Variant head = null;
	private List<Variant> multipleHead = new LinkedList<Variant>();
	private SootMethod callerMethod = null;
	
	
	public VariantPath(Variant headVariant,SootMethod caller){
		head = headVariant;
		callerMethod = caller;
		
		
	}
	public VariantPath(List<Variant>headVariants, SootMethod caller){
		multipleHead = headVariants;
		callerMethod = caller;
		
	}
	public void linkNodes(Variant pred,Variant succeed,CallSite site){
		if(site==null){
			pred.addSucceedVariant(succeed, null);
			succeed.addPrecursorVariant(pred, null);
		}else{
			pred.addSucceedVariant(succeed, site);
			succeed.addPrecursorVariant(pred, site);
		}
	}
	
	
	public void traverse(){ // 遍历整个路径
		
	}
}
