package vreAnalyzer;

import java.util.Map;
import Patch.Hadoop.ProjectParser;
import soot.SceneTransformer;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder.EntryNotFoundException;


public class vreAnalyzerInternalTransform extends SceneTransformer{

	

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		
		
				System.out.println("[vreAnalyzer] Internal transform[Start]");
				System.out.println("[vreAnalyzer] Program flow build[Start]");
				try {
					ProgramFlowBuilder.createInstance();
				} catch (EntryNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("[vreAnalyzer] Program flow build[Finish]");
				
				System.out.println("[vreAnalyzer] Points to graph build[Start]");
				PointsToAnalysis.inst().doAnalysis();
				System.out.println("[vreAnalyzer] Points to graph build[Finish]");
				// Finish
						
				System.out.println("[vreAnalyzer] Internal transform[Finish]");
						
				// Display Reusable Result by checking the mode
				ProjectParser.inst().ClassParser();
				// Following will only be allowed if it is started from GUI and source is binded
				// Annotated all jobs with selected color;
				if(vreAnalyzerCommandLine.isSourceBinding()&&
						vreAnalyzerCommandLine.isStartFromGUI()){
					ProjectParser.inst().annotateallJobs();
				}
				
				
	}

}
