package vreAnalyzer.Variants;

import soot.Value;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Variant {
	private static Map<Value,List<Stmt>>mapvlToStmt = new HashMap<Value,List<Stmt>>();

	private List<Stmt>bindingStmts = null;
	private List<Value>paddingValues = null;
	public Variant(Value vi,List<Stmt>stmts){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		paddingValues.add(vi);
		bindingStmts.addAll(stmts);

	}
	public Variant(List<Value>vis,List<Stmt>stmts){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		paddingValues.addAll(vis);
		bindingStmts.addAll(stmts);

	}
	public void addPaddingValue(List<Value>vis){
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
	public static void addVarStmtMap(Value vi,List<Stmt>stmts){
		if(mapvlToStmt.containsKey(vi)){
			mapvlToStmt.get(vi).addAll(stmts);
		}else{
			mapvlToStmt.put(vi, stmts);
		}
	}
	public static void addVarStmtMap(Value vi,Stmt stmt){
		if(mapvlToStmt.containsKey(vi)){
			mapvlToStmt.get(vi).add(stmt);
		}else{
			List<Stmt>paddingStmts = new LinkedList<Stmt>();
			paddingStmts.add(stmt);
			mapvlToStmt.put(vi, paddingStmts);
		}
	}
	public static List<Stmt>getVarStmt(Value vi){
		if(mapvlToStmt.containsKey(vi)){
			return mapvlToStmt.get(vi);
		}else
			return null;
	}
    public static Map<Value,List<Stmt>>getMapvlToStmt(){
        return mapvlToStmt;
    }

	public static void removeValue(Value vi) {
		if(mapvlToStmt.containsKey(vi))
			mapvlToStmt.remove(vi);
	}
}
