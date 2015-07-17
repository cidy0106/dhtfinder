package com.xidige.dhtfinder;

import java.io.IOException;
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
    		BaseUDP udp=new OIOUDP();
//			BaseUDP udp=new NIOUDP();
			udp.bind(6882);
			DHTWorker dhtWorker=new DHTWorker(KRPC.createNodeid(null,null),udp);			
			udp.setOnUDPRecv(dhtWorker);
			udp.start();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
