package com.xidige.dhtfinder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import com.xidige.dhtfinder.KRPC.NodeInfo;

public class TestUdp {
	public static void main(String a[]) throws UnsupportedEncodingException{		
		String msg="3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F3F29593F6869353F3F593F653F00410314523F17793F3F191A3F";
		byte[]buf= KRPC.hex2byte(msg);
		List<NodeInfo>rs= KRPC.nodebyte2NodeInfo(buf);
		for (int i = 0; i < rs.size(); i++) {
			NodeInfo nodeInfo=rs.get(i);
			System.out.printf("%s %s:%d\n",
					KRPC.byte2HexStr(nodeInfo.getNodeid().getBytes(KRPC.US_ASCII)),
					nodeInfo.getIp(),
					nodeInfo.getPort()
					);
		}
	}
	
	public static void testUDPServer(){
		final byte[]recvBuffer=new byte[1024];			
		try {
			final DatagramSocket server=new DatagramSocket(8080);
			
			new Thread(new Runnable() {				
				@Override
				public void run() {
					DatagramPacket packet=new DatagramPacket(recvBuffer, recvBuffer.length);
					DatagramPacket sendPacket=null;
					try {
						InetAddress clientAddress=null;
						int port=0;
						byte[] sendMsg="sended by server".getBytes();
						while(true){
							server.receive(packet);
							clientAddress=packet.getAddress();
							port=packet.getPort();
							
							System.out.println("["+clientAddress.getHostAddress()+":"+port+"]>>>"+new String(packet.getData()));
							
							//反馈点东西吧
							sendPacket=new DatagramPacket(sendMsg, sendMsg.length, clientAddress, port);
							server.send(sendPacket);
						}
					} catch (IOException e) {
						e.printStackTrace();
						if (server!=null) {
							server.close();
						}
					}finally{
						if (server!=null) {
							server.close();
						}
					}					
				}
			}).start();			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void testUDPClient(final String address){
		final byte[]recvBuffer=new byte[1024];		
		try {
			final DatagramSocket client=new DatagramSocket();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					byte[]sendMsg="Sended by client".getBytes();
					int port=80;
					InetAddress targetAddress;
					DatagramPacket sendPacket=null;
					DatagramPacket recvPacket=null;
					try {
						targetAddress = InetAddress.getByName(address);
						
						while(true){
							sendPacket=new DatagramPacket(sendMsg, sendMsg.length, targetAddress, port);
							client.send(sendPacket);
							
							//接收
							recvPacket=new DatagramPacket(recvBuffer, recvBuffer.length);
							client.receive(recvPacket);
							printPacket(recvPacket);
							Thread.sleep(1000);
						}
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						if(client!=null){
							client.close();
						}
					}
				}
			}).start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void printPacket(DatagramPacket packet){
		System.out.println("["+packet.getAddress().getHostAddress()+":"+packet.getPort()+"]>>>"+new String(packet.getData()));
	}
}
