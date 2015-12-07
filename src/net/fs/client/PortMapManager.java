// Copyright (c) 2015 D1SM.net

package net.fs.client;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import net.fs.rudp.Route;
import net.fs.utils.MLog;

public class PortMapManager {
	
	MapClient mapClient;
	
	ArrayList<MapRule> mapList=new ArrayList<MapRule>();
	
	HashMap<Integer, MapRule> mapRuleTable=new HashMap<Integer, MapRule>();
	
	String configFilePath="port_map.json";
	
	PortMapManager(MapClient mapClient){
		this.mapClient=mapClient;
		//listenPort();
		loadMapRule();
	}
	
	void addMapRule(MapRule mapRule) throws Exception{
		if(getMapRule(mapRule.name)!=null){
			throw new Exception("映射 "+mapRule.name+" 已存在,请修改名称!");
		}
		ServerSocket serverSocket=null;
		try {
			serverSocket = new ServerSocket(mapRule.getListen_port());
			listen(serverSocket);
			mapList.add(mapRule);
			mapRuleTable.put(mapRule.listen_port, mapRule);
			saveMapRule();
		} catch (IOException e2) {
			//e2.printStackTrace();
			throw new Exception("端口 "+mapRule.getListen_port()+" 已经被占用!");
		}finally{
//			if(serverSocket!=null){
//				serverSocket.close();
//			}
		}
	}
	
	void removeMapRule(String name){
		MapRule mapRule=getMapRule(name);
		if(mapRule!=null){
			mapList.remove(mapRule);
			mapRuleTable.remove(mapRule.listen_port);
			if(mapRule.serverSocket!=null){
				try {
					mapRule.serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				saveMapRule();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void updateMapRule(MapRule mapRule_origin,MapRule mapRule_new) throws Exception{
		if(getMapRule(mapRule_new.name)!=null&&!mapRule_origin.name.equals(mapRule_new.name)){
			throw new Exception("映射 "+mapRule_new.name+" 已存在,请修改名称!");
		}
		ServerSocket serverSocket=null;
		if(mapRule_origin.listen_port!=mapRule_new.listen_port){
			try {
				serverSocket = new ServerSocket(mapRule_new.getListen_port());
				listen(serverSocket);
				mapRule_origin.using=false;
				if(mapRule_origin.serverSocket!=null){
					mapRule_origin.serverSocket.close();
				}
				mapRule_origin.serverSocket=serverSocket;
				mapRuleTable.remove(mapRule_origin.listen_port);
				mapRuleTable.put(mapRule_new.listen_port, mapRule_new);
			} catch (IOException e2) {
				//e2.printStackTrace();
				throw new Exception("端口 "+mapRule_new.getListen_port()+" 已经被占用!");
			}finally{
//				if(serverSocket!=null){
//					serverSocket.close();
//				}
			}
		}
		mapRule_origin.name=mapRule_new.name;
		mapRule_origin.listen_port=mapRule_new.listen_port;
		mapRule_origin.dst_port=mapRule_new.dst_port;
		saveMapRule();
		
	}
	
	void saveMapRule() throws Exception{
		JSONObject json=new JSONObject();
		JSONArray json_map_list=new JSONArray();
		json.put("map_list", json_map_list);
		if(mapList.size()==0){

		}
		for(MapRule r:mapList){
			JSONObject json_rule=new JSONObject();
			json_rule.put("name", r.name);
			json_rule.put("listen_port", r.listen_port);
			json_rule.put("dst_port", r.dst_port);
			json_map_list.add(json_rule);
		}
		try {
			saveFile(json.toJSONString().getBytes("utf-8"), configFilePath);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("保存失败!");
		}
	}
	
	void loadMapRule(){
		String content;
		JSONObject json=null;
		try {
			content = readFileUtf8(configFilePath);
			json=JSONObject.parseObject(content);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if(json!=null&&json.containsKey("map_list")){
			JSONArray json_map_list=json.getJSONArray("map_list");
			for(int i=0;i<json_map_list.size();i++){
				JSONObject json_rule=(JSONObject) json_map_list.get(i);
				MapRule mapRule=new MapRule();
				mapRule.name=json_rule.getString("name");
				mapRule.listen_port=json_rule.getIntValue("listen_port");
				mapRule.dst_port=json_rule.getIntValue("dst_port");
				mapList.add(mapRule);
				ServerSocket serverSocket;
				try {
					serverSocket = new ServerSocket(mapRule.getListen_port());
					listen(serverSocket);
					mapRule.serverSocket=serverSocket;
				} catch (IOException e) {
					mapRule.using=true;
					e.printStackTrace();
				}
				mapRuleTable.put(mapRule.listen_port, mapRule);
			}
		}

	}
	
	MapRule getMapRule(String name){
		MapRule rule=null;
		for(MapRule r:mapList){
			if(r.getName().equals(name)){
				rule=r;
				break;
			}
		}
		return rule;
	}
	
	public ArrayList<MapRule> getMapList() {
		return mapList;
	}

	public void setMapList(ArrayList<MapRule> mapList) {
		this.mapList = mapList;
	}

	void listen(final ServerSocket serverSocket){
		Route.es.execute(new Runnable() {

			@Override
			public void run() {
				while(true){
					try {
						final Socket socket=serverSocket.accept();
						Route.es.execute(new Runnable() {
							
							@Override
							public void run() {
								int listenPort=serverSocket.getLocalPort();
								MapRule mapRule=mapRuleTable.get(listenPort);
								if(mapRule!=null){
									Route route=null;
									if(mapClient.isUseTcp()){
										route=mapClient.route_tcp;
									}else {
										route=mapClient.route_udp;
									}
									PortMapProcess process=new PortMapProcess(mapClient,route, socket,mapClient.serverAddress,mapClient.serverPort,null, 
											null,mapRule.dst_port);
								}
							}
							
						});

					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			}
		});
	}
	
	void saveFile(byte[] data,String path) throws Exception{
		FileOutputStream fos=null;
		try {
			fos=new FileOutputStream(path);
			fos.write(data);
		} catch (Exception e) {
			throw e;
		} finally {
			if(fos!=null){
				fos.close();
			}
		}
	}
	
	public static String readFileUtf8(String path) throws Exception{
		String str=null;
		FileInputStream fis=null;
		DataInputStream dis=null;
		try {
			File file=new File(path);

			int length=(int) file.length();
			byte[] data=new byte[length];

			fis=new FileInputStream(file);
			dis=new DataInputStream(fis);
			dis.readFully(data);
			str=new String(data,"utf-8");

		} catch (Exception e) {
			//e.printStackTrace();
			throw e;
		}finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(dis!=null){
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return str;
	}
}
