package Patch.Hadoop.Annotation;

import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import vreAnalyzer.UI.Annotator;
import vreAnalyzer.Util.SourceLocation;
import Patch.Hadoop.JobVariable;

public class JobAnnotation {
	public JobAnnotation(JobVariable job,Color color,JTextArea textArea) {
		Annotator annotor = new Annotator(textArea);
		SourceLocation sourceLoc = job.getSootStmtTag().getSourceLocation();
		int startpos = sourceLoc.getStartPos();
		int endpos = sourceLoc.getEndPos();
		int startline = sourceLoc.getStartLineNumber();
		int endline = sourceLoc.getEndLineNumber();
		try {
			int startIndex = textArea.getLineStartOffset(startline)+startpos;
			int endIndex = textArea.getLineStartOffset(endline)+endpos;
			annotor.annotate(color, startIndex, endIndex);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
