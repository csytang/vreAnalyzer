package vreAnalyzer.Tag;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * Tag for each callsite, this tag will be added to the statement which is a call site
 */
public class CallSiteTag implements Tag{
	
	public static String TAG_NAME = "scs";
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return TAG_NAME;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		// TODO Auto-generated method stub
		return null;
	}

}
