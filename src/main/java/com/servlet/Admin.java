package com.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.store.StoreDAO;
import com.store.StoreDTO;

/**
 * 관리자가 상점 데이터를 조회·추가·삭제할 수 있는 서블릿.
 * 
 * URL 패턴: /admin.do
 */
@WebServlet("/admin.do")
public class Admin extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Admin() {
        super();
    }

    /**
     * GET 요청 처리: 전체 상점 목록과 평균 평점을 조회하여 admin.jsp로 포워딩한다.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StoreDAO dao = StoreDAO.getInstance();
        List<StoreDTO> stores = dao.selectInfo();
        double avg = dao.avgrating();
        request.setAttribute("storeList", stores);
        request.setAttribute("avgRating", avg);
        request.getRequestDispatcher("/admin.jsp").forward(request, response);
    }

    /**
     * POST 요청 처리: 추가 또는 삭제 동작을 수행한 뒤 목록을 다시 로드한다.
     * 파라미터 "action" 값이 "delete"이면 삭제, 그 외는 추가로 간주한다.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        StoreDAO dao = StoreDAO.getInstance();
        String action = request.getParameter("action");
        if (action != null && action.equals("delete")) {
            // 삭제 처리: name 파라미터로 지정된 상점 삭제
            String name = request.getParameter("name");
            if (name != null && !name.trim().isEmpty()) {
                dao.deleteInfo(name.trim());
            }
        } else {
            // 삽입 처리: 모든 필수 필드를 받아 저장
            String name = request.getParameter("name");
            String adress = request.getParameter("adress");
            String ratingStr = request.getParameter("rating");
            String category = request.getParameter("category");
            if (name != null && adress != null && ratingStr != null && category != null
                    && !name.trim().isEmpty() && !adress.trim().isEmpty()) {
                try {
                    double rating = Double.parseDouble(ratingStr);
                    StoreDTO store = new StoreDTO();
                    store.setName(name.trim());
                    store.setAdress(adress.trim());
                    store.setRating(rating);
                    store.setCategory(category.trim());
                    dao.insertInfo(store);
                } catch (NumberFormatException e) {
                    // 평점 형식 오류는 무시하고 삽입하지 않음
                }
            }
        }
        // 변경 후 redirect하여 목록을 갱신한다.
        response.sendRedirect(request.getContextPath() + "/admin.do");
    }
}
