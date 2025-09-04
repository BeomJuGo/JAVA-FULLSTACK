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
 * Servlet implementation class main
 */
@WebServlet("/main.do")
public class Main extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Main() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 카테고리 파라미터 확인: 있으면 해당 카테고리만 조회한다.
        StoreDAO dao = StoreDAO.getInstance();
        String category = request.getParameter("category");
        List<StoreDTO> stores;
        if (category != null && !category.trim().isEmpty()) {
            stores = dao.selectInfoByCategory(category.trim());
        } else {
            stores = dao.selectInfo();
        }
        double avg = dao.avgrating();
        // JSP에서 storeList로 사용하므로 해당 이름으로 설정한다.
        request.setAttribute("storeList", stores);
        request.setAttribute("avgRating", avg);
        // 포워딩
        request.getRequestDispatcher("/main.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        StoreDAO dao = StoreDAO.getInstance();
        String action = request.getParameter("action");
        // 삭제 요청인 경우 action=delete, 삭제할 이름 파라미터는 delName
        if (action != null && action.equals("delete")) {
            String delName = request.getParameter("name");
            if (delName != null && !delName.trim().isEmpty()) {
                dao.deleteInfo(delName.trim());
            }
        } else {
            // 기본적으로 삽입 처리
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
                    // 평점 파싱 실패 시 무시하고 넘어감
                }
            }
        }
        // 갱신된 목록을 보기 위해 redirect
        response.sendRedirect(request.getContextPath() + "/main.do");
	}

}
