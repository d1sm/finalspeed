// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

import net.fs.rudp.StreamPipe.HttpHost;

public class ConnInfo {

	
	String requestHost=null;
	
	String requestPath=null;
	
	boolean http=false;
	
	HttpHost host=null;

	public String getRequestHost() {
		return requestHost;
	}

	public void setRequestHost(String requestHost) {
		this.requestHost = requestHost;
	}

	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	public boolean isHttp() {
		return http;
	}

	public void setHttp(boolean http) {
		this.http = http;
	}

	public HttpHost getHost() {
		return host;
	}

	public void setHost(HttpHost host) {
		this.host = host;
	}
	
}
