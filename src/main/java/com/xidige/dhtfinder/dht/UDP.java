package com.xidige.dhtfinder.dht;

import java.io.IOException;

public abstract class UDP {
	protected DHT dht;
	protected int port;
	public UDP(DHT dht,int port){
		this.dht=dht;
	}
	public abstract void start() throws IOException;
	public abstract void send(byte[]msg,String host,int port) throws IOException ;
}
