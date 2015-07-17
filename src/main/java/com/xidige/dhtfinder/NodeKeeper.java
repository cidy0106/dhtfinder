package com.xidige.dhtfinder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.xidige.dhtfinder.KRPC.NodeInfo;
/**
 * 
 * 持有所有node，单例
 * 
 * @author kime
 *
 */
public class NodeKeeper {
	private static List<NodeInfo>container=new LinkedList<KRPC.NodeInfo>();
	private NodeKeeper(){
	}
	private static class SingletonHolder{
		private static NodeKeeper INSTANCE=new NodeKeeper();
	}
	public static NodeKeeper getInstance(){
		return SingletonHolder.INSTANCE;
	}	
	
	public void add(NodeInfo nodeInfo){
		if (nodeInfo==null || nodeInfo.getNodeid()==null) {
			return;
		}
		if ( !container.contains(nodeInfo) ) {
//			synchronized (container) { //其实重复一下，没有太大关系，所以还是不加这个了
//				if ( !container.contains(nodeInfo) ) {
					container.add(nodeInfo);
//				}
//			}			
		}
	}
	public void addAll(List<NodeInfo> infos) {
		if (infos!=null && infos.size()>0) {
			for (int i = 0; i < infos.size(); i++) {
				add(infos.get(i));
			}
		}		
	}
	
	/**
	 * 拿第一个
	 * @return
	 */
	public NodeInfo poll(){
		if (container.size()>0) {
			synchronized (container) {
				if (container.size()>0) {
					return container.remove(0);
				}
			}
		}
		return null;
	}

	
}