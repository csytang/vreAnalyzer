package vreAnalyzer.Variants;

import soot.Value;
import soot.jimple.Stmt;
import vreAnalyzer.Tag.StmtTag;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Variant {
	private static Map<Value,List<Stmt>>mapvlToStmt = new HashMap<Value,List<Stmt>>();

	private List<Stmt>bindingStmts = null;
	private List<Value>paddingValues = null;
	private static Map<Value,Stmt>ValueStartsBind = new HashMap<Value,Stmt>();
	public Variant(Value vi,List<Stmt>stmts){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		ValueStartsBind.put(vi, stmts.get(0));
		paddingValues.add(vi);
		bindingStmts.addAll(stmts);
	}
	public static void addFirstBind(Value value,Stmt stmt){
		
		if(ValueStartsBind.containsKey(value)){
			Stmt exist = ValueStartsBind.get(value);
			if(exist.equals(stmt)){
				return;
			}
			StmtTag existTag = (StmtTag) exist.getTag(StmtTag.TAG_NAME);
			StmtTag stmtTag = (StmtTag) stmt.getTag(StmtTag.TAG_NAME);
			int existId = existTag.getIdxInMethod();
			int stmtId = stmtTag.getIdxInMethod();
			if(stmtId<existId){
				ValueStartsBind.put(value, stmt);
			}
		}else
			ValueStartsBind.put(value, stmt);
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
