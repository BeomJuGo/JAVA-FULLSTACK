package com.member;

import com.db.DBManager;
import com.member.MemberDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberDAO {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "test"; // DB 사용자
    private static final String PASSWORD = "1234"; // DB 비밀번호

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 회원가입
    public boolean join(MemberDTO member) {
        String sql = "INSERT INTO userinfo (id, pwd, user_name, user_level) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getId());
            pstmt.setString(2, member.getPwd());
            pstmt.setString(3, member.getUser_name());
            pstmt.setInt(4, member.getUser_level());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 아이디 중복 체크
    public boolean existsId(String id) {
        String sql = "SELECT COUNT(*) FROM userinfo WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 로그인
    public MemberDTO login(String id, String pwd) {
        String sql = "SELECT id, pwd, user_name, user_level FROM userinfo WHERE id=? AND pwd=?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            pstmt.setString(2, pwd);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new MemberDTO(
                    rs.getString("id"),
                    rs.getString("pwd"),
                    rs.getString("user_name"),
                    rs.getInt("user_level")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 로그인 실패
    }
    public List<MemberDTO> findAll() {
        List<MemberDTO> list = new ArrayList<>();
        String sql = "SELECT id, pwd, user_name, user_level FROM userinfo";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while(rs.next()) {
                MemberDTO m = new MemberDTO();
                m.setId(rs.getString("id"));
                m.setUser_name(rs.getString("user_name"));
                m.setPwd(rs.getString("pwd"));
                m.setUser_level(rs.getInt("user_level"));
                list.add(m);
            }
        } catch(SQLException e) { e.printStackTrace(); }
        return list;
    }
    public boolean update(MemberDTO member) {
        String sql = "UPDATE userinfo SET pwd=?, user_name=?, user_level=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, member.getPwd());
            pstmt.setString(2, member.getUser_name());
            pstmt.setInt(3, member.getUser_level());
            pstmt.setString(4, member.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean delete(String id) {
        String sql = "DELETE FROM userinfo WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
