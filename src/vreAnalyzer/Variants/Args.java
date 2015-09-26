
package vreAnalyzer.Variants;

import java.util.List;

import soot.SootMethod;
import soot.Value;

public class Args {
	private List<Value>args = null;
	private SootMethod callee = null;
	private SootMethod caller = null;
	public Args(SootMethod caller,SootMethod callee,List<Value>args){
		this.callee = callee;
		this.caller = caller;
		this.args = args;
	}
	public SootMethod getCallerMethod(){
		return this.caller;
	}
	public SootMethod getCalleeMethod(){
		return this.callee;
	}
	public List<Value> getArgs(){
		return this.args;
	}
}
