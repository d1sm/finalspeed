// Copyright (c) 2015 D1SM.net

package net.fs.cap;

import org.pcap4j.packet.Packet;

public class IPacket {
	
	int index;

	int sequence;
	
	int legth;
	
	Packet packet;
	
	long firstSendTime;
	
	long sendTime;
	
	long reSendCount;
	
}
