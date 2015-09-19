package vreAnalyzer.UI;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JSeparator;

public class NewProjectPanel extends JDialog {

	private JPanel contentPane;
	private JTextField textField;
	private JLabel lblMainJarclassDirectory;
	private JScrollPane scrollPane;
	private JButton btnAdd;
	private JButton btnRemove;
	private JLabel lblSupportingJarsclasses;
	private JScrollPane scrollPane_3;
	private JButton button;
	private JButton button_1;
	private JLabel lblSourceCodeDirectory;
	private JScrollPane scrollPane_4;
	private JButton button_2;
	private JButton button_3;
	private JButton btnNewButton;
	protected static JButton btnAnalyze;
	private boolean runFromConfigWizard = false;
	private boolean bindingsource = false;
	protected static boolean targetSet = false;
	protected static boolean sourceSet = false;
	private static NewProjectPanel instance;
	private static List<String>sootCommandsFromWizard = new LinkedList<String>();
	protected static boolean startFromSource = false;
	private JTextField textField_1;
	private JLabel label;
	private JTextField textField_3;
	public static NewProjectPanel inst(JFrame parent){
		if(instance==null){
			instance = new NewProjectPanel(parent);
		}
		instance.setVisible(true);
		return instance;
	}
	public static NewProjectPanel inst(){
		return instance;
	}
	
	/**
	 * Create the frame.
	 */
	public NewProjectPanel(JFrame parent) {
		
		super(parent,true);
		setTitle("New Project");
		
		setBounds(100, 100, 600, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JLabel lblProjectName = new JLabel("Project name");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblProjectName, 3, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblProjectName, 3, SpringLayout.WEST, contentPane);
		
		contentPane.add(lblProjectName);
		
		textField = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, textField, 5, SpringLayout.SOUTH, lblProjectName);
		sl_contentPane.putConstraint(SpringLayout.WEST, textField, 0, SpringLayout.WEST, lblProjectName);
		sl_contentPane.putConstraint(SpringLayout.EAST, textField, -10, SpringLayout.EAST, contentPane);
		contentPane.add(textField);
		
		lblMainJarclassDirectory = new JLabel("Main jar/class directory to analyze");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblMainJarclassDirectory, 6, SpringLayout.SOUTH, textField);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblMainJarclassDirectory, 0, SpringLayout.WEST, lblProjectName);
		contentPane.add(lblMainJarclassDirectory);
		
		
		JPanel panel = new JPanel(new BorderLayout());
		final JList<File> jarLists = new JList();
		jarLists.setModel(new DefaultListModel());
		panel.add(jarLists);
		
		scrollPane = new JScrollPane(panel);
		
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 3, SpringLayout.SOUTH, lblMainJarclassDirectory);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 3, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, 80, SpringLayout.SOUTH, lblMainJarclassDirectory);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -100, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
		btnAdd = new JButton("Add");
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAdd, -75, SpringLayout.EAST, textField);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnAdd, 0, SpringLayout.EAST, textField);
		btnAdd.addActionListener(new ActionListener() {
			// target to be analyzed
			public void actionPerformed(ActionEvent e) {
				// Add jar or directories to analysis
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				FileFilter type = new ExtensionFilter("Java class/jar:.jar or .class",new String[]{".jar",".class"});
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.addChoosableFileFilter(type);
				int returnValue = fc.showOpenDialog(null);
				if(returnValue==JFileChooser.APPROVE_OPTION){
					File[] selectedFiles = fc.getSelectedFiles();
					
					for(File f:selectedFiles){
						((DefaultListModel)jarLists.getModel()).addElement(f);
					}
					targetSet = true;
					if(sourceSet == true){
						btnAnalyze.setEnabled(true);
					}
				}
			}
		});
		contentPane.add(btnAdd);
		
		btnRemove = new JButton("Remove");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnAdd, -14, SpringLayout.NORTH, btnRemove);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRemove, 6, SpringLayout.EAST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnRemove, 0, SpringLayout.SOUTH, scrollPane);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<File> dlm = (DefaultListModel<File>) jarLists.getModel();
				if(jarLists.getSelectedIndices().length>0){
					int[]selectedIndices = jarLists.getSelectedIndices();
					for (int i = selectedIndices.length-1; i >=0; i--) {
			              dlm.removeElementAt(selectedIndices[i]);
			          } 		
				}
				
			}
		});
		contentPane.add(btnRemove);
		
		lblSupportingJarsclasses = new JLabel("Depending jars/classes");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSupportingJarsclasses, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSupportingJarsclasses, 0, SpringLayout.WEST, lblProjectName);
		contentPane.add(lblSupportingJarsclasses);
		
		final JList<File> dependingjarLists = new JList();
		dependingjarLists.setModel(new DefaultListModel());
		
		JPanel panel3 = new JPanel(new BorderLayout());
		panel3.add(dependingjarLists);
		scrollPane_3 = new JScrollPane(panel3);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_3, 6, SpringLayout.SOUTH, lblSupportingJarsclasses);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_3, 3, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_3, 83, SpringLayout.SOUTH, lblSupportingJarsclasses);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_3, -100, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_3);
		
		button = new JButton("Add");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				FileFilter type = new ExtensionFilter("Java class/jar:.jar or .class",new String[]{".jar",".class"});
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setFileFilter(type);
				int returnValue = fc.showOpenDialog(null);
				if(returnValue==JFileChooser.APPROVE_OPTION){
					File[] selectedFiles = fc.getSelectedFiles();
					for(File f:selectedFiles){
						((DefaultListModel)dependingjarLists.getModel()).addElement(f);
					}
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, button, 29, SpringLayout.SOUTH, btnRemove);
		sl_contentPane.putConstraint(SpringLayout.WEST, button, 0, SpringLayout.WEST, btnAdd);
		contentPane.add(button);
		
		button_1 = new JButton("Remove");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<File> dlm = (DefaultListModel<File>) dependingjarLists.getModel();
				if(dependingjarLists.getSelectedIndices().length>0){
					int[]selectedIndices = dependingjarLists.getSelectedIndices();
					for (int i = selectedIndices.length-1; i >=0; i--) {
			              dlm.removeElementAt(selectedIndices[i]);
			          } 		
				}
			}
		});
		sl_contentPane.putConstraint(SpringLayout.WEST, button_1, 0, SpringLayout.WEST, btnRemove);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, button_1, 0, SpringLayout.SOUTH, scrollPane_3);
		contentPane.add(button_1);
		
		lblSourceCodeDirectory = new JLabel("Source code directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSourceCodeDirectory, 6, SpringLayout.SOUTH, scrollPane_3);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSourceCodeDirectory, 0, SpringLayout.WEST, lblProjectName);
		contentPane.add(lblSourceCodeDirectory);
		
		JPanel panel4 = new JPanel(new BorderLayout());
		final JList<File> javasource = new JList();
		javasource.setModel(new DefaultListModel());
		panel4.add(javasource);
		scrollPane_4 = new JScrollPane(panel4);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_4, 6, SpringLayout.SOUTH, lblSourceCodeDirectory);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_4, 3, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_4, 83, SpringLayout.SOUTH, lblSourceCodeDirectory);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_4, -100, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_4);
		
		button_2 = new JButton("Add");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				FileFilter type2 = new ExtensionFilter("Java source:.java",new String[]{".java"});
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.setFileFilter(type2);
				int returnValue = fc.showOpenDialog(null);
				if(returnValue==JFileChooser.APPROVE_OPTION){
					File[] selectedFiles = fc.getSelectedFiles();
					
					for(File f:selectedFiles){
						((DefaultListModel)javasource.getModel()).addElement(f);
					}
					sourceSet = true;
					if(targetSet == true){
						btnAnalyze.setEnabled(true);
					}
				}
				
				
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, button_2, 0, SpringLayout.NORTH, scrollPane_4);
		sl_contentPane.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, btnAdd);
		contentPane.add(button_2);
		
		button_3 = new JButton("Remove");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<File> dlm = (DefaultListModel<File>) javasource.getModel();
				if(javasource.getSelectedIndices().length>0){
					int[]selectedIndices = javasource.getSelectedIndices();
					for (int i = selectedIndices.length-1; i >=0; i--) {
				            dlm.removeElementAt(selectedIndices[i]);
				    } 		
				}
				
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, button_3, 6, SpringLayout.SOUTH, button_2);
		sl_contentPane.putConstraint(SpringLayout.EAST, button_3, 0, SpringLayout.EAST, btnRemove);
		contentPane.add(button_3);
		
		JSeparator separator = new JSeparator();
		sl_contentPane.putConstraint(SpringLayout.WEST, separator, 2, SpringLayout.WEST, lblProjectName);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator, 0, SpringLayout.EAST, btnRemove);
		contentPane.add(separator);
		
		btnNewButton = new JButton("Cancle");
		sl_contentPane.putConstraint(SpringLayout.NORTH, separator, -22, SpringLayout.NORTH, btnNewButton);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, separator, -6, SpringLayout.NORTH, btnNewButton);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnNewButton, -10, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnNewButton, 0, SpringLayout.EAST, scrollPane);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				instance=null;
			}
		});
		contentPane.add(btnNewButton);
		
		btnAnalyze = new JButton("Analyze");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAnalyze, 0, SpringLayout.NORTH, btnNewButton);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnAnalyze, 0, SpringLayout.WEST, btnRemove);
		btnAnalyze.setEnabled(false);
		btnAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 1. jar/directory to be process
				List<File>target = new LinkedList<File>();
				DefaultListModel targetList = (DefaultListModel) jarLists.getModel();
				if(!targetList.isEmpty()){
					for(int i = 0;i < targetList.size();i++){
						target.add((File) targetList.getElementAt(i));
					}
					MainFrame.inst().setTargets(target);
				}
				
				if(!runFromConfigWizard){

					// 2. cp supporting jars
					List<File>supporingjars = new LinkedList<File>();
					DefaultListModel supportList = (DefaultListModel) dependingjarLists.getModel();
					for(int i = 0;i < supportList.size();i++){
						supporingjars.add((File) supportList.getElementAt(i));
					}

					MainFrame.inst().setSupporingJars(supporingjars);				
					MainFrame.inst().generateSootCommand();
				}
				// 3. ensure the binding pattern is set
				if(textField_1.getText().trim().equals("") ||
						textField_3.getText().trim().equals("")){
					JOptionPane.showMessageDialog(null, "Please set the patterns for source and class binding","No pattern setting error",JOptionPane.WARNING_MESSAGE);
					// show error image for null setting for pattern
					return;
				}
				@SuppressWarnings("rawtypes")
				DefaultListModel sourceList = (DefaultListModel) javasource.getModel();				
				// 4. binding the class to java source code
				
				if(runFromConfigWizard && startFromSource){// in the command wizard the source is set
					
					// set the source from the configuration wizard
					String srcDir = getSourceDirfromWizardCommand();
					if(srcDir.startsWith("\"")){
						srcDir = srcDir.substring(1);
					}
					if(srcDir.endsWith("\"")){
						srcDir = srcDir.substring(0, srcDir.length()-1);
					}
					List<File>sources = new LinkedList<File>();
					sources.add(new File(srcDir));
					MainFrame.inst().setSourceCode(sources);
					MainFrame.inst().loadSourceCodeandHTML();
					dispose();
					MainFrame.inst().bindSource(textField_1.getText(),textField_3.getText());	
					bindingsource = true;
				}else if(sourceList.size()!=0){// the source is set mannually
					
					List<File>sources = new LinkedList<File>();
					for(int i = 0;i < sourceList.size();i++){
						sources.add((File) sourceList.getElementAt(i));
					}
					MainFrame.inst().setSourceCode(sources);
					MainFrame.inst().loadSourceCodeandHTML();
					dispose();
					MainFrame.inst().bindSource(textField_1.getText(),textField_3.getText());	
					bindingsource = true;
				}else{ 
					dispose();
					bindingsource = false;
				}
				run(bindingsource);
				
			}

			
		});
		
		contentPane.add(btnAnalyze);
		
		JButton btnConfigureWizard = new JButton("[Direct]SootCommand");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnConfigureWizard, 11, SpringLayout.SOUTH, scrollPane_4);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnConfigureWizard, 40, SpringLayout.SOUTH, scrollPane_4);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnConfigureWizard, 0, SpringLayout.EAST, btnRemove);
		btnConfigureWizard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConfigureWizard.inst(instance);
				runFromConfigWizard = true;
			}
		});
		contentPane.add(btnConfigureWizard);
		
		JLabel lblSourceFilePath = new JLabel("Regular pattern for source file path:");
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSourceFilePath, 0, SpringLayout.WEST, lblProjectName);
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSourceFilePath, 10, SpringLayout.SOUTH, btnConfigureWizard);
		contentPane.add(lblSourceFilePath);
		
		textField_1 = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.WEST, textField_1, 26, SpringLayout.EAST, lblSourceFilePath);
		sl_contentPane.putConstraint(SpringLayout.EAST, textField_1, -79, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.NORTH, textField_1, 10, SpringLayout.SOUTH, btnConfigureWizard);
		contentPane.add(textField_1);
		textField_1.setColumns(10);
		
		label = new JLabel("Regular pattern for class file path:");
		sl_contentPane.putConstraint(SpringLayout.WEST, label, 0, SpringLayout.WEST, lblProjectName);
		sl_contentPane.putConstraint(SpringLayout.NORTH, label,20, SpringLayout.SOUTH, lblSourceFilePath);
		contentPane.add(label);
		
		textField_3 = new JTextField();
		sl_contentPane.putConstraint(SpringLayout.NORTH, textField_3, 16, SpringLayout.SOUTH, textField_1);
		sl_contentPane.putConstraint(SpringLayout.EAST, textField_3, 0, SpringLayout.EAST, textField_1);
		sl_contentPane.putConstraint(SpringLayout.WEST, textField_3, 0, SpringLayout.WEST, textField_1);
		textField_3.setColumns(10);
		contentPane.add(textField_3);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);	
		
	}
	private String getSourceDirfromWizardCommand() {
		// TODO Auto-generated method stub
		boolean getNextUnEmptyFlag = false;
		for(int i = 0;i < sootCommandsFromWizard.size();i++){
			if(sootCommandsFromWizard.get(i).equals("-process-dir")){
				getNextUnEmptyFlag = true;
				continue;
			}
			if(getNextUnEmptyFlag==true){
				if(!sootCommandsFromWizard.get(i).trim().equals("")){
					getNextUnEmptyFlag = false;
					return sootCommandsFromWizard.get(i);
				}
			}
		}
		
		return null;
	}
	public void run(final boolean bindingsource){
		
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				MainFrame.inst().runCommand(bindingsource);
			}
           
            	
		});
		thread.start();
		
		
			
	}
	
	public class ExtensionFilter extends FileFilter {
	    private String extensions[];

	    private String description;

	    public ExtensionFilter(String description, String extension) {
	      this(description, new String[] { extension });
	    }

	    public ExtensionFilter(String description, String extensions[]) {
	      this.description = description;
	      this.extensions = (String[]) extensions.clone();
	    }

	    public boolean accept(File file) {
	      if (file.isDirectory()) {
	        return true;
	      }
	      int count = extensions.length;
	      String path = file.getAbsolutePath();
	      for (int i = 0; i < count; i++) {
		        String ext = extensions[i];
		        if (path.endsWith(ext)
		            && (path.charAt(path.length() - ext.length()) == '.')) {
		          return true;
		        }
	      }
	      return false;
	    }

	    public String getDescription() {
	      return (description == null ? extensions[0] : description);
	    }
	  }
	public static void setWizardCommand(String text) {
		// TODO Auto-generated method stub
		sootCommandsFromWizard = new LinkedList<String>();
		for(String sub:text.split(" ")){
			sootCommandsFromWizard.add(sub);
		}
	}
}
