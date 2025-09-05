package com.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.db.DBManager;



public class StoreDAO {
	public StoreDAO() {
		// TODO Auto-generated constructor stub
	}
	private static StoreDAO instance = new StoreDAO();

	public static StoreDAO getInstance() {
		return instance;
	}
    /**
     * 전체 상점 목록을 가져온다.
     * @return store 테이블 전체 데이터를 목록 형태로 반환한다.
     */
    public List<StoreDTO> selectInfo() {
        List<StoreDTO> list = new ArrayList<>();
        String sql = "SELECT name, adress, rating, category FROM store ORDER BY name";
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
                vo.setAdress(rs.getString("adress"));
                vo.setRating(rs.getDouble("rating"));
                /*
                 * 카테고리 컬럼은 DB에서 문자열(한글 명칭)로 저장될 수 있으므로
                 * 문자열로 읽어와 DTO에 그대로 저장한다. 만약 숫자 코드(1~4)로 저장되어 있을 경우에도
                 * getString()을 사용하면 문자열로 변환되므로, 이후 화면에서 명칭을 그대로 사용할 수 있다.
                 */
                String catVal = rs.getString("category");
                vo.setCategory(convertCategoryToDisplay(catVal));
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
     *선택 상점의 평균 평점을 계산한다.
     * @return 평균 평점을 소수점 첫째자리까지 반올림한 값. 데이터가 없으면 0을 반환한다.
     */
    public double avgrating(String storeName) {
        String sql = "SELECT NVL(ROUND(AVG(rating), 1), 0) AS avg_rating FROM review WHERE store_name = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double result = 0.0;

        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, storeName); // 💡 store_name 파라미터 바인딩
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
     * 상점 정보를 추가한다.
     * @param store DTO 객체에 담긴 정보를 기반으로 store 테이블에 insert
     */
    public void insertInfo(StoreDTO store) {
        String sql = "INSERT INTO store (name, adress, rating, category) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, store.getName());
            pstmt.setString(2, store.getAdress());
            pstmt.setDouble(3, store.getRating());
            /*
             * DB의 category 컬럼에는 한글 명칭(일식, 중식, 양식, 한식) 또는 숫자 코드가 저장될 수 있다.
             * 저장 시 DTO의 카테고리 값을 그대로 입력한다. 숫자 코드 변환은 더 이상 수행하지 않는다.
             */
            pstmt.setString(4, store.getCategory());
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * 상점 정보를 삭제한다.
     * @param name PK인 상점 이름
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
     * 카테고리 명칭을 숫자 코드로 변환한다.
     * DB에는 번호(1:일식, 2:중식, 3:양식, 4:한식)를 저장한다.
     * @param category 한글 카테고리명
     * @return 정수 코드 (일식=1, 중식=2, 양식=3, 한식=4). 해당되지 않으면 0
     */
    private int getCategoryCode(String category) {
        if (category == null) return 0;
        switch (category.trim()) {
            case "일식":
                return 1;
            case "중식":
                return 2;
            case "양식":
                return 3;
            case "한식":
                return 4;
            default:
                return 0;
        }
    }

    /**
     * 카테고리 코드에서 한글 명칭을 반환한다.
     * @param code 카테고리 번호 (1~4)
     * @return 한글 명칭. 해당되지 않으면 빈 문자열
     */
    private String getCategoryName(int code) {
        switch (code) {
            case 1:
                return "일식";
            case 2:
                return "중식";
            case 3:
                return "양식";
            case 4:
                return "한식";
            default:
                return "";
        }
    }

    /**
     * DB에서 읽은 category 값(숫자 또는 문자열)을 화면 표시용 문자열로 변환한다.
     * 만약 값이 숫자 문자열(예: "1")이면 getCategoryName으로 매핑하고,
     * 한글 명칭인 경우에는 그대로 반환한다.
     * 그 외 값은 빈 문자열을 반환한다.
     *
     * @param catVal DB에서 조회한 카테고리 값
     * @return 화면에 표시할 카테고리명
     */
    private String convertCategoryToDisplay(String catVal) {
        if (catVal == null) {
            return "";
        }
        catVal = catVal.trim();
        // 숫자 코드가 문자열로 저장되어 있는 경우 처리
        try {
            int code = Integer.parseInt(catVal);
            return getCategoryName(code);
        } catch (NumberFormatException e) {
            // 숫자로 변환되지 않는 경우 한글 명칭으로 간주하고 그대로 사용
            return catVal;
        }
    }

    /**
     * 지정된 카테고리의 상점 목록을 가져온다.
     * @param category 한글 카테고리명 (일식, 중식, 양식, 한식)
     * @return 해당 카테고리에 속한 상점 목록
     */
    public List<StoreDTO> selectInfoByCategory(String category) {
        List<StoreDTO> list = new ArrayList<>();
        // 카테고리가 없거나 빈 문자열이면 전체 목록 반환
        if (category == null || category.trim().isEmpty()) {
            return selectInfo();
        }
        String sql = "SELECT name, adress, rating, category FROM store WHERE category = ? ORDER BY name";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBManager.conn();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.trim());
            rs = pstmt.executeQuery();
            while (rs.next()) {
                StoreDTO vo = new StoreDTO();
                vo.setName(rs.getString("name"));
                vo.setAdress(rs.getString("adress"));
                vo.setRating(rs.getDouble("rating"));
                String catVal = rs.getString("category");
                vo.setCategory(convertCategoryToDisplay(catVal));
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
     * 해당가게의 이름, 주소, 총 평균 가저옴
     */
    public StoreDTO findByStore(String name) {
        String sql = "SELECT name, adress, rating FROM store WHERE store_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        StoreDTO dto = null;

        try {
            conn = DBManager.conn();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            rs = ps.executeQuery();
            if (rs.next()) {
                dto = new StoreDTO();
                dto.setName(rs.getString("name"));
                dto.setAdress(rs.getString("address")); 
                dto.setRating(Integer.parseInt(getString("rating"))); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, ps, rs);
        }

        return dto;
    }
    
    
    
	private String getString(String string) {
		// TODO Auto-generated method stub
		return null;
	}

    
	
}
