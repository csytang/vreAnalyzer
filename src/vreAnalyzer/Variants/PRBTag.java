package vreAnalyzer.Variants;

import java.util.HashSet;
import java.util.Set;

import soot.Value;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

public class PRBTag implements Tag{
	/**
	 * This means runtime binding for this statement and these variables might be required currently, it might 
	 * relate to control flow relation. Namely partial require binding tag.
	 */
	public static String TAG_NAME = "prtbind";
	private Set<Value> bindingvalues;
	
	public PRBTag(Set<Value>values){
		this.bindingvalues = values;
		
	}
	public PRBTag(Value value){
		if(this.bindingvalues==null)
			this.bindingvalues = new HashSet<Value>();
		this.bindingvalues.add(value);
	}
	public void addBindingValue(Value value){
		if(this.bindingvalues==null)
			this.bindingvalues = new HashSet<Value>();
		this.bindingvalues.add(value);
	}
	public Set<Value> getBindingValues(){
		return this.bindingvalues;
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
