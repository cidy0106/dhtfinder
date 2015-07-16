package com.xidige.dhtfinder;

import java.io.IOException;
import java.net.InetAddress;

public interface UDPSend {
	public void send(byte[]msg,InetAddress target,int port) throws IOException ;
}
