/*
 * Copyright (c) 2015 This software is delivered by The Hong Kong Polytechnic University,
 * Department of Computing,
 * Software Development and Management Laboratory.
 * @ author: Chris Tang(csytang(AT)comp.polyu.edu.hk)
 * @ author/contact: Hareton Leung(cshleung(AT)comp.polyu.edu.hk)
 *
 */

package vreAnalyzer.Variants;

import vreAnalyzer.Text2HTML.Text2HTML;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.List;
import java.util.Stack;


public class VariantHTMLFiles {
    private DefaultMutableTreeNode varianthtmllist;
    
    /*
     * 根据文件创建HTML內容
     */
    
    public VariantHTMLFiles(List<File> sources){
        Stack<File> sourcefiles = new Stack<File>();
        sourcefiles.addAll(sources);
        varianthtmllist = new DefaultMutableTreeNode("variantanno_list");
        while(!sourcefiles.isEmpty()){
            File subfile = sourcefiles.pop();

            if(subfile.isDirectory()){
                File[]childdir = subfile.listFiles();
                for(File child:childdir){
                    sourcefiles.push(child);
                }
            }else{
                // only add .java file
                if(subfile.exists()&&
                        subfile.getPath().endsWith(".java")){
                    String htmlfileName = subfile.getPath().substring(0, subfile.getPath().length()-".java".length());
                    String[]splitsNames = htmlfileName.split("/");
                    String straightName = splitsNames[splitsNames.length-1];
                    htmlfileName = "";
                    for(int i = 0;i < splitsNames.length-1;i++){
                        htmlfileName += splitsNames[i];
                        htmlfileName += "/";
                    }

                    String replaceName = "variant_"+straightName;
                    htmlfileName += replaceName;
                    htmlfileName+=".html";
                    File htmlfile = new File(htmlfileName);
                    Text2HTML t2html = new Text2HTML(subfile, htmlfile);
                    DefaultMutableTreeNode htmlNode = new DefaultMutableTreeNode(htmlfile);
                    varianthtmllist.add(htmlNode);
                }
            }
        }
    }
    
    public DefaultMutableTreeNode getRootTreeNode(){
        return varianthtmllist;
    }
}
