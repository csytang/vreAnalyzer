package vreAnalyzer.UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ConfigureWizard extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JLabel lblVrehadoopCommand;
	private static ConfigureWizard instance;
	/**
	 * Create the dialog.
	 */
	public static ConfigureWizard inst(NewProjectPanel owner){
		if(instance==null)
			instance=new ConfigureWizard(owner);
		return instance;
	}
	
	public ConfigureWizard(NewProjectPanel owner) {
		super(owner,true);
		setTitle("Configure Wizard");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		SpringLayout sl_contentPanel = new SpringLayout();
		contentPanel.setLayout(sl_contentPanel);
		{
			lblVrehadoopCommand = new JLabel("vreHadoop Command:");
			sl_contentPanel.putConstraint(SpringLayout.NORTH, lblVrehadoopCommand, 10, SpringLayout.NORTH, contentPanel);
			sl_contentPanel.putConstraint(SpringLayout.WEST, lblVrehadoopCommand, 10, SpringLayout.WEST, contentPanel);
			sl_contentPanel.putConstraint(SpringLayout.SOUTH, lblVrehadoopCommand, 33, SpringLayout.NORTH, contentPanel);
			sl_contentPanel.putConstraint(SpringLayout.EAST, lblVrehadoopCommand, 174, SpringLayout.WEST, contentPanel);
			contentPanel.add(lblVrehadoopCommand);
		}
		
		JScrollPane scrollPane = new JScrollPane();
		sl_contentPanel.putConstraint(SpringLayout.NORTH, scrollPane, 6, SpringLayout.SOUTH, lblVrehadoopCommand);
		sl_contentPanel.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, contentPanel);
		sl_contentPanel.putConstraint(SpringLayout.SOUTH, scrollPane, 186, SpringLayout.SOUTH, lblVrehadoopCommand);
		sl_contentPanel.putConstraint(SpringLayout.EAST, scrollPane, 430, SpringLayout.WEST, contentPanel);
		contentPanel.add(scrollPane);
		
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(textArea.getText().isEmpty()){
							JOptionPane.showMessageDialog(owner, "Command cannot be empty","No command error",JOptionPane.WARNING_MESSAGE);
						}
						else{
							MainFrame.inst().generateSootCommand(textArea.getText());
							NewProjectPanel.inst().setWizardCommand(textArea.getText());
							dispose();
						}
					}

				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
