package vreAnalyzer.UI;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.awt.FlowLayout;

import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MainFrame extends JFrame {
	// 1. Instance
	private static MainFrame instance;
	
	// 2. Contents
	private JPanel contentPane;
	private final JTextField textField;
	private List<File>target;
	private List<File>supporingjars;
	private List<File>sources;
	private final JSplitPane upsplitPane;
	private static JTextArea txtrSource=null;
	private final JTextArea textArea;
	
	// 3. 
	private String[]comm;
	private static Map<String,File>classnametoSource;
	private List<File>allsourcefiles = new LinkedList<File>();
	// 4. Output redirect
	PrintStream printStream;
	
	public static void main(String[] args) {
		classnametoSource = new HashMap<String,File>();
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
		mainsplitPane.setResizeWeight(0.6);
		mainsplitPane.setOneTouchExpandable(true);
		mainsplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(mainsplitPane);
		
		upsplitPane = new JSplitPane();
		upsplitPane.setOneTouchExpandable(true);
		mainsplitPane.setLeftComponent(upsplitPane);
		
		JTree tree = new JTree();
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Source directory") {
			{
				}
			}
		));
		upsplitPane.setLeftComponent(tree);
		
		JPanel panel = new JPanel();
		upsplitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		panel.add(scrollPane_1, BorderLayout.CENTER);
		
		txtrSource = new JTextArea();
		scrollPane_1.setViewportView(txtrSource);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel.add(panel_1, BorderLayout.SOUTH);
		
		textField = new JTextField();
		panel_1.add(textField);
		textField.setColumns(40);
		
		JButton btnSearch = new JButton("Search");
		panel_1.add(btnSearch);
		
		JButton btnForwardSearch = new JButton("Forward Search");
		panel_1.add(btnForwardSearch);
		
		JButton btnBackwardSearch = new JButton("Backward Search");
		panel_1.add(btnBackwardSearch);
		
		JSplitPane bottomsplitPane = new JSplitPane();
		bottomsplitPane.setOneTouchExpandable(true);
		mainsplitPane.setRightComponent(bottomsplitPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		bottomsplitPane.setRightComponent(tabbedPane);
		
		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("Console", new ImageIcon(MainFrame.class.getResource("/image/console.png")), scrollPane, null);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		
		// Redirect output stream
		printStream = new PrintStream(new CustomOutputStream(textArea));
		System.setOut(printStream);
		System.setErr(printStream);
        
		scrollPane.setViewportView(textArea);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		
		tabbedPane.addTab("Statistics", new ImageIcon(MainFrame.class.getResource("/image/statistics.png")), scrollPane_2, null);
		
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
	public void generateSootCommand(String command){
		command = command.trim();
		comm = StringUtils.split(command);
		for(int index = 0;index < comm.length;index++){
			if(comm[index].startsWith("\"")&&
					comm[index].endsWith("\"")){
				comm[index] = comm[index].substring(1, comm[index].length()-1);
			}
		}
		
	}
	public void loadSourceCode() {
		// TODO Auto-generated method stub
		Stack<File>sourcefiles = new Stack<File>();
		sourcefiles.addAll(sources);
		DefaultMutableTreeNode filelist = new DefaultMutableTreeNode("source_list");
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
					DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(subfile);
					allsourcefiles.add(subfile);
					filelist.add(fileNode);
				}
			}
		}
		
		final JTree filefolder = new JTree(filelist);
		
		filefolder.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				DefaultMutableTreeNode selected = (DefaultMutableTreeNode)filefolder.getLastSelectedPathComponent();
				File selectedfile = (File)selected.getUserObject();
				List<String> content;
				try {
					content = Files.readAllLines(selectedfile.toPath(),Charset.defaultCharset());
					String allString = "";
					for(String subcontent:content){
						allString+=subcontent;
						allString+="\n";
					}
					txtrSource.setText(allString);
					txtrSource.setCaretPosition(0);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		
		upsplitPane.setLeftComponent(filefolder);

	}
	
	public void bindSource(){
		File nestedTarget = scanNested(target);
		File nestedSource = scanNested(allsourcefiles);
		SourceClassBinding bindinginstance = SourceClassBinding.inst(target,allsourcefiles,nestedTarget,nestedSource.getParentFile());
	}

	public File scanNested(Collection<File>files){
		int min = 1000;
		File result = null;
		for(File fi:files){
			if(StringUtils.countMatches(fi.getAbsolutePath(), "/")< min){
				result = fi;
				min = StringUtils.countMatches(fi.getAbsolutePath(), "/");
			}
		}
		return result;
	}
	public String[] getCommand() {
		// TODO Auto-generated method stub
		return comm;
	}
	public void writeConsole(final String str){
		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				textArea.append(str);
				textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		});
	}
	public void addBinding(String className,File source){
		if(classnametoSource==null)
			classnametoSource = new HashMap<String,File>();
		classnametoSource.put(className, source);
	}
	public static JTextArea getSrcTextArea(){
		return txtrSource;
	}
}
