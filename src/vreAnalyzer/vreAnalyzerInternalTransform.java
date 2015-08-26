package vreAnalyzer;

import java.util.Map;

import soot.SceneTransformer;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder.EntryNotFoundException;


public class vreAnalyzerInternalTransform extends SceneTransformer{

	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		// TODO Auto-generated method stub
		
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
		
		System.out.println("[vreAnalyzer] Points to graph build[Finish]");
		// Finish
				
		System.out.println("[vreAnalyzer] Internal transform[Finish]");
				
		// Display Reusable Result by checking the mode
		
		
		
		
	}

}
