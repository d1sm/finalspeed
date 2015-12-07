// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import net.fs.rudp.Route;
import net.fs.utils.Tools;
import net.fs.utils.Tools;
import net.miginfocom.swing.MigLayout;

import com.alibaba.fastjson.JSONObject;

public class ClientNoUI implements ClientUII{
	
	MapClient mapClient;
	
	ClientConfig config;
	
	String configFilePath="client_config.json";
	
	ClientNoUI(){
		loadConfig();
		Route.localDownloadSpeed=config.downloadSpeed;
		Route.localUploadSpeed=config.uploadSpeed;
//		mapClient=new MapClient(config.getSocks5Port());
//		mapClient.setUi(this);
//		mapClient.setMapServer(config.getServerAddress(), config.getServerPort(),config.getRemotePort()	,config.getPasswordMd5(),config.getPasswordMd5_Proxy(),config.isDirect_cn());
	}
	
	void openUrl(String url){
		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
	
	public void setMessage(String message){
		//MLog.info("状态: "+message);
	}
	
	ClientConfig loadConfig(){
		ClientConfig cfg=new ClientConfig();
		if(!new File(configFilePath).exists()){
			JSONObject json=new JSONObject();
			try {
				saveFile(json.toJSONString().getBytes(), configFilePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			String content=readFileUtf8(configFilePath);
			JSONObject json=JSONObject.parseObject(content);
			cfg.setServerAddress(json.getString("server_address"));
			cfg.setServerPort(json.getIntValue("server_port"));
			cfg.setRemotePort(json.getIntValue("remote_port"));
			if(json.containsKey("direct_cn")){
				cfg.setDirect_cn(json.getBooleanValue("direct_cn"));
			}
			cfg.setDownloadSpeed(json.getIntValue("download_speed"));
			cfg.setUploadSpeed(json.getIntValue("upload_speed"));
			if(json.containsKey("socks5_port")){
				cfg.setSocks5Port(json.getIntValue("socks5_port"));
			}
			config=cfg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cfg;
	}
		
	public static String readFileUtf8(String path) throws Exception{
		String str=null;
		FileInputStream fis=null;
		DataInputStream dis=null;
		try {
			File file=new File(path);

			int length=(int) file.length();
			byte[] data=new byte[length];

			fis=new FileInputStream(file);
			dis=new DataInputStream(fis);
			dis.readFully(data);
			str=new String(data,"utf-8");

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(dis!=null){
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return str;
	}
	
	void saveFile(byte[] data,String path) throws Exception{
		FileOutputStream fos=null;
		try {
			fos=new FileOutputStream(path);
			fos.write(data);
		} catch (Exception e) {
			throw e;
		} finally {
			if(fos!=null){
				fos.close();
			}
		}
	}
	
	public void updateUISpeed(int conn,int downloadSpeed,int uploadSpeed){
//		String string="连接数:"+conn+" 下载:"+Tools.getSizeStringKB(downloadSpeed)+"/S"
//				+" 上传:"+Tools.getSizeStringKB(uploadSpeed)+"/S";
//		if(downloadSpeedField!=null){
//			downloadSpeedField.setText(string);
//		}
	}

	JButton createButton(String name){
		JButton button=new JButton(name);
		button.setMargin(new Insets(0,5,0,5));
		button.setFocusPainted(false);
		return button;
	}
	
	
	void initUI(){
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				Font font = new Font("宋体",Font.PLAIN,12);   
				UIManager.put("ToolTip.font",font);   
				UIManager.put("Table.font",font);   
				UIManager.put("TableHeader.font",font);   
				UIManager.put("TextField.font",font);   
				UIManager.put("ComboBox.font",font);   
				UIManager.put("TextField.font",font);   
				UIManager.put("PasswordField.font",font);
				UIManager.put("TextArea.font,font",font);
				UIManager.put("TextPane.font",font);
				UIManager.put("EditorPane.font",font);   
				UIManager.put("FormattedTextField.font",font);   
				UIManager.put("Button.font",font);   
				UIManager.put("CheckBox.font",font);   
				UIManager.put("RadioButton.font",font);   
				UIManager.put("ToggleButton.font",font);   
				UIManager.put("ProgressBar.font",font);   
				UIManager.put("DesktopIcon.font",font);   
				UIManager.put("TitledBorder.font",font);   
				UIManager.put("Label.font",font);   
				UIManager.put("List.font",font);   
				UIManager.put("TabbedPane.font",font);   
				UIManager.put("MenuBar.font",font);   
				UIManager.put("Menu.font",font);   
				UIManager.put("MenuItem.font",font);   
				UIManager.put("PopupMenu.font",font);   
				UIManager.put("CheckBoxMenuItem.font",font);
				UIManager.put("RadioButtonMenuItem.font",font);
				UIManager.put("Spinner.font",font);
				UIManager.put("Tree.font",font);
				UIManager.put("ToolBar.font",font);
				UIManager.put("OptionPane.messageFont",font);
				UIManager.put("OptionPane.buttonFont",font);
				
				ToolTipManager.sharedInstance().setInitialDelay(200);
			}

		});
	}

	@Override
	public boolean login() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateNode(boolean testSpeed) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOsx_fw_pf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOsx_fw_ipfw() {
		// TODO Auto-generated method stub
		return false;
	}
}
