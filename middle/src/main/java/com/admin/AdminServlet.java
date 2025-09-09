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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/admin.do")
public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // 프로젝트 구조에 맞게 사용
    private final MemberDAO memberDAO = new MemberDAO();
    private final StoreDAO  storeDAO  = StoreDAO.getInstance();

    // 관리자 권한 체크
    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object obj = session.getAttribute("loginUser");
        if (!(obj instanceof MemberDTO)) return false;
        MemberDTO user = (MemberDTO) obj;
        return user.getUser_level() == 4; // 4 = 관리자
    }

    // 주소를 위도/경도로 변환 (Naver Geocoding API)
    private double[] geocode(String address) {
        HttpURLConnection conn = null;
        BufferedReader br = null;
        try {
            String query = URLEncoder.encode(address, StandardCharsets.UTF_8);
            URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + query);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // 네이버 클라우드 플랫폼에서 발급받은 키로 교체 필요
            conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "YOUR_CLIENT_ID");
            conn.setRequestProperty("X-NCP-APIGW-API-KEY", "YOUR_CLIENT_SECRET");

            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            String json = sb.toString();

            int idxX = json.indexOf("\"x\":\"");
            int idxY = json.indexOf("\"y\":\"");
            if (idxX == -1 || idxY == -1) return null;
            String xs = json.substring(idxX + 5, json.indexOf('"', idxX + 5));
            String ys = json.substring(idxY + 5, json.indexOf('"', idxY + 5));
            return new double[]{Double.parseDouble(ys), Double.parseDouble(xs)}; // lat, lon
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (br != null) br.close(); } catch (Exception ignore) {}
            if (conn != null) conn.disconnect();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }

        // 회원/가게 목록 채워서 JSP로 포워드
        try {
            List<MemberDTO> userList  = memberDAO.findAll();     // 구현되어 있어야 함
            List<StoreDTO>  storeList = storeDAO.selectInfo();   // 형님 프로젝트 메서드

            // JSP에서 어떤 이름을 쓰든 대응되게 둘 다 세팅
            request.setAttribute("userList", userList);
            request.setAttribute("users",    userList);
            request.setAttribute("storeList", storeList);
            request.setAttribute("stores",    storeList);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("userList", java.util.Collections.emptyList());
            request.setAttribute("storeList", java.util.Collections.emptyList());
        }

        request.getRequestDispatcher("/admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/main.jsp");
            return;
        }

        String action = request.getParameter("action");
        try {
            if ("deleteUser".equals(action)) {
                String userId = request.getParameter("userId");
                if (userId != null && !userId.isBlank()) {
                    memberDAO.delete(userId);
                }

            } else if ("editUser".equals(action)) {
                String userId = request.getParameter("userId");
                String name   = request.getParameter("userName");
                String pwd    = request.getParameter("userPwd");    // 비워오면 DAO에서 '변경 없음' 처리 권장
                String levelStr = request.getParameter("userLevel");
                int level = 1;
                try { level = Integer.parseInt(levelStr); } catch (Exception ignored) {}

                MemberDTO m = new MemberDTO();
                m.setId(userId);
                m.setUser_name(name);
                m.setPwd(pwd);
                m.setUser_level(level);
                memberDAO.update(m);

            } else if ("createUser".equals(action)) {
                String userId = request.getParameter("userId");
                String name   = request.getParameter("userName");
                String pwd    = request.getParameter("userPwd");
                String levelStr = request.getParameter("userLevel");
                int level = 1;
                try { level = Integer.parseInt(levelStr); } catch (Exception ignored) {}

                MemberDTO m = new MemberDTO();
                m.setId(userId);
                m.setUser_name(name);
                m.setPwd(pwd);
                m.setUser_level(level);
                memberDAO.join(m);

            } else if ("deleteStore".equals(action)) {
                // 우리 프로젝트는 name을 PK처럼 사용. 혹시 파라미터명이 storeId로 올 수도 있어 둘 다 허용
                String name = request.getParameter("name");
                if (name == null || name.isBlank()) {
                    name = request.getParameter("storeId");
                }
                if (name != null && !name.isBlank()) {
                    storeDAO.deleteInfo(name);
                }

            } else if ("editStore".equals(action)) {
                String originalName = request.getParameter("originalName"); // 기존 이름
                String name    = request.getParameter("name");              // 변경할 이름
                String address = request.getParameter("address");
                String category= request.getParameter("category");          // "일식","중식","양식","한식"

                StoreDTO s = new StoreDTO();
                s.setName(name);
                s.setAddress(address);
                s.setCategory(category);
                double[] coord = geocode(address);
                if (coord != null) {
                    s.setLatitude(coord[0]);
                    s.setLongitude(coord[1]);
                }

                if (originalName != null && !originalName.isBlank()) {
                    storeDAO.updateInfo(originalName, s);
                }

            } else if ("createStore".equals(action)) {
                String name    = request.getParameter("name");
                String address = request.getParameter("address");
                String category= request.getParameter("category");

                StoreDTO s = new StoreDTO();
                s.setName(name);
                s.setAddress(address);
                s.setCategory(category);
                s.setRating(0.0); // 기본값
                double[] coord = geocode(address);
                if (coord != null) {
                    s.setLatitude(coord[0]);
                    s.setLongitude(coord[1]);
                }
                storeDAO.insertInfo(s);
            }
        } catch (Exception e) {
            e.printStackTrace(); // 필요 시 에러 메시지를 request/session에 남겨도 됨
        }

        // 목록 다시 보기
        response.sendRedirect(request.getContextPath() + "/admin.do");
    }
}
