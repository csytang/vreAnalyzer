package vreAnalyzer.ControlFlowGraph.DefUse;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import vreAnalyzer.ControlFlowGraph.Branch;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.ControlFlowGraph.DefUse.Def.ConstCSParamDef;
import vreAnalyzer.ControlFlowGraph.DefUse.Def.ConstReturnDef;
import vreAnalyzer.ControlFlowGraph.DefUse.Def.Def;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.CSConstParamCUse;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.CSReturnedVarCUse;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.CUse;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.PUse;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.Use;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.ObjVariable;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.StdVariable;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CFGNodeSpecial;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.Util.Options;




public class CFGDefUse extends CFG {

	private boolean verbose = false;
	
	
	/** Simple comparator based on toString, to just order variables canonically. */
	public static class VariableComparator implements Comparator<Variable> {
		public int compare(Variable v1, Variable v2) { return v1.toString().compareTo(v2.toString()); }
	}
	
	////////////////////////////Fields///////////////////////////////////////////
    protected ArrayList<Use> idsToUses = new ArrayList<Use>(); // intra-procedural id->use map
	protected HashMap<Use, Integer> usesToIds = new HashMap<Use, Integer>(); // use->id map (reverse of previous map)
	protected ArrayList<Def> idsToDefs = new ArrayList<Def>(); // intra-procedural id->def map
	protected HashMap<Variable,BitSet> varsToUses = new HashMap<Variable,BitSet>(); // associates each var with all uses for it
	protected HashMap<Variable,BitSet> varsToDefs = new HashMap<Variable,BitSet>(); // associates each var with all defs for it
	protected List<Use> fieldUses = new ArrayList<Use>();
	protected List<Def> fieldDefs = new ArrayList<Def>();
	protected List<Use> arrElemUses = new ArrayList<Use>();
	protected List<Def> arrElemDefs = new ArrayList<Def>();
	/** All uses of class/static and instance (lib) objects. */
	protected List<Use> libObjUses = new ArrayList<Use>();
	/** All defs of class/static and instance (lib) objects. */
	protected List<Def> libObjDefs = new ArrayList<Def>();
	/** Uses of values returned from app method called from this CFG */
	protected Map<CFGNode,Use> callRetUses = new HashMap<CFGNode, Use>();
	/** Special defs of constant arguments to app method calls from this CFG */
	protected List<ConstCSParamDef> argConstDefs = new ArrayList<ConstCSParamDef>();
	/** Special defs of constant return-values to app method calling this CFG */
	protected List<ConstReturnDef> retConstDefs = new ArrayList<ConstReturnDef>();
	/////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////////////////////////
	
	public CFGDefUse(SootMethod mNode) {
		super(mNode);
		// TODO Auto-generated constructor stub
	}
	
	////////////////////////

	///////////////////////////////Member Function///////////////////////////////////
	public List<Use> getUses() { return idsToUses; }
	public List<Def> getDefs() { return idsToDefs; }
	public int getUseId(Use use) { return usesToIds.get(use); }
	public List<Use> getFieldUses() { return fieldUses; }
	public List<Def> getFieldDefs() { return fieldDefs; }
	public List<Use> getArrayElemUses() { return arrElemUses; }
	public List<Def> getArrayElemDefs() { return arrElemDefs; }
	public List<Use> getLibObjUses() { return libObjUses; }
	public List<Def> getLibObjDefs() { return libObjDefs; }
	public List<Def> getConstDefs() { return new ArrayList<Def>(argConstDefs.isEmpty()? retConstDefs : argConstDefs); }
	
	
	
	public List<Use> getUsesBefore(NodeDefUses defNode){
		List<Use> useBefore = new LinkedList<Use>();
		int currIdx = this.getIndexId(defNode);
		for(Use u:idsToUses){
			// It is a branck
			if(u.getN()==null)
			{	
				CFGNode branchSrc = u.getBranch().getSrc();
				if(branchSrc!=null && this.getIndexId(branchSrc)< currIdx)
					useBefore.add(u);
			}else{
				// Single node				
				if(this.getIndexId(u.getN())<currIdx)
					useBefore.add(u);
			}
		}
		return useBefore;
	}
	
	public List<Def> getDefsBefore(NodeDefUses defNode){
		List<Def> defBefore = new LinkedList<Def>();
		int currIdx = this.getIndexId(defNode);
		for(Def def:idsToDefs){
			// It is a branck
			if(def.getN()!=null)
			{	
				if(this.getIndexId(def.getN())< currIdx)
					defBefore.add(def);
			}
		}
		
		return defBefore;
	}
	/////////////////////////////////////////////////////////////////////////////////
	
	
	
	//////////////////////////////Override Method////////////////////////////////////////
	public void analyze() {
		super.analyze(); // should always perform superclass's initial analysis first
		// Here is the method at super class
		if(verbose)
			System.out.println("CFG for " + ProgramFlowBuilder.inst().getMethodIdx(method) + ": " + method);
		identifyDefsUses();
	}
	
	protected NodeDefUses instantiateNode(StmtTag s) { return new NodeDefUses(s,this.method); }
	/** identify defs and uses per node */
	protected void identifyDefsUses() {
		
		final boolean allowParmsRetUseDefs = Options.allowParmsRetUseDefs();
		
		// find uses -- includes actual params in calls
		for (CFGNode _n : nodes) {
			if (_n instanceof CFGNodeSpecial)
				continue; // ENTRY or EXIT
			
			NodeDefUses n = (NodeDefUses)_n;
			Stmt s = n.getStmt();
			
			// If this statement is not an identity statement (an identity represents use this or assigns parameter to argument)
			if (!(s instanceof IdentityStmt)) {
				// create uses for all local/field/array accesses
				List<Branch> outBrs = n.getOutBranches();
				
				List useBoxes = s.getUseBoxes();
				
				ArrayList<Use> uses = new ArrayList<Use>();
				

				// Consider special uses: constant parameters in app calls, and returned values
				StmtTag sTag = (StmtTag) s.getTag(StmtTag.TAG_NAME);
				CallSite cs = sTag.getCallSite();
				
				// Iterate the usebox of this DefUse CFGNode to add all uses to it
				for (Iterator itUse = useBoxes.iterator(); itUse.hasNext(); ) {
					// For a  value use
					Value useV = ((ValueBox) itUse.next()).getValue();
					
					// Local value use
					if (useV instanceof Local) {
						if (outBrs != null) {  // create one p-use per successor of n
							for (Branch outBr : outBrs)
								uses.add(new PUse(useV, outBr));
						}
						else
							uses.add(new CUse(useV, n));
					}
					// Field use
					else if (useV instanceof FieldRef) {
						assert outBrs == null; // jimple does not have field refs in branching stmts
						fieldUses.add(new CUse(useV, n));
					}
					// Array use
					else if (useV instanceof ArrayRef) {
						assert outBrs == null; // jimple does not have array refs in branching stmts
						arrElemUses.add(new CUse(useV, n));
					}
					// Constant use
					else if (useV instanceof Constant) {
						if (cs != null && cs.hasAppCallees()) {
							// NOTE: *includes* StringConstant, which here represents ref var for automatically-created string
							uses.add(new CSConstParamCUse((Constant)useV, cs, uses.size(), n));
						}
					}
				}
				
				// Callsite associated
				if (cs != null) {
					InvokeExpr invExpr = s.getInvokeExpr();
					if (cs.hasAppCallees()) {
						
						// Constant parameters in app calls
						int argIdx = 0;
						for (Object arg : invExpr.getArgs()) {
							// NOTE: *includes* StringConstant, which here represents ref var for automatically-created string
							if (arg instanceof Constant) // constant actual param in app call
								uses.add(new CSConstParamCUse((Constant)arg, cs, argIdx, n));
							++argIdx;
						}
						// *** IMPORTANT ***
						// Const args are now handled above, at iteration on use boxes, to ensure args keep their original order in local uses list
						
						// returned-value use
						if (!invExpr.getType().equals(VoidType.v())) {
							// we assume that rhs of return can't be a call (i.e., ret values in Jimple are not chained)
							assert !(s instanceof ReturnStmt);
							if (s instanceof AssignStmt) {
								Use uCallRet = new CSReturnedVarCUse(invExpr, cs, n);
								callRetUses.put(n, uCallRet);
							}
							else
								assert s instanceof InvokeStmt; // call that ignores returned value
						}
					}
					if (cs.hasLibCallees()) {
						// special uses occurring *inside* library call
						for (Value valObjUse : ObjDefUseModelManager.getInternalObjUses(invExpr))
							libObjUses.add(new CUse(new ObjVariable(valObjUse, n), n));
					}
				}
				
				// add to CFG all uses in this node, obtaining incremental new ids
				int[] usesIds = new int[uses.size()]; // maps node use id to CFG use id
				int i = 0;
				for (Use u : uses) {
					int useId = idsToUses.size(); // CFG use id
					idsToUses.add(u);
					usesToIds.put(u, useId); // store also in reverse map
					usesIds[i] = useId;
					++i;
				}
				n.setLocalUsesIds(usesIds);
			}
			else
				n.setLocalUsesIds(new int[0]);
		}
		
		// find defs and params
		final boolean isEntryCFG = ProgramFlowBuilder.inst().getEntryMethods().contains(method);
		final boolean isInstanceInit = method.getName().equals("<init>");
		for (CFGNode _n : nodes) {
			if (_n instanceof CFGNodeSpecial)
				continue; // ENTRY or EXIT
			
			NodeDefUses n = (NodeDefUses) _n;
			Stmt s = n.getStmt();
			if (!(s instanceof IdentityStmt) || isEntryCFG || allowParmsRetUseDefs ||
					(isInstanceInit && ((Local)((IdentityStmt)s).getLeftOp()).getName().equals("this")))
			{
				List defBoxes = s.getDefBoxes();
				ArrayList<Def> defs = new ArrayList<Def>();
				if (!defBoxes.isEmpty()) {
					assert defBoxes.size() == 1;
					Value defV = ((ValueBox) defBoxes.iterator().next()).getValue();
					if (defV instanceof Local) { // create local var def
						defs.add(new Def(defV, n));
					}
					else if (defV instanceof FieldRef) {
						fieldDefs.add(new Def(defV, n));
					}
					else if (defV instanceof ArrayRef) {
						arrElemDefs.add(new Def(defV, n));
					}
					else
						System.err.println("Unsupported def value type " + defV.getClass());
				}
				
				// add special defs: constant parameters in app calls
				//                   and defined lib objects
				StmtTag sTag = (StmtTag) s.getTag(StmtTag.TAG_NAME);
				CallSite cs = sTag.getCallSite();
				if (cs != null) {
					InvokeExpr invExpr = s.getInvokeExpr();
					if (cs.hasAppCallees()) {
						// constant actual params
						int argIdx = 0;
						for (Object arg : invExpr.getArgs()) {
							// constant actual param in app call
							// NOTE: for raw string "..." param, only represents ref to string that gets created
							if (arg instanceof Constant)
								argConstDefs.add(new ConstCSParamDef((Constant)arg, cs, argIdx, n));
							++argIdx;
						}
					}
					if (cs.hasLibCallees()) {
						// objects (potentially) defined (modified) *inside* lib call
						for (Value valObjDef : ObjDefUseModelManager.getInternalObjDefs(invExpr))
							libObjDefs.add(new Def(new ObjVariable(valObjDef, n), n));
					}
					// Special defs of java.lang.String objects created automatically because of raw-string parameters "..."
					// These strings are created *before* call (need to explicitly define) and *outside* call (not created by lib model)
					for (Object arg : invExpr.getArgs()) {
						// represents actual java.lang.String that gets created, not the ref var that is the parameter itself
						// TODO: def of auto-string obj actually occurs *before* call (model puts it first? what about app call?)
						if (arg instanceof StringConstant)
							libObjDefs.add(new Def(new ObjVariable((StringConstant)arg, n), n));
					}
				}
				// add special defs: constant return values
				if (s instanceof ReturnStmt) {
					List useBoxes = ((ReturnStmt)s).getUseBoxes();
					if (!useBoxes.isEmpty()) {
						assert useBoxes.size() == 1;
						Value vRet = ((ValueBox)useBoxes.iterator().next()).getValue();
						if (vRet instanceof Constant)
							retConstDefs.add(new ConstReturnDef((Constant)vRet, n));
					}
				}
				// add special defs: new array and new array elem
				//   NOTE: by convention new lib object is NOT a definition -- it is defined by <init> def/use model!
				if (s instanceof AssignStmt) {
					Value vRight = ((AssignStmt)s).getRightOp();
					if (vRight instanceof NewArrayExpr) {
						// define array object
						libObjDefs.add(new Def(new ObjVariable(vRight, n), n));
						// create definition for ANY elem in this array (and, for now, for all elems of array-elem's type)
						Value vSize = ((NewArrayExpr)vRight).getSize();
						arrElemDefs.add(new Def(new StdVariable(Jimple.v().newArrayRef(((AssignStmt)s).getLeftOp(), vSize)), n));
					}
				}
				
				// register defs, obtaining incremental new id
				int[] defsIds = new int[defs.size()];
				int i = 0;
				for (Def d : defs) {
					int defId = idsToDefs.size();
					idsToDefs.add(d);
					defsIds[i++] = defId;
				}
				n.setLocalDefsIds(defsIds);
			}
			else
				n.setLocalDefsIds(new int[0]);
		}
		// add obj defs for entry method's arguments and static library fields
		if (isEntryCFG) {
			// NOTE: we only support 'String[] args' obj defs for entry method 'main(String[])', for now
			if (method.toString().equals("<"+method.getDeclaringClass()+": void main(java.lang.String[])>")) {
				// def: array itself (local is already defined in code); use local as base of array ref (i.e., array elem)
				NodeDefUses nDUArgArrParam = (NodeDefUses) nodes.get(1); // first id stmt: args := @parameter0: java.lang.String[]
				RefType typeStr = Scene.v().getRefType("java.lang.String");
				libObjDefs.add(new Def(  // need to create array object ourselves (actual array created by JVM before entry to 'main')
						new ObjVariable(Jimple.v().newNewArrayExpr(typeStr, IntConstant.v(1)), nDUArgArrParam), // dim 1 shouldn't matter
						nDUArgArrParam)); // occurs at very first id stmt: args := @parameter0: java.lang.String[]
				// def: elements of 'args' array (i.e., String objects)
				libObjDefs.add(new Def(
						new ObjVariable(Jimple.v().newNewExpr(typeStr), nDUArgArrParam),
						nDUArgArrParam)); // occurs at very first id stmt: args := @parameter0: java.lang.String[]
			}
			
			// def: static field System.out (and obj pointed by field) of type java.io.PrintStream
			SootClass clsSystem = Scene.v().getRefType("java.lang.System").getSootClass();
			FieldRef fldRefOut = Jimple.v().newStaticFieldRef(clsSystem.getFieldByName("out").makeRef());
			fieldDefs.add(new Def(new StdVariable(fldRefOut), ENTRY));
//			SootClass clsPrintStream = Scene.v().getRefType("java.io.PrintStream").getSootClass();
			libObjDefs.add(new Def(new ObjVariable(fldRefOut, ENTRY), ENTRY));
//					new ObjVariable(Jimple.v().newNewExpr(clsPrintStream.getType()), ENTRY), ENTRY));
		}
		
		// map values to uses (in the form of bitsets)
		final int numUses = idsToUses.size();
		int useIdx = 0;
		for (Use use : idsToUses) {
			// get/create bitset for use's value
			BitSet bset = varsToUses.get(use.getVar());
			if (bset == null) {
				bset = new BitSet(numUses);
				varsToUses.put(use.getVar(), bset);
			}
			bset.set(useIdx); // mark use for this value
			++useIdx;
		}
		final int numDefs = idsToDefs.size();
		int defIdx = 0;
		for (Def def : idsToDefs) {
			// get/create bitset for def's value
			BitSet bset = varsToDefs.get(def.getVar());
			if (bset == null) {
				bset = new BitSet(numDefs);
				varsToDefs.put(def.getVar(), bset);
			}
			bset.set(defIdx); // mark def for this value
			++defIdx;
			
			// ensure creation of 0-filled value-use bitsets for defined values without uses
			if (varsToUses.get(def.getVar()) == null)
				varsToUses.put(def.getVar(), new BitSet(numUses));
		}
		
		if(verbose){
			System.out.println("  Method defs " + idsToDefs.size() + ", uses " + idsToUses.size() + ", values " + varsToUses.size());
			
			System.out.print("    Defs: ");
			int id = 0;
			for (Def d : idsToDefs)
				System.out.print((id++) + "=" + d + ",");
			System.out.println();
			
			System.out.print("    Uses: ");
			id = 0;
			for (Use u : idsToUses)
				System.out.print((id++) + "=" + u + ",");
			System.out.println();
			
			System.out.print("    Var uses: ");
			for (Variable var : varsToUses.keySet())
				System.out.print(var + "=" + varsToUses.get(var) + ",");
			System.out.println();
			
			System.out.print("    Var defs: ");
			for (Variable var : varsToDefs.keySet())
				System.out.print(var + "=" + varsToDefs.get(var) + ",");
			System.out.println();
		}
	}
	
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////
	
	
}
