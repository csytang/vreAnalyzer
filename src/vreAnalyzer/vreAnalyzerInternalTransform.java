package vreAnalyzer;

import java.util.Map;

import Patch.Hadoop.ProjectParser;
import soot.SceneTransformer;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder.EntryNotFoundException;
import vreAnalyzer.Reuse.Normal.Pipeline.NormalPipelines;
import vreAnalyzer.Util.Options;
import vreAnalyzer.Util.Options.reusableMode;

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
		PointsToAnalysis.inst().doAnalysis();
		System.out.println("[vreAnalyzer] Points to graph build[Finish]");
		// Finish
				
		System.out.println("[vreAnalyzer] Internal transform[Finish]");
				
		// Display Reusable Result by checking the mode
				
		if(Options.getMode()==reusableMode.Normal){
			NormalPipelines.inst().findCommonAssetsandReset();
		}else if(Options.getMode()==reusableMode.Hadoop){
			
			ProjectParser.inst().ClassParser();
			ProjectParser.inst().showCommons();
			
		}
		
	}

}
