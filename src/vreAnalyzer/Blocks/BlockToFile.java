package vreAnalyzer.Blocks;

import vreAnalyzer.CSV.CSVWriter;

public class BlockToFile {
	public static BlockToFile instance = null;
	private CSVWriter blockwriter;
	private String outputDirectory = ".";
	public static BlockToFile inst(){
		if(instance ==null){
			instance = new BlockToFile();
		}
		return instance;
	}
	public BlockToFile(){
		blockwriter = new CSVWriter(outputDirectory+"/blocks.csv");
	}
	public void startWrite(){
		/*
		 * "Block ID","Code Range(Jimple)","Type","Method(IF)","Class","Parent BlockID","Original(Y/N)","Feature ID"
		 */
		blockwriter.println("Block ID,Code Range(Jimple),Type,Method(IF),Class,Parent BlockID,Original(Y/N),Feature ID");
	}
	public void writeRow(String blockId,String coderange,BlockType blockType,String methodsString,String classString,String parentblockIdString,String original,String featureIdString){
		blockwriter.println("\""+blockId+"\","+"\""+coderange+"\","+"\""+blockType+"\","+
	"\""+methodsString+"\","+"\""+classString+"\""+"\""+parentblockIdString+"\""+
				"\""+original+"\""+"\""+featureIdString+"\"");
	}
	public void endWrite(){
		blockwriter.close();
	}
	
}
