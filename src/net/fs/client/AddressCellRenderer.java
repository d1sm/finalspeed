package net.fs.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

public class AddressCellRenderer implements ListCellRenderer{

	JPanel panel=null;
	
	JLabel addressLabel;
	
	Color color_normal=new Color(255,255,255);

	Color color_selected=new Color(210,233,255);
	
	JButton button_remove;
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		if(panel==null){
			init();
		}
		updateData( list,  value,  index,  isSelected, cellHasFocus);
		return panel;
	}
	
	void init(){
		panel=new JPanel();
		panel.setLayout(new MigLayout("insets 0 5 0 0","[grow,fill]rel[right]", "[]0[]"));
		panel.setOpaque(true);
		panel.setBackground(color_normal);
		addressLabel=new JLabel("");
		panel.add(addressLabel,"");
		addressLabel.setOpaque(false);
		
		button_remove=new JButton("x");
		//panel.add(button_remove,"align right");
		button_remove.setOpaque(false);
		button_remove.setContentAreaFilled(false);
		button_remove.setBorderPainted(false);
		button_remove.setMargin(new Insets(0, 10, 0, 10));
		button_remove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(e);
			}
		});

	}
	
	void updateData(JList list, Object value, int index, boolean isSelected,boolean cellHasFocus){
		addressLabel.setText(value.toString());
		if(isSelected){
			panel.setBackground(color_selected);
		}else {
			panel.setBackground(color_normal);
		}
	}
	
}
