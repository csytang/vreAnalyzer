package vreAnalyzer.Variants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import soot.SootMethod;
import soot.Value;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import vreAnalyzer.Elements.CallSite;

public class RBTag implements Tag{
	/**
	 * This means runtime binding for this statement and these variables are required.
	 * Namely require binding tag
	 */
	public static String TAG_NAME = "rtbind";
	private boolean fromRemoteCaller = false;// 如果有

	private Map<SootMethod,Set<CallSite>>callerToSites = new HashMap<SootMethod,Set<CallSite>>();
	private Set<Value> callerbindingvalues = new HashSet<Value>();// 这个只在 调用函数 中使用
	private Map<CallSite,Set<Value>>calleebindingvalues = new HashMap<CallSite,Set<Value>>();// 这个只在 被调用函数 中使用
	
	public RBTag(Set<Value>bindingvalues,boolean fromRemoteCaller,SootMethod callerMethod,CallSite remoteCallSite){
		this.fromRemoteCaller = fromRemoteCaller;
		if(fromRemoteCaller){
			Set<CallSite>callSiteSet = new HashSet<CallSite>();
			callSiteSet.add(remoteCallSite);
			this.callerToSites.put(callerMethod, callSiteSet);
			this.calleebindingvalues.put(remoteCallSite, bindingvalues);
		}else{
			callerbindingvalues = bindingvalues;
		}
	}
	public RBTag(Value bindingvalue,boolean fromRemoteCaller,SootMethod callerMethod,CallSite remoteCallSite){
		this.fromRemoteCaller = fromRemoteCaller;
		if(fromRemoteCaller){
			Set<CallSite>callSiteSet = new HashSet<CallSite>();
			callSiteSet.add(remoteCallSite);
			this.callerToSites.put(callerMethod, callSiteSet);
			//binding value set
			Set<Value>values = new HashSet<Value>();
			values.add(bindingvalue);
			this.calleebindingvalues.put(remoteCallSite, values);
		}else{
			this.callerbindingvalues.add(bindingvalue);
		}
	}
	
	public void addBindingValue(Value value,CallSite remoteCallSite){
		if(fromRemoteCaller){
			// 如果当前有这个CallSite
			if(this.calleebindingvalues.containsKey(remoteCallSite)){
				this.calleebindingvalues.get(remoteCallSite).add(value);
			}
			// 如果当前没有这个CallSite
			else{
				Set<Value>bindingvalue = new HashSet<Value>();
				bindingvalue.add(value);
				this.calleebindingvalues.put(remoteCallSite, bindingvalue);
			}
		}else{
			this.callerbindingvalues.add(value);
		}
	}
	public void addBindingValue(Set<Value>values,CallSite remoteCallSite){
		if(fromRemoteCaller){
			// 如果当前 有这个CallSite
			if(this.calleebindingvalues.containsKey(remoteCallSite)){
				this.calleebindingvalues.get(remoteCallSite).addAll(values);
			}
			// 如果当前没有这个 CallSite
			else{
				this.calleebindingvalues.put(remoteCallSite, values);
			}
		}else{
			this.callerbindingvalues.addAll(values);
		}
	}
	public Set<Value> getBindingValues(CallSite remoteCallSite){
		if(fromRemoteCaller){
			if(this.calleebindingvalues.containsKey(remoteCallSite)){
				return this.calleebindingvalues.get(remoteCallSite);
			}else{
				return null;
			}
		}else{
			return this.callerbindingvalues;
		}
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
