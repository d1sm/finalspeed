// Copyright (c) 2015 D1SM.net

package net.fs.rudp;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class ConnectionUDP {
	public InetAddress dstIp;
	public int dstPort;
	public Sender sender;
	public Receiver receiver;
	public UDPOutputStream uos;
	public UDPInputStream uis;
	long connetionId;
	Route route;
	int mode;
	private boolean connected=true;
	long lastLiveTime=System.currentTimeMillis();
	long lastSendLiveTime=0;
	
	static Random ran=new Random();
	
	int connectId;
	
	ConnectionProcessor connectionProcessor;
	
	private LinkedBlockingQueue<DatagramPacket> dpBuffer=new LinkedBlockingQueue<DatagramPacket>();
	
	public ClientControl clientControl;
	
	public boolean localClosed=false,remoteClosed=false,destroied=false;
	
	public boolean stopnow=false;
	
	public ConnectionUDP(Route ro,InetAddress dstIp,int dstPort,int mode,int connectId,ClientControl clientControl) throws Exception {
		this.clientControl=clientControl;
		this.route=ro;
		this.dstIp=dstIp;
		this.dstPort=dstPort;
		this.mode=mode;
		if(mode==1){
			//MLog.println("                 发起连接RUDP "+dstIp+":"+dstPort+" connectId "+connectId);
		}else if(mode==2){
			
			//MLog.println("                 接受连接RUDP "+dstIp+":"+dstPort+" connectId "+connectId);
		}
		this.connectId=connectId;
		try {
			sender=new Sender(this);
			receiver=new Receiver(this);
			uos=new UDPOutputStream (this);
			uis=new UDPInputStream(this);
			if(mode==2){
				ro.createTunnelProcessor().process(this);
			}
		} catch (Exception e) {
			e.printStackTrace();
			connected=false;
			route.connTable.remove(connectId);
			e.printStackTrace();
			//#MLog.println("                 连接失败RUDP "+connectId);
			synchronized(this){
				notifyAll();
			}
			throw e;
		}
		    //#MLog.println("                 连接成功RUDP "+connectId);
		    synchronized(this){
				notifyAll();
			}
	}
	
	public DatagramPacket getPacket(int connectId) throws InterruptedException{
		DatagramPacket dp=(DatagramPacket)dpBuffer.take();
		return dp;
	}
	
	@Override
	public String toString(){
		return new String(dstIp+":"+dstPort);
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	public void close_local(){
		if(!localClosed){
			localClosed=true;
			if(!stopnow){
				sender.sendCloseMessage_Conn();
			}
			destroy(false);
		}
	}
	
	public void close_remote() {
		if(!remoteClosed){
			remoteClosed=true;
			destroy(false);
		}
	}
	
	//完全关闭
	public void destroy(boolean force){
		if(!destroied){
			if((localClosed&&remoteClosed)||force){
				destroied=true;
				connected=false;
				uis.closeStream_Local();
				uos.closeStream_Local();
				sender.destroy();
				receiver.destroy();
				route.removeConnection(this);
				clientControl.removeConnection(this);
			}
		}
	}
	
	public void close_timeout(){
		////#MLog.println("超时关闭RDP连接");
	}
	
	void live(){
		lastLiveTime=System.currentTimeMillis();
	}
}
