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
	private static List<StaticVariants> blockpool = new LinkedList<StaticVariants>();
	public StaticVariants(CodeBlock block){		
		//{"Block ID","LOC","Features(IF)","Seperators"};
		blockpool.add(this);
		
		
		
	}
}
