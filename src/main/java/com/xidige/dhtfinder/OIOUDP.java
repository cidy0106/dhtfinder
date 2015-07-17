package com.xidige.dhtfinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class OIOUDP extends BaseUDP{
	private static final int BUFFER_SIZE = 10240;	
	private DatagramSocket client = null;	
	
	
	public OIOUDP(){
	}
	
	@Override
	public void send(byte[] msg, String host, int port) throws IOException {
		System.out.printf("[SENDTO : %s:%d]\n",host,port);
		DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName(host), port);
		try {
			client.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void start() throws IOException {
		client=new DatagramSocket(port);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] recvBuf = new byte[BUFFER_SIZE];
					
					while (true) {						
						DatagramPacket packet = new DatagramPacket(recvBuf, BUFFER_SIZE);
						client.receive(packet);
						if (onUDPRecv!=null) {
							byte[]buf=new byte[packet.getLength()];
							byte[]holdBuf=packet.getData();
							for (int i = 0; i < buf.length; i++) {
								buf[i]=holdBuf[i];
							}
							onUDPRecv.onRecv(packet.getAddress(), packet.getPort(), buf);
						}
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
