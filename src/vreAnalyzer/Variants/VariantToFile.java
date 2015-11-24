package vreAnalyzer.Variants;

import vreAnalyzer.CSV.CSVWriter;

public class VariantToFile {
	public static VariantToFile instance = null;
	private String outputDirectory = ".";
	private CSVWriter variantswriter;
	public static VariantToFile inst(){
		if(instance==null)
			instance = new VariantToFile();
		return instance;
	}
	public VariantToFile(){
		 variantswriter = new CSVWriter(outputDirectory+"/variant.csv");
	}
	public void startWrite(){
		/*
		 * "Variant ID","Block_Id","Code Range(jimple)","Separators","SootMethod","SootClass"
		 */
		variantswriter.println("Variant ID,Block_Id,Code Range(jimple),SootMethod,SootClass");
	}
	public void writeRow(String variantId,String blockidString,String coderange,String methodsString,String classString){
		variantswriter.println("\""+variantId+"\","+"\""+blockidString+"\","+"\""+coderange+"\","+"\""+methodsString+"\","+"\""+classString+"\"");
	}
	public void endWrite(){
		variantswriter.close();
	}
}
