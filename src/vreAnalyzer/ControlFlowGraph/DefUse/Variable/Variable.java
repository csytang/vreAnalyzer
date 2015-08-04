package vreAnalyzer.ControlFlowGraph.DefUse.Variable;

import soot.Value;
import vreAnalyzer.Util.Util;

/** Wrapper to provide equals and hashCode for regular vars (i.e., locals, fields, array elems, and lib objects) */
public abstract class Variable {
	/** Constant for constant call argument; Local for local var; FieldRef for field (base is ignored); ArrayRef for
	 *  array-element (base is ignored); InvokeExpr for lib object (instance or static/class) (base is ignored). */
	protected final Value val; // value
	public Value getValue() { return val; }
	public Variable(Value val) { this.val = val; }
	
	/** Since in many cases it's blurry to declare two variables equal or not, this equals method returns true only if there the objects are *certainly* equal.
	 *  Thus, it might return false for pairs of variables that are actually equal (in some sense). This behavior is safe for using Variables in containers.
	 *  However, to find out safely if two variables *may* be equal, use {@link #mayEqual(Variable)} or {@link #mayEqualAndAlias(Variable)(Variable)} instead. */
	@Override public abstract boolean equals(Object obj);
	public abstract boolean isConstant();
	public abstract boolean isLocal();
	public abstract boolean isLocalOrConst();
	public abstract boolean isFieldRef();
	public abstract boolean isArrayRef();
	public abstract boolean isObject();
	public abstract boolean isStrConstObj();
	/** Whether it's the object indirectly referenced by a (lib-)invoke. */
	public abstract boolean isLibCallObj();
	public abstract boolean isReturnedVar();
	public boolean isKillable() { return true; }
	public boolean isDefinite() { return Util.isValueDefinite(val); }
	/** Whether vars may "equal" (as vars, regardless of possible non-aliasing). */
	public abstract boolean mayEqual(Variable vOther);
}
