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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MainFrame extends JFrame {
	
	private static MainFrame instance;
	private JPanel contentPane;
	private JTextField textField;
	private Set<File>target;
	private Set<File>supporingjars;
	private Set<File>sources;
	
	
	
	/**
	 * Launch the application.
	 */


	
	public static void main(String[] args) {
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
		
		JMenuItem mntmNew = new JMenuItem("New...");
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewProjectPanel npp = new NewProjectPanel(instance);
				npp.setLocationRelativeTo(instance);
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmConfiguration = new JMenuItem("Input Configure");
		mntmConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				InputConfigure.inst(instance);
			}
		});
		mnFile.add(mntmConfiguration);
		
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
				About.inst(instance);
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
		
		JSplitPane upsplitPane = new JSplitPane();
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
		
		JTextArea txtrSource = new JTextArea();
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
		
		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		tabbedPane.addTab("Statistics", null, scrollPane_2, null);
		setVisible(true);
	}
	
	public void setTargets(Set<File>targets){
		this.target = targets;
	}
	
	public void setSupporingJars(Set<File>support){
		this.supporingjars = support;
	}
	public void setSourceCode(Set<File>source){
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
		Set<String>sootCommand = new HashSet<String>();
		// 1. cp
		sootCommand.add("-cp");
		
		// 2. all classes
		String cproot = "";
		for(File sub:supporingjars){
			cproot+=sub.getAbsolutePath();
			cproot+=":";
		}
		cproot = cproot.substring(0, cproot.length()-1);
		sootCommand.add(cproot);
		
		// 3. all 
		
	}
	
}
