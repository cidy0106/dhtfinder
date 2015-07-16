package com.xidige.dhtfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 先缓存在这里吧，不知道数据量会有多大
 * 
 * 每次接受到的infohash都先缓存在这里，然后定时放到数据库里去
 * @author kime
 *
 */
public class InfoHashKeeper {
	private static final Object LOCK_OBJECT=new Object();
	private static final int MAX_COUNT=200;
	private static Set<String>queue=new HashSet<String>();
	public static void addHash(String infohash){
		queue.add(infohash);
		if (queue.size()>MAX_COUNT) {
			synchronized (LOCK_OBJECT) {
				if (queue.size()>MAX_COUNT) {					
					Set<String>temp=queue;
					queue=new HashSet<String>();
					
					//全部存到数据库吧
					save2Db(temp);
					return;
				}				
			}
		}		
	}
	private static void save2Db(Set<String>infohashs){
		if (infohashs!=null && infohashs.size()>0) {
			Connection connection=DBConnectionPool.openConnection();
			PreparedStatement ps=null;
			boolean txSeted=false;
			boolean autocomminted=false;
			try {
				autocomminted=connection.getAutoCommit();
				connection.setAutoCommit(false);
				txSeted=true;
				
				ps=connection.prepareStatement("insert into infohash(infohash) select ? from dual where not exists(select id from infohash where infohash=?) ");
				
				for (Iterator<String> iterator = infohashs.iterator(); iterator.hasNext();) {
					String hash = iterator.next();
					hash=KRPC.byte2HexStr(hash.getBytes());
					ps.setString(1, hash);
					ps.setString(2, hash);
					ps.addBatch();
				}
				ps.executeBatch();				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if (ps!=null) {
					try {
						ps.close();
					} catch (SQLException e) {
					}
				}
				if (connection!=null) {
					if (txSeted) {
						try {
							connection.setAutoCommit(autocomminted);
						} catch (SQLException e) {
						}
					}
					DBConnectionPool.closeConnection(connection);
				}
			}
			
		}
	}
}
