package vreAnalyzer.ControlFlowGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CFGNodeSpecial;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.CallSiteTag;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.Util.Util;

public class CFG {
	
	///////////////////////Field//////////////////////////////////
	protected SootMethod method;
	protected SootClass sootcls;
	//CFG nodes
	protected ArrayList<CFGNode> nodes = new ArrayList<CFGNode>();
	private boolean containBraches = false;
	// Statement to CFGNode
	protected Map<Stmt,CFGNode> stmtToCFGNode = new HashMap<Stmt, CFGNode>();
	
	// CFGNode to id
	protected Map<CFGNode,Integer> nodeIds = new HashMap<CFGNode, Integer>();

	// Special ENTRY node, unique for this CFG
	public CFGNode ENTRY; 
	
	// Special EXIT node, unique for this CFG
	public CFGNode EXIT; 
	
	private static String ENTRY_NAME = "EN";
	private static String EXIT_NAME = "EX";
	
	
	//CFG
	//Entry branch
	protected Branch entryBranch = null;
	
	/** Register of created branches for this CFG */
	protected HashSet<Branch> branches = new HashSet<Branch>();
	
	private Set<CFG> callgraphSuccs = new HashSet<CFG>();
	private Set<CFG> callgraphPreds = new HashSet<CFG>();
	/////////////////////////////////////////////////////////////
	
	///////////////////////Constructors//////////////////////////
	/**
	 * The CFG constructor for the MethodNode
	 * @param method
	 */
	public CFG(SootMethod method) {
		// TODO Create a CFG for the mNode
		this.method = method;
		this.sootcls = method.getDeclaringClass();
		createNodesPredsSuccs();
		
	}
	protected CFGNode instantiateNode(StmtTag s) { return new CFGNode(s,this.method); }
	////////////////////////Member Functions/////////////////////////////////////
	public List<CFGNode> getNodes() { return nodes; }
	public SootMethod getMethod() {return method;}
	public SootClass getSootClass() {return sootcls;}
	public Branch getEntryBranch() { return entryBranch; }
	/** Returns first real node (i.e., the successor of ENTRY); EXIT if there are no real nodes */
	public CFGNode getFirstRealNode() { return ENTRY.getSuccs().get(0); }
	/** Returns first real node that is not an ID node; EXIT if there no non-id node */
	public CFGNode getFirstRealNonIdNode() {
		for (CFGNode n : nodes) {
			if (n != ENTRY && !(n.getStmt() instanceof IdentityStmt))
				return n;
		}
		assert false; // at least there should be EXIT
		return null;
	}
	public int getIndexId(CFGNode node){ return nodeIds.get(node);}
	//////////////////////////////////////////////////////////////
	
	
	/////////////////////Analysis///////////////////////////////
	/**
	 * Creates all CFG nodes, and sets successors and predecessors for each node.
	 * 
	 * *** Right now, THROW statements are NOT considered as returns, hence they don't have an edge to EXIT.
	 * *** THROWs just have no successors.
	 * 
	 *  
	 * @param m Method for which to create nodes
	 */
	private void createNodesPredsSuccs() {
		// TODO Auto-generated method stub
		this.ENTRY = new CFGNodeSpecial(ENTRY_NAME,this.method); // special ENTRY node
		this.EXIT = new CFGNodeSpecial(EXIT_NAME,this.method); // special EXIT node
		
		// Patching chain of the SootMethod
		PatchingChain<Unit> pchain = method.retrieveActiveBody().getUnits();
		MethodTag mNodeTag = (MethodTag) method.getTag(MethodTag.TAG_NAME);
			
		// First of all, create a node for each stmt
		int nId = 0;
		nodes.add(this.ENTRY);
		nodeIds.put(this.ENTRY, nId++);
		for (Iterator<Unit> itStmt = pchain.iterator(); itStmt.hasNext(); ) {
			Stmt s = (Stmt) itStmt.next();
			
			StmtTag sTag = (StmtTag) s.getTag(StmtTag.TAG_NAME);
			CFGNode n = instantiateNode(sTag); // New node using factory method
			
			// Binding the CFGNode to callsite,if any
			if(s.hasTag(CallSiteTag.TAG_NAME)){
				// Get the CallSite along with this statement and set the created CFGNode to this CallSite 
				if(mNodeTag.stmtgetCallSite(s)!=null)
					mNodeTag.stmtgetCallSite(s).setCFGNode(n);
			}
			
			nodes.add(n);
			stmtToCFGNode.put(s, n);
			nodeIds.put(n, nId++);
		}
		nodes.add(this.EXIT);
		nodeIds.put(this.EXIT, nId);
		
		// Compute set of predecessors and successors for each Stmt in Body
		if (pchain.isEmpty()) {  // If no stmts, just link ENTRY and EXIT
			ENTRY.addSucc(EXIT);
			EXIT.addPred(ENTRY);
		}
		else {
			// First, link virtual ENTRY to first stmt
			Stmt firstStmt = (Stmt) pchain.getFirst();
			CFGNode nFirst = stmtToCFGNode.get(firstStmt);
			ENTRY.addSucc(nFirst);
			nFirst.addPred(ENTRY);
					
			// Link each remaining node to its successors (or EXIT if no successors)
			for (Iterator<Unit> itStmt = pchain.iterator(); itStmt.hasNext(); ) {
				// Create CFG node for stmt
				Stmt s = (Stmt) itStmt.next();
				
				CFGNode n = stmtToCFGNode.get(s);
						
				// Add return stmts to exit points set as we discover them
				if (Util.isReturnStmt(s)) {
					n.addSucc(EXIT);
					EXIT.addPred(n);
				}
				else {
					// TODO: Handle throw-to-catch edges to identify missing duas and kills
							
					// Add unit as predecessor of each of its successors (if not an exit unit)
					List<UnitBox> lUBoxes = s.getUnitBoxes(); // branch targets, if branch stmt
					if (lUBoxes.isEmpty()) { // no target(s); successor is next unit in code
						if (s.fallsThrough()) {
							Stmt sSucc = (Stmt) pchain.getSuccOf(s);
							
							CFGNode nSucc = stmtToCFGNode.get(sSucc);
							nSucc.addPred(n);
							n.addSucc(nSucc);
							n.setFallThroughTgt(nSucc);
						}
						else
							assert s instanceof ThrowStmt;
					}
					else { // There are branch target(s)
						if (s.fallsThrough()) { // there is also an immediate successor
							Stmt sSucc = (Stmt) pchain.getSuccOf(s);
							
							CFGNode nSucc = stmtToCFGNode.get(sSucc);
							nSucc.addPred(n);
							n.addSucc(nSucc);
							n.setFallThroughTgt(nSucc);
						}
						for (Iterator<UnitBox> itUBox = lUBoxes.iterator(); itUBox.hasNext(); ) {
							UnitBox ubTarget = (UnitBox) itUBox.next();
							assert ubTarget.isBranchTarget();
							Stmt sSucc = (Stmt) ubTarget.getUnit();
							
							CFGNode nSucc = stmtToCFGNode.get(sSucc);
							nSucc.addPred(n);
							n.addSucc(nSucc);
						}
					}
				}
			}
		} 
		
	}
	
	/**
	 * analysis inside current CFG
	 * Must be called for each CFG after all CFGs have been created. 
	 * Performs initial specialized analysis that might require existence of all CFGs.
	 * Creates all branches, except for nodes in catch blocks (for now, at least).
	 * Determines successors and predecessors in method-level call graph. 
	 **/
	public void analyze() {
		// TODO Auto-generated method stub
		// Add special call branch for method
		entryBranch = getCreateBranch(null, ENTRY);
		
		// Look for branches and callees
		for (CFGNode n : getNodes()) {
			if (n.isInCatchBlock())
				continue; // for now, at least...
			
			// Create branches
			if (n.getSuccs().size() > 1) {
				// Create and store branches to successors (avoid multiple brs for same successor)
				List<CFGNode> succList;
				if(!containBraches){
					containBraches = true;
				}
				
				succList = new ArrayList<CFGNode>(); // remove repeated successor nodes
				for (CFGNode nSucc : n.getSuccs())
					if (!succList.contains(nSucc))
						succList.add(nSucc);
				
				
				for (CFGNode nSucc : succList) {
					// create branch to successor, adding it to out list of this node
					Branch brToSucc = getCreateBranch(n, nSucc);
					n.addOutBranch(brToSucc);
				}
			}
			
			// Link to callees (both directions)
			if (n.getStmt() instanceof InvokeStmt || (n.getStmt() instanceof AssignStmt && ((AssignStmt)n.getStmt()).getRightOp() instanceof InvokeExpr)) {
				CallSite cs = n.getStmtTag().getCallSite();
				if (cs != null) {
					Iterator<SootMethod> itmethodNode = cs.getAllCallees().iterator();
					while(itmethodNode.hasNext()) {
						SootMethod mNodeAppCallee = itmethodNode.next();
						
						// We only set the callee's CFG of application method
						if(mNodeAppCallee.hasTag(MethodTag.TAG_NAME)){
							CFG cfgCallee = ProgramFlowBuilder.inst().getCFG(mNodeAppCallee);
							callgraphSuccs.add(cfgCallee);
							cfgCallee.callgraphPreds.add(this);
						}
						
					}
				}
			}
		}
	
		
	}
	
	// DEBUG staff
	public void traverse(){
		CFGNode start = ENTRY;
		tranverseCurrent(start);
	}
	public void tranverseCurrent(CFGNode curr){
		System.out.println(curr);
		if((curr.toString()).equals("EX")){
			return;
		}else{
			if(!curr.getSuccs().isEmpty()){
				for(CFGNode cusub:curr.getSuccs()){
					tranverseCurrent(cusub);
				}
			}else
				return;
		}
	}
	// FINISH
	
	///////////////////////Supporting/////////////////////////////////
	/** Creates, registers and returns new branch */
	private Branch getCreateBranch(CFGNode src, CFGNode tgt) {
		Branch brNew = new Branch(src, tgt);
		
		if (branches.contains(brNew)) {
			// Find existing branch object in list, and return it
			for (Branch br : branches)
				if (br.equals(brNew))
					return br;
			assert false; // shouldn't get here
		}
		branches.add(brNew);
		return brNew;
	}
	public List<CFGNode> getTails() {
		// TODO Auto-generated method stub
		return EXIT.getPreds();
	}
	public List<CFGNode> getHeads() {
		// TODO Auto-generated method stub
		return ENTRY.getSuccs();
	}
	public boolean containsBraches(){
		return containBraches;
	}
	
	
}
