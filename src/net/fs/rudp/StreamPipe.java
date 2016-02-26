// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;


public class StreamPipe {
	
	DataInputStream is;
	
	DataOutputStream os;
		
	List<PipeListener> listenerList;
	
	boolean closed=false;
		
	int maxLen=2000;
		
	long lastResetTime;
	
	int maxSpeed=100*1024*1024;
		
	int port=0;
		
	int limiteConnectTime;
	
	String userId="";
		
	byte[] preReadData;
	
	int preReadDataLength;
		
	Socket socketA,socketB;
		
	boolean writing=false;
	
	int BUF_SIZE;
	
	ArrayList<byte[]> dataList=new ArrayList<byte[]>();
	
	Semaphore semp_have_data=new Semaphore(0);
	
	int cachedSize=0;
	
	int supserSocketId=-1;
	
	static int type_request=1;
	
	static int type_respone=2;
	
	int type=0;
	
	ConnInfo connInfo;
				
	public StreamPipe(ConnInfo connInfo,final DataInputStream is,final DataOutputStream os,final int BUF_SIZE,final int maxSpeed){
		this(connInfo,is, os, BUF_SIZE, maxSpeed, null, 0);
	}
	
	public StreamPipe(ConnInfo ci,final DataInputStream is,final DataOutputStream os,int BUF_SIZE1,final int maxSpeed,final byte[] preReadData,final int preReadDataLength){
		connInfo=ci;
		listenerList=new Vector<PipeListener>();
		this.maxSpeed=maxSpeed;
		this.preReadData=preReadData;
		BUF_SIZE=BUF_SIZE1;
		if(maxSpeed<=50*1024){
			//BUF_SIZE=100;
		}
		Runnable thread=new Runnable(){
			
			int count=0;
			public void run(){
				byte[] data=new byte[BUF_SIZE];
				int len=0;
				try {
					if(preReadData!=null){
//						String string=new String(preReadData,0,preReadDataLength);
//						Log.println("写预读111 "+string);
						try {
							os.write(preReadData,0,preReadDataLength);
						} catch (IOException e) {
							e.printStackTrace();
							return;
						}
//						Log.println("写预读222 ");
					}
					//Log.println("pipe发送 111 "+supserSocketId+" ");
					boolean parsed=false;
					try {
						while((len=is.read(data))>0){
							try {
								os.write(data,0,len);
							} catch (IOException e) {
								//e.printStackTrace();
								break;
							}
							
						}
					} catch (IOException e) {
						//e.printStackTrace();
					}
				}finally{
					close();
				}
			}
		};
		Route.es.execute(thread);
	}
	
	void close(){
		if(!closed){
			closed=true;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				//e1.printStackTrace();
			}

			if(socketA!=null){
				Route.es.execute(new Runnable() {
					
					public void run() {
						try {
							socketA.close();
						} catch (IOException e) {
							//e.printStackTrace();
						}
					}
					
				});
			
			}
			
			
			if(socketB!=null){

				Route.es.execute(new Runnable() {
					
					public void run() {
						try {
							socketB.close();
						} catch (IOException e) {
							//e.printStackTrace();
						}
					}
				});
				
			}
			fireClose();
		}
	}
	
	class HttpHost{
		String address;
		int port=80;
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}

	}

	HttpHost readHost(String data){
		HttpHost hh=new HttpHost();
		String host=null;
		data=data.replaceAll("\r", "");
		data=data.replaceAll(" ", "");
		String[] ls=data.split("\n");
		for(String l:ls){
			if(l.startsWith("Host:")){
				String s1=l.substring(5);
				int index2=s1.indexOf(":");
				if(index2>-1){
					int port=Integer.parseInt(s1.substring(index2+1));
					hh.setPort(port);
					s1=s1.substring(0,index2);
				}
				host=s1;
				hh.setAddress(host);
				////#MLog.println("ddd "+s1);
			}
		}
		return hh;
	}
	
	public void addListener(PipeListener listener){
		listenerList.add(listener);
	}
	
	void fireClose(){
		//Log.println("StreamPipe关闭 fireClose111 "+listenerList.size());
		for(PipeListener listener:listenerList){
			//Log.println("StreamPipe关闭 fireClose222");
			listener.pipeClose();
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getLimiteConnectTime() {
		return limiteConnectTime;
	}

	public void setLimiteConnectTime(int limiteConnectTime) {
		this.limiteConnectTime = limiteConnectTime;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Socket getSocketA() {
		return socketA;
	}

	public void setSocketA(Socket socketA) {
		this.socketA = socketA;
	}

	public Socket getSocketB() {
		return socketB;
	}

	public void setSocketB(Socket socketB) {
		this.socketB = socketB;
	}
	
	public int getSupserSocketId() {
		return supserSocketId;
	}

	public void setSupserSocketId(int supserSocketId) {
		this.supserSocketId = supserSocketId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public ConnInfo getConnInfo() {
		return connInfo;
	}

	public void setConnInfo(ConnInfo connInfo) {
		this.connInfo = connInfo;
	}
	
}
