// Copyright (c) 2015 D1SM.net

package net.fs.rudp.message;

public class MessageType {
	public static short sType_DataMessage=80;
	public static short sType_ConnectMessage1=71;
	public static short sType_ConnectMessage2=72;
	public static short sType_ConnectMessage3=73;
	public static short sType_CloseMessage_Stream=75;
	public static short sType_CloseMessage_Conn=76;
	
	public static short sType_AckMessage=61;
	public static short sType_LastReadMessage=65;
	public static short sType_AckListMessage=60;
	
	public static short sType_AskFillMessage=63;
	public static short sType_ReSendMessage=62;
	
	public static short sType_AckListMessageTun=66;
	

	public static short sType_UdpTunDataMessage=90;
	public static short sType_UdpTunOpenMessage=91;
	public static short sType_UdpTunCloseMessage=92;
	
	public static short sType_CleanMessage1=225;
	public static short sType_CleanMessage2=226;
	
	public static short sType_RegMessage=101;
	public static short sType_RegMessage2=102;
	public static short sType_ExitMessage=111;
	public static short sType_ExitMessage2=112;
	public static short sType_PubAddMessage=141;
	public static short sType_PubAddMessage2=142;
	public static short sType_PubDelMessage=151;
	public static short sType_PubDelMessage2=152;
	public static short sType_SLiveMessage=131;
	public static short sType_SLiveMessage2=132;
	public static short sType_GetSNodeMessage=161;
	public static short sType_GetSNodeMessage2=162;
	public static short sType_JoinDBFailMessage1=168;
	public static short sType_JoinDBFailMessage2=169;
	public static short sType_TimeSynMessage1=175;
	public static short sType_TimeSynMessage2=176;
	public static short sType_AdvCSMessage1=178;
	public static short sType_AdvCSMessage2=179;
	
	public static short sType_CastGroupMessage=181;
	public static short sType_CastGroupMessage2=182;
	public static short sType_CastGroupRandomMessage=191;
	public static short sType_CastGroupRandomMessage2=192;
	public static short sType_CastGroupRandomMessage3=193;
	
	public static short sType_Assist_RegMessage=500;
	public static short sType_Assist_RegMessage2=501;
	public static short sType_Assist_PingMessage1=510;
	public static short sType_Assist_PingMessage2=511;
	public static short sType_ReversePingMessage1=515;
	public static short sType_ReversePingMessage2=516;
	public static short sType_ReversePingMessage3=517;
	public static short sType_ReverseConnTCPMessage1=518;
	public static short sType_ReverseConnTCPMessage2=519;
	public static short sType_Assist_OtherOutAddressMessage=520;
	public static short sType_Assist_OtherOutAddressMessage2=521;
	public static short sType_Assist_OtherOutAddressMessage3=522;
	public static short sType_Assist_OtherOutAddressMessage4=523;
	
	public static short sType_Assist_LiveMessage =4125;
	public static short sType_Assist_LiveMessage2=4126;
	public static short sType_Assist_ExitMessage=540;
	public static short sType_Assist_ExitMessage2=541;
	
	
	public static short sType_DB_CastAddMessage=601;
	public static short sType_DB_CastAddMessage2=602;
	public static short sType_DB_CastRemoveMessage=701;
	public static short sType_DB_CastRemoveMessage2=702;
	public static short sType_DB_SourceSearchMessage=711;
	public static short sType_DB_SourceSearchMessage2=712;
	public static short sType_DB_SourceSumMessage1=715;
	public static short sType_DB_SourceSumMessage2=716;
	
	public static short sType_getOutAddressMessage=1181;
	public static short sType_getOutAddressMessage2=1182;
	
	public static short sType_PingMessage=301;
	public static short sType_PingMessage2=302;
	
	public static short sType_PingMessagec=311;
	public static short sType_PingMessagec2=312;
	
	public static short sType_PingMessager=321;
	public static short sType_PingMessager2=322;

}
