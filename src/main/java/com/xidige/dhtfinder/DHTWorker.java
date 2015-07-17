package com.xidige.dhtfinder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.xidige.dhtfinder.BaseUDP.UDPRecv;
import com.xidige.dhtfinder.KRPC.Krpc;
import com.xidige.dhtfinder.KRPC.NodeInfo;
import com.xidige.dhtfinder.KRPC.Request;
import com.xidige.dhtfinder.KRPC.Response;

public class DHTWorker implements UDPRecv {
	private String defualtNodeid = null;
	private KRPC krpc = new KRPC();
	private byte[] self = null;
	private UDPSend sender = null;

	private ExecutorService boss;
	private ScheduledExecutorService findNodeBoss;
	
	private NodeKeeper nodesContainer=NodeKeeper.getInstance();

	public DHTWorker(String nodeid, UDPSend sender) throws IOException {
		this.defualtNodeid = nodeid;
		this.sender = sender;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		krpc.wrapKrpc(findNode(this.defualtNodeid), out);
		self = out.toByteArray();

		boss = Executors.newSingleThreadExecutor();
		findNodeBoss=Executors.newSingleThreadScheduledExecutor();
		findNodeBoss.scheduleAtFixedRate(scheduleSender, 0, 1, TimeUnit.SECONDS);
	}
	
	private Runnable scheduleSender=new Runnable() {		
		@Override
		public void run() {
			// 发数据出去
			NodeInfo ni= nodesContainer.poll();
			if (ni!=null) {
				sendFindNode(ni.getNodeid(), ni.getIp(), ni.getPort());	
			}else{
				//发起find_node
				sendFindNode(defualtNodeid,"router.bittorrent.com", 6881);
				sendFindNode(defualtNodeid,"dht.transmissionbt.com", 6881);
				sendFindNode(defualtNodeid,"router.utorrent.com", 6881);
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	public byte[] findSelf() throws IOException {
		return self;
	}

	private void sendByThread(final byte[] byteArray, final String client,
			final int port) {
		boss.execute(new Runnable() {
			@Override
			public void run() {
				try {
					sender.send(byteArray, client, port);
				} catch (IOException e) {
				}
			}
		});
	}
	

	private class Worker implements Runnable {
		private String host;
		private int port;
		private byte[] msg;
		private KRPC krpc;

		public Worker(InetAddress client, int port, byte[] msg, KRPC krpc) {
			this(client.getHostAddress(), port, msg, krpc);
		}
		public Worker(String client, int port, byte[] msg, KRPC krpc) {
			this.host = client;
			this.port = port;
			this.msg = msg;
			this.krpc = krpc;
		}

		@Override
		public void run() {
			Map<String, Object> rs = null;
			try {
				rs = krpc.parseKrpc(msg);
				if (Response.r.equals(rs.get(Krpc.y))) {
					System.out.println("REC: 收到回复");

					Map<String, Object> temp = (Map<String, Object>) rs
							.get(Response.r);
					if (temp != null && temp.get(Response.nodes) != null) {
						System.out.println("REC: find_node");
						// 回复,处理find_node回复
						parseFindNode(rs);
						return;
					}
				} else if (Request.q.equals(rs.get(Krpc.y))) {
					// 其他点发过来的请求，ping，announce_peer
					String qtype = (String) rs.get(Request.q);
					System.out.println("REC: 收到请求," + qtype);
					if (Krpc.ping.equals(qtype)) {
						System.out.println("REC: ping");
						// 需要回复
						processPing(rs, host, port);
						return;
					} else if (Krpc.announce_peer.equals(qtype)) {
						System.out.println("REC: announce_peer");
						// 解析出infohash即可，不需要回复
						processAnnouncePeer(rs, host, port);
						return;
					} else if (Krpc.get_peers.equals(qtype)) {
						// get_peers请求，回复空node
						responseGetPeers(rs, host, port);
						return;
					}
					responseAny(rs, host, port);
					return;
				}
				System.out.println("REC: any other request");
				processAny(rs, host, port);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if (rs != null) {
					System.out.println("出现异常，回复[202] ");
					try {
						responseAny(rs, host, port);
					} catch (IOException e1) {
					}
				}
			}
		}

	}

	@Override
	public void onRecv(final InetAddress client, final int port,
			final byte[] msg) {
		boss.execute(new Worker(client, port, msg, krpc));
	}

	private String getId(Map<String, Object> rs) {
		Map<String, Object> aMap = (Map<String, Object>) rs.get(Request.a);
		if (aMap != null) {
			return (String) aMap.get(Request.id);
		}
		return null;
	}

	private String getInfohash(Map<String, Object> rs) {
		Map<String, Object> aMap = (Map<String, Object>) rs.get(Request.a);
		if (aMap != null) {
			return (String) aMap.get(Request.info_hash);
		}
		return null;
	}

	private void sendFindNode(String nodeid,String target,int port){
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			krpc.wrapKrpc(findNode(nodeid), out);
			sendByThread(out.toByteArray(), target, port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	private void responseAny(Map<String, Object> rs, String client,
			int port) throws IOException {
		Map<String, Object> any = new TreeMap<String, Object>(KRPC.MYCOMPARATOR);
		any.put(Krpc.t, rs.get(Krpc.t));
		any.put(Krpc.y, Response.e);

		List<Object> eList = new ArrayList<Object>();
		eList.add(202);
		eList.add("Server Error");
		any.put(Response.e, eList);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		krpc.wrapKrpc(any, out);

		sendByThread(out.toByteArray(), client, port);
	}

	private void responseGetPeers(Map<String, Object> rs, String client,
			int port) throws IOException {
		// 需要t，和自己的node
		Map<String, Object> getPeers = new TreeMap<String, Object>(
				KRPC.MYCOMPARATOR);
		getPeers.put(Krpc.t, rs.get(Krpc.t));
		getPeers.put(Krpc.y, Response.r);

		Map<String, Object> rMap = new TreeMap<String, Object>(
				KRPC.MYCOMPARATOR);
		rMap.put(Krpc.id, KRPC.createNodeid(getInfohash(rs), defualtNodeid));
		rMap.put(Response.nodes, "");
		rMap.put(Krpc.token, KRPC.createToken());

		getPeers.put(Response.r, rMap);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		krpc.wrapKrpc(getPeers, out);
		sendByThread(out.toByteArray(), client, port);
	}

	private void processAny(Map<String, Object> rs, String target, int port)
			throws IOException {
		Map<String, Object> pongMap = new TreeMap<String, Object>(
				KRPC.MYCOMPARATOR);
		pongMap.put(Krpc.t, rs.get(Krpc.t));
		pongMap.put(Krpc.y, Response.r);

		Map<String, Object> rMap = new TreeMap<String, Object>(
				KRPC.MYCOMPARATOR);
		rMap.put(Krpc.id, KRPC.createNodeid(getId(rs), defualtNodeid));

		pongMap.put(Response.r, rMap);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		krpc.wrapKrpc(pongMap, out);
		sendByThread(out.toByteArray(), target, port);
	}

	private void processAnnouncePeer(Map<String, Object> rs,
			String target, int port) throws IOException {
		// 解析a里面的info_hash
		Map<String, Object> aMap = (Map<String, Object>) rs.get(Request.a);
		String infohash = (String) aMap.get(Request.info_hash);
		InfoHashKeeper.addHash(infohash);
		System.out.println("get a infohash....");
		processAny(rs, target, port);
	}

	private void processPing(Map<String, Object> rs, String target,
			int port) throws IOException {
		processAny(rs, target, port);
	}

	private void parseFindNode(Map<String, Object> msg)
			throws UnknownHostException, IOException {
		Map<String, Object> temp = (Map<String, Object>) msg.get(Response.r);
		String nodes = (String) temp.get(Response.nodes);
		List<NodeInfo> infos = KRPC.nodebyte2NodeInfo(nodes);
		nodesContainer.addAll(infos);
	}

	public  Map<String, Object> findNode(String nodeid) {
		Map<String, Object> findNodeMap = new TreeMap<String, Object>(
				KRPC.MYCOMPARATOR);
		findNodeMap.put(Krpc.t, KRPC.createTid());
		findNodeMap.put(Krpc.y, Request.q);
		findNodeMap.put(Request.q, Request.find_node);

		Map<String, Object> aMap = new TreeMap<String, Object>(
				KRPC.MYCOMPARATOR);		
		aMap.put(Krpc.id, KRPC.createNodeid(nodeid,defualtNodeid));
		aMap.put(Request.target, KRPC.createNodeid(null, null));
		findNodeMap.put(Request.a, aMap);
		return findNodeMap;
	}

	public String getNodeid() {
		return defualtNodeid;
	}
}
