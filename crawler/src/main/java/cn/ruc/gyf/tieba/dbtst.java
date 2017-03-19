package cn.ruc.gyf.tieba;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.Statement;

import cn.ruc.gyf.util.JdbcUtil;


public class dbtst {
	
	
	
	
	
	
	public static void main(String[] args) throws Exception {

		JdbcUtil jdbcUtil = new JdbcUtil();
		Connection conn=jdbcUtil.getConnection();
		Statement st=null;
		ResultSet re=null;
		if(conn==null){
			System.out.println("cann't connect DB");
			System.exit(-1);
		}
		try {

			String sql="selast max(id) from tieba";
			st = (Statement) conn.createStatement();
			re=st.executeQuery(sql);
			if(re.next()){
				System.out.println(re.getInt(1));
			}
			st.close();
			jdbcUtil.releaseConn();
			System.out.println("ok");
		} catch (SQLException e) {
			System.out.println("update db err");
			System.exit(-1);
		}
		
		System.out.println("lllllllll");
		
	
	}
}
