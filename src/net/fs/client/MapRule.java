// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.io.Serializable;
import java.net.ServerSocket;

public class MapRule implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3504577683070928480L;

	int listen_port;
	
	int dst_port;
		
	String name;
	
	boolean using=false;
	
	ServerSocket serverSocket;

	public int getListen_port() {
		return listen_port;
	}

	public void setListen_port(int listen_port) {
		this.listen_port = listen_port;
	}

	public int getDst_port() {
		return dst_port;
	}

	public void setDst_port(int dst_port) {
		this.dst_port = dst_port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
