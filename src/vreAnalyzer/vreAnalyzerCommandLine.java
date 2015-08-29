package vreAnalyzer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import soot.PackManager;
import soot.PhaseOptions;
import soot.Transform;
import vreAnalyzer.Util.ConflictModelExpection;
import vreAnalyzer.Util.Options;



public class vreAnalyzerCommandLine{
	
	private static vreAnalyzerCommandLine instance;
	private static boolean startfromGUI = true;
	public static vreAnalyzerCommandLine inst(String[]args){
		if(instance==null){
			try {
				instance = new vreAnalyzerCommandLine(args);
			} catch (ConflictModelExpection e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}
	public static void main(String[]args) throws ConflictModelExpection{
		startfromGUI = false;
		new vreAnalyzerCommandLine(args);
	}
	public static boolean isStartFromGUI(){
		return startfromGUI;
	}
	public vreAnalyzerCommandLine(String[]args) throws ConflictModelExpection{
		// All input command list
		
		List<String>sootArgs = new LinkedList<String>(Arrays.asList(args));
				
		// Enable whole program mode
		
		sootArgs.add("-W");
		
		sootArgs.add("-p");
		sootArgs.add("wjop");
		sootArgs.add("enabled:true");
		
		//sootArgs.add("-allow-phantom-refs");
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
		sootArgs.add("none");
		//sootArgs.add("class");
		
		
		// Get all input commands
		String[]argsArray = sootArgs.toArray(new String[0]);
				
		// Use filter to get target commands to Soot
	    String[] filtersootArgs = Options.parseFilterArgs(argsArray);
		
		// Internal transfer
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.mt", new vreAnalyzerInternalTransform()));
		PhaseOptions.v().setPhaseOption("tag.ln", "on");
		System.out.print("[vreAnalyzer] vreAnalyzer args to Soot: ");
		for (String s : filtersootArgs){
				System.out.print(s + " ");
		}
		
		System.out.println();
		
		// Run Soot
		soot.Main.main(filtersootArgs);
		
	}
	
	
}
