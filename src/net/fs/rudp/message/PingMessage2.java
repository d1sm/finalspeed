// Copyright (c) 2015 D1SM.net

package net.fs.rudp.message;

import java.net.DatagramPacket;

import net.fs.utils.ByteIntConvert;
import net.fs.utils.ByteShortConvert;



public class PingMessage2 extends Message{
	
	public short sType=net.fs.rudp.message.MessageType.sType_PingMessage2;
	
	byte[] dpData=new byte[16];
	
	int pingId;
	
	public PingMessage2(int connectId,int clientId,int pingId){
		ByteShortConvert.toByteArray(ver, dpData, 0);  //add: ver
		ByteShortConvert.toByteArray(sType, dpData, 2);  //add: service type
		ByteIntConvert.toByteArray(connectId, dpData, 4); //add: sequence
		ByteIntConvert.toByteArray(clientId, dpData, 8); //add: sequence
		ByteIntConvert.toByteArray(pingId, dpData, 12); //add: sequence
		dp=new DatagramPacket(dpData,dpData.length);
	}
	
	public PingMessage2(DatagramPacket dp){
		this.dp=dp;
		dpData=dp.getData();
		ver=ByteShortConvert.toShort(dpData, 0);
		sType=ByteShortConvert.toShort(dpData, 2);
		connectId=ByteIntConvert.toInt(dpData, 4);
		clientId=ByteIntConvert.toInt(dpData, 8);
		pingId=ByteIntConvert.toInt(dpData, 12);
	}

	public int getPingId() {
		return pingId;
	}

	public void setPingId(int pingId) {
		this.pingId = pingId;
	}

}
