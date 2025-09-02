<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<title>관리자 페이지</title>
<style>
  body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial;font-size:14px}
  .wrap{max-width:1100px;margin:24px auto;padding:8px}
  h2{margin:24px 0 8px}
  table{width:100%;border-collapse:collapse}
  th,td{border:1px solid #333;padding:8px}
  th{background:#f2f2f2;text-align:left}
  .toolbar{display:flex;gap:8px;justify-content:flex-end;margin:8px 0 16px}
  .btn{border:1px solid #333;background:#fff;padding:4px 10px;cursor:pointer;text-decoration:none;color:#000}
  .btn:hover{background:#f6f6f6}
  .empty{padding:16px;color:#555}
</style>
</head>
<body>
<div class="wrap">
  <h1>관리자 페이지</h1>

  <!-- ========== 가게정보 섹션 ========== -->
  <section>
    <h2>가게정보</h2>
    <div class="toolbar">
      <a class="btn" href="${pageContext.request.contextPath}/storeCreate.jsp">가게 등록</a>
      <a class="btn" href="${pageContext.request.contextPath}/main.jsp">메인으로</a>
    </div>

    <c:choose>
      <c:when test="${not empty stores}">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>가게명</th>
              <th>주소</th>
              <th>연락처</th>
              <th>평균 평점</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="s" items="${stores}">
              <tr>
                <td><c:out value="${s.id}"/></td>
                <td><c:out value="${s.name}"/></td>
                <td><c:out value="${s.address}"/></td>
                <td><c:out value="${s.phone}"/></td>
                <td><c:out value="${s.avg}"/></td>
                <td>
                  <a class="btn" href="${pageContext.request.contextPath}/storeupdate.jsp?storeId=${s.id}">수정</a>
                  <!-- 필요 시 삭제 버튼/링크 추가 -->
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <div class="empty">등록된 가게가 없습니다.</div>
      </c:otherwise>
    </c:choose>
  </section>

  <!-- ========== 유저정보 섹션 ========== -->
  <section>
    <h2 style="margin-top:32px;">유저정보</h2>
    <div class="toolbar">
      <a class="btn" href="${pageContext.request.contextPath}/userCreate.jsp">유저 등록</a>
    </div>

    <c:choose>
      <c:when test="${not empty users}">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>아이디(닉네임)</th>
              <th>이메일</th>
              <th>권한</th>
              <th>상태</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="u" items="${users}">
              <tr>
                <td><c:out value="${u.id}"/></td>
                <td><c:out value="${u.username}"/></td>
                <td><c:out value="${u.email}"/></td>
                <td><c:out value="${u.role}"/></td>
                <td><c:out value="${u.status}"/></td>
                <td>
                  <a class="btn" href="${pageContext.request.contextPath}/userupdate.jsp?userId=${u.id}">수정</a>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </c:when>
      <c:otherwise>
        <div class="empty">등록된 유저가 없습니다.</div>
      </c:otherwise>
    </c:choose>
  </section>
</div>
</body>
</html>
