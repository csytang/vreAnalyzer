/*
 * Copyright (c) 2015 This software is delivered by The Hong Kong Polytechnic University,
 * Department of Computing,
 * Software Development and Management Laboratory.
 * @ author: Chris Tang(csytang(AT)comp.polyu.edu.hk)
 * @ author/contact: Hareton Leung(cshleung(AT)comp.polyu.edu.hk)
 *
 */

package vreAnalyzer.Variants;

import soot.jimple.Stmt;
import vreAnalyzer.Tag.SourceLocationTag;
import vreAnalyzer.Text2HTML.HTMLAnnotation;
import vreAnalyzer.UI.MainFrame;
import vreAnalyzer.vreAnalyzerCommandLine;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Created by tangchris on 15/9/22.
 */
public class VariantAnnotate {
    boolean startFromSource;
    int[][]positions;
    int[]lines;
    public VariantAnnotate(List<Stmt> stmts,File htmlFile,Color annotatedColor){
        startFromSource = vreAnalyzerCommandLine.isStartFromSource();
        if(startFromSource){
            positions = new int[stmts.size()][4];
        }else{
            lines = new int[stmts.size()];
        }
        for(int i = 0;i < stmts.size();i++) {
            Stmt stmt = stmts.get(i);
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
            } else {
                int startline = slcTag.getStartLineNumber();
                lines[i] = startline;
            }
            if(startFromSource){
                HTMLAnnotation.annotatemultipleLineHTML(htmlFile, positions, annotatedColor, MainFrame.inst().getHTMLToJava());
            }else{
                HTMLAnnotation.annotatemultipleLineHTML(htmlFile, lines, annotatedColor, MainFrame.inst().getHTMLToJava());
            }

        }
    }
}
