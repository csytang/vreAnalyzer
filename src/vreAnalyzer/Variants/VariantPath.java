package vreAnalyzer.Variants;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import soot.SootMethod;
import vreAnalyzer.Elements.CallSite;

public class VariantPath {
	enum VariantStat{single, branch};
	// 路径中当前节点
	private Variant curr = null;// 当前路径的最后一个单一节点
	private List<Variant> currList = new LinkedList<Variant>();// 当前路径的最后一个多节点集合
	
	// 开始节点
	private Variant head = null;
	private Set<Variant> headSet = new HashSet<Variant>();
	
	// 当前路径状态
	private VariantStat status = null;
	
	private SootMethod callerMethod = null;
	
	public VariantPath(Variant headVariant,SootMethod caller){
		head = headVariant;
		callerMethod = caller;
		curr = headVariant;
		status = VariantStat.single;
	}
	
	public VariantPath(List<Variant>headVariants, SootMethod caller){
		headSet.addAll(headVariants);
		callerMethod = caller;
		currList = headVariants;
		status = VariantStat.branch;
	}
	
	public void addNextNode(Variant variant,CallSite site){
		if(site==null){
			if(status==VariantStat.single){// 前驱节点为单一节点
				curr.addSucceedVariant(variant, null);
				variant.addPrecursorVariant(curr, null);
			}else{
				for(Variant var:currList){
					var.addSucceedVariant(variant, null);
					variant.addPrecursorVariant(var, null);
				}
			}
		}else{
			if(status==VariantStat.single){
				curr.addSucceedVariant(variant, site);
				variant.addPrecursorVariant(curr, null);
			}else{
				for(Variant var:currList){
					var.addSucceedVariant(variant, site);
					variant.addPrecursorVariant(var, site);
				}
			}
		}
	}
	
	public void addNextNode(List<Variant>variants,CallSite site){
		if(site==null){
			if(status==VariantStat.single){
				for(Variant var:variants){
					curr.addSucceedVariant(var, null);
					var.addPrecursorVariant(curr, null);
				}
			}else{
				for(Variant pre:currList){
					for(Variant var:variants){
						pre.addSucceedVariant(var, null);
						var.addPrecursorVariant(pre, null);
					}
				}
			}
		}else{
			if(status==VariantStat.single){
				for(Variant var:variants){
					curr.addSucceedVariant(var, site);
					var.addPrecursorVariant(curr, site);
				}
			}else{
				for(Variant pre:currList){
					for(Variant var:variants){
						pre.addSucceedVariant(var, site);
						var.addPrecursorVariant(pre, site);
					}
				}
			}
		}
	}
	
	public void addParallelNode(Variant variant,CallSite site){
		if(site==null){
			Set<Variant> precursors = curr.getPrecursorVariants(null);// 当前节点的前驱节点
			// 1. 对于所有的precursor来讲, 在所有的上加入新的 后继节点
			if(precursors.isEmpty()){
				if(head!=null && headSet.isEmpty()){
					headSet.add(head);
					headSet.add(variant);
					head = null;
				}else if(!headSet.isEmpty()){
					headSet.add(variant);
				}
			}else{
				for(Variant prevar:precursors){
					// 2.  额外检查所有并行点的后继
					if(prevar.getSucceedVariants(null).isEmpty()){
						prevar.addSucceedVariant(variant, null);
					}else{
						Set<Variant>varSucceeds = prevar.getSucceedVariants(null);
						for(Variant varsucc:varSucceeds){
							prevar.addSucceedVariant(variant, null);
							variant.addSucceedVariant(varsucc, null);
						}
					}
					// 3. 对于新加入的后继设计 前驱
					variant.addPrecursorVariant(prevar, null);
				}
			}
		}else{
			Set<Variant> precursors = curr.getPrecursorVariants(site);// 当前节点的前驱节点
			if(precursors.isEmpty()){
				Set<Variant>varSucceeds = curr.getSucceedVariants(site);
				for(Variant varsucc:varSucceeds){
					variant.addSucceedVariant(varsucc, site);
				}
			}else{
				// 1. 对于所有的precursor来讲, 在所有的上加入新的 后继节点
				for(Variant prevar:precursors){
					// 2.  额外检查所有并行点的后继
					if(prevar.getSucceedVariants(site).isEmpty()){
						prevar.addSucceedVariant(variant, site);
					}else{
						Set<Variant>varSucceeds = prevar.getSucceedVariants(site);
						for(Variant varsucc:varSucceeds){
							prevar.addSucceedVariant(variant, site);
							variant.addSucceedVariant(varsucc, site);
						}
					}
					// 3. 对于新加入的后继设计 前驱
					variant.addPrecursorVariant(prevar, site);
				}
			}
		}
	}
	
	public void addParallelNode(List<Variant>variants,CallSite site){
		if(site==null){
			Set<Variant> precursors = curr.getPrecursorVariants(null);// 当前节点的前驱节点
			// 1. 对于所有的precursor来讲, 在所有的上加入新的 后继节点
			if(precursors.isEmpty()){
				if(head!=null && headSet.isEmpty()){
					headSet.add(head);
					headSet.addAll(variants);
					head = null;
				}else if(!headSet.isEmpty()){
					headSet.addAll(variants);
				}
			}else{
				for(Variant prevar:precursors){
					for(Variant var:variants){
						// 2.  额外检查所有并行点的后继
						if(var.getSucceedVariants(null).isEmpty())
							prevar.addSucceedVariant(var, null);
						else{
							Set<Variant>varSucceeds = var.getSucceedVariants(null);
							for(Variant varsucc:varSucceeds){
								prevar.addSucceedVariant(var, null);
								var.addSucceedVariant(varsucc, null);
							}
						}
						// 3. 对于新加入的后继设计 前驱
						var.addPrecursorVariant(prevar, null);
					}
				}
			}
		}else{
			Set<Variant> precursors = curr.getPrecursorVariants(site);// 当前节点的前驱节点
			// 1. 对于所有的precursor来讲, 在所有的上加入新的 后继节点
			for(Variant prevar:precursors){
				for(Variant var:variants){
					// 2.  额外检查所有并行点的后继	
					if(var.getSucceedVariants(site).isEmpty()){
						prevar.addSucceedVariant(var, site);
					}else{
						Set<Variant>varSucceeds = var.getSucceedVariants(site);
						for(Variant varsucc:varSucceeds){
							prevar.addSucceedVariant(var, site);
							var.addSucceedVariant(varsucc, site);
						}
					}		
					// 3. 对于新加入的后继设计 前驱
					var.addPrecursorVariant(prevar, site);
				}
			}
		}
	}
	
	public List<Variant> getLastVariantInPath(){
		if(status==VariantStat.single){
			// 构造一个list并且返回这个list
			List<Variant>singleNodeList = new LinkedList<Variant>();
			singleNodeList.add(curr);
			return singleNodeList;
		}else{
			return currList;
		}
	}
	/**
	 * TODO
	 * 广度优先遍历
	 * @param caller
	 * @param callsite
	 */	
	public void layertraverse(SootMethod caller,CallSite callsite){
		Queue<Variant> queue = new LinkedList<Variant>();
		Queue<Variant> queuetemp = new LinkedList<Variant>();
		if(callsite==null){
			System.out.println("Inside caller method:"+caller.getName());
		}else{
			System.out.println("Inside callee method, call site is"+callsite.toString());
		}
		if(callsite==null){
			// 1. 加入entry节点 单一的 或多个的 
			if(head!=null && headSet.isEmpty()){
				queue.add(head);
			}else if(!headSet.isEmpty()){
				queue.addAll(headSet);
			}
			while(!queue.isEmpty()){
				// 2. 当队列不为空, 
				queuetemp.clear();
				while(!queue.isEmpty()){
					Variant varNode = queue.poll();
					System.out.println("Variant:"+varNode.getVariantId());
					Set<Variant> varNodeSuccs = varNode.getSucceedVariants(null);
					if(!varNodeSuccs.isEmpty())
						queuetemp.addAll(varNodeSuccs);
					for(Variant varsucc:varNodeSuccs){
						System.out.println("Link: "+varNode.getVariantId()+"\t to \t"+varsucc.getVariantId());
					}
				}
				if(!queuetemp.isEmpty())
					queue.addAll(queuetemp);
			}
		}else{
			// 1. 加入entry节点 单一的 或多个的 
			if(head!=null && headSet.isEmpty()){
				queue.add(head);
			}else if(!headSet.isEmpty()){
				queue.addAll(headSet);
			}
			while(!queue.isEmpty()){
				// 2. 当队列不为空, 
				queuetemp.clear();
				while(!queue.isEmpty()){
					Variant varNode = queue.poll();
					System.out.println("Variant:"+varNode.getVariantId());
					Set<Variant> varNodeSuccs = varNode.getSucceedVariants(callsite);
					if(!varNodeSuccs.isEmpty())
						queuetemp.addAll(varNodeSuccs);
					for(Variant varsucc:varNodeSuccs){
						System.out.println("Link: "+varNode.getVariantId()+"\t to \t"+varsucc.getVariantId());
					}
				}
				if(!queuetemp.isEmpty())
					queue.addAll(queuetemp);
			}
		}
	}
}
