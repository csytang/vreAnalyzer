package Patch.Hadoop;

import soot.SootClass;

public class MapReduceHub {
	
	private SootClass mapperClass;
	private SootClass reducerClass;
	
	
	public MapReduceHub(){
		
	}
	public void setMapperClass(SootClass mp){
		this.mapperClass = mp;
	}
	public void setReducerClass(SootClass re){
		this.reducerClass = re;
	}
	public SootClass getMapperClass(){
		return this.mapperClass;
	}
	public SootClass getReducerClass(){
		return this.reducerClass;
	}
}
