// Copyright (c) 2015 D1SM.net

package net.fs.utils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

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
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}
