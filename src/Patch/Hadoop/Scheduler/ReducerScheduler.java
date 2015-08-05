package Patch.Hadoop.Scheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Reuse.Scheduler.Scheduler;
import vreAnalyzer.Util.SootUtilities;

public class ReducerScheduler implements Scheduler{
	private boolean verbose = true;
	private SootClass integratedClass;
	
	
	private SootClass src;
	private SootClass other;

	private CFGNode[] reusableIndex;
	private SootMethod othermethod;
	private SootMethod srcmethod;
	private SootMethod method;
	/**
	 *  Here we keep the src name as original, when there is a name conflict, we change the name in others 
	 */
	private Map<Object,Object>bindings = null;
	
	public ReducerScheduler(SootMethod srcMethod, SootMethod otherMethod,
			CFGNode[] commons){
		// TODO Auto-generated constructor stub
				bindings = new HashMap<Object,Object>();
				src = srcMethod.getDeclaringClass();
				other = otherMethod.getDeclaringClass();
				this.othermethod = otherMethod;
				this.srcmethod = srcMethod;
				reusableIndex = commons;	
	}
	

	public void SootMethodIntegrate() {

		
		
		// 0. Create a integrated class based on these two class
		// load Object to support jvm support
        
		
		String className = "intclass_"+src.hashCode()+"_"+other.hashCode();
		
		
		integratedClass = SootUtilities.copyClass(src, className);
		
		bindings.putAll(SootUtilities.addTypesOfFields(integratedClass, other));

		// 1. Get the common method  first
		if(reusableIndex!=null && reusableIndex.length==4){
			
		

			method = SootUtilities.searchForMethodByName(integratedClass,srcmethod.getName());
			Body methodBody = method.retrieveActiveBody();
			bindings.putAll(SootUtilities.setBodyContentsFrom(methodBody,othermethod.retrieveActiveBody()));

			
			
			// 2. Get all local variable/ function parameter/ class fields in these two class 
			
			// 2.1 Fields (currently, it not support fields reuse) move to class reuse part
			
			// 2.2 Local variables in all methods 
			
			Chain<Local>otherLocals = othermethod.retrieveActiveBody().getLocals();
			
			
			for(Local otherlocal:otherLocals){
				// Whether the local is already existing in integrated class
				Local otherlocalShadow = (Local) otherlocal.clone();
				for(Local lo:methodBody.getLocals()){
					if(lo.getName().equals(otherlocalShadow.getName()))
					{
						otherlocalShadow.setName(otherlocal.getName()+"_"+other.hashCode());
						bindings.put(otherlocal, otherlocalShadow);
						break;
					}
				}
				methodBody.getLocals().add(otherlocalShadow);
			}
			
			if(verbose){
				System.out.println();
				System.out.println("All Local in Method:\t"+othermethod.getName()+"@"+other.getName());

				for(Local otherlocal:otherLocals){
					// Whether the local is already existing in integrated class
					System.out.print("\t"+otherlocal.getName());
				}
				System.out.println();
				
				System.out.println("All Local in Method:\t"+method.getName()+"@"+integratedClass.getName());
				
				for(Local inlocal:methodBody.getLocals()){
					System.out.print("\t"+inlocal.getName());
				}
				System.out.println();
			}

			SootUtilities.changeTypesOfFields(integratedClass, other, integratedClass);

			
			// 3.  Code compare and injection
			
			// 3.1 Current method
			
			// (1) First, all contents before the common area
			 
			CFG srcmethodCFG = ProgramFlowBuilder.inst().getCFG(srcmethod);
			CFG othermethodCFG = ProgramFlowBuilder.inst().getCFG(othermethod);
			// Write all contents before [Start] in src
			int srcStart = srcmethodCFG.getIndexId(reusableIndex[0]);
			//int srcEnd = srcmethodCFG.getIndexId(reusableIndex[1]);
			int otherStart = othermethodCFG.getIndexId(reusableIndex[2]);
			int otherEnd = othermethodCFG.getIndexId(reusableIndex[3]);

			
			// Write all contents before [Start] in other
			for(int i = otherStart-1;i >=0;i--){
				CFGNode othercfgNode = othermethodCFG.getNodes().get(i);
				if(!othercfgNode.isSpecial()){
					Stmt stmtClone = (Stmt) othercfgNode.getStmt().clone();
					if(verbose)
					{

						System.err.println("A stmt "+othermethod.getName()+"\t"+stmtClone.toString()+"\tadd to start");
						System.out.println();
					}
					
					for(UnitBox box:stmtClone.getUnitBoxes()){
						Unit newObject, oldObject = box.getUnit();
						if((newObject = (Unit)  bindings.get(oldObject)) != null )
			                box.setUnit(newObject);
						
					}
					methodBody.getUnits().addFirst(stmtClone);
				}
			}
			
			
			boolean shouldbeInsert = false;
			PatchingChain<Unit> methodUnits = methodBody.getUnits();
			
			Object[] methodUnitArray = methodUnits.toArray();
			
			/**
			 *  Check common asset in other class
			 *  We do this checking for following reasons:
			 *  1. The whole method in src has been copied to integrated method, but we can only ensure that the using of pointers are fully 
			 *  implemented with checking, but when we use a non pointer case,it cannot ensured and somewhat change the circumstance.
			 *  
			 *  ** Check the UseBox, if this time use is assigning a different value to this field, we should detect it.
			 *  It may include following cases (All types expect point2 can cover):
			 *  (1) Using of PrimType
			 *  (2) Using of RefLikeType
			 *  (3) 
			 *  
			 *  
			 *  
			 *  2. 
			 */
			for(int i = otherStart;i < otherEnd;i++){
					CFGNode cfgotherNode = othermethodCFG.getNodes().get(i);
					int index = i-otherStart+srcStart;
					
					
					// Special node does not need to write in program
					if(!cfgotherNode.isSpecial()){
						Stmt stmtClone = (Stmt) cfgotherNode.getStmt().clone();
						if(verbose)
						{
							System.out.println("A stmt "+othermethod.getName()+"\t"+stmtClone.toString());
							System.out.println();
						}
						shouldbeInsert = false;
						if (!(stmtClone instanceof IdentityStmt)) {
							for(ValueBox vbox:stmtClone.getUseAndDefBoxes()){
								Value key = vbox.getValue();
								// 如果是常量, 变量  等等  插入一个 赋值语句 使得不要影响 context
								// 使用in method type change中的方法重写
								
								/**
								 * 检查是否可以从CFGDefUse上直接获取信息
								 * 
								 */
								
								if(key instanceof FieldRef){
									FieldRef r = (FieldRef) key;
			                        SootFieldRef fieldRef = r.getFieldRef();
			                        if(fieldRef.type() instanceof PrimType){
			                        	SootField refField = other.getFieldByName(fieldRef.name());
			                        	if(bindings.containsKey(refField)){
			                        		SootField rmapfield = (SootField) bindings.get(r.getField());
			                        		r.setFieldRef(Scene.v().makeFieldRef(
			                        				rmapfield.getDeclaringClass(),
			                        				rmapfield.getName(), RefType.v(integratedClass),
			                        				rmapfield.isStatic()));
			                
											vbox.setValue(r);
											shouldbeInsert = true;
			                        	}
			                        }
								}else if(key instanceof Local){
									Local r = (Local) key;
									if(bindings.containsKey(r)){
		                        		Local rmapfield = (Local) bindings.get(r);
										vbox.setValue(rmapfield);
										shouldbeInsert = true;
		                        	}
										
								}else if(key instanceof ArrayRef){
									ArrayRef r = (ArrayRef)key;
									if(bindings.containsKey(r)){
										ArrayRef rmapfield = (ArrayRef) bindings.get(r);
										vbox.setValue(rmapfield);
										shouldbeInsert = true;
		                        	}
								}else if(key instanceof Constant){
									
								}
								else
									break;
							}
						}
						if(shouldbeInsert){
							if(verbose){
								System.out.println("Find one should insert203");
								System.out.println("Add a new statement:\t"+stmtClone);
							}
							methodBody.getUnits().insertAfter(stmtClone, (Unit)methodUnitArray[index]);
						}
						
					}
			}
			
			// Write other part follows common area
			for(int i  = otherEnd;i < othermethodCFG.getNodes().size();i++){
				CFGNode cfgNode =  othermethodCFG.getNodes().get(i);
				if(!cfgNode.isSpecial()){
					Stmt stmtClone = (Stmt) cfgNode.getStmt().clone();
					if(verbose)
					{
						System.err.println("A stmt "+othermethod.getName()+"\t"+stmtClone.toString()+"\t add to end");
						System.out.println();
					}
					
					for(UnitBox box:stmtClone.getUnitBoxes()){
						Unit newObject, oldObject = box.getUnit();
						if((newObject = (Unit)  bindings.get(oldObject)) != null )
			                box.setUnit(newObject);
						
					}
					methodBody.getUnits().add(stmtClone);
				}
			}
			
			
			
			//SootUtilities.changeTypesInMethods(integratedClass,other,integratedClass);
			
			
			// Clean up
	        
	        
	        
	        

			PatchingChain<Unit>integratedUnits = methodBody.getUnits();
			Iterator<Unit> unitIt = integratedUnits.iterator();

			if(verbose){
				System.out.print("All statement in method:\t"+method.getName()+"\n");
				integratedUnits = methodBody.getUnits();
				unitIt = integratedUnits.iterator();
				while(unitIt.hasNext()){
					Stmt st = (Stmt)unitIt.next();
					System.out.println("\t|"+st.toString());
				}
			}
		
			
		}
		
		
		
		
	}

	public SootClass SootClassInUpdate() {
		// TODO Auto-generated method stub
		// 4. Wrap all other methods in src
				for(SootMethod sm:src.getMethods()){
					if(sm != this.srcmethod){
						integratedClass.addMethod(SootUtilities.methodClone(sm));
						SootUtilities.changeTypesInMethods(integratedClass,src,integratedClass);
					}
				}
				
				// 4. Wrap all other methods in other
				for(SootMethod sm:other.getMethods()){
					if(sm != this.othermethod){
						integratedClass.addMethod(SootUtilities.methodClone(sm));
						SootUtilities.changeTypesInMethods(integratedClass,other,integratedClass);
					}
				}

				// 5. Clean this class up
				/**
				 *  5.1 
				 *  5.2 
				 *  5.3
				 *  5.4
				 *  5.5
				 *  
				 */
				
				
				
				return this.integratedClass;
	}

	public void MethodCleanUp() {
		// TODO Auto-generated method stub
		// After you clean this, check it again
		
				Body methodBody = method.retrieveActiveBody();
				Chain<Local> locals = methodBody.getLocals();
				PatchingChain<Unit>integratedUnits = methodBody.getUnits();
				Iterator<Unit> unitIt = integratedUnits.iterator();
				
				if(verbose){
					System.out.println("Class fields\n");
					for(SootField field:integratedClass.getFields()){
						System.out.println(field);
					}
					
					System.out.println("All locals:\n");
					for(Local loc:locals){
						System.out.println(loc);
					}
					
					System.out.print("All statement in method:\t"+method.getName()+"\n");
					integratedUnits = methodBody.getUnits();
					unitIt = integratedUnits.iterator();
					while(unitIt.hasNext()){
						Stmt st = (Stmt)unitIt.next();
						System.out.println("\t|"+st.toString());
					}
				}
	}

}
