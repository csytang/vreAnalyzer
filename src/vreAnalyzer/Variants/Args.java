
package vreAnalyzer.Variants;

import java.util.LinkedList;
import java.util.List;

import soot.SootMethod;
import soot.Value;

public class Args {
	private List<Value>args = null;
	private SootMethod callee = null;
	private SootMethod caller = null;
	private List<Value>unbindargs = null;
	public Args(SootMethod caller,SootMethod callee,List<Value>args){
		this.callee = callee;
		this.caller = caller;
		this.args = args;
		unbindargs = new LinkedList<Value>();
	}
	public SootMethod getCallerMethod(){
		return this.caller;
	}
	public void addUnBindArgs(List<Value>unbind){
		unbindargs.addAll(unbind);
	}
	public void addUnBindArg(Value unbind){
		unbindargs.add(unbind);
	}
	public List<Value>getUnBindArgs(){
		return unbindargs;
	}
	public SootMethod getCalleeMethod(){
		return this.callee;
	}
	public List<Value> getArgs(){
		return this.args;
	}
}
