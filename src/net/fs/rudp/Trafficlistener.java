// Copyright (c) 2015 D1SM.net

package net.fs.rudp;

public interface Trafficlistener {
	
	public void trafficDownload(TrafficEvent event);
	

	public void trafficUpload(TrafficEvent event);
	
}
