package vreAnalyzer.VariantPath;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.BlockView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import vreAnalyzer.UI.MainFrame;

public class PopupMenu{
	
	private JPopupMenu popup;
	private boolean allowShowingPopup = true;
	private boolean showPopup = false;
	private	Set<File> associatedFiles = new HashSet<File>();
	private Map<String,File> fileNameToFile = new HashMap<String,File>();
	
	//VariantPathToTable.java
	public PopupMenu(Set<File>files,JTable table){
		popup = new JPopupMenu();
		ActionListener menuListener = new ActionListener() {
		      public void actionPerformed(ActionEvent event) {
		    	  String pressedKey = event.getActionCommand();
		    	  File assocaiteFile = fileNameToFile.get(classNametoFileNamedecode(pressedKey));
		    	  
		    	 // 在屏幕上srctxtpanel上显示这个内容
					JEditorPane source_annotatedDisplayArea = MainFrame.inst().getSrcTextDisplayArea();
					source_annotatedDisplayArea.setContentType("text/html");
					source_annotatedDisplayArea.setEditorKit(new TooltipEditorKit());
					ToolTipManager.sharedInstance().registerComponent(source_annotatedDisplayArea);
					source_annotatedDisplayArea.addHyperlinkListener(new HyperlinkListener(){
					String tooltip;
						@Override
						public void hyperlinkUpdate(HyperlinkEvent e) {
							JEditorPane editor = (JEditorPane) e.getSource();
					        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
					          
					        }else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED){
						          tooltip = editor.getToolTipText();
						          javax.swing.text.Element elem = e.getSourceElement();
						          if (elem != null) {
							            AttributeSet attr = elem.getAttributes();
							            AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
							            if (a != null) {
							            	editor.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
							            }
						          }
					        }else if (e.getEventType() == HyperlinkEvent.EventType.EXITED){
					        	editor.setToolTipText(tooltip);
					        }
						}
					});
					List<String> content;
					try {
						content = Files.readAllLines(assocaiteFile.toPath(),Charset.defaultCharset());
						String allString = "";
						for(String subcontent:content){
							allString+=subcontent;
							allString+="\n";
						}
						source_annotatedDisplayArea.setText("");
						source_annotatedDisplayArea.setText(allString);
						source_annotatedDisplayArea.setCaretPosition(0);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		      }
		};
		associatedFiles = files;
		for(File associate:associatedFiles){
			String fileName = fileNametoClassNamedecode(associate.getName());
			fileNameToFile.put(associate.getName(), associate);
			JMenuItem item = new JMenuItem(fileName);
			item.addActionListener(menuListener);
			popup.add(item);
		}
		
		popup.setBorder(new BevelBorder(BevelBorder.RAISED));
		popup.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				allowShowingPopup = false;
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				allowShowingPopup = true;
			}
			
		});
		MousePopupListener mp = new MousePopupListener();
		table.addMouseListener(mp);
		MainFrame.inst().addMouseListener(mp);
	}
	
	// An inner class to check whether mouse events are the popup trigger
	class MousePopupListener extends MouseAdapter {
		    @Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
		    if(allowShowingPopup && !showPopup){
		    	checkPopup(e);
		    	showPopup = true;
		    }
		}
	    private void checkPopup(MouseEvent e) {
			if (e.getButton()==MouseEvent.BUTTON1) {
		    	popup.show(((JTable) e.getComponent()), e.getX(), e.getY());
		    	popup.requestFocus();
			    	
			}
		}
	}
	/**
	 * HTML 显示	
	 */
	class TooltipEditorKit extends HTMLEditorKit {
	    @Override public ViewFactory getViewFactory() {
	        return new HTMLFactory() {
	            @Override public View create(Element elem) {
	                AttributeSet attrs = elem.getAttributes();
	                Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
	                Object o = elementName==null ? attrs.getAttribute(StyleConstants.NameAttribute) : null;
	                if (o instanceof HTML.Tag) {
	                    HTML.Tag kind = (HTML.Tag) o;
	                    if (kind == HTML.Tag.DIV) {
	                        return new BlockView(elem, View.Y_AXIS) {
	                            @Override public String getToolTipText(float x, float y, Shape allocation) {
	                                String s = super.getToolTipText(x, y, allocation);
	                                if (s==null) {
	                                    s = (String) getElement().getAttributes().getAttribute(HTML.Attribute.TITLE);
	                                }
	                                return s;
	                            }
	                        };
	                    }
	                }
	                return super.create(elem);
	            }
	        };
	    }
	}
	public String fileNametoClassNamedecode(String fileName){
		return fileName.substring(0+"variant_".length(), fileName.length()-".html".length());
	}
	public String classNametoFileNamedecode(String className){
		return "variant_"+className+".html";
	}
}
