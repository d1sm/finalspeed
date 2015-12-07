// Copyright (c) 2015 D1SM.net

package net.fs.cap;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.fs.utils.MLog;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.EthernetPacket.EthernetHeader;
import org.pcap4j.packet.IpV4Packet.IpV4Header;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.TcpPacket.TcpHeader;
import org.pcap4j.util.MacAddress;


public class TCPTun {

	HashMap<Integer,TcpPacket>  sendedTable_server=new HashMap<Integer,TcpPacket> ();
	HashMap<Integer,TcpPacket>  sendedTable_history_server=new HashMap<Integer,TcpPacket> ();

	int clientSequence=Integer.MIN_VALUE;

	static Random random=new Random();

	PcapHandle sendHandle;

	HashSet<Short> selfAckTable=new HashSet<Short>();

	HashMap<Integer, SendRecord> sendrecordTable=new HashMap<Integer, SendRecord>();

	MacAddress dstMacaAddress;

	int sequenceNum=-1;

	Thread sendThread;

	boolean sended=false;

	Packet basePacket_server;

	short baseIdent=100;

	IPacket dst_readed_packet,last_send_packet;

	int presend_server;

	ArrayList<IPacket> packetList=new ArrayList<IPacket>();

	HashMap<Integer, IPacket> packetTable_l=new HashMap<Integer, IPacket>();

	HashMap<Integer, IPacket> packetTable=new HashMap<Integer, IPacket>();

	ArrayList<IPacket> unacked_list=new ArrayList<IPacket>();

	Object syn_packetList=new Object();

	int max_client_ack=Integer.MIN_VALUE;

	int sendIndex=0;

	long lasSetDelayTime=0;

	long lastDelay=300;

	Object syn_delay=new Object();

	Thread resendScanThread;

	boolean connectReady=false;

	boolean preDataReady=false;
	
	CapEnv capEnv;
	
	public Inet4Address remoteAddress;
	public short remotePort;
	int remoteStartSequence;
	int remoteSequence;
	int remoteIdent;
	int remoteSequence_max;

	Inet4Address localAddress;
	short localPort;
	int localStartSequence=random.nextInt();
	int localSequence;
	int localIdent=random.nextInt(Short.MAX_VALUE-100);
	
	Object syn_send_data=new Object();
		
	long lastSendAckTime;
	
	long lastReceiveDataTime;
	
	long createTime=System.currentTimeMillis();;
	
	String key;
	
	Object syn_ident=new Object();
	
	//客户端发起
	TCPTun(CapEnv capEnv,
			Inet4Address serverAddress,short serverPort,
			MacAddress srcAddress_mac,MacAddress dstAddrress_mac){
		this.capEnv=capEnv;
		sendHandle=capEnv.sendHandle;
		this.remoteAddress=serverAddress;
		this.remotePort=serverPort;
		localAddress=capEnv.local_ipv4;
		localPort=(short)(random.nextInt(64*1024-1-10000)+10000);
		Packet syncPacket=null;
		try {
			syncPacket = PacketUtils.createSync(srcAddress_mac, dstAddrress_mac, localAddress, localPort,serverAddress, serverPort, localStartSequence,getIdent());
			try {
				sendHandle.sendPacket(syncPacket);
				localSequence=localStartSequence+1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		MLog.println("发送第一次握手 "+" ident "+localIdent);
		MLog.println(""+syncPacket);
		
	}

	//服务端接收
	TCPTun(CapEnv capServerEnv,
			Inet4Address remoteAddress,short remotePort){
		this.capEnv=capServerEnv;
		this.remoteAddress=remoteAddress;
		this.remotePort=remotePort;
		sendHandle=capEnv.sendHandle;
		localPort=capServerEnv.listenPort;
		localAddress=capEnv.local_ipv4;
	}

	void init_client(Inet4Address clientAddress,int clientPort,
			Inet4Address serverAddress,int serverPort,
			int client_start_sequence){

	}

	void init_server(Inet4Address clientAddress,int clientPort,
			Inet4Address serverAddress,int serverPort,
			int client_start_sequence,int server_start_sequence){

	}

	public void process_server(final Packet packet,EthernetHeader ethernetHeader,IpV4Header ipV4Header,TcpPacket tcpPacket,boolean client){
		TcpHeader tcpHeader=tcpPacket.getHeader();
		
		if(!preDataReady){
			if(!connectReady){
				//第一次握手
				dstMacaAddress=ethernetHeader.getSrcAddr();
				if(tcpHeader.getSyn()&&!tcpHeader.getAck()){
					remoteStartSequence=tcpHeader.getSequenceNumber();
					remoteSequence=remoteStartSequence+1;
					remoteSequence_max=remoteSequence;
					MLog.println("接收第一次握手 "+remoteAddress.getHostAddress()+":"+remotePort+"->"+localAddress.getHostAddress()+":"+localPort+" ident "+ipV4Header.getIdentification());
					MLog.println(""+packet);
					Packet responePacket=PacketUtils.createSyncAck(
							capEnv.local_mac,
							capEnv.gateway_mac,
							localAddress,(short)localPort,
							ipV4Header.getSrcAddr(),tcpHeader.getSrcPort().value(),
							tcpHeader.getSequenceNumber()+1,localStartSequence,(short)0
							);
					try {
						sendHandle.sendPacket(responePacket);
					} catch (Exception e) {
						e.printStackTrace();
					}
					localSequence=localStartSequence+1;
					MLog.println("发送第二次握手 "+capEnv.local_mac+"->"+capEnv.gateway_mac+" "+localAddress+"->"+" ident "+0);

					MLog.println(""+responePacket);
				}

				if(!tcpHeader.getSyn()&&tcpHeader.getAck()){
					if(tcpPacket.getPayload()==null){
						//第三次握手,客户端确认
						if(tcpHeader.getAcknowledgmentNumber()==localSequence){
							MLog.println("接收第三次握手 "+" ident "+ipV4Header.getIdentification());
							MLog.println(packet+"");
							Thread t1=new Thread(){
								public void run(){
									//startSend(basePacket_server,syc_sequence_client+1);
								}
							};
							//t1.start();
							connectReady=true;
						}
					}
					//MLog.println("客户端响应preview\n "+packet);
					//MLog.println("request "+tcp.ack());
					sendedTable_server.remove(tcpHeader.getAcknowledgmentNumber());
					boolean selfAck=selfAckTable.contains(ipV4Header.getIdentification());
					//MLog.println("客户端确认 "+"selfack "+selfAck+" id "+ipV4Header.getIdentification()+" ack_sequence "+tcpHeader.getAcknowledgmentNumberAsLong()+" "+sendedTable_server.size()+"ppppppp "+tcpHeader);
				}
				
			}else {
				if(tcpPacket.getPayload()!=null){
					preDataReady=true;
					onReceiveDataPacket( tcpPacket, tcpHeader, ipV4Header );
					byte[] sim=getSimResponeHead();
					sendData(sim);
				}
			}
		}else {
			if(tcpPacket.getPayload()!=null){
				onReceiveDataPacket( tcpPacket, tcpHeader, ipV4Header );
				TunData td=new TunData();
				td.tun=this;
				td.data=tcpPacket.getPayload().getRawData();
				capEnv.vDatagramSocket.onReceinveFromTun(td);
			}
		}
		if(tcpHeader.getRst()){
			MLog.println("reset packet "+ipV4Header.getIdentification()+" "+tcpHeader.getSequenceNumber()+" "+remoteAddress.getHostAddress()+":"+remotePort+"->"+localAddress.getHostAddress()+":"+localPort+" "+" ident "+ipV4Header.getIdentification());
		}

	}

	public void process_client(CapEnv capEnv,final Packet packet,EthernetHeader ethernetHeader,IpV4Header ipV4Header,TcpPacket tcpPacket,boolean client){

		TcpHeader tcpHeader=tcpPacket.getHeader();
		byte[] payload=null;
		if(tcpPacket.getPayload()!=null){
			payload=tcpPacket.getPayload().getRawData();
		}

		if(!preDataReady){
			if(!connectReady){
				if(tcpHeader.getAck()&&tcpHeader.getSyn()){
					if(tcpHeader.getAcknowledgmentNumber()==(localStartSequence+1)){
						MLog.println("接收第二次握手 "+" ident "+ipV4Header.getIdentification());
						MLog.println(""+packet);
						remoteStartSequence=tcpHeader.getSequenceNumber();
						remoteSequence=remoteStartSequence+1;
						remoteSequence_max=remoteSequence;
						Packet p3=PacketUtils.createAck(capEnv.local_mac, capEnv.gateway_mac, capEnv.local_ipv4, localPort, remoteAddress, remotePort, remoteSequence , localSequence,getIdent());
						try {
							sendHandle.sendPacket(p3);
							MLog.println("发送第三次握手 "+" ident "+localIdent);
							MLog.println(""+p3);
							connectReady=true;
							
							byte[] sim=getSimRequestHead(remotePort);
							sendData(sim);
							MLog.println("发送请求 "+" ident "+localIdent);
						} catch (PcapNativeException e) {
							e.printStackTrace();
						} catch (NotOpenException e) {
							e.printStackTrace();
						}
					}
				}
			}else {
				if(tcpPacket.getPayload()!=null){
					preDataReady=true;
					onReceiveDataPacket( tcpPacket, tcpHeader, ipV4Header );
					MLog.println("接收响应 "+" ident "+ipV4Header.getIdentification());
				}
			}

		}else {
			if(tcpPacket.getPayload()!=null){
				//MLog.println("客户端正式接收数据 "+capClientEnv.vDatagramSocket);
				onReceiveDataPacket( tcpPacket, tcpHeader, ipV4Header );
				TunData td=new TunData();
				td.tun=this;
				td.data=tcpPacket.getPayload().getRawData();
				capEnv.vDatagramSocket.
				onReceinveFromTun(td);
			}
		}
		if(tcpHeader.getRst()){
			MLog.println("reset packet "+ipV4Header.getIdentification()+" "+tcpHeader.getSequenceNumber()+" "+remoteAddress.getHostAddress()+":"+remotePort+"->"+localAddress.getHostAddress()+":"+localPort);
		}

	}
	
	void onReceiveDataPacket(TcpPacket tcpPacket,TcpHeader tcpHeader,IpV4Header ipV4Header ){
		if(System.currentTimeMillis()-lastSendAckTime>1000){
			int rs=tcpHeader.getSequenceNumber()+tcpPacket.getPayload().getRawData().length;
			if(rs>remoteSequence_max){
				remoteSequence_max=rs;
			}
			Packet ackPacket=PacketUtils.createAck(
					capEnv.local_mac,
					capEnv.gateway_mac,
					localAddress,(short)localPort,
					ipV4Header.getSrcAddr(),tcpHeader.getSrcPort().value(),
					remoteSequence_max, localSequence,getIdent());
			try {
				sendHandle.sendPacket(ackPacket);
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			lastSendAckTime=System.currentTimeMillis();
			lastReceiveDataTime=System.currentTimeMillis();
		}
	}
	
	void sendData(byte[] data){
		Packet dataPacket=PacketUtils.createDataPacket(capEnv.local_mac,
							capEnv.gateway_mac,
							localAddress,localPort,
							remoteAddress,remotePort,
							localSequence,remoteSequence_max, data, (short) getIdent());
		synchronized (syn_send_data) {
			try {
				sendHandle.sendPacket(dataPacket);
				localSequence+=data.length;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	short getIdent(){
		synchronized (syn_ident) {
			localIdent++;
			if(localIdent>=Short.MAX_VALUE){
				localIdent=0;
			}
		}
		return (short) localIdent;
	}
	
	public static byte[] getSimResponeHead(){
		StringBuffer sb=new StringBuffer();
		
		sb.append("HTTP/1.1 200 OK"+"\r\n");
		sb.append("Server: Apache/2.2.15 (CentOS)"+"\r\n");
		sb.append("Accept-Ranges: bytes"+"\r\n");
		sb.append("Content-Length: "+(Math.abs(random.nextInt()))+"\r\n");
		sb.append("Connection: Keep-Alive"+"\r\n");
		sb.append("Content-Type: application/octet-stream"+"\r\n");
		sb.append("\r\n");
		
		String simRequest=sb.toString();
		byte[] simData=simRequest.getBytes();
		return simData;
	}
	
	public static byte[] getSimRequestHead(int port){
		StringBuffer sb=new StringBuffer();
		String domainName=getRandomString(5+random.nextInt(10))+".com";				
		sb.append("GET /"+getRandomString(8+random.nextInt(10))+"."+getRandomString(2+random.nextInt(5))+" HTTP/1.1"+"\r\n");
		sb.append("Accept: application/x-ms-application, image/jpeg, application/xaml+xml, image/gif, image/pjpeg, application/x-ms-xbap, */*"+"\r\n");
		sb.append("Accept-Language: zh-CN"+"\r\n");
		sb.append("Accept-Encoding: gzip, deflate"+"\r\n");
		sb.append("User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0"+"\r\n");
		sb.append("Host: "+domainName+"\r\n");
		sb.append("Connection: Keep-Alive"+"\r\n");
		sb.append("\r\n");
		String simRequest=sb.toString();
		byte[] simData=simRequest.getBytes();
		return simData;
	}
	
	public static String getRandomString(int length) { //length表示生成字符串的长度  
	    String base = "abcdefghkmnopqrstuvwxyz";     
	    Random random = new Random();     
	    StringBuffer sb = new StringBuffer();     
	    for (int i = 0; i < length; i++) {     
	        int number = random.nextInt(base.length());     
	        sb.append(base.charAt(number));     
	    }
	    return sb.toString();
	 } 

	public InetAddress getSourcrAddress() {
		return localAddress;
	}

	public int getSourcePort() {
		return localPort;
	}

	public void setSourcePort(short sourcePort) {
		this.localPort = sourcePort;
	}

	public boolean isConnectReady() {
		return connectReady;
	}

	public void setConnectReady(boolean connectReady) {
		this.connectReady = connectReady;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
