package vreAnalyzer.InformationRetrieve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Hierarchy;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import vreAnalyzer.Blocks.BlockGenerator;
import vreAnalyzer.Blocks.ClassBlock;
import vreAnalyzer.Blocks.CodeBlock;
import vreAnalyzer.Blocks.MethodBlock;
import vreAnalyzer.Blocks.SimpleBlock;
import vreAnalyzer.CSV.CSVWriter;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Variants.BindingResolver;
import vreAnalyzer.Variants.ConditionCheck;

public class FullBasicInfoToCSV {
	/*
	 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs,ContainVariantBrach,VariantBrachStart,VaraintBranchOrder" 
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
		fullInfoWriter = new CSVWriter(outputDirectory+"/full.csv");
	}
	
	public void run(){
		// sootmethod 到 sootmethod内中 code block的
		Map<SootMethod,Set<SimpleBlock>> simpleCodeBlockpool = BlockGenerator.inst().getSimpleCodeBlockMap();
		
		// sootclass 到 sootclass block的对应
		Map<SootClass,ClassBlock> classCodeBlockpool = BlockGenerator.inst().getClassCodeBlockMap();
		
		// sootmethod 到 methodblock 的对应
		Map<SootMethod,MethodBlock> methodCodeBlockpool = BlockGenerator.inst().getMethodBlockMap();
		
		// SootMethod 到 variants的 对应
		
		
		// variantId 的 blocksIds
		Map<Integer,Set<Integer>> variantIdToblockIds = BindingResolver.inst().getvariantIdToBlockIds();
		
		// 转化为 block to Variant ids
		Map<Integer,Set<Integer>> blockIdToVariantId = convertVaraintwithBlock(variantIdToblockIds);
		
		/*
		 *  SootMethod 到 ConditionalCheck
		 */
		Map<SootMethod,ConditionCheck> methodToConditionCheck = BindingResolver.inst().getmethodToConditionCheck();
		
		/**
		 * Id,
		 * Name,
		 * \"Type(P,C,M,CB)\",
		 * Parent Id,
		 * Parent Name,
		 * Parent Type,
		 * Code Range,
		 * CallerMethod/CalleeMethod,
		 * -Associated Caller/Callees-
		 * @ if it is a caller, put callee names
		 * @ if it is a callee, put caller name
		 * OverrideMethod,
		 * OverloadMethod,
		 * Variant Belongs/Contains,
		 * ContainVariantBrach,
		 * VariantBrachStart
		 */
		
		// 写入标题
		fullInfoWriter.println("Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,Associated caller/callees,OverrideMethod,OverloadMethod,Variant Belongs/Contains,ContainVariantBrach,VariantBrachStart");
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
		// 包 Package
		for(String packageName:packageWithClasses.keySet()){
			String packInfoTxt = new String();
			/*
			 *Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,Variant Belongs,ContainVariantBrach,VariantBrachStart"
			 *
			 */
			packInfoTxt += index+",";// Id
			packInfoTxt += packageName+",";// Name
			packInfoTxt += "Package,";// Type(P,C,M,CB)
			packInfoTxt += "-,";// Parent Id
			packInfoTxt += "-,";// Parent Name
			packInfoTxt += "-,";// Parent Type
			packInfoTxt += "-,";// Code Range
			packInfoTxt += "-,";// CallerMethod/CalleeMethod
			packInfoTxt += "-,";// Associated caller/callees
			//OverrideMethod,OverloadMethod,
			packInfoTxt += "-,"; // override method
			packInfoTxt += "-,"; // overload method;
			
			packInfoTxt += "-,";// Variant
			packInfoTxt += "?,";// ContainVariantBrach
			packInfoTxt += "-";// VariantBrachStart
			
			fullInfoWriter.println(packInfoTxt);
			int packageId = index;
			index++;
			
			// 类 Class
			Set<SootClass> packclassset = packageWithClasses.get(packageName);
			for(SootClass cls:packclassset){
				/*
				 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,OverrideMethod,OverloadMethod,Variant Belongs,ContainVariantBrach,VariantBrachStart"
				 */
				String clsInfoTxt = new String();
				clsInfoTxt += index+",";// Id
				clsInfoTxt += cls.getName()+",";// Name
				clsInfoTxt += "Class,";// Type(P,C,M,CB)
				clsInfoTxt += packageId+",";// Parent Id
				clsInfoTxt += packageName+",";// Parent Name
				clsInfoTxt += "Package,";// Parent Type
				clsInfoTxt += "\""+classCodeBlockpool.get(cls).getCodeRange()+"\",";// code range
				clsInfoTxt += "-,";// CallerMethod/CalleeMethod
				clsInfoTxt += "-,";// Associated caller/callees
				//OverrideMethod,OverloadMethod,
				clsInfoTxt += "-,"; // override method
				clsInfoTxt += "-,"; // overload method
				
				clsInfoTxt += "-,";// variant belongings
				clsInfoTxt += "?,";// ContainVariantBrach
				clsInfoTxt += "-";// VariantBrachStart
				
				fullInfoWriter.println(clsInfoTxt);
				int classId = index;
				index++;
				
				// 函数 Method 
				for(SootMethod method:cls.getMethods()){
					int methodId = 0;
					// 这个method上绑定的conditioncheck
					ConditionCheck conditionCheck = null;
					// 判断是不是app method
					if(ProgramFlowBuilder.inst().getAppConcreteMethods().contains(method)){
						/*
						 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod, OverrideMethod, OverloadMethod, Variant Belongs,ContainVariantBrach,VariantBrachStart"
						 */
						MethodBlock methodblock = methodCodeBlockpool.get(method);
						
						
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
						// CallerMethod/CalleeMethod
						// Associated caller/callees
						if(BindingResolver.inst().getCallerMethods().contains(method)){
							methodInfoTxt += "caller,";
							// get associated callees
							Set<SootMethod> callees = BindingResolver.inst().getCalleesForCaller(method);
							// convert method set into String
							String calleeString = convertMethodSetToString(callees);
							methodInfoTxt += calleeString;
							methodInfoTxt += ",";
						}else if(BindingResolver.inst().getCalleeMethods().contains(method)){
							methodInfoTxt += "callee,";
							// get associated caller
							Set<SootMethod> callers = BindingResolver.inst().getCallerForCallee(method);
							String callerString = convertMethodSetToString(callers);
							methodInfoTxt += callerString;
							methodInfoTxt += ",";
						}else{
							methodInfoTxt += "-,";
							methodInfoTxt += "-,";
						}						
						
						
						// OverrideMethod, OverloadMethod,
						// Override method
						boolean isoverridemethod = isoverride(method,method.getDeclaringClass());
						boolean isoverloadmethod = isoverload(method,method.getDeclaringClass());
						
						if(isoverridemethod){
							methodInfoTxt += "Y,";
						}else{
							methodInfoTxt += "N,";
						}
						
						if(isoverloadmethod){
							methodInfoTxt += "Y,";
						}else{
							methodInfoTxt += "N,";
						}
						
						
						
						methodInfoTxt += "-,";// variant contains
						
						// 这个method上绑定的conditioncheck
						conditionCheck = methodToConditionCheck.get(method);
						
						//ContainVariantBrach,VariantBrachStart
						boolean containsVariantBrach = false;
						
						String variantBrachStart = "-";// VariantBrachStart
						if(conditionCheck!=null){
							variantBrachStart = getBrachStartFromVariantList(conditionCheck,method);
							if(variantBrachStart!="-"){
								containsVariantBrach = true;
							}else{
								containsVariantBrach = false;
							}
						}
						if(containsVariantBrach){
							methodInfoTxt += "Y,";
						}else{
							methodInfoTxt += "N,";
						}
						
						
						variantBrachStart = "\""+variantBrachStart+"\"";
						methodInfoTxt += variantBrachStart;
						
						fullInfoWriter.println(methodInfoTxt);
						methodId = index;
						index++;
					}else{
						continue;
					}
					CFGNode conditionCFGNode = null;
					Map<CallSite,CFGNode> callsiteToCFGNodeMap = null;
					if(conditionCheck!=null){
						if(conditionCheck.isCallerMethod()){
							conditionCFGNode = conditionCheck.getinitConditionalCFGNode(null);
						}else{
							callsiteToCFGNodeMap = conditionCheck.getcalleeinitCFGNodes();
						}
					}
					
					// 代码块 Code Block
					Set<SimpleBlock> methodCodeBlockSet = simpleCodeBlockpool.get(method);
					if(methodCodeBlockSet!=null){
						for(CodeBlock cblock:methodCodeBlockSet){
							/*
							 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,Variant Belongs,ContainVariantBrach,VariantBrachStart"
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
							simpleInfoTxt += "-,";// CallerMethod or CalleeMethod
							// get variant this code block belongs to
							// OverrideMethod, OverloadMethod,
							simpleInfoTxt += "-,";// Associated caller/callees
							simpleInfoTxt += "-,";
							simpleInfoTxt += "-,";
							Set<Integer> variantIds = blockIdToVariantId.get(cblock.getBlockId());// Variant belongs
							simpleInfoTxt += "\""+covertVariantIdsToString(variantIds)+"\",";
														
							//ContainVariantBrach,VariantBrachStart
							boolean containsVariantBrach = false;
							
							String variantBrachStart = "-";
							
							if(conditionCheck!=null){
								if(conditionCheck.isCallerMethod()){
									//conditionCFGNode
									if(cblock.getCFGNodes().contains(conditionCFGNode)){
										// 条件语句在这个范围内
										Set<Value>values = conditionCheck.getinitConditionalValues(null);
										if(!values.isEmpty()){
											variantBrachStart = covertValueSetToString(values);
										}
									}else{
										// 条件语句不在这个范围内
										variantBrachStart = "-";
									}
								}else{
									//callsiteToCFGNodeMap
									variantBrachStart = "-";
									for(Map.Entry<CallSite, CFGNode>entry:callsiteToCFGNodeMap.entrySet()){
										CallSite site = entry.getKey();
										Set<Value> initConditionalValueSite = conditionCheck.getinitConditionalValues(site);
										variantBrachStart = covertValueSetToString(initConditionalValueSite,site,method);
									}
								}
							}
							if(variantBrachStart.equals("-")){
								containsVariantBrach = false;
							}else{
								containsVariantBrach = true;
							}
							
							if(containsVariantBrach){
								simpleInfoTxt += "Y,";
							}else{
								simpleInfoTxt += "N,";
							}
							
							variantBrachStart = "\""+variantBrachStart+"\"";
							simpleInfoTxt += variantBrachStart;
							
							fullInfoWriter.println(simpleInfoTxt);
							index++;
						}
					}
				}
			}
		}
		fullInfoWriter.close();	
	}
	
	/**
	 * 
	 * @param methodAssociatedVariants
	 * @return 
	 */
	private String getBrachStartFromVariantList(ConditionCheck conditionCheck,SootMethod method) {
		String branchOrder = "";
		boolean isCaller = conditionCheck.isCallerMethod();// 判断入口函数是否为caller函数 
		if(isCaller){
			Set<Value>conditionvalues = conditionCheck.getinitConditionalValues(null);
			if(conditionvalues.isEmpty()){
				return "";
			}else{
				branchOrder = "";
				branchOrder+="[";
				for(Value value:conditionvalues){
					branchOrder += value;
					branchOrder += ",";
				}
				if(conditionvalues.size()>=1){
					branchOrder = branchOrder.substring(0, branchOrder.length()-1);
				}
				branchOrder+="]";
			}
		}else{
			Map<CallSite,Set<Value>> calleeinitConditionalValues = conditionCheck.getcalleeinitConditionalValues();
			branchOrder = "";
			branchOrder+="[";
			for(Map.Entry<CallSite, Set<Value>>entry:calleeinitConditionalValues.entrySet()){
				CallSite callsite = entry.getKey();
				Set<Value> values = entry.getValue();
				if(values.isEmpty()){
					continue;
				}else{
					SootMethod callerMethod = callsite.getLoc().getMethod();
					branchOrder += callerMethod;
					branchOrder += "@";
					for(Value value:values){
						branchOrder += value;
						branchOrder += ",";
					}
					if(values.size()>=1){
						branchOrder = branchOrder.substring(0, branchOrder.length()-1);
					}
				}						
			}
			branchOrder+="]";
		}
		return branchOrder;
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
	
	public String covertValueSetToString(Set<Value>values){
		String valueString = "[";
		for(Value value:values){
			valueString += value.toString();
			valueString += ",";
		}
		if(values.size()>=1){
			valueString = valueString.substring(0,valueString.length()-1);
		}
		valueString += "]";
		return valueString;
	}
	public String covertValueSetToString(Set<Value>values,CallSite site,SootMethod callee){
		SootMethod caller = site.getCallCFGNode().getMethod();
		String valueString = "[";
		valueString += caller.getName();
		valueString += "->";
		valueString += callee.getName();
		valueString += ":";
		for(Value value:values){
			valueString += value.toString();
			valueString += ",";
		}
		if(values.size()>=1){
			valueString = valueString.substring(0,valueString.length()-1);
		}
		valueString += "]";
		return valueString;
	}
	public String convertMethodSetToString(Set<SootMethod>methods){
		String methodString = "[";
		for(SootMethod sm:methods){
			methodString += sm.getName();
			methodString += ",";
		}
		if(methods.size()>=1){
			methodString = methodString.substring(0, methodString.length()-1);
		}
		methodString += "]";
		return methodString;
	}
	/**
	 * 判断函数是否是重载函数
	 * @param method
	 * @param declaringClass
	 * @return
	 */
	private boolean isoverload(SootMethod method, SootClass declaringClass) {
		List<SootMethod>allmethods = declaringClass.getMethods();
		if(!allmethods.contains(method))
			return false;
		allmethods.remove(method);
		String methodName = method.getName();
		for(SootMethod smethod:allmethods){
			if(smethod.getName().equals(methodName) &&
					smethod.getModifiers()!=Modifier.VOLATILE)
				return true;
		}
		return false;
	}
	
	private boolean isoverride(SootMethod smethod, SootClass declaringClass) {
		Hierarchy hierarchy = Scene.v().getActiveHierarchy();
		
		if(declaringClass.isInterface()){
			List<SootClass> superinterfaces = hierarchy.getSuperinterfacesOf(declaringClass);
			if(smethod.getName().equals("<init>")){
				if(superinterfaces!=null){
					if(superinterfaces.size()==0)
						return false;
					else
						return true;
				}else
					return false;
			}
			if(superinterfaces!=null){
				for(SootClass cls:superinterfaces){
					List<SootMethod>methodlists =cls.getMethods();
					 for(SootMethod submethod:methodlists){
						 if(submethod.getName().equals(smethod.getName())){
							 return true;
						 }
					 }
				}
			}
		}else{
			List<SootClass> superclasses = hierarchy.getSuperclassesOf(declaringClass);
			if(smethod.getName().equals("<init>")){
				if(superclasses!=null){
					if(superclasses.size()==0)
						return false;
					else 
						return true;
				}
				else
					return false;
			}
			if(superclasses!=null){
				for(SootClass cls:superclasses){
					 List<SootMethod>methodlists =cls.getMethods();
					 for(SootMethod submethod:methodlists){
						 if(submethod.getName().equals(smethod.getName())){
							 return true;
						 }
					 }
				}
			}
		}
		
		return false;
	}
	
}
