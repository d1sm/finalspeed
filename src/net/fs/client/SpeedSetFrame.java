// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.awt.Dialog;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.fs.utils.MLog;
import net.miginfocom.swing.MigLayout;

public class SpeedSetFrame extends JDialog{

	private static final long serialVersionUID = -3248779355079724594L;
		
	ClientUI ui;
	
	JTextField text_ds,text_us;
	
	SpeedSetFrame(final ClientUI ui,JFrame parent){
		super(parent,Dialog.ModalityType.APPLICATION_MODAL);
		this.ui=ui;
		setTitle("设置带宽");
		
		JPanel panel=(JPanel) getContentPane();
		panel.setLayout(new MigLayout("alignx center,aligny center,insets 10 10 10 10"));
		
		
		panel.add(new JLabel("单位Mb ( 1Mb=128KB,10Mb=1280KB )"),"height ::,wrap");
		panel.add(new JLabel("请正确输入,该值会直接影响加速效果."),"height ::,wrap");

		JPanel p5=new JPanel();
		panel.add(p5,"wrap");
		p5.setLayout(new MigLayout(""));
		p5.add(new JLabel("下载带宽:"));
		text_ds=new JTextField("");
		p5.add(text_ds,"width 50::");
		p5.add(new JLabel("Mb"));
		
		p5.add(new JLabel("  "));
		
		p5.add(new JLabel("上传带宽:"));
		text_us=new JTextField("");
		p5.add(text_us,"width 50::");
		//text_us.setEditable(false);
		p5.add(new JLabel("Mb"));
		
		JPanel p6=new JPanel();
		panel.add(p6,"align center,wrap");
		p6.setLayout(new MigLayout("align center"));
		
		JButton button_ok=createButton("确定");
		p6.add(button_ok);
		button_ok.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String us=text_ds.getText().trim();
				String ds=text_us.getText().trim();
				try {
					int d=(int) (Float.parseFloat(us)*1024*1024/8/1.1);
					int u=(int) (Float.parseFloat(ds)*1024*1024/8/1.1);
					ui.setSpeed(d, u);
					setVisible(false);
				} catch (Exception e2) {
					//e2.printStackTrace();
					JOptionPane.showMessageDialog(ui.mainFrame, "输入错误!");
				}
				
			}
		});
		
		p6.add(new JLabel(" "));
		
		JButton button_cancel=createButton("取消");
		p6.add(button_cancel);
		button_cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		pack();
		setLocationRelativeTo(parent);
		if(ui.isVisible){
			setVisible(true);
			//MLog.println("请在client_config.json中设置带宽");
		} else {
			//MLog.println("请在client_config.json中设置带宽");
			//System.exit(0);
		}
	}
	
	JButton createButton(String name){
		JButton button=new JButton(name);
		button.setMargin(new Insets(0,5,0,5));
		button.setFocusPainted(false);
		return button;
	}

	

}
