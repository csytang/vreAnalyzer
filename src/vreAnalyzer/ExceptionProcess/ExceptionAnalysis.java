package vreAnalyzer.ExceptionProcess;
import java.util.HashMap;
import java.util.Map;
import soot.SootClass;


public class ExceptionAnalysis {
	//entry class method
	public ExceptionAnalysis(SootClass sootclass) {
		
		Map<SootClass, ClassExceptionAnalysis> class_map = new HashMap<SootClass, ClassExceptionAnalysis>();  
		
		
		ClassExceptionAnalysis class_exception_analysis = new ClassExceptionAnalysis(sootclass, class_map );
		class_map.put(sootclass, class_exception_analysis);
			
			
		
		boolean reached_fixed_point;
		do{
			reached_fixed_point = true;
			for (Map.Entry<SootClass, ClassExceptionAnalysis> entry : class_map.entrySet())
			{
				if(entry.getValue().DoAnalysis(false))
					reached_fixed_point = false;
			}
		} while( !reached_fixed_point );
		
		for (Map.Entry<SootClass, ClassExceptionAnalysis> entry : class_map.entrySet())
		{
			entry.getValue().DoAnalysis(true);
		}
	}

}