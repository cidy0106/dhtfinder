package com.xidige.dhtfinder.dht;

import java.io.IOException;

public interface DHT {
	public void onMessageHandle(byte[]msg,String srcHost,int port) throws IOException ;
}
