/*
 * Copyright (c) 2015 This software is delivered by The Hong Kong Polytechnic University, Department of Computing, Software Development and Management Laboratory. @ author: Chris Tang(csytang(AT)comp.polyu.edu.hk)
 */

package vreAnalyzer.Variants;

import soot.*;
import soot.jimple.IdentityRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.Use;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.Elements.Location;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.MethodTag;

import java.util.*;

public class BindingResolver {
	private SootMethod method = null;
	private CFGDefUse cfg = null;
	public static BindingResolver instance = null;
	private Map<SootMethod,List<List<Value>>>methodToArgs;
    private Map<Stmt,Value>firstStmtToValue;
    private List<Variant>variants = new LinkedList<Variant>();
    private Map<Value,Variant>valueToVariant = new HashMap<Value,Variant>();
	public static BindingResolver inst(){
		if(instance==null)
			instance = new BindingResolver();
		return instance;
	}
	public void parse(List<SootMethod> allAppMethods){
		/**
		 * Block ID,Code Range, FeatureID, Seperators, SootMethod,Class
		 */
		methodToArgs = new HashMap<SootMethod,List<List<Value>>>();
        firstStmtToValue = new HashMap<Stmt,Value>();
		Set<SootMethod>calleeSet = new HashSet<SootMethod>();
		Set<Value>needRemove = new HashSet<Value>();
		for(SootMethod method:allAppMethods){
			this.method = method;
			this.cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<Local>parameterLocals = this.method.getActiveBody().getParameterLocals();
			List<Use>uses = cfg.getUses();
			// Annotate and classify all local and argument
			for(Use u:uses) {
				CFGNode node = u.getSrcNode();
				Variable var = u.getVar();
				Stmt stmt = node.getStmt();
				if (u.getValue().getType() instanceof RefType || u.getValue().getType() instanceof ArrayType) {
					if(methodToArgs.containsKey(method)){
						if(var.getValue() instanceof IdentityRef){
							List<List<Value>>listValues = methodToArgs.get(method);
							int index = -1;
							for(int i = 0;i < parameterLocals.size();i++){
								Local locali = parameterLocals.get(i);
								Value vallocali = (Value)locali;
								if(vallocali.equals(var.getValue())){
									index = i;
									break;
								}
							}
							if(index!=-1) {
								// get the number of argument/index
								List<Stmt>bindingStmts = Variant.getVarStmt(var.getValue());
								if(bindingStmts!=null) {
									for (List<Value> list : listValues) {
										Value realbindingValue = list.get(index);
										Variant.addVarStmtMap(realbindingValue,bindingStmts);
									}
								}
								calleeSet.remove(method);
								needRemove.add(var.getValue());
							}

						}else{
							stmt.addTag(new PRBTag(var));
							Variant.addVarStmtMap(var.getValue(), stmt);
						}
					}else {
						stmt.addTag(new PRBTag(var));
						Variant.addVarStmtMap(var.getValue(), stmt);
					}
				} else {
					stmt.addTag(new LBTag(var));
					Variant.addVarStmtMap(var.getValue(), stmt);
				}
			}
			MethodTag mTag = (MethodTag)method.getTag(MethodTag.TAG_NAME);
			for(CallSite callsite:mTag.getAllCallerSites()){
				List<SootMethod> allAppCallees = callsite.getAppCallees();
				Location srcLoc = callsite.getLoc();
				Stmt srcInvokeStmt = srcLoc.getStmt();
				InvokeExpr invokeExpr = srcInvokeStmt.getInvokeExpr();
				List<Value>argus = invokeExpr.getArgs();
				for(SootMethod callee:allAppCallees){
					if(methodToArgs.containsKey(callee)){
						List<List<Value>>lists = methodToArgs.get(callee);
						lists.add(argus);
						calleeSet.add(callee);
					}else{
						List<List<Value>>lists = new LinkedList<List<Value>>();
						lists.add(argus);
						methodToArgs.put(callee,lists);
						calleeSet.add(callee);
					}
				}
			}

		}
		// 1. If there still remains in
		for(SootMethod callee:calleeSet){
			this.method = callee;
			this.cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<Local>parameterLocals = this.method.getActiveBody().getParameterLocals();
			List<Use>uses = cfg.getUses();
			// Annotate and classify all local and argument
			for(Use u:uses) {
				CFGNode node = u.getSrcNode();
				Variable var = u.getVar();
				Stmt stmt = node.getStmt();
				if (u.getValue().getType() instanceof RefType || u.getValue().getType() instanceof ArrayType) {
					if(methodToArgs.containsKey(method)){
						if(var.getValue() instanceof IdentityRef){
							List<List<Value>>listValues = methodToArgs.get(method);
							int index = -1;
							for(int i = 0;i < parameterLocals.size();i++){
								Local locali = parameterLocals.get(i);
								Value vallocali = (Value)locali;
								if(vallocali.equals(var.getValue())){
									index = i;
									break;
								}
							}
							if(index!=-1) {
								// get the number of argument/index
								List<Stmt>bindingStmts = Variant.getVarStmt(var.getValue());
								if(bindingStmts!=null) {
									for (List<Value> list : listValues) {
										Value realbindingValue = list.get(index);
										Variant.addVarStmtMap(realbindingValue,bindingStmts);
									}
								}
								needRemove.add(var.getValue());
							}

						}else{
							stmt.addTag(new PRBTag(var));
							Variant.addVarStmtMap(var.getValue(), stmt);
						}
					}else {
						stmt.addTag(new PRBTag(var));
						Variant.addVarStmtMap(var.getValue(), stmt);
					}
				} else {
					stmt.addTag(new LBTag(var));
					Variant.addVarStmtMap(var.getValue(), stmt);
				}
			}
		}

		// 2.
		Iterator iterator = needRemove.iterator();
		while(iterator.hasNext()){
			Value vi = (Value)iterator.next();
			Variant.removeValue(vi);
		}

        // 3. create the variants for each vi and binding
        // 4. combine variants
        for(Map.Entry<Value,List<Stmt>>entry:Variant.getMapvlToStmt().entrySet()){

            Value vi = entry.getKey();
            List<Stmt>stmts = entry.getValue();
            if(firstStmtToValue.containsKey(stmts.get(0))){
                Variant existVar = valueToVariant.get(firstStmtToValue.get(stmts.get(0)));
                existVar.addPaddingValue(vi);
                existVar.addBindingStmts(stmts);
                valueToVariant.put(vi,existVar);
            }else{
                firstStmtToValue.put(stmts.get(0),vi);
                Variant variant = new Variant(vi,stmts);
                variants.add(variant);
                valueToVariant.put(vi,variant);
            }
        }





	}
}
