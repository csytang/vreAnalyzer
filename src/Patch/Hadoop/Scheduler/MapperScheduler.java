package Patch.Hadoop.Scheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.UnitBox;
import soot.jimple.Stmt;
import soot.util.Chain;
import vreAnalyzer.ControlFlowGraph.CFG;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Reuse.Scheduler.Scheduler;
import vreAnalyzer.Util.SootUtilities;

public class MapperScheduler implements Scheduler{

    

	
	private boolean verbose = false;
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
	public MapperScheduler(SootMethod srcMethod, SootMethod otherMethod,
			CFGNode[] commons) {
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
			method = integratedClass.getMethod(srcmethod.getSubSignature());
			//method = SootUtilities.searchForMethodByName(integratedClass,srcmethod.getName());
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
						if((newObject = (Unit)  bindings.get(oldObject)) != null ){
							// Add used local to all localset;
							if(oldObject instanceof Local){
								methodBody.getLocals().add((Local) bindings.get(oldObject));
							}
							
							box.setUnit(newObject);
						}
						
					}
					methodBody.getUnits().addFirst(stmtClone);
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
						if((newObject = (Unit)  bindings.get(oldObject)) != null ){
			             
							// Add used local to all localset;
							if(oldObject instanceof Local){
								methodBody.getLocals().add((Local) bindings.get(oldObject));
							}
							
							box.setUnit(newObject);
			                
						}
						
					}
					methodBody.getUnits().add(stmtClone);
				}
			}
			
		}

		
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

	public SootClass SootClassInUpdate() {
		// TODO Auto-generated method stub
		// 4. Wrap all other methods in src
		// Already copied by class copy
				
		// 4. Wrap all other methods in other
		for(SootMethod sm:other.getMethods()){
			if(sm != this.othermethod){
				if(!integratedClass.declaresMethod(sm.getSubSignature())){
					integratedClass.addMethod(SootUtilities.methodClone(sm));
					SootUtilities.changeTypesInMethods(integratedClass,other,integratedClass);
				}
			}
		}


				
		return this.integratedClass;
	}
	
	
	

}
