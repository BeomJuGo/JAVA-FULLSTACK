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

@WebServlet("/main.do")
public class Main extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Main() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        StoreDAO dao = StoreDAO.getInstance();
        String category = request.getParameter("category");
        List<StoreDTO> stores;

        if (category != null && !category.trim().isEmpty()) {
            stores = dao.selectInfoByCategory(category.trim());
        } else {
            stores = dao.selectInfo();
        }

        request.setAttribute("storeList", stores);

        request.getRequestDispatcher("/main.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        StoreDAO dao = StoreDAO.getInstance();
        String action = request.getParameter("action");

        if ("delete".equals(action)) {
            String delName = request.getParameter("name");
            if (delName != null && !delName.trim().isEmpty()) {
                dao.deleteInfo(delName.trim());
            }
        } else {
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
                    // 평점 파싱 실패 무시
                }
            }
        }
        response.sendRedirect(request.getContextPath() + "/main.do");
    }
}
