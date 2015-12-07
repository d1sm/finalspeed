// Copyright (c) 2015 D1SM.net

package net.fs.utils;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;


public class Tools {

	public static HttpURLConnection getConnection(String urlString) throws Exception{
		URL url = new URL(urlString);
		HttpURLConnection conn = null;
		if(urlString.startsWith("http://")){
			conn = (HttpURLConnection) url.openConnection();
		}else if(urlString.startsWith("https://")){
			HttpsURLConnection conns=(HttpsURLConnection)url.openConnection();
			conns.setHostnameVerifier(new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
			conn=conns;
		}
		if(conn!=null){
			conn.setConnectTimeout(10*1000);
			conn.setReadTimeout(10*1000);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
		}
		return conn;
	}
	
	public static String getMD5(String str) {
		byte[] source=str.getBytes();
		return getMD5(source);
	}  

	public static String getMD5(byte[] source) {   
		String s = null;   
		char hexDigits[] = { 
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',  'e', 'f'};    
		try  
		{   
			java.security.MessageDigest md = java.security.MessageDigest.getInstance( "MD5" );   
			md.update( source );   
			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) { 
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
				str[k++] = hexDigits[byte0 & 0xf]; 
			}    
			s = new String(str); 

		}catch( Exception e )   
		{   
			e.printStackTrace();   
		}   
		return s;   
	}   
	

	public static String getSizeStringKB(long size){
		int gb=(int) (size/(1024*1024*1024));
		int gbl=(int) (size%(1024*1024*1024));
		int mb=gbl/(1024*1024);
		int mbl=gbl%(1024*1024);
		int kb=mbl/(1024);
		String ls="";
		if(gb>0){
			ls+=gb+",";
		}
		if(mb>0){
			String mbs="";
			if(gb>0){
				if(mb<10){
					mbs+="00";
				}else if(mb<100){
					mbs+="0";
				}
			}
			mbs+=mb;
			ls+=mbs+",";
		}else{
			if(gb>0){
				ls+="000,";
			}
		}

		if(kb>0){
			String kbs="";
			if(gb>0|mb>0){
				if(kb<10){
					kbs+="00";
				}else if(kb<100){
					kbs+="0";
				}
			}
			kbs+=kb;
			ls+=kbs+" KB";
		}else{
			if(mb>0|gb>0){
				ls+="000 KB";
			}
		}
		if(size==0){
			ls+=0+" KB";
		}
		if(size<1024){
			//ls=size+" b";
			ls=0+" KB";
		}
		return ls;
	}

}
