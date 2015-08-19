package vreAnalyzer.UI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

public class InputConfigure extends JDialog {

	private final JPanel contentPanel = new JPanel();

	private static InputConfigure instance;
	
	public InputConfigure(JFrame parent) {
		super(parent,false);
		setTitle("Input Configure Panel");
		setBounds(100, 100, 600, 530);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		SpringLayout sl_contentPanel = new SpringLayout();
		contentPanel.setLayout(sl_contentPanel);
		
		JLabel lblApplicationType = new JLabel("Application Type:");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, lblApplicationType, 24, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, lblApplicationType, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, lblApplicationType, 146, SpringLayout.WEST, contentPanel);
		contentPanel.add(lblApplicationType);
		
		JRadioButton rdbtnNormal = new JRadioButton("Normal");
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, lblApplicationType, -6, SpringLayout.NORTH, rdbtnNormal);
		sl_contentPanel.putConstraint(SpringLayout.NORTH, rdbtnNormal, 46, SpringLayout.NORTH, contentPanel);
		contentPanel.add(rdbtnNormal);
		
		JRadioButton rdbtnHadoopPure = new JRadioButton("Hadoop Pure");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, rdbtnHadoopPure, 46, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, rdbtnHadoopPure, 100, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.EAST, rdbtnNormal, -6, SpringLayout.WEST, rdbtnHadoopPure);
		contentPanel.add(rdbtnHadoopPure);
		
		JRadioButton rdbtnSpark = new JRadioButton("Spark");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, rdbtnSpark, 46, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, rdbtnSpark, 9, SpringLayout.EAST, rdbtnHadoopPure);
		contentPanel.add(rdbtnSpark);
		
		JRadioButton rdbtnHive = new JRadioButton("Hive");
		sl_contentPanel.putConstraint(SpringLayout.NORTH, rdbtnHive, 46, SpringLayout.NORTH, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.WEST, rdbtnHive, 11, SpringLayout.EAST, rdbtnSpark);
		
		ButtonGroup appMode = new ButtonGroup();
		appMode.add(rdbtnNormal);
		appMode.add(rdbtnHadoopPure);
		appMode.add(rdbtnSpark);
		appMode.add(rdbtnHive);
		
		contentPanel.add(rdbtnHive);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setLocationRelativeTo(null);
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}



	public static InputConfigure inst(JFrame parent) {
		// TODO Auto-generated method stub
		if(instance==null){
			instance = new InputConfigure(parent);
		}
		return instance;
	}
}
