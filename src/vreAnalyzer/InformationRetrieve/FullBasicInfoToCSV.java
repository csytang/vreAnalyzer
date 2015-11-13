package vreAnalyzer.InformationRetrieve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.CSV.CSVWriter;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;

public class FullBasicInfoToCSV {
	/*
	 * Package
	 * Class
	 * Method
	 * Code Block
	 * Variant
	 * Branch-->
	 */
	public static FullBasicInfoToCSV instance;
	// package 对应所有的 app 类
	private Map<String,Set<SootClass>> packageWithClasses = new HashMap<String,Set<SootClass>>();
	
	public static FullBasicInfoToCSV inst(){
		if(instance==null)
			instance = new FullBasicInfoToCSV();
		return instance;
	}
	
	private String outputDirectory = ".";
	private CSVWriter fullInfoWriter = null;
	private int index = 1;
	
	public FullBasicInfoToCSV(){
		/*
		 * Id, Name,Type(Package,Class,Method,CodeBlock),Parent Id,Parent Name,
		 */
		fullInfoWriter = new CSVWriter(outputDirectory+"/full.csv");
	}
	
	public void run(){
		// 写入标题
		fullInfoWriter.println("Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type");
		// 写入正文内容
		List<SootClass>appclasses = ProgramFlowBuilder.inst().getAppClasses();
		for(SootClass cls:appclasses){
			String packageName = cls.getPackageName();
			if(packageName==""){
				packageName = "(default package)";
			}
			if(packageWithClasses.containsKey(packageName)){
				packageWithClasses.get(packageName).add(cls);
			}else{
				Set<SootClass> clsSet = new HashSet<SootClass>();
				clsSet.add(cls);
				packageWithClasses.put(packageName, clsSet);
			}
		}
		
		for(String packageName:packageWithClasses.keySet()){
			String packInfoTxt = new String();
			packInfoTxt += index+",";
			packInfoTxt += packageName+",";
			packInfoTxt += "Package,";
			packInfoTxt += "-,";
			packInfoTxt += "-,";
			packInfoTxt += "-";
			fullInfoWriter.println(packInfoTxt);
			int packageId = index;
			index++;
			// 加入在这个package内部的类
			Set<SootClass> packclassset = packageWithClasses.get(packageName);
			for(SootClass cls:packclassset){
				String clsInfoTxt = new String();
				clsInfoTxt += index+",";
				clsInfoTxt += cls.getName()+",";
				clsInfoTxt += "Class,";
				clsInfoTxt += packageId+",";
				clsInfoTxt += packageName;
				fullInfoWriter.println(clsInfoTxt);
				int classId = index;
				index++;
				// 写入这个Class中的method
				for(SootMethod method:cls.getMethods()){
					// 判断是不是app method
					if(ProgramFlowBuilder.inst().getAppConcreteMethods().contains(method)){
						String methodInfoTxt = new String();
						methodInfoTxt += index+",";
						methodInfoTxt += method.getName()+",";
						methodInfoTxt += "Method,";
						methodInfoTxt += classId+",";
						methodInfoTxt += cls.getName()+",";
						fullInfoWriter.println(methodInfoTxt);
						index++;
					}
					// 在这个Method下加入下面的code block
					
					
					
				}
			}
		}
		
		
		
		fullInfoWriter.close();
		
	}
	
	
}
