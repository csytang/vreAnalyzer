package vreAnalyzer.UI;

import java.awt.Color;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class Annotator {
	Highlighter highlighter;
	HighlightPainter painter;
	public Annotator(JTextArea textArea){
		highlighter = textArea.getHighlighter();
	}
	public void annotate(Color color,int start,int end){
		painter = new DefaultHighlighter.DefaultHighlightPainter(color);
		try {
			highlighter.addHighlight(start, start, painter);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
