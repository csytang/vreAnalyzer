package vreAnalyzer.PointsTo;

/**
 * Copyright (C) 2013 Rohan Padhye
 * 
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import vreAnalyzer.Context.Context;
import vreAnalyzer.Context.ContextTransitionTable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;

/**
 * A generic inter-procedural analysis which is fully context-sensitive.
 * 
 * <p>
 * This class is a base for forward and backward inter-procedural analysis
 * classes. This inter-procedural analysis framework is fully context
 * sensitive even in the presence of recursion and uses data flow values
 * reaching a method to distinguish contexts.
 * </p>
 * 
 * @author Rohan Padhye
 * 
 * @param <M> the type of a method
 * @param <N> the type of a node in the CFG
 * @param <A> the type of a data flow value
 * 
 * @see Context
 * @see ContextTransitionTable
 * @see CallSite
 */
public abstract class InterProceduralAnalysis<SootMethod,CFGNode,PointsToGraph> {
	
	/** A work-list of contexts to process. */
	protected final NavigableSet<Context<SootMethod,CFGNode,PointsToGraph>> worklist;

	/** A mapping from methods to a list of contexts for quick lookups. */
	protected final Map<SootMethod,List<Context<SootMethod,CFGNode,PointsToGraph>>> contexts;

	/**
	 * A record of transitions from calling context and call-site to 
	 * called method and called context.
	 */
	protected final ContextTransitionTable<SootMethod,CFGNode,PointsToGraph> contextTransitions;


	/**
	 * <tt>true</tt> if the direction of analysis is backward, or <tt>false</tt>
	 * if it is forward.
	 */

	/**
	 * A flag, if set, directs the analysis to free memory storing
	 * data flow values of individual statements once a context has
	 * been analysed and would not be required to be re-analysed.
	 * 
	 * <p>This setting is only useful for analyses that aggregate secondary
	 * results on the fly (e.g. call graph analysis that can do away with
	 * points-to information once the calls for a particular context have
	 * been resolved). This is not safe for use in analyses whose results
	 * will be directly used later (e.g. liveness analysis).</p>
	 * 
	 * <p>Memory is freed when a context is removed from the work-list and no context
	 * reachable from it in the transition table is also on the work-list. This 
	 * ensures that the removed context will not be added again on the work-list
	 * for re-analysis of any statement.</p>
	 * 
	 * <p>Note that the data flow values at the entry/exit of the context are
	 * not freed, and hence it is still used to terminate recursion or as a cache
	 * hit for arbitrary call sites with same values.</p>
	 * 
	 * <p>The default value for this flag is <tt>false</tt>.</p>
	 * 
	 */
	
	/**
	 * Whether to print information about contexts.
	 */
	protected boolean verbose;
	
	/**
	 * Constructs a new inter-procedural analysis.
	 * 
	 * @param reverse <tt>true</tt> if the analysis is in the reverse direction,
	 *            <tt>false</tt> if it is in the forward direction
	 */
	public InterProceduralAnalysis() {

		// Set direction

		// Initialise map of methods to contexts.
		contexts = new HashMap<SootMethod,List<Context<SootMethod,CFGNode,PointsToGraph>>>();

		// Initialise context transition table
		contextTransitions = new ContextTransitionTable<SootMethod,CFGNode,PointsToGraph>();
		
		// Initialise the work-list
		worklist = new TreeSet<Context<SootMethod,CFGNode,PointsToGraph>>();

	}

	/**
	 * Returns the initial data flow value at the program entry points. For
	 * forward analyses this is the IN value at the ENTRY to each entry method,
	 * while for backward analyses this is the OUT value at the EXIT to each
	 * entry method.
	 * 
	 * <p>Note that this method will be called exactly once per entry point 
	 * specified by the program representation.</p>
	 * 
	 * @param entryPoint an entry point specified by the program representation
	 * @return the data flow value at the boundary
	 * 
	 * @see ProgramRepresentation#getEntryPoints()
	 */
	public abstract PointsToGraph boundaryValue(SootMethod entryPoint);

	/**
	 * Returns a copy of the given data flow value.
	 * 
	 * @param src the data flow value to copy
	 * @return a new data flow value which is a copy of the argument
	 */
	public abstract PointsToGraph copy(PointsToGraph src);

	/**
	 * Performs the actual data flow analysis.
	 * 
	 * <p>
	 * A work-list of contexts is maintained, each with it's own work-list of CFG nodes
	 * to process. For each node removed from the work-list of the newest context, 
	 * the meet of values along incoming edges (in the direction of analysis) is computed and
	 * then the flow function is processed depending on whether the node contains a call 
	 * or not. If the resulting data flow value has changed, then nodes along outgoing edges
	 * (in the direction of analysis) are also added to the work-list. </p>
	 * 
	 * <p>
	 * Analysis starts with the context for the program entry points with the given
	 * boundary values and ends when the work-list is empty.
	 * </p>
	 * 
	 * <p>See the SOAP '13 paper for the full algorithm in Figure 1.</p>
	 * 
	 */
	public abstract void doAnalysis();


	/**
	 * Returns the callers of a value context.
	 * 
	 * @param target the value context
	 * @return the call-sites which transition to the value context
	 */
	public Set<CallSite> getCallers(Context<SootMethod,CFGNode,PointsToGraph> target) {
		return this.contextTransitions.getCallers(target);
	}

	/**
	 * Retrieves a particular value context if it has been constructed.
	 * 
	 * @param method the method whose value context to find
	 * @param value the data flow value at the entry (forward flow) or exit
	 *            (backward flow) of the method
	 * @return the value context, if one is found with the given parameters,
	 *         or <tt>null</tt> otherwise
	 */
	public Context<SootMethod,CFGNode,PointsToGraph> getContext(SootMethod method, PointsToGraph value) {
		// If this method does not have any contexts, then we'll have to return nothing.
		if (!contexts.containsKey(method)) {
			return null;
		}
		// Otherwise, look for a context in this method's list with the given value.
	
			// Forward flow, so check for ENTRY FLOWS
		for (Context<SootMethod,CFGNode,PointsToGraph> context : contexts.get(method)) {
				if (value.equals(context.getEntryValue())) {
					return context;
				}
		}
		
		// If nothing found return null.
		return null;
	}
	
	
	/**
	 * Returns a list of value contexts constructed for a given method.
	 * 
	 * @param method the method whose contexts to retrieve
	 * @return an unmodifiable list of value contexts of the given method
	 */
	public List<Context<SootMethod,CFGNode,PointsToGraph>> getContexts(SootMethod method) {
		if (contexts.containsKey(method)) {
			return Collections.unmodifiableList(contexts.get(method));
		} else {
			return Collections.unmodifiableList(new LinkedList<Context<SootMethod,CFGNode,PointsToGraph>>());
		}
	}
	
	/**
	 * Returns a reference to the context transition table used by this analysis.
	 * 
	 * @return a reference to the context transition table used by this analysis
	 */
	public ContextTransitionTable<SootMethod,CFGNode,PointsToGraph> getContextTransitionTable() {
		return contextTransitions;
	}

	/**
	 * Returns a meet-over-valid-paths solution by merging data flow
	 * values across contexts for each program point.
	 * 
	 * <p>This method should not be invoked if the flag 
	 * {@link #freeResultsOnTheFly} had been set during analysis.</p>
	 * 
	 * @return a meet-over-valid-paths data flow solution
	 */
	public DataFlowSolution<CFGNode,PointsToGraph> getMeetOverValidPathsSolution() {
		Map<CFGNode,PointsToGraph> inValues = new HashMap<CFGNode,PointsToGraph>();
		Map<CFGNode,PointsToGraph> outValues = new HashMap<CFGNode,PointsToGraph>();
		// Merge over all contexts
		for (SootMethod method : contexts.keySet()) {
			Iterator itCFG = programRepresentation().getControlFlowGraph(method).getNodes().iterator();
			while (itCFG.hasNext()) {
				CFGNode node = (CFGNode) itCFG.next();
				PointsToGraph in = topValue();
				PointsToGraph out = topValue();
				for (Context<SootMethod,CFGNode,PointsToGraph> context : contexts.get(method)) {
					in = meet(in, context.getValueBefore(node));
					out = meet(out, context.getValueAfter(node));
				}
				inValues.put(node, in);
				outValues.put(node, out);
			}
		}
		// Return data flow solution
		return new DataFlowSolution<CFGNode,PointsToGraph>(inValues, outValues);
	}
	
	/**
	 * Returns all methods for which at least one context was created.
	 * @return an unmodifiable set of analysed methods
	 */
	public Set<SootMethod> getMethods() {
		return Collections.unmodifiableSet(contexts.keySet());
	}

	/**
	 * Returns the target of a call-site.
	 * 
	 * @param callSite the call-site whose targets to retrieve
	 * @return a map of target methods to their respective contexts
	 */
	public Map<SootMethod,Context<SootMethod,CFGNode,PointsToGraph>> getTargets(CallSite callSite) {
		return this.contextTransitions.getTargets(callSite);
	}

	/**
	 * Returns the meet of two data flow values.
	 * 
	 * @param op1 the first operand
	 * @param op2 the second operand
	 * @return a new data flow which is the result of the meet operation of the
	 *         two operands
	 */
	public abstract PointsToGraph meet(PointsToGraph op1, PointsToGraph op2);

	/**
	 * Returns a program representation on top of which the inter-procedural
	 * analysis runs. The program representation is used to build control
	 * flow graphs of individual procedures and resolve targets of virtual
	 * call sites.
	 * 
	 * @return The program representation underlying this analysis
	 */
	public abstract ProgramRepresentation<SootMethod,CFGNode> programRepresentation();

	/**
	 * Returns the default data flow value (lattice top).
	 * 
	 * @return the default data flow value (lattice top)
	 */
	public abstract PointsToGraph topValue();

}
