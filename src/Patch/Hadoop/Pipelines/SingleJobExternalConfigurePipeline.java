package Patch.Hadoop.Pipelines;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import soot.SootMethod;
import soot.Value;
import vreAnalyzer.ControlFlowGraph.DefUse.NodeDefUses;

public class SingleJobExternalConfigurePipeline {
	
	SootMethod setup;
	SootMethod sourceMethod;
	
	Object job;
	List<NodeDefUses>settings;
	boolean verbose = false;
	
	public SingleJobExternalConfigurePipeline(Object job,
			SootMethod appMethod,NodeDefUses use) {
		// TODO Auto-generated constructor stub
		this.job = job;
		
		this.sourceMethod = appMethod;
		this.settings = new LinkedList<NodeDefUses>();
		settings.add(use);
	}
	public void addSettingNode(NodeDefUses stmt){
		this.settings.add(stmt);
	}
	
	public SootMethod getSootMethod() {
		// TODO Auto-generated method stub
		return sourceMethod;
	}
	public List<NodeDefUses> extractSettings(){
		return settings;
	}
	public Map<NodeDefUses,NodeDefUses> getCommonAsset(SingleJobExternalConfigurePipeline otherJobexConfigure) {
		// TODO Auto-generated method stub
		Map<NodeDefUses,NodeDefUses>commons = new HashMap<NodeDefUses,NodeDefUses>();
		List<NodeDefUses>othersettings = otherJobexConfigure.extractSettings();
		
		for(int i = 0;i< this.settings.size();i++){
			NodeDefUses curr = this.settings.get(i);
			NodeDefUses other = othersettings.get(i);
			if(curr.getUsedVars().equals(other.getUsedVars())){
				commons.put(curr, other);
				if(verbose)
				{
					System.out.println("Same:\t"+curr.toString()+"\n\t"+other.toString());
				}
			}else{
				if(verbose)
					System.out.println("Not Same:\t"+curr.toString()+"\n\t"+other.toString());
			}
		}
		
		return commons;
		
	}
	
	
}
