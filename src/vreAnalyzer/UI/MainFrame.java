package vreAnalyzer.UI;

import org.apache.commons.lang3.StringUtils;
import Patch.Hadoop.ProjectParser;
import Patch.Hadoop.Job.ColorMap;
import vreAnalyzer.Text2HTML.Text2HTML;
import vreAnalyzer.Variants.VariantAnnotate;
import vreAnalyzer.Variants.VariantColorMap;
import vreAnalyzer.Variants.VariantHTMLFiles;
import vreAnalyzer.vreAnalyzerCommandLine;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import javax.swing.text.html.BlockView;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	// 1. Instance
	private static MainFrame instance;
	
	// 2. Contents
	private JPanel contentPane;
	private List<File> target;
	private List<File> supporingjars;
	private List<File> sources;
	private final JSplitPane sourcesplitPane;
	private static JEditorPane source_annotatedDisplayArea;
	private final JTextArea consoletextArea;
	private final JTree source_annotateDirTree;
	private static int textArealinecount = 0;
	private DefaultMutableTreeNode root;
	
	// 3. 
	private String[] comm;
	private static Map<String,File> classnametoSource;
	private List<File> allsourcefiles = new LinkedList<File>();
	private boolean startFromSource = false;
	
	// 4. Output redirect
	PrintStream printStream;
	private final JTable legendtable;
	private static Map<String,String>htmlToJava;
	private final JTable statistictable;
	private JTable variantPathtable;
	private JTable reusetable;
	private JTable variantstable;
	private JTable blocktable;
	
	public static void main(String[] args) {
		classnametoSource = new HashMap<String,File>();
		htmlToJava = new HashMap<String,String>();
		inst();
	}

	/**
	 * Create the frame.
	 */
	public static MainFrame inst(){
		if(instance==null){
			instance = new MainFrame();
		}
		return instance;
	}
	
	// MainFrame 的构造函数
	public MainFrame() {
		setTitle("vreAnalyzer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 720);
		setLocationRelativeTo(null);
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewProjectPanel.inst(instance);
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmCopy = new JMenuItem("Copy");
		mnEdit.add(mntmCopy);
		
		JMenuItem mntmPaste = new JMenuItem("Paste");
		mnEdit.add(mntmPaste);
		
		JSeparator separator_1 = new JSeparator();
		mnEdit.add(separator_1);
		
		JMenuItem mntmTurnToLine = new JMenuItem("Turn to Line");
		mnEdit.add(mntmTurnToLine);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unused")
				About abtinstance = About.inst(instance);
			}
		});
		mnHelp.add(mntmAbout);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JSplitPane mainsplitPane = new JSplitPane();
		mainsplitPane.setResizeWeight(0.5);
		mainsplitPane.setOneTouchExpandable(true);
		mainsplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(mainsplitPane);
		
		sourcesplitPane = new JSplitPane();
		sourcesplitPane.setOneTouchExpandable(true);
		mainsplitPane.setLeftComponent(sourcesplitPane);
		
		source_annotateDirTree = new JTree();
		source_annotateDirTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Source directory") {
			{
				}
			}
		));
		
		JPanel sourcepanel = new JPanel();
		sourcesplitPane.setRightComponent(sourcepanel);
		sourcepanel.setLayout(new BorderLayout(0, 0));
		sourcesplitPane.setDividerLocation(160+sourcesplitPane.getInsets().left);
		
		JScrollPane source_annotateDisplayPane = new JScrollPane();
		sourcepanel.add(source_annotateDisplayPane, BorderLayout.CENTER);
		
		source_annotatedDisplayArea = new JEditorPane(){
			
			private final transient Position.Bias[] bias = new Position.Bias[1];
		    private transient HyperlinkListener listener;
			@Override public void updateUI() {
		        removeHyperlinkListener(listener);
		        super.updateUI();
		        listener = new HyperlinkListener() {
		            private String tooltip;
		            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
		                JEditorPane editor = (JEditorPane) e.getSource();
		                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		                    JOptionPane.showMessageDialog(editor, e.getURL());
		                } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
		                    tooltip = editor.getToolTipText();
		                    Element elem = e.getSourceElement();
		                    if (elem!=null) {
		                        AttributeSet attr = elem.getAttributes();
		                        AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
		                        if (a!=null) {
		                            editor.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
		                        }
		                    }
		                } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
		                    editor.setToolTipText(tooltip);
		                }
		            }
		        };
		        addHyperlinkListener(listener);
		    }
		    @Override
			public String getToolTipText(MouseEvent e) {
		        String title = super.getToolTipText(e);
		        JEditorPane editor = (JEditorPane) e.getComponent();
		        //HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
		        if (!editor.isEditable()) {
		            Point pt = new Point(e.getX(), e.getY());
		            int pos = editor.getUI().viewToModel(editor, pt, bias);
		            if (bias[0] == Position.Bias.Backward && pos > 0) {
		                pos--;
		            }
		            if (pos >= 0 && editor.getDocument() instanceof HTMLDocument) {
		                HTMLDocument hdoc = (HTMLDocument) editor.getDocument();
		                String str = getSpanTitleAttribute(hdoc, pos);
		                if (str!=null) {
		                    title = str;
		                }
		            }
		        }
		        return title;
		    }
		    private String getSpanTitleAttribute(HTMLDocument hdoc, int pos) {
		        //HTMLDocument hdoc = (HTMLDocument) editor.getDocument();
		        Element elem = hdoc.getCharacterElement(pos);
		        //if (!doesElementContainLocation(editor, elem, pos, e.getX(), e.getY())) {
		        //    elem = null;
		        //}
		        //if (elem != null) {
		        AttributeSet a = elem.getAttributes();
		        AttributeSet span = (AttributeSet) a.getAttribute(HTML.Tag.SPAN);
		        if (span!=null) {
		            return (String) span.getAttribute(HTML.Attribute.TITLE);
		        }
		        //}
		        return null;
		    }
			
		};
		ToolTipManager.sharedInstance().registerComponent(source_annotatedDisplayArea);
		source_annotatedDisplayArea.setEditable(false);
		source_annotateDisplayPane.setViewportView(source_annotatedDisplayArea);
		
		JScrollPane sourceDirPane = new JScrollPane();
		sourceDirPane.setViewportView(source_annotateDirTree);
		sourcesplitPane.setLeftComponent(sourceDirPane);
		
		JSplitPane analysissplitPane = new JSplitPane();
		analysissplitPane.setOneTouchExpandable(true);
		mainsplitPane.setRightComponent(analysissplitPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		analysissplitPane.setRightComponent(tabbedPane);
		analysissplitPane.setDividerLocation(160+analysissplitPane.getInsets().left);
		JScrollPane consolePane = new JScrollPane();
		tabbedPane.addTab("Console", new ImageIcon(MainFrame.class.getResource("/image/console.png")), consolePane, null);
		
		consoletextArea = new JTextArea();
		consoletextArea.setEditable(false);
		
		// Redirect output stream
		printStream = new PrintStream(new CustomOutputStream(consoletextArea));
		//System.setOut(printStream);
		//System.setErr(printStream);
        
		consolePane.setViewportView(consoletextArea);
		
		JScrollPane blocksPane = new JScrollPane();
		tabbedPane.addTab("Code Blocks", new ImageIcon(MainFrame.class.getResource("/image/blocks.png")), blocksPane, "Divide all codes into snippets by branches");
		String blockheaders[] = {"Block ID","Code Range(Jimple)","Type","Method(IF)","Class","Parent BlockID","Original(Y/N)","Feature ID"};
		DefaultTableModel blocktableModel = new DefaultTableModel(null,blockheaders);
		blocktable = new JTable(blocktableModel);
		blocktable.getTableHeader().setReorderingAllowed(false);
		blocksPane.setViewportView(blocktable);
		
		JScrollPane reusePane = new JScrollPane();
		tabbedPane.addTab("Reuse Assets", new ImageIcon(MainFrame.class.getResource("/image/common.png")), reusePane, "Code assets reuse by two or more features");
		
		String reuseheaders[] = {"Block ID","Color","LOC","Feature IDs","Element Type"};
		DefaultTableModel coommonassettableModel = new DefaultTableModel(null,reuseheaders);
		reusetable = new JTable(coommonassettableModel);
		reusetable.getTableHeader().setReorderingAllowed(false);
		reusetable.setDefaultRenderer(Object.class, new TableCellRender());
		reusePane.setViewportView(reusetable);
		
		JScrollPane variantBlockPane = new JScrollPane();
		tabbedPane.addTab("Variant Blocks", new ImageIcon(MainFrame.class.getResource("/image/variants.png")), variantBlockPane, "variant assets from variabillity aspect");
		String variantsheaders[] = {"Variant ID","Block_Id","Code Range(jimple)","Separators","SootMethod","SootClass"};
		DefaultTableModel varitableModel = new DefaultTableModel(null,variantsheaders);
		variantstable = new JTable(varitableModel);
		variantstable.getTableHeader().setReorderingAllowed(false);
		variantBlockPane.setViewportView(variantstable);
		
		// 将Variant Path加入到这个列表中 并且判断各个Variant之间的执行 顺序
		JScrollPane VariantPathPane = new JScrollPane();
		tabbedPane.addTab("Variant Path", new ImageIcon(MainFrame.class.getResource("/image/path.png")), VariantPathPane, null);
		String variantPathHeader[] = {"Path ID","Path(in Id)"};
		DefaultTableModel variantPathModel = new DefaultTableModel(null,variantPathHeader);
		variantPathtable = new JTable(variantPathModel);
		variantPathtable.getTableHeader().setReorderingAllowed(false);
		VariantPathPane.setViewportView(variantPathtable);
		
		JScrollPane statisticsPane = new JScrollPane();
		
		tabbedPane.addTab("Statistics", new ImageIcon(MainFrame.class.getResource("/image/data-analytics1.png")), statisticsPane, null);
		String statheaders[] = {"Feature","Color","LOC","Reuse LOC","Dependenency(LOC)"};
		DefaultTableModel statisitictableModel = new DefaultTableModel(null,statheaders);
		statistictable = new JTable(statisitictableModel);
		statistictable.getTableHeader().setReorderingAllowed(false);
		statisticsPane.setViewportView(statistictable);
		
		JScrollPane LegendPane = new JScrollPane();
		
		analysissplitPane.setLeftComponent(LegendPane);
		
		String legendheaders[] = {"Feature/Variant","Color"};
		DefaultTableModel model = new DefaultTableModel(null, legendheaders){

			@Override
			public boolean isCellEditable(int row, int column) {
				// TODO Auto-generated method stub
				return false;
			}
			
		};
		
		legendtable = new JTable(model);
		legendtable.setDefaultRenderer(Object.class, new TableCellRender());
		
		LegendPane.setViewportView(legendtable);
		setVisible(true);

	}
	
	public void setTargets(List<File>targets){
		this.target = targets;
	}
	
	public void setSupporingJars(List<File>support){
		this.supporingjars = support;
	}
	
	public void setSourceCode(List<File>source){
		this.sources = source;
	}
	/**
	 * 
	 * -cp "/Users/tangchris/Desktop/bin/:
	 * /Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/jre/lib/rt.jar:
	 * /Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/jre/lib/jce.jar:
	 * /Users/tangchris/hadoop_jars/mapreduce/*:/Users/tangchris/hadoop_jars/hdfs/*:
	 * /Users/tangchris/hadoop_jars/yarn/*:/Users/tangchris/hadoop_jars/common/*:/Users/tangchris/otherjars/*"  
	 * -hadoop -process-dir "/Users/tangchris/Desktop/bin/"  -entry:WordCount2
	 * 
	 */
	public void generateSootCommand(){
		List<String>sootCommand = new ArrayList<String>();
		// 1. cp
		sootCommand.add("-cp");
		
		// 2. all classes
		String cproot = "";
		
		for(File sub:target){
			if(sub.isDirectory()){
				cproot+=sub.getAbsolutePath();
				cproot+="/*";
			}else{
				cproot+=sub.getAbsolutePath();
				cproot+=":";
			}
		}
		
		// 3. add supporing jars
		for(File sub:supporingjars){
			if(sub.isDirectory()){
				cproot+=sub.getAbsolutePath();
				cproot+="/*";
			}else{
				cproot+=sub.getAbsolutePath();
				cproot+=":";
			}
		}
		if(cproot.endsWith(":"))
			cproot = cproot.substring(0, cproot.length()-1);
		
		sootCommand.add(cproot);
		
		// 4. -hadoop 
		sootCommand.add("-hadoop");
		
		// 5. add directories
		sootCommand.add("-process-dir");
		
		for(File sub:target){
			if(sub.isDirectory()){
				cproot+=sub.getAbsolutePath();
				cproot+="/*";
			}else{
				cproot+=sub.getAbsolutePath();
			}
			cproot+=":";
			
		}
		comm = sootCommand.toArray(new String[sootCommand.size()]);
		
		
	}
	public void runCommand(boolean bindingsource){
		vreAnalyzerCommandLine.inst(MainFrame.inst().getCommand(), bindingsource,startFromSource);		
	}
	public void generateSootCommand(String command){
		command = command.trim();
		String commandtemp = "";
		boolean getNextUnEmptyFlag = false;
		comm = StringUtils.split(command);
		for(int index = 0;index < comm.length;index++){
			if(comm[index].startsWith("\"")&&
					comm[index].endsWith("\"")){
				comm[index] = comm[index].substring(1, comm[index].length()-1);
			}
			commandtemp = comm[index];
			if(commandtemp.equals("-src-prec")){
				getNextUnEmptyFlag = true;
				continue;
			}
			if(getNextUnEmptyFlag==true){
				if(!commandtemp.trim().equals("")){
					getNextUnEmptyFlag = false;
					if(commandtemp.equals("java")){
						NewProjectPanel.sourceSet = true;
						NewProjectPanel.startFromSource=true;
						startFromSource = true;
						if(NewProjectPanel.targetSet==true){
							NewProjectPanel.btnAnalyze.setEnabled(true);
						}
					}
				}
			}
		}
		
	}
	public void createVariantHTML(){
		VariantHTMLFiles variantHTMLFiles = new VariantHTMLFiles(sources);
		DefaultMutableTreeNode variantlist = variantHTMLFiles.getRootTreeNode();
		root.add(variantlist);
	}

	public void loadSourceCodeandHTML() {
		// TODO Auto-generated method stub
		Stack<File> sourcefiles = new Stack<File>();
		sourcefiles.addAll(sources);
		root = new DefaultMutableTreeNode("Root");
		DefaultMutableTreeNode filelist = new DefaultMutableTreeNode("source_list");
		DefaultMutableTreeNode htmllist = new DefaultMutableTreeNode("annotated_list");
		while (!sourcefiles.isEmpty()) {
			File subfile = sourcefiles.pop();

			if (subfile.isDirectory()) {
				File[] childdir = subfile.listFiles();
				for (File child : childdir) {
					sourcefiles.push(child);
				}
			} else {
				// only add .java file
				if (subfile.exists() &&
						subfile.getPath().endsWith(".java")) {
					String htmlfileName = subfile.getPath().substring(0, subfile.getPath().length() - ".java".length());
					htmlfileName += ".html";
					File htmlfile = new File(htmlfileName);
					Text2HTML t2html = new Text2HTML(subfile, htmlfile);
					htmlToJava.putAll(t2html.getHTMLMapping());
					DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(subfile);
					DefaultMutableTreeNode htmlNode = new DefaultMutableTreeNode(htmlfile);
					allsourcefiles.add(subfile);
					filelist.add(fileNode);
					htmllist.add(htmlNode);
				}
			}
		}


		root.add(filelist);
		root.add(htmllist);

	}
	public void finishDirLoad(){
		DefaultTreeModel treeModel = (DefaultTreeModel) source_annotateDirTree.getModel();
		treeModel.setRoot(root);
		treeModel.reload();
	}
	public boolean isVariantReady(){
		return VariantAnnotate.variantready;
	}
	public boolean isFeatureReady(){
		return ProjectParser.isfeatureAnnotatedReady;
	}
	public void addDirTreeListener(){
		source_annotateDirTree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				DefaultMutableTreeNode selected = (DefaultMutableTreeNode)source_annotateDirTree.getLastSelectedPathComponent();
				if(selected!=null){
					if(!selected.isRoot()){
						if(selected.getParent().toString().equals("annotated_list")){
							if(isFeatureReady()){
								legendtable.setModel(ColorMap.inst().getTableModel());
								// 删除现有的listener 加入feature listener
								ColorMap.inst().addLegendListener();
								
							}
						}else if(selected.getParent().toString().equals("variantanno_list")){
							if(isVariantReady()){
								legendtable.setModel(VariantColorMap.inst().getTableModel());
								// 删除现有的listener 加入variant legacy listener
								ColorMap.inst().removeLegendListener();
								
							}
						}
					}
				}
				if(selected==null){}
				else if(isLeaf(selected.getUserObject())){
					File selectedfile = (File)selected.getUserObject();
					if(selectedfile.getAbsolutePath().endsWith(".java")){
						source_annotatedDisplayArea.setContentType("text/plain");
					}else if(selectedfile.getAbsolutePath().endsWith(".html")){
						source_annotatedDisplayArea.setContentType("text/html");
						source_annotatedDisplayArea.setEditorKit(new TooltipEditorKit());
						ToolTipManager.sharedInstance().registerComponent(source_annotatedDisplayArea);
						source_annotatedDisplayArea.addHyperlinkListener(new HyperlinkListener(){
						String tooltip;
							@Override
							public void hyperlinkUpdate(HyperlinkEvent e) {
								// TODO Auto-generated method stub
								JEditorPane editor = (JEditorPane) e.getSource();
						        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						          
						        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
						          tooltip = editor.getToolTipText();
						          javax.swing.text.Element elem = e.getSourceElement();
						          if (elem != null) {
						            AttributeSet attr = elem.getAttributes();
						            AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
						            if (a != null) {
						              editor.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
						            }
						          }
						        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
						          editor.setToolTipText(tooltip);
						        }
							}
							
						});
					}
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
							// TODO Auto-generated catch block
							e1.printStackTrace();
					}
				}	
			}
		});
	}
	
	public void bindSource(String sourcePattern,String classPattern){
		
		/** 
		 *  1. First, clean the allsourcefiles and all target files to ensure
		 *  (1) in target: it can only contains .jar and .class file, no directories are included;
		 *  (2) in allsourcefile: it can only contain .java file;(is already cleaned in {@link loadSourceCodeandHTML()})
		 */
		List<File>purtaget = getPureClassandJar(target);
		
		
		@SuppressWarnings("unused")
		SourceClassBinding bindinginstance = SourceClassBinding.inst(purtaget,allsourcefiles,classPattern,sourcePattern);
	}
	
	// get all classes and jar from given filelist, which could be a directory 
	public List<File> getPureClassandJar(List<File>rawtarget){
		List<File>pureclassJar = new LinkedList<File>();
		Stack<File>classjarfilesStack = new Stack<File>();
		classjarfilesStack.addAll(rawtarget);
		while(!classjarfilesStack.isEmpty()){
			File classFile = classjarfilesStack.pop();
			if(classFile.isDirectory()){
				File[]subclassFiles = classFile.listFiles();
				for(File fi:subclassFiles){
					classjarfilesStack.push(fi);
				}
			}else if(classFile.getAbsolutePath().endsWith(".class")||
					classFile.getAbsolutePath().endsWith(".jar")){
				pureclassJar.add(classFile);
			}
		}
		return pureclassJar;
	}
	public boolean isLeaf(Object node) {
	    return (node instanceof File);
	}
	
	public String[] getCommand() {
		// TODO Auto-generated method stub
		return comm;
	}
	public Map<String,String> getHTMLToJava(){
		return htmlToJava;
	}
	public void writeConsole(final String str){
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				textArealinecount++;
				if(textArealinecount>=50){
					textArealinecount=0;
					consoletextArea.setText("");
				}
				consoletextArea.append(str);
				consoletextArea.setCaretPosition(consoletextArea.getDocument().getLength());
			}
		});
	}
	public void addBinding(String className,File source){
		if(classnametoSource==null)
			classnametoSource = new HashMap<String,File>();
		classnametoSource.put(className, source);
	}
	public static JEditorPane getSrcTextPane(){
		return source_annotatedDisplayArea;
	}
	public JTable getJobColorMapTable(){
		return legendtable;
	}
	public JTable getStatisticTable(){
		return statistictable;
	}
	public JTable getCommonAssetTable(){
		return reusetable;
	}
	public JTree getTree(){
		return source_annotateDirTree;
	}
	public JTable getVariantTable(){
		return variantstable;
	}
	public JTable getBlockTable() {
		// TODO Auto-generated method stub
		return blocktable;
	}
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
