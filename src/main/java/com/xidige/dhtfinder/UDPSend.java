package com.xidige.dhtfinder;

import java.io.IOException;

public interface UDPSend {
	public void send(byte[]msg,String host,int port) throws IOException ;
}
