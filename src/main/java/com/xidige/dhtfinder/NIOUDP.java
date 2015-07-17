package com.xidige.dhtfinder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NIOUDP extends BaseUDP{	
	private static final int BUFFER_SIZE = 10240;
	private Selector selector=null;	
	private ConcurrentLinkedQueue<Message>msgs=null;
	
	public NIOUDP(){
		this.msgs=new ConcurrentLinkedQueue<NIOUDP.Message>();
	}
	
	@Override
	public void send(byte[] msg, String target, int port)
			throws IOException {
		Message m=new Message();
		m.msg=msg;
		m.target=target;
		m.port=port;
		msgs.add(m);
	}

	@Override
	public void start() throws IOException {		
		DatagramChannel datagramChannel=DatagramChannel.open();
		datagramChannel.configureBlocking(false);
		datagramChannel.bind(new InetSocketAddress(port));		
		selector=Selector.open();
		datagramChannel.register(selector, SelectionKey.OP_READ| SelectionKey.OP_WRITE);		
		new Thread(new Runnable() {			
			@Override
			public void run() {
				ByteBuffer byteBuffer=ByteBuffer.allocate(BUFFER_SIZE);
				try {
					while (true) {
						if (selector.select() > 0) {
							Set<SelectionKey>selectedKeys=selector.selectedKeys();
							Iterator<SelectionKey>iterator=selectedKeys.iterator();
							while (iterator.hasNext()) {
								SelectionKey sk=iterator.next();
								iterator.remove();
								
								if (sk.isReadable()) {
									DatagramChannel dc=(DatagramChannel) sk.channel();
									InetSocketAddress isa=(InetSocketAddress) dc.receive(byteBuffer);
									byteBuffer.flip();
									
									if (onUDPRecv!=null) {
										onUDPRecv.onRecv(isa.getAddress(),isa.getPort() , byteBuffer.array());
									}									
								}else if (sk.isWritable()) {
									if (msgs.size()>0) {										
										Message m=msgs.poll();
										if (m!=null) {
											DatagramChannel dc=(DatagramChannel) sk.channel();
											System.out.printf("[SENDTO : %s:%d]\n",m.target,m.port);
											dc.send(ByteBuffer.wrap(m.msg),new InetSocketAddress(m.target, m.port));
										}
									}									
								}								
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private static class Message{
		byte[] msg;
		String target;
		int port;
	}
}
