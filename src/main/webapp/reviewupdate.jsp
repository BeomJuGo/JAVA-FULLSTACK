<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>리뷰 수정</title>
<style>
  body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial;font-size:14px}
  .wrap{max-width:720px;margin:24px auto}
  label{display:block;margin:10px 0 4px}
  input,select,textarea{width:100%;padding:8px;border:1px solid #333}
  textarea{min-height:140px;resize:vertical}
  .row{display:grid;grid-template-columns:1fr 1fr;gap:12px}
  .btn{margin-top:16px;padding:8px 12px;border:1px solid #333;background:#fff;cursor:pointer;text-decoration:none;color:#000;display:inline-block}
  .btn:hover{background:#f6f6f6}
  .readonly{background:#f8f8f8}
</style>
</head>
<body>
<div class="wrap">
  <h1>리뷰 수정</h1>

  <!-- 권한 가드: 로그인 + (작성자거나 ADMIN) -->
  <c:choose>
    <c:when test="${not empty sessionScope.loginUser 
                   and (review.userId eq sessionScope.loginUser.id 
                        or sessionScope.loginUser.role eq 'ADMIN')}">

      <form method="post" action="${pageContext.request.contextPath}/review/update">
        <!-- 라우팅에 필요한 값들 -->
        <input type="hidden" name="id" value="${review.id}">
        <input type="hidden" name="storeId" value="${param.storeId}">
        <input type="hidden" name="page" value="${param.page}">

        <div class="row">
          <div>
            <label>작성자</label>
            <input class="readonly" type="text" value="${review.userId}" readonly>
          </div>
          <div>
            <label>평점 (1~5)</label>
            <select name="rating" required>
              <c:forEach var="n" begin="1" end="5">
                <option value="${n}" ${review.rating==n ? 'selected' : ''}>${n}</option>
              </c:forEach>
            </select>
          </div>
        </div>

        <label>내용</label>
        <textarea name="content" maxlength="1000" required>${review.review}</textarea>

        <button class="btn" type="submit">저장</button>
        <a class="btn" href="${pageContext.request.contextPath}/storeDetail.jsp?id=${param.storeId}&page=${param.page}">취소</a>
      </form>

    </c:when>
    <c:otherwise>
      <p>권한이 없습니다.</p>
      <a class="btn" href="${pageContext.request.contextPath}/main.jsp">메인으로</a>
    </c:otherwise>
  </c:choose>
</div>
</body>
</html>
