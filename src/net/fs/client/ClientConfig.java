// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.util.ArrayList;

public class ClientConfig {
	
	String serverAddress="";
	
	int serverPort;
	
	int remotePort;
	
	int downloadSpeed,uploadSpeed;
	
	boolean direct_cn=true;
	
	int socks5Port=1083;
	
	String remoteAddress;
	
	String protocal="tcp";
	
	boolean autoStart=false;
	
	ArrayList<String> recentAddressList=new ArrayList<String>();

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public boolean isDirect_cn() {
		return direct_cn;
	}

	public void setDirect_cn(boolean direct_cn) {
		this.direct_cn = direct_cn;
	}

	public int getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(int downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public int getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(int uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

	public int getSocks5Port() {
		return socks5Port;
	}

	public void setSocks5Port(int socks5Port) {
		this.socks5Port = socks5Port;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getProtocal() {
		return protocal;
	}

	public void setProtocal(String protocal) {
		this.protocal = protocal;
	}

	public boolean isAutoStart() {
		return autoStart;
	}

	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	public ArrayList<String> getRecentAddressList() {
		return recentAddressList;
	}

	public void setRecentAddressList(ArrayList<String> recentAddressList) {
		this.recentAddressList = recentAddressList;
	}
	
}
