package vreAnalyzer.InformationRetrieve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import soot.SootClass;
import soot.SootMethod;
import vreAnalyzer.Blocks.BlockGenerator;
import vreAnalyzer.Blocks.ClassBlock;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Blocks.MethodBlock;
import vreAnalyzer.Blocks.SimpleBlock;
import vreAnalyzer.CSV.CSVWriter;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Variants.BindingResolver;

public class FullBasicInfoToCSV {
	/*
	 * Package
	 * Class
	 * Method
	 * Code Block
	 * Variant
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
		 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type
		 */
		fullInfoWriter = new CSVWriter(outputDirectory+"/full.csv");
	}
	
	public void run(){
		// sootmethod 到 sootmethod内中 code block的
		Map<SootMethod,Set<SimpleBlock>> simpleCodeBlockpool = BlockGenerator.inst().getSimpleCodeBlockMap();
		
		// sootclass 到 sootclass block的对应
		Map<SootClass,ClassBlock> classCodeBlockpool = BlockGenerator.inst().getClassCodeBlockMap();
		
		// sootmethod 到 methodblock 的对应
		Map<SootMethod,MethodBlock> methodCodeBlockpool = BlockGenerator.inst().getMethodBlockMap();
		
		// variantId 的 blocksIds
		Map<Integer,Set<Integer>> variantIdToblockIds = BindingResolver.inst().getvariantIdToBlockIds();
		
		// 转化为 block to Variant ids
		Map<Integer,Set<Integer>> blockIdToVariantId = convertVaraintwithBlock(variantIdToblockIds);
		
		// 写入标题
		fullInfoWriter.println("Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs");
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
			/*
			 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs"
			 */
			packInfoTxt += index+",";// Id
			packInfoTxt += packageName+",";// Name
			packInfoTxt += "Package,";// Type(P,C,M,CB)
			packInfoTxt += "-,";// Parent Id
			packInfoTxt += "-,";// Parent Name
			packInfoTxt += "-,";// Parent Type
			packInfoTxt += "-,";// Code Range
			packInfoTxt += "-,";// Variant
			
			fullInfoWriter.println(packInfoTxt);
			int packageId = index;
			index++;
			
			// 加入在这个package内部的类
			Set<SootClass> packclassset = packageWithClasses.get(packageName);
			for(SootClass cls:packclassset){
				/*
				 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs"
				 */
				String clsInfoTxt = new String();
				clsInfoTxt += index+",";// Id
				clsInfoTxt += cls.getName()+",";// Name
				clsInfoTxt += "Class,";// Type(P,C,M,CB)
				clsInfoTxt += packageId+",";// Parent Id
				clsInfoTxt += packageName+",";// Parent Name
				clsInfoTxt += "Package,";// Parent Type
				clsInfoTxt += "\""+classCodeBlockpool.get(cls).getCodeRange()+"\",";// code range
				clsInfoTxt += "-,";// variant belongings
				
				fullInfoWriter.println(clsInfoTxt);
				int classId = index;
				index++;
				
				// 写入这个Class中的method
				for(SootMethod method:cls.getMethods()){
					int methodId = 0;
					// 判断是不是app method
					if(ProgramFlowBuilder.inst().getAppConcreteMethods().contains(method)){
						/*
						 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs"
						 */
						MethodBlock methodblock = methodCodeBlockpool.get(method);
						//Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type
						String methodInfoTxt = new String();
						methodInfoTxt += index+",";// id
						methodInfoTxt += method.getName()+",";// name
						methodInfoTxt += "Method,";// type
						methodInfoTxt += classId+",";// parentId
						methodInfoTxt += cls.getName()+",";//parent name
						methodInfoTxt += "Class,";// parent type
						if(methodblock==null){
							methodInfoTxt += "[],";
						}else{
							String codeRange = methodblock.getCodeRange();
							methodInfoTxt += "\""+codeRange+"\",";// code range
						}
						methodInfoTxt += "-";// variant belongs
						
						fullInfoWriter.println(methodInfoTxt);
						methodId = index;
						index++;
					}
					
					// 在这个Method下加入下面的code block
					Set<SimpleBlock> methodCodeBlockSet = simpleCodeBlockpool.get(method);
					if(methodCodeBlockSet!=null){
						for(CodeBlock cblock:methodCodeBlockSet){
							/*
							 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs"
							 */
							String simpleInfoTxt = new String();
							simpleInfoTxt += index+",";// id
							simpleInfoTxt += "block_"+cblock.getBlockId()+",";// Name
							simpleInfoTxt += "Code Block,";// Type(P,C,M,CB)
							simpleInfoTxt += methodId+",";// Parent Id
							simpleInfoTxt += method.getName()+",";// Parent Name
							simpleInfoTxt += "Method,";// Parent type
							String codeRange = cblock.getCodeRange();
							simpleInfoTxt += "\""+codeRange+"\",";// Code Range
							// get variant this code block belongs to
							Set<Integer> variantIds = blockIdToVariantId.get(cblock.getBlockId());// Variant belongs
							simpleInfoTxt += "\""+covertVariantIdsToString(variantIds)+"\"";
							fullInfoWriter.println(simpleInfoTxt);
							index++;
						}
					}
				}
			}
		}
		fullInfoWriter.close();	
	}
	/*
	 * 将varianttoBlockids 转化为 blockto VariantIds
	 */
	private Map<Integer, Set<Integer>> convertVaraintwithBlock(Map<Integer, Set<Integer>> variantIdToblockIds) {
		// TODO Auto-generated method stub
		Map<Integer,Set<Integer>>blockIdToVariantIds  = new HashMap<Integer,Set<Integer>>();
		for(Map.Entry<Integer,Set<Integer>>entry:variantIdToblockIds.entrySet()){
			int variantId = entry.getKey();
			Set<Integer> blockIds = entry.getValue();
			for(int blockid:blockIds){
				if(blockIdToVariantIds.containsKey(blockIds)){
					blockIdToVariantIds.get(blockIds).add(variantId);
				}else{
					Set<Integer> variantIds = new HashSet<Integer>();
					variantIds.add(variantId);
					blockIdToVariantIds.put(blockid, variantIds);
				}
			}
		}
		return blockIdToVariantIds;
	}
	/**
	 * 转化variant的id到字符串
	 */
	public String covertVariantIdsToString(Set<Integer> variantIds){
		if(variantIds==null){
			return "[]";
		}
		String variantString = "[";
		for(int variantid:variantIds){
			variantString += variantid;
			variantString += ",";
		}
		if(variantIds.size()>=1){
			variantString = variantString.substring(0,variantString.length()-1);
		}
		variantString += "]";
		return variantString;
	}
}
