package vreAnalyzer;
import soot.SceneTransformer;
import vreAnalyzer.Blocks.BlockGenerator;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder.EntryNotFoundException;
import vreAnalyzer.Variants.BindingResolver;
import java.util.Map;

import Patch.Hadoop.ProjectParser;

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
		if(vreAnalyzerCommandLine.isStartFromGUI())
			BlockGenerator.inst(ProgramFlowBuilder.inst().getAppClasses());
		System.out.println("[vreAnalyzer] Program flow build[Finish]");
			
		System.out.println("[vreAnalyzer] Points to graph build[Start]");
		PointsToAnalysis.inst().doAnalysis();
		System.out.println("[vreAnalyzer] Points to graph build[Finish]");
		// Finish
						
		System.out.println("[vreAnalyzer] Internal transform[Finish]");
		System.out.println("[vreAnalyzer] BindingResolve[Start]");
		BindingResolver.inst().run();
		
		System.out.println("[vreAnalyzer] BindingResolve[Finish]");
		System.out.println("[vreAnalyzer] Project analysis[Start]");
		// Display Reusable Result by checking the mode
		ProjectParser.inst().runProjectParser();
		System.out.println("[vreAnalyzer] Project analysis[Finish]");
		
				
				
	}

}
