// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import org.pcap4j.core.Pcaps;

import net.fs.rudp.Route;
import net.fs.utils.LogOutputStream;
import net.fs.utils.MLog;
import net.fs.utils.Tools;
import net.miginfocom.swing.MigLayout;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ClientUI implements ClientUII, WindowListener {

    JFrame mainFrame;

    JComponent mainPanel;

    JComboBox text_serverAddress;

    MapClient mapClient;

    JLabel uploadSpeedField, downloadSpeedField, stateText;

    ClientConfig config = null;

    String configFilePath = "client_config.json";

    String logoImg = "img/offline.png";

    String offlineImg = "img/offline.png";

    String name = "FinalSpeed";

    private TrayIcon trayIcon;

    private SystemTray tray;

    int serverVersion = -1;

    int localVersion = 5;

    boolean checkingUpdate = false;

    String domain = "";

    String homeUrl;

    public static ClientUI ui;

    JTextField text_ds, text_us;

    boolean ky = true;

    String errorMsg = "保存失败请检查输入信息!";

    JButton button_site;

    MapRuleListModel model;

    public MapRuleListTable tcpMapRuleListTable;

    boolean capSuccess = false;
    Exception capException = null;
    boolean b1 = false;

    boolean success_firewall_windows = true;

    boolean success_firewall_osx = true;

    String systemName = null;

    public boolean osx_fw_pf = false;

    public boolean osx_fw_ipfw = false;

    public boolean isVisible = true;

    JRadioButton r_tcp, r_udp;

    String updateUrl;
    
    boolean min=false;
    
    LogFrame logFrame;
    
    LogOutputStream los;
    
    boolean tcpEnable=true;
    
    {
        domain = "ip4a.com";
        homeUrl = "http://www.ip4a.com/?client_fs";
        updateUrl = "http://fs.d1sm.net/finalspeed/update.properties";
    }

    ClientUI(final boolean isVisible,boolean min) {
    	this.min=min;
        setVisible(isVisible);
        
        if(isVisible){
        	 los=new LogOutputStream(System.out);
             System.setOut(los);
             System.setErr(los);
        }
        
        
        systemName = System.getProperty("os.name").toLowerCase();
        MLog.info("System: " + systemName + " " + System.getProperty("os.version"));
        ui = this;
        mainFrame = new JFrame();
        mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(logoImg));
        initUI();
        checkQuanxian();
        loadConfig();
        mainFrame.setTitle("FinalSpeed 1.2");
        mainPanel = (JPanel) mainFrame.getContentPane();
        mainPanel.setLayout(new MigLayout("align center , insets 10 10 10 10"));
        mainPanel.setBorder(null);

        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                text_ds.requestFocus();
            }
        });

        JPanel centerPanel = new JPanel();
        mainPanel.add(centerPanel, "wrap");
        centerPanel.setLayout(new MigLayout("insets 0 0 0 0"));

        JPanel loginPanel = new JPanel();
        centerPanel.add(loginPanel, "");
        loginPanel.setLayout(new MigLayout("insets 0 0 0 0"));

        JLabel label_msg = new JLabel();
        label_msg.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new MigLayout("insets 10 0 10 0"));

        centerPanel.add(rightPanel, "width :: ,top");

        JPanel mapPanel = new JPanel();
        mapPanel.setLayout(new MigLayout("insets 0 0 0 0"));
        mapPanel.setBorder(BorderFactory.createTitledBorder("加速列表"));

        rightPanel.add(mapPanel);

        model = new MapRuleListModel();
        tcpMapRuleListTable = new MapRuleListTable(this, model);

        JScrollPane tablePanel = new JScrollPane();
        tablePanel.setViewportView(tcpMapRuleListTable);

        mapPanel.add(tablePanel, "height 50:160:1024 ,growy,width :250:,wrap");
        tablePanel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                tcpMapRuleListTable.clearSelection();
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

        });


        JPanel p9 = new JPanel();
        p9.setLayout(new MigLayout("insets 1 0 3 0 "));
        mapPanel.add(p9, "align center,wrap");
        JButton button_add = createButton("添加");
        p9.add(button_add);
        button_add.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                AddMapFrame sf = new AddMapFrame(ui, mainFrame, null, false);
            }

        });
        JButton button_edit = createButton("修改");
        p9.add(button_edit);
        button_edit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tcpMapRuleListTable.getSelectedRow();
                if (index > -1) {
                    MapRule mapRule = model.getMapRuleAt(index);
                    AddMapFrame sf = new AddMapFrame(ui, mainFrame, mapRule, true);
                }
            }

        });
        JButton button_remove = createButton("删除");
        p9.add(button_remove);
        button_remove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int index = tcpMapRuleListTable.getSelectedRow();
                if (index > -1) {
                    MapRule mapRule = model.getMapRuleAt(index);

                    mapClient.portMapManager.removeMapRule(mapRule.getName());
                    loadMapRule();
                }
            }

        });

        JPanel pa = new JPanel();
        pa.setBorder(BorderFactory.createTitledBorder("服务器"));
        pa.setLayout(new MigLayout("insets 0 0 0 0"));
        loginPanel.add(pa, "growx,wrap");
        JPanel p1 = new JPanel();
        p1.setLayout(new MigLayout("insets 0 0 0 0"));
        pa.add(p1, "wrap");
        p1.add(new JLabel("地址:"), "width 50::");
        text_serverAddress = new JComboBox();
        text_serverAddress.setToolTipText("主机:端口号");
        p1.add(text_serverAddress, "width 150::");
        text_serverAddress.setEditable(true);
        TextComponentPopupMenu.installToComponent(text_serverAddress);
        
        ListCellRenderer renderer = new AddressCellRenderer();
        text_serverAddress.setRenderer(renderer);
        text_serverAddress.setEditable(true);
        
        text_serverAddress.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println(text_serverAddress.getSelectedItem().toString());
			}
		});
        
        for(int n=0;n<config.getRecentAddressList().size();n++){
        	text_serverAddress.addItem(config.getRecentAddressList().get(n));
        }
        
        if(config.getRecentAddressList().size()==0){
        	text_serverAddress.setSelectedItem("");
        }
        
        JButton button_removeAddress=createButton("删除");
        p1.add(button_removeAddress, "");
        button_removeAddress.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String address=text_serverAddress.getSelectedItem().toString();
				if(!address.equals("")){
					int result=JOptionPane.showConfirmDialog(mainFrame, "确定删除吗?","消息", JOptionPane.YES_NO_OPTION);
					if(result==JOptionPane.OK_OPTION){
						text_serverAddress.removeItem(address);
						String selectText="";
						if(text_serverAddress.getModel().getSize()>0){
							selectText=text_serverAddress.getModel().getElementAt(0).toString();
						}
						text_serverAddress.setSelectedItem(selectText);
					}
				}
			}
		});

        JPanel panelr = new JPanel();
        pa.add(panelr, "wrap");
        panelr.setLayout(new MigLayout("insets 0 0 0 0"));
        panelr.add(new JLabel("传输协议:"));
        r_tcp = new JRadioButton("TCP");
        r_tcp.setFocusPainted(false);
        panelr.add(r_tcp);
        r_udp = new JRadioButton("UDP");
        r_udp.setFocusPainted(false);
        panelr.add(r_udp);
        ButtonGroup bg = new ButtonGroup();
        bg.add(r_tcp);
        bg.add(r_udp);
        if (config.getProtocal().equals("udp")) {
            r_udp.setSelected(true);
        } else {
            r_tcp.setSelected(true);
        }


        JPanel sp = new JPanel();
        sp.setBorder(BorderFactory.createTitledBorder("物理带宽"));
        sp.setLayout(new MigLayout("insets 5 5 5 5"));
        JPanel pa1 = new JPanel();
        sp.add(pa1, "wrap");
        pa1.setLayout(new MigLayout("insets 0 0 0 0"));
        loginPanel.add(sp, "wrap");
        pa1.add(new JLabel("下载:"), "width ::");
        text_ds = new JTextField("0");
        pa1.add(text_ds, "width 80::");
        text_ds.setHorizontalAlignment(JTextField.RIGHT);
        text_ds.setEditable(false);

        JButton button_set_speed = createButton("设置带宽");
        pa1.add(button_set_speed);
        button_set_speed.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SpeedSetFrame sf = new SpeedSetFrame(ui, mainFrame);
            }
        });

        
        JPanel pa2 = new JPanel();
        sp.add(pa2, "wrap");
        pa2.setLayout(new MigLayout("insets 0 0 0 0"));
        loginPanel.add(sp, "wrap");
        pa2.add(new JLabel("上传:"), "width ::");
        text_us = new JTextField("0");
        pa2.add(text_us, "width 80::");
        text_us.setHorizontalAlignment(JTextField.RIGHT);
        text_us.setEditable(false);
        
        
        JPanel sp2 = new JPanel();
        sp2.setLayout(new MigLayout("insets 0 0 0 0"));
        loginPanel.add(sp2, "align center,  wrap");
        
        final JCheckBox cb=new JCheckBox("开机启动",config.isAutoStart());
        sp2.add(cb, "align center");
		cb.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				config.setAutoStart(cb.isSelected());
				saveConfig();
				setAutoRun(config.isAutoStart());
			}

		});
		
		JButton button_show_log=createButton("显示日志");
		sp2.add(button_show_log,"wrap");
		button_show_log.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(logFrame==null){
					 logFrame=new LogFrame(ui);
		             logFrame.setSize(700, 400);
		             logFrame.setLocationRelativeTo(null);
		             los.addListener(logFrame);
		             
		             if(los.getBuffer()!=null){
		            	logFrame.showText(los.getBuffer().toString());
		     			los.setBuffer(null);
		             }
				}
				logFrame.setVisible(true);
			}
		});

        JPanel p4 = new JPanel();
        p4.setLayout(new MigLayout("insets 5 0 0 0 "));
        loginPanel.add(p4, "align center,wrap");
        JButton button_save = createButton("确定");
        p4.add(button_save);

        button_site = createButton("网站");
        p4.add(button_site);
        button_site.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openUrl(homeUrl);
            }
        });

        JButton button_exit = createButton("退出");
        p4.add(button_exit);
        button_exit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        button_save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (config.getDownloadSpeed() == 0 || config.getUploadSpeed() == 0) {
                    SpeedSetFrame sf = new SpeedSetFrame(ui, mainFrame);
                }
                setMessage("");
                saveConfig();
            }
        });
        

        
        stateText = new JLabel("");
        mainPanel.add(stateText, "align right ,wrap");
        

        JPanel p5 = new JPanel();
        p5.setLayout(new MigLayout("insets 5 0 0 0 "));
        mainPanel.add(p5, "align right");
        JButton button_fsa = createButton_Link("FS高级版","http://www.xsocks.me/?fsc");
        p5.add(button_fsa);
        JButton button_wlt = createButton_Link("网络通内网穿透","http://www.youtusoft.com/?fsc");
        p5.add(button_wlt);

        downloadSpeedField = new JLabel();
        downloadSpeedField.setHorizontalAlignment(JLabel.RIGHT);
        p5.add(downloadSpeedField, "width 130:: ,align right ");
        
        updateUISpeed(0, 0, 0);
        setMessage(" ");

        text_serverAddress.setSelectedItem(getServerAddressFromConfig());

        if (config.getRemoteAddress() != null && !config.getRemoteAddress().equals("") && config.getRemotePort() > 0) {
            String remoteAddressTxt = config.getRemoteAddress() + ":" + config.getRemotePort();
        }

        int width = 500;
        if (systemName.contains("os x")) {
            width = 600;
        }
        //mainFrame.setSize(width, 380);

        mainFrame.pack();

        mainFrame.setLocationRelativeTo(null);

        PopupMenu trayMenu = new PopupMenu();
        if(SystemTray.isSupported()){
             mainFrame.addWindowListener(this);
        	 tray = SystemTray.getSystemTray();
             trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(offlineImg), name, trayMenu);
             trayIcon.setImageAutoSize(true);
             ActionListener listener = new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     mainFrame.toFront();
                     setVisible(true);
                     mainFrame.setVisible(true);
                 }
             };
             trayIcon.addActionListener(listener);
             trayIcon.addMouseListener(new MouseListener() {

                 public void mouseClicked(MouseEvent arg0) {
                 }

                 public void mouseEntered(MouseEvent arg0) {
                 }

                 public void mouseExited(MouseEvent arg0) {
                 }

                 public void mousePressed(MouseEvent arg0) {
                 }

                 public void mouseReleased(MouseEvent arg0) {
                 }

             });

             try {
                 tray.add(trayIcon);
             } catch (AWTException e1) {
                 e1.printStackTrace();
             }
             MenuItem item3;
             try {
                 item3 = new MenuItem("Exit");
                 //item3 = new MenuItem("Exit");
                 ActionListener al = new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         exit();
                     }
                 };
                 item3.addActionListener(al);
                 trayMenu.add(item3);

             } catch (Exception e1) {
                 e1.printStackTrace();
             }
        }else{
        	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }

        boolean tcpEnvSuccess=true;
        checkFireWallOn();
        if (!success_firewall_windows) {
        	tcpEnvSuccess=false;
            if (isVisible) {
                mainFrame.setVisible(true);
                JOptionPane.showMessageDialog(mainFrame, "启动windows防火墙失败,请先运行防火墙服务.");
            }
            MLog.println("启动windows防火墙失败,请先运行防火墙服务.");
           // System.exit(0);
        }
        if (!success_firewall_osx) {
        	tcpEnvSuccess=false;
            if (isVisible) {
                mainFrame.setVisible(true);
                JOptionPane.showMessageDialog(mainFrame, "启动ipfw/pfctl防火墙失败,请先安装.");
            }
            MLog.println("启动ipfw/pfctl防火墙失败,请先安装.");
            //System.exit(0);
        }

        Thread thread = new Thread() {
            public void run() {
                try {
                    Pcaps.findAllDevs();
                    b1 = true;
                } catch (Exception e3) {
                    e3.printStackTrace();

                }
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        //JOptionPane.showMessageDialog(mainFrame,System.getProperty("os.name"));
        if (!b1) {
        	tcpEnvSuccess=false;
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        String msg = "启动失败,请先安装libpcap,否则无法使用tcp协议";
                        if (systemName.contains("windows")) {
                            msg = "启动失败,请先安装winpcap,否则无法使用tcp协议";
                        }
                        if (isVisible) {
                            mainFrame.setVisible(true);
                            JOptionPane.showMessageDialog(mainFrame, msg);
                        }
                        MLog.println(msg);
                        if (systemName.contains("windows")) {
                            try {
                                Process p = Runtime.getRuntime().exec("winpcap_install.exe", null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            tcpEnable=false;
                            //System.exit(0);
                        }
                    }

                });
            } catch (InvocationTargetException e2) {
                e2.printStackTrace();
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }


        try {
            mapClient = new MapClient(this,tcpEnvSuccess);
        } catch (final Exception e1) {
            e1.printStackTrace();
            capException = e1;
            //System.exit(0);
        }

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {

                    if (!mapClient.route_tcp.capEnv.tcpEnable) {
                        if (isVisible) {
                            mainFrame.setVisible(true);
                        }
                        r_tcp.setEnabled(false);
                        r_udp.setSelected(true);
                        //JOptionPane.showMessageDialog(mainFrame,"无可用网络接口,只能使用udp协议.");
                    }

                    //System.exit(0);
                }

            });
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }

        mapClient.setUi(this);

        mapClient.setMapServer(config.getServerAddress(), config.getServerPort(), config.getRemotePort(), null, null, config.isDirect_cn(), config.getProtocal().equals("tcp"),
                null);

        Route.es.execute(new Runnable() {

            @Override
            public void run() {
                checkUpdate();
            }
        });

        setSpeed(config.getDownloadSpeed(), config.getUploadSpeed());
        if (isVisible&!min) {
            mainFrame.setVisible(true);
        }

        loadMapRule();

        if (config.getDownloadSpeed() == 0 || config.getUploadSpeed() == 0) {
            SpeedSetFrame sf = new SpeedSetFrame(ui, mainFrame);
        }

        
    }
    
    String getServerAddressFromConfig(){
    	 String server_addressTxt = config.getServerAddress();
         if (config.getServerAddress() != null && !config.getServerAddress().equals("")) {
             if (config.getServerPort() != 150
                     && config.getServerPort() != 0) {
                 server_addressTxt += (":" + config.getServerPort());
             }
         }
         return server_addressTxt;
    }

    void checkFireWallOn() {
        if (systemName.contains("os x")) {
            String runFirewall = "ipfw";
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                osx_fw_ipfw = true;
            } catch (IOException e) {
                //e.printStackTrace();
            }
            runFirewall = "pfctl";
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                osx_fw_pf = true;
            } catch (IOException e) {
               // e.printStackTrace();
            }
            success_firewall_osx = osx_fw_ipfw | osx_fw_pf;
        } else if (systemName.contains("linux")) {
            String runFirewall = "service iptables start";
        } else if (systemName.contains("windows")) {
            String runFirewall = "netsh advfirewall set allprofiles state on";
            Thread standReadThread = null;
            Thread errorReadThread = null;
            try {
                final Process p = Runtime.getRuntime().exec(runFirewall, null);
                standReadThread = new Thread() {
                    public void run() {
                        InputStream is = p.getInputStream();
                        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
                        while (true) {
                            String line;
                            try {
                                line = localBufferedReader.readLine();
                                if (line == null) {
                                    break;
                                } else {
                                    if (line.contains("Windows")) {
                                        success_firewall_windows = false;
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                //error();
                                exit();
                                break;
                            }
                        }
                    }
                };
                standReadThread.start();

                errorReadThread = new Thread() {
                    public void run() {
                        InputStream is = p.getErrorStream();
                        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(is));
                        while (true) {
                            String line;
                            try {
                                line = localBufferedReader.readLine();
                                if (line == null) {
                                    break;
                                } else {
                                    System.out.println("error" + line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                //error();
                                exit();
                                break;
                            }
                        }
                    }
                };
                errorReadThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                success_firewall_windows = false;
                //error();
            }

            if (standReadThread != null) {
                try {
                    standReadThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (errorReadThread != null) {
                try {
                    errorReadThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    void checkQuanxian() {
        if (systemName.contains("windows")) {
            boolean b = false;
            File file = new File(System.getenv("WINDIR") + "\\test.file");
            //System.out.println("kkkkkkk "+file.getAbsolutePath());
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            b = file.exists();
            file.delete();

            if (!b) {
                //mainFrame.setVisible(true);
                if (isVisible) {
                    JOptionPane.showMessageDialog(null, "请以管理员身份运行! ");
                }
                MLog.println("请以管理员身份运行,否则可能无法正常工作! ");
//                System.exit(0);
            }
        }
    }

    void loadMapRule() {
        tcpMapRuleListTable.setMapRuleList(mapClient.portMapManager.getMapList());
    }

    void select(String name) {
        int index = model.getMapRuleIndex(name);
        if (index > -1) {
            tcpMapRuleListTable.getSelectionModel().setSelectionInterval(index, index);
        }
    }

    void setSpeed(int downloadSpeed, int uploadSpeed) {
        config.setDownloadSpeed(downloadSpeed);
        config.setUploadSpeed(uploadSpeed);
        int s1 = (int) ((float) downloadSpeed * 1.1f);
        text_ds.setText(" " + Tools.getSizeStringKB(s1) + "/s ");
        int s2 = (int) ((float) uploadSpeed * 1.1f);
        text_us.setText(" " + Tools.getSizeStringKB(s2) + "/s ");
        Route.localDownloadSpeed = downloadSpeed;
        Route.localUploadSpeed = config.uploadSpeed;

        saveConfig();
    }


    void exit() {
        mainFrame.setVisible(false);
        System.exit(0);
    }

    void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    public void setMessage(String message) {
        stateText.setText("状态: " + message);
    }

    ClientConfig loadConfig() {
        ClientConfig cfg = new ClientConfig();
        if (!new File(configFilePath).exists()) {
            JSONObject json = new JSONObject();
            try {
                saveFile(json.toJSONString().getBytes(), configFilePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            String content = readFileUtf8(configFilePath);
            JSONObject json = JSONObject.parseObject(content);
            cfg.setServerAddress(json.getString("server_address"));
            cfg.setServerPort(json.getIntValue("server_port"));
            cfg.setRemotePort(json.getIntValue("remote_port"));
            cfg.setRemoteAddress(json.getString("remote_address"));
            if (json.containsKey("direct_cn")) {
                cfg.setDirect_cn(json.getBooleanValue("direct_cn"));
            }
            cfg.setDownloadSpeed(json.getIntValue("download_speed"));
            cfg.setUploadSpeed(json.getIntValue("upload_speed"));
            if (json.containsKey("socks5_port")) {
                cfg.setSocks5Port(json.getIntValue("socks5_port"));
            }
            if (json.containsKey("protocal")) {
                cfg.setProtocal(json.getString("protocal"));
            }
            if (json.containsKey("auto_start")) {
                cfg.setAutoStart(json.getBooleanValue("auto_start"));
            }
            if (json.containsKey("recent_address_list")) {
            	JSONArray list=json.getJSONArray("recent_address_list");
            	for (int i = 0; i < list.size(); i++) {
            		cfg.getRecentAddressList().add(list.get(i).toString());
				}
            }
           
            config = cfg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfg;
    }

    void saveConfig() {
        Thread thread = new Thread() {
            public void run() {
                boolean success = false;
                try {
                    int serverPort = 150;
                    String addressTxt ="";
                    if(text_serverAddress.getSelectedItem()!=null){
                    	addressTxt =text_serverAddress.getSelectedItem().toString();
                    }
                    addressTxt = addressTxt.trim().replaceAll(" ", "");
                   
                    String serverAddress = addressTxt;
                    if (addressTxt.startsWith("[")) {
                        int index = addressTxt.lastIndexOf("]:");
                        if (index > 0) {
                            serverAddress = addressTxt.substring(0, index + 1);
                            String ports = addressTxt.substring(index + 2);
                            serverPort = Integer.parseInt(ports);
                        }
                    } else {
                        int index = addressTxt.lastIndexOf(":");
                        if (index > 0) {
                            serverAddress = addressTxt.substring(0, index);
                            String ports = addressTxt.substring(index + 1);
                            serverPort = Integer.parseInt(ports);
                        }
                    }

                    String protocal = "tcp";
                    if (r_udp.isSelected()) {
                        protocal = "udp";
                    }

                    JSONObject json = new JSONObject();
                    json.put("server_address", serverAddress);
                    json.put("server_port", serverPort);
                    json.put("download_speed", config.getDownloadSpeed());
                    json.put("upload_speed", config.getUploadSpeed());
                    json.put("socks5_port", config.getSocks5Port());
                    json.put("protocal", protocal);
                    json.put("auto_start", config.isAutoStart());

                    
                    if(text_serverAddress.getModel().getSize()>0){
                    	text_serverAddress.removeItem(addressTxt);
                    }
                    text_serverAddress.insertItemAt(addressTxt, 0);
                    text_serverAddress.setSelectedItem(addressTxt);;
                    

                    JSONArray recentAddressList=new JSONArray();
                    
                    
                    int size=text_serverAddress.getModel().getSize();
                    for(int n=0;n<size;n++){
                    	String address=text_serverAddress.getModel().getElementAt(n).toString();
                    	if(!address.equals("")){
                    		recentAddressList.add(address);
                    	}
                    }
                    json.put("recent_address_list", recentAddressList);
                    
                    
                    saveFile(json.toJSONString().getBytes("utf-8"), configFilePath);
                    config.setServerAddress(serverAddress);
                    config.setServerPort(serverPort);
                    config.setProtocal(protocal);
                    success = true;

                    String realAddress = serverAddress;
                    if (realAddress != null) {
                        realAddress = realAddress.replace("[", "");
                        realAddress = realAddress.replace("]", "");
                    }

                    boolean tcp = protocal.equals("tcp");

                    mapClient.setMapServer(realAddress, serverPort, 0, null, null, config.isDirect_cn(), tcp,
                            null);
                    mapClient.closeAndTryConnect();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (!success) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(mainFrame, errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                }


            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String readFileUtf8(String path) throws Exception {
        String str = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        try {
            File file = new File(path);

            int length = (int) file.length();
            byte[] data = new byte[length];

            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            dis.readFully(data);
            str = new String(data, "utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return str;
    }

    void saveFile(byte[] data, String path) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data);
        } catch (Exception e) {
            if (systemName.contains("windows")) {
                JOptionPane.showMessageDialog(null, "保存配置文件失败,请尝试以管理员身份运行! " + path);
                System.exit(0);
            }
            throw e;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public void updateUISpeed(int conn, int downloadSpeed, int uploadSpeed) {
        String string =
                " 下载:" + Tools.getSizeStringKB(downloadSpeed) + "/s"
                        + " 上传:" + Tools.getSizeStringKB(uploadSpeed) + "/s";
        if (downloadSpeedField != null) {
            downloadSpeedField.setText(string);
        }
    }

    JButton createButton(String name) {
        JButton button = new JButton(name);
        button.setMargin(new Insets(0, 5, 0, 5));
        button.setFocusPainted(false);
        return button;
    }
    
    JButton createButton_Link(String name,final String url) {
        JButton button = new JButton(name);
        Color c = new Color(0,0,255);
        button.setBackground(c);  
        button.setForeground(new Color(100,100,255));
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setMargin(new Insets(0, 2, 0, 2));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openUrl(url);
            }
        });
        return button;
    }
    

    boolean haveNewVersion() {
        return serverVersion > localVersion;
    }

    public void checkUpdate() {
        for (int i = 0; i < 3; i++) {
            checkingUpdate = true;
            try {
                Properties propServer = new Properties();
                HttpURLConnection uc = Tools.getConnection(updateUrl);
                uc.setUseCaches(false);
                InputStream in = uc.getInputStream();
                propServer.load(in);
                serverVersion = Integer.parseInt(propServer.getProperty("version"));
                break;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } finally {
                checkingUpdate = false;
            }
        }
        if (this.haveNewVersion()) {
            int option = JOptionPane.showConfirmDialog(mainFrame, "发现新版本,立即更新吗?", "提醒", JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.YES_OPTION) {
                openUrl(homeUrl);
            }
        }

    }

    void initUI() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Font font = new Font("宋体", Font.PLAIN, 12);
                UIManager.put("ToolTip.font", font);
                UIManager.put("Table.font", font);
                UIManager.put("TableHeader.font", font);
                UIManager.put("TextField.font", font);
                UIManager.put("ComboBox.font", font);
                UIManager.put("TextField.font", font);
                UIManager.put("PasswordField.font", font);
                UIManager.put("TextArea.font,font", font);
                UIManager.put("TextPane.font", font);
                UIManager.put("EditorPane.font", font);
                UIManager.put("FormattedTextField.font", font);
                UIManager.put("Button.font", font);
                UIManager.put("CheckBox.font", font);
                UIManager.put("RadioButton.font", font);
                UIManager.put("ToggleButton.font", font);
                UIManager.put("ProgressBar.font", font);
                UIManager.put("DesktopIcon.font", font);
                UIManager.put("TitledBorder.font", font);
                UIManager.put("Label.font", font);
                UIManager.put("List.font", font);
                UIManager.put("TabbedPane.font", font);
                UIManager.put("MenuBar.font", font);
                UIManager.put("Menu.font", font);
                UIManager.put("MenuItem.font", font);
                UIManager.put("PopupMenu.font", font);
                UIManager.put("CheckBoxMenuItem.font", font);
                UIManager.put("RadioButtonMenuItem.font", font);
                UIManager.put("Spinner.font", font);
                UIManager.put("Tree.font", font);
                UIManager.put("ToolBar.font", font);
                UIManager.put("OptionPane.messageFont", font);
                UIManager.put("OptionPane.buttonFont", font);

                ToolTipManager.sharedInstance().setInitialDelay(130);
            }

        });
    }
    
	public static void setAutoRun(boolean run) {
		String s = new File(".").getAbsolutePath();
		String currentPaht = s.substring(0, s.length() - 1);
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(currentPaht, "\\");
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());
			sb.append("\\\\");
		}
		ArrayList<String> list = new ArrayList<String>();
		list.add("Windows Registry Editor Version 5.00");
		String name="fsclient";
//		if(PMClientUI.mc){
//			name="wlg_mc";
//		}
		if (run) {
			list.add("[HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run]");
			list.add("\""+name+"\"=\"" + sb.toString() + "finalspeedclient.exe -min" + "\"");
		} else {
			list.add("[HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run]");
			list.add("\""+name+"\"=-");
		}

		File file = null;
		try {
			file = new File("import.reg");
			FileWriter fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			for (int i = 0; i < list.size(); i++) {
				String ss = list.get(i);
				if (!ss.equals("")) {
					pw.println(ss);
				}
			}
			pw.flush();
			pw.close();
			Process p = Runtime.getRuntime().exec("regedit /s " + "import.reg");
			p.waitFor();
		} catch (Exception e1) {
			// e1.printStackTrace();
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        mainFrame.setVisible(false);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }


    @Override
    public boolean login() {
        return false;
    }


    @Override
    public boolean updateNode(boolean testSpeed) {
        return true;

    }

    public boolean isOsx_fw_pf() {
        return osx_fw_pf;
    }

    public void setOsx_fw_pf(boolean osx_fw_pf) {
        this.osx_fw_pf = osx_fw_pf;
    }

    public boolean isOsx_fw_ipfw() {
        return osx_fw_ipfw;
    }

    public void setOsx_fw_ipfw(boolean osx_fw_ipfw) {
        this.osx_fw_ipfw = osx_fw_ipfw;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
}
