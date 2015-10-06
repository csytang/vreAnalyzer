package vreAnalyzer.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Options {
	private static String entryClassName = null; // default: find unique 'main' in program
	
	/** Indicates the behavior of a call model: 'this' is defined and args (including 'this) are used, or only args are used, or no def nor use. */
	public static enum DUModelBehavior { DEF_USES_ALL, USES_ALL, NONE };
	
	// All possible mode
	public enum reusableMode {
			Normal("Normal"),Hadoop("Hadoop");
			private final String name;
			private reusableMode(String s){
				name = s;
			}
			public String toString(){
				return this.name();
			}
	}
	
	
	public static reusableMode mode = reusableMode.Normal;
	
	// Flag to tag a single change
	private static boolean isChanges = false;
	
	
	private static Set<String> normalClassList = null;
	
	private static boolean allowParmsRetUseDefs = false;
	
	private static DUModelBehavior duModelBehavior = DUModelBehavior.NONE; // by default : safest assumption
	
	public Options(){
		
	}
	/** 
	 * Parses argument list (command line args), and gets settings for this program, removing them and returning soot-only args
	 * @throws ConflictModelExpection 
	 */
	public static String[] parseFilterArgs(String[] args,boolean startFromSource) throws ConflictModelExpection{
		String nobody = "-no-bodies-for-excluded";
		List<String> argsSoot = new ArrayList<String>();
		if(!startFromSource){
			argsSoot.add(nobody);
		}
		for (int argIdx = 0; argIdx < args.length; ++argIdx) {
			String arg = args[argIdx];
			/*
			 *replace the * to all sub jars 
			 */
			if(arg.equals("-cp")){
				String classPath = args[1+argIdx];
				String replaceString = "";
				String []subclassList = classPath.split(":");
				for(int i = 0;i < subclassList.length;i++){
					if(subclassList[i].endsWith("/*")){
						File proFile = new File(subclassList[i].substring(0, subclassList[i].length()-1));
						String subreplaceString=findjarsReplaceWithFiles(proFile);
						subclassList[i]= subreplaceString;
					}
					replaceString+=subclassList[i];
					replaceString+=":";
				}
				replaceString = replaceString.substring(0, replaceString.length()-1);
				args[1+argIdx]=replaceString;
				argsSoot.add(arg);
			}
			/*
			 *if user set the customize entry class to analysis, then the analyzing process starts from this class
			 */
			else if(arg.startsWith("-entry:")){
				entryClassName = new String(arg).substring("-entry:".length());
				
				System.out.println("Specified main entry class is '" + entryClassName + "'");
				argsSoot.add("-main-class");
				argsSoot.add(entryClassName);
			}
			else if (arg.startsWith("-dumodel:")) {
				String value = arg.substring("-dumodel:".length());
				if (value.equals("defuses"))
					duModelBehavior = DUModelBehavior.DEF_USES_ALL; // this is the default value
				else if (value.equals("uses") || value.equals("usesonly"))
					duModelBehavior = DUModelBehavior.USES_ALL;
				else {
					assert value.equals("none");
					duModelBehavior = DUModelBehavior.NONE;
				}
			}
			else if (arg.equals("-paramdefuses"))
				allowParmsRetUseDefs = true;
			else if(arg.equals("-normal")){
				
				
				
				// also enable def-use mode by hand, which will be used inside the reusable checker
				allowParmsRetUseDefs = true;
				
				if(!isChanges){
					isChanges = true;
				}else{
					throw new ConflictModelExpection("Conflict mode '-normal' and " + mode.toString());
				}
				mode = reusableMode.Normal;
				String className = args[1+argIdx];
				
				//clean the next parameter
				// Note cannot be "", it may incur lost parameter;
				args[1+argIdx] = " ";
				
				
				normalClassList = new HashSet(Arrays.asList(className.split(":")));
			}
			else if(arg.equals("-hadoop")){
				
				
				// also enable def-use mode, which will be used inside the reusable checker
				allowParmsRetUseDefs = true;
				
				if(!isChanges){
					isChanges = true;
				}else{
					throw new ConflictModelExpection();
				}
				mode = reusableMode.Hadoop;
			}
			
			else
			{
				if (arg.equals("--help")) {
					System.out.println("vreHadoop Options:");
					System.out.println("\t-entry:<class>\tSpecifies the class where to find 'main'");
					System.out.println("\t-normal:nables normal pipeline reusable detection analysis without using any patch (default: false)");
					System.out.println("\t\t|-hadoop\tEnables Hadoop reusable detection (default: false)");
					System.out.println("\t-paramdefuses\tConsiders uses at call site parameters and returns, and defs at method entry parameters (default: false)");
					
				}
				argsSoot.add(arg);
			}
		}
	
		
		
		String[] filteredArgs = new String[argsSoot.size()];
		return argsSoot.toArray(filteredArgs);
	}
	private static String findjarsReplaceWithFiles(File subclassPath) {
		// TODO Auto-generated method stub
		String alljarPaths = "";
		File directory = subclassPath;
		File[]lists = directory.listFiles();
		for (int i = 0; i < lists.length; i++) {
		      if (lists[i].isFile() && lists[i].getName().endsWith(".jar")) {
		    	  alljarPaths+=lists[i].getAbsolutePath();
		    	  alljarPaths+=":";
		      }else if(lists[i].isDirectory()){
		    	  String replace = findjarsReplaceWithFiles(lists[i]);
		    	  if(!replace.equals("")){
		    		  alljarPaths+=findjarsReplaceWithFiles(lists[i]);
		    		  alljarPaths+=":";
		    	  }
		      }
		}
		if(!alljarPaths.isEmpty())
			alljarPaths = alljarPaths.substring(0, alljarPaths.length()-1);
		return alljarPaths;
	}
	public static String entryClassName()
	{
		return entryClassName; 
	}
	public static reusableMode getMode(){
		return mode;
	}
	public static Set<String> getComparedinNormal(){
		return normalClassList;
	}
	public static boolean allowParmsRetUseDefs() { return allowParmsRetUseDefs; }
	
	public static DUModelBehavior duModelBehavior() { return duModelBehavior; }
}
