// Copyright (c) 2015 D1SM.net

package net.fs.cap;

import java.net.Inet4Address;
import java.util.ArrayList;

import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Rfc1349Tos;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpMaximumSegmentSizeOption;
import org.pcap4j.packet.TcpNoOperationOption;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.TcpPacket.TcpOption;
import org.pcap4j.packet.TcpSackPermittedOption;
import org.pcap4j.packet.TcpWindowScaleOption;
import org.pcap4j.packet.UnknownPacket;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;
import org.pcap4j.packet.namednumber.TcpPort;
import org.pcap4j.util.MacAddress;

import net.fs.utils.ByteShortConvert;

public class PacketUtils {
	
	static byte ttl=64;
	
	static short mtu=1440;
	
	static byte shiftCount=6;
	
	static short window=(short) (64*1024-1);
	
	public static boolean ppp=false;
	
	public static byte[] pppHead_static={0x11,0x00,0x44,0x44,0x00,0x44,0x00,0x21};
	
	
	public static Packet buildIpV4(
			MacAddress srcAddress_mac,
			MacAddress dstAddrress_mac,
			IpV4Packet.Builder builder_ipv4){

		org.pcap4j.packet.Packet.Builder builder=null;
		EtherType etherType=null;
		Packet p=null;
		if(ppp){
			etherType=EtherType.PPPOE_SESSION_STAGE;
			
			UnknownPacket.Builder pppBuilder=new UnknownPacket.Builder();
			byte[] ipData=builder_ipv4.build().getRawData();
			
			byte[] lenb=new byte[2];
			ByteShortConvert.toByteArray((short) (ipData.length+2), lenb, 0);
			
			byte[] pppHead=new byte[8];
			System.arraycopy(pppHead_static, 0, pppHead, 0, pppHead.length);
			System.arraycopy(lenb, 0, pppHead, 4, 2);
			
			byte[] newData=new byte[pppHead.length+ipData.length];
			System.arraycopy(pppHead, 0, newData, 0, pppHead.length);
			System.arraycopy(ipData, 0, newData, 8, ipData.length);
			pppBuilder.rawData(newData);
			
			builder=pppBuilder;
		}else {
			etherType=EtherType.IPV4;
			builder=builder_ipv4;
		}
		
		EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
		etherBuilder.dstAddr(dstAddrress_mac)
		.srcAddr(srcAddress_mac)
		.type(etherType)
		.payloadBuilder(builder)
		.paddingAtBuild(true);
		
		p = etherBuilder.build();
		
		return p;
	}
	
	static Packet createDataPacket(
			MacAddress srcAddress_mac,
			MacAddress dstAddrress_mac,
			Inet4Address srcAddress,short srcPort,
			Inet4Address dstAddress,short dstPort,
			int sequence,int ack, byte[] data,short ident){
		Packet p=null;

		TcpPacket.Builder builder_tcp=new TcpPacket.Builder();
		builder_tcp.payloadBuilder(new UnknownPacket.Builder().rawData(data));
		builder_tcp.correctChecksumAtBuild(true);
		builder_tcp.correctLengthAtBuild(true);
		builder_tcp.paddingAtBuild(true);
		builder_tcp.ack(true);
		builder_tcp.acknowledgmentNumber(ack);
		//builder_tcp.checksum(tcpHeader.getChecksum());
		//builder_tcp.dataOffset((byte)8);
		builder_tcp.dstAddr(dstAddress);
		builder_tcp.dstPort(new TcpPort( dstPort,""));
		builder_tcp.fin(false);
		//builder_tcp.options(tcpHeader.getOptions());
		//builder_tcp.padding(tcpHeader.getPadding());
		builder_tcp.psh(false);
		builder_tcp.reserved((byte) 0);
		builder_tcp.rst(false);
		builder_tcp.sequenceNumber(sequence);
		builder_tcp.srcAddr(srcAddress);
		builder_tcp.srcPort(new TcpPort( srcPort,""));
		builder_tcp.syn(false);
		builder_tcp.urg(false);
		//builder_tcp.urgentPointer(tcpHeader.getUrgentPointer());
		builder_tcp.window( window);

		IpV4Packet.Builder builder_ipv4=new IpV4Packet.Builder();
		builder_ipv4.correctChecksumAtBuild(true);
		builder_ipv4.correctLengthAtBuild(true);
		builder_ipv4.dontFragmentFlag(true);
		builder_ipv4.paddingAtBuild(true);
		builder_ipv4.dstAddr(dstAddress);
		builder_ipv4.fragmentOffset( (short)0);
		//builder_ipv4.headerChecksum(ipV4Header.getHeaderChecksum());
		//short identification= Math.abs(random.nextInt(Short.MAX_VALUE));
		//identification=ident;
		builder_ipv4.identification(ident);
		builder_ipv4.ihl((byte) 5);
		builder_ipv4.moreFragmentFlag(false);
		//builder_ipv4.options(ipV4Header.getOptions());
		//builder_ipv4.padding(ipV4Header.getPadding());

		builder_ipv4.protocol(IpNumber.TCP);
		//builder_ipv4.reservedFlag(ipV4Header.getReservedFlag());
		builder_ipv4.srcAddr(srcAddress);
		builder_ipv4.tos(IpV4Rfc1349Tos.newInstance((byte) 0));
		//builder_ipv4.totalLength( 52);
		builder_ipv4.ttl(ttl);
		builder_ipv4.version(IpVersion.IPV4);
		builder_ipv4.payloadBuilder(builder_tcp);

		p = buildIpV4(srcAddress_mac,dstAddrress_mac,builder_ipv4);

		return p;
	}
	
	static Packet createAck(
			MacAddress srcAddress_mac,
			MacAddress dstAddrress_mac,
			Inet4Address srcAddress,short srcPort,
			Inet4Address dstAddress,short dstPort,
			int ack_sequence,int sequence,short ident){

		TcpPacket.Builder builder_tcp=new TcpPacket.Builder();
		//builder_tcp.payloadBuilder(new UnknownPacket.Builder().rawData(new byte[0]));
		builder_tcp.correctChecksumAtBuild(true);
		builder_tcp.correctLengthAtBuild(true);
		builder_tcp.paddingAtBuild(true);
		builder_tcp.ack(true);
		builder_tcp.acknowledgmentNumber(ack_sequence);
		//builder_tcp.checksum(tcpHeader.getChecksum());
		//builder_tcp.dataOffset((byte) 8);
		builder_tcp.dstAddr(dstAddress);
		builder_tcp.dstPort(new TcpPort( dstPort,""));
		//builder_tcp.fin(tcpHeader.getFin());

		builder_tcp.psh(false);
		builder_tcp.reserved((byte) 0);
		builder_tcp.rst(false);
		builder_tcp.sequenceNumber(sequence);
		builder_tcp.srcAddr(srcAddress);
		builder_tcp.srcPort(new TcpPort( srcPort,""));
		builder_tcp.syn(false);
		builder_tcp.urg(false);
		//builder_tcp.urgentPointer(tcpHeader.getUrgentPointer());
		builder_tcp.window( window);

		IpV4Packet.Builder builder_ipv4=new IpV4Packet.Builder();
		builder_ipv4.correctChecksumAtBuild(true);
		builder_ipv4.correctLengthAtBuild(true);
		builder_ipv4.paddingAtBuild(true);
		builder_ipv4.dstAddr(dstAddress);
		builder_ipv4.dontFragmentFlag(true);
		builder_ipv4.fragmentOffset( (short) 0);
		//builder_ipv4.headerChecksum(ipV4Header.getHeaderChecksum());
		//short identification= Math.abs(random.nextInt(Short.MAX_VALUE));
		builder_ipv4.identification(ident);
		builder_ipv4.ihl((byte) 5);
		//builder_ipv4.moreFragmentFlag(ipV4Header.getMoreFragmentFlag());
		//builder_ipv4.options(ipV4Header.getOptions());
		//builder_ipv4.padding(ipV4Header.getPadding());

		builder_ipv4.protocol(IpNumber.TCP);
		//		builder_ipv4.reservedFlag(ipV4Header.getReservedFlag());
		builder_ipv4.srcAddr(srcAddress);
		builder_ipv4.tos(IpV4Rfc1349Tos.newInstance((byte) 0));
		//builder_ipv4.totalLength( 52);
		builder_ipv4.ttl(ttl);
		builder_ipv4.version(IpVersion.IPV4);
		builder_ipv4.payloadBuilder(builder_tcp);
		//
		
		Packet p = buildIpV4(srcAddress_mac,dstAddrress_mac,builder_ipv4);
		//System.out.println("自定义确认 "+" identification "+identification+" ack_sequence "+ack_sequence+" # "+tcpPacket.getHeader());
		return p;

	}
	

	static Packet createSyncAck(
			MacAddress srcAddress_mac,
			MacAddress dstAddrress_mac,
			Inet4Address srcAddress,short srcPort,
			Inet4Address dstAddress,short dstPort,
			int ack_sequence,int sequence,short ident){

		TcpPacket.Builder builder_tcp=new TcpPacket.Builder();
		//builder_tcp.payloadBuilder(new UnknownPacket.Builder().rawData(new byte[0]));
		builder_tcp.correctChecksumAtBuild(true);
		builder_tcp.correctLengthAtBuild(true);
		builder_tcp.paddingAtBuild(true);
		builder_tcp.ack(true);
		builder_tcp.acknowledgmentNumber(ack_sequence);
		//builder_tcp.checksum(tcpHeader.getChecksum());
		//builder_tcp.dataOffset((byte) 8);
		builder_tcp.dstAddr(dstAddress);
		builder_tcp.dstPort(new TcpPort(dstPort,""));
		//builder_tcp.fin(tcpHeader.getFin());


		ArrayList<TcpOption> tcp_options=new ArrayList<TcpOption>();
		
		TcpNoOperationOption nop=TcpNoOperationOption.getInstance();

		TcpMaximumSegmentSizeOption seg_option=new TcpMaximumSegmentSizeOption.Builder().maxSegSize(mtu).correctLengthAtBuild(true).build();
		tcp_options.add(seg_option);
		
		tcp_options.add(nop);
		tcp_options.add(nop);

		TcpSackPermittedOption sack_permit_option=TcpSackPermittedOption.getInstance();
		tcp_options.add(sack_permit_option);
		
		tcp_options.add(nop);

		TcpWindowScaleOption win_option=new TcpWindowScaleOption.Builder().shiftCount(shiftCount).correctLengthAtBuild(true).build();
		tcp_options.add(win_option);

		builder_tcp.options(tcp_options);

		//builder_tcp.padding(tcpHeader.getPadding());
		builder_tcp.psh(false);
		builder_tcp.reserved((byte) 0);
		builder_tcp.rst(false);
		builder_tcp.sequenceNumber(sequence);
		builder_tcp.srcAddr(srcAddress);
		builder_tcp.srcPort(new TcpPort(srcPort,""));
		builder_tcp.syn(true);
		builder_tcp.urg(false);
		//builder_tcp.urgentPointer(tcpHeader.getUrgentPointer());
		builder_tcp.window( window);

		IpV4Packet.Builder builder_ipv4=new IpV4Packet.Builder();
		builder_ipv4.correctChecksumAtBuild(true);
		builder_ipv4.correctLengthAtBuild(true);
		builder_ipv4.paddingAtBuild(true);
		builder_ipv4.dstAddr(dstAddress);
		builder_ipv4.dontFragmentFlag(true);
		builder_ipv4.fragmentOffset((short)0);
		//builder_ipv4.headerChecksum(ipV4Header.getHeaderChecksum());
	//	short identification= Math.abs(random.nextInt(Short.MAX_VALUE));
		builder_ipv4.identification(ident);
		builder_ipv4.ihl((byte) 5);
		//builder_ipv4.moreFragmentFlag(ipV4Header.getMoreFragmentFlag());
		//builder_ipv4.options(ipV4Header.getOptions());
		//builder_ipv4.padding(ipV4Header.getPadding());

		builder_ipv4.protocol(IpNumber.TCP);
		//		builder_ipv4.reservedFlag(ipV4Header.getReservedFlag());
		builder_ipv4.srcAddr(srcAddress);
		builder_ipv4.tos(IpV4Rfc1349Tos.newInstance((byte) 0));
		//builder_ipv4.totalLength( 52);
		builder_ipv4.ttl(ttl);
		builder_ipv4.version(IpVersion.IPV4);
		builder_ipv4.payloadBuilder(builder_tcp);
		//
		Packet p = buildIpV4(srcAddress_mac,dstAddrress_mac,builder_ipv4);
		//System.out.println("自定义确认 "+" identification "+identification+" ack_sequence "+ack_sequence+" # "+tcpPacket.getHeader());
		return p;

	}
	
	static Packet createSync(
			MacAddress srcAddress_mac,
			MacAddress dstAddrress_mac,
			Inet4Address srcAddress,short srcPort,
			Inet4Address dstAddress,short dstPort,
			int sequence,short ident){
		TcpPacket.Builder builder_tcp=new TcpPacket.Builder();
		//builder_tcp.payloadBuilder(new UnknownPacket.Builder().rawData(new byte[0]));
		builder_tcp.correctChecksumAtBuild(true);
		builder_tcp.correctLengthAtBuild(true);
		builder_tcp.paddingAtBuild(true);
		//builder_tcp.ack(true);
		//builder_tcp.acknowledgmentNumber(ack_sequence);
		//builder_tcp.checksum(tcpHeader.getChecksum());
		//builder_tcp.dataOffset((byte) 8);
		builder_tcp.dstAddr(dstAddress);
		builder_tcp.dstPort(new TcpPort( dstPort,""));
		//builder_tcp.fin(tcpHeader.getFin());
		
		TcpNoOperationOption nop=TcpNoOperationOption.getInstance();
		
		ArrayList<TcpOption> tcp_options=new ArrayList<TcpOption>();
		
		TcpMaximumSegmentSizeOption seg_option=new TcpMaximumSegmentSizeOption.Builder().maxSegSize(mtu).correctLengthAtBuild(true).build();
		tcp_options.add(seg_option);

		tcp_options.add(nop);

		TcpWindowScaleOption win_option=new TcpWindowScaleOption.Builder().shiftCount((byte)6).correctLengthAtBuild(true).build();
		tcp_options.add(win_option);

		tcp_options.add(nop);
		tcp_options.add(nop);
		
		TcpSackPermittedOption sack_permit_option=TcpSackPermittedOption.getInstance();
		tcp_options.add(sack_permit_option);
		
		builder_tcp.options(tcp_options);
		
		//builder_tcp.padding(tcpHeader.getPadding());
		builder_tcp.psh(false);
		builder_tcp.reserved((byte) 0);
		builder_tcp.rst(false);
		builder_tcp.sequenceNumber(sequence);
		builder_tcp.srcAddr(srcAddress);
		builder_tcp.srcPort(new TcpPort( srcPort,""));
		builder_tcp.syn(true);
		builder_tcp.urg(false);
		//builder_tcp.urgentPointer(tcpHeader.getUrgentPointer());
		builder_tcp.window( window);

		IpV4Packet.Builder builder_ipv4=new IpV4Packet.Builder();
		builder_ipv4.correctChecksumAtBuild(true);
		builder_ipv4.correctLengthAtBuild(true);
		builder_ipv4.paddingAtBuild(true);
		builder_ipv4.dstAddr(dstAddress);
		builder_ipv4.dontFragmentFlag(true);
		builder_ipv4.fragmentOffset((short)0);
		//builder_ipv4.headerChecksum(ipV4Header.getHeaderChecksum());
		//short identification= Math.abs(random.nextInt(Short.MAX_VALUE));
		builder_ipv4.identification(ident);
		builder_ipv4.ihl((byte) 5);
		//builder_ipv4.moreFragmentFlag(ipV4Header.getMoreFragmentFlag());
		//builder_ipv4.options(ipV4Header.getOptions());
		//builder_ipv4.padding(ipV4Header.getPadding());
		
		builder_ipv4.protocol(IpNumber.TCP);
//		builder_ipv4.reservedFlag(ipV4Header.getReservedFlag());
		builder_ipv4.srcAddr(srcAddress);
		builder_ipv4.tos(IpV4Rfc1349Tos.newInstance((byte) 0));
		//builder_ipv4.totalLength( 52);
		builder_ipv4.ttl(ttl);
		builder_ipv4.version(IpVersion.IPV4);
		builder_ipv4.payloadBuilder(builder_tcp);
//
		Packet p = buildIpV4(srcAddress_mac,dstAddrress_mac,builder_ipv4);
//		IpV4Packet p4=builder_ipv4.build();
//		TcpPacket tcpPacket=builder_tcp.build();
		//selfAckTable.add(identification);
		//System.out.println("自定义确认 "+" identification "+identification+" ack_sequence "+ack_sequence+" # "+tcpPacket.getHeader());
		return p;

	}
		
}
