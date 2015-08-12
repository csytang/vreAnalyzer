package vreAnalyzer.UI;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.WindowConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JSeparator;

public class NewProjectPanel extends JFrame {

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
	private JButton btnWizard;
	private JButton btnNewButton;
	private JButton btnAnalyze;

	
	/**
	 * Create the frame.
	 */
	public NewProjectPanel() {
		setTitle("New Project");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 500);
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
		JTextArea textarea = new JTextArea();
		panel.add(textarea);
		textarea.setEditable(false);
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
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		contentPane.add(btnAdd);
		
		btnRemove = new JButton("Remove");
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnAdd, -14, SpringLayout.NORTH, btnRemove);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnRemove, 6, SpringLayout.EAST, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnRemove, 0, SpringLayout.SOUTH, scrollPane);
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		contentPane.add(btnRemove);
		
		lblSupportingJarsclasses = new JLabel("Depending jars/classes");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSupportingJarsclasses, 6, SpringLayout.SOUTH, scrollPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSupportingJarsclasses, 0, SpringLayout.WEST, lblProjectName);
		contentPane.add(lblSupportingJarsclasses);
		
		
		JTextArea textarea2 = new JTextArea();
		textarea2.setEditable(false);
		JPanel panel3 = new JPanel(new BorderLayout());
		panel3.add(textarea2);
		scrollPane_3 = new JScrollPane(panel3);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_3, 6, SpringLayout.SOUTH, lblSupportingJarsclasses);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_3, 3, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_3, 83, SpringLayout.SOUTH, lblSupportingJarsclasses);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_3, -100, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_3);
		
		button = new JButton("Add");
		sl_contentPane.putConstraint(SpringLayout.NORTH, button, 29, SpringLayout.SOUTH, btnRemove);
		sl_contentPane.putConstraint(SpringLayout.WEST, button, 0, SpringLayout.WEST, btnAdd);
		contentPane.add(button);
		
		button_1 = new JButton("Remove");
		sl_contentPane.putConstraint(SpringLayout.WEST, button_1, 0, SpringLayout.WEST, btnRemove);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, button_1, 0, SpringLayout.SOUTH, scrollPane_3);
		contentPane.add(button_1);
		
		lblSourceCodeDirectory = new JLabel("Source code directory");
		sl_contentPane.putConstraint(SpringLayout.NORTH, lblSourceCodeDirectory, 6, SpringLayout.SOUTH, scrollPane_3);
		sl_contentPane.putConstraint(SpringLayout.WEST, lblSourceCodeDirectory, 0, SpringLayout.WEST, lblProjectName);
		contentPane.add(lblSourceCodeDirectory);
		
		JPanel panel4 = new JPanel(new BorderLayout());
		JTextArea textarea3 = new JTextArea();
		textarea3.setEditable(false);
		panel4.add(textarea3);
		scrollPane_4 = new JScrollPane(panel4);
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane_4, 6, SpringLayout.SOUTH, lblSourceCodeDirectory);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane_4, 3, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane_4, 83, SpringLayout.SOUTH, lblSourceCodeDirectory);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane_4, -100, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane_4);
		
		button_2 = new JButton("Add");
		sl_contentPane.putConstraint(SpringLayout.NORTH, button_2, 0, SpringLayout.NORTH, scrollPane_4);
		sl_contentPane.putConstraint(SpringLayout.WEST, button_2, 0, SpringLayout.WEST, btnAdd);
		contentPane.add(button_2);
		
		button_3 = new JButton("Remove");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		sl_contentPane.putConstraint(SpringLayout.NORTH, button_3, 6, SpringLayout.SOUTH, button_2);
		sl_contentPane.putConstraint(SpringLayout.EAST, button_3, 0, SpringLayout.EAST, btnRemove);
		contentPane.add(button_3);
		
		btnWizard = new JButton("Wizard");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnWizard, 6, SpringLayout.SOUTH, button_3);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnWizard, 0, SpringLayout.EAST, textField);
		btnWizard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		contentPane.add(btnWizard);
		
		JSeparator separator = new JSeparator();
		sl_contentPane.putConstraint(SpringLayout.NORTH, separator, 24, SpringLayout.SOUTH, btnWizard);
		sl_contentPane.putConstraint(SpringLayout.WEST, separator, 0, SpringLayout.WEST, lblProjectName);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, separator, -42, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, separator, -3, SpringLayout.EAST, contentPane);
		contentPane.add(separator);
		
		btnNewButton = new JButton("Cancle");
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnNewButton, 432, SpringLayout.NORTH, contentPane);
		contentPane.add(btnNewButton);
		
		btnAnalyze = new JButton("Analyze");
		btnAnalyze.setEnabled(false);
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnAnalyze, 432, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnNewButton, -6, SpringLayout.WEST, btnAnalyze);
		sl_contentPane.putConstraint(SpringLayout.EAST, btnAnalyze, 0, SpringLayout.EAST, textField);
		btnAnalyze.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		contentPane.add(btnAnalyze);
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);	
		//textField.setColumns(10);
		setVisible(true);
	}
}
