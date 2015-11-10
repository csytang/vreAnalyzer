package vreAnalyzer.Util.Graphviz;

import java.io.File;

public class GraphvizController {
	
	GraphvizDotDraw graphvizdraw = null;
	String type = "png";
	String repesentationType= "dot";
	File out = null;
	public GraphvizController(){
		graphvizdraw = new GraphvizDotDraw();
	}
	
	public void startScript(){
		graphvizdraw.addln(graphvizdraw.start_graph());
	}
	
	public void addLine(String line){
		graphvizdraw.addln(line);
	}
	
	public void endScript(){
		graphvizdraw.addln(graphvizdraw.end_graph());
	}
	
	/**
	 * outPath中包含"/"
	 * fileName 为pure文件名
	 */
	public void drawGraph(String outpath,String fileName){
		graphvizdraw.increaseDpi();   // 106 dpi
		out = new File(outpath+"/"+fileName+"."+ type); 
		graphvizdraw.writeGraphToFile(graphvizdraw.getGraph(graphvizdraw.getDotSource(), type, repesentationType), out);
	}
	public File getOutputFile(){
		return out;
	}
}
