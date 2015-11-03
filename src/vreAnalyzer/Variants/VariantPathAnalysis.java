package vreAnalyzer.Variants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class VariantPathAnalysis {
	public static VariantPathAnalysis instance;
	// 包含函数调用的函数
    protected List<SootMethod> callerMethod = null;
    // 不包含任何函数调用的函数
    protected List<SootMethod> calleeMethod = null;
    
	private List<VariantPath> variantPathList = new LinkedList<VariantPath>();
	
	private boolean isFirstVariant = true;
	
	private Queue<Variant> panddingVariants = new LinkedList <Variant>();
	
	public static VariantPathAnalysis inst(){
		if(instance==null)
			instance = new VariantPathAnalysis();
		return instance;
	}
	public VariantPathAnalysis(){
		variantPathList = new LinkedList<VariantPath>();
	}
	
	public void parse(Map<SootMethod,List<Variant>> methodToVariants){
		// 1. 获得所有的Caller函数
		List<SootMethod>callerMethods = BindingResolver.inst().getCallerMethods();
		List<Variant> nodebindingVariants = new LinkedList<Variant>();
		VariantPath curr = null;
		for(SootMethod caller:callerMethods){
			List<Variant>bindingVariants = methodToVariants.get(caller);
			CFG cfg = ProgramFlowBuilder.inst().getCFG(caller);
			List<CFGNode>cfgNodes = cfg.getNodes();
			for(CFGNode node:cfgNodes){
				if(node.isSpecial()){
					continue;
				}
				nodebindingVariants.clear();
				nodebindingVariants = getBindingVaraints(bindingVariants,node,null);
				if(!nodebindingVariants.isEmpty()){// 有variant涉及到这个语句
					if(isFirstVariant){// 如果是第一个涉及到Variant的语句
						// 创建一个VariantPath
						VariantPath variantPath = null;
						if(nodebindingVariants.size()==1){
							variantPath = new VariantPath(nodebindingVariants.get(0),caller);
						}else{
							variantPath = new VariantPath(nodebindingVariants,caller);
						}
						// 进入队列
						for(Variant variant:nodebindingVariants){
							panddingVariants.add(variant);
						}
						curr = variantPath;
						isFirstVariant = false;
					}else if(!panddingVariants.isEmpty()){
						if(containsList(panddingVariants,bindingVariants)){// 如果当前有Variant正在分析 并且获得的Variant不是新的Variant
					
						}else{// 如果当前的Variant正在分析 未结束 并且获得了新的Variant
							
						}
					}else if(panddingVariants.isEmpty()){// 如果当前没有Variant分析 获得了新的Variant
						
					}
					
				}
				
			}
		}
		
		// 2. 获得所有的Callee函数
		List<SootMethod>calleeMethods = BindingResolver.inst().getCalleeMethods();
		for(SootMethod callee:calleeMethods){
			List<Variant>bindingVariants = methodToVariants.get(callee);
			CFG cfg = ProgramFlowBuilder.inst().getCFG(callee);
			List<CFGNode>cfgNodes = cfg.getNodes();
			for(CFGNode node:cfgNodes){
				if(node.isSpecial())
					continue;
				
			}
		}
		
		
	}
	
	public List<Variant> getBindingVaraints(List<Variant> variantlist,CFGNode cfgNode,CallSite callsite){
		List<Variant> bindingVariants = new LinkedList<Variant>();
		if(callsite==null){
			
		}else{
			
		}
		return bindingVariants;
	}
	public boolean containsList(List<Variant> variantlist,List<Variant> variantlistb){
		if(variantlist.containsAll(variantlistb))
			return true;
		else
			return false;
	}
	public boolean containsList(Queue<Variant> queueVariants, List<Variant> listVariants) {
		// TODO Auto-generated method stub
		List<Variant> queueToList = new LinkedList<Variant>(queueVariants);
		if(queueToList.containsAll(listVariants))
			return true;
		else
			return false;
	}
}
