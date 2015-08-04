package vreAnalyzer.ControlFlowGraph.DefUse.Variable;

import soot.Local;
import soot.RefType;
import soot.Type;
import soot.Value;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Util.Util;

public class ObjVariable extends Variable {
	/** Might appear counter-intuitive to the notion of location-less var,
	 *  but node actually allows to distinguish different vars (e.g., string constants). */
	protected final CFGNode n;
	public ObjVariable(Value valD, CFGNode nD) { super(valD); this.n = nD; }
	/** Returns true only if objects MUST NECESSARILY be equal. */
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof ObjVariable))
			return false;
//		// uses EXACT set match of possible runtime types (needed for transitivity of 'equals')
		Value valOther = ((ObjVariable)obj).val;
		// TODO: make consistent with 'may' object equality check
		return Util.objValuesMustEqual(val, valOther, n == ((ObjVariable)obj).n);
	}
	@Override
	public int hashCode() { return Util.objValueHashCode(val); }
	@Override
	public String toString() {
		final boolean instance = !(val instanceof StaticInvokeExpr);
		Type objType; // can be RefType or ArrayType
		if (val instanceof NewExpr)
			objType = ((NewExpr)val).getType();
		else if (val instanceof NewArrayExpr)
			objType = ((NewArrayExpr)val).getType(); // provides array type with type of element inside
		else if (val instanceof InvokeExpr)
			objType = ((InvokeExpr)val).getMethod().getDeclaringClass().getType();
		else if (val instanceof Local || val instanceof StaticFieldRef)
			objType = val.getType();
		else {
			assert val instanceof StringConstant || val instanceof ClassConstant;
			objType = (RefType)val.getType(); //Scene.v().getSootClass("java.lang.String").getType();
		}
		
		return "O" + (instance? "I" : "C") + objType;
	}
	
	@Override public boolean isConstant() { return false; }
	@Override public boolean isLocal() { return false; }
	@Override public boolean isLocalOrConst() { return false; }
	@Override public boolean isFieldRef() { return false; }
	@Override public boolean isArrayRef() { return false; }
	@Override public boolean isObject() { return true; }
	@Override public boolean isStrConstObj() { return val instanceof StringConstant; }
	@Override public boolean isLibCallObj() { return val instanceof InvokeExpr; }
	@Override public boolean isReturnedVar() { return false; }
	
	/** Whether vars may "equal" (as vars, regardless of possible non-aliasing). */
	@Override public boolean mayEqual(Variable vOther) {
		if (!(vOther instanceof ObjVariable))
			return false;
		// TODO: make consistent with 'must' object equality check
		return Util.objValuesMayEqual(val, vOther.getValue());
	}
}
