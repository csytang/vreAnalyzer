package vreAnalyzer.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.Modifier;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.Elements.Location;
import vreAnalyzer.Util.SootClassWrapper;
import vreAnalyzer.Util.Util;

public class MethodTag implements Tag {
	public static String TAG_NAME = "mdf";
	/////////////////////////Field///////////////////////////////
	// SootMethod attached;
	private SootMethod sm = null;
	private SootClass cls = null;
	// All statement in the method (unordered)
	private ArrayList<Stmt> stmtList;
	
	// Stores exits (return nodes)' location{@link location}; the OUT sets are the out sets of the method 
	private List<Location> exitPoints;
	
	// Store the statement to id
	private HashMap<Stmt, Integer> revStmtMap;
	private boolean isOverloadMethod = false;
	private boolean isOverrideMethod = false;
	
	// Store the formal parameters
	private Local[] formalParams;
	
	// All callee methods (including lib and app)
	private HashSet<SootMethod> allCalleeMethods = new HashSet<SootMethod>();
	
	
	// App caller methods
	//private HashSet<SootMethod> appCallerMethods = new HashSet<SootMethod>();
	
	// No Lib caller methods, cus application method cannot be invoked by lib method
	
	/** For now, doesn't includes call sites in catch blocks */
	private ArrayList<CallSite> callSites = new ArrayList<CallSite>(); // holds all call sites inside method, which represent call locations and callees
	private ArrayList<CallSite> callerSites = new ArrayList<CallSite>();// holds all caller site to this method;
	private Map<Stmt,CallSite> stmtToCallSite = new HashMap<Stmt,CallSite>();
	
	
	
	private boolean verbose = false;
	///////////////////////////////////////////////////////////
	
	
	/////////////////////////Constructor//////////////////////
	// Use for clone only
	public MethodTag(){
		
	}
	
	// By default
	public MethodTag(SootMethod sm) {
		// TODO Auto-generated constructor stub
		this.sm = sm;
		this.cls = sm.getDeclaringClass();
		this.stmtList = new ArrayList<Stmt>(); // maps id->stmt
		List<SootMethod>methods = sm.getDeclaringClass().getMethods();
		int methodCount = getMethodsCountForName(sm.getName(),methods);
		if(methodCount>1)
			isOverloadMethod = true;
		else
			isOverloadMethod = false;
		this.isOverrideMethod = isOverrideSootMethod();
		// Init all statements
		int sIdx = 0;
		for (Unit u : sm.retrieveActiveBody().getUnits())
		{
			StmtTag stnode = new StmtTag(sm,(Stmt)u,sIdx++);
			// create source location tag for the statement
			SourceLocationTag slnode = new SourceLocationTag(sm,u);
			stmtList.add((Stmt)u);
			u.addTag(stnode);
			u.addTag(slnode);
		}
		
		assert !this.stmtList.isEmpty();
		
		// Set the exit point;
		this.exitPoints = null; // will be created later
		
		// Create reverse map stmt->id
		this.revStmtMap = new HashMap<Stmt, Integer>();
		for (int sId = 0; sId < stmtList.size(); ++sId)
			revStmtMap.put(stmtList.get(sId), sId);
		
		// For the method's statement(StmtNode) initial the order and exits, if any
		initPredsAndExits();
		
		// Find the parameter
		findFormalParams();
		
		
	}
	//////////////////////////////////////////////////////////
	

	


	////////////////////////Member Functions/////////////////
	public SootMethod getSootMethod() {return this.sm;}
	public Local[] getFormalParams() { return this.formalParams; }
	/**
     * Gets a method in the specified class that overrides the specified method of the base class. ie if M of class BaseClass
     * return MO of subclass SC where MO is the method of SC tht overrides M of baseclass
     * @param sc the soot class
     * @param baseMethod the base method
     * @return a method in the specified class that overrides the specified method of the base class. return null if
     * no override method exists
     */
    public boolean isOverrideSootMethod() {
        List<SootMethod> list = new SootClassWrapper(cls).getAllMethodsInherited();//sc.getMethods();
        for (SootMethod meth : list) { 
            if (meth.getName().equals(sm.getName())) {
                if (meth.getParameterCount() == sm.getParameterCount()) {       
                    if (meth.getParameterCount() == 0) { // special case of a method with no arguments
                        return true;
                    }
                    
                    // method has parameters
                    List<Type> parameterTypeList = meth.getParameterTypes();
                    List<Type> parameterTypeListParent = sm.getParameterTypes();
                    //List<MethodParameter> methParameterList = md.getParameterList();
                    boolean allThesame = true;
                    for (int i=0; i<meth.getParameterCount(); ++i) {
                        if (!parameterTypeList.get(i).toString().equals(parameterTypeListParent.get(i).toString())) {
                            allThesame = false;
                            break;//return meth;
                        }
                    }
                    if (allThesame) {
                        return true;
                    }
                }
            }
        }
        
        if (cls.isAbstract() || cls.isInterface()) {// it is permitted for abstract or iface not to have the method
            return false;
        }
		return false;
        
        
    }
	public HashSet<SootMethod> getAllCalleeMethods(){return this.allCalleeMethods;}
	public String toString() {return this.sm.toString();}
	public CallSite stmtgetCallSite(Stmt stmt){return this.stmtToCallSite.get(stmt);}
	public void addCallerSite(CallSite callersite){this.callerSites.add(callersite);}
	public boolean isOverloadMethod(){return this.isOverloadMethod;}
	public boolean isOverrideMethod(){return this.isOverrideMethod;}
	public int getMethodsCountForName(String methodName,List<SootMethod>methods){
		int methodCount = 0;
		for(SootMethod method:methods){
			if(method.getName().equals(methodName)&&
					!Modifier.isVolatile(sm.getModifiers()))
				methodCount++;
		}
		return methodCount;
	}
	//////////////////////////////////////////////////////////
	
	///////////////////////Analysis////////////////////////////
	/**
	 * Init the preds order and exits for statements
	 */
	private void initPredsAndExits() {
		// TODO Create all statements' order and exits
		// Exit stmt(s) and OUT set(s) initialization
		// Contains all exit points, holding out sets of method
		exitPoints = new ArrayList<Location>();
		
		@SuppressWarnings("rawtypes")
		PatchingChain pchain = sm.retrieveActiveBody().getUnits();
		
		// Compute set of predecessors and successors for each Stmt in Body
		for (Iterator<Stmt> itStmt = stmtList.iterator(); itStmt.hasNext(); ) {
			// Current statement
			Stmt s = itStmt.next();
			
			// Add return stmts to exit points set as we discover them
			boolean isExit = Util.isReturnStmt(s);
			StmtTag sTag = (StmtTag) s.getTag(StmtTag.TAG_NAME);
			// Processing the exit as a special case
			if (isExit)  // Store exit and create new, empty OUT RD set for it
				exitPoints.add(sTag.getLocation());
			// No a exit point
			else {
				// TODO: Handle throw-to-catch edges to identify missing duas and killings 
				
				// Add unit as predecessor of each of its successors (if not an exit unit)
				List<UnitBox> lUBoxes = s.getUnitBoxes(); // Branch targets, if branch stmt
				
				// No target(s); Successor is next unit in code
				if (lUBoxes.isEmpty()) { 
					if (s.fallsThrough()) {
						Stmt sSucc = (Stmt) pchain.getSuccOf(s);
						
						// Get mapped StmtNode of sSucc
						StmtTag sinkStmtTag = (StmtTag)sSucc.getTag(StmtTag.TAG_NAME);
						
						// Set the predecessor statement for sink
						sinkStmtTag.addPredecessorStmt(s);
						sTag.addSuccessorStmt(sSucc);
					}
					else
						assert s instanceof ThrowStmt;
					
					
				}
				// There are branch target(s)
				else{
					if (s.fallsThrough()) { // there is also an immediate successor
						Stmt sSucc = (Stmt) pchain.getSuccOf(s);
						
						// Get mapped StmtNode of sSucc
						StmtTag sinkStmtTag = (StmtTag) sSucc.getTag(StmtTag.TAG_NAME);
						// Set the predecessor statement for sink
						sinkStmtTag.addPredecessorStmt(s);
						sTag.addSuccessorStmt(sSucc);
					}
					
					// For all sub branches
					for (Iterator<UnitBox> itUBox = lUBoxes.iterator(); itUBox.hasNext(); ) {
						UnitBox ubTarget = itUBox.next();
						Stmt sSucc = (Stmt) ubTarget.getUnit();
						
						// Get mapped StmtNode of sSucc
						StmtTag sinkStmtTag = (StmtTag) sSucc.getTag(StmtTag.TAG_NAME);
						// Set the predecessor statement for sink
						sinkStmtTag.addPredecessorStmt(s);
						sTag.addSuccessorStmt(sSucc);
					}
					
				}
			}
			// Print predecessors of current unit
			
		}
		
		// Mark stmts in catch blocks; initial value is true for all stmts, except entry
		Stmt sEntry = (Stmt) pchain.getFirst();
		StmtTag sEntryTag = (StmtTag)sEntry.getTag(StmtTag.TAG_NAME);
		sEntryTag.setInCatchBlock(false);
				
		boolean fixedPoint = false;
		while (!fixedPoint) {
				fixedPoint = true;
					
					for (Iterator<Stmt> itStmt = pchain.iterator(); itStmt.hasNext(); ) {
						Stmt s = itStmt.next();
						StmtTag sTag = (StmtTag)s.getTag(StmtTag.TAG_NAME);
						if (sTag.isInCatchBlock() && sTag.hasPredecessorStmts()) {
							boolean allPredsInCatchBlock = true;
							for (Stmt sPredNode : sTag.getPredecessorStmts()) {
								if (!((StmtTag) sPredNode.getTag(StmtTag.TAG_NAME)).isInCatchBlock()) {
									allPredsInCatchBlock = false;
									break;
								}
							}
							if (!allPredsInCatchBlock) {
								sTag.setInCatchBlock(false);
								fixedPoint = false;
							}
						}
					}
				}
		
	}
	
	/**
	 * Set the formal parameters
	 * Constructs the list of locals corresponding to the method's formal parameters; doesn't include 'this'
	 */
	private void findFormalParams() {
		ArrayList<Local> formals = new ArrayList<Local>();
		PatchingChain pc = sm.retrieveActiveBody().getUnits();
		if (!sm.isStatic()) {  
			// Sanity check for 'this' local (first formal parameter in instance methods)
			IdentityStmt idS = (IdentityStmt) pc.getFirst();
			
			Local t = (Local) idS.getLeftOp();
			
			//assert t.getName().equals("this") || t.getName().equals("r0") || t.getName().equals("l0");
		}
		Stmt s;
		for (Iterator itS = pc.iterator(); itS.hasNext() && (s = (Stmt)itS.next()) instanceof IdentityStmt; ) {
			IdentityStmt is = (IdentityStmt) s;
			formals.add((Local)is.getLeftOp());
		}
		
		formalParams = new Local[formals.size()];
		formalParams = formals.toArray(formalParams);
	}

	/** 
	 * Asks statements to compute call information and return call site, if any 
	 * 
	 * */
	public void initCallSites() {
		// TODO Init call site and set call relation for current method
		
		for (Iterator itStmt = stmtList.iterator(); itStmt.hasNext(); ) {
			// 1. This statement contains a method invocation
			Stmt s =  (Stmt) itStmt.next();
			StmtTag sTag = (StmtTag) s.getTag(StmtTag.TAG_NAME);
			
			// 2. Make statement find its call targets, create call branches, and provide call site
			CallSite cs = null;
			try {
				cs = sTag.initCallSite();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 3. Store call site provided by this statement, but only if not in catch block
			if (!sTag.isInCatchBlock() && cs != null) {
				// Add this call site to callSite
				callSites.add(cs);		
				
				// Add the map from Stmt to the callsite
				stmtToCallSite.put(s,cs);
				allCalleeMethods.addAll(cs.getAllCallees());
				for(SootMethod callee:cs.getAllCallees()){
					((MethodTag) sm.getTag(MethodTag.TAG_NAME)).addCallerSite(cs);
				}
				// DEBUG
				if(verbose){
					System.out.println("Stmt:\t"+s.toString()+"'s callees are");
					if(!allCalleeMethods.isEmpty()){
						Iterator<SootMethod> itcallee = allCalleeMethods.iterator();
						while(itcallee.hasNext()){
							SootMethod appCallee = (SootMethod) itcallee.next();
							System.out.println("\t  Callee:\t"+appCallee.toString());
						}
					}
					else{
						System.err.println("Warning no callee found");
					}
					System.out.println();
				}
				// FINISH
			}			
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return TAG_NAME;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<CallSite> getAllCallerSites() {
		// TODO Auto-generated method stub
		return callerSites;
	}

	
	//////////////////////////////////////////////////////////

}
