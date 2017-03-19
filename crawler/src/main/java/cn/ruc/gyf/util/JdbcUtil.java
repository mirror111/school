package cn.ruc.gyf.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcUtil {

	private static String USERNAME ;
	private static String PASSWORD;
	private static String DRIVER;
	private static String URL;
	
	private Connection connection;
	
	static{
		//加载数据库配置信息，并给相关的属性赋值
		loadConfig();
	}

	/**
	 * 加载数据库配置信息，并给相关的属性赋值
	 */
	public static void loadConfig() {
		try {
			USERNAME = "";
			PASSWORD = "";
			DRIVER= "com.mysql.jdbc.Driver";
			URL = "";
		} catch (Exception e) {
			System.out.println("Read config file err");
			e.printStackTrace();
		}
	}

	public JdbcUtil() {

	}

	/**
	 * 获取数据库连接
	 * 
	 * @return 数据库连接
	 */
	public Connection getConnection() {
		try {
			Class.forName(DRIVER); // 注册驱动
			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); // 获取连接
		} catch (Exception e) {
			System.out.println("get connection error!");
			e.printStackTrace();
		}
		return connection;
	}

	/**
	 * 释放资源
	 */
	public void releaseConn() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		JdbcUtil jdbcUtil = new JdbcUtil();
		jdbcUtil.getConnection();
		jdbcUtil.releaseConn();
	}
}
