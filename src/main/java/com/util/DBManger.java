package Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DBManager {
	public static Connection conn() {
		Connection conn = null;

		try {
			// DBCP 접속 방법
			Context ct = new InitialContext();
			Context env = (Context) ct.lookup("java:/comp/env");
			DataSource ds = (DataSource) env.lookup("jdbc/FileUp");
			conn = ds.getConnection();

			/*
			 * Connection conn = null; Context ct = new InitialContext(); DataSource ds =
			 * (DataSource)ct.lookup("java:/comp/env/jdbc/join"); conn = ds.getConnection();
			 */

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
