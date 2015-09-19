package vreAnalyzer.UI;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableCellRender extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// TODO Auto-generated method stub
		Component cr= super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
				row, column);
		
		if(table.isRowSelected(row)){
			Object color = value;
			if(color instanceof Color){
				Color bgcolor = (Color)color;
				cr.setBackground(bgcolor);
			}
			else{
				cr.setBackground(table.getSelectionBackground());
				cr.setForeground(table.getSelectionForeground());
			}
		}else{
			Object color = value;
			if(color instanceof Color){
				Color bgcolor = (Color)color;
				cr.setBackground(bgcolor);
			}else{
				cr.setBackground(table.getBackground());
				cr.setForeground(table.getForeground());
			}
		}
		return cr;
	}
	
}
