package vreAnalyzer.UI;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.FlowLayout;

import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.commons.lang3.StringUtils;

import vreAnalyzer.vreAnalyzerCommandLine;
import vreAnalyzer.Text2HTML.Text2HTML;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.JTable;

public class MainFrame extends JFrame {
	// 1. Instance
	private static MainFrame instance;
	
	// 2. Contents
	private JPanel contentPane;
	private final JTextField textField;
	private List<File>target;
	private List<File>supporingjars;
	private List<File>sources;
	private final JSplitPane sourcesplitPane;
	private static JTextPane txtrSource=null;
	private static JTextPane source_annotatedDisplayArea;
	private final JTextArea consoletextArea;
	private final JTree source_annotateDirTree;
	private static int textArealinecount = 0;
	
	// 3. 
	private String[]comm;
	private static Map<String,File>classnametoSource;
	private List<File>allsourcefiles = new LinkedList<File>();
	private boolean startFromSource = false;
	
	// 4. Output redirect
	PrintStream printStream;
	private final JTable legendtable;
	private static Map<String,String>htmlToJava;
	private final JTable statistictable;
	private JTable comassettable;
	
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
		
		source_annotatedDisplayArea = new JTextPane();
		source_annotateDisplayPane.setViewportView(source_annotatedDisplayArea);
		
		JPanel searchPanel = new JPanel();
		FlowLayout fl_searchPanel = (FlowLayout) searchPanel.getLayout();
		fl_searchPanel.setAlignment(FlowLayout.LEFT);
		sourcepanel.add(searchPanel, BorderLayout.SOUTH);
		
		textField = new JTextField();
		searchPanel.add(textField);
		textField.setColumns(40);
		
		JButton btnSearch = new JButton("Search");
		searchPanel.add(btnSearch);
		
		JButton btnForwardSearch = new JButton("Forward Search");
		searchPanel.add(btnForwardSearch);
		
		JButton btnBackwardSearch = new JButton("Backward Search");
		searchPanel.add(btnBackwardSearch);
		
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
		
		JScrollPane comassetPane = new JScrollPane();
		tabbedPane.addTab("Common Assets", new ImageIcon(MainFrame.class.getResource("/image/common.png")), comassetPane, null);
		
		String commonassetheaders[] = {"CommonAsset ID","Color","LOC","Shared Feature IDs","Element Type"};
		DefaultTableModel coommonassettableModel = new DefaultTableModel(null,commonassetheaders);
		comassettable = new JTable(coommonassettableModel);
		comassettable.getTableHeader().setReorderingAllowed(false);
		comassetPane.setViewportView(comassettable);
		
		JScrollPane statisticsPane = new JScrollPane();
		
		tabbedPane.addTab("Statistics", new ImageIcon(MainFrame.class.getResource("/image/data-analytics1.png")), statisticsPane, null);
		String statheaders[] = {"Feature","Color","LOC","Reuse LOC","Dependenency(LOC)"};
		DefaultTableModel statisitictableModel = new DefaultTableModel(null,statheaders);
		statistictable = new JTable(statisitictableModel);
		statistictable.getTableHeader().setReorderingAllowed(false);
		statisticsPane.setViewportView(statistictable);
		
		JScrollPane LegendPane = new JScrollPane();
		
		analysissplitPane.setLeftComponent(LegendPane);
		
		String legendheaders[] = {"Feature","Color"};
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
	
	public void loadSourceCodeandHTML() {
		// TODO Auto-generated method stub
		Stack<File>sourcefiles = new Stack<File>();
		sourcefiles.addAll(sources);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
		DefaultMutableTreeNode filelist = new DefaultMutableTreeNode("source_list");
		DefaultMutableTreeNode htmllist = new DefaultMutableTreeNode("annotated_list");
		while(!sourcefiles.isEmpty()){
			File subfile = sourcefiles.pop();
			
			if(subfile.isDirectory()){
				File[]childdir = subfile.listFiles();
				for(File child:childdir){
					sourcefiles.push(child);
				}
			}else{
				// only add .java file
				if(subfile.exists()&&
						subfile.getPath().endsWith(".java")){
					String htmlfileName = subfile.getPath().substring(0, subfile.getPath().length()-".java".length());
					htmlfileName+=".html";
					File htmlfile = new File(htmlfileName);
					Text2HTML t2html = new Text2HTML(subfile,htmlfile);
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
		DefaultTreeModel treeModel = (DefaultTreeModel)source_annotateDirTree.getModel();
		treeModel.setRoot(root);
		treeModel.reload();
		
		source_annotateDirTree.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				DefaultMutableTreeNode selected = (DefaultMutableTreeNode)source_annotateDirTree.getLastSelectedPathComponent();
				if(selected==null){
					
				}
				else if(isLeaf(selected.getUserObject())){
					File selectedfile = (File)selected.getUserObject();
					if(selectedfile.getAbsolutePath().endsWith(".java")){
						txtrSource.setContentType("text/plain");
					}else if(selectedfile.getAbsolutePath().endsWith(".html")){
						txtrSource.setContentType("text/html");
						txtrSource.setEditorKit(new HTMLEditorKit());
						ToolTipManager.sharedInstance().registerComponent(txtrSource);
						txtrSource.addHyperlinkListener(new HyperlinkListener(){
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
							
							txtrSource.setText("");
							txtrSource.setText(allString);
							txtrSource.setCaretPosition(0);
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
	public static JTextPane getSrcTextPane(){
		return source_annotatedDisplayArea;
	}
	public JTable getJobColorMapTable(){
		return legendtable;
	}
	public JTable getStatisticTable(){
		return statistictable;
	}
	public JTree getTree(){
		return source_annotateDirTree;
	}
}
