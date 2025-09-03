package com.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import Util.DBManager;



public class StoreDAO {
	public StoreDAO() {
		// TODO Auto-generated constructor stub
	}
	private static StoreDAO instance = new StoreDAO();

	public static StoreDAO getInstance() {
		return instance;
	}
	public List<StoreDTO> selectAllStore() {
		List<StoreDTO> list = new ArrayList();

		String sql = "select * from store order by name desc";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			conn = DBManager.conn();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				StoreDTO vo = new StoreDTO();
				vo.setAdress(rs.getString("adress"));
				vo.setName(rs.getString("name"));
				vo.setRating(rs.getDouble("rating"));
				vo.setCategory(rs.getString("pictureUrl"));
				

				list.add(vo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DBManager.close(conn, pstmt, rs);
		}

		return list;
	}

	// 상품 삭제 메소드
	public void deleteStore(String name) {
		String sql = "delete store where name=?";
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
	
}
