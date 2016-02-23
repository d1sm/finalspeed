// Copyright (c) 2015 D1SM.net

package net.fs.utils;

public final class ByteShortConvert {
   
    public static byte[] toByteArray(short i,byte[] b,int offset) {
    	 b[offset] = (byte) (i >> 8);
    	   b[offset + 1] = (byte) (i >> 0);
        return b; 
    }


    
    public static short toShort(byte[] b,int offset) { 
        return  (short) (((b[offset] << 8) | b[offset + 1] & 0xff)); 
    }    
    
    //无符号
    public static byte[] toByteArrayUnsigned(int s,byte[] b,int offset) {
   	 b[offset] = (byte) (s >> 8);
   	   b[offset+1] = (byte) (s >> 0);
       return b; 
   }
    
  //无符号
    public static int toShortUnsigned(byte[] b,int offset) {
    	int i = 0;
    	i |= b[offset+0] & 0xFF;
    	i <<= 8;
    	i |= b[offset+1] & 0xFF;
    	return i;
    }    
    
     
}
