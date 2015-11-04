/*
  * Copyright (c) 2015 This software is delivered by The Hong Kong Polytechnic University,
 * Department of Computing,
 * Software Development and Management Laboratory.
 * @ author: Chris Tang(csytang(AT)comp.polyu.edu.hk)
 * @ author/contact: Hareton Leung(cshleung(AT)comp.polyu.edu.hk)
 *
 */

package vreAnalyzer.Variants;

import soot.SootClass;
import soot.SootMethod;
import soot.jimple.Stmt;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.UI.SourceClassBinding;
import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.Elements.CallSite;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class VariantAnnotate {
    boolean startFromSource;
    int[][]positions;
    int[]lines;
    private boolean hide = false;
    public static boolean variantready = false;
    private static List<Variant> shouldremoveVariants = new LinkedList<Variant>();
    public static void setvariantready(){
    	variantready = true;
    }
    public static List<Variant> getShouldBeHideVariants(){
    	return shouldremoveVariants;
    }
    public VariantAnnotate(Variant variant,String variantId, Color annotatedColor){
        startFromSource = vreAnalyzerCommandLine.isStartFromSource();
        // 先处理在Caller中的部分
        hide = true;
        List<Stmt> callerstmts = variant.getBindingStmts(null);
        if(startFromSource) {
            positions = new int[callerstmts.size()][4];
        }else{
            lines = new int[callerstmts.size()];
        }
        SootMethod callermethod = variant.getCallerMethod();
        SootClass cls = callermethod.getDeclaringClass();
        File sourceFile = SourceClassBinding.getSourceFileFromClassName(cls.toString());
		if(sourceFile==null){
			System.err.println("Cannot fine the class:\t"+cls.toString());
			return;
		}
		String htmlfileNametemp = sourceFile.getPath().substring(0, sourceFile.getPath().length()-".java".length());
		String htmlfileName = "";
		String[] subpatharray = htmlfileNametemp.split("/");
		for(int i = 0;i < subpatharray.length;i++){
			if(i!=(subpatharray.length-1)){
				htmlfileName += subpatharray[i];
				htmlfileName+="/";
			}else{
				htmlfileName += "variant_"+subpatharray[i];
				htmlfileName += ".html";
			}
		}
		File htmlFile = new File(htmlfileName);
		
        for(int i = 0;i < callerstmts.size();i++) {
        	Stmt stmt = callerstmts.get(i);
        	SourceLocationTag slcTag = (SourceLocationTag) stmt.getTag(SourceLocationTag.TAG_NAME);
        	if (startFromSource) {
                int startline = slcTag.getStartLineNumber();
                int startcolumn = slcTag.getStartPos();
                int endline = slcTag.getEndLineNumber();
                int endcolumn = slcTag.getEndPos();
                positions[i][0] = startline;
                positions[i][1] = startcolumn;
                positions[i][2] = endline;
                positions[i][3] = endcolumn;
            }else {
                int startline = slcTag.getStartLineNumber();
                lines[i] = startline;
            }
        }
        if(startFromSource) {
            if(!shouldbeHide(positions)){
            		hide = false;
            }
            HTMLAnnotation.annotatemultipleLineHTML_Variant(variant,variantId,htmlFile, positions, annotatedColor, MainFrame.inst().getHTMLToJava());
        }else{
            if(!shouldbeHide(lines)){
            	hide = false;
            }
            HTMLAnnotation.annotatemultipleLineHTML_Variant(variant,variantId,htmlFile, lines, annotatedColor, MainFrame.inst().getHTMLToJava());
        }
        
        
        // 处理在Callee中的部分
        // 1. 获得所有的callsite
        List<CallSite>callsiteList = variant.getCallSiteList();
        for(CallSite callsite:callsiteList) {
        	 List<Stmt> calleestmts = variant.getBindingStmts(callsite);
        	 if(startFromSource) {
                 positions = new int[calleestmts.size()][4];
             }else{
                 lines = new int[calleestmts.size()];
             }
             List<SootMethod> calleemethods = callsite.getAppCallees();
             for(SootMethod calleemethod:calleemethods){
            	 SootClass calleecls = calleemethod.getDeclaringClass();
            	 File calleesourceFile = SourceClassBinding.getSourceFileFromClassName(calleecls.toString());
            	 String calleehtmlfileNametemp = calleesourceFile.getPath().substring(0, calleesourceFile.getPath().length()-".java".length());
          		 String calleehtmlfileName = "";
          		 String[] calleesubpatharray = calleehtmlfileNametemp.split("/");
          		 for(int i = 0;i < calleesubpatharray.length;i++){
         			if(i!=(calleesubpatharray.length-1)){
         				calleehtmlfileName += calleesubpatharray[i];
         				calleehtmlfileName+="/";
         			}else{
         				calleehtmlfileName += "variant_"+calleesubpatharray[i];
         				calleehtmlfileName += ".html";
         			}
         		 }
          		 File calleehtmlFile = new File(calleehtmlfileName);
          		 for(int i = 0;i < calleestmts.size();i++) {
                 	Stmt stmt = calleestmts.get(i);
                 	SourceLocationTag slcTag = (SourceLocationTag) stmt.getTag(SourceLocationTag.TAG_NAME);
                 	if (startFromSource) {
                         int startline = slcTag.getStartLineNumber();
                         int startcolumn = slcTag.getStartPos();
                         int endline = slcTag.getEndLineNumber();
                         int endcolumn = slcTag.getEndPos();
                         positions[i][0] = startline;
                         positions[i][1] = startcolumn;
                         positions[i][2] = endline;
                         positions[i][3] = endcolumn;
                     }else {
                         int startline = slcTag.getStartLineNumber();
                         lines[i] = startline;
                     }
          		 }
                 if(startFromSource) {
                    if(!shouldbeHide(positions)){
                    	hide = false; 
                    }
                    HTMLAnnotation.annotatemultipleLineHTML_Variant(variant,variantId,calleehtmlFile, positions, annotatedColor, MainFrame.inst().getHTMLToJava());
                  }else{
                    if(!shouldbeHide(lines)){
                     	hide = false;
                    }
                    HTMLAnnotation.annotatemultipleLineHTML_Variant(variant,variantId,calleehtmlFile, lines, annotatedColor, MainFrame.inst().getHTMLToJava());
                 }
             }
        }
        if(hide){
        	// 刪除掉这个Variant
        	// BindingResolver.inst().removeHiddenVariant(variant);
        	shouldremoveVariants.add(variant);
        }
    }
	private boolean shouldbeHide(int[][] positions) {
		// TODO Auto-generated method stub
		for(int i = 0;i < positions.length;i++){
			for(int j =0; j < positions[0].length;j++){
				if(positions[i][j]>1)
					return false;
			}
		}
		return true;
	}
	private boolean shouldbeHide(int[] lines) {
		// 判断一个variant是否应该被隐藏
		
		for(int i:lines){
			if(i>1)
				return false;
		}
		return true;
		
	}
	
}
