package vreAnalyzer.Variants;

import java.util.HashSet;
import java.util.Set;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;

public class LBTag implements Tag{
	public static String TAG_NAME = "ltbind";
	private Set<Variable> variables;
	public LBTag(Set<Variable>variables){
		this.variables = variables;
	}
	public LBTag(Variable variable){
		if(this.variables==null)
			this.variables = new HashSet<Variable>();
		this.variables.add(variable);
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
