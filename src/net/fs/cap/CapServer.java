// Copyright (c) 2015 D1SM.net

package net.fs.cap;


public class CapServer {
	
	CapServer(){
		CapEnv capEnv=null;
		try {
			capEnv=new CapEnv(false,true);
			capEnv.init();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

}
