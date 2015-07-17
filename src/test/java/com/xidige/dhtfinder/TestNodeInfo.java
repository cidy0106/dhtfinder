package com.xidige.dhtfinder;

import org.junit.Assert;
import org.junit.Test;

import com.xidige.dhtfinder.KRPC.NodeInfo;

public class TestNodeInfo {
	@Test
	public void testEquals(){
		NodeInfo n1=new NodeInfo();
		NodeInfo n2=new NodeInfo();
		
		n1.setNodeid("1");
		n2.setNodeid("1");
		Assert.assertEquals("equals true", true, n1.equals(n2));
		
		
		n2.setNodeid("2");
		Assert.assertEquals("equals false", false, n1.equals(n2));
	}
}
