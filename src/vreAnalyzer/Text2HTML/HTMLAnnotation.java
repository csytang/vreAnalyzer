package vreAnalyzer.Text2HTML;

import Patch.Hadoop.Job.ColorMap;
import Patch.Hadoop.Job.JobVariable;
import vreAnalyzer.Variants.Variant;
import vreAnalyzer.Variants.VariantColorMap;

import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTMLAnnotation {
	static Color annotateColor = null;
	private static JobVariable currJob;
	private static Variant currVar;
	// background <span style="background-color:red">xxx(text)</span>
	//////////////////////////////Feature/////////////////////////////////////
	public static void annotateHTML_Feature(JobVariable job,String hovertext,File htmlFile,int startline,int startcolumn,int endline,int endcolumn,Map<String,String> htmlToJava){
		try {
			currJob = job;
			String hex = job.getHexColor();
			FileReader htmlReader = new FileReader(htmlFile);
			
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			String startlinecontent = htmlbyline[startline-1];
			String endlinecontent = htmlbyline[endline-1];
			String spanStart = "<span title = \""+hovertext+"\""+" style=\"background-color:"+hex+"\">";
			String spanEnd = "</span>";
			StringBuffer buffStart = new StringBuffer(startlinecontent);
			String startjavaString = htmlToJava.get(startlinecontent + "<br>");
			String substartjavaString = startjavaString.substring(startcolumn-1, startjavaString.length());
			String starthtmlreverse = Text2HTML.txtToHtml(substartjavaString);
			if(starthtmlreverse.endsWith("<br>")){
				starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
			}
			int startcolumninHTML = startlinecontent.indexOf(starthtmlreverse);
			int endcolumninHTML = 0;
			buffStart.insert(startcolumninHTML, spanStart);
			if(startline==endline){
				substartjavaString = startjavaString.substring(startcolumn-1, endcolumn);
				starthtmlreverse = Text2HTML.txtToHtml(substartjavaString);
				if(starthtmlreverse.endsWith("<br>")){
					starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
				}
				endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
				buffStart.insert(endcolumninHTML, spanEnd);
				htmlbyline[startline-1] = buffStart.toString();
			}else{
				StringBuffer buffEnd = new StringBuffer(endlinecontent);
				String endjavaString = htmlToJava.get(endlinecontent + "<br>");
				String subendjavaString = endjavaString.substring(endcolumn - 1, endjavaString.length());
				String endhtmlreverse = Text2HTML.txtToHtml(subendjavaString);
				if(endhtmlreverse.endsWith("<br>")){
					endhtmlreverse = endhtmlreverse.substring(0, endhtmlreverse.length()-"<br>".length());
				}
				endcolumninHTML = endlinecontent.indexOf(endhtmlreverse);
				buffEnd.insert(endcolumninHTML, spanEnd);
				htmlbyline[startline-1] = buffStart.toString();
				htmlbyline[endline-1] = buffEnd.toString();
			}
			
			
			String line = "";
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
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
	public static void annotateHTML_Feature(JobVariable job, String hovertext,File htmlFile, int lineNumber, Map<String, String> htmlToJava) {
		// TODO Auto-generated method stub
		currJob = job;
		try {
			if(lineNumber<=1)
				return;
			
			String hex = job.getHexColor();
			FileReader htmlReader = new FileReader(htmlFile);
			
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			
			String linecontent = htmlbyline[lineNumber-1];
			
			String spanStart = "<span title = \""+hovertext+"\""+" style=\"background-color:"+hex+"\">";
			String spanEnd = "</span>";
			String updateline = "";
			if(!((updateline = isColorTitleAssociatedSLResolver_Feature(linecontent, spanStart)).equals(""))){
				htmlbyline[lineNumber-1] = updateline;
			}else{
				StringBuffer buffStart = new StringBuffer(linecontent);
				String startjavaString = htmlToJava.get(linecontent+"<br>");
				String starthtmlreverse = Text2HTML.txtToHtml(startjavaString);
				if(starthtmlreverse.endsWith("<br>")){
					starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
				}
				int startcolumninHTML = linecontent.indexOf(starthtmlreverse);
				int endcolumninHTML = 0;
				buffStart.insert(startcolumninHTML, spanStart);
				endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
				buffStart.insert(endcolumninHTML, spanEnd);
				htmlbyline[lineNumber-1] = buffStart.toString();
			}
			
			
			String line = "";
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
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
	public static void annotatemultipleLineHTML_Feature(String hovertext,File htmlFile,int[][] position,Color annotatedColor,Map<String,String> htmlToJava){
		try {
			annotateColor = annotatedColor;
			String hex = Integer.toHexString(annotateColor.getRGB() & 0xffffff);
			if (hex.length() < 6) {
			    hex = "0" + hex;
			}
			hex = "#" + hex;
			FileReader htmlReader = new FileReader(htmlFile);
			
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			for(int i = 0;i < position[0].length;i++){
				for(int j = 0;j < position[i].length;j++){
					int startline = position[i][0];
					int startcolumn = position[i][1];
					int endline = position[i][2];
					int endcolumn = position[i][3];
					String startlinecontent = htmlbyline[startline-1];
					String endlinecontent = htmlbyline[endline-1];
					String spanStart = "<span title = \""+hovertext+"\""+" style=\"background-color:"+hex+"\">";
					String spanEnd = "</span>";
					StringBuffer buffStart = new StringBuffer(startlinecontent);
					String startjavaString = htmlToJava.get(startlinecontent+"<br>");
					String substartjavaString = startjavaString.substring(startcolumn-1, startjavaString.length());
					String starthtmlreverse = Text2HTML.txtToHtml(substartjavaString);
					if(starthtmlreverse.endsWith("<br>")){
						starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
					}
					int startcolumninHTML = startlinecontent.indexOf(starthtmlreverse);
					int endcolumninHTML = 0;
					buffStart.insert(startcolumninHTML, spanStart);
					if(startline==endline){
						substartjavaString = startjavaString.substring(startcolumn-1, endcolumn);
						starthtmlreverse = Text2HTML.txtToHtml(substartjavaString);
						if(starthtmlreverse.endsWith("<br>")){
							starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
						}
						endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
						buffStart.insert(endcolumninHTML, spanEnd);
						htmlbyline[startline-1] = buffStart.toString();
					}else{
						StringBuffer buffEnd = new StringBuffer(endlinecontent);
						String endjavaString = htmlToJava.get(endlinecontent+"<br>");
						String subendjavaString = endjavaString.substring(endcolumn-1, endjavaString.length());
						String endhtmlreverse = Text2HTML.txtToHtml(subendjavaString);
						if(endhtmlreverse.endsWith("<br>")){
							endhtmlreverse = endhtmlreverse.substring(0, endhtmlreverse.length()-"<br>".length());
						}
						endcolumninHTML = endlinecontent.indexOf(endhtmlreverse);
						buffEnd.insert(endcolumninHTML, spanEnd);
						htmlbyline[startline-1] = buffStart.toString();
						htmlbyline[endline-1] = buffEnd.toString();
					}
					
				}
			}
			String line = "";
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
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
	public static void annotatemultipleLineHTML_Feature(String hovertext,File htmlFile,int[] lines,Color annotatedColor,Map<String,String> htmlToJava){
		try {
			annotateColor = annotatedColor;
			String hex = Integer.toHexString(annotateColor.getRGB() & 0xffffff);
			if (hex.length() < 6) {
			    hex = "0" + hex;
			}
			hex = "#" + hex;
			FileReader htmlReader = new FileReader(htmlFile);
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			for(int lineNumber:lines){
				if(lineNumber<=1)
					continue;
				String linecontent = htmlbyline[lineNumber-1];
				
				String spanStart = "<span title = \""+hovertext+"\""+" style=\"background-color:"+hex+"\">";
				String spanEnd = "</span>";
				String updateline="";
				if(!((updateline = isColorTitleAssociatedSLResolver_Feature(linecontent, spanStart)).equals(""))){
					htmlbyline[lineNumber-1] = updateline;
				}else{
					StringBuffer buffStart = new StringBuffer(linecontent);
					String startjavaString = htmlToJava.get(linecontent+"<br>");
					String starthtmlreverse = Text2HTML.txtToHtml(startjavaString);
					if(starthtmlreverse.endsWith("<br>")){
						starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
					}
					int startcolumninHTML = linecontent.indexOf(starthtmlreverse);
					int endcolumninHTML = 0;
					buffStart.insert(startcolumninHTML, spanStart);
					endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
					buffStart.insert(endcolumninHTML, spanEnd);
					htmlbyline[lineNumber-1] = buffStart.toString();
				}
			}
			String line = "";
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
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

	private static String isColorTitleAssociatedSLResolver_Feature(String linecontent,String spanStart){
		String colorpattern = new String("<span title = \".+\" style=\"background-color:.+\">");
		Pattern startPattern = Pattern.compile(colorpattern);
		Matcher matcher = startPattern.matcher(linecontent);
		if(matcher.find()){
			String matchedString = matcher.group(0);
			if(matchedString.equals(spanStart)){
				return linecontent;
			}else{
				String merged  = linecontent;
				// this may add a new color to this
				// 1. title index
				int ortitleindex = linecontent.indexOf("<span title = \"")+"<span title = \"".length();
				int ortitleendindex = linecontent.indexOf("\" style=\"background-color");
				int spantitleindex = spanStart.indexOf("<span title = \"")+"<span title = \"".length();
				int spantitleendindex = spanStart.indexOf("\" style=\"background-color");
				String spantitle = spanStart.substring(spantitleindex, spantitleendindex);
				String ortitle = linecontent.substring(ortitleindex, ortitleendindex);
				if(ortitle.contains(spantitle))
					return merged;
				// insert title first
				merged = new StringBuilder(merged).insert(ortitleindex, spantitle).toString();
				
				
				// 2. color index
				// Turn to CommonAsset find the mapping color
				
				int orcolorindex = merged.indexOf("\" style=\"background-color:")+"\" style=\"background-color:".length();
				int orcolorendindex = merged.indexOf("\">");
				String orcolor = merged.substring(orcolorindex, orcolorendindex);
				Set<JobVariable> jbvars = ColorMap.inst().getJobVariableFromhexColor(orcolor);
				if(!jbvars.contains(currJob)){
					Set<JobVariable>currjobs = new HashSet<JobVariable>();		
					currjobs.addAll(jbvars);
					currjobs.add(currJob);
					Color mergedColor = ColorMap.inst().getCombinedColor(currjobs);
					String hex = Integer.toHexString(mergedColor.getRGB() & 0xffffff);
					if (hex.length() < 6) {
					    hex = "0" + hex;
					}
					hex = "#" + hex;
					// add this mapping to the set
					ColorMap.inst().addHexColorToJob(hex, currjobs);
					merged = new StringBuilder(merged).replace(orcolorindex, orcolorendindex, hex).toString();
				}
				return merged;
			}
		}else{
			return "";
		}
	}
	///////////////////////////////////////////////////////////////////////
	
	//////////////////////////////Variant/////////////////////////////////
	public static void annotatemultipleLineHTML_Variant(Variant variant,String hovertext,File htmlFile,int[][] position,Color annotatedColor,Map<String,String> htmlToJava){
		try {
			currVar = variant;
			annotateColor = annotatedColor;
			String hex = Integer.toHexString(annotateColor.getRGB() & 0xffffff);
			if (hex.length() < 6) {
			    hex = "0" + hex;
			}
			hex = "#" + hex;
			FileReader htmlReader = new FileReader(htmlFile);
			
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			for(int i = 0;i < position[0].length;i++){
				for(int j = 0;j < position[i].length;j++){
					int startline = position[i][0];
					int startcolumn = position[i][1];
					int endline = position[i][2];
					int endcolumn = position[i][3];
					String startlinecontent = htmlbyline[startline-1];
					String endlinecontent = htmlbyline[endline-1];
					String spanStart = "<span title = \""+hovertext+"\""+" style=\"background-color:"+hex+"\">";
					String spanEnd = "</span>";
					StringBuffer buffStart = new StringBuffer(startlinecontent);
					String startjavaString = htmlToJava.get(startlinecontent+"<br>");
					String substartjavaString = startjavaString.substring(startcolumn-1, startjavaString.length());
					String starthtmlreverse = Text2HTML.txtToHtml(substartjavaString);
					if(starthtmlreverse.endsWith("<br>")){
						starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
					}
					int startcolumninHTML = startlinecontent.indexOf(starthtmlreverse);
					int endcolumninHTML = 0;
					buffStart.insert(startcolumninHTML, spanStart);
					if(startline==endline){
						substartjavaString = startjavaString.substring(startcolumn-1, endcolumn);
						starthtmlreverse = Text2HTML.txtToHtml(substartjavaString);
						if(starthtmlreverse.endsWith("<br>")){
							starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
						}
						endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
						buffStart.insert(endcolumninHTML, spanEnd);
						htmlbyline[startline-1] = buffStart.toString();
					}else{
						StringBuffer buffEnd = new StringBuffer(endlinecontent);
						String endjavaString = htmlToJava.get(endlinecontent+"<br>");
						String subendjavaString = endjavaString.substring(endcolumn-1, endjavaString.length());
						String endhtmlreverse = Text2HTML.txtToHtml(subendjavaString);
						if(endhtmlreverse.endsWith("<br>")){
							endhtmlreverse = endhtmlreverse.substring(0, endhtmlreverse.length()-"<br>".length());
						}
						endcolumninHTML = endlinecontent.indexOf(endhtmlreverse);
						buffEnd.insert(endcolumninHTML, spanEnd);
						htmlbyline[startline-1] = buffStart.toString();
						htmlbyline[endline-1] = buffEnd.toString();
					}
					
				}
			}
			String line = "";
			
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
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

	public static void annotatemultipleLineHTML_Variant(Variant variant,String hovertext,File htmlFile,int[] lines,Color annotatedColor,Map<String,String> htmlToJava){
		try {
			currVar = variant;
			annotateColor = annotatedColor;
			String hex = Integer.toHexString(annotateColor.getRGB() & 0xffffff);
			if (hex.length() < 6) {
			    hex = "0" + hex;
			}
			hex = "#" + hex;
			FileReader htmlReader = new FileReader(htmlFile);
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
			
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
			
			
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			for(int lineNumber:lines){
				if(lineNumber<=1)
					continue;
				String linecontent = htmlbyline[lineNumber-1];
				
				String spanStart = "<span title = \""+hovertext+"\""+" style=\"background-color:"+hex+"\">";
				String spanEnd = "</span>";
				String updateline="";
				if(!((updateline = isColorTitleAssociatedSLResolver_Variant(linecontent, spanStart)).equals(""))){
					htmlbyline[lineNumber-1] = updateline;
				}else{
					StringBuffer buffStart = new StringBuffer(linecontent);
					String startjavaString = htmlToJava.get(linecontent+"<br>");
					String starthtmlreverse = Text2HTML.txtToHtml(startjavaString);
					if(starthtmlreverse.endsWith("<br>")){
						starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
					}
					int startcolumninHTML = linecontent.indexOf(starthtmlreverse);
					int endcolumninHTML = 0;
					buffStart.insert(startcolumninHTML, spanStart);
					endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
					buffStart.insert(endcolumninHTML, spanEnd);
					htmlbyline[lineNumber-1] = buffStart.toString();
				}
			}
			String line = "";
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
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

	private static String isColorTitleAssociatedSLResolver_Variant(String linecontent,String spanStart){
		String colorpattern = new String("<span title = \".+\" style=\"background-color:.+\">");
		Pattern startPattern = Pattern.compile(colorpattern);
		Matcher matcher = startPattern.matcher(linecontent);
		if(matcher.find()){
			String matchedString = matcher.group(0);
			if(matchedString.equals(spanStart)){
				return linecontent;
			}else{
				String merged  = linecontent;
				// this may add a new color to this
				// 1. title index
				int ortitleindex = linecontent.indexOf("<span title = \"")+"<span title = \"".length();
				int ortitleendindex = linecontent.indexOf("\" style=\"background-color");
				int spantitleindex = spanStart.indexOf("<span title = \"")+"<span title = \"".length();
				int spantitleendindex = spanStart.indexOf("\" style=\"background-color");
				String spantitle = spanStart.substring(spantitleindex, spantitleendindex);
				String ortitle = linecontent.substring(ortitleindex, ortitleendindex);
				if(ortitle.contains(spantitle))
					return merged;
				// insert title first
				merged = new StringBuilder(merged).insert(ortitleindex, spantitle).toString();
				
				
				// 2. color index
				// Turn to CommonAsset find the mapping color
				
				int orcolorindex = merged.indexOf("\" style=\"background-color:")+"\" style=\"background-color:".length();
				int orcolorendindex = merged.indexOf("\">");
				String orcolor = merged.substring(orcolorindex, orcolorendindex);
				
				Set<Variant>varints = VariantColorMap.inst().getVariantFromhexColor(orcolor);
				if(!varints.contains(currVar)){
					Set<Variant>currvariants = new HashSet<Variant>();
					currvariants.addAll(varints);
					currvariants.add(currVar);
					Color mergedColor = VariantColorMap.inst().getCombinedColor(currvariants);
					String hex = Integer.toHexString(mergedColor.getRGB() & 0xffffff);
					if (hex.length() < 6) {
					    hex = "0" + hex;
					}
					hex = "#" + hex;
					VariantColorMap.inst().addHexColorToVariant(hex,currvariants);
					merged = new StringBuilder(merged).replace(orcolorindex, orcolorendindex, hex).toString();
				}
				return merged;
			}
		}else{
			return "";
		}
	}
	///////////////////////////////////////////////////////////////////////
	/*
	 * 单一颜色着色 
	 */
	public static void annotatemultipleLinesingleColor(File htmlFile,int[] lines,Color annotatedColor,Map<String,String> htmlToJava){
		annotateColor = annotatedColor;
		String hex = Integer.toHexString(annotateColor.getRGB() & 0xffffff);
		if (hex.length() < 6) {
		    hex = "0" + hex;
		}
		hex = "#" + hex;
		FileReader htmlReader;
		try {
			htmlReader = new FileReader(htmlFile);
		
			BufferedReader brhtml = new BufferedReader(htmlReader);
			String allhtmlcontent = "";
			String htmlline = "";
		
			while((htmlline = brhtml.readLine()) != null){				
				allhtmlcontent+=htmlline;
				allhtmlcontent+="\n";
			}
			brhtml.close();
			htmlReader.close();
		
		
			FileWriter htmlWriter = new FileWriter(htmlFile);
			BufferedWriter bwhtml = new BufferedWriter(htmlWriter);
			String[] htmlbyline = allhtmlcontent.split("<br>");
			for(int lineNumber:lines){
				if(lineNumber<=1)
					continue;
				String linecontent = htmlbyline[lineNumber-1];
			
				String spanStart = "<span style=\"background-color:"+hex+"\">";
				String spanEnd = "</span>";
				String updateline="";
				if(!((updateline = isColorTitleAssociatedSLResolver_Variant(linecontent, spanStart)).equals(""))){
					htmlbyline[lineNumber-1] = updateline;
				}else{
					StringBuffer buffStart = new StringBuffer(linecontent);
					String startjavaString = htmlToJava.get(linecontent+"<br>");
					String starthtmlreverse = Text2HTML.txtToHtml(startjavaString);
					if(starthtmlreverse.endsWith("<br>")){
						starthtmlreverse = starthtmlreverse.substring(0, starthtmlreverse.length()-"<br>".length());
					}
					int startcolumninHTML = linecontent.indexOf(starthtmlreverse);
					int endcolumninHTML = 0;
					buffStart.insert(startcolumninHTML, spanStart);
					endcolumninHTML = startcolumninHTML+starthtmlreverse.length()+spanStart.length();
					buffStart.insert(endcolumninHTML, spanEnd);
					htmlbyline[lineNumber-1] = buffStart.toString();
				}
			}
			String line = "";
			for(int i = 0;i < htmlbyline.length; i++){
				line = htmlbyline[i];
				if(i!=htmlbyline.length-1){
					line+="<br>";
				}
				bwhtml.write(line);
			}
			bwhtml.flush();
			bwhtml.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
