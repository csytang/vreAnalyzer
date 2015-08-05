package vreAnalyzer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Patch.Hadoop.HubClassParser;
import soot.PackManager;
import soot.SceneTransformer;
import soot.Transform;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder.EntryNotFoundException;
import vreAnalyzer.Reuse.Normal.Pipeline.NormalPipelines;
import vreAnalyzer.Util.ConflictModelExpection;
import vreAnalyzer.Util.Options;
import vreAnalyzer.Util.Options.reusableMode;



public class vreHadoop extends SceneTransformer{
	
	
	
	public static void main(String[]args) throws ConflictModelExpection{
		// All input command list
		@SuppressWarnings("unchecked")
		List<String>sootArgs = new LinkedList(Arrays.asList(args));
				
		// Enable whole program mode
		sootArgs.add("-W");
		
		sootArgs.add("-p");
		sootArgs.add("wjop");
		sootArgs.add("enabled:true");
			
		// Enable points-to analysis
		sootArgs.add("-p");
		sootArgs.add("cg");
		sootArgs.add("implicit-entry:false");
								
		// Enable Spark analysis
		sootArgs.add("-p");
		sootArgs.add("cg.spark");
		sootArgs.add("enabled:true");
		
		// Use asm-backend instead of  Jasmin backend
		sootArgs.add("-asm-backend");
		
		// Keep the original line number for debugging
		sootArgs.add("-keep-line-number");
		sootArgs.add("-keep-bytecode-offset");
		
		// Output
		sootArgs.add("-f");
		sootArgs.add("j");
		//sootArgs.add("class");
		
		
		// Get all input commands
		String[]argsArray = sootArgs.toArray(new String[0]);
				
		// Use filter to get target commands to Soot
		String[] filtersootArgs = Options.parseFilterArgs(argsArray);
				
		// Internal transfer
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.mt", new vreHadoop()));
				
		System.out.print("vreHadoop args to Soot: ");
		for (String s : filtersootArgs)
			System.out.print(s + " ");
		System.out.println();
				
		// Run Soot
		soot.Main.main(filtersootArgs);
		
	}
	
	/**
	 * Internal transform, customized for own purpose
	 */
	@Override
	protected void internalTransform(String arg0, Map<String, String> arg1) {
		// TODO Auto-generated method stub
		System.out.println("vreHadoop's internal transform[Start]");
		
		try {
			ProgramFlowBuilder.createInstance();
		} catch (EntryNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PointsToAnalysis.inst().doAnalysis();
		
		// Finish
		System.out.println("vreHadoop's internal transform[Done]");
		
		// Display Reusable Result
		if(Options.getMode()==reusableMode.Normal){
			NormalPipelines.inst().findCommonAssetsandReset();
		}else if(Options.getMode()==reusableMode.Hadoop){
			HubClassParser.inst(ProgramFlowBuilder.inst().getEntryClass()).Parse();
		}
	}
}
