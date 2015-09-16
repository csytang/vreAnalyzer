package vreAnalyzer.Blocks;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import Patch.Hadoop.ReuseAssets.AssetType;
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
			for(SootMethod method:clsmethods){
				if(method.getTag(MethodTag.TAG_NAME)!=null){
					// 1. Create method block
					CFG cfg = ProgramFlowBuilder.inst().getCFG(method);
					List<CFGNode>nodes = cfg.getNodes();
					MethodBlock.tryToCreate(nodes, method);
					classallnode.addAll(nodes);
					// 2. Create inside method blocks
					/**
					 * Strategy:
					 * 1. go through the control flow, find the branch, create separate block for each branch
					 * 
					 * 2. //"Block ID","LOC","Type","Method(IF)","Class"
					 */
					CFGNode entry = cfg.ENTRY;
					List<CFGNode>temp = new LinkedList<CFGNode>();
					Stack<CFGNode>analysisstack = new Stack<CFGNode>();
					analysisstack.push(entry);
					while(!analysisstack.isEmpty()){
						CFGNode curr = analysisstack.pop();
						temp.add(curr);
						for(CFGNode next:curr.getSuccs()){
							analysisstack.push(next);
						}
						if(curr.getSuccs().size()>1){
							SimpleBlock.tryToCreate(temp, method);
							temp.clear();
						}else if(curr.getSuccs().size()==1){
							temp.add(curr.getSuccs().get(0));
						}
					}
				}
			}
			// 2. Create class block
			ClassBlock.tryToCreate(classallnode, cls);
		}
	}
	public void increaseId(){
		blockid++;
	}
	public int getBlockId(){
		return blockid;
	}
	public void addNewBlockToPool(CodeBlock block){
		//2. //"Block ID","Type","Method(IF)","Class"
		if(block.getType()==AssetType.Class)
			blockmodel.addRow(new Object[]{block.getBlockId(),block.getType(),"-",block.getSootClass().getName()});
		else
			blockmodel.addRow(new Object[]{block.getBlockId(),block.getType(),block.getSootMethod().getName(),block.getSootClass().getName()});
		blockpool.add(block);
	}
}
