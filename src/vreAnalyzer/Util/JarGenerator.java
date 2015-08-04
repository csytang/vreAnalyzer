package vreAnalyzer.Util;

import java.io.IOException;

public class JarGenerator {
	public static JarGenerator instance = null;
	public JarGenerator(){
		
	}
	public static JarGenerator inst(){
		if(instance==null){
			instance = new JarGenerator();
		}
		return instance;
	}
	/**
	 * 
	 * @param filePath out classes
	 * @param outPut output jar path end with jar
	 * @throws IOException
	 */
	public void generateJar(String[]filePath,String outPut) throws IOException{
		assert(outPut.endsWith(".jar"));
		Process p;
		String allClasses = "";
		for(String file:filePath){
			assert(file.endsWith(".class"));
			allClasses+=file;
			allClasses+=" ";
		}
		String command = "jar cvf "+outPut+" "+allClasses;
		p = Runtime.getRuntime().exec(command);
	}
}
