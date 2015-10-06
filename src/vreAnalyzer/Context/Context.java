package vreAnalyzer.Context;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.ControlFlowGraph.CFG;

/**
 * @author tangchris
 * 
 */

/**
 * A value-based context for a context-sensitive inter-procedural data flow
 * analysis.
 * 
 * <p>
 * A value-based context is identified as a pair of a method and the data
 * flow value at the entry of the method, for forward flows, or the data
 * flow value at the exit of the method, for backward flows. Thus, if
 * two distinct calls are made to a method and each call-site has the same
 * data flow value then it is considered that the target of that call is
 * the same context. This concept allows termination in the presence of 
 * recursion as the number of contexts is limited by the size of the lattice
 * (which must be finite).
 * </p>
 * 
 * <p>
 * Each value context has its own work-list of CFG nodes to analyse, and the
 * results of analysis are stored in a map from nodes to the data flow values
 * before/after the node.
 * </p>
 * 
 * @author Rohan Padhye
 * 
 * @param <M> the type of a method
 * @param <N> the type of a node in the CFG
 * @param <A> the type of a data flow value
 */
public class Context<SootMethod,CFGNode,PointsToGraph> implements soot.Context, Comparable<Context<SootMethod,CFGNode,PointsToGraph>> {

	/** A counter for global context identifiers. */
	private static int count = 0;
	
	/** Debug stuff */
	static java.util.Set<Object> freeContexts = new java.util.HashSet<Object>();
	///static int totalNodes = 0;
	//static int liveNodes = 0;

	/** Whether or not this context has been fully analysed at least once. */
	private boolean analysed;

	/** The control-flow graph of this method's body. */
	private CFG controlFlowGraph;

	/** The data flow value associated with the entry to the method. **/
	private PointsToGraph entryValue;

	/** The data flow value associated with the exit of the method. */
	private PointsToGraph exitValue;

	/** A globally unique identifier. */
	private int id;

	
	/** The method for which this calling context context applies. */
	private SootMethod method;

	/** The data flow values at the exit of each node. */
	private Map<CFGNode,PointsToGraph> outValues;

	/** The data flow values at the entry of each node. */
	private Map<CFGNode,PointsToGraph> inValues;

	/** The work-list of nodes that still need to be analysed. */
	private NavigableSet<CFGNode> workList;

	/**
	 * Creates a new context for the given method.
	 * 
	 * @param method the method to which this value context belongs
	 * @param cfg the control-flow graph for the body of <tt>method</tt>
	 * @param reverse <tt>true</tt> if the analysis is in the reverse direction,
	 *            and <tt>false</tt> if the analysis is in the forward direction
	 */
	@SuppressWarnings({ "unchecked", "static-access" })
	public Context(SootMethod stmethod, CFG cfg) {
		// Increment count and set id.
		count++;
		this.id = count;

		// Initialise fields.
		this.method = stmethod;
		this.controlFlowGraph = cfg;
		this.inValues = new HashMap<CFGNode,PointsToGraph>();
		this.outValues = new HashMap<CFGNode,PointsToGraph>();
		this.analysed = false;
		
		
		// Then a mapping from a N to the position in the order.
		final Map<CFGNode,Integer> numbers = new HashMap<CFGNode,Integer>();
		
		// Map the lowest priority to the null N, which is used to aggregate
		// ENTRY/EXIT flows.
		List<CFGNode>orderedNodes = new LinkedList<CFGNode>();
		if(cfg!=null){
			orderedNodes =  (List<CFGNode>) cfg.getNodes();
		}
		
		int num = 1;
		for (CFGNode N : orderedNodes) {
			numbers.put(N, num);
			num++;
		}
		
		numbers.put(null, Integer.MAX_VALUE);
		
		// Now, create a sorted set with a comparator created on-the-fly using
		// the total order.
		this.workList = new TreeSet<CFGNode>(new Comparator<CFGNode>() {
			public int compare(CFGNode u, CFGNode v) {
				return numbers.get(u) - numbers.get(v);
			}
		});
		if(controlFlowGraph!=null){
		    for(vreAnalyzer.Elements.CFGNode cf:controlFlowGraph.getNodes()){
		    	if(!cf.toString().equals("EN"))
		    		this.workList.add((CFGNode) cf);
		    }
		}
		
		// Record this context to csv file
		vreAnalyzerCommandLine.inst().contextwriter.println(this.id+","+ 
		"\""+method.toString()+"\""+","+
		"\""+cfg.getSootClass().getName()+"\"");
		
	}

	/**
	 * Compares two contexts by their globally unique IDs.
	 * 
	 * This functionality is useful in the framework's internal methods
	 * where ordered processing of newer contexts first helps speed up
	 * certain operations.
	 */
	@Override
	public int compareTo(Context<SootMethod,CFGNode,PointsToGraph> other) {
		return this.getId() - other.getId();
	}

	
	

	/** 
	 * Returns a reference to the control flow graph of this context's method. 
	 * 
	 * @return a reference to the control flow graph of this context's method
	 */
	public CFG getControlFlowGraph() {
		return controlFlowGraph;
	}
	
	/** Returns the total number of contexts created so far. */
	public static int getCount() {
		return count;
	}

	/**
	 * Returns a reference to the data flow value at the method entry.
	 * 
	 * @return a reference to the data flow value at the method entry
	 */
	public PointsToGraph getEntryValue() {
		return entryValue;
	}

	/**
	 * Returns a reference to the data flow value at the method exit.
	 * 
	 * @return a reference to the data flow value at the method exit
	 */
	public PointsToGraph getExitValue() {
		return exitValue;
	}

	/**
	 * Returns the globally unique identifier of this context.
	 * 
	 * @return the globally unique identifier of this context
	 */
	public int getId() {
		return id;
	}

	/**
	 * Returns a reference to this context's method.
	 * 
	 * @return a reference to this context's method
	 */
	public SootMethod getMethod() {
		return method;
	}
	
	/**
	 * Gets the data flow value at the exit of the given node.
	 * 
	 * @param node a node in the control flow graph
	 * @return the data flow value at the exit of the given node
	 */
	public PointsToGraph getValueAfter(CFGNode node) {
		return outValues.get(node);
	}

	/**
	 * Gets the data flow value at the entry of the given node.
	 * 
	 * @param node a node in the control flow graph
	 * @return the data flow value at the entry of the given node
	 */
	public PointsToGraph getValueBefore(CFGNode node) {
		return inValues.get(node);
	}

	/**
	 * Returns a reference to this context's work-list.
	 * 
	 * @return a reference to this context's work-list
	 */
	public NavigableSet<CFGNode> getWorkList() {
		return workList;
	}

	/** 
	 * Returns whether or not this context has been analysed at least once. 
	 *
	 * @return <tt>true</tt> if the context has been analysed at least once,
	 * or <tt>false</tt> otherwise
	 */
	public boolean isAnalysed() {
		return analysed;
	}

	/**
	 * Returns whether the information about individual CFG nodes has
	 * been released.
	 * 
	 * @return <tt>true</tt> if the context data has been released
	 */
	boolean isFreed() {
		return inValues == null && outValues == null;
	}

	/** 
	 * Marks this context as analysed.
	 */
	public void markAnalysed() {
		this.analysed = true;
	}

	/** 
	 * Sets the entry flow of this context. 
	 * 
	 * @param entryValue the new data flow value at the method entry
	 */
	public void setEntryValue(PointsToGraph entryValue) {
		this.entryValue = entryValue;
	}

	/** 
	 * Sets the exit flow of this context. 
	 * 
	 * @param exitValue the new data flow value at the method exit
	 */
	public void setExitValue(PointsToGraph exitValue) {
		this.exitValue = exitValue;
	}
	/** 
	 * Sets the data flow value at the exit of the given node.
	 * 
	 * @param node a node in the control flow graph
	 * @param value the new data flow at the node exit
	 */
	public void setValueAfter(CFGNode node, PointsToGraph value) {
		outValues.put(node, value);
	}
	/** 
	 * Sets the data flow value at the entry of the given node.
	 * 
	 * @param node a node in the control flow graph
	 * @param value the new data flow at the node entry
	 */
	public void setValueBefore(CFGNode node, PointsToGraph value) {
		inValues.put(node, value);
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
