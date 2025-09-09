package com.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.db.DBManager;

public class StoreDAO {
    private StoreDAO() {}
    private static final StoreDAO instance = new StoreDAO();
    public static StoreDAO getInstance() { return instance; }

    /**
     * 전체 상점 목록
     * - review 테이블을 LEFT JOIN 하여 평균 평점을 즉시 계산(소수 1자리)
     */
    public List<StoreDTO> selectInfo() {
        List<StoreDTO> list = new ArrayList<>();
        String sql =
            "SELECT s.name, s.address, s.category, s.latitude, s.longitude, " +
            "       ROUND(NVL(AVG(r.rating), 0), 1) AS rating " +
            "  FROM store s " +
            "  LEFT JOIN review r ON r.store_name = s.name " +
            " GROUP BY s.name, s.address, s.category, s.latitude, s.longitude " +
            " ORDER BY s.name";

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
                vo.setCategory(rs.getString("category"));
                vo.setLatitude(rs.getDouble("latitude"));
                vo.setLongitude(rs.getDouble("longitude"));
                vo.setRating(rs.getDouble("rating")); // 평균
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
     * 전체 평균 평점 (모든 리뷰의 평균, 소수 1자리)
     */
    public double avgrating() {
        String sql = "SELECT NVL(ROUND(AVG(r.rating), 1), 0) AS avg_rating FROM review r";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double result = 0.0;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) result = rs.getDouble("avg_rating");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
        return result;
    }

    /**
     * 상점 등록
     * - rating 칼럼을 쓰더라도 기본값(0.0) 정도만 저장하고,
     *   화면 표시는 항상 JOIN AVG 결과를 사용.
     */
    public void insertInfo(StoreDTO store) {
        String sql = "INSERT INTO store (name, address, rating, category, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getAddress());
            pstmt.setDouble(3, store.getRating());   // 보통 0.0으로 전달
            pstmt.setString(4, store.getCategory()); // "일식","중식","양식","한식"
            pstmt.setDouble(5, store.getLatitude());
            pstmt.setDouble(6, store.getLongitude());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * 상점 삭제 (먼저 자식 리뷰 삭제 → 가게 삭제)
     * - ON DELETE CASCADE 없는 환경에서도 ORA-02292 방지
     */
    public void deleteInfo(String name) {
        String sqlDelReviews = "DELETE FROM review WHERE store_name = ?";
        String sqlDelStore   = "DELETE FROM store  WHERE name = ?";

        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        try {
            conn = DBManager.conn();
            conn.setAutoCommit(false);

            ps1 = conn.prepareStatement(sqlDelReviews);
            ps1.setString(1, name);
            ps1.executeUpdate();

            ps2 = conn.prepareStatement(sqlDelStore);
            ps2.setString(1, name);
            ps2.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
        } finally {
            try { if (ps1 != null) ps1.close(); } catch (Exception ignore) {}
            try { if (ps2 != null) ps2.close(); } catch (Exception ignore) {}
            try { if (conn != null) conn.close(); } catch (Exception ignore) {}
        }
    }

    /**
     * 상점 수정 (이름/주소/카테고리) — rating은 직접 수정하지 않음
     */
    public void updateInfo(String originalName, StoreDTO store) {
        String sql = "UPDATE store SET name = ?, address = ?, category = ?, latitude = ?, longitude = ? WHERE name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getAddress());
            pstmt.setString(3, store.getCategory());
            pstmt.setDouble(4, store.getLatitude());
            pstmt.setDouble(5, store.getLongitude());
            pstmt.setString(6, originalName);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * 카테고리별 목록 (문자열 카테고리)
     * - review에서 평균 계산
     */
    public List<StoreDTO> selectInfoByCategory(String category) {
        List<StoreDTO> list = new ArrayList<>();
        boolean all = (category == null || category.trim().isEmpty());

        String sqlAll =
            "SELECT s.name, s.address, s.category, s.latitude, s.longitude, " +
            "       ROUND(NVL(AVG(r.rating), 0), 1) AS rating " +
            "  FROM store s " +
            "  LEFT JOIN review r ON r.store_name = s.name " +
            " GROUP BY s.name, s.address, s.category, s.latitude, s.longitude " +
            " ORDER BY s.name";

        String sqlByCat =
            "SELECT s.name, s.address, s.category, s.latitude, s.longitude, " +
            "       ROUND(NVL(AVG(r.rating), 0), 1) AS rating " +
            "  FROM store s " +
            "  LEFT JOIN review r ON r.store_name = s.name " +
            " WHERE s.category = ? " +
            " GROUP BY s.name, s.address, s.category, s.latitude, s.longitude " +
            " ORDER BY s.name";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(all ? sqlAll : sqlByCat);
            if (!all) pstmt.setString(1, category.trim());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                StoreDTO vo = new StoreDTO();
                vo.setName(rs.getString("name"));
                vo.setAddress(rs.getString("address"));
                vo.setCategory(rs.getString("category"));
                vo.setLatitude(rs.getDouble("latitude"));
                vo.setLongitude(rs.getDouble("longitude"));
                vo.setRating(rs.getDouble("rating")); // 평균
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
     * (선택) 특정 가게의 평균을 store.rating 칼럼에 반영하고 싶을 때 사용
     */
    public void recalcAvg(String storeName) {
        String sql =
            "UPDATE store s " +
            "   SET s.rating = (SELECT ROUND(NVL(AVG(r.rating),0),1) " +
            "                     FROM review r " +
            "                    WHERE r.store_name = s.name) " +
            " WHERE s.name = ?";
        try (Connection c = DBManager.conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, storeName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM store WHERE name = ?";
        try (Connection c = DBManager.conn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
}
