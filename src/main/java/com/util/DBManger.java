package com.db;

import java.sql.*;

public class DBManager {
    // ▼ 본인 환경에 맞게
    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/XE";
    private static final String USER = "scott";
    private static final String PASSWORD = "tiger";

    static {
        try {
            // 최신 드라이버 클래스명
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Oracle JDBC Driver not found. Put ojdbc11.jar in WEB-INF/lib.", e);
        }
    }

    public static Connection conn() throws SQLException {
        // 실패 시 SQLException 던짐 (null 리턴 금지!)
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection c, Statement s) {
        if (s != null) try { s.close(); } catch (Exception ignore) {}
        if (c != null) try { c.close(); } catch (Exception ignore) {}
    }

    public static void close(Connection c, Statement s, ResultSet r) {
        if (r != null) try { r.close(); } catch (Exception ignore) {}
        if (s != null) try { s.close(); } catch (Exception ignore) {}
        if (c != null) try { c.close(); } catch (Exception ignore) {}
    }
}
