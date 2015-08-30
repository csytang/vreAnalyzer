package vreAnalyzer.PointsTo;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import soot.ArrayType;
import soot.Local;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.AnyNewExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JNewArrayExpr;
import soot.jimple.internal.JNewExpr;
import vreAnalyzer.Context.Context;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.SootPresentation.DefaultJimpleRepresentation;


public class PointsToAnalysis extends InterProceduralAnalysis<SootMethod,CFGNode,PointsToGraph>{
	
	////////////////////////////Field//////////////////////////////////
	
	// A set of classes whose static initialisers have been processed. 
	public static Set<SootClass> clinitCalled;
	
	/**
	 * A shared points-to graph that maintains information about objects
	 * reachable from static fields (modelled as fields of a dummy global variable).
	 * 
	 * <p>For static load/store statements, we union this points-to graph with the
	 * points-to graph in the flow function, perform the operation, and then
	 * separate stuff out again.</p>
	 */
	private PointsToGraph staticHeap;
	
	// A self constructed dummy method
	private static final SootMethod DUMMY_METHOD = new SootMethod("DUMMY_METHOD", Collections.EMPTY_LIST, Scene.v().getObjectType());
	
	// A stack holds all context to analysis
	private boolean verbose = false;
	protected Stack<Context<SootMethod,CFGNode,PointsToGraph>> analysisStack;
	
	///////////////////////////////////////////////////////////////////
	
	/////////////////////////Constructor///////////////////////////////
	private static PointsToAnalysis ptAnalysis;
	
	public static PointsToAnalysis inst(){
		if(ptAnalysis==null){
			ptAnalysis = new PointsToAnalysis();
		}
		return ptAnalysis;
	}
	
	public PointsToAnalysis() {
		super();
		
		
		// Create an empty analysis stack
		this.analysisStack = new Stack<Context<SootMethod,CFGNode,PointsToGraph>>();
		// No classes statically initialised yet
		this.clinitCalled = new HashSet<SootClass>();
		// Create a static points-to graph with a single "global" root object
		this.staticHeap = topValue();
		this.staticHeap.assignNew(PointsToGraph.GLOBAL_LOCAL, PointsToGraph.GLOBAL_SITE);
	}
	
	/////////////////////////////////////////////////////////////////
	
	////////////////////////Analysis///////////////////////////////////
	@Override
	public void doAnalysis() {
		// TODO Auto-generated method stub
		
		// Initialise the MAIN context
		
		for (SootMethod entryPoint : programRepresentation().getEntryPoints()) {
			Context<SootMethod,CFGNode,PointsToGraph> context = new Context<SootMethod,CFGNode,PointsToGraph>(entryPoint, programRepresentation().getControlFlowGraph(entryPoint));
			
			PointsToGraph boundaryInformation = boundaryValue(entryPoint);	
			
			initContext(context, boundaryInformation);
		}
		
		// Stack-of-work-lists data flow analysis.
		while (!analysisStack.isEmpty()) {
			// Get the context at the top of the stack.
			Context<SootMethod,CFGNode,PointsToGraph> context = analysisStack.peek();
			
			
			// Either analyse the next pending unit or pop out of the method
			if (!context.getWorkList().isEmpty()) {
				// work-list contains items; So the next unit to analyse.
				CFGNode unit = context.getWorkList().pollFirst();//Remove the first to analysis
				if(verbose)
					System.out.println("Stmt:"+unit.toString());
				if (unit.toString()!="EX") {
					
					// Compute the IN data flow value (only for non-entry units).
					List<CFGNode> predecessors = unit.getPreds();
					
					if (predecessors.size() != 0) {
						// Merge all the OUT values of the predecessors
						Iterator<CFGNode> predIterator = predecessors.iterator();
						
						// Initialise IN to the OUT value of the first predecessor
						PointsToGraph in = context.getValueAfter(predIterator.next());
						if(verbose){
							System.out.println("In P2Graph is");
							System.out.println(in);
						}
						// Then, merge OUT of remaining predecessors with the
						// intermediate IN value
						while (predIterator.hasNext()) {
							PointsToGraph predOut = context.getValueAfter(predIterator.next());
							in = meet(in, predOut);
						}
						
						// Set the IN value at the context
						context.setValueBefore(unit, in);
					}
					
					// Store the value of OUT before the flow function is processed.
					PointsToGraph prevOut = context.getValueAfter(unit);
					
					// Get the value of IN 
					PointsToGraph in = context.getValueBefore(unit);

					// Now perform the flow function.
					PointsToGraph out = flowFunction(context, unit, in);
					
					// If the result is null, then no change 
					if (out == null){
						out = prevOut;
					}
					// Set the OUT value
					context.setValueAfter(unit, out);
					
					if(verbose){
						System.out.println("Out P2Graph is");
						System.out.println(out);
						System.out.println("------------------------");
					}
						
					
				
				}
				else{
					// NULL unit, which means the end of the method.
					assert (context.getWorkList().isEmpty());
					
					// Exit flow value is the merge of the OUTs of the tail nodes.
					PointsToGraph exitFlow = topValue();
					for (CFGNode tail : context.getControlFlowGraph().getTails()) {
						PointsToGraph tailOut = context.getValueAfter(tail);
						exitFlow = meet(exitFlow, tailOut);
					}
					// Set the exit flow of the context.
					context.setExitValue(exitFlow);
					
					// Mark this context as analysed at least once.
					context.markAnalysed();
				}
			}
			else {
				// If work-list is empty, then remove it from the analysis.
				
				analysisStack.remove(context);
			}
			
			
		}
		// Sanity check
		for (List<Context<SootMethod, CFGNode, PointsToGraph>> contextList : contexts.values()) {
			for (Context context : contextList) {
				if(verbose){
					System.out.println("The exit points of method\t "+context.getMethod()+" \tto is:");
					System.out.println(context.getExitValue());
				}
				if (context.isAnalysed() == false) {
					System.err.println("*** ATTENTION ***: Only partial analysis of X" + context +" " + context.getMethod());
				}
			}			
		}
		
	}
	
	///////////////////////////////////////////////////////////////////

	/**
	 * Creates a new context and initialises data flow values.
	 * 
	 * <p>
	 * The following steps are performed:
	 * <ol>
	 * <li>Initialise all nodes to default flow value (lattice top).</li>
	 * <li>Initialise the entry nodes (heads) with a copy of the entry value.</li>
	 * <li>Add entry points to work-list.</li>
	 * <li>Push this context on the top of the analysis stack.</li>
	 * </ol>
	 * </p>
	 * 
	 * @param context the context to initialise
	 * @param entryValue the data flow value at the entry of this method
	 */

	private void initContext(Context<SootMethod, CFGNode, PointsToGraph> context,PointsToGraph entryValue) {
		// TODO Auto-generated method stub
		// Initialise the MAIN context
		// Get the method
		SootMethod method = context.getMethod();

		// First initialise all points to default flow value.
		for (CFGNode unit : context.getControlFlowGraph().getNodes()) {
			context.setValueBefore(unit, topValue());
			context.setValueAfter(unit, topValue());
		}

		// Now, initialise entry points with a copy of the given entry flow.
		context.setEntryValue(copy(entryValue));
		context.setValueBefore(context.getControlFlowGraph().ENTRY, null);
		context.setValueAfter(context.getControlFlowGraph().ENTRY, copy(entryValue));
		for (CFGNode unit : context.getControlFlowGraph().getHeads()) {
			context.setValueBefore(unit, copy(entryValue));
			// Add entry points to work-list
			if(verbose){
				System.out.println("Set value before:\t"+unit+"\tis");
				System.out.println(entryValue);
			}
			context.getWorkList().add(unit);
		}

		// Add this new context to the given method's mapping.
		if (!contexts.containsKey(method)) {
			contexts.put(method, new LinkedList<Context<SootMethod,CFGNode,PointsToGraph>>());
		}
		contexts.get(method).add(context);

		// Push this context on the top of the analysis stack.
		analysisStack.add(context);
		
	}
	
	/**
	 * Performs operations on points-to graphs depending on the statement inside
	 * a CFG node. 
	 */
	protected PointsToGraph flowFunction(Context<SootMethod,CFGNode,PointsToGraph> context, CFGNode unit, PointsToGraph in) 
	{
		

		// First set OUT to copy of IN (this is default for most statements).
		PointsToGraph out = new PointsToGraph(in);

		// This analysis is written assuming that units are statements (and not,
		// for example, basic blocks)
		
		Stmt stmt = (Stmt) unit.getStmt();

		// What kind of statement?
		if (stmt instanceof DefinitionStmt) {
			// Assignment of LHS to an RHS
			Value lhsOp = ((DefinitionStmt) stmt).getLeftOp();
			Value rhsOp = ((DefinitionStmt) stmt).getRightOp();

			// Invoke static initialisers if static members accessed
			// for the first time
			StaticFieldRef staticReference = null;
			if (lhsOp instanceof StaticFieldRef) {
				staticReference = ((StaticFieldRef) lhsOp);				
			} else if (rhsOp instanceof StaticFieldRef) {
				staticReference = ((StaticFieldRef) rhsOp);
			}
			if (staticReference != null) {
				SootClass declaringClass = staticReference.getField().getDeclaringClass();
				if (clinitCalled.contains(declaringClass) == false) {
					clinitCalled.add(declaringClass);
					// Don't initialise library classes
					if (declaringClass.isLibraryClass()) {
						// Set all static fields to null
						for (SootField field : declaringClass.getFields()) {
							// Only for static reference fields
							if (field.isStatic() && field.getType() instanceof RefLikeType) {
								staticHeap.setFieldSummary(PointsToGraph.GLOBAL_LOCAL, field);
							}
						}
					} 
				}
			}
			

			// Handle statement depending on type
			if (lhsOp.getType() instanceof RefLikeType) { 
				// Both LHS and RHS are RefLikeType
				if (lhsOp instanceof InstanceFieldRef || lhsOp instanceof ArrayRef) {
					// SETFIELD
					Local lhs = (Local)(lhsOp instanceof InstanceFieldRef ?
							 	((InstanceFieldRef) lhsOp).getBase() : 
								((ArrayRef) lhsOp).getBase());
					SootField field = lhsOp instanceof InstanceFieldRef ?
							((InstanceFieldRef) lhsOp).getField() : PointsToGraph.ARRAY_FIELD;
					
					// RHS can be a local or constant (string, class, null)
					if (rhsOp instanceof Local) {
						Local rhs = (Local) rhsOp;
						out.setField(lhs, field, rhs);
					} else if (rhsOp instanceof Constant) {
						Constant rhs = (Constant) rhsOp;
						out.setFieldConstant(lhs, field, rhs);
					} else {
						throw new RuntimeException(rhsOp.toString());
					}
				} else if (rhsOp instanceof InstanceFieldRef || rhsOp instanceof ArrayRef) {
					// GETFIELD
					Local rhs = (Local)(rhsOp instanceof InstanceFieldRef ?
						 	((InstanceFieldRef) rhsOp).getBase() : 
							((ArrayRef) rhsOp).getBase());
					SootField field = rhsOp instanceof InstanceFieldRef ?
							((InstanceFieldRef) rhsOp).getField() : PointsToGraph.ARRAY_FIELD;
							
					// LHS has to be local
					if (lhsOp instanceof Local) {
						Local lhs = (Local) lhsOp;
						out.getField(lhs, rhs, field);
					} else {
						throw new RuntimeException(lhsOp.toString());
					}
				} else if (rhsOp instanceof AnyNewExpr) {
					// NEW, NEWARRAY or NEWMULTIARRAY
					AnyNewExpr anyNewExpr = (AnyNewExpr) rhsOp;
					if (lhsOp instanceof Local) {
						Local lhs = (Local) lhsOp;
						out.assignNew(lhs, anyNewExpr);
					} else {
						throw new RuntimeException(lhsOp.toString());						
					}
				} else if (rhsOp instanceof InvokeExpr) {
					// STATICINVOKE, SPECIALINVOKE, VIRTUALINVOKE or INTERFACEINVOKE
					InvokeExpr expr = (InvokeExpr) rhsOp;	
					// Handle method invocation!
					out = handleInvoke(context, unit, expr, in);
				} else if (lhsOp instanceof StaticFieldRef) { 
					// Get parameters
					SootField staticField = ((StaticFieldRef) lhsOp).getField();
					// Temporarily union locals and globals
					PointsToGraph tmp = topValue();
					tmp.union(out, staticHeap);
					// Store RHS into static field
					if (rhsOp instanceof Local) {
						Local rhsLocal = (Local) rhsOp;
						tmp.setField(PointsToGraph.GLOBAL_LOCAL, staticField, rhsLocal);
					} else if (rhsOp instanceof Constant) {
						Constant rhsConstant = (Constant) rhsOp;
						tmp.setFieldConstant(PointsToGraph.GLOBAL_LOCAL, staticField, rhsConstant);
					} else {
						throw new RuntimeException(rhsOp.toString());
					}
					// Now get rid of all locals, params, etc.
					Set<Local> locals = new HashSet<Local>(tmp.roots.keySet());
					for (Local local : locals) {
						// Everything except the GLOBAL must go!
						if (local != PointsToGraph.GLOBAL_LOCAL) {
							tmp.kill(local);
						}
					}
					// Global information is updated!
					staticHeap = tmp;
					
				} else if (rhsOp instanceof StaticFieldRef) {
					// Get parameters
					Local lhsLocal = (Local) lhsOp;
					SootField staticField = ((StaticFieldRef) rhsOp).getField();
					// Temporarily union locals and globals
					PointsToGraph tmp = topValue();
					tmp.union(out, staticHeap);
					// Load static field into LHS local
					tmp.getField(lhsLocal, PointsToGraph.GLOBAL_LOCAL, staticField);
					// Now get rid of globals that we do not care about
					tmp.kill(PointsToGraph.GLOBAL_LOCAL);
					// Local information is updated!
					out = tmp;			
					
				}  else if (rhsOp instanceof CaughtExceptionRef) { 
					Local lhs = (Local) lhsOp;
					out.assignSummary(lhs);
				} else if (rhsOp instanceof IdentityRef) { 
					// Ignore identities
				} else if (lhsOp instanceof Local) {
					// Assignment
					Local lhs = (Local) lhsOp;
					// RHS op is a local, constant or class cast
					if (rhsOp instanceof Local) {
						Local rhs = (Local) rhsOp;
						out.assign(lhs, rhs);
					} else if (rhsOp instanceof Constant) {
						Constant rhs = (Constant) rhsOp;
						out.assignConstant(lhs, rhs);
					} else if (rhsOp instanceof CastExpr) { 
						Value op = ((CastExpr) rhsOp).getOp();
						if (op instanceof Local) {
							Local rhs = (Local) op;
							out.assign(lhs, rhs);
						} else if (op instanceof Constant) {
							Constant rhs = (Constant) op;
							out.assignConstant(lhs, rhs);
						} else {
							throw new RuntimeException(op.toString());
						}
					} else {
						throw new RuntimeException(rhsOp.toString());
					}
				} else {
					throw new RuntimeException(unit.toString());
				}
			} else if (rhsOp instanceof InvokeExpr) {
				// For non-reference types, only method invocations are important
				InvokeExpr expr = (InvokeExpr) rhsOp;	
				// Handle method invocation!
				out = handleInvoke(context, unit, expr, in);
			} 

		} else if (stmt instanceof InvokeStmt) {
			// INVOKE without a return
			InvokeExpr expr = stmt.getInvokeExpr();
			// Handle method invocation!
			out = handleInvoke(context, unit, expr, in);
		} else if (stmt instanceof ReturnStmt) {
			// Returning a value (not return-void as those are of type ReturnVoidStmt)
			Value op = ((ReturnStmt) stmt).getOp();
			Local lhs = PointsToGraph.RETURN_LOCAL;
			// We only care about reference-type returns
			if (op.getType() instanceof RefLikeType) {
				// We can return a local or a constant
				if (op instanceof Local) {
					Local rhs = (Local) op;
					out.assign(lhs, rhs);
				} else if (op instanceof Constant) {
					Constant rhs = (Constant) op;
					out.assignConstant(lhs, rhs);
				} else {
					throw new RuntimeException(op.toString());
				}				
			}
		}

		return out;
	}
	public void updateSuccessPointerwithNewExprs(List<Context<SootMethod,CFGNode,PointsToGraph>> contextsList,CFGDefUse cfg,CFGNode cfgNode,Local local,JNewExpr argsExpr){
		List<CFGNode>allnodes = cfg.getNodes();
		int startidx = cfg.getIndexId(cfgNode);
		for(int i = startidx;i < allnodes.size();i++){
			CFGNode current = allnodes.get(i);
			for(Context context:contextsList){
				PointsToGraph p2gbefore = (PointsToGraph)context.getValueBefore(current);
				PointsToGraph p2gafter = (PointsToGraph)context.getValueAfter(current);
				Set<AnyNewExpr> newexprsbf = p2gbefore.getRoots().get(local);
				if(newexprsbf==null){
					p2gbefore.assignNew(local, argsExpr);
				}else if(newexprsbf.size()==0){
					p2gbefore.assignNew(local, argsExpr);
				}
				Set<AnyNewExpr> newexprsaf = p2gafter.getRoots().get(local);
				if(newexprsaf==null){
					p2gafter.assignNew(local, argsExpr);
				}else if(newexprsaf.size()==0){
					p2gafter.assignNew(local, argsExpr);
				}
			}
		}
		for(Context context:contextsList){
			PointsToGraph p2exit = (PointsToGraph)context.getEntryValue();
			Set<AnyNewExpr> exitexpr = p2exit.getRoots().get(local);
			if(exitexpr==null){
				p2exit.assignNew(local, argsExpr);
				context.setExitValue(p2exit);
			}else if(exitexpr.size()==0){
				p2exit.assignNew(local, argsExpr);
				context.setExitValue(p2exit);
			}
		}
	}
	public PointsToGraph topValue() {
		return new PointsToGraph();
	}

	/**
	 * Returns a points-to graph with the locals of main initialised to
	 * <tt>null</tt>, except the command-line arguments which are
	 * initialised to an array of strings.
	 * 
	 */
	@Override
	public PointsToGraph boundaryValue(SootMethod entryPoint) {
		// TODO Auto-generated method stub
        PointsToGraph entryValue = new PointsToGraph();
		
		// Locals of main... (only reference types)
		// SootMethod mainMethod = Scene.v().getMainMethod();
		SootMethod entryMethod = entryPoint;

		// Create locals for this method
		for (Local local : entryMethod.getActiveBody().getLocals()) {
			if (local.getType() instanceof RefLikeType) {
				entryValue.assign(local, null);
			}
		}

		if(!entryMethod.getActiveBody().getParameterLocals().isEmpty()){
			// if main method 
			if(entryMethod == Scene.v().getMainMethod()){
				Local argsLocal = entryMethod.getActiveBody().getParameterLocal(0);
				NewArrayExpr argsExpr = new JNewArrayExpr(Scene.v().getRefType("java.lang.String"), IntConstant.v(0));
				entryValue.assignNew(argsLocal, argsExpr);
				entryValue.setFieldConstant(argsLocal, PointsToGraph.ARRAY_FIELD, PointsToGraph.STRING_CONST);
			}else{
				for(int i = 0;i < entryMethod.getActiveBody().getParameterLocals().size();i++){
					Local argsLocal = entryMethod.getActiveBody().getParameterLocal(i);
					Type parameterType = entryMethod.getActiveBody().getParameterLocal(i).getType();
					if(parameterType instanceof RefLikeType){
						if(parameterType instanceof ArrayType){
							NewArrayExpr argsExpr = Jimple.v().newNewArrayExpr(parameterType, IntConstant.v(0));
							entryValue.assignNew(argsLocal, argsExpr);
						}else{
							RefType parameter = (RefType)parameterType;
							JNewExpr argsExpr = new JNewExpr(parameter);
							entryValue.assignNew(argsLocal, argsExpr);
						}
					}
				}
			}
		}
		
		return entryValue;
	}
	
	/**
	 * Returns a copy of the given points-to graph.
	 */
	@Override
	public PointsToGraph copy(PointsToGraph src) {
		// TODO Auto-generated method stub
		return new PointsToGraph(src);
	}

	@Override
	public PointsToGraph meet(PointsToGraph op1, PointsToGraph op2) {
		// TODO Auto-generated method stub
		PointsToGraph result = new PointsToGraph();
		result.union(op1, op2);
		return result;
	}

	@Override
	public ProgramRepresentation<SootMethod, CFGNode> programRepresentation() {
		// TODO Auto-generated method stub
		return DefaultJimpleRepresentation.v();
	}
	
	protected PointsToGraph processCall(Context<SootMethod,CFGNode,PointsToGraph> callerContext, CFGNode callNode, SootMethod method, PointsToGraph entryValue)  {
		
		if(callNode.getStmt()!=null){
			System.out.println("Stmt:\t"+callNode.getStmt());
		}
		
		CallSite callSite = callNode.getCallSite();
		if(callSite==null)
		{
			try {
				callSite = callNode.getStmtTag().initCallSite();
				callSite.setCallingContext(callerContext);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(callSite!=null)
			callSite.setCallingContext(callerContext);
		// Check if the called method has a context associated with this entry flow:
		Context<SootMethod,CFGNode,PointsToGraph> calleeContext = getContext(method, entryValue);
		// If not, then set 'calleeContext' to a new context with the given entry flow.
		// if the method is lib method no need to process its context
		if (calleeContext == null) {
		
			calleeContext = new Context<SootMethod,CFGNode,PointsToGraph>(method, programRepresentation().getControlFlowGraph(method));
			if (verbose) {
				System.out.println("[NEW] X" + callerContext + " -> X" + calleeContext + " " + method + " ");
			}
		}
		contextTransitions.addTransition(callSite, calleeContext);
		// Check if 'caleeContext' has been analysed (surely not if it is just newly made):
		if (calleeContext.isAnalysed()) {
			if (verbose) {
				System.out.println("[HIT] X" + callerContext + " -> X" + calleeContext + " " + method + " ");
			}
			// If yes, then return the 'exitFlow' of the 'calleeContext'.
			return calleeContext.getExitValue();
		} else {
			// If not, then return 'null'.
			return null;
		}
	}
	
	
	/**
	 * Handles a call site by resolving the targets of the method invocation. 
	 * 
	 * The resultant flow is the union of the exit flows of all the analysed
	 * callees. If the method returns a reference-like value, this is also taken
	 * into account.
	 **/
	
	protected PointsToGraph handleInvoke(Context<SootMethod,CFGNode,PointsToGraph> callerContext, CFGNode callStmt,
			InvokeExpr ie, PointsToGraph in) {
		// PreProcessing
		// Get the caller method
		
		SootMethod callerMethod = callerContext.getMethod();
		// Initialise the final result as TOP first
		PointsToGraph resultFlow = topValue();

		// If this statement is an assignment to an object, then set LHS for
		// RETURN values.
		Local lhs = null;
		Value lhsOp = null;
		if (callStmt instanceof AssignStmt) {
			lhsOp = ((AssignStmt) callStmt).getLeftOp();
			if (lhsOp.getType() instanceof RefLikeType) {
				lhs = (Local) lhsOp;
			}
		}

		// Find target methods for this call site (invoke expression) using the points-to data
		Set<SootMethod> targets = getTargets(callerMethod, callStmt, ie, in);

		// If "targets" is null, that means the invoking instance was SUMMARY
		// So we use the DUMMY METHOD (which is a method with no body)
		if (targets == null) {
			targets = getDummyTarget();	
			CallSite srcCallSite = callStmt.getCallSite();
			if(srcCallSite!=null){
				srcCallSite.setCallingContext(callerContext);
				this.contextTransitions.addTransition(srcCallSite, callerContext);
				if (verbose) {
					System.out.println("[DEF] X" + callerContext + " -> DEFAULT " + ie.getMethod());
				}
				
			}
			
		} 
		
		// Make calls for all target methods
		for (SootMethod calledMethod : targets) {
			
			// The call-edge is obtained by assign parameters and THIS, and killing caller's locals
			PointsToGraph callEdge = copy(in);
			if (calledMethod.hasActiveBody()) {
				// We need to maintain a set of locals not to kill (in case the call is recursive)
				Set<Local> doNotKill = new HashSet<Local>();
				
				// Initialise sticky parameter
				callEdge.assign(PointsToGraph.STICKY_LOCAL, null);
				doNotKill.add(PointsToGraph.STICKY_LOCAL);
				
				// Assign this...
				if (ie instanceof InstanceInvokeExpr) {
					Local receiver = (Local)((InstanceInvokeExpr) ie).getBase();
					Local thisLocal = calledMethod.getActiveBody().getThisLocal();
					callEdge.assign(thisLocal, receiver);
					doNotKill.add(thisLocal);
					// Sticky it!
					callEdge.assignSticky(PointsToGraph.STICKY_LOCAL, thisLocal);
				}
				
				// Assign parameters...
				for (int i = 0; i < calledMethod.getParameterCount(); i++) {
					// Only for reference-like parameters
					if (calledMethod.getParameterType(i) instanceof RefLikeType) {
						Local parameter = calledMethod.getActiveBody().getParameterLocal(i);
						Value argValue = ie.getArg(i);
						// The argument can be a constant or local, so handle accordingly
						if (argValue instanceof Local) {
							Local argLocal = (Local) argValue;
							callEdge.assign(parameter, argLocal);
							doNotKill.add(parameter);
							// Sticky it!
							callEdge.assignSticky(PointsToGraph.STICKY_LOCAL, argLocal);
						} else if (argValue instanceof Constant) {
							Constant argConstant = (Constant) argValue;
							callEdge.assignConstant(parameter, argConstant);
							doNotKill.add(parameter);
							// No need to sticky constants as caller does not store them anyway
						} else {
							throw new RuntimeException(argValue.toString());
						}
					}
				}
				
				// Kill caller data...
				for (Local callerLocal : callerMethod.getActiveBody().getLocals()) {
					if (doNotKill.contains(callerLocal) == false)
						callEdge.kill(callerLocal);
				}
				
				// There should be no "return local", but we kill it anyway (just in case)
				callEdge.kill(PointsToGraph.RETURN_LOCAL);
				
				// Create callee locals..
				for (Local calleeLocal : calledMethod.getActiveBody().getLocals()) {
					if (calleeLocal.getType() instanceof RefLikeType 
							&& doNotKill.contains(calleeLocal) == false) {
						callEdge.assign(calleeLocal, null);
					}
				}
			}
			
			// The intra-procedural edge is the IN value minus the objects from the call edge
			PointsToGraph intraEdge = copy(in);
			if (lhs != null) {
				// Oh, and remove the LHS targets too
				intraEdge.assign(lhs, null);
			}
			//intraEdge.subtractHeap(callEdge);
			
			// Value at the start of the called procedure is 
			// whatever went through the call edge
			PointsToGraph entryFlow = callEdge;
			
			

			// Make the call to this method!! (in case of body-less methods, no change)
			PointsToGraph exitFlow = calledMethod.hasActiveBody() ? processCall(callerContext, callStmt, calledMethod, entryFlow) : entryFlow;

			// If the called context was analysed, exitFlow will be set, else it
			// will be null.
			if (exitFlow != null) {
				
				// Propagate stuff from called procedure's exit to the caller's return-site
				PointsToGraph returnEdge = copy(exitFlow);
				
				// Two ways to handle this:
				if (calledMethod.hasActiveBody()) {
					// Kill all the called method's locals. That's right.
					for (Local calleeLocal : calledMethod.getActiveBody().getLocals()) {
						returnEdge.kill(calleeLocal);
					}
					// Remove the stickies (so not to interfere with stickies in the intra-edge)
					// but do not collect unreachable nodes
					returnEdge.killWithoutGC(PointsToGraph.STICKY_LOCAL);					
				} 
				
				// Let's unite the intra-edge with the return edge
				PointsToGraph callOut = topValue();
				callOut.union(intraEdge, returnEdge);
				
				
				// Now we are only left with the return value, if any
				if (calledMethod.hasActiveBody()) {
					if (lhs != null) {
						callOut.assign(lhs, PointsToGraph.RETURN_LOCAL);
					}
					// Kill the @return variable whether there was an LHS or not
					callOut.kill(PointsToGraph.RETURN_LOCAL);
				} else {
					// Handle returned objects for native methods
					if (lhs != null) {
						// If a string is returned, optimise
						if (lhs.getType().equals(PointsToGraph.STRING_CONST.getType())) {
							callOut.assignConstant(lhs, PointsToGraph.STRING_CONST);
						} else if (lhs.getType().equals(PointsToGraph.CLASS_CONST.getType())) {
							callOut.assignConstant(lhs, PointsToGraph.CLASS_CONST);
						} else {
							// Have to assume the worst!
							// System.err.println("Warning! Summary node returned at " + 
							//		callStmt + " in " + callerMethod);
							callOut.assignSummary(lhs);
						}
					}
					
					// Also assume that all parameters are modified
					for (int i = 0; i < calledMethod.getParameterCount(); i++) {
						
						// Only for reference-like parameters
						if (calledMethod.getParameterType(i) instanceof RefLikeType) {
							Value argValue = ie.getArg(i);
							// Summarize if the argument is local (i.e. not a constant)
							if (argValue instanceof Local) {
								Local argLocal = (Local) argValue;
								callOut.summarizeTargetFields(argLocal);
							}
						}
					}
				}
				
				
				// As we may have multiple virtual calls, merge the value at OUT
				// of this target's call-site with an accumulator (resultFlow)
				resultFlow = meet(resultFlow, callOut);
			}
		}		
		
		
		
		// If at least one call succeeded, result flow is not TOP
		if (resultFlow.equals(topValue())) {
			return in;
		} else {
			return resultFlow;
		}
	}
	
	/**
	 * Computes the targets of an invoke expression using a given points-to graph.
	 * 
	 * <p>For static invocations, there is only target. For instance method
	 * invocations, the targets depend on the type of receiver objects pointed-to
	 * by the instance variable whose method is being invoked.</p>
	 * 
	 * <p>If the instance variable points to a summary node, then the returned
	 * value is <tt>null</tt> signifying a <em>default</em> call-site.</p>
	 */
	
	private Set<SootMethod> getTargets(SootMethod callerMethod, CFGNode callStmtNode, InvokeExpr ie, PointsToGraph ptg) {
		Set<SootMethod> targets = new HashSet<SootMethod>();
		SootMethod invokedMethod = ie.getMethod();
		
		// If invoke a method defined in library class instead of a overwrite,
		// not need to get the target
		if(invokedMethod.getDeclaringClass().isLibraryClass()){
			return null;
		}
		
		String subsignature = invokedMethod.getSubSignature();
		Stmt callStmt = callStmtNode.getStmt();
		// Static and special invocations refer to the target method directly
		if (ie instanceof StaticInvokeExpr || ie instanceof SpecialInvokeExpr) {
			targets.add(invokedMethod);
			return targets;
		} else {
			assert (ie instanceof InterfaceInvokeExpr || ie instanceof VirtualInvokeExpr);
			// Get the receiver
			Local receiver = (Local) ((InstanceInvokeExpr) ie).getBase();
			// Get what objects the receiver points-to
			Set<AnyNewExpr> heapNodes = ptg.getTargets(receiver);
			if (heapNodes != null) {
				// For each object, find the invoked method for the declared type
				for (AnyNewExpr heapNode : heapNodes) {
					if (heapNode == PointsToGraph.SUMMARY_NODE) {						
						// If even one pointee is a summary node, then this is a default site
						return null;
					} else if (heapNode instanceof NewArrayExpr) {
						// Probably getClass() or something like that on an array
						return null;
					}
					// Find the top-most class that declares a method with the given
					// signature and add it to the resulting targets
					SootClass sootClass = ((RefType) heapNode.getType()).getSootClass();
					do {
						if (sootClass.declaresMethod(subsignature)) {
							targets.add(sootClass.getMethod(subsignature));
							break;
						} else if (sootClass.hasSuperclass()) {
							sootClass = sootClass.getSuperclass();
						} else {
							sootClass = null;
						}
					} while (sootClass != null);
				}
			}
			if (targets.isEmpty()) {
				System.err.println("Warning! Null call target at:" + callStmt+ " in " + callerMethod);
			}
			return targets;
		}
	}
	
	
	private Set<SootMethod> getDummyTarget() {
		Set<SootMethod> targets = new HashSet<SootMethod>();
		targets.add(DUMMY_METHOD);
		return targets;
	}
	public Map<SootMethod,List<Context<SootMethod,CFGNode,PointsToGraph>>>getContexts(){
		return contexts;
	}
}
