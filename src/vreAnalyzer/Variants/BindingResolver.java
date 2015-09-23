package vreAnalyzer.Variants;

import soot.*;
import soot.jimple.AnyNewExpr;
import soot.jimple.IdentityRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import vreAnalyzer.ControlFlowGraph.DefUse.CFGDefUse;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;
import vreAnalyzer.ControlFlowGraph.DefUse.Use.Use;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;
import vreAnalyzer.Elements.CFGNode;
import vreAnalyzer.Elements.CallSite;
import vreAnalyzer.Elements.Location;
import vreAnalyzer.PointsTo.PointsToAnalysis;
import vreAnalyzer.PointsTo.PointsToGraph;
import vreAnalyzer.ProgramFlow.ProgramFlowBuilder;
import vreAnalyzer.Tag.MethodTag;
import vreAnalyzer.Tag.StmtTag;
import vreAnalyzer.UI.SourceClassBinding;
import vreAnalyzer.vreAnalyzerCommandLine;

import java.io.File;
import java.util.*;

public class BindingResolver {


	public static BindingResolver instance = null;
	private Map<SootMethod,List<List<Value>>>methodToArgs;
    private Map<Stmt, Value>firstStmtToValue;
	private boolean verbose = true;
    private List<Variant>variants = new LinkedList<Variant>();
    private Map<Value,Variant>valueToVariant = new HashMap<Value,Variant>();
	public static BindingResolver inst(){
		if(instance==null)
			instance = new BindingResolver();
		return instance;
	}
	public void parse(){
		/**
		 * Block ID,Code Range, FeatureID, Seperators, SootMethod,Class
		 */
		List<SootMethod> allAppMethods = ProgramFlowBuilder.inst().getAppConcreteMethods();
		methodToArgs = new HashMap<SootMethod,List<List<Value>>>();
        firstStmtToValue = new HashMap<Stmt,Value>();
		Set<SootMethod>calleeSet = new HashSet<SootMethod>();
		Set<Value>needRemove = new HashSet<Value>();
		Map<Value,List<Stmt>>methodTemp = new HashMap<Value,List<Stmt>>();
		Map<Set<AnyNewExpr>,Value>sitesToValue = new HashMap<Set<AnyNewExpr>,Value>();
		for(SootMethod method:allAppMethods){
			/**
			 * 1. 插入的時候進行檢查 如果value指向同一個地址 不用分開多個(fixed)
			 * 2. Call Site處有問題
			 *    是不是要有那種binding的才要處理 其他的不需要
			 */
			methodTemp.clear();
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(method);
			List<Local>parameterLocals = method.getActiveBody().getParameterLocals();
			List<Use>uses = cfg.getUses();
			NodeDefUses exitNode = (NodeDefUses) cfg.EXIT;
			vreAnalyzer.Context.Context exitContext = null;
			List<vreAnalyzer.Context.Context<SootMethod,CFGNode,PointsToGraph>> contexts = PointsToAnalysis.inst().getContexts(method);
			for(vreAnalyzer.Context.Context context:contexts){
				if(context.getValueBefore(exitNode)!=null)
					exitContext = context;
			}
			PointsToGraph graph = (PointsToGraph) exitContext.getValueBefore(exitNode);
			HashMap<Local,Set<AnyNewExpr>>roots = graph.getRoots();
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
										//Variant.addVarStmtMap(realbindingValue,bindingStmts);
										methodTemp.put(realbindingValue,bindingStmts);
									}
								}
								calleeSet.remove(method);
								needRemove.add(var.getValue());
							}

						}else{
							stmt.addTag(new PRBTag(var));
							//Variant.addVarStmtMap(var.getValue(), stmt);
							List<Stmt>bindingStmts = new LinkedList<Stmt>();
							bindingStmts.add(stmt);
							methodTemp.put(var.getValue(),bindingStmts);
						}
					}else {
						stmt.addTag(new PRBTag(var));
						//Variant.addVarStmtMap(var.getValue(), stmt);
						List<Stmt>bindingStmts = new LinkedList<Stmt>();
						bindingStmts.add(stmt);
						methodTemp.put(var.getValue(),bindingStmts);
					}
				} else {
					stmt.addTag(new LBTag(var));
					/**
					 * remove currently
					 */
					//Variant.addVarStmtMap(var.getValue(), stmt);
				}
			}

			// add all temped var-stmt map into {@link Variant}mapvlToStmt
			/*
			 * if two values are points to same location, just add one
			 */
			for(Map.Entry<Value,List<Stmt>>entry:methodTemp.entrySet()){
				Value vi = entry.getKey();
				List<Stmt>stmts = entry.getValue();
				if(vi instanceof Local){
					Local vilocal = (Local)vi;
					Set<AnyNewExpr>sites = roots.get(vilocal);
					if(sitesToValue.containsKey(sites)){
						Value bindvi = sitesToValue.get(sites);
						Variant bindvariant = valueToVariant.get(bindvi);
						bindvariant.addPaddingValue(vi);

						bindvariant.addBindingStmts(stmts);
						valueToVariant.put(vi, bindvariant);
					}else{
						Variant variant = new Variant(vi,stmts);
						valueToVariant.put(vi,variant);
						sitesToValue.put(sites,vi);
					}
				}
			}

			MethodTag mTag = (MethodTag)method.getTag(MethodTag.TAG_NAME);
			for(CallSite callsite:mTag.getAllCalleeSites()){
				Location srcLoc = callsite.getLoc();
				Stmt srcInvokeStmt = srcLoc.getStmt();


				InvokeExpr invokeExpr = srcInvokeStmt.getInvokeExpr();
				List<Value>argus = invokeExpr.getArgs();
				for(SootMethod callee:callsite.getAppCallees()){
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
			CFGDefUse cfg = (CFGDefUse)ProgramFlowBuilder.inst().getCFG(callee);
			/*
			   If there is no activebody for the callee, skip it
			   since no need need to binding the caller parameters with callee arguments
			 */
			if(!callee.hasActiveBody()||
					cfg==null)
				continue;
			Body activeBody = callee.getActiveBody();
			if(activeBody==null)
				continue;
			List<Local>parameterLocals = activeBody.getParameterLocals();
			List<Use>uses = cfg.getUses();
			// Annotate and classify all local and argument
			for(Use u:uses) {
				CFGNode node = u.getSrcNode();
				Variable var = u.getVar();
				/*
				    if it is not inside the variant value site no need to consider
				    since it doesnot invoke in a binding issue;
				  */
				if(!valueToVariant.containsKey(var.getValue()))
					continue;
				Stmt stmt = node.getStmt();
				if (u.getValue().getType() instanceof RefType || u.getValue().getType() instanceof ArrayType) {
					if(methodToArgs.containsKey(callee)){
						if(var.getValue() instanceof IdentityRef){
							List<List<Value>>listValues = methodToArgs.get(callee);
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
					//Variant.addVarStmtMap(var.getValue(), stmt);
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
		if(verbose)
			System.out.println("#Variants by Value:"+variants.size());

	}

	/**
	 * 1. current solution, first we all annotated all the files(testing)
	 * 2. seperate stmts list into combination of code blocks
	 */
	public void annotate() {
		if (vreAnalyzerCommandLine.isSourceBinding() &&
				vreAnalyzerCommandLine.isStartFromGUI())
		{
			Map<SootClass, List<Stmt>> classVariantStmtMap = new HashMap<SootClass, List<Stmt>>();
			for (Variant variant : variants) {
				List<Stmt> bindingStmts = variant.getBindingStmts();
				for (Stmt stmt : bindingStmts) {
					StmtTag stmtTag = (StmtTag) stmt.getTag(StmtTag.TAG_NAME);
					SootClass cls = stmtTag.getSootMethod().getDeclaringClass();
					if (classVariantStmtMap.containsKey(cls)) {
						classVariantStmtMap.get(cls).add(stmt);
					} else {
						List<Stmt> stmts = new LinkedList<Stmt>();
						stmts.add(stmt);
						classVariantStmtMap.put(cls, stmts);
					}
				}
			}
			for (Map.Entry<SootClass, List<Stmt>> entry : classVariantStmtMap.entrySet()) {
					SootClass cls = entry.getKey();
					List<Stmt> stmts = entry.getValue();
					File sourceFile = SourceClassBinding.getSourceFileFromClassName(cls.toString());
					String htmlfileName = sourceFile.getPath().substring(0, sourceFile.getPath().length() - ".java".length());
					int startIndex = htmlfileName.lastIndexOf("/");
					startIndex += 1;
					String realName = htmlfileName.substring(startIndex);
					realName = "variant_" + realName;
					htmlfileName = htmlfileName.substring(0, startIndex) + realName;
					htmlfileName += ".html";
					File htmlFile = new File(htmlfileName);
					VariantAnnotate variantAnnotate = new VariantAnnotate(stmts, htmlFile);
				}

			}
	}
}
