package vreAnalyzer.Blocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import soot.Modifier;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.UI.MainFrame;

public class BlockGenerator {
	public static BlockGenerator instance;
	private static int blockid = 0;
	private static ArrayList<CodeBlock> blockpool = new ArrayList<CodeBlock>();
	// sootmethod 到 codeblock集合
	private static Map<SootMethod,Set<SimpleBlock>> simpleCodeBlockpool = new HashMap<SootMethod,Set<SimpleBlock>>();
	private static Map<SootClass,ClassBlock> classBlockpool = new HashMap<SootClass,ClassBlock>();
	private static Map<SootMethod,MethodBlock> methodBlockpool = new HashMap<SootMethod,MethodBlock>();
	
	private DefaultTableModel blockmodel;
	
	public static BlockGenerator inst(List<SootClass> appClasses){
		if(instance==null)
			instance = new BlockGenerator(appClasses);
		return instance;
	}

	public static BlockGenerator inst(){
		return instance;
	}
	
	public BlockGenerator(List<SootClass> appClasses){
		
		JTable blocktable = MainFrame.inst().getBlockTable();
		blockmodel = (DefaultTableModel)blocktable.getModel();
		
		
		for(SootClass cls:appClasses){
			
			List<SootMethod> clsmethods = cls.getMethods();
			List<CFGNode> classallnode = new LinkedList<CFGNode>();
			List<CFGNode> marked = new LinkedList<CFGNode>();
			for (Iterator<SootMethod> itMthd = cls.getMethods().iterator(); itMthd.hasNext();) {
				SootMethod m = itMthd.next();
				if (!m.isAbstract() && m.toString().indexOf(": java.lang.Class class$") == -1){
					clsmethods.add(m);
					CFG cfg = ProgramFlowBuilder.inst().getCFG(m);
					List<CFGNode>nodes = cfg.getNodes();
					classallnode.addAll(nodes);
				}
			}
			// 2. Create class block
			ClassBlock clsblock = ClassBlock.tryToCreate(classallnode, cls);
			if(classBlockpool.containsKey(cls)){
				continue;
			}else{
				classBlockpool.put(cls, clsblock);
			}
			
			
			addNewBlockToPool(clsblock,true);
			for(SootMethod method:clsmethods){
				if(method.getTag(MethodTag.TAG_NAME)!=null && !Modifier.isVolatile(method.getModifiers())){
					marked.clear();
					// 2.1. Create method block
					CFG cfg = ProgramFlowBuilder.inst().getCFG(method);
					List<CFGNode>nodes = cfg.getNodes();
					MethodBlock methodBlock = MethodBlock.tryToCreate(nodes, method,clsblock.getBlockId());
					addNewBlockToPool(methodBlock,true);
					if(methodBlockpool.containsKey(method)){
						continue;
					}else{
						methodBlockpool.put(method, methodBlock);
					}
					@SuppressWarnings("unused")
					MethodTag mTag = (MethodTag) method.getTag(MethodTag.TAG_NAME);
					
					// 2.2. Create inside method blocks
					/**
					 * Strategy:
					 * 1. go through the control flow, find the branch, create separate block for each branch
					 * 2. //"Block ID","Type","Method(IF)","Class" line numbers
					 */
					if(cfg.containsBraches()){
						CFGNode entry = cfg.ENTRY;
						List<CFGNode>temp = new LinkedList<CFGNode>();
						Stack<CFGNode>analysisstack = new Stack<CFGNode>();
						analysisstack.push(entry);
						
						//DFS
						while(!analysisstack.isEmpty()){
							CFGNode curr = analysisstack.pop();
							if(marked.contains(curr)&&!temp.isEmpty()){
								SimpleBlock sblock = SimpleBlock.tryToCreate(temp, method,methodBlock.getBlockId());
								if(simpleCodeBlockpool.containsKey(method)){
									simpleCodeBlockpool.get(method).add(sblock);
								}else{
									Set<SimpleBlock>blockset = new HashSet<SimpleBlock>();
									blockset.add(sblock);
									simpleCodeBlockpool.put(method, blockset);
								}
								addNewBlockToPool(sblock,true);// sub block
								// 1. add a new static variant
								temp.clear();
								continue;
							}else if(marked.contains(curr)){
								continue;
							}
							temp.add(curr);
							for(CFGNode next:curr.getSuccs()){
								analysisstack.push(next);
							}
							if(curr.getSuccs().size()>1&&!temp.isEmpty()){
								SimpleBlock sblock = SimpleBlock.tryToCreate(temp, method,methodBlock.getBlockId());
								if(simpleCodeBlockpool.containsKey(method)){
									simpleCodeBlockpool.get(method).add(sblock);
								}else{
									Set<SimpleBlock>blockset = new HashSet<SimpleBlock>();
									blockset.add(sblock);
									simpleCodeBlockpool.put(method, blockset);
								}
								addNewBlockToPool(sblock,true);// prior block
								temp.clear();
							}
							marked.add(curr);
						}
						
						if(!temp.isEmpty()){
							SimpleBlock sblock = SimpleBlock.tryToCreate(temp, method,methodBlock.getBlockId());
							if(simpleCodeBlockpool.containsKey(method)){
								simpleCodeBlockpool.get(method).add(sblock);
							}else{
								Set<SimpleBlock>blockset = new HashSet<SimpleBlock>();
								blockset.add(sblock);
								simpleCodeBlockpool.put(method, blockset);
							}
							addNewBlockToPool(sblock,true);
							temp.clear();
						}
					}
				}
			}
		}
	}
	
	
	public static void increaseId(){
		blockid++;
	}
	public static int getBlockId(){
		return blockid;
	}
	public List<CodeBlock> getblockPool(){
		return blockpool;
	}
	
	public void addNewBlockToPool(CodeBlock block,boolean original){
		//2. //"Block ID","Type","Method(IF)","Class"
		if(!blockpool.contains(block)){
			if(block.getType()==BlockType.Class){
				if(original){
					blockmodel.addRow(new Object[]{block.getBlockId(),"-",block.getType(),"-",block.getSootClass().getName(),"-","Y",""});
					// 将block内容加入到文件中
					BlockToFile.inst().writeRow(block.getBlockId()+"", "-", block.getType(), "-", block.getSootClass().getName(), "-","Y", "");
				}else{
					blockmodel.addRow(new Object[]{block.getBlockId(),"-",block.getType(),"-",block.getSootClass().getName(),"-","N",""});
					// 将block内容加入到文件中
					BlockToFile.inst().writeRow(block.getBlockId()+"","-",block.getType(),"-",block.getSootClass().getName(),"-","N","");
				}
			}
			else{
				if(original){
					blockmodel.addRow(new Object[]{block.getBlockId(),block.getCodeRange(),block.getType(),block.getSootMethod().getName(),block.getSootClass().getName(),block.getParentId(),"Y",""});
					// 将block内容加入到文件中
					BlockToFile.inst().writeRow(block.getBlockId()+"", block.getCodeRange(), block.getType(),
							block.getSootMethod().getName(),block.getSootClass().getName(),block.getParentId()+"","Y","");
				}else{
					blockmodel.addRow(new Object[]{block.getBlockId(),block.getCodeRange(),block.getType(),block.getSootMethod().getName(),block.getSootClass().getName(),block.getParentId(),"N",""});
					// 将block中内容加入到文件中
					BlockToFile.inst().writeRow(block.getBlockId()+"", block.getCodeRange(), block.getType(), block.getSootMethod().getName(), block.getSootClass().getName(), block.getParentId()+"", "N", "");				
				}
			}
			blockpool.add(block);
		}
	}

	public Map<SootMethod, Set<SimpleBlock>> getSimpleCodeBlockMap() {
		return simpleCodeBlockpool;
	}
	public Map<SootClass,ClassBlock> getClassCodeBlockMap(){
		return classBlockpool;
	}
	public Map<SootMethod,MethodBlock> getMethodBlockMap(){
		return methodBlockpool;
	}
}
