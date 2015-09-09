package vreAnalyzer.CSV;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CSVWriter {
	PrintWriter pwriter = null;
	
	public CSVWriter(String file){
		try {
			pwriter = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void println(String line){
		pwriter.println(line);
	}
	public void close(){
		pwriter.close();
	}
}
