package vreAnalyzer.Elements;

import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;




public class CFGNodeSpecial extends NodeDefUses{
	private String name;
	
	public CFGNodeSpecial(String name,SootMethod sm){
		super(null,sm);
		this.name = name;
	}
	public String getIdStringInMethod() 
	{
		return name; 
	}
	public boolean isSpecial() 
	{ 
		return true; 
	}
	public String toString(){
		return name;
	}
}
