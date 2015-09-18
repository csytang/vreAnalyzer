package vreAnalyzer.Blocks;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
import vreAnalyzer.Variants.StaticVariants;

public class BlockGenerator {
	public static BlockGenerator instance;
	private static int blockid = 0;
	private static ArrayList<CodeBlock> blockpool = new ArrayList<CodeBlock>();
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
			
			List<SootMethod>clsmethods = cls.getMethods();
			List<CFGNode>classallnode = new LinkedList<CFGNode>();
			List<CFGNode>marked = new LinkedList<CFGNode>();
			for(SootMethod method:clsmethods){
				CFG cfg = ProgramFlowBuilder.inst().getCFG(method);
				List<CFGNode>nodes = cfg.getNodes();
				classallnode.addAll(nodes);
			}
			// 2. Create class block
			ClassBlock clsblock = ClassBlock.tryToCreate(classallnode, cls);
			addNewBlockToPool(clsblock,true);
			for(SootMethod method:clsmethods){
				if(method.getTag(MethodTag.TAG_NAME)!=null&&
						!Modifier.isVolatile(method.getModifiers())){
					marked.clear();
					// 1. Create method block
					CFG cfg = ProgramFlowBuilder.inst().getCFG(method);
					List<CFGNode>nodes = cfg.getNodes();
					MethodBlock methodBlock = MethodBlock.tryToCreate(nodes, method,clsblock.getBlockId());
					addNewBlockToPool(methodBlock,true);
					classallnode.addAll(nodes);
					// 2. Create inside method blocks
					/**
					 * Strategy:
					 * 1. go through the control flow, find the branch, create separate block for each branch
					 * 
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
								addNewBlockToPool(sblock,true);// sub block
								
								// 1. add a new static variant
								StaticVariants stvariant = new StaticVariants(sblock);
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
								addNewBlockToPool(SimpleBlock.tryToCreate(temp, method,methodBlock.getBlockId()),true);// prior block
								temp.clear();
							}
							marked.add(curr);
						}
						
						if(!temp.isEmpty()){
							addNewBlockToPool(SimpleBlock.tryToCreate(temp, method,methodBlock.getBlockId()),true);
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
	public void addNewBlockToPool(CodeBlock block,boolean original){
		//2. //"Block ID","Type","Method(IF)","Class"
		if(!blockpool.contains(block)){
			if(block.getType()==BlockType.Class){
				if(original)
					blockmodel.addRow(new Object[]{block.getBlockId(),"Nil",block.getType(),"Nil",block.getSootClass().getName(),"-","Y"});
				else
					blockmodel.addRow(new Object[]{block.getBlockId(),"Nil",block.getType(),"Nil",block.getSootClass().getName(),"-","N"});
			}
			else{
				if(original)
					blockmodel.addRow(new Object[]{block.getBlockId(),block.getCodeRange(),block.getType(),block.getSootMethod().getName(),block.getSootClass().getName(),block.getParentId(),"Y"});
				else
					blockmodel.addRow(new Object[]{block.getBlockId(),block.getCodeRange(),block.getType(),block.getSootMethod().getName(),block.getSootClass().getName(),block.getParentId(),"N"});
			}
			blockpool.add(block);
		}
	}
}
