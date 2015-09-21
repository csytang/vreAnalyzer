package vreAnalyzer.Variants;

import java.util.HashSet;
import java.util.Set;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import vreAnalyzer.ControlFlowGraph.DefUse.Variable.Variable;

public class PRBTag implements Tag{
	/**
	 * This means runtime binding for this statement and these variables are required.
	 */
	public static String TAG_NAME = "prtbind";
	private Set<Variable> variables;
	public PRBTag(Set<Variable>variables){
		this.variables = variables;
	}
	public PRBTag(Variable variable){
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
