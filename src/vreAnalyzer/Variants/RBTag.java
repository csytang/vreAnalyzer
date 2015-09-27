package vreAnalyzer.Variants;
import java.util.HashSet;
import java.util.Set;

import soot.Value;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class RBTag implements Tag{
	/**
	 * This means runtime binding for this statement and these variables are required.
	 */
	public static String TAG_NAME = "rtbind";
	private Set<Value> bindingvalues;
	public RBTag(Set<Value>values){
		this.bindingvalues = values;
	}
	public RBTag(Value value){
		if(this.bindingvalues==null)
			this.bindingvalues = new HashSet<Value>();
		this.bindingvalues.add(value);
	}
	public void addBindingValue(Value value){
		if(this.bindingvalues==null)
			this.bindingvalues = new HashSet<Value>();
		this.bindingvalues.add(value);
	}
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
