package vreAnalyzer.UI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



public class SourceClassBinding{
	private static Map<String,File>classNameToSourceFile;
	private static SourceClassBinding instance;
	private static String clsPatternString = "";
	private static String sourcePatternString = "";
	private int index = 0;
	/**
	 * Create the dialog.
	 */
	public static SourceClassBinding inst(List<File>classes,List<File>source,String clsPattern,String sourcePattern){
		if(instance==null){
			instance = new SourceClassBinding(classes,source,clsPattern,sourcePattern);
		}
		return instance;
	}
	public static SourceClassBinding inst(){
		return instance;
	}
	public SourceClassBinding(List<File>classes,List<File>source,String clsPattern,String sourcePattern) {
		classNameToSourceFile = new HashMap<String,File>();
		SourceClassBinding.clsPatternString = clsPattern;
		SourceClassBinding.sourcePatternString = sourcePattern;
		startdirBinding(classes,source);		
	}
	public static File getSourceFileFromClassName(String className){
		Pattern clsPattern = Pattern.compile(SourceClassBinding.clsPatternString);
		for(Map.Entry<String, File>entry:classNameToSourceFile.entrySet()){
			String classFullName = entry.getKey();
			Matcher sourceMatcher = clsPattern.matcher(classFullName);
			if(!sourceMatcher.find()){
				continue;
			}
			String matchedPath = sourceMatcher.group(0);
			int firstMatch = classFullName.indexOf(matchedPath);
			String realName = classFullName.substring(matchedPath.length()+firstMatch, classFullName.length()-".class".length());
			// 1. replace all / to .
			realName = realName.replace("/", ".");
			if(realName.equals(className)){
				return entry.getValue();
			}
		}
		return null;
	}
	public void startdirNameBinding(List<String>classes,ArrayList<File>source){
		System.out.println("[vreHadoop] String dir NameBinding");
		
		index = 0;
		int totalsize = classes.size();
		Pattern clsPattern = Pattern.compile(SourceClassBinding.clsPatternString);
		for(String clsName:classes){
			//System.out.println("Class name:"+clsName);
			//String realName = clsName.substring(0+clsSysPathPattern.length(), clsName.length()-".class".length());
			Matcher clsMatcher = clsPattern.matcher(clsName);
			
			// If one is not under the class pattern, we now skip it
			if(!clsMatcher.find()){
				continue;
			}
			
			String matchedPath = clsMatcher.group(0);
			int firstMatch = clsName.indexOf(matchedPath);
			String realName = clsName.substring(matchedPath.length()+firstMatch, clsName.length()-".class".length());
			if(realName.contains("$")){
				realName = realName.substring(0, realName.indexOf("$"));
			}
			
			File result = allSearch(source,realName,matchedPath);
			if(result==null){
				System.err.println("Unable to find the class:\t"+clsName);
				continue;
			}
			
			classNameToSourceFile.put(clsName, result);
			MainFrame.inst().addBinding(clsName, result);
			 
			System.out.println("[vreHadoop] Binding class ["+clsName+"] with source ["+result.getAbsolutePath()+"]");
			index++;
		
		}
		
	}
	
	
	private File allSearch(ArrayList<File> source,String value,String matchedClassPath) {
		// TODO Auto-generated method stub
		for(File sourcefile:source){
			String path = sourcefile.getAbsolutePath();
			// 1. Use matcher
			Pattern sourcePattern = Pattern.compile(SourceClassBinding.sourcePatternString);
			Matcher sourceMatcher = sourcePattern.matcher(path);
			if(!sourceMatcher.find()){
				continue;
			}
			String matchedPath = sourceMatcher.group(0);
			int firstMatch = path.indexOf(matchedPath);
			String subpath = path.substring(matchedPath.length()+firstMatch, path.length()-".java".length());
			if(subpath.equals(value)){
				return sourcefile;
			}
		}
		return null;
		
		
	}
	// start binding from source
	public void startdirBinding(List<File>classes,List<File>source){
		System.out.println("[vreHadoop] Binding source code with jar/class files");
		ArrayList<File>sortedSource = new ArrayList<File>(source);
		Collections.sort(sortedSource,new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				// TODO Auto-generated method stub
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
			
			
		});
		
		List<String>classNames = new LinkedList<String>();
		
		
		for(File fi:classes){
			if(fi.getAbsolutePath().endsWith(".class")){
				String className = fi.getAbsolutePath();	
				classNames.add(className);
			}else if(fi.getAbsolutePath().endsWith(".jar")){
				String jarPath = fi.getAbsolutePath();
				jarPath = jarPath.substring(0, jarPath.lastIndexOf("/"));
				ZipInputStream zip;
				try {
					zip = new ZipInputStream(new FileInputStream(fi));
					for(ZipEntry entry=zip.getNextEntry();entry!=null;entry = zip.getNextEntry()){
						if(!entry.isDirectory() && entry.getName().endsWith(".class")){
								String className = entry.getName();
								// 1. If one is from jar file, it should attached the original prarent file path
								className = jarPath+"/"+className;
								classNames.add(className);
							}
					}
							
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
						
			}
		}
		startdirNameBinding(classNames,sortedSource);
		
	}
	
	
	
}
