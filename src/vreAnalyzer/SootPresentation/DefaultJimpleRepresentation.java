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
package vreAnalyzer.SootPresentation;

import java.util.List;

import soot.Scene;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.PointsTo.ProgramRepresentation;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;


/**
 * A default program representation for Soot using the Jimple IR. This 
 * representation uses control-flow graphs of individual units including exceptional 
 * control flow, and resolves virtual calls using the default context-insensitive
 * call graph.
 * 
 * <p><strong>Note</strong>: This class follows the Singleton pattern. The singleton 
 * object is available through {@link #v()}.</p>
 * 
 * @author Rohan Padhye
 *
 */
public class DefaultJimpleRepresentation implements ProgramRepresentation<SootMethod, CFGNode> {
	
	
	
	// Private constructor, see #v() to retrieve singleton object
	private DefaultJimpleRepresentation() {
		
	}
	
	/**
	 * Returns a singleton list containing the <code>main</code> method.
	 * @see Scene#getMainMethod()
	 */
	@Override
	public List<SootMethod> getEntryPoints() {
		return ProgramFlowBuilder.inst().getAppConcreteMethods();
		//return ProgramFlowBuilder.inst().getEntryMethods();
	}

	/**
	 * Returns an {@link ExceptionalUnitGraph} for a given method.
	 */
	@Override
	public CFG getControlFlowGraph(SootMethod method) {
		return ProgramFlowBuilder.inst().getCFG(method);
	}

	/**
	 * Returns <code>true</code> iff the Jimple statement contains an
	 * invoke expression.
	 */
	@Override
	public boolean isCall(CFGNode node) {
		return ((soot.jimple.Stmt) node.getStmt()).containsInvokeExpr();
	}

	/**
	 * Resolves virtual calls using the default call graph and returns
	 * a list of methods which are the targets of explicit edges.
	 * TODO: Should we consider thread/clinit edges?
	 */
	@Override
	public List<SootMethod> resolveTargets(SootMethod method, CFGNode node) {
		return node.getCallSite().getAllCallees();
	}

	// The singleton object
	private static DefaultJimpleRepresentation singleton = new DefaultJimpleRepresentation();
	
	/**
	 * Returns a reference to the singleton object of this class.
	 */
	public static DefaultJimpleRepresentation v() { return singleton; }
	
}
