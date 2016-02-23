// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

import java.net.DatagramPacket;

public interface MessageInterface {
	public int getVer();
	public int getSType();
	public DatagramPacket getDatagramPacket();
}
  
