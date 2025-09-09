// src/main/java/com/store/StoreListServlet.java
package com.store;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/store/list.do")
public class StoreListServlet extends HttpServlet {
    private final StoreDAO dao = StoreDAO.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String category = req.getParameter("category"); // "일식","중식","양식","한식" 또는 null/빈문자열
        List<StoreDTO> list = (category == null || category.trim().isEmpty())
                ? dao.selectInfo()
                : dao.selectInfoByCategory(category.trim());

        // JSON 직렬화 (간단 수동)
        StringBuilder json = new StringBuilder();
        json.append("{\"items\":[");
        for (int i = 0; i < list.size(); i++) {
            StoreDTO s = list.get(i);
            if (i > 0) json.append(',');
            json.append('{')
                .append("\"name\":\"").append(escape(s.getName())).append("\",")
                .append("\"address\":\"").append(escape(s.getAddress())).append("\",")
                .append("\"rating\":").append(s.getRating()).append(',')
                .append("\"category\":\"").append(escape(s.getCategory())).append("\",")
                .append("\"latitude\":").append(s.getLatitude()).append(',')
                .append("\"longitude\":").append(s.getLongitude())
                .append('}');
        }
        json.append("]}");

        try (PrintWriter out = resp.getWriter()) {
            out.print(json.toString());
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        // 아주 단순한 JSON escape (따옴표/역슬래시/개행 등)
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
