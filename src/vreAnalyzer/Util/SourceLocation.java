package vreAnalyzer.Util;

import soot.tagkit.Host;
import soot.tagkit.SourceLnPosTag;
import soot.SootMethod;
import soot.Unit;

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
    public int startlineNumber = -1;
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
        if(slnpt!=null){
        	startPos = slnpt.startPos();
        	System.out.println("Start Pos:\t"+startPos);
        	endPos = slnpt.endPos();
        	System.out.println("End Pos:\t"+endPos);
        	startlineNumber = slnpt.startLn();
        	System.out.println("Start Line:\t"+startlineNumber);
        	endlineNumber = slnpt.endLn();
        	System.out.println("End Line:\t"+endlineNumber);
        }
    }
    public int getStartLineNumber(){
    	return startlineNumber;
    }
    public int getEndLineNumber(){
    	return endlineNumber;
    }
    public int getStartPos(){
    	return startPos;
    }
    public int getEndPos(){
    	return endPos;
    }

    
}