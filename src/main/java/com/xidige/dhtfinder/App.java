package com.xidige.dhtfinder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Hello world!
 * mvn exec:java -Dexec.mainClass="com.xidige.dhtfinder.App"
 *
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	try {				
//			OIOUDP udp=new OIOUDP();
			BaseUDP udp=new NIOUDP();
			udp.bind(6882);
			DHTWorker dhtWorker=new DHTWorker(KRPC.createNodeid(null,null),udp);			
			udp.setOnUDPRecv(dhtWorker);
			udp.start();
			
			//先查自己，可以得到其他node
			udp.send(dhtWorker.findSelf(), InetAddress.getByName("router.bittorrent.com"), 6881);
			udp.send(dhtWorker.findSelf(), InetAddress.getByName("dht.transmissionbt.com"), 6881);
			udp.send(dhtWorker.findSelf(), InetAddress.getByName("router.utorrent.com"), 6881);
			
//			udp.send(dhtWorker.findSelf(), InetAddress.getByName("68.63.64.9"),24874);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("118.63.63.5"),16191);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("84.63.15.16"),6719);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("123.63.82.63"),1040);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("50.63.91.75"),23370);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("91.20.118.99"),6719);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("114.25.56.46"),21374);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("63.74.63.63"),16215);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("123.63.20.36"),11327);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("110.33.63.101"),25919);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("63.29.63.63"),16191);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("63.63.82.90"),9023);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("68.0.79.52"),16191);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("63.63.55.67"),1024);
//					udp.send(dhtWorker.findSelf(), InetAddress.getByName("116.31.63.63"),5695);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
