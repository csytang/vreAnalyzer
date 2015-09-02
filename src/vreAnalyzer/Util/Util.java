package vreAnalyzer.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.ArrayType;
import soot.FastHierarchy;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JRetStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.util.NumberedString;
import vreAnalyzer.Context.Context;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.PointsTo.PointsToGraph;


public class Util {
	
	static boolean verbose = false;
	///////////////////////////Functional methods//////////////////////////////////
	//Whether a statement is a return statement
	public static boolean isReturnStmt(Stmt s) {
		return s instanceof JRetStmt || s instanceof JReturnStmt || s instanceof JReturnVoidStmt;
	}

	public static boolean isValueDefinite(Value v) {
		// TODO: determine if local is in a recursive method (in which case, it is NOT definite)
		// case 1 or 2: constant or local
		if (v instanceof Constant || v instanceof Local)
			return true;
		
		// case 3: field is definite only if it's static
		if (v instanceof FieldRef)
			return ((FieldRef) v).getFieldRef().isStatic();
		
		// case 4: array elems are all treated, for now, as not definite (conservative assumption)
		// case 5: only class objects (due to static invoke expr) are definite
		assert v instanceof ArrayRef || v instanceof InvokeExpr;
		return v instanceof StaticInvokeExpr;
	}
	
	/**
	 * This method will find the specific call target with the given call expression
	 * @param instInvExpr
	 * @param mAppTgts
	 * @param mLibTgts
	 * @return
	 */
	
	public static boolean getConcreteCallTargets(InstanceInvokeExpr instInvExpr,
			Set<SootMethod> mAppTgts, Set<SootMethod> mLibTgts) {
		// TODO  get class of method ref; we start searching from this class
		SootMethodRef mref = instInvExpr.getMethodRef();
		
		// Starting class
		SootClass cls = mref.declaringClass(); 
		// Signature to search for
		final NumberedString subsignature = mref.getSubSignature(); 
		
		/* 
		 * CASE 1: Object is of declared class type or inherited from some superclass
		 *         find first superclass, starting from current cls, that declares method; there HAS to be such a class
		 * note that if cls is interface, superclass if java.lang.Object
		 * note that we don't check if there is indeed an interface declaring the method; we assume this is the case if no superclass declares it
		 * 情況1: Object是從一種聲明的類類型或者繼承自聲明的類類型
		 * 方法：從本類開始向上檢索 一定會搜索到這個類
		 * 注意：如果cls是一種藉口的話
		 */
		
		while ((!cls.declaresMethod(subsignature)) && cls.hasSuperclass())
			cls = cls.getSuperclass(); // Never an interface
		
		// Now, method might not be in superclass, or might be abstract; in that case, it's not a target
		SootMethod m = null;
		
		if (cls.declaresMethod(subsignature)) {
			m = cls.getMethod(subsignature);
			if (!m.isAbstract()) {
				if (cls.isApplicationClass()){
					mAppTgts.add(m); // add app method
				}
				else{
					mLibTgts.add(m); // add lib method
				}
					
			}
		}
		if(!mLibTgts.isEmpty() || !mAppTgts.isEmpty()){
				return true;
		}
		/*
		 *  (only for virtual/interface calls)
		 *  CASE 2: object's actual type is a subclass; any subclass declaring the method is a possible target
		 *  we have to check all superclasses of implementers, because starting cls might be interface
		 */
		List<SootClass> allSubclasses;
		if (instInvExpr instanceof VirtualInvokeExpr||instInvExpr instanceof InterfaceInvokeExpr) {
			
			cls = mref.declaringClass(); // start again from declaring class
			
			allSubclasses = getAllSubtypes(cls);
			for (SootClass subCls : allSubclasses) {
				m = getMethodInClassOrSuperclass(subCls, subsignature);
				if (m != null && !m.isAbstract()) {
				
					if (m.getDeclaringClass().isApplicationClass()){
						mAppTgts.add(m); // add app method
					}
					else{
						mLibTgts.add(m); // add lib method
					}
				}
			}
			
			
			if(mLibTgts.isEmpty() && mAppTgts.isEmpty()){
				
				boolean detected = false;
				
				cls = mref.declaringClass();
				Stack<SootClass>superClassAndInterfaces = new Stack<SootClass>();
				superClassAndInterfaces.push(cls);
				while (!superClassAndInterfaces.isEmpty()){
						cls = superClassAndInterfaces.pop();
						
						for(SootClass sm:cls.getInterfaces()){
							superClassAndInterfaces.push(sm);
							
							if(sm.declaresMethod(subsignature)){
								m = sm.getMethod(subsignature);
								
								if(sm.isApplicationClass()){
									System.out.println("[vreAnalyzer] Warnning!! for invoke\t"+instInvExpr.toString() +"\tUnsafe app target setting to:\t"+m.getName());
									mAppTgts.add(m);
								}
								else{
									System.out.println("[vreAnalyzer] Warnning!! for invoke\t"+instInvExpr.toString() +"\tUnsafe lib target setting to:\t"+m.getName());
									mLibTgts.add(m);
								}
								detected = true;
								return true;
							}
						}
						if(!detected){
							cls = cls.getSuperclass();
						}else
							break;
				}
				
			}
		}
		
		
		return (!mLibTgts.isEmpty() || !mAppTgts.isEmpty());
		
		
	}
	
	/** Returns method in given class or first upwards superclass,
	 *  or null if not found in any class (no interface checked) */
	private static SootMethod getMethodInClassOrSuperclass(SootClass cls, NumberedString subsignature) {
		if (cls.declaresMethod(subsignature))
			return cls.getMethod(subsignature);
		if (cls.hasSuperclass())
			return getMethodInClassOrSuperclass(cls.getSuperclass(), subsignature);
		return null;
	}
	
	
	/** 
	 * Returns the transitive closure of subinterfaces and subclasses of given class or interface (excluding given class).
	 *  */
	public static List<SootClass> getAllSubtypes(SootClass cls) {
		// TODO store (cache) all subclasses in Class Tag
		List<SootClass> subclasses = new ArrayList<SootClass>();
		FastHierarchy hierarchy = Scene.v().getOrMakeFastHierarchy();
		//subclasses.add(cls);
		for (Iterator<SootClass> itSubCls = hierarchy.getSubclassesOf(cls).iterator(); itSubCls.hasNext(); ) {
			SootClass subCls = (SootClass) itSubCls.next();
			subclasses.add(subCls);
			subclasses.addAll(getAllSubtypes(subCls));
		}
		for (Iterator<SootClass> itSubCls = hierarchy.getAllImplementersOfInterface(cls).iterator(); itSubCls.hasNext(); ) {
			SootClass subCls = (SootClass) itSubCls.next();
			subclasses.add(subCls);
			subclasses.addAll(getAllSubtypes(subCls));
		}
		return subclasses;
	}
	

	
	/** A value represents a var if it's a constant, local, field ref (for field itself), or array elem ref (any element in it).
	 *  Takes 'must' param to distinguish between 'just possibly equal' and 'definitely equal' values (a distinction for fields and array elem refs). 
	 *  Objects are *not* considered here. */
	public static boolean valuesEqual(Value v1, Value v2, boolean must) {
		// case 1: both null refs or constants
		if (v1 == null || v2 == null)
			return v1 == v2;
		if (v1 instanceof Constant) {
			if (v2 instanceof Constant)
				return v1.equals(v2);
			return false;
		}
		
		// case 2: locals
		if (v1 instanceof Local)
			return v1 == v2;
		// case 3: field refs (if 'must' is false, base is ignored)
		if (v1 instanceof FieldRef) {
			SootFieldRef sfr1 = ((FieldRef) v1).getFieldRef();
			if (!(v2 instanceof FieldRef))
				return false;
			SootFieldRef sfr2 = ((FieldRef) v2).getFieldRef();
			
			if (sfr1.declaringClass() == sfr2.declaringClass() && sfr1.name().equals(sfr2.name())) {
				if (!must)
					return true;
				// additional 'must' requirements
				return !(v1 instanceof InstanceFieldRef) || ((InstanceFieldRef)v1).getBase() == ((InstanceFieldRef)v2).getBase();
			}
			else
				return false;
		}
		// case 4: array elems; if 'must' is false, base is ignored; FOR NOW, index is never compared, even if constant (because of excess of instrumentation?) 
		//         they are the same if elem types are equal and index values are not definitely different
		assert (v1 instanceof ArrayRef);
		
		ArrayRef ae1 = (ArrayRef)v1;
		if (!(v2 instanceof ArrayRef))
			return false;
		ArrayRef ae2 = (ArrayRef)v2;
		if (!ae1.getBase().getType().equals(ae2.getBase().getType()))
			return false;
		// OLD -- *** FOR NOW, we avoid distinguishing array elems with constant indices as different variables,
		// OLD --  because it leads to too much instrumentation at defs and uses of array elem with unknown index.
		if (!must)
			return true;
		return ae1.getBase().equals(ae2.getBase()) && ae1.getIndex().equals(ae2.getIndex());
	}
	
	
	
	/** Hash code for Value representing a variable if it's a constant, local, field ref (for field itself), or array elem ref (for all elements of array of that type).
	 *  Hash codes are equal for two values iff the two values must be equal, as per {@link #valuesEqual(Value, Value, boolean)} with must=true. */
	public static int valueHashCode(Value v) {
		// case 1: null or constant
		if (v == null)
			return 0;
		if (v instanceof Constant)
			return v.hashCode();
		
		// case 2: local
		if (v instanceof Local)
			return v.hashCode();
		// case 3: field ref (base is ignored)
		if (v instanceof FieldRef) {
			SootFieldRef sfr = ((FieldRef) v).getFieldRef();
			return sfr.resolve().hashCode();
		}
		// case 4: array elems
		assert (v instanceof ArrayRef);
		ArrayRef ae1 = (ArrayRef)v;
		// *** FOR NOW, we avoid distinguishing array elems with constant indices as different variables,
		//  because it leads to too much instrumentation at defs and uses of array elem with unknown index.
		return ae1.getBase().getType().hashCode();// + ((ae1.getIndex() instanceof Constant)? ae1.getIndex().hashCode() : 0);
	}
	
	
	// TODO: make consistent with 'may' object equality check
	/** Uses EXACT of possible runtime types referenced by values.
		 *  These types of Value can represent an object:
		 *    1. NewExpr/NewArrayExpr (instance)
		 *    2. InvokeExpr           (static or instance)
		 *    3. Local/statfield ref var  (instance)
		 *    4. StringConstant       (instance)
		 *    5. ClassConstant       (instance)
		 */
	public static boolean objValuesMustEqual(Value v1, Value v2, boolean sameLocation) {
			// class set that objects represent is NOT A SUFFICIENT condition for equality
			if (v1 instanceof NewExpr || v1 instanceof NewArrayExpr)
				return v1 == v2; //this.n == vOther.n; // location distinguishes new-object expressions
			else if (v1 instanceof StaticInvokeExpr)
				return v2 instanceof StaticInvokeExpr &&
					   ((StaticInvokeExpr)v1).getMethodRef().declaringClass() == ((StaticInvokeExpr)v2).getMethodRef().declaringClass(); // not the same as getMethod().getDeclaringClas() !!!
			else if (v1 instanceof InstanceInvokeExpr)
				return v2 instanceof InstanceInvokeExpr &&
					   ((InstanceInvokeExpr)v1).getBase() == ((InstanceInvokeExpr)v2).getBase();  // discard equality if bases are not the same
			else if (v1 instanceof Local)
				return v1 == v2; // discard equality if locals are not the same
			else if (v1 instanceof StaticFieldRef)
				return v2 instanceof StaticFieldRef &&
					   ((StaticFieldRef)v1).getField() == ((StaticFieldRef)v2).getField(); // discard equality if static fields are not the same
			else if (v1 instanceof ClassConstant)
				return v2 instanceof ClassConstant &&
						((ClassConstant)v1).getType().equals(((ClassConstant)v1).getType());
			else {
				assert v1 instanceof StringConstant;
				return sameLocation && v1.equals(v2);
			}
	}
	/** Hash code for Value representing an object (static or instance) */
	public static int objValueHashCode(Value v) {
		// case 5: (lib) object, either static or instance
		if (v instanceof StaticInvokeExpr)
			return ((StaticInvokeExpr)v).getMethodRef().declaringClass().hashCode(); // not the same as getMethod().getDeclaringClas() !!!
		else {
			// no good solution that guarantees that equal (may-point-to-same-instance) refs will get same hash code,
			// so just use constant value
			return 1;
		}
	}
	
	// TODO: make consistent with 'must' object equality check
		/** Uses intersection of possible runtime types referenced by values.
		 *  These types of Value can represent an object:
		 *    1. NewExpr/NewArrayExpr (instance)
		 *    2. InvokeExpr           (static or instance)
		 *    3. Local/statfield ref var  (instance)
		 *    4. StringConstant       (instance)
		 *    5. ClassConstant       (instance)
		 */
	public static boolean objValuesMayEqual(Value v1, Value v2) {
			// case 5: (lib) object, either static or instance
			//         objects are (possibly) the same if they are both static or both instance,
			//         and there is at least one object type that v1 and v2 can refer to in their call.
			// NOTE: includes automatically constructed Strings, represented by StringConstant values, or Class constants (java.lang.Class)
			
			// first, handle special case with more precision: both values are string constants
			if (v1 instanceof StringConstant && v2 instanceof StringConstant)
				return ((StringConstant)v1).value.equals(((StringConstant)v2).value);
			// second, handle special case with more precision: both values are class constants
			if (v1 instanceof ClassConstant && v2 instanceof ClassConstant)
				return ((ClassConstant)v1).value.equals(((ClassConstant)v2).value);
			
			// get list of ref types of instances to which each value can refer
			// (null if val refers to class itself, not instances)
			List<RefLikeType> clsTargetsSorted1 = getAllPossibleRuntimeRefTypes(v1);
			List<RefLikeType> clsTargetsSorted2 = getAllPossibleRuntimeRefTypes(v2);
			
			// second, handle special case of values referring to class object (static)
			if (clsTargetsSorted1 == null) {
				if (clsTargetsSorted2 != null)
					return false;
				SootClass cls1 = ((StaticInvokeExpr)v1).getMethodRef().declaringClass(); // not the same as getMethod().getDeclaringClas() !!!
				SootClass cls2 = ((StaticInvokeExpr)v2).getMethodRef().declaringClass(); // not the same as getMethod().getDeclaringClas() !!!
				return cls1.equals(cls2);
			}
			else if (clsTargetsSorted2 == null)
				return false;
			
			// now, just check for intersection of returned lists of possible target classes
			return intersectSortedAscending(clsTargetsSorted1, clsTargetsSorted2);
	}
	
	/** Cache to avoid re-computing potentially large sets of types possibly referenced by a value. */
	private static final Map<Value,List<RefLikeType>> valToRefTypes = new HashMap<Value, List<RefLikeType>>();
	private static final HashCodeComparator<RefLikeType> hashCodeComparator = new HashCodeComparator<RefLikeType>();
	
	/** Returns list of all possible ref/array types of instances that Value can represent as variables. Ref types are in ascending order based on their hashcodes.
	 *  Returns null if no instance obj can be represented by Value (e.g., static method call). */
	private static List<RefLikeType> getAllPossibleRuntimeRefTypes(Value val) {
		// first, try to extract from cache to avoid re-computation
		List<RefLikeType> typeTargets = valToRefTypes.get(val);
		if (typeTargets != null)
			return typeTargets;
		typeTargets = new ArrayList<RefLikeType>();
		
		// 1.a) NewExpr        (instance)
		if (val instanceof NewExpr)
			typeTargets.add((RefLikeType)((NewExpr)val).getType());
		// 1.b) NewArrayExpr   (instance)
		else if (val instanceof NewArrayExpr)
			typeTargets.add((RefLikeType)((NewArrayExpr)val).getType());
		// 2. InvokeExpr     (static or instance)
		else if (val instanceof InvokeExpr) {
			if (val instanceof StaticInvokeExpr)
				typeTargets = null; // special case
			else if (val instanceof SpecialInvokeExpr)
				typeTargets.add((RefType)((SpecialInvokeExpr)val).getBase().getType());
			else {
				assert val instanceof InstanceInvokeExpr;
				SootClass declCls = ((InvokeExpr)val).getMethod().getDeclaringClass();
				typeTargets.add(declCls.getType());
				
				for (SootClass clsSub : getAllSubtypes(declCls))
					typeTargets.add(clsSub.getType());
			}
		}
		// 3. Local/statfield ref var  (instance)
		else if (val instanceof Local || val instanceof StaticFieldRef) {
			RefLikeType lType = (RefLikeType) val.getType();
			typeTargets.add(lType);
			if (lType instanceof RefType) {
				// add all possible subtypes of ref type
				for (SootClass clsSub : getAllSubtypes(((RefType)lType).getSootClass()))
					typeTargets.add(clsSub.getType());
			}
			else
				assert lType instanceof ArrayType; // array type has no subtypes
		}
		// 4. StringConstant (instance)
		else {
			assert val instanceof StringConstant || val instanceof ClassConstant;
			typeTargets.add((RefType)val.getType()); //Scene.v().getRefType("java.lang.String"));
		}
		
		// sort list before returning, to facilitate intersection-checking of lists
		if (typeTargets != null)
			Collections.sort(typeTargets, hashCodeComparator);
		valToRefTypes.put(val, typeTargets);
		return typeTargets;
	}
	
	/** Comparator based on the value returned by the {@link Object#hashCode()} method. Null references are considered the lowest possible value. */
	public static class HashCodeComparator<T> implements Comparator<T> {
		/** Returns a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second, respectively. */
		public int compare(T o1, T o2) {
			if (o1 == null)
				return (o2 == null)? 0 : -1;
			if (o2 == null)
				return 1;
			
			final int h1 = o1.hashCode();
			final int h2 = o2.hashCode();
			return (h1 < h2)? -1 : ((h1 == h2)? 0 : 1);
		}
	}
	
	/** Checks for intersection of lists, exploiting that the lists are (must be!) sorted in ascending order of the hashcodes of their elements. */
	private static boolean intersectSortedAscending(List<? extends Object> l1, List<? extends Object> l2) {
		// use iterators
		Iterator<? extends Object> it1 = l1.iterator();
		Iterator<? extends Object> it2 = l2.iterator();
		if (!it1.hasNext() || !it2.hasNext())
			return false; // one of the list is empty; no possible intersection
		// start iterating over l1 or l2, based on lowest hash code
		Object o1 = it1.next();
		Object o2 = it2.next();
		int h1 = o1.hashCode();
		int h2 = o2.hashCode();
		while (true) {
			// compare objects only if hash code is the same
			if (h1 == h2 && o1.equals(o2))
				return true;
			
			// objects are different; determine whether to advance o1 or o2 to next element
			if (h1 <= h2) {
				if (it1.hasNext()) {
					o1 = it1.next();
					h1 = o1.hashCode();
				}
				else
					return false;
			}
			else {
				if (it2.hasNext()) {
					o2 = it2.next();
					h2 = o2.hashCode();
				}
				else
					return false;
			}
		}
	}

	public static void updateSucceedP2G(Local local,AnyNewExpr expr,CFGNode cfgNode,CFG cfg,Context allcontext){
		List<CFGNode>allnodes = cfg.getNodes();
		int start = allnodes.indexOf(cfgNode);
		for(int i = start+1;i < allnodes.size();i++){
			CFGNode curr = allnodes.get(i);
			PointsToGraph currbf = (PointsToGraph) allcontext.getValueBefore(curr);
			PointsToGraph curraf = (PointsToGraph) allcontext.getValueAfter(curr);
			currbf.assignNew(local, expr);
			curraf.assignNew(local, expr);
		}
	}

	
	
	
	////////////////////////////////////////////////////////////////////////////////
	
}
