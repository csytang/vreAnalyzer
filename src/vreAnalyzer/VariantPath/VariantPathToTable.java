package vreAnalyzer.VariantPath;

import java.awt.Shape;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import vreAnalyzer.UI.NonEditableModel;
import vreAnalyzer.Util.Graphviz.ImageDisplay;
import vreAnalyzer.Variants.Variant;

public class VariantPathToTable {
	// 单例模式
	public static VariantPathToTable instance;
	// 从表格行 对应到 路径id
	private static Map<Integer,Integer> rowToPathId = new HashMap<Integer,Integer>();
	// 从路径id 对应到 图片文件上
	private static Map<Integer,File> pathIdToImgFile = new HashMap<Integer,File>();
	// 对应 从路径id 到 variantfile上
	private static Map<Integer,Set<File>> pathIdToVarFiles = new HashMap<Integer,Set<File>>();
	
	private boolean verbose = true;
	private static int tableRowIndex = 0;
	
	
	public static VariantPathToTable inst(){
		if(instance==null)
			instance = new VariantPathToTable();
		return instance;
	}
	
	private JTable variantPathTable;
	private NonEditableModel varitablePathModel;
	
	public VariantPathToTable(){
		/*
		 * "Path ID","Variants(in Id)"
		 */
		variantPathTable = MainFrame.inst().getVariantPathTable();
		varitablePathModel = (NonEditableModel) variantPathTable.getModel();
	}
	public void addARowToTable(int pathId,Set<Variant>variants,Set<File> variantfile){
		String idList = "";
		idList += "[";
		
		for(Variant variant:variants){
			idList += variant.getVariantId();
			idList += ",";
		}
		rowToPathId.put(VariantPathToTable.tableRowIndex, pathId);
		if(verbose){
			System.out.println("Add Table to row:"+VariantPathToTable.tableRowIndex+"\twith pathId:"+pathId);
		}
		if(variants.size()>=1){
			idList = idList.substring(0, idList.length()-1);
		}
		idList += "]";
		varitablePathModel.addRow(new Object[]{pathId,idList});
		pathIdToVarFiles.put(pathId, variantfile);
		VariantPathToTable.tableRowIndex++;
	}
	
	/**
	 * 加入路径的按键监听
	 */
	public void addPathListener(){
		pathIdToImgFile = VariantPathAnalysis.inst().getpathIdToImgFile();
		variantPathTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(variantPathTable.getSelectedRow() > -1){
					int selectedRow = variantPathTable.getSelectedRow();
					int pathId = rowToPathId.get(selectedRow);
					File img = pathIdToImgFile.get(pathId);
					JPanel imagePanel = new JPanel();
					try {
						ImageDisplay imdisplay = new ImageDisplay(img,imagePanel);
						MainFrame.inst().getImageDisplayPanel().setViewportView(imdisplay.getImagePanel());
						Set<File>variantFiles = pathIdToVarFiles.get(pathId);
						// 显示文件
						/*
						 * variant_sourceName.html
						 * 1. 是否会有多个响应？
						 * 2. 如果有如何处理
						 */
						if(variantFiles.size()==1){
							File selectedfile = null;
							for(File file:variantFiles){
								selectedfile = file;
							}
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
								content = Files.readAllLines(selectedfile.toPath(),Charset.defaultCharset());
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
						}else{
							//有多个文件绑定在这个path上
							System.err.println("Multiple files associated, check!!! for pathId:"+pathId);
							System.exit(-1);
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
	}
	
	/*
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
	
}
