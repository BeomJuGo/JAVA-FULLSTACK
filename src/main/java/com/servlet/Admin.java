package com.admin;

import com.member.MemberDAO;
import com.member.MemberDTO;
import com.store.StoreDAO;
import com.store.StoreDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin.do")
public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final MemberDAO memberDAO = new MemberDAO();
    private final StoreDAO storeDAO = StoreDAO.getInstance();

    // (옵션) 관리자 권한 체크 공용 메서드
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object obj = session.getAttribute("loginUser");
        if (!(obj instanceof MemberDTO)) return false;
        MemberDTO user = (MemberDTO) obj;
        return user.getUser_level() == 4; // 4 = 관리자
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // (옵션) 관리자만 접근
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }

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

        // (옵션) 관리자만 처리
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("deleteUser".equals(action)) {
            // 회원 삭제
            String userId = request.getParameter("userId");
            if (userId != null && !userId.isBlank()) {
                memberDAO.delete(userId);
            }

        } else if ("editUser".equals(action)) {
            // 회원 수정
            String userId = request.getParameter("userId");
            String name = request.getParameter("userName");
            String pwd = request.getParameter("userPwd"); // 비번 비우면 DAO에서 '변경 안 함' 처리하도록 권장
            String levelStr = request.getParameter("userLevel");
            int level = 1;
            try { level = Integer.parseInt(levelStr); } catch (Exception ignored) {}

            MemberDTO m = new MemberDTO(userId, pwd, name, level);
            memberDAO.update(m);

        } else if ("createUser".equals(action)) {
            // 회원 등록
            String userId = request.getParameter("userId");
            String name = request.getParameter("userName");
            String pwd = request.getParameter("userPwd");
            String levelStr = request.getParameter("userLevel");
            int level = 1;
            try { level = Integer.parseInt(levelStr); } catch (Exception ignored) {}

            MemberDTO m = new MemberDTO(userId, pwd, name, level);
            memberDAO.join(m);

        } else if ("deleteStore".equals(action)) {
            // 가게 삭제 (PK: name)
            String storeId = request.getParameter("storeId");
            if (storeId != null && !storeId.isBlank()) {
                storeDAO.deleteInfo(storeId);
            }

        } else if ("editStore".equals(action)) {
            // 가게 수정 (이름/주소/카테고리) — rating은 수정하지 않음
            String originalName = request.getParameter("originalName"); // 기존 이름
            String name = request.getParameter("name");                 // 변경할 이름
            String address = request.getParameter("address");
            String category = request.getParameter("category");         // "일식","중식","양식","한식"

            StoreDTO s = new StoreDTO();
            s.setName(name);
            s.setAddress(address);
            s.setCategory(category);

            if (originalName != null && !originalName.isBlank()) {
                storeDAO.updateInfo(originalName, s);
            }

        } else if ("createStore".equals(action)) {
            // 가게 등록 (rating은 기본 0.0, category는 문자열)
            String name = request.getParameter("name");
            String address = request.getParameter("address");
            String category = request.getParameter("category");         // "일식","중식","양식","한식"

            StoreDTO s = new StoreDTO();
            s.setName(name);
            s.setAddress(address);
            s.setCategory(category);
            s.setRating(0.0); // 기본값

            storeDAO.insertInfo(s);
        }

        // 다시 목록으로 리다이렉트
        response.sendRedirect("admin.do");
    }
}
