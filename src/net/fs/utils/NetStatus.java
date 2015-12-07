// Copyright (c) 2015 D1SM.net

package net.fs.utils;

import java.util.Vector;


public class NetStatus {
	
	public long uploadSum;
	public long downloadSum;
	
	Thread mainThread;
	
	int averageTime;
	
	Vector<SpeedUnit> speedList;
	SpeedUnit currentUnit;
	
	public int upSpeed=0;
	public int downSpeed=0;
	
	public NetStatus(){
		this(2);
	}
	
	public NetStatus(int averageTime){
		this.averageTime=averageTime;
		speedList=new Vector<SpeedUnit>();
		for(int i=0;i<averageTime;i++){
			SpeedUnit unit=new SpeedUnit();
			if(i==0){
				currentUnit=unit;
			}
			speedList.add(unit);
		}
		mainThread=new Thread(){
			public void run(){
				long lastTime=System.currentTimeMillis();
				while(true){
					////#MLog.println("xxxxxxxxxx");
					if(Math.abs(System.currentTimeMillis()-lastTime)>1000){
						////#MLog.println("yyyyyyyyyyy ");
						lastTime=System.currentTimeMillis();
						calcuSpeed();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					//	e.printStackTrace();
						break;
					}
				}
			}
		};
		mainThread.start();
	}
	
	public void stop(){
		mainThread.interrupt();
	}
	
	
	public int getUpSpeed() {
		return upSpeed;
	}

	public void setUpSpeed(int upSpeed) {
		this.upSpeed = upSpeed;
	}

	public int getDownSpeed() {
		return downSpeed;
	}

	public void setDownSpeed(int downSpeed) {
		this.downSpeed = downSpeed;
	}
	
	
	void calcuSpeed(){
		int ds = 0,us=0;
		for(SpeedUnit unit:speedList){
			ds+=unit.downSum;
			us+=unit.upSum;
		}
		upSpeed=(int) ((float)us/speedList.size());
		downSpeed=(int)(float)ds/speedList.size();
		
		speedList.remove(0);
		SpeedUnit unit=new SpeedUnit();
		currentUnit=unit;
		speedList.add(unit);
	}
	
	public void addDownload(int sum){
		downloadSum+=sum;
		currentUnit.addDown(sum);
	}
	
	public void addUpload(int sum){
		uploadSum+=sum;
		currentUnit.addUp(sum);
	}
	
	public void sendAvail(){
		
	}
	
	public void receiveAvail(){
		
	}
	
	public void setUpLimite(int speed){
		
	}
	
	public void setDownLimite(int speed){
		
	}
}


class SpeedUnit{
	int downSum;
	int upSum;
	SpeedUnit(){
		
	}
	
	void addUp(int n){
		upSum+=n;
	}
	
	void addDown(int n){
		downSum+=n;
	}
}

