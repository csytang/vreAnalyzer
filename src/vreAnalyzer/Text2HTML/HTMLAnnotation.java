package vreAnalyzer.Text2HTML;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import vreAnalyzer.UI.RandomColor;

public class HTMLAnnotation {
	static Color annotateColor = null;
	// background <span style="background-color:red">xxx(text)</span>
	/**
	 * 
	 * @param htmlfile: a html file to be annotated by color marker
	 * @param startline
	 * @param startcolumn: start from 0
	 * @param endline
	 * @param endcolumn
	 * @param annotatedColor
	 */
	public static void annotateHTML(File htmlfile,int startline,int startcolumn,int endline,int endcolumn,Color annotatedColor){
		try {
			annotateColor = annotatedColor;
			String hex = Integer.toHexString(annotateColor.getRGB() & 0xffffff);
			if (hex.length() < 6) {
			    hex = "0" + hex;
			}
			hex = "#" + hex;
			FileReader htmlReader = new FileReader(htmlfile);
			
			BufferedReader brhtml = new BufferedReader(htmlReader);
			int linecount = 0;
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			
			FileWriter htmlWriter = new FileWriter(htmlfile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			String startlinecontent = htmlbyline[startline];
			String endlinecontent = htmlbyline[endline];
			String spanStart = "<span style=\"background-color:"+hex+"\">";
			String spanEnd = "</span>";
			StringBuffer buffStart = new StringBuffer(startlinecontent);
			buffStart.insert(startcolumn, spanStart);
			
			if(startline==endline){
				buffStart.insert(endcolumn+spanStart.length(), spanEnd);
				htmlbyline[startline] = buffStart.toString();
			}else{
				StringBuffer buffEnd = new StringBuffer(endlinecontent);
				buffEnd.insert(endcolumn, spanEnd);
				htmlbyline[startline] = buffStart.toString();
				htmlbyline[endline] = buffEnd.toString();
			}
			
			
			
			String line = "";
			for(String content:htmlbyline){
				line=content;
				line+="<br>";
				bwhtml.write(line);
			}
			bwhtml.flush();
			bwhtml.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
}
