package vreAnalyzer.Variants;

import soot.Value;
import soot.jimple.Stmt;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Variant {
	private static Map<Value,List<Stmt>>mapvlToStmt = new HashMap<Value,List<Stmt>>();
	public Variant(){
		
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

	public static void removeValue(Value vi) {
		if(mapvlToStmt.containsKey(vi))
			mapvlToStmt.remove(vi);
	}
}
