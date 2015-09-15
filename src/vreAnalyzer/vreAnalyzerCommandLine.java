package vreAnalyzer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import soot.PackManager;
import soot.Transform;
import vreAnalyzer.CSV.CSVWriter;
import vreAnalyzer.Util.ConflictModelExpection;
import vreAnalyzer.Util.Options;



public class vreAnalyzerCommandLine{
	
	private static vreAnalyzerCommandLine instance;
	private static boolean isstartfromGUI = true;
	private static boolean issourceBinding = false;
	private static boolean startfromSource = false;
	public static CSVWriter contextwriter = null;
	public static CSVWriter featurewriter = null;
	private String outputDirectory = "";
	
	public static vreAnalyzerCommandLine inst(){
		return instance;
	}
	public static vreAnalyzerCommandLine inst(String[]args,boolean isSourceBinding,boolean startFromSource){
		if(instance==null){
			try {
				// 1. binding information
				issourceBinding = isSourceBinding;
				startfromSource = startFromSource;
				
				
				instance = new vreAnalyzerCommandLine(args);
			} catch (ConflictModelExpection e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}
	public static void main(String[]args) throws ConflictModelExpection{
		isstartfromGUI = false;
		issourceBinding = false;
		new vreAnalyzerCommandLine(args);
	}
	public static boolean isStartFromGUI(){
		return isstartfromGUI;
	}
	public static boolean isSourceBinding(){
		return issourceBinding;
	}
	public vreAnalyzerCommandLine(String[]args) throws ConflictModelExpection{
		// All input command list
		outputDirectory = ".";
		
		List<String>sootArgs = new LinkedList<String>(Arrays.asList(args));
				
		// Enable whole program mode
		if(!startfromSource){
			sootArgs.add("-W");
			
			sootArgs.add("-p");
			sootArgs.add("wjop");
			sootArgs.add("enabled:true");
			
			sootArgs.add("-allow-phantom-refs");
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
		}
		
		// Output
		sootArgs.add("-f");
		sootArgs.add("none");
		//sootArgs.add("class");
		
		
		// Get all input commands
		String[]argsArray = sootArgs.toArray(new String[0]);
				
		// Use filter to get target commands to Soot
	    String[] filtersootArgs = Options.parseFilterArgs(argsArray,startfromSource);
		
	    
	    // 1. data output to files
	    System.out.println("[vreAnalyzer] vreAnalyzer writes context information to:"+outputDirectory+"/context.csv");
	 	contextwriter = new CSVWriter(outputDirectory+"/context.csv");
	 	contextwriter.println("ContextID,SootMethod,SootClass");
	 	// 2. job output to files
	 	System.out.println("[vreAnalyzer] vreAnalyzer writes feature information to:"+outputDirectory+"/feature.csv");
	 	featurewriter = new CSVWriter(outputDirectory+"/feature.csv");
	 	featurewriter.println("JobName,Stmt,SootMethod,SootClass,File");
	 	
	 	
		// Internal transfer
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myTransformer", new vreAnalyzerInternalTransform()));
		
		System.out.print("[vreAnalyzer] vreAnalyzer args to Soot: ");
		for (String s : filtersootArgs){
				System.out.print(s + " ");
		}
		
		System.out.println();
		
		// Run Soot
		soot.Main.main(filtersootArgs);
		
		
		contextwriter.close();
		featurewriter.close();
	}
	
	
}
