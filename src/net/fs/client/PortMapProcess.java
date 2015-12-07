// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import net.fs.rudp.ClientProcessorInterface;
import net.fs.rudp.ConnectionUDP;
import net.fs.rudp.Constant;
import net.fs.rudp.Route;
import net.fs.rudp.UDPInputStream;
import net.fs.rudp.UDPOutputStream;

import com.alibaba.fastjson.JSONObject;

public class PortMapProcess implements ClientProcessorInterface{

	Random ran=new Random();

	UDPInputStream  tis;

	UDPOutputStream tos;

	String serverAddress="";

	int serverPort;

	ConnectionUDP conn;

	MapClient mapClient;

	public Socket srcSocket,dstSocket;

	String password_proxy_md5;

	DataInputStream srcIs=null;
	DataOutputStream srcOs=null;

	boolean closed=false;
	boolean success=false;

	public PortMapProcess(MapClient mapClient,Route route,final Socket srcSocket,String serverAddress2,int serverPort2,String password_proxy_md5,
			String dstAddress,int dstPort){
		this.mapClient=mapClient;
		this.serverAddress=serverAddress2;
		this.serverPort=serverPort2;

		this.srcSocket=srcSocket;
		this.password_proxy_md5=password_proxy_md5;

		try {
			srcIs = new DataInputStream(srcSocket.getInputStream());
			srcOs=new DataOutputStream(srcSocket.getOutputStream());
			conn = route.getConnection(serverAddress, serverPort,null);
			tis=conn.uis;
			tos=conn.uos;

			JSONObject requestJson=new JSONObject();
			requestJson.put("dst_address", dstAddress);
			requestJson.put("dst_port", dstPort);
			requestJson.put("password_proxy_md5", password_proxy_md5);
			byte[] requestData=requestJson.toJSONString().getBytes("utf-8");
			tos.write(requestData, 0, requestData.length);


			final Pipe p1=new Pipe();
			final Pipe p2=new Pipe();


			byte[] responeData=tis.read2();

			String hs=new String(responeData,"utf-8");
			JSONObject responeJSon=JSONObject.parseObject(hs);
			int code=responeJSon.getIntValue("code");
			String message=responeJSon.getString("message");
			String uimessage="";
			if(code==Constant.code_success){

				Route.es.execute(new Runnable() {

					@Override
					public void run() {
						try {
							p2.pipe(tis, srcOs,1024*1024*1024,null);
						}catch (Exception e) {
							e.printStackTrace();
						}finally{
							close();
						}
					}

				});

				Route.es.execute(new Runnable() {

					@Override
					public void run() {
						try {
							p1.pipe(srcIs, tos,200*1024,p2);
						} catch (Exception e) {
							//e.printStackTrace();
						}finally{
							close();
						}
					}

				});
				success=true;
				uimessage=("连接服务器成功");
			}else {
				close();
				uimessage="连接服务器失败,"+message;
			}
			if(ClientUI.ui!=null){
				ClientUI.ui.setMessage(uimessage);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	void close(){
		if(!closed){
			closed=true;
			if(srcIs!=null){
				try {
					srcIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(srcOs!=null){
				try {
					srcOs.close();
				} catch (IOException e) {
					e.printStackTrace();
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
			if(srcSocket!=null){
				try {
					srcSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mapClient.onProcessClose(this);

		}
	}

	@Override
	public void onMapClientClose() {
		try {
			srcSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
