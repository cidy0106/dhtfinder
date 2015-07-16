package com.xidige.dhtfinder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.googlecode.jbencode.Value;
import com.googlecode.jbencode.composite.DictionaryValue;
import com.googlecode.jbencode.composite.EntryValue;
import com.googlecode.jbencode.composite.ListValue;
import com.googlecode.jbencode.primitive.IntegerValue;
import com.googlecode.jbencode.primitive.StringValue;

public class KRPC {	
	public static final String US_ASCII = "US-ASCII";
	private static final String SHA1 = "SHA1";
	private static final char CHAR[]={'0','1','2','3','4','5','6','7','8',
		'9','A'	,'B','C','D','E','F'
	};
	
	public interface Krpc{
		public static final String t="t";
		public static final String y="y";
		public static final String id="id";
		
		public static final String ping="ping";
		public static final String find_node="find_node";
		public static final String get_peers="get_peers";
		public static final String announce_peer="announce_peer";
		public static final String token="token";
	}
	public interface Request extends Krpc{
		public static final String q="q";
		public static final String a="a";
		public static final String target="target";
		public static final String info_hash="info_hash";	
	}
	public interface Response extends Krpc{
		public static final String r="r";
		public static final String e="e";
		public static final String nodes="nodes";
	}
	
	
	public static class NodeInfo{
		private String nodeid;
		private String ip;
		private int port;
		public String getNodeid() {
			return nodeid;
		}
		public void setNodeid(String nodeid) {
			this.nodeid = nodeid;
		}
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}
	
	public static List<NodeInfo> nodebyte2NodeInfo(byte[]buf){
		List<NodeInfo>nodes=new ArrayList<KRPC.NodeInfo>();
		if (buf!=null	&& buf.length%26==0 ) {
			try {				
				NodeInfo info=null;
				byte[]ipBuf=new byte[4];
				int minLen=buf.length-26;
				for (int i = 0; i <minLen ;) {
					info=new NodeInfo();
					info.setNodeid(new String(buf, i, 20 , US_ASCII));
					
					i+=20;
					//ip,一个字节一个段
					ipBuf[0]=buf[i];
					ipBuf[1]=buf[i+1];
					ipBuf[2]=buf[i+2];
					ipBuf[3]=buf[i+3];
					try {
						info.setIp(InetAddress.getByAddress(ipBuf).getHostAddress());
					} catch (UnknownHostException e) {
						e.printStackTrace();
						
						info.setIp(String.format("%d.%d.%d.%d",0x00ff&buf[i]
						,0x00ff&buf[i+1],0x00ff&buf[i+2],0x00ff&buf[i+3]));
					}
					i+=4;

					//端口
					int port=0;
					port=( (0xff00&(buf[i]<<8)) | (0xff&(buf[i+1])) );
					info.setPort(port);
					nodes.add(info);
					i+=2;
				}
				if (nodes.size()>0) {
					return nodes;
				}
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}		
		}
		if (nodes.size()>0) {
			return nodes;
		}
		return null;
	}
	/**
	 * 前4字节是ip，后两个是端口
	 * 网络字节序
	 * @param bstr
	 * @return
	 */
	public static List<NodeInfo> nodebyte2NodeInfo(String bstr){		
		if (bstr==null ) {
			return null;
		}
		try {
			byte[]buf=bstr.getBytes(US_ASCII);
			return nodebyte2NodeInfo(buf);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	/**
	 * 创建用于token的字符串
	 * @return
	 */
	public static String createToken(){
		byte[]input=new byte[8];
		for (int i = 0; i < 8; i++) {
			input[i]|=System.nanoTime();
		}
		return new String(input);
	}
	/**
	 * 创建个20字节的nodeid
	 * 这里直接使用给定nodeid的前prefix个字节作为前缀
	 * 
	 * @param srcNodeid
	 * @param currentNodeid
	 * @return
	 */
	public static String createNodeid(String srcNodeid,String currentNodeid){
		if (srcNodeid==null && currentNodeid!=null) {
			srcNodeid=currentNodeid;
		}
		try {			
			byte[]input=new byte[20];
			int i=0;
			byte[] srcBuf=null;
			if (srcNodeid != null) {
				srcBuf = srcNodeid.getBytes(US_ASCII);
				for (i = 0; i < 10 && i < srcBuf.length; i++) {
					input[i] = srcBuf[i];
				}
			}
			if (currentNodeid != null) {
				srcBuf=currentNodeid.getBytes(US_ASCII);
				for (int j=0; j<srcBuf.length && i< input.length; i++,j++) {
					input[i] = srcBuf[i];
				}
			}
			for (; i < 20; i++) {
				input[i] |= System.nanoTime();
			}
			return new String(input,US_ASCII);			
		} catch (UnsupportedEncodingException e) {
			char[]chars=new char[10];
			for (int j = 0; j < chars.length; j++) {
				chars[j]|=System.nanoTime();
			}
			return new String(chars);
		}
	}
	/**
	 * 4个字节的t串
	 * @return
	 */
	public static String createTid(){
		byte[]buf=new byte[4];
		long seed=System.nanoTime();
		buf[0]|=seed>>24;
		buf[1]|=seed>>16;
		buf[2]|=seed>>8;
		buf[3]|=seed;
		return new String(buf);
	}
	
	
	/**
	 * 字节数组转成十六进制字符串
	 * infohash
	 * @param input
	 * @return
	 */
	public static String byte2HexStr(byte[]input){
		StringBuffer buffer=new StringBuffer();
		for (int i = 0; i < input.length; i++) {
			buffer.append(CHAR[ (input[i]&0xf0) >> 4]);
			buffer.append(CHAR[ input[i] & 0xf ]);			
		}
		return buffer.toString();
	}
	
	/**
	 * 十六进制字符串转数据字节
	 * infohash
	 * @param hex
	 * @return
	 * @throws IOException
	 */
	public static byte[] hex2byte(String hex){
		int slen=hex.length();
		int blen=slen/2;
		if (hex==null ||(slen=hex.length())==0 
				|| (blen=slen/2)==0) {
			return null;
		}
		byte[]buf=new byte[blen];
		for (int i = 0,j=0; i < blen && j<slen; i++) {
			buf[i]=(byte) ((hexChar2Int(hex.charAt(j++))<<4) | hexChar2Int(hex.charAt(j++)) ) ;
		}
		return buf;
	}
	/**
	 * 
	 * @param ch
	 * @return 非16进制，返回-1
	 */
	private static int hexChar2Int(char ch){
		if ('a'<=ch && ch<='z') {
			return 10+ch-'a'; // 'a'=97
		}else if ('A'<=ch && ch<='Z') {
			return 10+ch-'A'; // 'A'=65
		}else if ('0'<=ch && ch<='9') {
			return ch-'0'; // '0'=48
		}
		return -1;
	}
	
	
	public void wrapKrpc(Object object, OutputStream out) throws IOException{
		if (object instanceof Map) {
			out.write('d');
			for (Iterator<Entry<String,Object>> iterator = ((Map<String,Object>)object).entrySet().iterator(); iterator.hasNext();) {
				Entry<String,Object> entry = iterator.next();
				wrapKrpc(entry.getKey(), out);
				wrapKrpc(entry.getValue(), out);
			}
			out.write('e');
		}else if (object instanceof List) {
			out.write('l');
			for (Iterator<Object> iterator = ((List<Object>)object).iterator(); iterator.hasNext();) {
				Object ele = iterator.next();
				wrapKrpc(ele, out);
			}
			out.write('e');
		}else if (object instanceof Number) {
			out.write('i');
			out.write( String.valueOf(object).getBytes(US_ASCII) );
			out.write('e');
		}else if (object instanceof String) {
			byte[] str=((String) object).getBytes(US_ASCII);
			out.write(String.valueOf(str.length).getBytes(US_ASCII));
			out.write(':');
			out.write(str);
		}
	}
	
	public Map<String, Object>parseKrpc(byte[]input) throws IOException{
		Value<?>value=BencodeUtil.decode(input);
		return (Map<String, Object>) parse(value);
	}
	private static Object parse(Value<?>value)throws IOException{
		if (value instanceof IntegerValue) {
			return parse((IntegerValue)value);
		}else if (value instanceof StringValue) {
			return parse((StringValue)value);
		}else if (value instanceof ListValue) {
			return parse((ListValue)value);
		}else if (value instanceof DictionaryValue) {
			return parse((DictionaryValue)value);
		}else {
			throw new IOException("Error occur");
		}
	}
	private static String parse(StringValue value) throws IOException{
		return new String(((StringValue)value).resolve(),US_ASCII);
	}
	private static Object parse(IntegerValue value) throws IOException{
		return ((IntegerValue)value).resolve();	
	}
	private static List<Object>parse(ListValue value)throws IOException{
		List<Object>dest=new ArrayList<Object>();
		for (Iterator<Value<?>> iterator = value.iterator(); iterator.hasNext();) {
			Value<?> v = iterator.next();
			dest.add(parse(v));			
		}
		return dest;
	}
	private static Map<String, Object>parse(DictionaryValue value)throws IOException{
		Map<String, Object>dest=new TreeMap<String, Object>(MYCOMPARATOR);
		for (Iterator<EntryValue> iterator = value.iterator(); iterator.hasNext();) {
			EntryValue entryValue=iterator.next();
			dest.put(new String(entryValue.getKey().resolve(),US_ASCII), parse(entryValue.getValue()));
		}
		return dest;
	}
	
	public final static Comparator<String> MYCOMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}
	};
}
