package com.xidige.dhtfinder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class DBConnectionPool {	
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_NAME = "dht";
	private static final String DB_URL = "jdbc:mysql://localhost/"+DB_NAME+"?useUnicode=true&characterEncoding=utf-8";  
    private static final String DB_USER = "root";  
    private static final String DB_PASSWORD = "";  
    
    private static final int MAX_WORKING=200;//最大连接数
    private static final int MAX_IDLE=10;//最大空闲数
    
    private static boolean DB_DRIVER_TEST=false;//已经测试过驱动了
    private static LinkedList<Connection> WORKING_POOLS=new LinkedList<Connection>(); 
	private static LinkedList<Connection> IDLE_POOLS=new LinkedList<Connection>();
	private static Object lock=new Object();
    private DBConnectionPool() {
	}    
    /**
     * 如果已经达到最大链接,那么直接返回null
     * @return
     */
	public static Connection openConnection(){
		Connection connection=null;		
		if (WORKING_POOLS.size()>MAX_WORKING) {
			System.err.println("已达到最大连接池数===>"+MAX_WORKING);
			return null;
		}
		synchronized (lock) {
			if (WORKING_POOLS.size()>MAX_WORKING) {
				System.err.println("已达到最大连接池数===>"+MAX_WORKING);
				return null;
			}
			if(IDLE_POOLS.isEmpty()){
				//测试驱动
				if (!DB_DRIVER_TEST) {
					try {
						Class.forName(DB_DRIVER);
						DB_DRIVER_TEST=true;
					} catch (ClassNotFoundException e) {
						System.err.println(e.getMessage());
					} 
				}
				if (DB_DRIVER_TEST) {
					try {
						//创建几个空闲链接
						for (int i = MAX_IDLE; i > 0; i--) {
							connection = DriverManager.getConnection(DB_URL,
									DB_USER, DB_PASSWORD);
							IDLE_POOLS.add(connection);
						}
					} catch (SQLException e) {
						System.err.println(e.getMessage());
					}
				}					
			}
			
			if(!IDLE_POOLS.isEmpty()){
				connection=IDLE_POOLS.remove(0);
				try {
					if (connection.isClosed()) {
						connection=null;
					}
				} catch (SQLException e) {
					connection=null;
				}			
			}
			
			if(connection!=null){
				WORKING_POOLS.add(connection);
				return connection;
			}else {
				return null;
			}
		}		
	}
	public static  void closeConnection(Connection connection){
		if (connection==null) {
			return;
		}
		synchronized(lock){
			if(connection!=null && WORKING_POOLS.remove(connection) ){
				if (MAX_IDLE>IDLE_POOLS.size()) {
					//加入空闲池
					try {
						if (!connection.isClosed()) {
							IDLE_POOLS.add(connection);
							return;
						}else {
							connection=null;
						}
					} catch (SQLException e) {
						e.printStackTrace();
						connection=null;
					}				
				}else{
					//空闲池有很多了,这个不要了
					try {
						connection.close();
						connection=null;
					} catch (Exception e) {
					}					
				}
			}			
		}
	}
}
