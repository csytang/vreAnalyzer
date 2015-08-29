package vreAnalyzer.Util;

import soot.jimple.Stmt;
import soot.tagkit.Tag;
import soot.tagkit.Host;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.SourceLineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.SootMethod;
import soot.Unit;

import java.util.Collection;

/**
 *   Stores which class and member a particular sooty thing belongs to, and
 * attempts to extract line number information
 * Used for debugging purposes / communicating with the user.
 */
public class SourceLocation {
    private String className;
    private String memberName;
    private Host hostName;
    // If debugging information is available, these will be assigned
    public String filePath = "";
    public int lineNumber = -1;
    public int startPos = 0;
    public int endPos = 0;
    public int endlineNumber = -1;
    // Determine the line location of a particular Host, and glean className /
    // memberName information from the passed SootMember.
    public SourceLocation(SootMethod member, Unit h) {
        className = member.getDeclaringClass().getName();
        memberName = member.getName();
        hostName = h;
        SourceLnPosTag slnpt = (SourceLnPosTag) h.getTag("SourceLnPosTag");
        if (slnpt != null) {
            System.out.println("   @ line: " + slnpt.startLn());
        }
    }


    
}