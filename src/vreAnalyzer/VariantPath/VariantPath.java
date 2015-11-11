package vreAnalyzer.VariantPath;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import soot.SootMethod;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.Util.Graphviz.GraphvizController;
import vreAnalyzer.Variants.Variant;

public class VariantPath {
	/*
	 * 检查全部的current set
	 */
	enum VariantStat{single, branch};
	// 路径中当前节点
	private Variant curr = null;// 当前路径的最后一个单一节点
	private Set<Variant> currSet = new HashSet<Variant>();// 当前路径的最后一个多节点集合
	
	// 开始节点
	private Variant head = null;
	private Set<Variant> headSet = new HashSet<Variant>();
	
	// 当前路径状态
	private VariantStat status = null;
	
	private SootMethod callerMethod = null;
	// 路径表示ID
	private int pathId = -1;
	
	// 创建一个GraphvizController 
	private GraphvizController graphvizController = null;
	private Set<Variant> fullVariantSet = new HashSet<Variant>();
	
	private File imageFile = null;
	
	public VariantPath(Variant headVariant,SootMethod caller,int id){
		head = headVariant;
		callerMethod = caller;
		curr = headVariant;
		status = VariantStat.single;
		pathId = id;
		fullVariantSet.add(headVariant);
	}
	
	public VariantPath(List<Variant>headVariants, SootMethod caller,int id){
		headSet.addAll(headVariants);
		callerMethod = caller;
		currSet.addAll(headVariants);
		status = VariantStat.branch;
		pathId = id;
		fullVariantSet.addAll(headVariants);
	}
	
	public void addNextNode(Variant variant,CallSite site){
		if(site==null){
			if(status==VariantStat.single){// 前驱节点为单一节点
				curr.addSucceedVariant(variant, null);
				variant.addPrecursorVariant(curr, null);
				curr = variant;
				status = VariantStat.single;
			}else{
				for(Variant var:currSet){
					var.addSucceedVariant(variant, null);
					variant.addPrecursorVariant(var, null);
				}
				currSet.clear();
				curr = variant;
				status = VariantStat.single;
			}
		}else{
			if(status==VariantStat.single){
				curr.addSucceedVariant(variant, site);
				variant.addPrecursorVariant(curr, null);
				curr = variant;
				status = VariantStat.single;
			}else{
				for(Variant var:currSet){
					var.addSucceedVariant(variant, site);
					variant.addPrecursorVariant(var, site);
				}
				currSet.clear();
				curr = variant;
				status = VariantStat.single;
			}
		}
		fullVariantSet.add(variant);
	}
	
	public void addNextNode(List<Variant>variants,CallSite site){
		if(site==null){
			if(status==VariantStat.single){
				for(Variant var:variants){
					curr.addSucceedVariant(var, null);
					var.addPrecursorVariant(curr, null);
				}
				curr = null;
				status = VariantStat.branch;
				currSet.clear();
				currSet.addAll(variants);
			}else{
				for(Variant pre:currSet){
					for(Variant var:variants){
						pre.addSucceedVariant(var, null);
						var.addPrecursorVariant(pre, null);
					}
				}
				status = VariantStat.branch;
				currSet.clear();
				currSet.addAll(variants);
			}
		}else{
			if(status==VariantStat.single){
				for(Variant var:variants){
					curr.addSucceedVariant(var, site);
					var.addPrecursorVariant(curr, site);
				}
				curr = null;
				status = VariantStat.branch;
				currSet.clear();
				currSet.addAll(variants);
			}else{
				for(Variant pre:currSet){
					for(Variant var:variants){
						pre.addSucceedVariant(var, site);
						var.addPrecursorVariant(pre, site);
					}
				}
				status = VariantStat.branch;
				currSet.clear();
				currSet.addAll(variants);
			}
		}
		fullVariantSet.addAll(variants);
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
					currSet.clear();
					currSet.addAll(headSet);
					status = VariantStat.branch;
				}else if(!headSet.isEmpty()){
					headSet.add(variant);
					currSet.clear();
					currSet.addAll(headSet);
					status = VariantStat.branch;
				}
			}else{
				// 不需要改变currentSet
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
		fullVariantSet.add(variant);
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
					currSet.clear();
					currSet.addAll(headSet);
					status = VariantStat.branch;
				}else if(!headSet.isEmpty()){
					headSet.addAll(variants);
					currSet.clear();
					currSet.addAll(headSet);
					status = VariantStat.branch;
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
		fullVariantSet.addAll(variants);
	}
	
	public Set<Variant> getLastVariantInPath(){
		if(status==VariantStat.single){
			// 构造一个list并且返回这个list
			Set<Variant>singleNodeSet = new HashSet<Variant>();
			singleNodeSet.add(curr);
			return singleNodeSet;
		}else{
			return currSet;
		}
	}
		
	public void layertraverse(SootMethod caller,CallSite callsite){
		// 创建GraphvizController
		graphvizController = new GraphvizController();
		graphvizController.startScript();
		graphvizController.addLine("startNode [shape=box];");
		graphvizController.addLine("endNode [shape=box];");
		
		System.out.println("----------------------------");
		System.out.println("Currently process the path:"+pathId);
		int layer = 1;// 显示层数
		Set<Variant> analyzedPool = new HashSet<Variant>();//分析过的放入此集合中
		Queue<Variant> queue = new LinkedList<Variant>();
		Queue<Variant> queuetemp = new LinkedList<Variant>();
		List<Variant> lastLayer = new LinkedList<Variant>();// 最后一层的内容
		
		if(callsite==null){
			System.out.println("Inside caller method:"+caller.getName());
		}else{
			System.out.println("Inside callee method, call site is"+callsite.toString());
		}
		
		if(callsite==null){
			analyzedPool.clear();
			lastLayer.clear();
			// 1. 加入entry节点 单一的 或多个的 
			if(head!=null && headSet.isEmpty()){
				queue.add(head);
				
			}else if(!headSet.isEmpty()){
				queue.addAll(headSet);
				
			}
			while(!queue.isEmpty()){
				// 2. 当队列不为空, 
				queuetemp.clear();
				System.out.println("\nIn layer:"+layer);
				lastLayer.clear();
				
				while(!queue.isEmpty()){
					Variant varNode = queue.poll();
					lastLayer.add(varNode);
					
					if(layer==1){
						graphvizController.addLine("startNode->"+varNode.getVariantId()+";");
					}
					analyzedPool.add(varNode);
					System.out.println("Variant:"+varNode.getVariantId());
					Set<Variant> varNodeSuccs = varNode.getSucceedVariants(null);
					if(!varNodeSuccs.isEmpty()){
						for(Variant var:varNodeSuccs){
							if(!analyzedPool.contains(var)){
								queuetemp.add(var);
							}
						}
					}
					for(Variant varsucc:varNodeSuccs){
						System.out.println("Link: "+varNode.getVariantId()+"\t to \t"+varsucc.getVariantId());
						graphvizController.addLine(varNode.getVariantId()+"->"+varsucc.getVariantId()+";");
					}
				}
				
				if(!queuetemp.isEmpty()){
					queue.addAll(queuetemp);
				}
				layer++;
			}
		}else{
			analyzedPool.clear();
			lastLayer.clear();
			
			// 1. 加入entry节点 单一的 或多个的 
			if(head!=null && headSet.isEmpty()){
				queue.add(head);
			}else if(!headSet.isEmpty()){
				queue.addAll(headSet);
			}
			while(!queue.isEmpty()){
				// 2. 当队列不为空, 
				queuetemp.clear();
				lastLayer.clear();
				
				System.out.println("\nIn layer:"+layer);
				while(!queue.isEmpty()){
					Variant varNode = queue.poll();
					if(layer==1){
						graphvizController.addLine("startNode->"+varNode.getVariantId()+";");
					}
					analyzedPool.add(varNode);
					lastLayer.add(varNode);
					System.out.println("Variant:"+varNode.getVariantId());
					Set<Variant> varNodeSuccs = varNode.getSucceedVariants(callsite);
					if(!varNodeSuccs.isEmpty())
						queuetemp.addAll(varNodeSuccs);
					for(Variant varsucc:varNodeSuccs){
						if(!analyzedPool.contains(varsucc)){
							queuetemp.add(varsucc);
						}
						System.out.println("Link: "+varNode.getVariantId()+"\t to \t"+varsucc.getVariantId());
						graphvizController.addLine(varNode.getVariantId()+"->"+varsucc.getVariantId()+";");
					}
				}
				if(!queuetemp.isEmpty())
					queue.addAll(queuetemp);
				layer++;
			}
		}
		for(Variant lastLayervariant:lastLayer){
			graphvizController.addLine(lastLayervariant.getVariantId()+"->endNode;");
		}
		graphvizController.endScript();
		// 输出路径
		graphvizController.drawGraph(".", pathId+"");
		
		imageFile = graphvizController.getOutputFile();
	}

	public void addToTable(){
		VariantPathToTable vtTable = new VariantPathToTable();
		vtTable.addARowToTable(pathId, fullVariantSet);
	}
	
	public GraphvizController getgraphvizController(){
		return graphvizController;
	}
	
	public Set<Variant> getfullVariants(){
		return fullVariantSet;
	}
	
	public int getPathId(){
		return pathId;
	}
	
	public File getImageFile(){
		return imageFile;
	}
}
