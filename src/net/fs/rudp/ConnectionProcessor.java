// Copyright (c) 2015 D1SM.net

package net.fs.rudp;



public interface ConnectionProcessor {
	abstract void process(final ConnectionUDP conn);
}
