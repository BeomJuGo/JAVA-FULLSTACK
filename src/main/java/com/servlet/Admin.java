package com.admin;

import com.member.MemberDAO;
import com.member.MemberDTO;
import com.store.StoreDAO;
import com.store.StoreDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin.do")
public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private MemberDAO memberDAO = new MemberDAO();
    private StoreDAO storeDAO = StoreDAO.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 회원 목록
        List<MemberDTO> users = memberDAO.findAll();
        request.setAttribute("users", users);

        // 가게 목록
        List<StoreDTO> stores = storeDAO.selectInfo();
        request.setAttribute("stores", stores);

        // admin.jsp로 포워딩
        request.getRequestDispatcher("admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        // ---------- 회원 관련 ----------
        if ("deleteUser".equals(action)) {
            String userId = request.getParameter("userId");
            memberDAO.delete(userId);

        } else if ("editUser".equals(action)) {
            String userId = request.getParameter("userId");
            String name = request.getParameter("userName");
            String pwd = request.getParameter("userPwd");
            int level = Integer.parseInt(request.getParameter("userLevel"));
            MemberDTO m = new MemberDTO(userId, pwd, name, level);
            memberDAO.update(m);

        } else if ("createUser".equals(action)) {
            String userId = request.getParameter("userId");
            String name = request.getParameter("userName");
            String pwd = request.getParameter("userPwd");
            int level = Integer.parseInt(request.getParameter("userLevel"));
            MemberDTO m = new MemberDTO(userId, pwd, name, level);
            memberDAO.join(m);
        }

        // ---------- 가게 관련 ----------
        else if ("createStore".equals(action)) {
            String name = request.getParameter("storeName");
            String adress = request.getParameter("storeAdress");
            double rating = Double.parseDouble(request.getParameter("storeRating"));
            String category = request.getParameter("storeCategory");

            StoreDTO store = new StoreDTO();
            store.setName(name);
            store.setAdress(adress);
            store.setRating(rating);
            store.setCategory(category);

            storeDAO.insertInfo(store);

        } else if ("deleteStore".equals(action)) {
            String storeName = request.getParameter("storeName");
            storeDAO.deleteInfo(storeName);

        } else if ("editStore".equals(action)) {
            String name = request.getParameter("storeName");
            String adress = request.getParameter("storeAdress");
            double rating = Double.parseDouble(request.getParameter("storeRating"));
            String category = request.getParameter("storeCategory");

            StoreDTO store = new StoreDTO();
            store.setName(name);
            store.setAdress(adress);
            store.setRating(rating);
            store.setCategory(category);

            // update 메서드 필요 (DAO에 추가해야 함)
            storeDAO.updateInfo(store);
        } 

        // 다시 목록으로 리다이렉트
        response.sendRedirect("admin.do");
    }
}
