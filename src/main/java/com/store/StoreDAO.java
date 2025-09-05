package com.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.db.DBManager;

public class StoreDAO {
	private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "scott"; // DB 사용자
    private static final String PASSWORD = "tiger"; // DB 비밀번호
    
    
    private StoreDAO() {}
    private static final StoreDAO instance = new StoreDAO();
    public static StoreDAO getInstance() { return instance; }

    /**
     * 전체 상점 목록
     */
    public List<StoreDTO> selectInfo() {
        List<StoreDTO> list = new ArrayList<>();
        String sql = "SELECT name, address, rating, category FROM store ORDER BY name";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                StoreDTO vo = new StoreDTO();
                vo.setName(rs.getString("name"));
                vo.setAddress(rs.getString("address"));
                vo.setRating(rs.getDouble("rating"));
                // ✅ category는 문자열로 그대로 사용
                vo.setCategory(rs.getString("category"));
                list.add(vo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
        return list;
    }

    /**
     * 전체 평균 평점 (소수 첫째자리)
     */
    public double avgrating() {
        String sql = "SELECT NVL(ROUND(AVG(rating), 1), 0) AS avg_rating FROM store";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double result = 0.0;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                result = rs.getDouble("avg_rating");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
        return result;
    }

    /**
     * 상점 등록
     */
    public void insertInfo(StoreDTO store) {
        String sql = "INSERT INTO store (name, address, rating, category) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getAddress());
            // rating null 안전 처리
            pstmt.setDouble(3, store.getRating());  // rating이 double이면 무조건 값 있음
            pstmt.setString(4, store.getCategory()); // "일식","중식","양식","한식"
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * 상점 삭제 (PK: name)
     */
    public void deleteInfo(String name) {
        String sql = "DELETE FROM store WHERE name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * 상점 수정 (이름/주소/카테고리) — rating은 수정하지 않음
     * @param originalName 기존 상점명 (WHERE 조건)
     * @param store        변경할 데이터 (name, address, category)
     */
    public void updateInfo(String originalName, StoreDTO store) {
        String sql = "UPDATE store SET name = ?, address = ?, category = ? WHERE name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getAddress());
            pstmt.setString(3, store.getCategory());
            pstmt.setString(4, originalName);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * 카테고리별 목록 (문자열 카테고리)
     * category가 비어있으면 전체 반환
     */
    public List<StoreDTO> selectInfoByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return selectInfo();
        }
        List<StoreDTO> list = new ArrayList<>();
        String sql = "SELECT name, address, rating, category FROM store WHERE category = ? ORDER BY name";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.trim()); // "일식","중식","양식","한식"
            rs = pstmt.executeQuery();
            while (rs.next()) {
                StoreDTO vo = new StoreDTO();
                vo.setName(rs.getString("name"));
                vo.setAddress(rs.getString("address"));
                vo.setRating(rs.getDouble("rating"));
                vo.setCategory(rs.getString("category"));
                list.add(vo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
        return list;
    }
}
