// Copyright (c) 2015 D1SM.net

package net.fs.rudp.message;


import java.net.DatagramPacket;
import java.net.InetAddress;

import net.fs.utils.ByteIntConvert;
import net.fs.utils.ByteShortConvert;

public class DataMessage extends Message{
	short sType=net.fs.rudp.message.MessageType.sType_DataMessage;
	int sequence=0;
	int length=0;
	byte [] data;
	byte [] dpData;
	int timeId;
	
	InetAddress dstAddress;
	
	int dstPort;
	
	int offset;
		
	int firstSendTimeId;
	
	long firstSendTime;
	
	public DataMessage(int sequence,byte[] dataq,int offset,short length,
			int connectId,int clientId){
		this.sequence=sequence;
		this.offset=offset;
		this.length=(short) (length);
		this.data=new byte[this.length];
		this.clientId=clientId;
		this.connectId=connectId;
		System.arraycopy(dataq, offset, this.data, 0, length);
		this.length=data.length;
		
	}
	
	public void create(int timeId){
		this.timeId=timeId;
		dpData=new byte[this.length+16+8];
		ByteShortConvert.toByteArray(ver, dpData, 0);  //add: ver
		ByteShortConvert.toByteArray(sType, dpData, 2);  //add: service type
		
		ByteIntConvert.toByteArray(connectId, dpData, 4); //add: sequence
		ByteIntConvert.toByteArray(clientId, dpData, 8); //add: sequence
		
		ByteIntConvert.toByteArray(this.sequence, dpData, 12); //add: sequence
		ByteShortConvert.toByteArray((short) this.length, dpData, 16); //add:length
		ByteIntConvert.toByteArray(this.timeId, dpData, 18); //add: sequence
		System.arraycopy(this.data, 0, dpData, 22, this.length);
		dp=new DatagramPacket(dpData,dpData.length);
		dp.setAddress(dstAddress);
		dp.setPort(dstPort);
		
	}
	
	public DataMessage(DatagramPacket dp){
		this.dp=dp;
		dpData=dp.getData();
		ver=ByteShortConvert.toShort(dpData, 0);
		sType=ByteShortConvert.toShort(dpData, 2);
		
		connectId=ByteIntConvert.toInt(dpData, 4);
		clientId=ByteIntConvert.toInt(dpData, 8);
		
		sequence=ByteIntConvert.toInt(dpData, 12);
		length=ByteShortConvert.toShort(dpData, 16);
		timeId=ByteIntConvert.toInt(dpData, 18);
		data=new byte[length];
		System.arraycopy(dpData, 22, data, 0, length);
	}
	
	public int getSequence(){
		return sequence;
	}
	
	 public byte[] getData(){
		return data;
	}
	 
	public int getLength(){
		return length;
	}

	public int getTimeId() {
		return timeId;
	}

	public void setTimeId(int timeId) {
		this.timeId = timeId;
	}

	public InetAddress getDstAddress() {
		return dstAddress;
	}

	public void setDstAddress(InetAddress dstAddress) {
		this.dstAddress = dstAddress;
	}

	public int getDstPort() {
		return dstPort;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public int getFirstSendTimeId() {
		return firstSendTimeId;
	}

	public void setFirstSendTimeId(int firstSendTimeId) {
		this.firstSendTimeId = firstSendTimeId;
	}

	public long getFirstSendTime() {
		return firstSendTime;
	}

	public void setFirstSendTime(long firstSendTime) {
		this.firstSendTime = firstSendTime;
	}
	
}
