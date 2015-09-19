package vreAnalyzer.Variants;
import java.util.LinkedList;
import java.util.List;

import vreAnalyzer.Blocks.CodeBlock;

public class StaticVariants {
	/**
	 * this type of variant is create and could be resolved statically, since it is 
	 * determined by branches statements like
	 * IfStmt, WhileStmt, SwitchStmt, ForStmt, GoStmt 
	 * It is decided by the conditional expression
	 */
	private CodeBlock variant;
	private BindingType type;
	private int rowinVariantTable = -1;
	private static List<StaticVariants> svariantpool = new LinkedList<StaticVariants>();
	public StaticVariants(CodeBlock block,BindingType type){		
		//{"Block ID","LOC","Features(IF)","Seperators"};
		this.variant = block;
		this.type = type;
	}
	public List<StaticVariants> getPool(){
		return svariantpool;
	}
	public static boolean poolContain(StaticVariants vi){
		return svariantpool.contains(vi);
	}
	public static void addToPool(StaticVariants vi){
		svariantpool.add(vi);
	}
	public void setRowInVariantTable(int row){
		rowinVariantTable = row;
	}
	public int getRowInVariantTable(){
		return rowinVariantTable;
	}
	public CodeBlock getBlock() {
		// TODO Auto-generated method stub
		return variant;
	}
}
