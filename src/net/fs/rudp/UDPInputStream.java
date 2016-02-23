// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPInputStream {
	
	DatagramSocket ds;
	InetAddress dstIp;
	int dstPort;
	Receiver receiver;
	
	boolean streamClosed=false;
	
	ConnectionUDP conn;
	
	UDPInputStream(ConnectionUDP conn){
		this.conn=conn;
		receiver=conn.receiver;
	}
	
	public int read(byte[] b, int off, int len) throws ConnectException, InterruptedException {
		byte[] b2=null;
		b2 = read2();
		if(len<b2.length){
			throw new ConnectException("error5");
		}else{
			System.arraycopy(b2, 0, b, off, b2.length);
			return b2.length;
		}
	}
	
	public byte[] read2() throws ConnectException, InterruptedException{
		return receiver.receive();
	}
	
	public void closeStream_Local(){
		if(!streamClosed){
			receiver.closeStream_Local();
		}
	}


}
