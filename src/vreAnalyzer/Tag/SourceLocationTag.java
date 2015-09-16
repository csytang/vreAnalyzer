package vreAnalyzer.Tag;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Host;
import soot.tagkit.SourceLnPosTag;
import soot.tagkit.Tag;
import soot.SootMethod;
import soot.Unit;

/**
 *   Stores which class and member a particular sooty thing belongs to, and
 * attempts to extract line number information
 * Used for debugging purposes / communicating with the user.
 */
public class SourceLocationTag implements Tag {
	public enum LocationType {SOURCE_TAG,BYTE_TAG};
	public static String TAG_NAME = "slt";
    private String className;
    private String memberName;
    private Host hostName;
    private LocationType locType;
    // If debugging information is available, these will be assigned
    public String filePath = "";
    public int startlineNumber = -1;
    public int startPos = 0;
    public int endPos = 0;
    public int endlineNumber = -1;
    // Determine the line location of a particular Host, and glean className /
    // memberName information from the passed SootMember.
    public SourceLocationTag(SootMethod member, Unit h) {
        className = member.getDeclaringClass().getName();
        memberName = member.getName();
        hostName = h;
        SourceLnPosTag slnpt = (SourceLnPosTag) h.getTag("SourceLnPosTag");
        if(slnpt!=null){
        	// get line number tag from source code     	
        	startPos = slnpt.startPos();
        	endPos = slnpt.endPos();
        	startlineNumber = slnpt.startLn();
        	endlineNumber = slnpt.endLn();
        	locType = LocationType.SOURCE_TAG;
        }else{
        	// get line from bytecode
        	startlineNumber = h.getJavaSourceStartLineNumber();
        	h.getJavaSourceStartColumnNumber();
        	locType = LocationType.BYTE_TAG;
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
    public LocationType getTagType(){
    	return locType;
    }
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return SourceLocationTag.TAG_NAME;
	}
	@Override
	public byte[] getValue() throws AttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

    
}