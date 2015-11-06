package vreAnalyzer.Variants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import soot.SootMethod;
import soot.jimple.Stmt;
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
    
	private Map<SootMethod,VariantPath> methodTovariantPath = new HashMap<SootMethod,VariantPath>();
	
	private boolean isFirstVariant = true;
	
	private Queue<Variant> panddingVariants = new LinkedList <Variant>();
	private Map<Variant,List<Stmt>> panddingVariantToUnProcessedStmt = new HashMap<Variant,List<Stmt>>();
	
	private boolean verbose = true;
	
	public static VariantPathAnalysis inst(){
		if(instance==null)
			instance = new VariantPathAnalysis();
		return instance;
	}
	public VariantPathAnalysis(){
		
	}
	
	public void parse(Map<SootMethod,List<Variant>> methodToVariants){
		// 1. 获得所有的Caller函数

		List<SootMethod> callerMethods = BindingResolver.inst().getCallerMethods();
		List<Variant> nodebindingVariants = new LinkedList<Variant>();
		VariantPath curr = null;
		for(SootMethod caller:callerMethods){
			if(verbose){
				System.out.println("\ncaller 函数中:"+caller.getName()+"\n");
			}
			List<Variant>bindingVariants = methodToVariants.get(caller);
			CFG cfg = ProgramFlowBuilder.inst().getCFG(caller);
			List<CFGNode> cfgNodes = cfg.getNodes();
			isFirstVariant = true;
			for(CFGNode node:cfgNodes){
				if(node.isSpecial()){
					continue;
				}
				if(verbose){
					System.out.println("当前分析语句:"+node.getStmt().toString());
				}
				nodebindingVariants.clear();
				nodebindingVariants = getBindingVaraints(bindingVariants,node,null);
				
				if(verbose){
					if(!nodebindingVariants.isEmpty()){
						String nodebindingVIds = "[";
						for(Variant variant:nodebindingVariants){
							nodebindingVIds += variant.getVariantId();
							nodebindingVIds += ",";
						}
						if(!nodebindingVariants.isEmpty()){
							nodebindingVIds = nodebindingVIds.substring(0, nodebindingVIds.length()-1);
						}
						nodebindingVIds+="]";
						System.out.println("当前语句中的Variant包括:"+nodebindingVIds);
					}else{
						System.out.println("当前语句不涉及任何Variant");
					}
					
				}
				if(!nodebindingVariants.isEmpty()){// 有variant涉及到这个语句
					if(isFirstVariant){// 如果是第一个涉及到Variant的语句
						// 创建一个VariantPath
						VariantPath variantPath = null;
						if(nodebindingVariants.size()==1){
							variantPath = new VariantPath(nodebindingVariants.get(0),caller);
							if(verbose){
								String variantIds = "[";
								for(Variant bindVariant:nodebindingVariants){
									variantIds+=bindVariant.getVariantId();
									variantIds+=",";
								}
								if(!nodebindingVariants.isEmpty()){
									variantIds = variantIds.substring(0, variantIds.length()-1);
								}
								variantIds += "]";
								System.out.println("创建一个VariantPath:"+variantIds+"\n");
							}
							
						}else{
							variantPath = new VariantPath(nodebindingVariants,caller);
							if(verbose){
								String variantIds = "[";
								for(Variant bindVariant:nodebindingVariants){
									variantIds+=bindVariant.getVariantId();
									variantIds+=",";
								}
								if(!nodebindingVariants.isEmpty()){
									variantIds = variantIds.substring(0, variantIds.length()-1);
								}
								variantIds += "]";
								System.out.println("创建一个VariantPath:"+variantIds+"\n");
							}
						}
						// 进入队列
						for(Variant variant:nodebindingVariants){
							panddingVariants.add(variant);
							List<Stmt>bindingStmts = variant.getBindingStmts(null);
							if(verbose){
								System.out.println("Variant(ID):"+variant.getVariantId()+"的绑定语句有:");
								System.out.println("------------------------------------");
								for(Stmt stmt:bindingStmts){
									System.out.println("语句:"+stmt);
								}
								System.out.println("------------------------------------");
								System.out.println("删除掉绑定语句:"+node.getStmt());
							}
							bindingStmts.remove(node.getStmt());
							panddingVariantToUnProcessedStmt.put(variant, bindingStmts);
						}
						// 将对应的加入到map中
						methodTovariantPath.put(caller,variantPath);
						curr = variantPath;
						isFirstVariant = false;
					}else if(!panddingVariants.isEmpty()){
						if(overlapList(panddingVariants,nodebindingVariants)){// 存在overlap 而不是全部包含关系
							for(Map.Entry<Variant, List<Stmt>>entry:panddingVariantToUnProcessedStmt.entrySet()){
								// 删除掉相关的 stmt
								if(nodebindingVariants.contains(entry.getKey())){
									List<Stmt> unsolvedStmts = entry.getValue();
									unsolvedStmts.remove(node.getStmt());
								}
							}
						}else{// 如果当前的Variant正在分析 未结束 并且获得了新的Variant
							// 1. 遍历所有在paddingVariants中的Variant
							/*
							 * 2. 如果有一个Variant的stmt已经全部经过 那么移除这个Variant
							 *    将这个Variant建立到下一个Variant的链接 使用前后链接link
							 */
							List<Variant>needToRemoveList = new LinkedList<Variant>();
							needToRemoveList.clear();
							for(Map.Entry<Variant, List<Stmt>>entry:panddingVariantToUnProcessedStmt.entrySet()){
								// 删除掉相关的 stmt
								Variant variant = entry.getKey();
								List<Stmt> unsolvedStmts = entry.getValue();
								if(unsolvedStmts.isEmpty()){
									panddingVariants.remove(variant);
									needToRemoveList.add(variant);
								}
							}
							if(!needToRemoveList.isEmpty()){
								for(Variant variant:needToRemoveList){
									for(Variant succeed:bindingVariants){
										//curr.linkNodes(variant, succeed, null);
										
										curr.addNextNode(succeed, null);
										if(verbose){
											// 将这个连接加入到VariantPath中
											System.out.println("VariantPath连接两个节点从:"+variant.getVariantId()+" 到 "+succeed.getVariantId());
										}
									}
									// 将这个Variant从 paddingVariantToProcessStmt中删除
									panddingVariantToUnProcessedStmt.remove(variant);
								}
							}
							
							// 3. 将这个在这个语句上绑定的Variant加入到stack中
							for(Variant needaddedVariant:nodebindingVariants){
								panddingVariants.add(needaddedVariant);
								List<Stmt>bindingstmts = needaddedVariant.getBindingStmts(null);
								bindingstmts.remove(node.getStmt());
								panddingVariantToUnProcessedStmt.put(needaddedVariant, bindingstmts);
							}
						}
					}	
				}	
			}
			
			if(!panddingVariants.isEmpty()){
				// 将里面所有内容并列加入
				List<Variant> paddingVariantList = new LinkedList<Variant>();
				paddingVariantList.addAll(panddingVariants);
				curr.addParallelNode(paddingVariantList, null);
			}
			if(verbose){
				// 遍历这个path VariantPath curr
				curr.layertraverse(caller,null);
			}
		}
		
		
		
		// 2. 获得所有的callsite
		List<SootMethod> calleeMethods = BindingResolver.inst().getCalleeMethods();
		
		Map<SootMethod,List<CallSite>> methodToCallSites = BindingResolver.inst().getMethodToCallSites();
		
		for(Map.Entry<SootMethod, List<CallSite>>entry:methodToCallSites.entrySet()){
			SootMethod callermethod = entry.getKey();
			List<CallSite> callsiteList = entry.getValue();
			
			// 所有的之后Variant连接都会在这个path上
			VariantPath callervariantPath =  methodTovariantPath.get(callermethod);
			
			for(CallSite callsite:callsiteList){
				List<SootMethod> callees = callsite.getAppCallees();
				List<SootMethod> shouldbeRemoved = new LinkedList<SootMethod>();
				for(SootMethod callee:callees){
					if(!calleeMethods.contains(callee)){
						shouldbeRemoved.add(callee);
					}
				}
				// 更新 删除不是appmethod的节点
				if(shouldbeRemoved!=null){
					callees.removeAll(shouldbeRemoved);
				}
				// 遍历所有的函数
				for(SootMethod callee:callees){
					// 1. 获得callee的cfg 
					CFG cfg = ProgramFlowBuilder.inst().getCFG(callee);
					List<CFGNode> cfgNodes = cfg.getNodes();
					isFirstVariant = true;
					List<Variant>bindingVariants = methodToVariants.get(callee);
					// 2. 遍历所有的节点
					for(CFGNode node:cfgNodes){
						if(node.isSpecial())
							continue;
						if(verbose){
							System.out.println("当前分析语句:"+node.getStmt().toString());
						}

						nodebindingVariants.clear();
						nodebindingVariants = getBindingVaraints(bindingVariants,node,callsite);
						
						if(verbose){
							if(!nodebindingVariants.isEmpty()){
								String nodebindingVIds = "[";
								for(Variant variant:nodebindingVariants){
									nodebindingVIds += variant.getVariantId();
									nodebindingVIds += ",";
								}
								if(!nodebindingVariants.isEmpty()){
									nodebindingVIds = nodebindingVIds.substring(0, nodebindingVIds.length()-1);
								}
								nodebindingVIds+="]";
								System.out.println("当前语句中的Variant包括:"+nodebindingVIds);
							}else{
								System.out.println("当前语句不涉及任何Variant");
							}
							
						}
						
						if(!nodebindingVariants.isEmpty()){// 有variant涉及到这个语句
							if(panddingVariants.isEmpty()){// 将当前的Variant及需要绑定的语句加入
								// 进入队列
								for(Variant variant:nodebindingVariants){
									panddingVariants.add(variant);
									List<Stmt>bindingStmts = variant.getBindingStmts(null);
									bindingStmts.remove(node.getStmt());
									panddingVariantToUnProcessedStmt.put(variant, bindingStmts);
								}
								curr = callervariantPath;
							}else{// paddingVariants不是空
								if(overlapList(panddingVariants,nodebindingVariants)){// 存在overlap 而不是全部包含关系
									for(Map.Entry<Variant, List<Stmt>>paddingentry:panddingVariantToUnProcessedStmt.entrySet()){
										// 删除掉相关的 stmt
										if(nodebindingVariants.contains(paddingentry.getKey())){
											List<Stmt> unsolvedStmts = paddingentry.getValue();
											unsolvedStmts.remove(node.getStmt());
										}
									}
								}else{// 如果当前的Variant正在分析 未结束 并且获得了新的Variant
									// 1. 遍历所有在paddingVariants中的Variant
									/*
									 * 2. 如果有一个Variant的stmt已经全部经过 那么移除这个Variant
									 *    将这个Variant建立到下一个Variant的链接 使用前后链接link
									 */
									List<Variant>needToRemoveList = new LinkedList<Variant>();
									needToRemoveList.clear();
									for(Map.Entry<Variant, List<Stmt>>unprocessedentry:panddingVariantToUnProcessedStmt.entrySet()){
										// 删除掉相关的 stmt
										Variant variant = unprocessedentry.getKey();
										List<Stmt> unsolvedStmts = unprocessedentry.getValue();
										if(unsolvedStmts.isEmpty()){
											panddingVariants.remove(variant);
											needToRemoveList.add(variant);
										}
									}
									if(!needToRemoveList.isEmpty()){
										for(Variant variant:needToRemoveList){
											for(Variant succeed:bindingVariants){
												curr.addNextNode(succeed, callsite);
												if(verbose){
													System.out.println("caller:"+callermethod.getName()+"的callee:"
													+callee+"中连接variant从"+variant.getVariantId()+"到"+succeed.getVariantId());
												}
											}
											// 将这个Variant从 paddingVariantToProcessStmt中删除
											panddingVariantToUnProcessedStmt.remove(variant);
										}
									}
									
									// 3. 将这个在这个语句上绑定的Variant加入到stack中
									for(Variant needaddedVariant:nodebindingVariants){
										panddingVariants.add(needaddedVariant);
										List<Stmt>bindingstmts = needaddedVariant.getBindingStmts(null);
										bindingstmts.remove(node.getStmt());
										panddingVariantToUnProcessedStmt.put(needaddedVariant, bindingstmts);
									}
								}
							}
						}
					}
					// 确认检查 
					if(!panddingVariants.isEmpty()){
						// 将里面所有内容并列加入
						List<Variant>paddingVariantList = new LinkedList<Variant>();
						paddingVariantList.addAll(panddingVariants);
						curr.addParallelNode(paddingVariantList, callsite);
					}
					
					if(verbose){
						// 遍历这个path
						curr.layertraverse(callermethod,callsite);
					}
					
				}
			}			
		}
		
	}
	
	public List<Variant> getBindingVaraints(List<Variant> variantlist,CFGNode cfgNode,CallSite callsite){
		List<Variant> bindingVariants = new LinkedList<Variant>();
		if(callsite==null){
			for(Variant variant:variantlist){
				if(variant.getBindingStmts(null).contains(cfgNode.getStmt())){
					bindingVariants.add(variant);
				}
			}
		}else{
			for(Variant variant:variantlist){
				if(variant.getBindingStmts(callsite).contains(cfgNode.getStmt())){
					bindingVariants.add(variant);
				}
			}
		}
		return bindingVariants;
	}
	
	// list的overlap
	public boolean overlapList(Queue<Variant> queueVariants, List<Variant> listVariants){
		// 是否有覆盖
		List<Variant> queueToList = new LinkedList<Variant>(queueVariants);
		List<Variant> intersection = getIntersection(queueToList,listVariants);
		if(intersection.isEmpty()){
			return false;
		}else{
			return true;
		}
	}
	
	// 获得两个列表的交集
	public <T> List<T> getIntersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();
        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }
	
}
