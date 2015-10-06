package vreAnalyzer.ExceptionProcess;
import java.util.HashMap;
import java.util.Map;

import soot.SootClass;
import soot.SootMethod;


public class ClassExceptionAnalysis {
	
	// map from class to its class exception analysis
	Map<SootClass, ClassExceptionAnalysis> class_map;
	
	// map from method to its analysis result
	Map<SootMethod, MethodExceptionAnalysis> method_analysis;
	
	// input soot class
	SootClass soot_class;
	
	
	public ClassExceptionAnalysis( SootClass c, Map<SootClass, ClassExceptionAnalysis> cm ) {
		soot_class = c;
		class_map = cm;
		method_analysis = new HashMap<SootMethod, MethodExceptionAnalysis>();
		
		for(SootMethod method : soot_class.getMethods()) {
			method_analysis.put( method, new MethodExceptionAnalysis( soot_class, method, class_map ));
		}
		
	}
	
	public boolean DoAnalysis( boolean verbose ) {
		boolean return_value = false;
		for( Map.Entry<SootMethod, MethodExceptionAnalysis> entry : method_analysis.entrySet() ) {
			if( entry.getValue().DoAnalysis( verbose ) ) 
				return_value = true;
		}
		
		return return_value;
	}
}