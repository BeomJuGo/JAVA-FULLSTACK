package com.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import com.review.ReviewDAO;
import com.review.ReviewDTO;
import com.store.StoreDAO;

/**
 * Servlet implementation class Review
 */
@WebServlet("/Review.do")
@MultipartConfig
public class Review extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Review() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("/review.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		//review_add에서 가져오기
		
		String storeName = request.getParameter("name");
		String address = request.getParameter("address");
		String reviewText = request.getParameter("review");
		//평점
		int rating = 0;
		try {
			rating = Integer.parseInt(request.getParameter("rating"));
		} catch (Exception e) {
			rating = 0;
		}
		//사진경로
		Part picPart = null;
		try {
			picPart = request.getPart("pic");
		} catch (Exception e) {
			picPart = null;
		}

		
		String uploadedUrl = null;
		if (picPart != null && picPart.getSize() > 0) {
			String fileName = Paths.get(picPart.getSubmittedFileName()).getFileName().toString();
			if (!fileName.isEmpty()) {
				String uploadPath = getServletContext().getRealPath("/uploads");
				File uploadDir = new File(uploadPath);
				if (!uploadDir.exists()) {
					uploadDir.mkdirs();
				}
				File dest = new File(uploadDir, fileName);
				picPart.write(dest.getAbsolutePath());
				uploadedUrl = request.getContextPath() + "/uploads/" + fileName;
			}
		}

		HttpSession session = request.getSession(false);
		String userId = "익명";
		if (session != null && session.getAttribute("loginUser") != null) {
			try {
				Object loginUser = session.getAttribute("loginUser");
				userId = String.valueOf(loginUser.getClass().getMethod("getId").invoke(loginUser));
			} catch (Exception e) {
				userId = "익명";
			}
		}

		// DB 저장
		ReviewDTO dto = new ReviewDTO();
		dto.setUserId(userId);
		dto.setStoreName(storeName);
		dto.setReview(reviewText);
		dto.setRating(rating);
		dto.setReviewImg(uploadedUrl);
		ReviewDAO.getInstance().insert(dto);

		// DB 조회
		List<ReviewDTO> list = ReviewDAO.getInstance().findByStore(storeName);
		double avg = StoreDAO.getInstance().avgrating(storeName);

		request.setAttribute("store", new java.util.HashMap<String, Object>() {{
			put("name", storeName);
			put("address", address);
			put("avg", avg);
		}});
		request.setAttribute("reviews", list);
		request.getRequestDispatcher("/review.jsp").forward(request, response);
	}
}
