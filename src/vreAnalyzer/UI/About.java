package vreAnalyzer.UI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.WindowConstants;

public class About extends JDialog {

	private JPanel contentPane;
	private static About instance=null;
	
	/**
	 * Create the frame.
	 * @throws IOException 
	 */
	public About(JFrame parent) throws IOException {
		super(parent,true);
		setTitle("About vreAnalyzer alpha");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 530);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		panel.add(btnOk);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panel_1.add(tabbedPane);
		
		
		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("About", null, scrollPane, null);
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");
		editorPane.setPage(About.class.getResource("/help/About.html"));
		//editorPane.setPage(About.class.getResource("/help/About.html"));
		
				
		editorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// TODO Auto-generated method stub
				editorPaneHyperlinkUpdate(evt);
			}
			
		});
		scrollPane.setColumnHeaderView(editorPane);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		tabbedPane.addTab("License", null, scrollPane_1, null);
		
		JEditorPane editorPane_1 = new JEditorPane();
		editorPane_1.setEditable(false);
		editorPane_1.setPage(About.class.getResource("/help/License.html"));
		editorPane_1.addHyperlinkListener(new javax.swing.event.HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// TODO Auto-generated method stub
				editorPaneHyperlinkUpdate(evt);
			}
			
		});
		scrollPane_1.setViewportView(editorPane_1);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		
		tabbedPane.addTab("Acknowledgement", null, scrollPane_2, null);
		
		JEditorPane editorPane_2 = new JEditorPane();
		editorPane_2.setEditable(false);
		editorPane_2.setPage(About.class.getResource("/help/Acknowledgements.html"));
		editorPane_2.addHyperlinkListener(new javax.swing.event.HyperlinkListener(){

			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// TODO Auto-generated method stub
				editorPaneHyperlinkUpdate(evt);
			}
			
		});
		scrollPane_2.setViewportView(editorPane_2);
		setLocationRelativeTo(null);
		
		
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}
	
	static void editorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {// GEN-FIRST:event_editorPaneHyperlinkUpdate
        try {
            if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                URL url = evt.getURL();
                openWebpage(url);
            }
        } catch (Exception e) {
        }
    }// GEN-LAST:event_editorPaneHyperlinkUpdate
	public static void openWebpage(URI uri) {
	    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	}

	public static void openWebpage(URL url) {
	    try {
	        openWebpage(url.toURI());
	    } catch (URISyntaxException e) {
	        e.printStackTrace();
	    }
	}

	public static About inst(JFrame parent) {
		// TODO Auto-generated method stub
		if(About.instance==null){
			try {
				About.instance = new About(parent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		About.instance.setVisible(true);
		return instance;
	}

}
