package vreAnalyzer.UI;
import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JProgressBar;


public class SourceClassBinding extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final JProgressBar progressBar;
	private final JLabel lblBinding;
	private static SourceClassBinding instance;
	/**
	 * Create the dialog.
	 */
	public static SourceClassBinding inst(JFrame parent){
		if(instance==null){
			instance = new SourceClassBinding(parent);
		}
		return instance;
	}
	public static SourceClassBinding inst(){
		return instance;
	}
	public SourceClassBinding(JFrame parent) {
		super(parent,true);
		setTitle("Class and souce binding...");
		setBounds(100, 100, 450, 180);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		SpringLayout sl_contentPanel = new SpringLayout();
		contentPanel.setLayout(sl_contentPanel);
		
		lblBinding = new JLabel("Binding:");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblBinding, 34, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, lblBinding, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, lblBinding, 57, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, lblBinding, 430, SpringLayout.WEST, contentPanel);
		contentPanel.add(lblBinding);
		
		progressBar = new JProgressBar();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, progressBar, 27, SpringLayout.SOUTH, lblBinding);
		sl_contentPanel.putConstraint(SpringLayout.WEST, progressBar, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, progressBar, 50, SpringLayout.SOUTH, lblBinding);
		sl_contentPanel.putConstraint(SpringLayout.EAST, progressBar, 430, SpringLayout.WEST, contentPanel);
		contentPanel.add(progressBar);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(false);
	}
	public void startdirNameBinding(List<String>classes,ArrayList<File>source,String clsSysPathPattern,String sourceSysPathPattern){
		setVisible(true);
		int index = 0;
		int totalsize = classes.size();
		for(String clsName:classes){
			String realName = clsName.substring(0+clsSysPathPattern.length(), clsName.length()-".class".length());
			File result = source.get(bindarySearch(source,sourceSysPathPattern,realName,0,source.size()-1));
			MainFrame.inst().addBinding(clsName, result);
			index++;
			lblBinding.setText("Binding: class "+realName);
			float value = (float)index/totalsize*100;
			progressBar.setValue((int)value);
		}
	}
	private int bindarySearch(ArrayList<File> source,String sourceSystemPath,String value,int min, int max) {
		// TODO Auto-generated method stub
		if(min > max){
			return -1;
		}
		int	mid = (max+min)/2;
		
		String path = source.get(mid).getAbsolutePath();
		String subpath = path.substring(sourceSystemPath.length(), path.length()-".java".length());
		if(subpath.equals(value)){
			return mid;
		}else if(subpath.compareTo(value)>0){
			return bindarySearch(source,sourceSystemPath,value,min,mid-1);
		}else{
			return bindarySearch(source,sourceSystemPath,value,mid+1,max);
		}
		
	}
	public void startdirBinding(List<File>classes,List<File>source,File clsParent,File sourceParent){
		ArrayList<File>sortedSource = new ArrayList<File>(source);
		Collections.sort(sortedSource,new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				// TODO Auto-generated method stub
				return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			}
			
			
		});
		
		List<String>classNames = new LinkedList<String>();
		Stack<File>classstack = new Stack<File>();
		classstack.addAll(classes);
		while(!classstack.isEmpty()){
			File fi = classstack.pop();
			if(!fi.isDirectory()){
					if(fi.getName().endsWith(".class")){
						String className = fi.getAbsolutePath().replace('/', '.');
						classNames.add(className);
					}else if(fi.getName().endsWith(".jar")){
						ZipInputStream zip;
						try {
							zip = new ZipInputStream(new FileInputStream(fi));
							for(ZipEntry entry=zip.getNextEntry();entry!=null;entry = zip.getNextEntry()){
								if(!entry.isDirectory() &&
										entry.getName().endsWith(".class")){
									String className = entry.getName().replace('/', '.');
									classNames.add(className);
								}
							}
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
			}else{
				File[]fileList = fi.listFiles();
				for(File subfile:fileList){
					classstack.push(subfile);
				}
			}
			
		}
		String parentPath = clsParent.getName().replace('/', '.');
		String parentSourcePath = sourceParent.getAbsolutePath().replace('/', '.');
		startdirNameBinding(classNames,sortedSource,parentPath,parentSourcePath);
	}
	
	
}
