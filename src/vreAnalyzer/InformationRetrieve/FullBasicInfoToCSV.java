package vreAnalyzer.InformationRetrieve;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import soot.Hierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
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
	 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,Variant Belongs,ContainVariantBrach,VariantBrachStart[@Position],ConditionalVariable,IsVaraintCodeSegment" 
	 */
	public static FullBasicInfoToCSV instance;
	// package 对应所有的 app 类
	private Map<String,Set<SootClass>> packageWithClasses = new HashMap<String,Set<SootClass>>();
	// method 到 callsite
	private Map<SootMethod,List<CallSite>> callerMethodToCallSites = BindingResolver.inst().getCallerMethodToCallSites();
	private Map<SootMethod,Set<CallSite>> calleeMethodToCallSites = BindingResolver.inst().getCalleeMethodToCallSites();
	/*
	 *  SootMethod 到 ConditionalCheck
	 */
	private Map<SootMethod,ConditionCheck> methodToConditionCheck = BindingResolver.inst().getmethodToConditionCheck();
	public static FullBasicInfoToCSV inst(){
		if(instance==null)
			instance = new FullBasicInfoToCSV();
		return instance;
	}
	
	private String outputDirectory = ".";
	private CSVWriter fullInfoWriter = null;
	private int index = 1;
	
	
	private Set<SootMethod> callers = new HashSet<SootMethod>();
	private Set<SootMethod> callees = new HashSet<SootMethod>();
	
	
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
		 * VariantBrachStart[@position]
		 * IsVaraintCodeSegment
		 */
		
		// 写入标题
		fullInfoWriter.println("Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,Associated caller/callees,Strict OverrideMethod,Strict OverloadMethod,Variant Belongs/Contains,ContainVariantBrach,VariantBrachStart[@Position],IsVaraintCodeSegment");
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
			 *Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,Variant Belongs,ContainVariantBrach,VariantBrachStart[@Position],IsVaraintCodeSegment"
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
			packInfoTxt += "-,";// VariantBrachStart[@Position]
			
			packInfoTxt += "-";//IsVaraintCodeSegment
			
			fullInfoWriter.println(packInfoTxt);
			int packageId = index;
			index++;
			
			// 类 Class
			Set<SootClass> packclassset = packageWithClasses.get(packageName);
			for(SootClass cls:packclassset){
				/*
				 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod,OverrideMethod,OverloadMethod,Variant Belongs,ContainVariantBrach,VariantBrachStart[@Position],IsVaraintCodeSegment"
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
				clsInfoTxt += "-,";// VariantBrachStart[@Position],IsVaraintCodeSegment
				clsInfoTxt += "-";// IsVaraintCodeSegment
				
				
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
						 * Id,Name,\"Type(P,C,M,CB)\",Parent Id,Parent Name,Parent Type,Code Range,CallerMethod/CalleeMethod, OverrideMethod, OverloadMethod, Variant Belongs,ContainVariantBrach,VariantBrachStart[@Position],IsVaraintCodeSegment"
						 */
						MethodBlock methodblock = methodCodeBlockpool.get(method);
						boolean isCaller = false;
						boolean isCallee = false;
						boolean isNon = false;
						
						String methodInfoTxt = new String();
						methodInfoTxt += index+",";// id
						if(method.getName()=="<init>"){
							methodInfoTxt += cls.getName()+",";// name
						}else{
							methodInfoTxt += method.getName()+",";// name
						}
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
							callees = BindingResolver.inst().getCalleesForCaller(method);
							// convert method set into String
							String calleeString = convertMethodSetToString(callees);
							
							methodInfoTxt += "\""+calleeString+"\"";
							methodInfoTxt += ",";
							
							isCaller = true;
							isCallee = false;
						}else if(BindingResolver.inst().getCalleeMethods().contains(method)){
							methodInfoTxt += "callee,";
							// get associated caller
							callers = BindingResolver.inst().getCallerForCallee(method);
							String callerString = convertMethodSetToString(callers);
							methodInfoTxt += "\""+callerString+"\"";
							methodInfoTxt += ",";
							
							isCaller = false;
							isCallee = true;
						}else{
							methodInfoTxt += "-,";
							methodInfoTxt += "-,";
							
							isCaller = false;
							isCallee = false;
							isNon = true;
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
						//VariantBrachStart[@Position],IsVaraintCodeSegment
						
						variantBrachStart = "\""+variantBrachStart+"@["+method.getName()+"]"+"\"";
						Set<SootMethod> variantCodeSegmentSet;
						
						String segmentList = "";
						if(isCaller){
							variantCodeSegmentSet = variantCodeSegmentCheck(method,callees,isCaller,isCallee);
							if(variantCodeSegmentSet!=null){
								if(variantCodeSegmentSet.size()==0){
									segmentList = "[]";
								}else{
									segmentList = convertMethodSetToString(variantCodeSegmentSet);
								}
							}else{
								segmentList = "[]";
							}
						}else if(isCallee){
							variantCodeSegmentSet = variantCodeSegmentCheck(method,callers,isCaller,isCallee);
							if(variantCodeSegmentSet!=null){
								if(variantCodeSegmentSet.size()==0){
									segmentList = "[]";
								}else{
									segmentList = convertMethodSetToString(variantCodeSegmentSet);
								}
							}else{
								segmentList = "[]";
							}
						}

						methodInfoTxt += variantBrachStart+",";
						methodInfoTxt += "\""+segmentList+"\"";
						
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
							
							variantBrachStart = "\""+variantBrachStart+"@["+method.getName()+"]"+"\"";
							simpleInfoTxt += variantBrachStart + ",";
							
							//is segment
							simpleInfoTxt += "[]";
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
	 * @param method
	 * @param associatedcallercallees
	 * @param isCaller
	 * @param isCallee
	 * @return
	 */
	private Set<SootMethod> variantCodeSegmentCheck(SootMethod method,Set<SootMethod>associatedcallercallees,boolean isCaller,boolean isCallee) {
		// TODO Auto-generated method stub
		// 1. Caller函数 
		
		
		if(isCaller){
			Set<SootMethod> possiblecalleeVariants = new HashSet<SootMethod>();
			
			List<CallSite> allcallsites = callerMethodToCallSites.get(method);
			Map<SootMethod,Set<CallSite>> calleeToCallSites = new HashMap<SootMethod,Set<CallSite>>();
			for(CallSite callsite:allcallsites){
				for(SootMethod smethod:callsite.getAppCallees()){
					if(calleeToCallSites.containsKey(smethod)){
						calleeToCallSites.get(smethod).add(callsite);
					}else{
						Set<CallSite> callsites = new HashSet<CallSite>();
						callsites.add(callsite);
						calleeToCallSites.put(smethod, callsites);
					}
				}
			}
			ConditionCheck callercheck = methodToConditionCheck.get(method);
			if(callercheck==null)
				return null;
			Set<Value> conditionalValues = callercheck.getallConditionalValues(null);
			for(SootMethod calleemethod:associatedcallercallees){
				// 1.1 获得Callsite site
				Set<CallSite>callsites = calleeToCallSites.get(calleemethod);
				// 1.2 获得这个site的在callermethod的位置 
				for(CallSite site:callsites){
					CFGNode callCFGNode = site.getCallCFGNode();
					// 判断这个location在一个分支之中 
					// 1.3 如果conditioncheck中的check value是传入的参数则 返回真 否则 返回假
					Stmt srcStmt = callCFGNode.getStmt();
					InvokeExpr invokeExpr = srcStmt.getInvokeExpr();
					List<Value>args = invokeExpr.getArgs();
					for(Value arg:args){
						if(conditionalValues.contains(arg)){
							possiblecalleeVariants.add(calleemethod);
							break;
						}
					}
				}
				
			}
			return possiblecalleeVariants;
			
		}
		
		// 2. Callee函数
		else if(isCallee){
			Set<SootMethod> possiblecallerVariants = new HashSet<SootMethod>();
			Set<CallSite> sinkCallsites = calleeMethodToCallSites.get(method);
			
			for(SootMethod callermethod:associatedcallercallees){
				// 2.1 获得Callsite site
				ConditionCheck callercheck = methodToConditionCheck.get(callermethod);
				if(callercheck==null)
					continue;
				// 2.2 获得这个site的在callermethod的位置 
				List<CallSite> callsiteList = callerMethodToCallSites.get(callermethod);
				// 2.3 如果condition check中的check value是传入的参数则 返回真 否则 返回假
				if(callsiteList==null){
					continue;
				}
				for(CallSite site:callsiteList){
					if(sinkCallsites.contains(site)){
						Set<Value> values = callercheck.getallConditionalValues(site);
						if(values==null){
							continue;
						}
						CFGNode callCFGNode = site.getCallCFGNode();
						// 判断这个location在一个分支之中 
						// 1.3 如果conditioncheck中的check value是传入的参数则 返回真 否则 返回假
						Stmt srcStmt = callCFGNode.getStmt();
						InvokeExpr invokeExpr = srcStmt.getInvokeExpr();
						List<Value> args = invokeExpr.getArgs();
						for(Value arg:args){
							if(values.contains(arg)){
								possiblecallerVariants.add(callermethod);
								break;
							}
						}
					}
				}
				
				
			}
			
			return possiblecallerVariants;
		}
		return null;
		
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
			if(sm.getName().equals("<init>")){
				methodString += sm.getDeclaringClass().getName();
				methodString += "@[init]";
			}else{
				methodString += sm.getName();
			}
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
		if(!method.isConcrete()){
			return false;
		}
		List<SootMethod>allmethods = declaringClass.getMethods();
		if(!allmethods.contains(method))
			return false;
		allmethods.remove(method);
		String methodName = method.getName();
		for(SootMethod smethod:allmethods){
			if(smethod.getName().equals(methodName) &&
					smethod.isConcrete())
				return true;
		}
		return false;
	}
	
	// 复写父类函数
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
