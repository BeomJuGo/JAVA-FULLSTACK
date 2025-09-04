package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DBManager {
	public static Connection conn() {
		Connection conn = null;

		try {
			// DBCP 방식 대신 일반 JDBC 직접 연결 방식으로 변경
			String driver = "oracle.jdbc.OracleDriver";
			String url = "jdbc:oracle:thin:@127.0.0.1:1521:xe";
			String id = "scott";
			String pw = "tiger";
			
			Class.forName(driver);
			conn = DriverManager.getConnection(url, id, pw);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}

	// insert, delete, update 모듈
	public static void close(Connection conn, Statement stmt) {
		try {
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// select 모듈
	public static void close(Connection conn, Statement stmt, ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
