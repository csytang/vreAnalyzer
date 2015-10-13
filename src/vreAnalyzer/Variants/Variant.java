package vreAnalyzer.Variants;

import soot.Value;
import soot.jimple.Stmt;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Variant {

	private List<Stmt>bindingStmts = null;
	private List<Value>paddingValues = null;
	private Map<Value,Stmt>ValueStartsBind = new HashMap<Value,Stmt>();
	private Stmt startStmt = null;
	public Variant(Value vi,List<Stmt>stmts){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		ValueStartsBind.put(vi, stmts.get(0));
		paddingValues.add(vi);
		bindingStmts.addAll(stmts);
		startStmt = stmts.get(0);
	}
	public Variant(Value vi,Stmt stmt){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		ValueStartsBind.put(vi, stmt);
		paddingValues.add(vi);
		bindingStmts.add(stmt);
		startStmt = stmt;
	}
	public void addPaddingValue(List<Value>vis){
		this.paddingValues.addAll(vis);
	}
	public void addPaddingValue(Set<Value>vis){
		this.paddingValues.addAll(vis);
	}
	public void addPaddingValue(Value vi){
		this.paddingValues.add(vi);
	}
    public void addBindingStmts(Stmt stmt){
        this.bindingStmts.add(stmt);
    }
    public void addBindingStmts(List<Stmt>stmts){
        this.bindingStmts.addAll(stmts);
    }
	public List<Stmt> getBindingStmts(){return this.bindingStmts; }
	public List<Value> getPaddingValues() {return this.paddingValues; }
	public Stmt getFirstBindStmt(){return this.startStmt; }
	
}
