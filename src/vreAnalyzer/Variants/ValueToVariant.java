package vreAnalyzer.Variants;

import soot.SootMethod;
import soot.Value;

public class ValueToVariant {
	private SootMethod method;
	private Value value;
	private Variant variant;
	public ValueToVariant(Value value, Variant variant){
		this.value = value;
		this.variant = variant;	
	}
	public SootMethod getSootMethod(){
		return this.method;
	}
	public Value getValue(){
		return this.value;
	}
	public Variant getvariant(){
		return this.variant;
	}
}
