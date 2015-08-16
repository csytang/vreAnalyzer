package vreAnalyzer.ProgramFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.FastHierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Reuse.Normal.Pipeline.NormalPipelines;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.Util.Options;
import vreAnalyzer.Util.Options.reusableMode;



public class ProgramFlowBuilder {
	
	
	//////////////////////////////Field////////////////////////////////
	/** Holds singleton instance */
	private static ProgramFlowBuilder cgSingleton = null;
	/** Returns singleton instance */
	public static ProgramFlowBuilder inst() { return cgSingleton; }
	
	private List<SootClass> appClasses = null;
	
	
	/** All application concrete methods, reachable or not */
	private List<SootMethod> allAppMethods = null;
	private List<SootMethod> entryMethods = null;
	
	// Map from method to CFG
	private HashMap<SootMethod,CFG> mToCFG = null;
	
	private List<CFG> allCFGs = null;
	
	/** Only maps reachable nodes to CFGs */
	private HashMap<CFGNode,CFG> nodeToCFG = null; // built on demand
	
	
	/** Unique index for each method (same order for any execution) */
	private Map<SootMethod,Integer> mToId = new HashMap<SootMethod, Integer>();
	
	private boolean verbose = true;
	//////////////////////////////////////////////////////////////

	
	/////////////////////////////Constructor////////////////////////////

	public ProgramFlowBuilder(){
	}
	
	//////////////////////////////////////////////////////////////
	
	//////////////////////Member functions//////////////////////////
	public int getContainingMethodIdx(CFGNode src) {return getMethodIdx(getContainingCFG(src).getMethod());}
	public int getMethodIdx(SootMethod m) { return mToId.get(m); }
	public CFG getContainingCFG(CFGNode n) {
		// build map on demand
		if (nodeToCFG == null) {
			nodeToCFG = new HashMap<CFGNode, CFG>();
			for (CFG cfg : allCFGs) {
				for (CFGNode _n : cfg.getNodes())
					nodeToCFG.put(_n,cfg);
			}
		}
		
		return nodeToCFG.get(n);
	}
	public List<SootMethod> getEntryMethods(){return entryMethods;}
	////////////////////////////////////////////////////////////////
	
	//////////////////////////Instance Creator//////////////////////////
	public static void createInstance() throws EntryNotFoundException{
		cgSingleton = new ProgramFlowBuilder();
		
		cgSingleton.initMethods();
		cgSingleton.analyzeCFGs();
		
	}
	
	
	/**
	 * Init all methods and call graphs, control flow graph
	 * @throws EntryNotFoundException
	 */
	private void initMethods() throws EntryNotFoundException{
		this.allAppMethods = getAppConcreteMethods();
		this.entryMethods = findEntryAppMethods();
		 
		
		// Init MethodNode to CFG mapping
		mToCFG = new HashMap<SootMethod,CFG>();
		
		allCFGs = new LinkedList<CFG>();
		
		
		// Purely intraprocedural initialization
		// For all application Soot classes:
		// Find contextual defs and uses for each Stmt in each Method
		// Determine local kill summary for each Method
		// For each Soot Method: list defs, uses and kills
		createAppContexts();
		
	    
	}
	
	
	/**
	 * {@link MethodNode}
	 * 1. Use SootMethods to create MethodNode
	 * 2. Set the CallSite/Call relation within the MethodNode
	 */
	private void createAppContexts() {
		// TODO Initialization requiring previously computed intra-procedural info
		int mIdx = 0;
		for (SootMethod m : allAppMethods) {
			
			// Compute initial (local) reaching ctx calls in method
			MethodTag mNode = new MethodTag(m);
			m.addTag(mNode);
			
			// Add this method to its mapping id
			mToId.put(m, mIdx++);
			
			//System.out.println();
		}
		for (SootMethod m: allAppMethods){
			// Initial Call Site and set call relation
			MethodTag mNode = (MethodTag) m.getTag(MethodTag.TAG_NAME);
			mNode.initCallSites();
			
			// Create control flow graph (CFG) for this method
			initCFG(m);
			
		}
		
	}
	
	/**
	 * Create CFGs for each MethodNode
	 */
	public void initCFG(SootMethod m){
		if(Options.allowParmsRetUseDefs()){
			CFGDefUse dfcfg = new CFGDefUse(m);
			allCFGs.add(dfcfg);
			mToCFG.put(m, dfcfg);
		}
		else{
			CFG cfg = new CFG(m);
			allCFGs.add(cfg);
			mToCFG.put(m, cfg);
		}
	}
	
	
	
	/**
	 * Analysis CFGs
	 */
	public void analyzeCFGs(){
		for(CFG cfg:allCFGs){
			if(cfg instanceof CFGDefUse){
				CFGDefUse defcfg = (CFGDefUse)cfg;
				defcfg.analyze();
			}
			else
				cfg.analyze();
			
		}
	}
	
	
	// DEBUG staff
	public void printCFGs(){
		for(CFG cfg:allCFGs){
			cfg.traverse();
		}
	}
	// FINISH
	
	public CFG getCFG(SootMethod mNodeAppCallee) {
		// TODO Auto-generated method stub
		return this.mToCFG.get(mNodeAppCallee);
	}
	
	
	public SootClass findAppClassByNameAndSuper(String name,SootClass superClass){
		
		FastHierarchy fhierarchy = Scene.v().getOrMakeFastHierarchy();
		
		for(SootClass ap:appClasses){
			if(ap.getName().equals(name) &&
					fhierarchy.isSubclass(ap, superClass)){
				return ap;
			}
		}
		return null;
	}
	//////////////////////////////////////////////////////////////

	
	
	
	
	
	/////////////////////////Analysis/////////////////////////////////////
	/** Gets/creates collection of all application classes */
	public List<SootClass> getAppClasses() {
		if (appClasses == null) {
			appClasses = new ArrayList<SootClass>();
			for (Iterator<SootClass> itCls = Scene.v().getApplicationClasses().iterator();itCls.hasNext();){
				SootClass appClass = itCls.next();
				if(Options.allowReuableChecking()){
					
					reusableMode mode = Options.getMode();
					
					// detect reusable asset in normal mode
					if(mode == reusableMode.Normal){
						Set<String> allComparedClass = Options.getComparedinNormal();
						if(allComparedClass!=null && allComparedClass.contains(appClass.getName())){
							NormalPipelines.inst().createNewSingleNormal(appClass);
						}
					}
					
					// Other models has been redirected to other patch code 
				}
				
				appClasses.add(appClass);
			}
		}
		
		return appClasses;
	}
	/** Gets/creates collection of all application classes */
	
	
	
	
	/** Gets/creates collection of all concrete methods (i.e., methods with a body) in application classes */
	public List<SootMethod> getAppConcreteMethods() {
		if (allAppMethods == null) {
			allAppMethods = new ArrayList<SootMethod>();
			List<SootClass> appClasses = getAppClasses();
			for (SootClass cls : appClasses) {
				
				for (Iterator<SootMethod> itMthd = cls.getMethods().iterator(); itMthd.hasNext();) {
					SootMethod m = itMthd.next();
					if (!m.isAbstract() && m.toString().indexOf(": java.lang.Class class$") == -1){
						allAppMethods.add(m);
						
					}
				}
			}
			// sort list of all methods
			Collections.sort(allAppMethods, new MethodComparator());
		}
		return allAppMethods;
	}
	
	private static final String mainSubsig = "void main(java.lang.String[])";
	private List<SootMethod> findEntryAppMethods() throws EntryNotFoundException {
		if (entryMethods != null)
			return entryMethods;
		entryMethods = new ArrayList<SootMethod>();
		
		// Get main class, and test classes
		String entryClassName = Options.entryClassName();
		
		// Search for all app class with a 'main' if no entry or test classes were provided
		if (entryClassName == null) {
			for (SootClass cls : ProgramFlowBuilder.inst().getAppClasses()) {
				try {
					entryMethods.add(cls.getMethod(mainSubsig));
				}
				catch (Exception e) { }
			}
			if (entryMethods.isEmpty()){
				throw new EntryNotFoundException("No 'main' found in app classes");
			}
		}
		
		// Given entry class's 'main' is the first entry method in list
		if (entryClassName != null) {
			// Find entry class matching given name
			for (SootClass cls : ProgramFlowBuilder.inst().getAppClasses()) {
				if (cls.getName().equals(entryClassName)) {
					// Find main method in entry class
					try {
						SootMethod mEntry = cls.getMethod(mainSubsig);
						entryMethods.add(mEntry);
						break;
					}
					catch (Exception e) {
						throw new EntryNotFoundException("Entry class " + entryClassName + " has no 'main'");
					}
				}
			}
			if (entryMethods.isEmpty())
				throw new EntryNotFoundException("Entry class name " + entryClassName + " not found");
		}
		
		
		assert !entryMethods.isEmpty();
		return entryMethods;
	}
	
	
	//////////////////////////////////////////////////////////////
	/** Comparator to find unique ordering of methods based on lexical order */
	private static class MethodComparator implements Comparator<SootMethod> {
		public int compare(SootMethod o1, SootMethod o2) {
			return o1.toString().compareTo(o2.toString());
		}
	}
	
	public static class EntryNotFoundException extends Exception {
		
		private static final long serialVersionUID = -9085832646954189629L;

		public EntryNotFoundException(String msg) { super(msg); }
	}

	public SootClass getEntryClass() {
		// TODO Auto-generated method stub
		return entryMethods.get(0).getDeclaringClass();
	}

	public SootClass findLibClassByName(String string) {
		// TODO Auto-generated method stub
		for (Iterator<SootClass> itCls = Scene.v().getLibraryClasses().iterator();itCls.hasNext();){
			SootClass libClass = itCls.next();
			if(libClass.getName().equals(string))
				return libClass;
		}
		return null;
	}
	
	
}
