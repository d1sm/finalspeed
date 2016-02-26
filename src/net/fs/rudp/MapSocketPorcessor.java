// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.fs.utils.MLog;


public class MapSocketPorcessor implements PipeListener{
	
	
	Socket srcSocket;
	Socket dstSocket;

	public DataInputStream srcIs;
	public DataOutputStream dstOs;
	public DataInputStream dstIs;
	public DataOutputStream srcOs;

	Object srcReadOb=new Object();
	Object dstReadOb=new Object();
	Object dstWriteOb=new Object();
	Object srcWriteOb=new Object();

	ArrayList<byte[]> srcReadBuffer=new ArrayList<byte[]>();
	ArrayList<byte[]> dstReadBuffer=new ArrayList<byte[]>();

	Thread srcReadThread;
	Thread dstWriteThread;
	Thread srcWriteThread;
	Thread dstReadThread;
	byte[] srcPreRead=new byte[0];

	int maxDstRead=1;
	int maxSrcRead=1;

	boolean isSuperSocket=false;

	boolean dstReadComplete=false;

	boolean srcReadComplete=false;

	boolean srcWriteComplete=false;

	boolean dstWriteComplete=false;

	boolean dstClosed=false;

	boolean srcClosed=false;

	String st="           ";


	String ss="";

	static int n=0;

	Random ran=new Random();
	
	long id;
	
	static int m=0,a,b;
	
	boolean closed=false;
	
	long lastActiveTime=System.currentTimeMillis();
		
	//static HashMap<Long,MapSocketPorcessor> procTable=new HashMap<Long,MapSocketPorcessor>();
	
	static ExecutorService es;
	
	MapSocketPorcessor mp;
	
	Socket socketA,socketB;
	
	int supserSocketId=-1;
	
	String requestHost=null;
	
	static {
		es=Executors.newCachedThreadPool();
	}
	
	public MapSocketPorcessor(){
		
	}
	
	public void setDstSocket(Socket dstSocket){
		this.dstSocket=dstSocket;
	}
	
	public void setId(long id){
		this.id=id;
	}
	
	public boolean isClosed(){
		return closed;
	}

	public long getLastActiveTime(){
		return lastActiveTime;
	}
	
	void active(){
		lastActiveTime=System.currentTimeMillis();
	}
	
	public long getId(){
		return id;
	}

	public void closeAll(){
		closeAll(true);
	}
	
	public void closeAll(boolean closeDst){
		//procTable.remove(id);
		//Log.println("closeAll AAAAAAAAA");
		if(!closed){
			//Log.println("closeAll BBBBBBBBBB");
			closed=true;
			//#MLog.println("MapSocketPorcessor Close");
			try {
				srcSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(closeDst){
				try {
					dstSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void tryClose(){
		closeAll();
	}

	public void start(){
		Runnable t=new Runnable(){
			public void run(){
				try {
					ConnInfo connInfo=new ConnInfo();
					StreamPipe p1=new StreamPipe(connInfo,srcIs,dstOs,10*1024,1000*1024);
					StreamPipe p2=new StreamPipe(connInfo,dstIs,srcOs,10*1024,1000*1024);
					p1.setType(StreamPipe.type_request);
					//p1.addListener(mp);
					//p2.addListener(mp);
					p1.setSocketA(socketA);
					p1.setSocketB(socketB);
					p2.setType(StreamPipe.type_respone);
					p2.setSocketA(socketA);
					p2.setSocketB(socketB);
					p1.setSupserSocketId(supserSocketId);
					p2.setSupserSocketId(supserSocketId);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		es.execute(t);
	}

	public void pipeClose() {
		//closeAll();
	
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

	public String getRequestHost() {
		return requestHost;
	}

	public void setRequestHost(String requestHost) {
		this.requestHost = requestHost;
	}

}
