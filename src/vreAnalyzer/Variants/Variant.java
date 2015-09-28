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

	private List<Stmt>bindingStmts = null;
	private List<Value>paddingValues = null;
	private Map<Value,Stmt>ValueStartsBind = new HashMap<Value,Stmt>();
	public Variant(Value vi,List<Stmt>stmts){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		ValueStartsBind.put(vi, stmts.get(0));
		paddingValues.add(vi);
		bindingStmts.addAll(stmts);
		VariantColorMap.inst().registerNewColor(this);
	}
	public Variant(Value vi,Stmt stmt){
		paddingValues = new LinkedList<Value>();
		bindingStmts = new LinkedList<Stmt>();
		ValueStartsBind.put(vi, stmt);
		paddingValues.add(vi);
		bindingStmts.add(stmt);
		VariantColorMap.inst().registerNewColor(this);
	}
	public void addFirstBind(Value value,Stmt stmt){
		StmtTag stmtTag = (StmtTag) stmt.getTag(StmtTag.TAG_NAME);
		int stmtId = stmtTag.getIdxInMethod();
		if(ValueStartsBind.containsKey(value)){
			Stmt exist = ValueStartsBind.get(value);
			if(exist.equals(stmt)){
				return;
			}
			StmtTag existTag = (StmtTag) exist.getTag(StmtTag.TAG_NAME);
			int existId = existTag.getIdxInMethod();
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
	
	
}
