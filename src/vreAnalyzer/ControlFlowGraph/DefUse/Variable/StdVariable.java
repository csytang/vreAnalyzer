package vreAnalyzer.ControlFlowGraph.DefUse.Variable;

import soot.Local;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import vreAnalyzer.Util.Util;

/** Represents all variables other than (lib) objects. */
public class StdVariable extends Variable{
	public StdVariable(Value val) { super(val); } //assert isLocalOrConst() || isFieldRef() || isArrayRef(); }
	/** Returns true only if objects MUST NECESSARILY be equal. */
	@Override public boolean equals(Object obj) {
		if (!(obj.getClass() == StdVariable.class)) return false;
		return equalsImpl((StdVariable)obj);
	}
	protected boolean equalsImpl(StdVariable vOther) { return Util.valuesEqual(val, vOther.val, true); }
	@Override
	public int hashCode() { return Util.valueHashCode(val); }
	@Override
	public String toString() {
		if (val instanceof Local) return val.toString();
		if (val instanceof FieldRef) { return "F" + ((FieldRef) val).getField().getName(); }
		
		assert val instanceof ArrayRef;
		
		ArrayRef a = (ArrayRef) val;
		Value vIdx = a.getIndex();
		String arrType = a.getBase().getType().toString();
		arrType = arrType.replace('/', '.').replace("[]", "");
		return "A" + arrType + ((vIdx instanceof Constant)? "I" + vIdx : "");
	}
	@Override public boolean isConstant() { return val instanceof Constant; }
	@Override public boolean isLocal() { return val instanceof Local; }
	@Override public boolean isLocalOrConst() { return isLocal() || isConstant(); }
	@Override public boolean isFieldRef() { return val instanceof FieldRef; }
	@Override public boolean isArrayRef() { return val instanceof ArrayRef; }
	@Override public boolean isObject() { return false; }
	@Override public boolean isStrConstObj() { return false; }
	@Override public boolean isLibCallObj() { return false; }
	@Override public boolean isReturnedVar() { return false; }
	
	@Override public boolean isDefinite() { return Util.isValueDefinite(val); }
	
	/** Whether vars may "equal" (as vars, regardless of possible non-aliasing). */
	@Override public boolean mayEqual(Variable vOther) {
		if (!(vOther instanceof StdVariable)) return false;
		return Util.valuesEqual(val, ((StdVariable)vOther).val, false);
	}
}
