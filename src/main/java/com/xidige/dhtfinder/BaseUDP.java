package com.xidige.dhtfinder;

import java.io.IOException;
import java.net.InetAddress;

public abstract class BaseUDP implements UDPSend {
	protected int port=-1;
	protected UDPRecv onUDPRecv;
	protected void bind(int port){
		this.port=port;
	};	
	public void setOnUDPRecv(UDPRecv onUDPRecv) {
		this.onUDPRecv = onUDPRecv;
	}
	@Override
	public abstract void send(byte[] msg, String target, int port)
			throws IOException;
	public abstract void start()throws IOException;
	
	

	public static interface UDPRecv{
		public void onRecv(InetAddress client,int port,byte[]msg);
	}
}
