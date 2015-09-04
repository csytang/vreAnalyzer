package vreAnalyzer.Text2HTML;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class HTMLAnnotation {
	static Color annotateColor = null;
	// background <span style="background-color:red">xxx(text)</span>
	/**
	 * 
	 * @param htmlfile: a html file to be annotated by color marker
	 * @param startline start from 0
	 * @param startcolumn: 
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
			String startlinecontent = htmlbyline[startline-1];
			String endlinecontent = htmlbyline[endline-1];
			String spanStart = "<span style=\"background-color:"+hex+"\">";
			String spanEnd = "</span>";
			StringBuffer buffStart = new StringBuffer(startlinecontent);
			int startcolumninHTML = getColumnInHTML(startcolumn,startlinecontent);
			int endcolumninHTML = 0;
			buffStart.insert(startcolumninHTML, spanStart);
			if(startline==endline){
				endcolumninHTML = getColumnInHTML(endcolumn+spanStart.length(),startlinecontent);
				buffStart.insert(endcolumninHTML, spanEnd);
				htmlbyline[startline-1] = buffStart.toString();
			}else{
				endcolumninHTML = getColumnInHTML(endcolumn,endlinecontent);
				StringBuffer buffEnd = new StringBuffer(endlinecontent);
				buffEnd.insert(endcolumninHTML, spanEnd);
				htmlbyline[startline-1] = buffStart.toString();
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
	private static int getColumnInHTML(int startcolumn, String startlinecontent) {
		// TODO Auto-generated method stub
		int startcolumnInHTML = 0;
		int columncount = 0;
		int nbspCount = 0;
		char[]charset = startlinecontent.toCharArray();	
		String symbol = "";
		String tag = "";
		for(int i = 0;i < charset.length;i++){
			char c = charset[i];
			if(columncount==startcolumn)
				break;
			startcolumnInHTML++;
			if(c=='&'){
				symbol+=c;
				while(c!=';'){
					i++;
					startcolumnInHTML++;
					symbol+=c;
				}
				i++;
				startcolumnInHTML++;
				symbol+=c;
				switch(symbol){
				case"&lt;":{//<
					columncount++;
					break;
				}
				case "&gt;":{//>
					columncount++;
					break;
				}
				case "&amp;":{//&
					columncount++;
					break;
				}
				case "&quot;":{//"
					columncount++;
					break;
				}
				case "&nbsp;":{//
					nbspCount++;
					if(nbspCount==3){
						columncount+=6;
					}
					break;
				}
				}
			}else if(c=='<'){
				while(c!='>'){
					startcolumnInHTML++;
					i++;
					c = charset[i];
				}
			}
		}
		return startcolumnInHTML;
	}
	
	
	
}
