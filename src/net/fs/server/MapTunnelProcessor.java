// Copyright (c) 2015 D1SM.net

package net.fs.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.fs.client.Pipe;
import net.fs.rudp.ConnectionProcessor;
import net.fs.rudp.ConnectionUDP;
import net.fs.rudp.Constant;
import net.fs.rudp.Route;
import net.fs.rudp.UDPInputStream;
import net.fs.rudp.UDPOutputStream;
import net.fs.utils.MLog;

import com.alibaba.fastjson.JSONObject;


public class MapTunnelProcessor implements ConnectionProcessor{

	Socket dstSocket=null;

	boolean closed=false;

	MapTunnelProcessor pc;

	ConnectionUDP conn;


	UDPInputStream  tis;

	UDPOutputStream tos;

	InputStream sis;

	OutputStream sos;

	public void process(final ConnectionUDP conn){
		this.conn=conn;
		pc=this;
		Route.es.execute(new Runnable(){
			public void run(){
				process();
			}
		});
	}


	void process(){

		tis=conn.uis;
		tos=conn.uos;

		byte[] headData;
		try {
			headData = tis.read2();
			String hs=new String(headData,"utf-8");
			JSONObject requestJSon=JSONObject.parseObject(hs);
			final int dstPort=requestJSon.getIntValue("dst_port");
			String message="";
			JSONObject responeJSon=new JSONObject();
			int code=Constant.code_failed;			
			code=Constant.code_success;
			responeJSon.put("code", code);
			responeJSon.put("message", message);
			byte[] responeData=responeJSon.toJSONString().getBytes("utf-8");
			tos.write(responeData, 0, responeData.length);
			if(code!=Constant.code_success){
				close();
				return;
			}
			dstSocket = new Socket("127.0.0.1", dstPort);
			dstSocket.setTcpNoDelay(true);
			sis=dstSocket.getInputStream();
			sos=dstSocket.getOutputStream();

			final Pipe p1=new Pipe();
			final Pipe p2=new Pipe();

			Route.es.execute(new Runnable() {

				public void run() {
					try {
						p1.pipe(sis, tos,100*1024,p2);
					}catch (Exception e) {
						//e.printStackTrace();
					}finally{
						close();
						if(p1.getReadedLength()==0){
							MLog.println("端口"+dstPort+"无返回数据");
						}
					}
				}

			});
			Route.es.execute(new Runnable() {

				public void run() {
					try {
						p2.pipe(tis,sos,100*1024*1024,conn);
					}catch (Exception e) {
						//e.printStackTrace();
					}finally{
						close();
					}
				}
			});


		} catch (Exception e2) {
			//e2.printStackTrace();
			close();
		}



	}

	void close(){
		if(!closed){
			closed=true;
			if(sis!=null){
				try {
					sis.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
			if(sos!=null){
				try {
					sos.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
			if(tos!=null){
				tos.closeStream_Local();
			}
			if(tis!=null){
				tis.closeStream_Local();
			}
			if(conn!=null){
				conn.close_local();
			}
			if(dstSocket!=null){
				try {
					dstSocket.close();
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
	}

}
