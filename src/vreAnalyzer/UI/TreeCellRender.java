package vreAnalyzer.UI;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeCellRender extends DefaultTreeCellRenderer{
	List<File>targets;
	public TreeCellRender(List<File>renders){
		targets = renders;
	}
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		// TODO Auto-generated method stub
		final Component c =  (Component)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		Object nodeObject = node.getUserObject();
		if(nodeObject instanceof File){
			
			File fileObject = (File)nodeObject;
			if(targets.contains(fileObject)){	
				this.setOpaque(true);
				this.setBackground(Color.RED);
				this.setForeground(Color.WHITE);
			}
		}else{
			this.setOpaque(false);
			this.setBackground(Color.WHITE);
		}
		
		return c;
	}
	
}
