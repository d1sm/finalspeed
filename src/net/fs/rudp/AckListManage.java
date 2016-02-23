// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

import java.util.HashMap;
import java.util.Iterator;

public class AckListManage implements Runnable{
	Thread mainThread;
	HashMap<Integer, AckListTask> taskTable;
	public AckListManage(){
		taskTable=new HashMap<Integer, AckListTask>();
		mainThread=new Thread(this);
		mainThread.start();
	}
	
	synchronized void addAck(ConnectionUDP conn,int sequence){
		if(!taskTable.containsKey(conn.connectId)){
			AckListTask at=new AckListTask(conn);
			taskTable.put(conn.connectId, at);
		}
		AckListTask at=taskTable.get(conn.connectId);
		at.addAck(sequence);
	}
	
	synchronized void addLastRead(ConnectionUDP conn){
		if(!taskTable.containsKey(conn.connectId)){
			AckListTask at=new AckListTask(conn);
			taskTable.put(conn.connectId, at);
		}
	}
	
	public void run(){
		while(true){
			synchronized (this){
				Iterator<Integer> it=taskTable.keySet().iterator();
				while(it.hasNext()){
					int id=it.next();
					AckListTask at=taskTable.get(id);
					at.run();
				}
				taskTable.clear();
				taskTable=null;
				taskTable=new HashMap<Integer, AckListTask>();
			}
			
			try {
				Thread.sleep(RUDPConfig.ackListDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
