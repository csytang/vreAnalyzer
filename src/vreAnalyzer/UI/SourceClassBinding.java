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
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



public class SourceClassBinding{
	private static Map<String,File>classNameToSourceFile;
	private static SourceClassBinding instance;
	private int index = 0;
	/**
	 * Create the dialog.
	 */
	public static SourceClassBinding inst(List<File>classes,List<File>source,String clsParent,String sourceParent){
		if(instance==null){
			instance = new SourceClassBinding(classes,source,clsParent,sourceParent);
		}
		return instance;
	}
	public static SourceClassBinding inst(){
		return instance;
	}
	public SourceClassBinding(List<File>classes,List<File>source,String clsParent,String sourceParent) {
		classNameToSourceFile = new HashMap<String,File>();
		startdirBinding(classes,source,clsParent,sourceParent);		
	}
	public static File getSourceFileFromClassName(String className){
		for(Map.Entry<String, File>entry:classNameToSourceFile.entrySet()){
			String classFullName = entry.getKey();
			String[]subName = classFullName.split("/");
			String name = subName[subName.length-1];
			if(name.substring(0, name.length()-".class".length()).equals(className))
				return entry.getValue();
		}
		return classNameToSourceFile.get(className);
	}
	public void startdirNameBinding(List<String>classes,ArrayList<File>source,String clsSysPathPattern,String sourceSysPathPattern){
		System.out.println("[vreHadoop] String dir NameBinding");
		
		index = 0;
		int totalsize = classes.size();
		
		for(String clsName:classes){
			System.out.println("Class name:"+clsName);
			String realName = clsName.substring(0+clsSysPathPattern.length(), clsName.length()-".class".length());
			if(realName.contains("$")){
				realName = realName.substring(0, realName.indexOf("$"));
			}
		
			
			File result = source.get(bindarySearch(source,sourceSysPathPattern,realName,0,source.size()-1));
			classNameToSourceFile.put(clsName, result);
			MainFrame.inst().addBinding(clsName, result);
			 
			System.out.println("[vreHadoop] Binding class ["+clsName+"] with source ["+result.getAbsolutePath()+"]");
			index++;
		
		}
		
	}
	private int bindarySearch(ArrayList<File> source,String sourceSystemPath,String value,int min, int max) {
		// TODO Auto-generated method stub
		if(min > max){
			return -1;
		}
		int	mid = (max+min)/2;
		
		String path = source.get(mid).getAbsolutePath();
		String subpath = path.substring(sourceSystemPath.length(), path.length()-".java".length());
		if(subpath.equals(value)){
			return mid;
		}else if(subpath.compareTo(value)>0){
			return bindarySearch(source,sourceSystemPath,value,min,mid-1);
		}else{
			return bindarySearch(source,sourceSystemPath,value,mid+1,max);
		}
		
	}
	
	public void startdirBinding(List<File>classes,List<File>source,String clsParent,String sourceParent){
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
				ZipInputStream zip;
				try {
					zip = new ZipInputStream(new FileInputStream(fi));
					for(ZipEntry entry=zip.getNextEntry();entry!=null;entry = zip.getNextEntry()){
						if(!entry.isDirectory() && entry.getName().endsWith(".class")){
								String className = entry.getName();
									
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
		startdirNameBinding(classNames,sortedSource,clsParent,sourceParent);
		
	}
	
	
	
}
